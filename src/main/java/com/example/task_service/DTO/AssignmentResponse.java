package com.example.task_service.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AssignmentResponse {
    private UUID taskId;
    private String userId;
    private String submissionStatus;
    private String score;
    private String fileStatus;
    private String submittedDate;
    private String profilePhoto;
    private String fileName;
    private String downloadUrl;


    public AssignmentResponse(UUID taskId, String userId, String submissionStatus, String score, String fileStatus, String fileName, String downloadUrl, LocalDateTime submittedDate) {
        this.taskId = taskId;
        this.userId = userId;
        this.submissionStatus = submissionStatus;
        this.score = score;
        this.fileStatus = fileStatus;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.submittedDate = formatSubmittedDate(submittedDate);
    }


    public AssignmentResponse(UUID taskId, String userId, String submissionStatus, String score, String fileStatus) {
        this.taskId = taskId;
        this.userId = userId;
        this.submissionStatus = submissionStatus;
        this.score = score;
        this.fileStatus = fileStatus;
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

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(String submissionStatus) {
        this.submissionStatus = submissionStatus;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }


    private String formatSubmittedDate(LocalDateTime submittedDate) {
        if (submittedDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return submittedDate.format(formatter);
        }
        return null;
    }
}