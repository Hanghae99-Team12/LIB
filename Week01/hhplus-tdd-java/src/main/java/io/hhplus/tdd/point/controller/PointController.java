package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.service.impl.PointServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private final PointServiceImpl pointServiceImpl;

    /*
        [ TODO List ]
        1. 존재하지 않는 유저인 경우
        2. 금액이 음수로 넘어가는 경우 (사용 금액이 남은 잔액보다 모자란 경우)
        3. 오버플로우도 고려 해야하나?? (최대 잔고)
        4. 동시성 제어에 대한 통합 테스트
        5. 동시성 제어 보고서 작성
     */

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(@PathVariable long id) {
        return pointServiceImpl.getUserById(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(@PathVariable long id) {
        return pointServiceImpl.getHistoriesById(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody PointRequest pointRequest
    ) {
        return pointServiceImpl.chargePointById(id, pointRequest);
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody PointRequest pointRequest
    ) {
        return pointServiceImpl.usePointById(id, pointRequest);
    }
}