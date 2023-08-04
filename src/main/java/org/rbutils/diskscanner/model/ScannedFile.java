package org.rbutils.diskscanner.model;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.*;

import jakarta.persistence.*;

import java.io.FileInputStream;
import java.io.IOException;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScannedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filePath;
    private String baseName;
    private String extension;
    private long size;
    private long lastModifiedTime;
    private long creationTime;
    private long lastAccessTime;
    private String md5;
    private String sha1;

    public String computeFileHash() throws IOException {
        final int bufferSize = 10 * 1024 * 1024;
        byte[] buffer = new byte[bufferSize];

        try (FileInputStream fis = new FileInputStream(this.getFilePath())) {
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
