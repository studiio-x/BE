package net.studioxai.studioxBe.domain.item.dto;

import lombok.Getter;
import net.studioxai.studioxBe.domain.item.entity.Item;

public record ItemGet(String url) {

    public static ItemGet toDto(String url) {
        return new ItemGet(url);
    }
}
