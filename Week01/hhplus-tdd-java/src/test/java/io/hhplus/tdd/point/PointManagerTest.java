package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointManagerTest {

    @Autowired
    private PointManager pointManager;

    @Test
    @DisplayName("동시성 테스트 - User ID 기준으로 제어 되는지 1 ~ 5 번 유저 에게 Job 배분")
    public void concurrentTestForEachUser()  {
        // given: 2 초 걸리는 작업, 총 작업 10개를, 5개의 스레드
        final long JOB_TIME = 2;
        final int JOB_COUNT = 10;
        final long[] userIds = new long[] {1, 2, 3, 4, 5};
        final CountDownLatch latch = new CountDownLatch(JOB_COUNT);

        // when: 스레드 병렬 실행으로 작업 시작
        // then: 작업물 / 스레드 수 * 작업 시간 + 1 의 수행 시간 내에 완료
        assertTimeout(Duration.ofSeconds(JOB_COUNT / userIds.length * JOB_TIME + 1), () -> {
            for(int i = 0; i < JOB_COUNT; i++) {
                long userId = userIds[i % userIds.length];
                PointRequest pointRequest = new PointRequest(1000);
                pointManager.runTask(userId, pointRequest, () -> {
                    try {
                        TimeUnit.SECONDS.sleep(JOB_TIME);
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return null;
                });
            }

            latch.await();
        });
    }

    @Test
    @DisplayName("포인트 사용이 원할화게 진행 되는지 (10 원 충전 x 100 번)")
    public void isChargeFunctionOk() throws InterruptedException {
        // given: 작업량 100, 충전 금액 10
        final long userId = 1L;
        final int JOB_COUNT = 100;
        final long CHARGE_POINT = 10L;
        final CountDownLatch latch = new CountDownLatch(JOB_COUNT);
        List<Future<UserPoint>> pointHistories = new ArrayList<>();

        // when: 10원 100번 충전
        for(int i = 0; i < JOB_COUNT; i++) {
            pointHistories.add(
                pointManager.runTask(userId, new PointRequest(0), () -> {
                    latch.countDown();

                    return new UserPoint(userId, CHARGE_POINT, System.currentTimeMillis());
                 })
            );
        }

        latch.await();
        // then: 스레드 blocking 후 10원 * 100번 금액인 1000 원이 최종 값 확인
        int sum = pointHistories.stream()
                                .map(future -> {
                                    try {
                                        return future.get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .map(UserPoint::point)
                                .mapToInt(Long::intValue)
                                .sum();

        assertEquals(CHARGE_POINT * JOB_COUNT, sum);
    }

    @Test
    @DisplayName("더 이상 차감될 포인트가 남아있지 않는 경우")
    public void isUseFunctionOk() throws InterruptedException {
        // given: 작업량 10, 사용 금액 200
        final long userId = 1L;
        final int JOB_COUNT = 10;
        final int USE_POINT = -200;
        final CountDownLatch latch = new CountDownLatch(JOB_COUNT);
        List<Future<UserPoint>> pointHistories = new ArrayList<>();

        AtomicInteger balance = new AtomicInteger(1000);
        AtomicInteger succeed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        // when: 1000원에서 200원씩 차감해서 0원인 경우 차감이 되는지 확인
        for(int i = 0; i < JOB_COUNT; i++) {
            pointHistories.add(
                pointManager.runTask(userId, new PointRequest(0), () -> {
                    latch.countDown();

                    if(balance.addAndGet(USE_POINT) >= 0) {
                        succeed.incrementAndGet();
                    } else {
                        failed.incrementAndGet();
                    }

                    return null;
                })
            );
        }

        latch.await();

        // then: 5번 성공, 5번 실패 (1000 -> 800 -> 600 -> 400 -> 200 -> 0)
        assertAll(
                () -> assertEquals(succeed.get(), 5),
                () -> assertEquals(succeed.get(), 5)
        );
    }

    @Test
    @DisplayName("요청 시간에 따라서 빠른 요청이 우선 처리 되는지")
    public void isProcessAccordingToPriority() throws InterruptedException {
        // given: 작업량 20
        final long userId = 1L;
        final int JOB_COUNT = 20;
        final CountDownLatch latch = new CountDownLatch(JOB_COUNT);
        List<Future<UserPoint>> pointHistories = new ArrayList<>();

        // when: 텀을 두고 비동기 요청
        for(int i = 0; i < JOB_COUNT; i++) {
            TimeUnit.MICROSECONDS.sleep(50);

            pointHistories.add(
                pointManager.runTask(userId, new PointRequest(0), () -> {
                    latch.countDown();

                    return new UserPoint(userId, 1, System.currentTimeMillis());
                })
            );
        }

        latch.await();

        // then: 요청이 순서대로 처리 되었는지 확인
        List<Long> srtTimeList = pointHistories.stream()
                                           .map(future -> {
                                               try {
                                                   return future.get();
                                               } catch (InterruptedException | ExecutionException e) {
                                                   throw new RuntimeException(e);
                                               }
                                           })
                                           .map(UserPoint::updateMillis)
                                           .collect(Collectors.toList());

        assertThat(srtTimeList).isSorted();
    }
}