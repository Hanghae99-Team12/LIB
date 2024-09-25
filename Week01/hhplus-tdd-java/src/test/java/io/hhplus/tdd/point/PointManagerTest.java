package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointManagerTest {

    @Autowired
    PointManager pointManager;

    private final Logger log = LoggerFactory.getLogger(PointManagerTest.class);

    @Test
    @DisplayName("동시성 테스트 - User ID 기준으로 제어 되는지 1 ~ 5 번 유저 Job 3개씩")
    public void concurrentTestForEachUser() throws InterruptedException {
        long srtTime = System.currentTimeMillis();
        long[] userIds = new long[] {1, 2, 3, 4, 5};
        long JOB_TIME = 2;
        int JOB_LOOP_COUNT = 5;
        CountDownLatch latch = new CountDownLatch(userIds.length * JOB_LOOP_COUNT);

        for(int i = 0; i < JOB_LOOP_COUNT; i++) {
            for(long userId : userIds) {
                PointRequest pointRequest = new PointRequest(userId);

                pointManager.runTask(userId, pointRequest, () -> {
                    try {
                        TimeUnit.SECONDS.sleep(JOB_TIME);
                        /*log.info("[Request Time: {}] User [{}] Job Done By [thread {}] For [{} sec]",
                                 LocalDateTime.ofInstant(Instant.ofEpochMilli(pointRequest.reqTime()), ZoneId.systemDefault()),
                                 userId, Thread.currentThread().getId(), getTimeGap(srtTime));*/
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        latch.await();

        assertEquals((JOB_TIME * JOB_LOOP_COUNT), getTimeGap(srtTime));
    }

    /*
        [ TODO List ]
        1. 시간별로 완료 되는지
        2. 요금 충전 하다가 마이너스 했을 때
     */

    @Test
    public void test8() throws InterruptedException {
        final Map<Long, String> map = new ConcurrentHashMap<>();
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        map.put(1L, "abc");
        scheduler.schedule(() -> {
            map.remove(1L);
        }, 5, TimeUnit.SECONDS);

        System.out.println(map.get(1L));
        TimeUnit.SECONDS.sleep(7);
        System.out.println(map.get(1L));
    }

    private long getTimeGap(long srtTime) {
        return (System.currentTimeMillis() - srtTime) / 1000;
    }
}