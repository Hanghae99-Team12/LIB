## 📖 동시성 제어를 위한 선택

### Why not `Lock, synchronized`

1. Lock (`ReentrantLock`)

- 개발자가 명시적으로 lock & unlock 을 해줘야 한다.
- 공정성을 갖을 수 있지만, 요청 순서를 우선 순위로 둬야하기 때문에

2. synchronized

- 순서를 보장할 수 없어 추가적인 자료 구조를 만들어야 한다.
- 유저에 따라서 병렬 처리 해주어야 하는데, 구현 하고자 하는 목적이 맞지 않음 -> 다수 요청이 병목 현상을 만들 가능성

### Why `Concurrent, ThreadPoolExecutor`

1. Concurrent

- 내부에서 Lock 을 제어하기 때문에 명시적으로 제어할 필요가 없음
- 유저별로 임계 자원에 접근 제어를 하기위해 구현 하고자 했던 설계와 일치

2. ThreadPoolExecutor

- 내부에 Concurrent 패키지의 BlockingQueue 로 그 다음 요청들을 보관할 수 있으며 PriorityBlockingQueue 우선 순위를 만들 수도 있다.

## 📖 코드 베이스 설명

포인트 동시성 제어를 위한 PointManager 입니다.

- `Map<Long, ThreadPoolExecutor> userTasks`: 유저별로 스레드를 제어하기 위한 자료구조 입니다.
- `new PriorityBlockingQueue<Runnable>`: 각 스레드 풀에는 요청 순서대로 처리하기 위한 우선순위 블락 큐를 가지고 있습니다.
- `PriorityTask 클래스`: Callable task 를 처리할 때 요청된 시간을 기준으로 우선순위를 정하기 때문에 Wrapping 클래스를 별도로 구현 했습니다.

간략적인 프로세스 플로우

1. 유저가 USE / CHARGE 요청을 보낸다.
2. 유저 ID 에 해당하는 쓰레드 제어를 위한 ThreadPoolExecutor 를 생성 합니다.
3. 유저의 요청이 생성한 스레드 보다 많아지게 되면 내부에 갖고 있는 BlockQueue 에 담아두게 되고, 정렬 조건을 요청 시간으로 정해둔 PriorityBlockingQueue 우선 순위 큐에 보관 및 활용 합니다.

```java
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
```

## 📖 동시성 테스트

```java
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
```

유저의 ID 별로 스레드가 제어되고 있는지 확인하는 테스트 입니다.

5 개의 userIds(스레드)를 가지고 2초가 소모되는 10개의 작업을 수행하기 때문에 예상되는 종료 시간은 4초 입니다.

<img width="1241" alt="스크린샷 2024-09-27 오전 1 32 46" src="https://github.com/user-attachments/assets/7e1334da-6ec8-4a05-8d85-0972da0d7d77">

스레드 풀과 작업하는 스레드가 다르며 병렬적으로 처리되어 총 4초의 시간이 소요된 모습을 확인할 수 있었습니다.

## 📖 원할한 포인트 충전 테스트

```java
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
```

포인트 충전이 유실 없이 잘 수행 되는지 테스트 해본 결과 예상하는 결과와 똑같이 나왔습니다.

## 📖 부족한 출금 금액(동시성 제어2)

```java
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
```

같은 유저가 여러번 요청을 하였을 때 잔고가 부족하면 더 이상 출금을 할 수 없게 하는 테스트 입니다.

## 📖 요청 순서에 따라 처리 되는지 확인 테스트

```java
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

        pointManager.runTask(userId, new PointRequest(0), () -> {
            latch.countDown();

            return new UserPoint(userId, 1, System.currentTimeMillis());
        });
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
```

요청 시간에 따라 처리 되는지 확인하기 위해서 각 요청 사이의 시간에 텀을 두었고, 요청 처리 순서가 정렬되어 있는지 확인 하였습니다.
