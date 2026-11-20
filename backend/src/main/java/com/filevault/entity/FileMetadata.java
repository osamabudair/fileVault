package com.filevault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata", indexes = {
    @Index(name = "idx_file_metadata_owner_trashed", columnList = "owner_id, trashed"),
    @Index(name = "idx_file_metadata_owner_category", columnList = "owner_id, category"),
    @Index(name = "idx_file_metadata_owner_favorite", columnList = "owner_id, favorite")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, unique = true, length = 255)
    private String storedName;

    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private FileCategory category;

    @Column(name = "favorite", nullable = false)
    @Builder.Default
    private boolean favorite = false;

    @Column(name = "trashed", nullable = false)
    @Builder.Default
    private boolean trashed = false;

    @Column(name = "trashed_at")
    private Instant trashedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_file_metadata_owner"))
    private User owner;
}
