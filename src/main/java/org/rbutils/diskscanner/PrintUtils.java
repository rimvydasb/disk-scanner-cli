package org.rbutils.diskscanner;

import org.rbutils.diskscanner.model.ExtensionInfo;

import java.util.List;

public class PrintUtils {
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
