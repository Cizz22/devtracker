package com.devtracker.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.devtracker.dto.ProjectRequest;
import com.devtracker.dto.ProjectResponse;
import com.devtracker.model.Project;
import com.devtracker.repository.ProjectRepository;;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final ProjectRepository repository;

    public List<ProjectResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProjectResponse createProject(ProjectRequest request) {
        validateProjectPath(request.getPath());

        Project project = Project.builder()
                .name(request.getName())
                .path(request.getPath())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Project savedProject = repository.save(project);
        log.info("Project created: {}", savedProject);
        return mapToResponse(savedProject);
    }

    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        Project project = getProjectOrThrow(id);

        if (!project.getPath().equals(request.getPath())) {
            validateProjectPath(request.getPath());
        }

        project.setName(request.getName());
        project.setPath(request.getPath());
        project.setDescription(request.getDescription());
        project.setActive(request.getActive() != null ? request.getActive() : true);

        Project updatedProject = repository.save(project);
        return mapToResponse(updatedProject);
    }

    public void deleteProject(UUID id) {
        Project project = getProjectOrThrow(id);
        // Soft delete or check for active sessions
        if (hasActiveSessions(id)) {
            throw new IllegalStateException("Cannot delete project with active sessions");
        }
        repository.delete(project);
    }

    public ProjectResponse getProject(UUID id) {
        return mapToResponse(getProjectOrThrow(id));
    }

    public List<ProjectResponse> getActiveProjects() {
        return repository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isValidProjectPath(String path) {
        // Validate path exists and is a valid directory
        Path projectPath = Paths.get(path);
        return Files.isDirectory(projectPath);
    }

    // Private Helper Methods
    // Private helper methods
    private Project getProjectOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    private void validateProjectPath(String path) {
        if (!isValidProjectPath(path)) {
            throw new RuntimeException("Invalid project path: " + path);
        }

        // // Check if path exists in database
        // Project pathExists = repository.findByPath(path).orElseThrow();
        // // Check if path exists in database
        // log.debug("Path exists in database: {}", pathExists);
    
        // Print pathExists
        

        if (repository.existsByPath(path)) {
            throw new RuntimeException("Project path already exists: " + path);
        }
    }

    private boolean hasActiveSessions(UUID projectId) {
        // Need Implementation
        // return codingSessionRepository.countActiveSessionsByProjectId(projectId) > 0;
        return false;
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .path(project.getPath())
                .description(project.getDescription())
                .active(project.getActive())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
