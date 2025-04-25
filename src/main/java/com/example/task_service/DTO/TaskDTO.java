package com.example.task_service.DTO;

import java.time.LocalDate;

public class TaskDTO {
    private String title;
    private String description;
    private LocalDate dueDate;
    private byte[] file;


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public byte[] getFile() { return file; }
    public void setFile(byte[] file) { this.file = file; }
}