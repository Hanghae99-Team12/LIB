package io.hhplus.tdd.point.service.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.enums.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

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
    public UserPoint chargePointById(long id, PointRequest pointRequest) {
        long afterAmount = userPointTable.selectById(id).point() + pointRequest.amount();
        pointHistoryTable.insert(id, afterAmount, CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, afterAmount);
    }

    @Override
    public UserPoint usePointById(long id, PointRequest pointRequest) {
        long afterAmount = userPointTable.selectById(id).point() - pointRequest.amount();
        pointHistoryTable.insert(id, afterAmount, USE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, afterAmount);
    }
}
