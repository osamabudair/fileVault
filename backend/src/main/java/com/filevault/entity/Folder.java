package com.filevault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "folders", indexes = {
    @Index(name = "idx_folders_owner_parent", columnList = "owner_id, parent_id"),
    @Index(name = "idx_folders_owner_vault_parent", columnList = "owner_id, in_vault, parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_folders_parent"))
    private Folder parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_folders_owner"))
    private User owner;

    @Column(name = "in_vault", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean inVault = false;

    @Column(name = "trashed", nullable = false)
    @Builder.Default
    private boolean trashed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
