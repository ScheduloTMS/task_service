package com.example.task_service.DTO;



import com.example.task_service.Entity.TaskEntity;

import java.util.List;


public class TaskWithStatusDTO {
    private TaskEntity task;
    private String status;

    private List<AssignmentResponse> assignedStudents;

    public TaskWithStatusDTO(TaskEntity task, String status) {
        this.task = task;
        this.status = status;
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TaskWithStatusDTO(TaskEntity task, String status, List<AssignmentResponse> students) {
        this(task, status);
        this.assignedStudents = students;
    }
}
