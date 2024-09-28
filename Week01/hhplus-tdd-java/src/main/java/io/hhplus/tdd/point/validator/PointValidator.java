package io.hhplus.tdd.point.validator;

import io.hhplus.tdd.exception.PointException;
import org.springframework.stereotype.Component;

import static io.hhplus.tdd.exception.PointExceptionMsg.*;

@Component
public class PointValidator {
    private final long MAX_POINT = 100_000L;
    private final long MIN_POINT = 0L;

    public void validatePoint(long inputAmount, long afterAmount) throws PointException {
        if(inputAmount <= MIN_POINT) {
            throw new PointException(NOT_POSITIVE);
        }

        if(afterAmount < MIN_POINT) {
            throw new PointException(NOT_ENOUGH_POINT);
        }

        if(MAX_POINT < afterAmount) {
            throw new PointException(POINT_OVERFLOW);
        }
    }
}