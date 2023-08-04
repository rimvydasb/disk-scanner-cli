package org.rbutils.diskscanner;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class FileUtils {
    public static String compute10MbFileHash(String path, String filename) throws IOException {

        String fullPath = Paths.get(path, filename).toString();

        final int bufferSize = 10 * 1024 * 1024;
        byte[] buffer = new byte[bufferSize];

        try (FileInputStream fis = new FileInputStream(fullPath)) {
            int bytesRead = fis.read(buffer);
            if (bytesRead > 0) {
                HashFunction hashFunction = Hashing.murmur3_128();
                long hash = hashFunction.hashBytes(buffer, 0, bytesRead).asLong();
                return Long.toString(hash);
            } else {
                throw new IOException("Failed to read the file");
            }
        }
    }
}
