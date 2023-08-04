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
    private String hash10Mb;
    private String fullHash;
}
