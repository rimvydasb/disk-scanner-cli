package org.rbutils.diskscanner;

import org.rbutils.diskscanner.model.ExtensionInfo;
import org.rbutils.diskscanner.model.ScannedFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException {
        Path startPath = Paths.get("D:/Photos/");

        String databaseName = startPath.toString().replaceAll("[^a-zA-Z0-9]", "");

        long startTime = System.currentTimeMillis();

        var treeWalker = new TreeWalker(startPath);

        List<ScannedFile> scannedFiles = treeWalker.walkTree();

        long endTime = System.currentTimeMillis();

        scannedFiles.forEach(System.out::println);

        var databaseSaver = new DatabaseSaver(databaseName);
        databaseSaver.saveScannedFiles(scannedFiles);
        //databaseSaver.updateHashForDuplicates();

        long timeSpent = endTime - startTime; // Calculate the time spent
        System.out.println("Time spent: " + timeSpent + " milliseconds"); // Print the time spent

        printExtensionsTable(databaseSaver.fetchInfo());
    }

    public static void printExtensionsTable(List<ExtensionInfo> extensionInfoList) {
        String headerFormat = "| %-15s | %-10s | %-15s |%n";
        String rowFormat = "| %-15s | %-10d | %-15d |%n";

        System.out.printf(headerFormat, "Extension", "Count", "Size (MB)");
        System.out.println("|-----------------|------------|-----------------|");

        for (ExtensionInfo info : extensionInfoList) {
            long totalSizeMB = info.getTotalSize() / 1024 / 1024; // converting size to MB
            System.out.printf(rowFormat, info.getExtension(), info.getCount(), totalSizeMB);
        }
    }
}
