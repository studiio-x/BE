package net.studioxai.studioxBe.domain.chat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {

    INVALID_CONCEPT_INDEX(HttpStatus.BAD_REQUEST, "CHAT_400_1", "유효하지 않은 컨셉 인덱스입니다. 0~3 사이의 값을 입력해주세요."),
    IMAGE_NOT_IN_PROJECT(HttpStatus.BAD_REQUEST, "CHAT_400_2", "해당 이미지는 이 프로젝트에 속하지 않습니다."),

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404_1", "채팅방을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404_2", "해당 이미지를 찾을 수 없습니다."),

    CONCEPT_SELECTION_PENDING(HttpStatus.CONFLICT, "CHAT_409_1", "컨셉 선택이 진행 중입니다. 컨셉을 먼저 선택해주세요."),
    NO_PENDING_CONCEPT(HttpStatus.CONFLICT, "CHAT_409_2", "선택 가능한 컨셉이 없습니다. 먼저 채팅 메시지를 보내주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
