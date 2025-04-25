package com.example.task_service.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public class RemarkDTO {
    private UUID remarkId;
    private UUID taskId;
    private String userId;
    private String userName;
    private String userProfilePhoto;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public RemarkDTO(UUID remarkId, UUID taskId, String userId, String userName, String userProfilePhoto,
                     String comment, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.remarkId = remarkId;
        this.taskId = taskId;
        this.userId = userId;
        this.userName = userName;
        this.userProfilePhoto = userProfilePhoto;
        this.comment = comment;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public UUID getRemarkId() {
        return remarkId;
    }

    public void setRemarkId(UUID remarkId) {
        this.remarkId = remarkId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfilePhoto() {
        return userProfilePhoto;
    }

    public void setUserProfilePhoto(String userProfilePhoto) {
        this.userProfilePhoto = userProfilePhoto;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}