package net.studioxai.studioxBe.domain.folder.dto.response;

import net.studioxai.studioxBe.domain.folder.dto.FoldersDto;
import net.studioxai.studioxBe.global.dto.PageInfo;

import java.util.List;

public record FoldersResponse(
        List<FoldersDto> folders,
        PageInfo pageInfo
) {
    public static FoldersResponse create(List<FoldersDto> folders, PageInfo pageInfo) {
        return new FoldersResponse(folders, pageInfo);
    }
}
