package io.hhplus.tdd.point.validator;

import io.hhplus.tdd.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PointValidatorTest {

    @Autowired
    PointValidator pointValidator;

    @DisplayName("입력되는 금액은 양수만 가능")
    @ParameterizedTest(name = "{index} 번 테스트 파라미터[{0}]")
    @ValueSource(longs = {0L, -1L, -100_000L, Long.MIN_VALUE})
    public void inputMustBePositive(long inputAmount) {
        assertThatThrownBy(() -> pointValidator.validatePoint(inputAmount, 0L)).isInstanceOf(PointException.class);
    }

    @DisplayName("보유한 포인트가 부족한 케이스")
    @ParameterizedTest(name = "{index} 번 테스트 파라미터[{0}]")
    @ValueSource(longs = {-1L, -100_000L, Long.MIN_VALUE})
    public void hasNotEnoughPoint(long afterAmount) {
        assertThatThrownBy(() -> pointValidator.validatePoint(1L, afterAmount)).isInstanceOf(PointException.class);
    }

    @DisplayName("보유한 포인트가 상한치를 넘어가는 경우")
    @ParameterizedTest(name = "{index} 번 테스트 파라미터[{0}]")
    @ValueSource(longs = {100_001L, 200_000L, 100_000_000L, Long.MAX_VALUE})
    public void pointOverflow(long afterAmount) {
        assertThatThrownBy(() -> pointValidator.validatePoint(1L, afterAmount)).isInstanceOf(PointException.class);
    }
}