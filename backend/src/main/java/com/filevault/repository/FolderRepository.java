package com.filevault.repository;

import com.filevault.entity.Folder;
import com.filevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByOwnerAndInVaultFalseAndParentIsNullAndTrashedFalseOrderByNameAsc(User owner);

    List<Folder> findByOwnerAndInVaultFalseAndParentAndTrashedFalseOrderByNameAsc(User owner, Folder parent);

    boolean existsByOwnerAndInVaultFalseAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(User owner, String name);

    boolean existsByOwnerAndInVaultFalseAndParentAndNameIgnoreCaseAndTrashedFalse(User owner, Folder parent, String name);

    List<Folder> findByOwnerAndInVaultTrueAndParentIsNullAndTrashedFalseOrderByNameAsc(User owner);

    List<Folder> findByOwnerAndInVaultTrueAndParentAndTrashedFalseOrderByNameAsc(User owner, Folder parent);

    boolean existsByOwnerAndInVaultTrueAndParentIsNullAndNameIgnoreCaseAndTrashedFalse(User owner, String name);

    boolean existsByOwnerAndInVaultTrueAndParentAndNameIgnoreCaseAndTrashedFalse(User owner, Folder parent, String name);

    List<Folder> findByOwnerAndParent(User owner, Folder parent);
}
