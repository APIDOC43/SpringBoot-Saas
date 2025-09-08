# APIDOC43: API 문서 자동 생성

![image](https://github.com/user-attachments/assets/f9d609a0-9d72-4df1-8af4-36e7959b705d)

**APIDOC43**은 LLM과 코드 분석을 결합한 API 문서 생성 서비스입니다.

```
├── api_spec_generator           // OpenAPI 명세 생성 모듈
├── code_parser                  // Java 소스코드 파싱 모듈
├── pipline_orchestrator         // 비동기 파이프라인 모듈
└── saas_platform                // SaaS 플랫폼 모듈
```

---

## 핵심 구현

### 1. 배치 저장 최적화 (DB I/O 82% 감소)
```java
@Scheduled(fixedDelay = 30000)
public void flushEntities() {
    super.flush(); // 30초마다 배치 처리
}
```
**[OasBatchSaverService.java](src/main/java/com/hocs/server/api_spec_generator/service/OasBatchSaverService.java)**
- ArrayBlockingQueue 기반 메모리 버퍼링
- **쿼리 횟수**: 28회 → 5회 (82% 감소)
- 재시도 메커니즘과 실패 처리 로직

### 2. 비동기 처리 + 스레드풀 분리 (처리 시간 54% 단축)
```java
// 요청 유형별 스레드풀과 세마포어 분리 관리
public void submit(ThrottleRequest request) {
    Semaphore semaphore = resolver.getRelatedSemaphore(request.getTaskType());
    CompletableFuture.runAsync(() -> pipelineService.execute(task), executor);
}
```
**[PipelineThrottleService.java](src/main/java/com/hocs/server/pipline_orchestrator/ratelimit/PipelineThrottleService.java)**
- **처리 시간**: 202초 → 93초 (54% 단축)
- Rate Limiting + Semaphore 기반 동시성 제어
- 신규/기존 사용자 요청 분리 처리로 UX 개선

### 3. 표현식 타입 추론
```java
// 메서드 체이닝 표현식의 타입을 재귀적으로 분석
public Optional<String> resolveExpressionType(Expression expr, String currentClassName, 
                                               JavaClassifiedDataContainer container)
```
**[ExpressionResolver.java](src/main/java/com/hocs/server/code_parser/core/service/ExpressionResolver.java)**
- JavaParser AST를 활용한 스코프 체인 추적
- 메서드 호출, 필드 접근, 객체 생성 표현식 분석

### 4. 의존성 추적
```java
// DFS 알고리즘으로 메서드 호출 그래프 구축
private void findMethodCallDependencies(String className, String methodName, 
                                        Set<String> requiredFiles, Set<String> visitedMethods)
```
**[DependencyExplorer.java](src/main/java/com/hocs/server/code_parser/core/service/DependencyExplorer.java)**
- 순환 참조 방지 (visitedMethods Set 사용)
- 인터페이스-구현체 매핑 추적

### 5. Spring 어노테이션 파싱
```java
// HTTP 매핑 어노테이션에서 메서드와 경로 추출
public static ApiEndpoint generateApiEndpoint(String basePath, MethodDeclaration method)
```
**[EndpointPathUtil.java](src/main/java/com/hocs/server/code_parser/core/util/EndpointPathUtil.java)**
- @GetMapping, @PostMapping 등 Spring 어노테이션 지원
- 클래스/메서드 레벨 경로 결합

---

## 성과

- **처리 시간**: 202초 → 93초 (54% 단축)
- **DB 쿼리**: 단위 요청당 28회 → 5회 (82% 감소)
- **동시성**: ArrayBlockingQueue drainTo 메서드로 thread-safe 보장
- **스레드풀 분리**: 신규/기존 사용자 대기시간 개선

---

## 기술 스택

- Java 17, Spring Boot 3, JPA 3
- MySQL 8, MongoDB
- Docker, AWS
- OpenAI API, JavaParser

---

## 링크
- 랜딩페이지: https://apidoc43.softr.app/
