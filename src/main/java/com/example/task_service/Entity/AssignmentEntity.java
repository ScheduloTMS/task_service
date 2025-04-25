package com.example.task_service.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {

    @EmbeddedId
    private AssignmentId id;

    @Lob
    private byte[] fileUploads;
    private String submissionStatus;
    private LocalDateTime submittedDate;
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private String score;

    @Column(nullable = true)
    private LocalDateTime deletedAt;



    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "task_id", referencedColumnName = "task_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TaskEntity task;


    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users student;


    public AssignmentEntity() {}

    public AssignmentEntity(UUID taskId, String userId, byte[] fileUploads, String submissionStatus, String score) {
        this.id = new AssignmentId(taskId, userId);
        this.fileUploads = fileUploads;
        this.submissionStatus = submissionStatus;
        this.submittedDate = LocalDateTime.now();
        this.score = score;
        this.updatedAt = null;

    }

    public Users getStudent() {
        return student;
    }

    public void setStudent(Users student) {
        this.student = student;
    }


    public AssignmentId getId() { return id; }
    public void setId(AssignmentId id) { this.id = id; }

    public byte[] getFileUploads() { return fileUploads; }
    public void setFileUploads(byte[] fileUploads) { this.fileUploads = fileUploads; }

    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }

    public LocalDateTime getSubmittedDate() { return submittedDate; }
    public void setsubmittedDate(LocalDateTime submittedDate) { this.submittedDate = submittedDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }

}