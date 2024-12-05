package com.devtracker.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private String path;
    private String description;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}