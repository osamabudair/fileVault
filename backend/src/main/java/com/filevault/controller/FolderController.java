package com.filevault.controller;

import com.filevault.dto.FolderRequest;
import com.filevault.dto.FolderResponse;
import com.filevault.service.FolderService;
import com.filevault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final VaultService vaultService;

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody FolderRequest request) {
        FolderResponse created = folderService.createFolder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FolderResponse>> listFolders(
        @RequestParam(value = "parentId", required = false) UUID parentId
    ) {
        return ResponseEntity.ok(folderService.listFolders(parentId));
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<FolderResponse> renameFolder(
        @PathVariable("id") UUID id,
        @Valid @RequestBody FolderRequest request,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        return ResponseEntity.ok(folderService.renameFolder(id, request, vaultToken));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<FolderResponse> moveFolder(
        @PathVariable("id") UUID id,
        @RequestBody(required = false) Map<String, UUID> body,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        UUID targetParentId = null;
        if (body != null) {
            targetParentId = body.containsKey("targetParentId") ? body.get("targetParentId") : body.get("parentId");
        }
        return ResponseEntity.ok(folderService.moveFolder(id, targetParentId, vaultToken));
    }

    @PatchMapping("/{id}/vault/lock")
    public ResponseEntity<FolderResponse> lockFolderInVault(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vaultService.lockFolderInVault(id));
    }

    @PatchMapping("/{id}/vault/unlock")
    public ResponseEntity<FolderResponse> unlockFolderFromVault(
        @PathVariable("id") UUID id,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        return ResponseEntity.ok(vaultService.unlockFolderFromVault(id, vaultToken));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFolder(
        @PathVariable("id") UUID id,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        folderService.deleteFolder(id, vaultToken);
        return ResponseEntity.ok(Map.of("message", "Folder deleted successfully."));
    }
}
