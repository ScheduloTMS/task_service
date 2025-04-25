package com.example.task_service.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "remarks")
public class RemarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID remarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "task_id", referencedColumnName = "task_id"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    private AssignmentEntity assignment;

    @Column(nullable = false)
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;


    public RemarkEntity() {}

    public RemarkEntity(AssignmentEntity assignment, String comment) {
        this.assignment = assignment;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }


    public UUID getRemarkId() { return remarkId; }
    public void setRemarkId(UUID remarkId) { this.remarkId = remarkId; }

    public AssignmentEntity getAssignment() { return assignment; }
    public void setAssignment(AssignmentEntity assignment) { this.assignment = assignment; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}