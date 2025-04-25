package com.example.task_service.Controller;


import com.example.task_service.DTO.ApiResponse;
import com.example.task_service.DTO.AssignmentResponse;
import com.example.task_service.DTO.StudentAssignmentRequest;
import com.example.task_service.Entity.AssignmentEntity;
import com.example.task_service.Service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping("/{taskId}/assign")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse> assignStudents(
            @PathVariable UUID taskId,
            @RequestBody StudentAssignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            assignmentService.assignStudents(taskId, request.getStudentIds(), userDetails.getUsername());

            return ResponseEntity.ok(new ApiResponse(
                    "success",
                    200,
                    "Students assigned to task successfully",
                    null
            ));

        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(
                            "error",
                            403,
                            ex.getMessage(),
                            null
                    ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(
                            "error",
                            400,
                            ex.getMessage(),
                            null
                    ));
        }
    }


    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> createAssignment(
            @RequestParam UUID taskId,
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String email = userDetails.getUsername();
            String userId = assignmentService.getUserIdByEmail(email);
            System.out.println("User ID fetched for " + email + ": " + userId);
            if (!assignmentService.isAssignedToTask(taskId, userId)) {
                ApiResponse response = new ApiResponse(
                        "error",
                        HttpStatus.FORBIDDEN.value(),
                        "You are not assigned to this task",
                        null
                );
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            byte[] fileData = (file != null) ? file.getBytes() : null;
            String submissionStatus = "Submitted, Marked for review";
            String score = null;

            assignmentService.saveAssignment(taskId, userId, fileData, submissionStatus, score);

            ApiResponse response = new ApiResponse(
                    "success",
                    HttpStatus.CREATED.value(),
                    "Assignment created successfully",
                    null
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            ApiResponse response = new ApiResponse(
                    "error",
                    HttpStatus.BAD_REQUEST.value(),
                    "Error processing file",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse> updateAssignment(
            @RequestParam UUID taskId,
            @RequestParam String userId,
            @RequestParam String score) {

        try {
            if (!assignmentService.hasStudentSubmittedFile(taskId, userId)) {
                ApiResponse response = new ApiResponse(
                        "error",
                        HttpStatus.FORBIDDEN.value(),
                        "Student has not submitted a file for this task",
                        null
                );
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String submissionStatus = "Reviewed";
            assignmentService.updateAssignment(taskId, userId, null, submissionStatus, score);

            ApiResponse response = new ApiResponse(
                    "success",
                    HttpStatus.OK.value(),
                    "Score uploaded successfully",
                    null
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse response = new ApiResponse(
                    "error",
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse> getAssignment(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        String userId = assignmentService.getUserIdByEmail(email);

        try {
            Optional<AssignmentEntity> assignment = assignmentService.getAssignmentByUserAndTask(userId, taskId);

            if (assignment.isPresent()) {
                AssignmentEntity assignmentEntity = assignment.get();

                return ResponseEntity.ok(new ApiResponse(
                        "success",
                        200,
                        "Assignment retrieved successfully",
                        new AssignmentResponse(
                                assignmentEntity.getId().getTaskId(),
                                assignmentEntity.getId().getUserId(),
                                assignmentEntity.getSubmissionStatus(),

                                assignmentEntity.getScore(),
                                assignmentEntity.getFileUploads() != null ? "File attached" : "No file"

                        )
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(
                                "error",
                                404,
                                "Assignment not found or not assigned to user",
                                null
                        ));
            }

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(
                            "error",
                            404,
                            ex.getMessage(),
                            null
                    ));
        }
    }

    @GetMapping("/{taskId}/students")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse> getAssignedStudentsForTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String mentorEmail = userDetails.getUsername();
        var students = assignmentService.getStudentsAssignedToTask(taskId, mentorEmail);

        return ResponseEntity.ok(new ApiResponse(
                "success",
                200,
                "Assigned students retrieved successfully",
                students
        ));
    }


    @GetMapping("/{taskId}/{studentId}")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse> getStudentAssignment(
            @PathVariable UUID taskId,
            @PathVariable String studentId) {

        try {
            Optional<AssignmentEntity> assignment = assignmentService.getAssignmentByUserAndTask(studentId, taskId);

            if (assignment.isPresent()) {
                AssignmentEntity assignmentEntity = assignment.get();

                String fileStatus = "No file";
                String fileName = null;
                String downloadUrl = null;

                if (assignmentEntity.getFileUploads() != null) {
                    fileStatus = "File attached";
                    fileName = "assignment_"  + studentId + ".pdf"; // or .docx etc.
                    downloadUrl = "/api/files/download/" + taskId + "/" + studentId;
                }

                AssignmentResponse response = new AssignmentResponse(
                        assignmentEntity.getId().getTaskId(),
                        assignmentEntity.getId().getUserId(),
                        assignmentEntity.getSubmissionStatus(),
                        assignmentEntity.getScore(),
                        assignmentEntity.getFileUploads() != null ? "File attached" : "No file",
                        assignmentEntity.getFileUploads() != null ? "assignment_"  + studentId + ".pdf" : null,
                        "/api/files/download/" + taskId + "/" + studentId,
                        assignmentEntity.getSubmittedDate()  // Pass LocalDateTime here
                );

                return ResponseEntity.ok(new ApiResponse(
                        "success",
                        200,
                        "Assignment retrieved successfully",
                        response
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(
                                "error",
                                404,
                                "Assignment not found for student",
                                null
                        ));
            }

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(
                            "error",
                            404,
                            ex.getMessage(),
                            null
                    ));
        }
    }

}