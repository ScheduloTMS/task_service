package com.example.task_service.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class AssignmentId implements Serializable {

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "user_id")
    private String userId;

    public AssignmentId() {}

    public AssignmentId(UUID taskId, String userId) {
        this.taskId = taskId;
        this.userId = userId;
    }


    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}