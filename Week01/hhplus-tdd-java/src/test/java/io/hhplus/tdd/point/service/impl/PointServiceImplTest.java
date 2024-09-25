package io.hhplus.tdd.point.service.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.enums.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private final long USER_ID = 1L;
    private final long AMOUNT = 1000L;

    @Test
    @DisplayName("유저 포인트 조회")
    public void selectUserById() {
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, AMOUNT, System.currentTimeMillis()));

        UserPoint userPoint = userPointTable.selectById(USER_ID);

        assertAll(
                () -> assertEquals(1000L, userPoint.point()),
                () -> assertEquals(1L, userPoint.id())
        );
    }

    @Test
    @DisplayName("포인트 이용 내역 조회")
    public void selectPointHistoryById() throws Exception {
        when(pointHistoryTable.selectAllByUserId(USER_ID)).thenReturn(
                List.of(
                        new PointHistory(1, USER_ID, AMOUNT, USE, System.currentTimeMillis()),
                        new PointHistory(2, USER_ID, AMOUNT, CHARGE, System.currentTimeMillis()),
                        new PointHistory(3, USER_ID, AMOUNT, USE, System.currentTimeMillis())
                )
        );

        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(USER_ID);

        assertAll(
                () -> assertEquals(3, pointHistories.size()),
                () -> assertEquals(USE, pointHistories.get(0).type()),
                () -> assertEquals(CHARGE, pointHistories.get(1).type())
        );
    }

    @Test
    @DisplayName("포인트 충전")
    public void chargePoint() throws Exception {
        long chargePoint = 500L;
        long afterAmount = AMOUNT + chargePoint;

        pointManipulateStubbing(afterAmount);

        UserPoint userPoint = userPointTable.selectById(USER_ID);
        UserPoint afterUserPoint = userPointTable.insertOrUpdate(USER_ID, afterAmount);

        assertAll(
                () -> assertEquals(1000L, userPoint.point()),
                () -> assertEquals(1500L, afterUserPoint.point()),
                () -> assertEquals(userPoint.id(), afterUserPoint.id())
        );
    }

    @Test
    @DisplayName("포인트 차감")
    public void usePoint() throws Exception {
        long usePoint = 500L;
        long afterAmount = AMOUNT - usePoint;

        pointManipulateStubbing(afterAmount);

        UserPoint userPoint = userPointTable.selectById(USER_ID);
        UserPoint afterUserPoint = userPointTable.insertOrUpdate(USER_ID, afterAmount);

        assertAll(
                () -> assertEquals(1000L, userPoint.point()),
                () -> assertEquals(500L, afterUserPoint.point()),
                () -> assertEquals(userPoint.id(), afterUserPoint.id())
        );
    }

    private void pointManipulateStubbing(long afterAmount) {
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, AMOUNT, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(USER_ID, afterAmount)).thenReturn(new UserPoint(USER_ID, afterAmount, System.currentTimeMillis()));
    }
}