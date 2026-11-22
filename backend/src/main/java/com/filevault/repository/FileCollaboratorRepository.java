package com.filevault.repository;

import com.filevault.entity.FileCollaborator;
import com.filevault.entity.FileMetadata;
import com.filevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileCollaboratorRepository extends JpaRepository<FileCollaborator, UUID> {

    @Query("""
        SELECT c FROM FileCollaborator c
        JOIN FETCH c.sharedWithUser
        WHERE c.file = :file
        ORDER BY c.sharedAt ASC
        """)
    List<FileCollaborator> findByFileWithUsers(@Param("file") FileMetadata file);

    Optional<FileCollaborator> findByIdAndFile(UUID id, FileMetadata file);

    boolean existsByFileAndSharedWithUser(FileMetadata file, User sharedWithUser);

    long countByFile(FileMetadata file);

    void deleteByFile(FileMetadata file);

    @Query("""
        SELECT c.file FROM FileCollaborator c
        WHERE c.sharedWithUser = :user
          AND c.file.trashed = false
          AND c.file.inVault = false
        ORDER BY c.sharedAt DESC
        """)
    List<FileMetadata> findFilesSharedWithUser(@Param("user") User user);
}
