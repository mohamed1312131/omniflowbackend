package com.example.track.repository;

import com.example.track.domain.Sprint;
import com.example.track.domain.Sprint.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    
    List<Sprint> findByProjectIdOrderByStartDateDesc(UUID projectId);
    
    Optional<Sprint> findByProjectIdAndStatus(UUID projectId, SprintStatus status);
    
    boolean existsByProjectIdAndStatus(UUID projectId, SprintStatus status);
}
