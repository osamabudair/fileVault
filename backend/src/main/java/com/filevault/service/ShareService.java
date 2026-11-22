package com.filevault.service;

import com.filevault.dto.FileResponse;
import com.filevault.dto.ShareDetailsResponse;
import com.filevault.entity.FileCollaborator;
import com.filevault.entity.FileMetadata;
import com.filevault.entity.FileShareLink;
import com.filevault.entity.User;
import com.filevault.repository.FileCollaboratorRepository;
import com.filevault.repository.FileMetadataRepository;
import com.filevault.repository.FileShareLinkRepository;
import com.filevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final FileMetadataRepository fileMetadataRepository;
    private final FileShareLinkRepository fileShareLinkRepository;
    private final FileCollaboratorRepository fileCollaboratorRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public ShareDetailsResponse getShareDetails(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);
        return buildShareDetailsResponse(file);
    }

    @Transactional
    public ShareDetailsResponse createOrEnableShareLink(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);
        validateFileShareable(file);

        FileShareLink shareLink = fileShareLinkRepository.findByFile(file)
            .orElseGet(() -> FileShareLink.builder()
                .file(file)
                .shareToken(generateSecureShareToken())
                .active(true)
                .build());

        shareLink.setActive(true);
        fileShareLinkRepository.save(shareLink);

        return buildShareDetailsResponse(file);
    }

    @Transactional
    public ShareDetailsResponse disableShareLink(UUID fileId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        fileShareLinkRepository.findByFile(file).ifPresent(link -> {
            link.setActive(false);
            fileShareLinkRepository.save(link);
        });

        return buildShareDetailsResponse(file);
    }

    @Transactional
    public ShareDetailsResponse inviteCollaborator(UUID fileId, String targetEmail) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);
        validateFileShareable(file);

        String normalizedTargetEmail = targetEmail.toLowerCase(Locale.ROOT).trim();
        if (currentUser.getEmail().equalsIgnoreCase(normalizedTargetEmail)) {
            throw new IllegalArgumentException("You cannot invite yourself as a collaborator.");
        }

        User targetUser = userRepository.findByEmail(normalizedTargetEmail)
            .orElseThrow(() -> new NoSuchElementException("No registered FileVault user found with email: " + normalizedTargetEmail));

        if (!fileCollaboratorRepository.existsByFileAndSharedWithUser(file, targetUser)) {
            FileCollaborator collaborator = FileCollaborator.builder()
                .file(file)
                .sharedWithUser(targetUser)
                .permission("VIEW")
                .build();
            fileCollaboratorRepository.save(collaborator);
        }

        return buildShareDetailsResponse(file);
    }

    @Transactional
    public ShareDetailsResponse revokeCollaborator(UUID fileId, UUID collaboratorId) {
        User currentUser = getCurrentAuthenticatedUser();
        FileMetadata file = getOwnedFileOrThrow(fileId, currentUser);

        FileCollaborator collaborator = fileCollaboratorRepository.findByIdAndFile(collaboratorId, file)
            .orElseThrow(() -> new NoSuchElementException("Collaborator record not found for this file."));

        fileCollaboratorRepository.delete(collaborator);
        return buildShareDetailsResponse(file);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listFilesSharedWithCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return fileCollaboratorRepository.findFilesSharedWithUser(currentUser)
            .stream()
            .map(this::toEnrichedFileResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ShareDetailsResponse.PublicFileResponse getPublicShareMetadata(String shareToken) {
        FileMetadata file = resolveValidPublicSharedFile(shareToken);
        return ShareDetailsResponse.PublicFileResponse.builder()
            .name(file.getOriginalName())
            .sizeBytes(file.getSizeBytes())
            .formattedSize(FileResponse.formatSize(file.getSizeBytes()))
            .category(file.getCategory().name().toLowerCase(Locale.ROOT))
            .contentType(file.getContentType())
            .updatedAt(file.getUpdatedAt())
            .previewUrl("/api/v1/public/share/" + shareToken + "/preview")
            .downloadUrl("/api/v1/public/share/" + shareToken + "/download")
            .build();
    }

    @Transactional(readOnly = true)
    public FileService.FileDownloadPayload loadPublicSharedResource(String shareToken) {
        FileMetadata file = resolveValidPublicSharedFile(shareToken);
        Resource resource = fileStorageService.loadFileAsResource(file.getOwner().getId(), file.getStoredName());
        return new FileService.FileDownloadPayload(resource, file);
    }

    public FileResponse toEnrichedFileResponse(FileMetadata file) {
        List<FileCollaborator> collaborators = fileCollaboratorRepository.findByFileWithUsers(file);
        List<String> sharedWithNames = collaborators.stream()
            .map(c -> c.getSharedWithUser().getFullName() + " (" + c.getSharedWithUser().getEmail() + ")")
            .toList();

        boolean hasActiveLink = fileShareLinkRepository.findByFile(file)
            .map(link -> link.isActive() && (link.getExpiresAt() == null || link.getExpiresAt().isAfter(Instant.now())))
            .orElse(false);

        return FileResponse.fromEntity(file, collaborators.size(), hasActiveLink, sharedWithNames);
    }

    private FileMetadata resolveValidPublicSharedFile(String shareToken) {
        if (shareToken == null || shareToken.isBlank()) {
            throw new NoSuchElementException("Invalid or expired share link.");
        }
        FileShareLink shareLink = fileShareLinkRepository.findByShareToken(shareToken.trim())
            .orElseThrow(() -> new NoSuchElementException("Share link not found or has been revoked."));

        if (!shareLink.isActive()) {
            throw new NoSuchElementException("This share link has been disabled by the owner.");
        }
        if (shareLink.getExpiresAt() != null && shareLink.getExpiresAt().isBefore(Instant.now())) {
            throw new NoSuchElementException("This share link has expired.");
        }

        FileMetadata file = shareLink.getFile();
        if (file.isTrashed() || file.isInVault()) {
            throw new NoSuchElementException("The shared file is no longer available.");
        }
        return file;
    }

    private ShareDetailsResponse buildShareDetailsResponse(FileMetadata file) {
        Optional<FileShareLink> shareLinkOpt = fileShareLinkRepository.findByFile(file);
        List<FileCollaborator> collaborators = fileCollaboratorRepository.findByFileWithUsers(file);

        List<ShareDetailsResponse.CollaboratorSummary> summaries = collaborators.stream()
            .map(c -> ShareDetailsResponse.CollaboratorSummary.builder()
                .id(c.getId())
                .userId(c.getSharedWithUser().getId())
                .fullName(c.getSharedWithUser().getFullName())
                .email(c.getSharedWithUser().getEmail())
                .permission(c.getPermission())
                .sharedAt(c.getSharedAt())
                .build())
            .toList();

        boolean active = shareLinkOpt.map(FileShareLink::isActive).orElse(false);
        String token = shareLinkOpt.map(FileShareLink::getShareToken).orElse(null);
        String url = (active && token != null) ? "/api/v1/public/share/" + token : null;

        return ShareDetailsResponse.builder()
            .fileId(file.getId())
            .fileName(file.getOriginalName())
            .shareToken(token)
            .shareUrl(url)
            .active(active)
            .expiresAt(shareLinkOpt.map(FileShareLink::getExpiresAt).orElse(null))
            .collaborators(summaries)
            .build();
    }

    private void validateFileShareable(FileMetadata file) {
        if (file.isTrashed()) {
            throw new IllegalArgumentException("Cannot share a file while it is in Trash.");
        }
        if (file.isInVault()) {
            throw new IllegalArgumentException("Files locked inside the Secret Vault cannot be shared.");
        }
    }

    private String generateSecureShareToken() {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private FileMetadata getOwnedFileOrThrow(UUID fileId, User currentUser) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
            .orElseThrow(() -> new NoSuchElementException("File not found."));
        if (!file.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage sharing for this file.");
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
