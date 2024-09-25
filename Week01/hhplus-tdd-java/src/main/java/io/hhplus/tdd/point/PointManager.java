package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
class PointManager {
    private final Map<Long, ThreadPoolExecutor> userTasks = new ConcurrentHashMap<>();

    public Future<?> runTask(final long id, final PointRequest pointRequest, final Runnable task) {
        // log.info("Current User Id [{}] & Queue Size [{}]", id, threadPoolExecutor.getQueue().size());

        return userTasks.computeIfAbsent(id, key -> createPriorityExecutor())
                        .submit(new PriorityTask(task, pointRequest));
    }

    /**
     * Runnable 이 Comparable 을 구현하고 있지 않아서, 타입에 따라서 반환 하도록 Comparator 구현
     * @return ExecutorService
     */
    private ThreadPoolExecutor createPriorityExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                      new PriorityBlockingQueue<Runnable>(11, Comparator.comparingLong(e -> {
                                          if(e instanceof PriorityTask) {
                                              return ((PriorityTask) e).getPriority();
                                          }
                                          return Long.MAX_VALUE;
                                      })));
    }

    private class PriorityTask implements Runnable {
        private final Runnable task;
        private final PointRequest pointRequest;

        private PriorityTask(final Runnable task, final PointRequest pointRequest) {
            this.task = task;
            this.pointRequest = pointRequest;
        }

        public long getPriority() {
            return pointRequest.reqTime();
        }

        @Override
        public void run() {
            task.run();
        }
    }
}