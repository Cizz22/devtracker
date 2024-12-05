package com.devtracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;
    
    @NotBlank(message = "Project path is required")
    private String path;
    
    private String description;
    private Boolean active;
}
