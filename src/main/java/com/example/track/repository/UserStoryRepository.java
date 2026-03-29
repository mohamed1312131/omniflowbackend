package com.example.track.repository;

import com.example.track.domain.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, UUID> {

    List<UserStory> findByProjectIdOrderByPositionAsc(UUID projectId);

    List<UserStory> findByProjectIdAndSprintIdOrderByPositionAsc(UUID projectId, UUID sprintId);

    List<UserStory> findByProjectIdAndSprintIdIsNullOrderByPositionAsc(UUID projectId);

    List<UserStory> findBySprintIdOrderByPositionAsc(UUID sprintId);

    @Query("SELECT DISTINCT s FROM UserStory s LEFT JOIN FETCH s.tasks t LEFT JOIN FETCH t.assignee WHERE s.sprint.id = :sprintId ORDER BY s.position ASC")
    List<UserStory> findBySprintIdWithTasksOrderByPositionAsc(@Param("sprintId") UUID sprintId);
}
