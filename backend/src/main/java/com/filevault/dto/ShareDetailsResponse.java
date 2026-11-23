package com.filevault.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareDetailsResponse {

    private UUID fileId;
    private String fileName;
    private String shareToken;
    private String shareUrl;
    private boolean active;
    private Instant expiresAt;
    private List<CollaboratorSummary> collaborators;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaboratorSummary {
        private UUID id;
        private UUID userId;
        private String fullName;
        private String email;
        private String permission;
        private Instant sharedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareLinkRequest {
        private Integer expiresInHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteRequest {
        @NotBlank(message = "Collaborator email is required")
        @Email(message = "Please enter a valid email address")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublicFileResponse {
        private String name;
        private long sizeBytes;
        private String formattedSize;
        private String category;
        private String contentType;
        private Instant updatedAt;
        private String previewUrl;
        private String downloadUrl;
    }
}
