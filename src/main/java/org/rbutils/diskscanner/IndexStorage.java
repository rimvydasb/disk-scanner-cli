package org.rbutils.diskscanner;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.rbutils.diskscanner.model.ExtensionInfo;
import org.rbutils.diskscanner.model.ScannedFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class IndexStorage {

    private static final Logger logger = LoggerFactory.getLogger(IndexStorage.class);

    private final SessionFactory sessionFactory;

    private static final Map<String, Object> SETTINGS = Map.of(
            Environment.DRIVER, "org.hsqldb.jdbc.JDBCDriver",
            Environment.USER, "SA",
            Environment.PASS, "",
            Environment.DIALECT, "org.hibernate.dialect.HSQLDialect",
            Environment.SHOW_SQL, "false",
            Environment.HBM2DDL_AUTO, "create-drop"
    );

    public IndexStorage(String databaseFileName, boolean resetDatabase) {
        Map<String, Object> settings = new HashMap<>(SETTINGS);

        String databaseFilePath = System.getProperty("user.dir") + "/.index/";

        initDatabaseStorage(databaseFilePath);

        settings.put(Environment.URL, "jdbc:hsqldb:file:" + databaseFilePath + databaseFileName + ";shutdown=true");

        if (!resetDatabase) {
            settings.put(Environment.HBM2DDL_AUTO, "update");
        }

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();

        MetadataSources metadataSources = new MetadataSources(registry);
        metadataSources.addAnnotatedClass(ScannedFile.class);

        sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
    }

    public long getCount() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select count(*) from ScannedFile", Long.class).uniqueResult();
        }
    }

    private static void initDatabaseStorage(String databaseFilePath) {
        Path path = Paths.get(databaseFilePath);
        if (!java.nio.file.Files.exists(path)) {
            try {
                java.nio.file.Files.createDirectory(path);
            } catch (IOException e) {
                logger.error("Error creating directory: " + path, e);
            }
        }
    }

    public void saveScannedFiles(List<ScannedFile> scannedFiles) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            for (ScannedFile file : scannedFiles) {
                session.merge(file);
            }
            tx.commit();
        }
    }

    public void updateHashForDuplicates() {
        try (Session session = sessionFactory.openSession()) {
            List<ScannedFile> scannedFiles = session.createQuery("from ScannedFile where hash10Mb is null", ScannedFile.class).list();

            Map<Long, List<ScannedFile>> filesByBaseName = scannedFiles.stream()
                    .collect(Collectors.groupingBy(ScannedFile::getSize));

            logger.info("Found " + scannedFiles.size() + " files without hash including " + filesByBaseName.size() + " with unique sizes");

            Transaction tx = session.beginTransaction();
            filesByBaseName.forEach((size, files) -> {
                if (files.size() > 1) {
                    files.forEach(file -> {
                        try {
                            String hash = FileUtils.compute10MbFileHash(file.getFilePath(), file.getBaseName());
                            file.setHash10Mb(hash);
                            session.merge(file);
                            logger.debug("Updated hash for " + file.getFilePath());
                        } catch (IOException e) {
                            logger.error("Failed to update hash for " + file.getFilePath(), e);
                        }
                    });
                }
            });
            tx.commit();
        }
    }

    public List<ExtensionInfo> fetchInfo() {
        String query = "SELECT NEW org.rbutils.diskscanner.model.ExtensionInfo(s.extension, COUNT(s.extension), SUM(s.size)) " +
                "FROM ScannedFile s " +
                "GROUP BY s.extension " +
                "ORDER BY COUNT(s.extension) DESC";

        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(query, ExtensionInfo.class).getResultList();
        }
    }
}
