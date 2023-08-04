package org.rbutils.diskscanner;

import org.rbutils.diskscanner.model.ScannedFile;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class TreeWalker {

    private final Path startPath;

    private static final List<String> IGNORE_LIST = List.of("cpi", "db", "bin", "bdm", "bnp", "inp", "int", "mpl", "xml");

    public TreeWalker(Path startPath) {
        this.startPath = startPath;
    }

    public List<ScannedFile> walkTree() throws IOException {
        List<ScannedFile> files = new ArrayList<>();
        Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    ScannedFile scannedFile = createScannedFile(file, attrs);
                    if (!scannedFile.getExtension().isEmpty() && !IGNORE_LIST.contains(scannedFile.getExtension())) {
                        files.add(scannedFile);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        return files;
    }

    private ScannedFile createScannedFile(Path file, BasicFileAttributes attrs) {
        int i = file.getFileName().toString().lastIndexOf('.');
        String extension = (i > 0) ? file.getFileName().toString().substring(i + 1) : "";

        return ScannedFile.builder()
                .filePath(file.toString())
                .baseName(file.getFileName().toString().toLowerCase())
                .extension(extension.toLowerCase())
                .size(attrs.size())
                .lastModifiedTime(attrs.lastModifiedTime().toMillis())
                .creationTime(attrs.creationTime().toMillis())
                .lastAccessTime(attrs.lastAccessTime().toMillis())
                .build();
    }
}
