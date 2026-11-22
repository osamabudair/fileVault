package com.filevault.repository;

import com.filevault.entity.FileMetadata;
import com.filevault.entity.FileShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileShareLinkRepository extends JpaRepository<FileShareLink, UUID> {

    Optional<FileShareLink> findByFile(FileMetadata file);

    Optional<FileShareLink> findByShareToken(String shareToken);

    void deleteByFile(FileMetadata file);
}
