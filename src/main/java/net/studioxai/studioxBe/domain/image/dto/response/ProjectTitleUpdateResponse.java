package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.Project;

public record ProjectTitleUpdateResponse(
        Long projectId,
        String title
) {
    public static ProjectTitleUpdateResponse of(Project project) {
        return new ProjectTitleUpdateResponse(
                project.getId(),
                project.getTitle()
        );
    }
}

