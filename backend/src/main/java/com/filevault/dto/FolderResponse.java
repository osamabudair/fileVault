package com.filevault.dto;

import com.filevault.entity.Folder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponse {

    private UUID id;
    private String name;
    private UUID parentId;
    private String parentName;
    private boolean inVault;
    private long itemCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static FolderResponse fromEntity(Folder folder, long itemCount) {
        return FolderResponse.builder()
            .id(folder.getId())
            .name(folder.getName())
            .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
            .parentName(folder.getParent() != null ? folder.getParent().getName() : null)
            .inVault(folder.isInVault())
            .itemCount(itemCount)
            .createdAt(folder.getCreatedAt())
            .updatedAt(folder.getUpdatedAt())
            .build();
    }
}
