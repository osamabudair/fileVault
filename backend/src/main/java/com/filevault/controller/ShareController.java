package com.filevault.controller;

import com.filevault.dto.FileResponse;
import com.filevault.dto.ShareDetailsResponse;
import com.filevault.entity.FileMetadata;
import com.filevault.service.FileService;
import com.filevault.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/api/v1/files/{id}/share")
    public ResponseEntity<ShareDetailsResponse> getShareDetails(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(shareService.getShareDetails(id));
    }

    @PostMapping("/api/v1/files/{id}/share/link")
    public ResponseEntity<ShareDetailsResponse> createOrEnableShareLink(
        @PathVariable("id") UUID id,
        @RequestBody(required = false) ShareDetailsResponse.ShareLinkRequest request
    ) {
        Integer expiresInHours = (request != null) ? request.getExpiresInHours() : null;
        return ResponseEntity.ok(shareService.createOrEnableShareLink(id, expiresInHours));
    }

    @DeleteMapping("/api/v1/files/{id}/share/link")
    public ResponseEntity<ShareDetailsResponse> disableShareLink(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(shareService.disableShareLink(id));
    }

    @PostMapping("/api/v1/files/{id}/share/invite")
    public ResponseEntity<ShareDetailsResponse> inviteCollaborator(
        @PathVariable("id") UUID id,
        @Valid @RequestBody ShareDetailsResponse.InviteRequest request
    ) {
        return ResponseEntity.ok(shareService.inviteCollaborator(id, request.getEmail()));
    }

    @DeleteMapping("/api/v1/files/{id}/share/collaborators/{collaboratorId}")
    public ResponseEntity<ShareDetailsResponse> revokeCollaborator(
        @PathVariable("id") UUID id,
        @PathVariable("collaboratorId") UUID collaboratorId
    ) {
        return ResponseEntity.ok(shareService.revokeCollaborator(id, collaboratorId));
    }

    @GetMapping("/api/v1/files/shared-with-me")
    public ResponseEntity<List<FileResponse>> listFilesSharedWithMe() {
        return ResponseEntity.ok(shareService.listFilesSharedWithCurrentUser());
    }

    @GetMapping("/api/v1/public/share/{shareToken}")
    public ResponseEntity<ShareDetailsResponse.PublicFileResponse> getPublicShareMetadata(
        @PathVariable("shareToken") String shareToken
    ) {
        return ResponseEntity.ok(shareService.getPublicShareMetadata(shareToken));
    }

    @GetMapping("/api/v1/public/share/{shareToken}/preview")
    public ResponseEntity<Resource> previewPublicSharedFile(@PathVariable("shareToken") String shareToken) {
        FileService.FileDownloadPayload payload = shareService.loadPublicSharedResource(shareToken);
        FileMetadata metadata = payload.metadata();

        String contentDisposition = ContentDisposition.inline()
            .filename(metadata.getOriginalName(), StandardCharsets.UTF_8)
            .build()
            .toString();

        return ResponseEntity.ok()
            .contentType(parseSafeMediaType(metadata.getContentType()))
            .contentLength(metadata.getSizeBytes())
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .header("X-Content-Type-Options", "nosniff")
            .body(payload.resource());
    }

    @GetMapping("/api/v1/public/share/{shareToken}/download")
    public ResponseEntity<Resource> downloadPublicSharedFile(@PathVariable("shareToken") String shareToken) {
        FileService.FileDownloadPayload payload = shareService.loadPublicSharedResource(shareToken);
        FileMetadata metadata = payload.metadata();

        String contentDisposition = ContentDisposition.attachment()
            .filename(metadata.getOriginalName(), StandardCharsets.UTF_8)
            .build()
            .toString();

        return ResponseEntity.ok()
            .contentType(parseSafeMediaType(metadata.getContentType()))
            .contentLength(metadata.getSizeBytes())
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .header("X-Content-Type-Options", "nosniff")
            .body(payload.resource());
    }

    private MediaType parseSafeMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
