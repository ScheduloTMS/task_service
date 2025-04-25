package com.example.task_service.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "task_id")
    private UUID taskId;

    @NotNull(message = "Title cannot be null")
    private String title;

    @NotNull(message = "Description cannot be null")
    private String description;

    private LocalDate createdAt;

    @NotNull(message = "Due date cannot be null")
    private LocalDate dueDate;

    @Lob
    private byte[] file;

    private LocalDateTime deletedAt;

    private String createdBy;




    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public byte[] getFile() { return file; }
    public void setFile(byte[] file) { this.file = file; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }

}