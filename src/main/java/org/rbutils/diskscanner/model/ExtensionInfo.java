package org.rbutils.diskscanner.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExtensionInfo {
    private String extension;
    private long count;
    private long totalSize;
}
