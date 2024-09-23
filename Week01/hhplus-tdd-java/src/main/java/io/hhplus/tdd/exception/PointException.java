package io.hhplus.tdd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PointException extends RuntimeException{
    HttpStatus code;

    public PointException(PointExceptionMsg pointExceptionMsg) {
        super(pointExceptionMsg.getMsg());
        this.code = pointExceptionMsg.getCode();
    }
}