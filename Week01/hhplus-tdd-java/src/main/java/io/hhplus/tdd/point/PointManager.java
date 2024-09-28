package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.UserPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class PointManager {
    private final Map<Long, ThreadPoolExecutor> userTasks = new ConcurrentHashMap<>();

    public Future<UserPoint> runTask(final long id, final PointRequest pointRequest, final Callable<UserPoint> task) {
        return userTasks.computeIfAbsent(id, key -> createPriorityExecutor())
                        .submit(new PriorityTask(task, pointRequest));
    }

    private ThreadPoolExecutor createPriorityExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                      new PriorityBlockingQueue<Runnable>(11, Comparator.comparingLong(e -> {
                                          if(e instanceof PriorityTask) {
                                              return ((PriorityTask) e).getPriority();
                                          }
                                          return Long.MAX_VALUE;
                                      })));
    }

    private class PriorityTask implements Callable<UserPoint> {
        private final Callable<UserPoint> task;
        private final PointRequest pointRequest;

        private PriorityTask(final Callable<UserPoint> task, final PointRequest pointRequest) {
            this.task = task;
            this.pointRequest = pointRequest;
        }

        public long getPriority() {
            return pointRequest.reqTime();
        }

        @Override
        public UserPoint call() throws Exception {
            return task.call();
        }
    }
}