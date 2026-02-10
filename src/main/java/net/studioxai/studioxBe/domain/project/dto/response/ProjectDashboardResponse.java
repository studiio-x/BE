package net.studioxai.studioxBe.domain.project.dto.response;

import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ProjectDashboardResponse(
        Long projectId,
        String title,
        @ImageUrl String representativeImageObjectKey
        )
{
    public static ProjectDashboardResponse from(Project project) {
        return new ProjectDashboardResponse(
                project.getId(),
                project.getTitle(),
                project.getRepresentativeImageObjectKey()
        );
    }
}

