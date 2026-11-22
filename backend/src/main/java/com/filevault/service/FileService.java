package com.filevault.service;

import com.filevault.dto.FileResponse;
import com.filevault.dto.StorageSummaryResponse;
import com.filevault.entity.FileCategory;
import com.filevault.entity.FileMetadata;
import com.filevault.entity.Folder;
import com.filevault.entity.User;
import com.filevault.repository.FileCollaboratorRepository;
import com.filevault.repository.FileMetadataRepository;
import com.filevault.repository.FileShareLinkRepository;
import com.filevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileShareLinkRepository fileShareLinkRepository;
    private final FileCollaboratorRepository fileCollaboratorRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final FolderService folderService;
    private final VaultService vaultService;
    private final ShareService shareService;

    @Value("${file.storage.max-user-quota-bytes:53687091200}")
    private long maxUserQuotaBytes;

    public record FileDownloadPayload(Resource resource, FileMetadata metadata) {}

    @Transactional
    public FileResponse uploadFile(MultipartFile file, UUID folderId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a non-empty file to upload.");
        }

        Folder targetFolder = null;
        boolean uploadIntoVault = false;
        if (folderId != null) {
            targetFolder = folderService.getOwnedFolderOrThrow(folderId, currentUser);
            if (targetFolder.isInVault()) {
                vaultService.verifyVaultTokenOrThrow(vaultToken, currentUser);
                uploadIntoVault = true;
            }
        }

        long incomingSize = file.getSize();
        long currentUsedBytes = fileMetadataRepository.sumTotalBytesByOwner(currentUser);
        if (currentUsedBytes + incomingSize > maxUserQuotaBytes) {
            throw new IllegalArgumentException(
                "Storage quota exceeded. Uploading this file would exceed your "
                    + FileResponse.formatSize(maxUserQuotaBytes) + " storage limit."
            );
        }

        String sanitizedOriginalName = fileStorageService.sanitizeOriginalFilename(file.getOriginalFilename());
        String contentType = resolveContentType(file.getContentType());
        FileCategory category = FileCategory.detect(sanitizedOriginalName, contentType);

        String storedName = fileStorageService.storeFile(file, currentUser.getId());

        FileMetadata metadata = FileMetadata.builder()
            .originalName(sanitizedOriginalName)
            .storedName(storedName)
            .contentType(contentType)
            .sizeBytes(incomingSize)
            .category(category)
            .favorite(false)
            .trashed(false)
            .inVault(uploadIntoVault)
            .folder(targetFolder)
            .owner(currentUser)
            .build();

        FileMetadata saved = fileMetadataRepository.saveAndFlush(metadata);
        return shareService.toEnrichedFileResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listUserFiles(String view, String categoryParam, String search, UUID folderId) {
        User currentUser = getCurrentAuthenticatedUser();

        String normalizedView = view != null ? view.trim().toLowerCase(Locale.ROOT) : "all";
        if ("shared".equals(normalizedView)) {
            return shareService.listFilesSharedWithCurrentUser();
        }

        boolean trashed = "trash".equals(normalizedView);
        boolean favoriteOnly = "favorites".equals(normalizedView);

        FileCategory category = FileCategory.fromQueryParam(categoryParam);
        String sanitizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        List<FileMetadata> results;
        if (folderId != null) {
            Folder folder = folderService.getOwnedFolderOrThrow(folderId, currentUser);
            if (folder.isInVault()) {
                throw new AccessDeniedException("Use /api/v1/vault/files to browse Secret Vault folders.");
            }
            results = fileMetadataRepository.findUserFilesInFolder(currentUser, folder, category, sanitizedSearch);
        } else {
            boolean rootOnly = "all".equals(normalizedView) && sanitizedSearch == null && category == null;
            results = fileMetadataRepository.findUserFiles(
                currentUser,
                trashed,
                favoriteOnly,
                rootOnly,
                category,
                sanitizedSearch
            );
        }

        return results.stream()
            .map(shareService::toEnrichedFileResponse)
            .toList();
    }

    @Transactional
    public FileResponse moveFileToFolder(UUID fileId, UUID targetFolderId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        if (file.isTrashed()) {
            throw new IllegalArgumentException("Cannot move a file while it is in Trash.");
        }
        if (file.isInVault()) {
            vaultService.verifyVaultTokenOrThrow(vaultToken, currentUser);
        }

        Folder targetFolder = null;
        if (targetFolderId != null) {
            targetFolder = folderService.getOwnedFolderOrThrow(targetFolderId, currentUser);
            if (targetFolder.isInVault() != file.isInVault()) {
                throw new IllegalArgumentException(
                    "Cannot move a file across Secret Vault boundaries. Use the vault lock/unlock endpoints instead."
                );
            }
        }

        file.setFolder(targetFolder);
        file.setUpdatedAt(Instant.now());
        return shareService.toEnrichedFileResponse(fileMetadataRepository.save(file));
    }

    @Transactional(readOnly = true)
    public StorageSummaryResponse getStorageSummary() {
        User currentUser = getCurrentAuthenticatedUser();
        long usedBytes = fileMetadataRepository.sumTotalBytesByOwner(currentUser);

        Map<FileCategory, StorageSummaryResponse.CategoryBreakdown> breakdownMap = new EnumMap<>(FileCategory.class);
        for (FileCategory category : FileCategory.values()) {
            breakdownMap.put(category, StorageSummaryResponse.CategoryBreakdown.builder()
                .count(0L)
                .sizeBytes(0L)
                .formattedSize("0 B")
                .build());
        }

        List<FileMetadataRepository.CategoryUsageProjection> projections =
            fileMetadataRepository.findCategoryUsageByOwner(currentUser);

        for (FileMetadataRepository.CategoryUsageProjection proj : projections) {
            breakdownMap.put(proj.getCategory(), StorageSummaryResponse.CategoryBreakdown.builder()
                .count(proj.getFileCount())
                .sizeBytes(proj.getTotalBytes())
                .formattedSize(FileResponse.formatSize(proj.getTotalBytes()))
                .build());
        }

        double percentage = maxUserQuotaBytes > 0
            ? Math.min(100.0, Math.round((usedBytes * 10000.0) / maxUserQuotaBytes) / 100.0)
            : 0.0;

        return StorageSummaryResponse.builder()
            .usedBytes(usedBytes)
            .maxBytes(maxUserQuotaBytes)
            .formattedUsed(FileResponse.formatSize(usedBytes))
            .formattedMax(FileResponse.formatSize(maxUserQuotaBytes))
            .usagePercentage(percentage)
            .breakdown(breakdownMap)
            .build();
    }

    @Transactional
    public FileResponse toggleFavorite(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        if (file.isTrashed() || file.isInVault()) {
            throw new IllegalArgumentException("Cannot favorite a file while it is in Trash or Secret Vault.");
        }

        file.setFavorite(!file.isFavorite());
        file.setUpdatedAt(Instant.now());
        return shareService.toEnrichedFileResponse(fileMetadataRepository.save(file));
    }

    @Transactional
    public FileResponse moveToTrash(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        if (file.isInVault()) {
            throw new IllegalArgumentException("Unlock the file from Secret Vault before moving it to Trash.");
        }

        fileShareLinkRepository.findByFile(file).ifPresent(link -> {
            link.setActive(false);
            fileShareLinkRepository.save(link);
        });

        file.setTrashed(true);
        file.setTrashedAt(Instant.now());
        file.setFavorite(false);
        file.setUpdatedAt(Instant.now());
        return shareService.toEnrichedFileResponse(fileMetadataRepository.save(file));
    }

    @Transactional
    public FileResponse restoreFromTrash(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        file.setTrashed(false);
        file.setTrashedAt(null);
        file.setUpdatedAt(Instant.now());
        return shareService.toEnrichedFileResponse(fileMetadataRepository.save(file));
    }

    @Transactional
    public void deletePermanently(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        fileShareLinkRepository.deleteByFile(file);
        fileCollaboratorRepository.deleteByFile(file);
        fileMetadataRepository.delete(file);
        fileStorageService.deletePhysicalFile(currentUser.getId(), file.getStoredName());
    }

    @Transactional(readOnly = true)
    public FileDownloadPayload loadAccessibleFileResource(UUID fileId, String vaultToken) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = fileMetadataRepository.findById(fileId)
            .orElseThrow(() -> new NoSuchElementException("File not found."));

        boolean isOwner = file.getOwner().getId().equals(currentUser.getId());
        if (isOwner) {
            if (file.isInVault()) {
                vaultService.verifyVaultTokenOrThrow(vaultToken, currentUser);
            }
        } else {
            boolean isCollaborator = !file.isTrashed()
                && !file.isInVault()
                && fileCollaboratorRepository.existsByFileAndSharedWithUser(file, currentUser);
            if (!isCollaborator) {
                throw new AccessDeniedException("You do not have permission to access this file.");
            }
        }

        Resource resource = fileStorageService.loadFileAsResource(file.getOwner().getId(), file.getStoredName());
        return new FileDownloadPayload(resource, file);
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

    private String resolveContentType(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        try {
            return MediaType.parseMediaType(rawContentType).toString();
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
