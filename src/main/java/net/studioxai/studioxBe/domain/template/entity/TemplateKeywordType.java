package net.studioxai.studioxBe.domain.template.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum TemplateKeywordType {

    GENERAL_DISPLAY("일반 디스플레이"),
    FABRIC_VELVET("패브릭 & 벨벳"),
    OUTDOOR("아웃도어");

    private final String title;

    public static Optional<TemplateKeywordType> findByTitleLike(String keyword) {
        return Arrays.stream(values())
                .filter(type -> type.title.contains(keyword))
                .findFirst();
    }
}
