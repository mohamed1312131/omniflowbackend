package com.example.track.repository;

import com.example.track.domain.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, UUID> {
    
    List<SubTask> findByTaskIdOrderByPositionAsc(UUID taskId);
}
