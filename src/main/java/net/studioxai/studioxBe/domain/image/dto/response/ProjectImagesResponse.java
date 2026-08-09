package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.dto.ImagesDto;
import net.studioxai.studioxBe.global.dto.PageInfo;

import java.util.List;

public record ProjectImagesResponse(
        List<ImagesDto> images,
        PageInfo pageInfo
) {
    public static ProjectImagesResponse create(
            List<ImagesDto> images,
            PageInfo pageInfo
    ) {
        return new ProjectImagesResponse(images, pageInfo);
    }
}
