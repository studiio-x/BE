package net.studioxai.studioxBe.domain.item.dto;

import lombok.Getter;
import net.studioxai.studioxBe.domain.item.entity.Item;
import net.studioxai.studioxBe.global.entity.PageInfo;

import java.util.List;

public record ItemGet(
        List<String> urls,
        PageInfo pageInfo
) {

    public static ItemGet toDto(List<String> urls, PageInfo pageInfo) {
        return new ItemGet(urls, pageInfo);
    }
}
