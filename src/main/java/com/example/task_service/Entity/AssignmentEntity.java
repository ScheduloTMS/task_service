package com.example.task_service.Entity;

import com.example.task_service.DTO.UserDTO;
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

    // Replace the Users dependency with simple fields for userId, name, and photo
    private String studentUserId;
    private String studentName;
    private String studentPhoto;

    public AssignmentEntity() {}

    public AssignmentEntity(UUID taskId, String userId, byte[] fileUploads, String submissionStatus, String score, String studentUserId, String studentName, String studentPhoto) {
        this.id = new AssignmentId(taskId, userId);
        this.fileUploads = fileUploads;
        this.submissionStatus = submissionStatus;
        this.submittedDate = LocalDateTime.now();
        this.score = score;
        this.updatedAt = null;
        this.studentUserId = studentUserId;
        this.studentName = studentName;
        this.studentPhoto = studentPhoto;
    }

    // Getters and setters for the new fields
    public String getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(String studentUserId) {
        this.studentUserId = studentUserId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentPhoto() {
        return studentPhoto;
    }

    public void setStudentPhoto(String studentPhoto) {
        this.studentPhoto = studentPhoto;
    }

    public AssignmentId getId() {
        return id;
    }

    public void setId(AssignmentId id) {
        this.id = id;
    }

    public byte[] getFileUploads() {
        return fileUploads;
    }

    public void setFileUploads(byte[] fileUploads) {
        this.fileUploads = fileUploads;
    }

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(String submissionStatus) {
        this.submissionStatus = submissionStatus;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setsubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }

    // Corrected getStudent() method to return UserDTO
    public UserDTO getStudent() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(this.studentUserId);
        userDTO.setName(this.studentName);
        userDTO.setPhoto(this.studentPhoto);
        userDTO.setEmail(null); // Email is not available in the AssignmentEntity, so setting it to null
        userDTO.setRole(null);  // Role is not available in the AssignmentEntity, so setting it to null
        userDTO.setAuthorities(null);  // Authorities are not available, setting to null
        return userDTO;
    }
}
