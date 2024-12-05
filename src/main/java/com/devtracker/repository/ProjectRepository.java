package com.devtracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.devtracker.model.Project;;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByActiveTrue();

    boolean existsByPath(String path);

    Optional<Project> findByPath(String path);

}
