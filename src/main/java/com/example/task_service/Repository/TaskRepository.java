package com.example.task_service.Repository;


import com.example.task_service.Entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByCreatedByAndDeletedAtIsNull(String createdBy);
    Optional<TaskEntity> findByTaskIdAndCreatedBy(UUID taskId, String createdBy);
    Optional<TaskEntity> findByTaskId(UUID taskId);
}