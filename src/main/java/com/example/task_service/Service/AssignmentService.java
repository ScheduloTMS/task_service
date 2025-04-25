package com.example.task_service.Service;


import com.example.task_service.DTO.StudentDTO;
import com.example.task_service.Entity.AssignmentEntity;
import com.example.task_service.Entity.AssignmentId;
import com.example.task_service.Entity.TaskEntity;
import com.example.task_service.Repository.AssignmentRepository;
import com.example.task_service.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private TaskRepository taskRepository;



    @Transactional
    public void saveAssignment(UUID taskId, String userId, byte[] fileUploads, String submissionStatus, String score) {
        AssignmentEntity assignment = new AssignmentEntity(taskId, userId, fileUploads, submissionStatus, score);
        assignment.setsubmittedDate(LocalDateTime.now());

        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        if (taskEntity.getDueDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new RuntimeException("Cannot submit assignment after the due date");
        }

        assignmentRepository.save(assignment);
    }

    @Transactional
    public void updateAssignment(UUID taskId, String userId, byte[] fileData, String submissionStatus, String score) {
        AssignmentId id = new AssignmentId(taskId, userId);
        AssignmentEntity assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));


        assignment.setSubmissionStatus(submissionStatus);
        assignment.setScore(score);
        assignment.setUpdatedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public Optional<AssignmentEntity> getAssignmentByUserAndTask(String userId, UUID taskId) {

        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));


        if (taskEntity.getCreatedBy().equals(userId)) {

            return Optional.of(new AssignmentEntity(taskId, userId, null, "Not Submitted", null));
        }


        AssignmentId assignmentId = new AssignmentId(taskId, userId);
        return assignmentRepository.findById(assignmentId);
    }

    public boolean isStudentAssignedToTask(UUID taskId, String userId) {
        return assignmentRepository.existsById(new AssignmentId(taskId, userId));
    }

    public boolean isAssignedToTask(UUID taskId, String userId) {
        boolean exists = assignmentRepository.existsById(new AssignmentId(taskId, userId));
        System.out.println("Assignment exists for user " + userId + " and task " + taskId + ": " + exists);
        return exists;
    }


    @Transactional(readOnly = true)
    public boolean hasStudentSubmittedFile(UUID taskId, String userId) {
        AssignmentId id = new AssignmentId(taskId, userId);
        return assignmentRepository.findById(id)
                .map(assignment -> assignment.getFileUploads() != null)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<AssignmentEntity> getAssignmentsForStudent(String email) {
        Users user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return assignmentRepository.findById_UserIdAndDeletedAtIsNull(user.getUserId());
    }


    @Transactional(readOnly = true)
    public String getUserIdByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email))
                .getUserId();
    }


    @Transactional
    public void assignStudents(UUID taskId, List<String> studentIds, String mentorEmail) throws AccessDeniedException {

        Users mentor = userRepository.findByEmailAndDeletedAtIsNull(mentorEmail)
                .orElseThrow(() -> new RuntimeException("Mentor not found or has been deleted with email: " + mentorEmail));

        String mentorId = mentor.getUserId();


        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        if (!task.getCreatedBy().equals(mentorEmail)) {
            throw new AccessDeniedException("You are not authorized to assign students to this task");
        }


        AssignmentId mentorAssignmentId = new AssignmentId(taskId, mentorId);
        if (!assignmentRepository.existsById(mentorAssignmentId)) {
            AssignmentEntity mentorAssignment = new AssignmentEntity();
            mentorAssignment.setId(mentorAssignmentId);
            mentorAssignment.setTask(task);
            mentorAssignment.setStudent(mentor);
            mentorAssignment.setFileUploads(null);
            mentorAssignment.setSubmissionStatus("N/A");
            mentorAssignment.setScore(null);
            mentorAssignment.setsubmittedDate(null);
            mentorAssignment.setUpdatedAt(LocalDateTime.now());

            assignmentRepository.save(mentorAssignment);
        }


        for (String studentId : studentIds) {
            Users student = userRepository.findById(studentId)
                    .filter(user -> user.getDeletedAt() == null)
                    .orElseThrow(() -> new RuntimeException("User not found or has been deleted with ID: " + studentId));

            AssignmentId assignmentId = new AssignmentId(taskId, studentId);

            if (assignmentRepository.existsById(assignmentId)) {
                throw new RuntimeException("Student with ID " + studentId + " is already assigned to this task");
            }

            AssignmentEntity assignment = new AssignmentEntity();
            assignment.setId(assignmentId);
            assignment.setTask(task);
            assignment.setStudent(student);
            assignment.setFileUploads(null);
            assignment.setSubmissionStatus("Not Submitted");
            assignment.setScore(null);
            assignment.setsubmittedDate(LocalDateTime.now()); // Set the submission date

            assignment.setUpdatedAt(LocalDateTime.now());

            assignmentRepository.save(assignment);
        }
    }



    public List<AssignmentEntity> getAssignmentsByTaskId(UUID taskId) {
        return assignmentRepository.findByIdTaskId(taskId);
    }



    public AssignmentEntity getAssignmentByTaskAndStudent(UUID taskId, String userId) {
        return assignmentRepository.findByIdTaskIdAndIdUserId(taskId, userId).orElse(null);
    }
    public List<StudentDTO> getStudentsAssignedToTask(UUID taskId, String mentorEmail) {


        List<Users> assignedStudents = assignmentRepository.findStudentsByTaskId(taskId);


        return assignedStudents.stream()
                .map(student -> new StudentDTO(
                        student.getUserId(),
                        student.getName(),
                        student.getPhoto()

                ))
                .collect(Collectors.toList());
    }


}