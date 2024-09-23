package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.UserPoint;

import java.util.List;

public interface PointService {
    UserPoint getUserById(long id);
    List<PointHistory> getHistoriesById(long id);
    UserPoint chargePointById(long id, PointRequest pointRequest);
    UserPoint usePointById(long id, PointRequest pointRequest);
}