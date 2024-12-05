package com.devtracker.resolver;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.devtracker.dto.ProjectRequest;
import com.devtracker.dto.ProjectResponse;
import com.devtracker.service.ProjectService;

@Controller
@RequiredArgsConstructor
public class ProjectResolver {
    private final ProjectService projectService;

    @QueryMapping
    public List<ProjectResponse> projects() {
        return projectService.findAll();
    }

    @QueryMapping
    public ProjectResponse project(@Argument UUID id) {
        return projectService.getProject(id);
    }

    @QueryMapping
    public List<ProjectResponse> activeProjects() {
        return projectService.getActiveProjects();
    }

    @MutationMapping
    public ProjectResponse createProject(@Argument ProjectRequest request) {
        return projectService.createProject(request);
    }

    @MutationMapping
    public ProjectResponse updateProject(@Argument UUID id, ProjectRequest request) {
        return projectService.updateProject(id, request);
    }

    @MutationMapping
    public void deleteProject(@Argument UUID id) {
        projectService.deleteProject(id);
    }

}
