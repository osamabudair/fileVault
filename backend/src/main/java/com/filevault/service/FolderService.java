package com.filevault.service;

import com.filevault.dto.FolderRequest;
import com.filevault.dto.FolderResponse;
import com.filevault.entity.FileMetadata;
import com.filevault.entity.Folder;
import com.filevault.entity.User;
import com.filevault.repository.FileMetadataRepository;
import com.filevault.repository.FolderRepository;
import com.filevault.repository.UserRepository;
import com.filevault.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public FolderResponse createFolder(FolderRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        String trimmedName = sanitizeFolderName(request.getName());

        Folder parent = null;
        if (request.getParentId() != null) {
            parent = getOwnedFolderOrThrow(request.getParentId(), currentUser);
            if (parent.isInVault()) {
                throw new IllegalArgumentException("Use /api/v1/vault/folders to create folders inside the Secret Vault.");
            }
            if (folderRepository.existsByOwnerAndInVaultFalseAndParentAndNameIgnoreCaseAndTrashedFalse(currentUser, parent, trimmedName)) {
                throw new IllegalArgumentException("A folder with this name already exists inside the selected folder.");
            }
        } else {
            if (folderRepository.existsByOwnerAndInVaultFalseAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(currentUser, trimmedName)) {
                throw new IllegalArgumentException("A folder with this name already exists in your root directory.");
            }
        }

        Folder folder = Folder.builder()
            .name(trimmedName)
            .parent(parent)
            .owner(currentUser)
            .inVault(false)
            .trashed(false)
            .build();

        Folder saved = folderRepository.saveAndFlush(folder);
        return FolderResponse.fromEntity(saved, 0L);
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> listFolders(UUID parentId) {
        User currentUser = getCurrentAuthenticatedUser();

        List<Folder> folders;
        if (parentId == null) {
            folders = folderRepository.findByOwnerAndInVaultFalseAndParentIsNullAndTrashedFalseOrderByNameAsc(currentUser);
        } else {
            Folder parent = getOwnedFolderOrThrow(parentId, currentUser);
            if (parent.isInVault()) {
                throw new AccessDeniedException("Use /api/v1/vault/folders to list folders inside the Secret Vault.");
            }
            folders = folderRepository.findByOwnerAndInVaultFalseAndParentAndTrashedFalseOrderByNameAsc(currentUser, parent);
        }

        return folders.stream()
            .map(folder -> FolderResponse.fromEntity(
                folder,
                fileMetadataRepository.countByOwnerAndFolderAndTrashedFalseAndInVaultFalse(currentUser, folder)
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> getFolderBreadcrumbs(UUID folderId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        Folder current = getOwnedFolderOrThrow(folderId, currentUser);
        if (current.isInVault()) {
            verifyVaultTokenOrThrow(vaultToken, currentUser);
        }

        LinkedList<FolderResponse> breadcrumbs = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();
        Folder cursor = current;
        while (cursor != null && visited.add(cursor.getId())) {
            breadcrumbs.addFirst(FolderResponse.fromEntity(cursor, countFolderItems(currentUser, cursor)));
            cursor = cursor.getParent();
        }
        return breadcrumbs;
    }

    @Transactional
    public FolderResponse renameFolder(UUID folderId, FolderRequest request, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        Folder folder = getOwnedFolderOrThrow(folderId, currentUser);
        if (folder.isInVault()) {
            verifyVaultTokenOrThrow(vaultToken, currentUser);
        }

        String newName = sanitizeFolderName(request.getName());
        if (!folder.getName().equalsIgnoreCase(newName)) {
            if (hasDuplicateFolderName(currentUser, folder.getParent(), newName, folder.isInVault())) {
                throw new IllegalArgumentException("Another folder with this name already exists in this location.");
            }
        }

        folder.setName(newName);
        folder.setUpdatedAt(Instant.now());
        Folder saved = folderRepository.saveAndFlush(folder);
        return FolderResponse.fromEntity(saved, countFolderItems(currentUser, saved));
    }

    @Transactional
    public FolderResponse moveFolder(UUID folderId, UUID targetParentId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        Folder sourceFolder = getOwnedFolderOrThrow(folderId, currentUser);
        if (sourceFolder.isInVault()) {
            verifyVaultTokenOrThrow(vaultToken, currentUser);
        }

        Folder targetParent = null;
        if (targetParentId != null) {
            targetParent = getOwnedFolderOrThrow(targetParentId, currentUser);
            if (sourceFolder.isInVault() != targetParent.isInVault()) {
                throw new IllegalArgumentException(
                    "Cannot move a folder across Secret Vault boundaries. Use the vault lock/unlock endpoints instead."
                );
            }

            // Cycle prevention: verify targetParent is not sourceFolder or any descendant of sourceFolder
            Folder cursor = targetParent;
            while (cursor != null) {
                if (cursor.getId().equals(sourceFolder.getId())) {
                    throw new IllegalArgumentException("Cannot move a folder into itself or one of its own subfolders.");
                }
                cursor = cursor.getParent();
            }
        }

        UUID currentParentId = sourceFolder.getParent() != null ? sourceFolder.getParent().getId() : null;
        if (!Objects.equals(currentParentId, targetParentId)) {
            if (hasDuplicateFolderName(currentUser, targetParent, sourceFolder.getName(), sourceFolder.isInVault())) {
                throw new IllegalArgumentException("A folder with this name already exists in the destination folder.");
            }
            sourceFolder.setParent(targetParent);
            sourceFolder.setUpdatedAt(Instant.now());
            sourceFolder = folderRepository.saveAndFlush(sourceFolder);
        }

        return FolderResponse.fromEntity(sourceFolder, countFolderItems(currentUser, sourceFolder));
    }

    @Transactional
    public void deleteFolder(UUID folderId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        Folder folder = getOwnedFolderOrThrow(folderId, currentUser);
        if (folder.isInVault()) {
            verifyVaultTokenOrThrow(vaultToken, currentUser);
        }

        Folder fallbackParent = folder.getParent();

        List<FileMetadata> containedFiles = fileMetadataRepository.findByOwnerAndFolder(currentUser, folder);
        for (FileMetadata file : containedFiles) {
            file.setFolder(fallbackParent);
        }
        fileMetadataRepository.saveAll(containedFiles);

        List<Folder> childFolders = folderRepository.findByOwnerAndParent(currentUser, folder);
        for (Folder child : childFolders) {
            child.setParent(fallbackParent);
        }
        folderRepository.saveAll(childFolders);

        folderRepository.delete(folder);
    }

    public Folder getOwnedFolderOrThrow(UUID folderId, User currentUser) {
        Folder folder = folderRepository.findById(folderId)
            .orElseThrow(() -> new NoSuchElementException("Folder not found."));
        if (!folder.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this folder.");
        }
        return folder;
    }

    public String sanitizeFolderName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Folder name cannot be blank.");
        }
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t\\u0000]", "").trim();
        if (cleaned.isEmpty() || ".".equals(cleaned) || "..".equals(cleaned)) {
            throw new IllegalArgumentException("Please provide a valid folder name.");
        }
        return cleaned;
    }

    public long countFolderItems(User owner, Folder folder) {
        return folder.isInVault()
            ? fileMetadataRepository.countByOwnerAndFolderAndTrashedFalseAndInVaultTrue(owner, folder)
            : fileMetadataRepository.countByOwnerAndFolderAndTrashedFalseAndInVaultFalse(owner, folder);
    }

    private boolean hasDuplicateFolderName(User owner, Folder parent, String name, boolean inVault) {
        if (inVault) {
            return parent == null
                ? folderRepository.existsByOwnerAndInVaultTrueAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(owner, name)
                : folderRepository.existsByOwnerAndInVaultTrueAndParentAndNameIgnoreCaseAndTrashedFalse(owner, parent, name);
        }
        return parent == null
            ? folderRepository.existsByOwnerAndInVaultFalseAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(owner, name)
            : folderRepository.existsByOwnerAndInVaultFalseAndParentAndNameIgnoreCaseAndTrashedFalse(owner, parent, name);
    }

    private void verifyVaultTokenOrThrow(String vaultToken, User currentUser) {
        if (!jwtService.isVaultTokenValid(vaultToken, currentUser.getEmail())) {
            throw new AccessDeniedException("A valid X-Vault-Token header is required to modify Secret Vault folders.");
        }
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
