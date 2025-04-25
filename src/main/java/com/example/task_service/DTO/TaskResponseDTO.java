package com.example.task_service.DTO;

import java.util.List;

public class TaskResponseDTO {
    private String id;
    private String title;
    private String description;
    private String mentor;
    private String dueDate;
    private String createdAt;
    private String status;
    private List<String> assignedStudents;


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMentor() { return mentor; }
    public void setMentor(String mentor) { this.mentor = mentor; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getAssignedStudents() { return assignedStudents; }
    public void setAssignedStudents(List<String> assignedStudents) { this.assignedStudents = assignedStudents; }
}
