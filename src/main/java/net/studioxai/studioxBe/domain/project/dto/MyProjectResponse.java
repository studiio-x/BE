package net.studioxai.studioxBe.domain.project.dto;

public record MyProjectResponse(
        Long projectId,
        String name,
        boolean isAdmin
        ) {
}
