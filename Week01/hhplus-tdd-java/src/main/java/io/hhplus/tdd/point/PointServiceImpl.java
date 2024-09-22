package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    @Override
    public UserPoint getUserById(long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> getHistoriesById(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    @Override
    public UserPoint chargePointById(long id, CalculateRequest calculateRequest) {
        long afterAmount = userPointTable.selectById(id).point() + calculateRequest.amount();
        pointHistoryTable.insert(id, afterAmount, CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, afterAmount);
    }

    @Override
    public UserPoint usePointById(long id, CalculateRequest calculateRequest) {
        long afterAmount = userPointTable.selectById(id).point() - calculateRequest.amount();
        pointHistoryTable.insert(id, afterAmount, USE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, afterAmount);
    }
}
