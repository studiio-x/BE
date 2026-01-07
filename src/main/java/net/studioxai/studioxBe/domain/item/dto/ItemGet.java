package net.studioxai.studioxBe.domain.item.dto;

import net.studioxai.studioxBe.global.dto.PageInfo;

import java.util.List;

public record ItemGet(
        List<String> urls,
        PageInfo pageInfo
) {

    public static ItemGet toDto(List<String> urls, PageInfo pageInfo) {
        return new ItemGet(urls, pageInfo);
    }
}
