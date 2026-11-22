package com.filevault.repository;

import com.filevault.entity.FileCategory;
import com.filevault.entity.FileMetadata;
import com.filevault.entity.Folder;
import com.filevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByIdAndOwner(UUID id, User owner);

    @Query("""
        SELECT f FROM FileMetadata f
        WHERE f.owner = :owner
          AND f.inVault = false
          AND f.trashed = :trashed
          AND (:favoriteOnly = false OR f.favorite = true)
          AND (:rootOnly = false OR f.folder IS NULL)
          AND (:category IS NULL OR f.category = :category)
          AND (:search IS NULL OR LOWER(f.originalName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        ORDER BY f.updatedAt DESC
        """)
    List<FileMetadata> findUserFiles(
        @Param("owner") User owner,
        @Param("trashed") boolean trashed,
        @Param("favoriteOnly") boolean favoriteOnly,
        @Param("rootOnly") boolean rootOnly,
        @Param("category") FileCategory category,
        @Param("search") String search
    );

    @Query("""
        SELECT f FROM FileMetadata f
        WHERE f.owner = :owner
          AND f.inVault = false
          AND f.trashed = false
          AND f.folder = :folder
          AND (:category IS NULL OR f.category = :category)
          AND (:search IS NULL OR LOWER(f.originalName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        ORDER BY f.updatedAt DESC
        """)
    List<FileMetadata> findUserFilesInFolder(
        @Param("owner") User owner,
        @Param("folder") Folder folder,
        @Param("category") FileCategory category,
        @Param("search") String search
    );

    @Query("""
        SELECT f FROM FileMetadata f
        WHERE f.owner = :owner
          AND f.inVault = true
          AND f.trashed = false
          AND f.folder IS NULL
        ORDER BY f.updatedAt DESC
        """)
    List<FileMetadata> findVaultFiles(@Param("owner") User owner);

    @Query("""
        SELECT f FROM FileMetadata f
        WHERE f.owner = :owner
          AND f.inVault = true
          AND f.trashed = false
          AND f.folder = :folder
        ORDER BY f.updatedAt DESC
        """)
    List<FileMetadata> findVaultFilesInFolder(
        @Param("owner") User owner,
        @Param("folder") Folder folder
    );

    List<FileMetadata> findByOwnerAndFolder(User owner, Folder folder);

    long countByOwnerAndFolderAndTrashedFalseAndInVaultFalse(User owner, Folder folder);

    long countByOwnerAndFolderAndTrashedFalseAndInVaultTrue(User owner, Folder folder);

    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileMetadata f WHERE f.owner = :owner")
    long sumTotalBytesByOwner(@Param("owner") User owner);

    @Query("""
        SELECT f.category AS category, COUNT(f) AS fileCount, COALESCE(SUM(f.sizeBytes), 0) AS totalBytes
        FROM FileMetadata f
        WHERE f.owner = :owner AND f.trashed = false
        GROUP BY f.category
        """)
    List<CategoryUsageProjection> findCategoryUsageByOwner(@Param("owner") User owner);

    interface CategoryUsageProjection {
        FileCategory getCategory();
        long getFileCount();
        long getTotalBytes();
    }
}
