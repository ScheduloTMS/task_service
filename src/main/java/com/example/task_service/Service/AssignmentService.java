package com.example.task_service.Service;

import com.example.task_service.DTO.UserDTO;
import com.example.task_service.DTO.StudentDTO;
import com.example.task_service.Entity.AssignmentEntity;
import com.example.task_service.Entity.AssignmentId;
import com.example.task_service.Entity.TaskEntity;
import com.example.task_service.Repository.AssignmentRepository;
import com.example.task_service.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate; // RestTemplate to make HTTP requests to User Service

    private final String userServiceUrl = "http://user-service/api/users"; // Assuming the User Service URL

    @Transactional
    public void saveAssignment(UUID taskId, String userId, byte[] fileUploads, String submissionStatus, String score) {
        // Fetch user details using UserDTO
        UserDTO userDTO = restTemplate.getForObject(userServiceUrl + "/" + userId, UserDTO.class);
        if (userDTO == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        AssignmentEntity assignment = new AssignmentEntity(
                taskId,
                userId,
                fileUploads,
                submissionStatus,
                score,
                userDTO.getUserId(),
                userDTO.getName(),
                userDTO.getPhoto() // Pass the additional fields
        );
        assignment.setsubmittedDate(LocalDateTime.now());

        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        if (taskEntity.getDueDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new RuntimeException("Cannot submit assignment after the due date");
        }

        assignmentRepository.save(assignment);
    }


    @Transactional
    public boolean isAssignedToTask(UUID taskId, String userId) {
        // Create the AssignmentId from taskId and userId
        AssignmentId assignmentId = new AssignmentId(taskId, userId);

        // Check if an assignment exists with the given AssignmentId
        return assignmentRepository.existsById(assignmentId);
    }

    @Transactional
    public void updateAssignment(UUID taskId, String userId, byte[] fileData, String submissionStatus, String score) {
        AssignmentId id = new AssignmentId(taskId, userId);
        AssignmentEntity assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setSubmissionStatus(submissionStatus);
        assignment.setScore(score);
        assignment.setUpdatedAt(LocalDateTime.now());

        // Fetch user details using UserDTO
        UserDTO userDTO = restTemplate.getForObject(userServiceUrl + "/" + userId, UserDTO.class);
        if (userDTO == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public Optional<AssignmentEntity> getAssignmentByUserAndTask(String userId, UUID taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Fetch user details using UserDTO
        UserDTO userDTO = restTemplate.getForObject(userServiceUrl + "/" + userId, UserDTO.class);
        if (userDTO == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        AssignmentId assignmentId = new AssignmentId(taskId, userId);
        return assignmentRepository.findById(assignmentId);
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
        // Fetch user details using UserDTO
        UserDTO userDTO = restTemplate.getForObject(userServiceUrl + "/email/" + email, UserDTO.class);
        if (userDTO == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        return assignmentRepository.findById_UserIdAndDeletedAtIsNull(userDTO.getUserId());
    }

    @Transactional(readOnly = true)
    public String getUserIdByEmail(String email) {
        // Fetch user details using UserDTO
        UserDTO userDTO = restTemplate.getForObject(userServiceUrl + "/email/" + email, UserDTO.class);
        if (userDTO == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        return userDTO.getUserId();
    }

    @Transactional
    public void assignStudents(UUID taskId, List<String> studentIds, String mentorEmail) throws AccessDeniedException {
        // Fetch mentor details using UserDTO
        UserDTO mentorDTO = restTemplate.getForObject(userServiceUrl + "/email/" + mentorEmail, UserDTO.class);
        if (mentorDTO == null) {
            throw new RuntimeException("Mentor not found with email: " + mentorEmail);
        }

        String mentorId = mentorDTO.getUserId();

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        if (!task.getCreatedBy().equals(mentorEmail)) {
            throw new AccessDeniedException("You are not authorized to assign students to this task");
        }

        // Check if the mentor is already assigned
        AssignmentId mentorAssignmentId = new AssignmentId(taskId, mentorId);
        if (!assignmentRepository.existsById(mentorAssignmentId)) {
            AssignmentEntity mentorAssignment = new AssignmentEntity();
            mentorAssignment.setId(mentorAssignmentId);
            mentorAssignment.setTask(task);
            mentorAssignment.setSubmissionStatus("N/A");
            mentorAssignment.setScore(null);
            mentorAssignment.setsubmittedDate(null);
            mentorAssignment.setUpdatedAt(LocalDateTime.now());

            assignmentRepository.save(mentorAssignment);
        }

        // Now assign students to the task
        for (String studentId : studentIds) {
            // Fetch student details using UserDTO
            UserDTO studentDTO = restTemplate.getForObject(userServiceUrl + "/" + studentId, UserDTO.class);
            if (studentDTO == null) {
                throw new RuntimeException("User not found with ID: " + studentId);
            }

            AssignmentId assignmentId = new AssignmentId(taskId, studentId);

            if (assignmentRepository.existsById(assignmentId)) {
                throw new RuntimeException("Student with ID " + studentId + " is already assigned to this task");
            }

            AssignmentEntity assignment = new AssignmentEntity();
            assignment.setId(assignmentId);
            assignment.setTask(task);
            // Set individual student fields directly
            assignment.setStudentUserId(studentDTO.getUserId());
            assignment.setStudentName(studentDTO.getName());
            assignment.setStudentPhoto(studentDTO.getPhoto());
            assignment.setFileUploads(null);
            assignment.setSubmissionStatus("Not Submitted");
            assignment.setScore(null);
            assignment.setsubmittedDate(LocalDateTime.now());
            assignment.setUpdatedAt(LocalDateTime.now());

            assignmentRepository.save(assignment);
        }
    }

    @Transactional(readOnly = true)
    public boolean isStudentAssignedToTask(UUID taskId, String userId) {
        // Create the AssignmentId from taskId and userId
        AssignmentId assignmentId = new AssignmentId(taskId, userId);

        // Check if an assignment exists with the given AssignmentId
        return assignmentRepository.existsById(assignmentId);
    }


    @Transactional(readOnly = true)
    public List<AssignmentEntity> getAssignmentsByTaskId(UUID taskId) {
        return assignmentRepository.findByIdTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public AssignmentEntity getAssignmentByTaskAndStudent(UUID taskId, String userId) {
        return assignmentRepository.findByIdTaskIdAndIdUserId(taskId, userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getStudentsAssignedToTask(UUID taskId, String mentorEmail) {
        // Fetch mentor details using UserDTO
        UserDTO mentorDTO = restTemplate.getForObject(userServiceUrl + "/email/" + mentorEmail, UserDTO.class);
        if (mentorDTO == null) {
            throw new RuntimeException("Mentor not found with email: " + mentorEmail);
        }

        List<AssignmentEntity> assignedAssignments = assignmentRepository.findByIdTaskId(taskId);
        List<StudentDTO> students = assignedAssignments.stream()
                .map(assignment -> new StudentDTO(
                        assignment.getStudent().getUserId(),
                        assignment.getStudent().getName(),
                        assignment.getStudent().getPhoto()
                ))
                .collect(Collectors.toList());

        return students;
    }
}
