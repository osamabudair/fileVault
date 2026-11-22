package com.filevault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.filevault.entity.FileCategory;
import com.filevault.entity.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private UUID id;
    private String name;
    private long sizeBytes;
    private long size;
    private String formattedSize;
    private String category;
    private FileCategory fileCategory;
    private String type;
    private String contentType;
    private boolean favorite;
    private boolean trashed;
    private boolean inVault;
    private UUID folderId;
    private String folderName;
    private int sharedCount;
    private boolean hasPublicLink;
    private List<String> sharedWith;
    private Instant trashedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String downloadUrl;
    private String previewUrl;

    @JsonProperty("isFavorite")
    public boolean isFavoriteProperty() {
        return favorite;
    }

    @JsonProperty("isTrash")
    public boolean isTrashProperty() {
        return trashed;
    }

    @JsonProperty("isShared")
    public boolean isSharedProperty() {
        return hasPublicLink || sharedCount > 0;
    }

    public static FileResponse fromEntity(FileMetadata entity) {
        return fromEntity(entity, 0, false, List.of());
    }

    public static FileResponse fromEntity(
        FileMetadata entity,
        int sharedCount,
        boolean hasPublicLink,
        List<String> sharedWithNames
    ) {
        String lowerCategory = entity.getCategory().name().toLowerCase(Locale.ROOT);
        String idStr = entity.getId().toString();
        return FileResponse.builder()
            .id(entity.getId())
            .name(entity.getOriginalName())
            .sizeBytes(entity.getSizeBytes())
            .size(entity.getSizeBytes())
            .formattedSize(formatSize(entity.getSizeBytes()))
            .category(lowerCategory)
            .fileCategory(entity.getCategory())
            .type(entity.getContentType())
            .contentType(entity.getContentType())
            .favorite(entity.isFavorite())
            .trashed(entity.isTrashed())
            .inVault(entity.isInVault())
            .folderId(entity.getFolder() != null ? entity.getFolder().getId() : null)
            .folderName(entity.getFolder() != null ? entity.getFolder().getName() : null)
            .sharedCount(sharedCount)
            .hasPublicLink(hasPublicLink)
            .sharedWith(sharedWithNames != null ? sharedWithNames : List.of())
            .trashedAt(entity.getTrashedAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .downloadUrl("/api/v1/files/" + idStr + "/download")
            .previewUrl("/api/v1/files/" + idStr + "/preview")
            .build();
    }

    public static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = Math.min((int) (Math.log10(bytes) / Math.log10(1024)), units.length - 1);
        double value = bytes / Math.pow(1024, digitGroups);
        if (digitGroups == 0) {
            return bytes + " B";
        }
        return String.format(Locale.US, "%.1f %s", value, units[digitGroups]);
    }
}
