package com.example.track.repository;

import com.example.track.domain.ProjectMember;
import com.example.track.domain.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    
    List<ProjectMember> findByProjectId(UUID projectId);
    
    List<ProjectMember> findByUserId(UUID userId);
    
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
    
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
    
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.projectId = :projectId")
    long countByProjectId(UUID projectId);
}
