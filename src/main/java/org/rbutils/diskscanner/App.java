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
import java.util.logging.Level;
import java.util.logging.Logger;

@Command(name = "diskscanner", mixinStandardHelpOptions = true, version = "1.0", description = "Scans disk and provides information.")
public class App implements Callable<Integer> {
    @CommandLine.Option(names = {"-scanPath"}, description = "The start path for scanning.")
    private Path scanPath;

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws IOException {
        String databaseName = scanPath.toString().replaceAll("[^a-zA-Z0-9]", "");

        Stopwatch stopwatch = Stopwatch.createStarted();

        var treeWalker = new TreeWalker(scanPath);

        List<ScannedFile> scannedFiles = treeWalker.walkTree();

        scannedFiles.forEach(file -> logger.log(Level.INFO, file.toString()));

        var databaseSaver = new DatabaseSaver(databaseName);
        databaseSaver.saveScannedFiles(scannedFiles);
        //databaseSaver.updateHashForDuplicates();

        long timeSpent = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.log(Level.INFO, "Time spent: " + timeSpent + " milliseconds");

        PrintUtils.printExtensionsTable(databaseSaver.fetchInfo());

        return 0;
    }
}
