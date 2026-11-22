package com.filevault.controller;

import com.filevault.dto.FileResponse;
import com.filevault.dto.FolderRequest;
import com.filevault.dto.FolderResponse;
import com.filevault.dto.VaultPinRequest;
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
@RequestMapping("/api/v1/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVaultStatus() {
        return ResponseEntity.ok(vaultService.getVaultStatus());
    }

    @PostMapping("/setup-pin")
    public ResponseEntity<Map<String, Object>> setupPin(@Valid @RequestBody VaultPinRequest.PinPayload payload) {
        return ResponseEntity.ok(vaultService.setupPin(payload.getPin()));
    }

    @PostMapping("/change-pin")
    public ResponseEntity<Map<String, Object>> changePin(@Valid @RequestBody VaultPinRequest.ChangePinPayload payload) {
        return ResponseEntity.ok(vaultService.changePin(payload.getOldPin(), payload.getNewPin()));
    }

    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> unlockVault(@Valid @RequestBody VaultPinRequest.PinPayload payload) {
        return ResponseEntity.ok(vaultService.unlockVault(payload.getPin()));
    }

    @PostMapping("/folders")
    public ResponseEntity<FolderResponse> createVaultFolder(
        @Valid @RequestBody FolderRequest request,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        FolderResponse created = vaultService.createVaultFolder(request, vaultToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/folders")
    public ResponseEntity<List<FolderResponse>> listVaultFolders(
        @RequestParam(value = "parentId", required = false) UUID parentId,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        return ResponseEntity.ok(vaultService.listVaultFolders(parentId, vaultToken));
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileResponse>> listVaultFiles(
        @RequestParam(value = "folderId", required = false) UUID folderId,
        @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken
    ) {
        return ResponseEntity.ok(vaultService.listVaultFiles(folderId, vaultToken));
    }
}
