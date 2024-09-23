package io.hhplus.tdd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PointExceptionMsg {
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "보유한 포인트가 부족 합니다."),
    POINT_OVERFLOW(HttpStatus.BAD_REQUEST, "보유 가능한 최대 포인트를 넘었습니다."),
    NOT_POSITIVE(HttpStatus.BAD_REQUEST, "양수만 입력 값이 될 수 있습니다.")
    ;

    private final HttpStatus code;
    private final String msg;

    PointExceptionMsg(HttpStatus code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
