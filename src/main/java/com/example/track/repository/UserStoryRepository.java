package com.example.track.repository;

import com.example.track.domain.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, UUID> {
    
    List<UserStory> findByProjectIdOrderByPositionAsc(UUID projectId);
    
    List<UserStory> findByProjectIdAndSprintIdOrderByPositionAsc(UUID projectId, UUID sprintId);
    
    List<UserStory> findByProjectIdAndSprintIdIsNullOrderByPositionAsc(UUID projectId);
    
    List<UserStory> findBySprintIdOrderByPositionAsc(UUID sprintId);
}
