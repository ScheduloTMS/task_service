package com.example.task_service.DTO;

import java.util.List;

public class StudentAssignmentRequest {
    private List<String> studentIds;


    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }
}