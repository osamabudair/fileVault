package com.filevault.service;

import com.filevault.dto.FileResponse;
import com.filevault.dto.FolderRequest;
import com.filevault.dto.FolderResponse;
import com.filevault.entity.FileMetadata;
import com.filevault.entity.Folder;
import com.filevault.entity.User;
import com.filevault.repository.FileCollaboratorRepository;
import com.filevault.repository.FileMetadataRepository;
import com.filevault.repository.FileShareLinkRepository;
import com.filevault.repository.FolderRepository;
import com.filevault.repository.UserRepository;
import com.filevault.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VaultService {

    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FolderRepository folderRepository;
    private final FileShareLinkRepository fileShareLinkRepository;
    private final FileCollaboratorRepository fileCollaboratorRepository;
    private final FolderService folderService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public Map<String, Object> getVaultStatus() {
        User currentUser = getCurrentAuthenticatedUser();
        boolean configured = currentUser.getVaultPinHash() != null && !currentUser.getVaultPinHash().isBlank();
        return Map.of("configured", configured);
    }

    @Transactional
    public Map<String, Object> setupPin(String pin) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser.getVaultPinHash() != null && !currentUser.getVaultPinHash().isBlank()) {
            throw new IllegalArgumentException("Vault PIN is already configured. Use change-pin instead.");
        }
        currentUser.setVaultPinHash(passwordEncoder.encode(pin));
        userRepository.save(currentUser);
        return Map.of("configured", true, "message", "Secret Vault PIN configured successfully.");
    }

    @Transactional
    public Map<String, Object> changePin(String oldPin, String newPin) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser.getVaultPinHash() == null || currentUser.getVaultPinHash().isBlank()) {
            throw new IllegalArgumentException("Vault PIN is not configured yet.");
        }
        if (!passwordEncoder.matches(oldPin, currentUser.getVaultPinHash())) {
            throw new BadCredentialsException("Current Vault PIN is incorrect.");
        }
        currentUser.setVaultPinHash(passwordEncoder.encode(newPin));
        userRepository.save(currentUser);
        return Map.of("message", "Secret Vault PIN updated successfully.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> unlockVault(String pin) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser.getVaultPinHash() == null || currentUser.getVaultPinHash().isBlank()) {
            throw new IllegalArgumentException("Secret Vault PIN has not been set up yet.");
        }
        if (!passwordEncoder.matches(pin, currentUser.getVaultPinHash())) {
            throw new BadCredentialsException("Invalid Secret Vault PIN.");
        }
        String vaultToken = jwtService.generateVaultToken(currentUser.getEmail(), currentUser.getId());
        return Map.of(
            "vaultToken", vaultToken,
            "expiresInSeconds", 600
        );
    }

    @Transactional
    public FolderResponse createVaultFolder(FolderRequest request, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        verifyVaultTokenOrThrow(vaultToken, currentUser);

        String trimmedName = folderService.sanitizeFolderName(request.getName());
        Folder parent = null;
        if (request.getParentId() != null) {
            parent = folderService.getOwnedFolderOrThrow(request.getParentId(), currentUser);
            if (!parent.isInVault()) {
                throw new IllegalArgumentException("Parent folder must also be inside the Secret Vault.");
            }
            if (folderRepository.existsByOwnerAndInVaultTrueAndParentAndNameIgnoreCaseAndTrashedFalse(currentUser, parent, trimmedName)) {
                throw new IllegalArgumentException("A folder with this name already exists inside the selected Vault folder.");
            }
        } else {
            if (folderRepository.existsByOwnerAndInVaultTrueAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(currentUser, trimmedName)) {
                throw new IllegalArgumentException("A folder with this name already exists in the Secret Vault root.");
            }
        }

        Folder folder = Folder.builder()
            .name(trimmedName)
            .parent(parent)
            .owner(currentUser)
            .inVault(true)
            .trashed(false)
            .build();

        Folder saved = folderRepository.saveAndFlush(folder);
        return FolderResponse.fromEntity(saved, 0L);
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> listVaultFolders(UUID parentId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        verifyVaultTokenOrThrow(vaultToken, currentUser);

        List<Folder> folders;
        if (parentId == null) {
            folders = folderRepository.findByOwnerAndInVaultTrueAndParentIsNullAndTrashedFalseOrderByNameAsc(currentUser);
        } else {
            Folder parent = folderService.getOwnedFolderOrThrow(parentId, currentUser);
            if (!parent.isInVault()) {
                throw new IllegalArgumentException("Specified parent folder is not inside the Secret Vault.");
            }
            folders = folderRepository.findByOwnerAndInVaultTrueAndParentAndTrashedFalseOrderByNameAsc(currentUser, parent);
        }

        return folders.stream()
            .map(folder -> FolderResponse.fromEntity(
                folder,
                fileMetadataRepository.countByOwnerAndFolderAndTrashedFalseAndInVaultTrue(currentUser, folder)
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listVaultFiles(UUID folderId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        verifyVaultTokenOrThrow(vaultToken, currentUser);

        List<FileMetadata> files;
        if (folderId == null) {
            files = fileMetadataRepository.findVaultFiles(currentUser);
        } else {
            Folder folder = folderService.getOwnedFolderOrThrow(folderId, currentUser);
            if (!folder.isInVault()) {
                throw new IllegalArgumentException("Specified folder is not inside the Secret Vault.");
            }
            files = fileMetadataRepository.findVaultFilesInFolder(currentUser, folder);
        }

        return files.stream()
            .map(FileResponse::fromEntity)
            .toList();
    }

    @Transactional
    public FolderResponse lockFolderInVault(UUID folderId) {
        User currentUser = getCurrentAuthenticatedUser();
        Folder targetFolder = folderService.getOwnedFolderOrThrow(folderId, currentUser);

        if (targetFolder.isTrashed()) {
            throw new IllegalArgumentException("Cannot move a trashed folder into the Secret Vault.");
        }
        if (targetFolder.isInVault()) {
            throw new IllegalArgumentException("Folder is already locked in the Secret Vault.");
        }
        if (folderRepository.existsByOwnerAndInVaultTrueAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(currentUser, targetFolder.getName())) {
            throw new IllegalArgumentException("A folder named '" + targetFolder.getName() + "' already exists in the Secret Vault root.");
        }

        targetFolder.setParent(null);
        recursivelySetFolderVaultState(currentUser, targetFolder, true);

        Folder saved = folderRepository.saveAndFlush(targetFolder);
        return FolderResponse.fromEntity(saved, folderService.countFolderItems(currentUser, saved));
    }

    @Transactional
    public FolderResponse unlockFolderFromVault(UUID folderId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        verifyVaultTokenOrThrow(vaultToken, currentUser);

        Folder targetFolder = folderService.getOwnedFolderOrThrow(folderId, currentUser);
        if (!targetFolder.isInVault()) {
            throw new IllegalArgumentException("Folder is not currently inside the Secret Vault.");
        }
        if (folderRepository.existsByOwnerAndInVaultFalseAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(currentUser, targetFolder.getName())) {
            throw new IllegalArgumentException("A folder named '" + targetFolder.getName() + "' already exists in your main root directory.");
        }

        targetFolder.setParent(null);
        recursivelySetFolderVaultState(currentUser, targetFolder, false);

        Folder saved = folderRepository.saveAndFlush(targetFolder);
        return FolderResponse.fromEntity(saved, folderService.countFolderItems(currentUser, saved));
    }

    @Transactional
    public FileResponse lockFileInVault(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        if (file.isTrashed()) {
            throw new IllegalArgumentException("Cannot move a trashed file into the Secret Vault.");
        }

        fileShareLinkRepository.deleteByFile(file);
        fileCollaboratorRepository.deleteByFile(file);

        file.setInVault(true);
        file.setFolder(null);
        file.setFavorite(false);
        file.setUpdatedAt(Instant.now());

        return FileResponse.fromEntity(fileMetadataRepository.save(file));
    }

    @Transactional
    public FileResponse unlockFileFromVault(UUID fileId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        verifyVaultTokenOrThrow(vaultToken, currentUser);

        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);
        if (!file.isInVault()) {
            throw new IllegalArgumentException("File is not currently inside the Secret Vault.");
        }

        file.setInVault(false);
        file.setFolder(null);
        file.setUpdatedAt(Instant.now());
        return FileResponse.fromEntity(fileMetadataRepository.save(file));
    }

    public void verifyVaultTokenOrThrow(String vaultToken, User currentUser) {
        if (!jwtService.isVaultTokenValid(vaultToken, currentUser.getEmail())) {
            throw new AccessDeniedException("A valid X-Vault-Token header is required to access Secret Vault items.");
        }
    }

    private void recursivelySetFolderVaultState(User owner, Folder folder, boolean inVault) {
        folder.setInVault(inVault);
        folder.setUpdatedAt(Instant.now());
        folderRepository.save(folder);

        List<FileMetadata> files = fileMetadataRepository.findByOwnerAndFolder(owner, folder);
        for (FileMetadata file : files) {
            if (inVault) {
                fileShareLinkRepository.deleteByFile(file);
                fileCollaboratorRepository.deleteByFile(file);
                file.setFavorite(false);
            }
            file.setInVault(inVault);
            file.setUpdatedAt(Instant.now());
        }
        fileMetadataRepository.saveAll(files);

        List<Folder> children = folderRepository.findByOwnerAndParent(owner, folder);
        for (Folder child : children) {
            recursivelySetFolderVaultState(owner, child, inVault);
        }
    }

    private FileMetadata getOwnedFileOrThrow(UUID fileId, User currentUser) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
            .orElseThrow(() -> new NoSuchElementException("File not found."));
        if (!file.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this file.");
        }
        return file;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new BadCredentialsException("Authenticated user session is required.");
        }
        String email = authentication.getName().toLowerCase(Locale.ROOT).trim();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Authenticated user account no longer exists."));
    }
}
