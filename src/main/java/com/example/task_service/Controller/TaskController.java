package com.example.task_service.Controller;


import com.example.task_service.DTO.ApiResponse;
import com.example.task_service.DTO.AssignmentResponse;
import com.example.task_service.DTO.TaskWithStatusDTO;
import com.example.task_service.Entity.AssignmentEntity;
import com.example.task_service.Entity.TaskEntity;
import com.example.task_service.Service.AssignmentService;
import com.example.task_service.Service.TaskService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    @Autowired
    private AssignmentService assignmentService;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse> createTask(
            @RequestParam @NotNull(message = "Title cannot be null") String title,
            @RequestParam @NotNull(message = "Description cannot be null") String description,
            @RequestParam @NotNull(message = "Due date cannot be null") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            byte[] fileData = (file != null) ? file.getBytes() : null;
            TaskEntity createdTask = taskService.createTask(title, description, dueDate, fileData, userDetails.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("success", 201, "Task created successfully", createdTask));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("error", 400, "Failed to create task: " + e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", 500, e.getMessage(), null));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getAllTasks(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            String role = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .findFirst().orElse("ROLE_STUDENT");

            List<TaskWithStatusDTO> responseList;

            if (role.equals("ROLE_MENTOR")) {
                List<TaskEntity> tasks = taskService.getAllTasksForUser(email);

                responseList = tasks.stream().map(task -> {
                    List<AssignmentEntity> assignments = assignmentService.getAssignmentsByTaskId(task.getTaskId());

                    long total = assignments.size();
                    long reviewed = assignments.stream().filter(a -> a.getScore() != null).count();
                    long overdue = assignments.stream().filter(a ->
                            a.getFileUploads() == null &&
                                    task.getDueDate().isBefore(LocalDate.now())
                    ).count();

                    String status;
                    if (total == 0) {
                        status = "To Do";
                    } else if (overdue > 0) {
                        status = "Overdue";
                    } else if (reviewed == total) {
                        status = "Completed";
                    } else if (reviewed > 0) {
                        status = "In Progress";
                    } else {
                        status = "To Do";
                    }

                    return new TaskWithStatusDTO(task, status);
                }).collect(Collectors.toList());

            } else {
                List<AssignmentEntity> assignments = assignmentService.getAssignmentsForStudent(email);

                responseList = assignments.stream().map(assignment -> {
                    TaskEntity task = taskService.getTaskById(assignment.getId().getTaskId());

                    String status;
                    if (assignment.getFileUploads() == null && task.getDueDate().isBefore(LocalDate.now())) {
                        status = "Overdue";
                    } else if (assignment.getFileUploads() == null) {
                        status = "To Do";
                    } else if (assignment.getScore() != null) {
                        status = "Completed";
                    } else {
                        status = "In Progress";
                    }

                    return new TaskWithStatusDTO(task, status);
                }).collect(Collectors.toList());
            }

            return ResponseEntity.ok(new ApiResponse("success", 200, "Tasks retrieved successfully", responseList));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("error", 404, e.getMessage(), null));
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse> getTaskById(@PathVariable UUID taskId,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            TaskEntity task;
            String role = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .findFirst().orElse("ROLE_STUDENT");


            Users user = userService.findByEmail(userDetails.getUsername());
            String userId = user.getUserId();
            String status;

            if (role.equals("ROLE_MENTOR")) {
                task = taskService.getTaskByIdForUser(taskId, user.getEmail());

                List<AssignmentEntity> assignments = assignmentService.getAssignmentsByTaskId(task.getTaskId());
                long total = assignments.size();
                long reviewed = assignments.stream().filter(a -> a.getScore() != null).count();
                long overdue = assignments.stream().filter(a ->
                        a.getFileUploads() == null &&
                                task.getDueDate().isBefore(LocalDate.now())
                ).count();

                if (total == 0) {
                    status = "To Do";
                } else if (overdue > 0) {
                    status = "Overdue";
                } else if (reviewed == total) {
                    status = "Completed";
                } else if (reviewed > 0) {
                    status = "In Progress";
                } else {
                    status = "To Do";
                }

                List<AssignmentResponse> studentResponses = assignments.stream().map(a -> {
                    String studentName = a.getStudent() != null ? a.getStudent().getName() : a.getId().getUserId();
                    return new AssignmentResponse(
                            a.getId().getTaskId(),
                            studentName,
                            a.getSubmissionStatus(),
                            a.getScore(),
                            a.getFileUploads() != null ? "Submitted" : "Not Submitted"
                    );
                }).toList();

                TaskWithStatusDTO responseDTO = new TaskWithStatusDTO(task, status, studentResponses);
                return ResponseEntity.ok(new ApiResponse("success", 200, "Task retrieved successfully", responseDTO));

            } else {

                if (!assignmentService.isStudentAssignedToTask(taskId, userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ApiResponse("error", 403, "You are not assigned to this task", null));
                }

                task = taskService.getTaskById(taskId);
                AssignmentEntity assignment = assignmentService.getAssignmentByTaskAndStudent(taskId, userId);

                if (assignment == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse("error", 404, "Assignment not found for student and task", null));
                }

                if (assignment.getFileUploads() == null && task.getDueDate().isBefore(LocalDate.now())) {
                    status = "Overdue";
                } else if (assignment.getFileUploads() == null) {
                    status = "To Do";
                } else if (assignment.getScore() != null) {
                    status = "Completed";
                } else {
                    status = "In Progress";
                }

                TaskWithStatusDTO responseDTO = new TaskWithStatusDTO(task, status);
                return ResponseEntity.ok(new ApiResponse("success", 200, "Task retrieved successfully", responseDTO));
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("error", 404, e.getMessage(), null));
        }
    }


    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('MENTOR')")
    @Transactional
    public ResponseEntity<ApiResponse> updateTask(
            @PathVariable UUID taskId,
            @RequestParam @NotNull String title,
            @RequestParam @NotNull String description,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            byte[] fileData = (file != null) ? file.getBytes() : null;
            TaskEntity updatedTask = taskService.updateTask(taskId, title, description, dueDate, fileData, userDetails.getUsername());

            return ResponseEntity.ok(new ApiResponse("success", 200, "Task updated successfully", updatedTask));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("error", 400, "Failed to update task: " + e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("error", 404, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse> deleteTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            taskService.deleteTask(taskId, userDetails.getUsername());
            return ResponseEntity.ok(new ApiResponse("success", 200, "Task deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("error", 404, e.getMessage(), null));
        }
    }
}