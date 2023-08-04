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
import java.util.stream.Collectors;

public class DatabaseSaver {

    private final SessionFactory sessionFactory;

    private final Map<String, Object> SETTINGS = Map.of(
            Environment.DRIVER, "org.hsqldb.jdbc.JDBCDriver",
            Environment.USER, "SA",
            Environment.PASS, "",
            Environment.DIALECT, "org.hibernate.dialect.HSQLDialect",
            Environment.SHOW_SQL, "true",
            Environment.HBM2DDL_AUTO, "create-drop"
    );

    public DatabaseSaver(String databaseFileName) {

        Map<String, Object> settings = new HashMap<>(SETTINGS);

        String databaseFilePath = System.getProperty("user.dir") + "/.index/";

        initDatabaseStorage(databaseFilePath);

        settings.put(Environment.URL, "jdbc:hsqldb:file:" + databaseFilePath + databaseFileName + ";shutdown=true");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();

        MetadataSources metadataSources = new MetadataSources(registry);
        metadataSources.addAnnotatedClass(ScannedFile.class);

        sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
    }

    private static void initDatabaseStorage(String databaseFilePath) {
        Path path = Paths.get(databaseFilePath);
        if (!java.nio.file.Files.exists(path)) {
            try {
                java.nio.file.Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveScannedFiles(List<ScannedFile> scannedFiles) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            for (ScannedFile file : scannedFiles) {
                session.save(file);
            }
            tx.commit();
        }
    }

    public void updateHashForDuplicates() {
        try (Session session = sessionFactory.openSession()) {
            List<ScannedFile> scannedFiles = session.createQuery("from ScannedFile where md5 is null", ScannedFile.class).list();

            Map<String, List<ScannedFile>> filesByBaseName = scannedFiles.stream()
                    .collect(Collectors.groupingBy(ScannedFile::getBaseName));

            Transaction tx = session.beginTransaction();
            filesByBaseName.forEach((baseName, files) -> {
                if (files.size() > 1) {
                    files.forEach(file -> {
                        try {
                            String hash = file.computeFileHash();
                            file.setMd5(hash);
                            session.update(file);
                            System.out.println("Updated hash for " + file.getFilePath());
                        } catch (IOException e) {
                            e.printStackTrace();
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
