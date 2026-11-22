package com.filevault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "file_collaborators",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_file_collaborators_file_user", columnNames = {"file_id", "shared_with_user_id"})
    },
    indexes = {
        @Index(name = "idx_file_collaborators_user", columnList = "shared_with_user_id"),
        @Index(name = "idx_file_collaborators_file", columnList = "file_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCollaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false, foreignKey = @ForeignKey(name = "fk_file_collaborators_file"))
    private FileMetadata file;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_file_collaborators_user"))
    private User sharedWithUser;

    @Column(name = "permission", nullable = false, length = 24)
    @Builder.Default
    private String permission = "VIEW";

    @CreationTimestamp
    @Column(name = "shared_at", nullable = false, updatable = false)
    private Instant sharedAt;
}
