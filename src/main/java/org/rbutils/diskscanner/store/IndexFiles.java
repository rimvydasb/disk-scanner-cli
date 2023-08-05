package org.rbutils.diskscanner.store;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IndexFiles {

    private static final Logger logger = LoggerFactory.getLogger(IndexFiles.class);

    @Getter
    private final String databaseFileName;
    private final String databaseFilePath;
    private final File propertiesFile;
    private final File scriptFile;
    @Getter
    private final String hsqlFile;

    public IndexFiles(String databaseFileName) {
        this.databaseFileName = databaseFileName;
        this.databaseFilePath = System.getProperty("user.dir") + "/.index/";
        this.hsqlFile = this.databaseFilePath + databaseFileName;
        this.propertiesFile = new File(databaseFilePath + databaseFileName + ".properties");
        this.scriptFile = new File(databaseFilePath + databaseFileName + ".script");
        initDatabaseStorage();
    }

    public boolean exists() {
        return propertiesFile.exists() && scriptFile.exists();
    }

    private void initDatabaseStorage() {
        Path path = Paths.get(databaseFilePath);
        if (!java.nio.file.Files.exists(path)) {
            try {
                java.nio.file.Files.createDirectory(path);
            } catch (IOException e) {
                logger.error("Error creating directory: " + path, e);
            }
        }
    }
}
