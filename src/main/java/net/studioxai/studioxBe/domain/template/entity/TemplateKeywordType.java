package net.studioxai.studioxBe.domain.template.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum TemplateKeywordType {

    SKINCARE_BOTTLE("스킨케어 용기"),
    SKINCARE_JAR("스킨케어 크림 용기"),
    SHAMPOO_MODEL("샴푸 모델"),
    PERFUME("향수"),
    SKINCARE_MODEL("스킨케어 모델"),
    SHAMPOO_BOTTLE("샴푸 모델"),
    PERFUME_MODEL("향수"),
    LEAF_SKINCARE("잎 스킨케어"),
    WATER_SKINCARE("수분 스킨케어"),
    SKINCARE_TUBE("스킨케어 튜브 용기"),
    HIGHEND_MODEL("명품 모델"),
    MULTIPLE_MODELS("모델 여러명"),
    BLACK_HAIR("흑발"),
    COSMETIC_COMPACT("쿠션"),
    BAG_MODEL("가방"),
    BAG_SHOES_MODEL("가방 & 신발"),
    SHOES_MODEL("신발"),
    OUTDOOR_MODEL("야외"),
    DRINKS("음료"),
    SNEAKERS("신발"),
    DROPPER_BOTTLE("스포이드형 용기"),
    COSMETIC_CONTAINER("화장품 용기"),
    WHISKEY("위스키"),
    FRIDGE("냉장고"),
    FRUITY("과일"),
    NATURE("자연"),
    HAND_AESTHETIC("손"),
    BLONDE("금발"),
    HAIR_PRODUCT_MODEL("헤어 제품 모델"),
    HAIR_MODEL("헤어 모델"),
    MOISTURIZING_AMPULE("수분 앰플"),
    SCULPTURE("조각상"),
    SILK("천 & 실크"),
    LIP_GLOSS_MODEL("립 모델"),
    ACCESSORIES_MODEL("의류 모델"),
    MODEL_IN_CAR("차"),
    MODEL_IN_CITY("도시"),
    SHADOW_MODEL("그림자"),
    SUNGLASS_MODEL("선글라스"),
    JEWELRIES("악세서리"),
    PEOPLE_CAFE("카페"),
    HAND_CREAM("핸드크림"),
    BUBBLY_FOAM("버블");
    ;
    private final String title;

    public static Optional<TemplateKeywordType> findByTitleLike(String keyword) {
        return Arrays.stream(values())
                .filter(type -> type.title.contains(keyword))
                .findFirst();
    }
}
