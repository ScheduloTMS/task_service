package com.example.task_service.Repository;


import com.example.task_service.Entity.RemarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RemarkRepository extends JpaRepository<RemarkEntity, UUID> {

    @Query("SELECT r FROM RemarkEntity r WHERE r.assignment.id.taskId = :taskId AND r.assignment.id.userId = :userId")
    List<RemarkEntity> findByAssignment_TaskIdAndAssignment_UserId(
            @Param("taskId") UUID taskId,
            @Param("userId") String userId
    );

    @Query("SELECT r FROM RemarkEntity r WHERE r.assignment.id.taskId = :taskId")
    List<RemarkEntity> findByAssignment_TaskId(@Param("taskId") UUID taskId);

}