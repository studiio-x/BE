package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.dto.ProjectsDto;
import net.studioxai.studioxBe.global.dto.PageInfo;

import java.util.List;

public record ProjectsResponse(
        List<ProjectsDto> projects,
        PageInfo pageInfo
) {
    public static ProjectsResponse create(
            List<ProjectsDto> projects,
            PageInfo pageInfo
    ) {
        return new ProjectsResponse(projects, pageInfo);
    }
}


