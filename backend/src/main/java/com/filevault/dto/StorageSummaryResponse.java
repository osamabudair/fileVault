package com.filevault.dto;

import com.filevault.entity.FileCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageSummaryResponse {

    private long usedBytes;
    private long maxBytes;
    private String formattedUsed;
    private String formattedMax;
    private double usagePercentage;
    private Map<FileCategory, CategoryBreakdown> breakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private long count;
        private long sizeBytes;
        private String formattedSize;
    }
}
