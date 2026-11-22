package com.filevault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderRequest {

    @NotBlank(message = "Folder name is required")
    @Size(min = 1, max = 120, message = "Folder name must be between 1 and 120 characters")
    private String name;

    private UUID parentId;
}
