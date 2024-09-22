package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {
    UserPoint getUserById(long id);
    List<PointHistory> getHistoriesById(long id);
    UserPoint chargePointById(long id, CalculateRequest calculateRequest);
    UserPoint usePointById(long id, CalculateRequest calculateRequest);
}