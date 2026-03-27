package com.example.track.repository;

import com.example.track.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    // Module 1: No additional query methods needed
    // All projects are fetched via findAll() and filtered in service layer
}
