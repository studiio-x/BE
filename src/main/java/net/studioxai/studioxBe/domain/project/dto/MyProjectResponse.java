package net.studioxai.studioxBe.domain.project.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public record MyProjectResponse(
        Long projectId,
        String name,
        @JsonValue boolean isAdmin
        ) {
}
