package org.rbutils.diskscanner;

import com.google.common.base.Stopwatch;
import org.rbutils.diskscanner.model.ScannedFile;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "diskscanner", mixinStandardHelpOptions = true, version = "1.0", description = "Scans disk and provides information.")
public class App implements Callable<Integer> {
    @CommandLine.Option(names = {"-scanPath"}, description = "The start path for scanning.")
    private Path scanPath;

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @CommandLine.Option(names = {"-resetIndex"}, description = "Reset collected indexes.")
    private boolean resetDatabase = false;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws IOException {
        String databaseName = scanPath.toString().replaceAll("[^a-zA-Z0-9]", "");

        logger.info("Using database: " + databaseName);

        Stopwatch stopwatch = Stopwatch.createStarted();

        var databaseSaver = new IndexStorage(databaseName, resetDatabase);

        if (databaseSaver.getCount() == 0) {

            var treeWalker = new FileTreeWalker(scanPath);

            List<ScannedFile> scannedFiles = treeWalker.walkTree();

            scannedFiles.forEach(file -> logger.debug(file.toString()));

            logger.info("Index count: " + databaseSaver.getCount());

            databaseSaver.saveScannedFiles(scannedFiles);
            databaseSaver.updateHashForDuplicates();
        }

        long timeSpent = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info("Time spent: " + timeSpent + " milliseconds");

        logger.info("Index count: " + databaseSaver.getCount());
        PrintUtils.printExtensionsTable(databaseSaver.fetchInfo());

        return 0;
    }
}
