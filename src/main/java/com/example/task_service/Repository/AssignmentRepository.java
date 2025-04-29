package com.example.task_service.Repository;


import com.example.task_service.Entity.AssignmentEntity;
import com.example.task_service.Entity.AssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentEntity, AssignmentId> {
    Optional<AssignmentEntity> findById(AssignmentId id);


    @Query("SELECT a FROM AssignmentEntity a WHERE a.id.taskId = :taskId AND a.deletedAt IS NULL")
    List<AssignmentEntity> findAllById_TaskId(@Param("taskId") UUID taskId);

    List<AssignmentEntity> findByIdTaskId(UUID taskId);

    Optional<AssignmentEntity> findByIdTaskIdAndIdUserId(UUID taskId, String userId);

    List<AssignmentEntity> findById_UserIdAndDeletedAtIsNull(String userId);

//    @Query("SELECT u FROM Users u JOIN AssignmentEntity a ON u.id = a.id.userId WHERE a.id.taskId = :taskId")
//    List<Users> findStudentsByTaskId(@Param("taskId") UUID taskId);
}