package io.hhplus.tdd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PointException extends RuntimeException{
    HttpStatus code;

    public PointException(String message, HttpStatus code) {
        super(message);
        this.code = code;
    }
}
