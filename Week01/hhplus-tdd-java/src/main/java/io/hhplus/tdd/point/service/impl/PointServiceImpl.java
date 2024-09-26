package io.hhplus.tdd.point.service.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointManager;
import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.validator.PointValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.enums.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;
    private final PointValidator pointValidator;
    private final PointManager pointManager;

    @Override
    public UserPoint getUserById(final long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> getHistoriesById(final long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    @Override
    public UserPoint chargePointById(final long id, final PointRequest pointRequest) throws ExecutionException, InterruptedException {
        return manipulatePoint(id, CHARGE, pointRequest);
    }

    @Override
    public UserPoint usePointById(final long id, final PointRequest pointRequest) throws ExecutionException, InterruptedException {
        return manipulatePoint(id, USE, pointRequest);
    }

    private UserPoint manipulatePoint(final long id, final TransactionType type, final PointRequest pointRequest) throws ExecutionException, InterruptedException {
        long inputAmount = pointRequest.amount() * (type == CHARGE ? 1 : -1);  // CHARGE(+), USE(-)
        long afterAmount = userPointTable.selectById(id).point() + inputAmount;

        return pointManager.runTask(id, pointRequest, () -> {
            pointValidator.validatePoint(pointRequest.amount(), afterAmount);
            pointHistoryTable.insert(id, afterAmount, type, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(id, afterAmount);
        }).get();
    }
}