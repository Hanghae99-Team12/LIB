# ✨ 항해 백엔드 플러스 6기 TDD 개발 과제

# 📂 디렉토리 구조

       ├ 📁 hhplus
       ⎮    ├ 📁 tdd
       ⎮    ⎮    ├ 📁 database
       ⎮    ⎮    ⎮   ├︎ 📃 PointHistory.java
       ⎮    ⎮    ⎮   ├︎ 📃 UserPointTable.java
       ⎮    ⎮    ├ 📁 exception
       ⎮    ⎮    ⎮   ├︎ 📃 ApiControllerAdvice.java
       ⎮    ⎮    ⎮   ├︎ 📃 ErrorResponse.java
       ⎮    ⎮    ⎮   ├︎ 📃 PointException.java
       ⎮    ⎮    ⎮   ├︎ 📃 PointExceptionMsg.java
       ⎮    ⎮    ├ 📁 point
       ⎮    ⎮    ⎮   ├︎ 📁 controller
       ⎮    ⎮    ⎮   ⎮   ├︎ 📃 PointController.java
       ⎮    ⎮    ⎮   ├︎ 📁 dto
       ⎮    ⎮    ⎮   ⎮   ├︎ 📃 PointHistory.java
       ⎮    ⎮    ⎮   ⎮   ├︎ 📃 PointRequest.java
       ⎮    ⎮    ⎮   ⎮   ├︎ 📃 UserPoint.java
       ⎮    ⎮    ⎮   ├︎ 📁 enums
       ⎮    ⎮    ⎮   ⎮   ├︎ 📃 TransactionType.java
       ⎮    ⎮    ⎮   ├︎ 📁 service
       ⎮    ⎮    ⎮   ⎮   ├︎ 📁 impl
       ⎮    ⎮    ⎮   ⎮   ⎮   ├︎ 📃 PointServiceImpl.java
       ⎮    ⎮    ⎮   ⎮   ├︎ 📃 PointService.java
       ⎮    ⎮    ⎮   ├︎ 📁 validator
       ⎮    ⎮    ⎮   ⎮   ├︎ 📃 PointValidator.java
       ⎮    ⎮    ⎮   ├︎ 📃 PointManage.java (어디 둬야할지...?)
       ⎮    ⎮    ├ 📃 TddApplication.java

# 📆 Week01 - TDD 로 개발하기

## 📌 Task

- Default
  - [X] `/point` 패키지 (디렉토리) 내에 `PointService` 기본 기능 작성
  - [X] `/database` 패키지의 구현체는 수정하지 않고, 이를 활용해 기능을 구현
  - [X] PATCH  `/point/{id}/charge` : 포인트를 충전한다.
  - [X] PATCH `/point/{id}/use` : 포인트를 사용한다.
  - [X] GET `/point/{id}` : 포인트를 조회한다.
  - [X] GET `/point/{id}/histories` : 포인트 내역을 조회한다.
  - [X] 잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.
  - [X] 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.

- Step1
  - [X] 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
  - [X] 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링
  - [X] 동시성 제어에 대한 통합 테스트 작성

- Step2
  - [X] 동시성 제어 방식에 대한 분석 및 보고서 작성 ( README.md )
