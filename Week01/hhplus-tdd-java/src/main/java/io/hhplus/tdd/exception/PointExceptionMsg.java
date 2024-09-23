package io.hhplus.tdd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PointExceptionMsg {
    TEST(HttpStatus.BAD_REQUEST, "1");

    private final HttpStatus code;
    private final String msg;

    PointExceptionMsg(HttpStatus code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
