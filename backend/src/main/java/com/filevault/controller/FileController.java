package com.filevault.controller;

import com.filevault.dto.FileResponse;
import com.filevault.dto.StorageSummaryResponse;
import com.filevault.entity.FileMetadata;
import com.filevault.service.FileService;
import com.filevault.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final VaultService vaultService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "folderId", required = false) UUID folderId,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        FileResponse uploaded = fileService.uploadFile(file, folderId, vaultToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> listFiles(
        @RequestParam(value = "view", defaultValue = "all") String view,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "folderId", required = false) UUID folderId
    ) {
        return ResponseEntity.ok(fileService.listUserFiles(view, category, search, folderId));
    }

    @GetMapping("/storage")
    public ResponseEntity<StorageSummaryResponse> getStorageSummary() {
        return ResponseEntity.ok(fileService.getStorageSummary());
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<FileResponse> moveFileToFolder(
        @PathVariable("id") UUID id,
        @RequestBody(required = false) Map<String, UUID> body,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        UUID targetFolderId = (body != null) ? body.get("folderId") : null;
        return ResponseEntity.ok(fileService.moveFileToFolder(id, targetFolderId, vaultToken));
    }

    @PatchMapping("/{id}/vault/lock")
    public ResponseEntity<FileResponse> lockFileInVault(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vaultService.lockFileInVault(id));
    }

    @PatchMapping("/{id}/vault/unlock")
    public ResponseEntity<FileResponse> unlockFileFromVault(
        @PathVariable("id") UUID id,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        return ResponseEntity.ok(vaultService.unlockFileFromVault(id, vaultToken));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<FileResponse> toggleFavorite(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(fileService.toggleFavorite(id));
    }

    @PatchMapping("/{id}/trash")
    public ResponseEntity<FileResponse> moveToTrash(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(fileService.moveToTrash(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<FileResponse> restoreFromTrash(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(fileService.restoreFromTrash(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePermanently(@PathVariable("id") UUID id) {
        fileService.deletePermanently(id);
        return ResponseEntity.ok(Map.of("message", "File permanently deleted."));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
        @PathVariable("id") UUID id,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        FileService.FileDownloadPayload payload = fileService.loadAccessibleFileResource(id, vaultToken);
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

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(
        @PathVariable("id") UUID id,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        FileService.FileDownloadPayload payload = fileService.loadAccessibleFileResource(id, vaultToken);
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
