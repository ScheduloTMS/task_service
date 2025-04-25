package com.example.task_service.Service;


import com.example.task_service.Entity.AssignmentEntity;
import com.example.task_service.Entity.TaskEntity;
import com.example.task_service.Repository.AssignmentRepository;
import com.example.task_service.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;



    public TaskEntity getTaskById(UUID taskId) {
        try {
            return taskRepository.findByTaskId(taskId)
                    .filter(task -> task.getDeletedAt() == null)
                    .orElseThrow(() -> new RuntimeException("Task not found"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve task: " + e.getMessage());
        }
    }

    @Transactional
    public TaskEntity createTask(String title, String description, LocalDate dueDate, byte[] fileData, String createdBy) {
        try {
            if (taskRepository.findByCreatedByAndDeletedAtIsNull(createdBy)
                    .stream().anyMatch(task -> task.getTitle().equalsIgnoreCase(title))) {
                throw new RuntimeException("Task with the same title already exists");
            }

            TaskEntity task = new TaskEntity();
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            task.setFile(fileData);
            task.setCreatedBy(createdBy);
            return taskRepository.save(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    public TaskEntity getTaskByIdForUser(UUID taskId, String username) {
        try {
            TaskEntity task = getTaskById(taskId);
            if (!task.getCreatedBy().equals(username)) {
                throw new RuntimeException("You are not authorized to access this task");
            }
            return task;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve task for user: " + e.getMessage());
        }
    }

    public List<TaskEntity> getAllTasksForUser(String username) {
        try {
            return taskRepository.findByCreatedByAndDeletedAtIsNull(username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve tasks for user: " + e.getMessage());
        }
    }




    @Transactional
    public TaskEntity updateTask(UUID taskId, String title, String description, LocalDate dueDate, byte[] fileData, String username) {
        try {
            TaskEntity task = getTaskByIdForUser(taskId, username);

            if (task.getDeletedAt() != null) {
                throw new RuntimeException("Cannot update a deleted task");
            }

            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            if (fileData != null) {
                task.setFile(fileData);
            }

            return taskRepository.save(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteTask(UUID taskId, String username) {
        try {
            TaskEntity task = getTaskByIdForUser(taskId, username);

            if (task.getDeletedAt() != null) {
                throw new RuntimeException("Task already deleted");
            }

            task.setDeletedAt(java.time.LocalDateTime.now());


            List<AssignmentEntity> assignments = assignmentRepository.findAllById_TaskId(taskId);
            for (AssignmentEntity assignment : assignments) {
                assignment.setDeletedAt(java.time.LocalDateTime.now());
                assignment.setUpdatedAt(java.time.LocalDateTime.now());
                assignmentRepository.save(assignment);
            }

            taskRepository.save(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task: " + e.getMessage());
        }
    }



}
