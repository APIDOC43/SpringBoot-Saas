# APIDOC43: API 문서 자동 생성

![image](https://github.com/user-attachments/assets/f9d609a0-9d72-4df1-8af4-36e7959b705d)

**APIDOC43**은 LLM과 코드 분석을 결합한 API 문서 생성 서비스입니다.

```
├── api_spec_generator           // OpenAPI 명세 생성 
├── code_parser                  // Java 소스코드 파싱
├── pipline_orchestrator         // 비동기 파이프라인 
└── saas_platform                // 문서관리 API (미개발)
```

---

## API 문서 생성과정

### 간략 시퀀스
```mermaid
sequenceDiagram
   participant Client as Client
   participant Pipeline as 명세생성파이프라인
   participant Collector as codePaser
   participant LLM as LLM
   
   Client ->> Pipeline: 요청: Project
    loop API controller 갯수만큼
   Pipeline ->> Collector: 요청: Project code
   
   Collector -->> Pipeline: API Task 반환
       end
   loop API task 갯수만큼
       Pipeline ->> LLM: API 정보 기반 명세 생성
       
       LLM -->> Pipeline: API 명세 반환
   end
   
   Pipeline -->> Client: 전체 API 명세 반환
```

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
```mermaid
sequenceDiagram
   participant Pipeline as 파이프라인
   participant Queue as 메모리큐
   participant BatchSaver as 배치저장
   participant Merger as 엔티티병합
   participant DB as 데이터베이스
   
   loop API 처리
       Pipeline ->> Queue: 데이터 추가 
   end
   
   Note over Queue: 30초 대기 (버퍼링)
   
   BatchSaver ->> Queue: 1000개씩 일괄 수집
   BatchSaver ->> Merger: 동일 Request ID 기준 엔티티 병합
   Merger -->> BatchSaver: 병합된 하나의 엔티티 반환
   BatchSaver ->> DB: Bulk Insert
   Note right of DB: 28회 → 5회 (82% 감소)
```

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
```mermaid
sequenceDiagram
    participant Request as 요청
    participant Classifier as Task분류기
    participant HeavyPool as Heavy스레드풀
    participant FastPool as Fast스레드풀
    participant LLM
    
    Request ->> Classifier: API 개수 확인
    
    alt 신규 사용자 (API 많음)
        Classifier ->> HeavyPool: Heavy 작업 할당
        HeavyPool ->> LLM: 대용량 처리
    else 기존 사용자 (API 적음)
        Classifier ->> FastPool: Fast 작업 할당
        FastPool ->> LLM: 빠른 처리
    end
    
    Note over FastPool: 기존 사용자 대기시간 개선
```

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

---


### 전체 시퀀스
```mermaid
sequenceDiagram
    participant Client as Client
    participant Ingress as PiplineIngressFacade
    participant Classifier as TaskClassifier  
    participant HeavyThrottle as Heavy스로틀링
    participant FastThrottle as Fast스로틀링
    participant CodeParser as codePaser
    participant DependencyBatch as 의존성배치저장
    participant LLM as LLM
    participant OASBatch as OAS배치저장
    
    Client ->> Ingress: 요청: Project
    
    loop API controller 갯수만큼
        Ingress ->> CodeParser: 요청: Project code
        CodeParser ->> DependencyBatch: API 의존성 그래프 큐 추가
        CodeParser -->> Ingress: API Task 반환
    end
    
    Ingress ->> Classifier: Task 개수 기반 분류
    Classifier -->> Ingress: TaskType (HEAVY/FAST)
    
    Note over DependencyBatch: 30초마다 의존성 배치 저장
    DependencyBatch ->> DependencyBatch: 큐에서 배치 처리
    
    loop API task 갯수만큼
        alt TaskType = HEAVY
            Ingress ->> HeavyThrottle: API 명세 생성 요청
            HeavyThrottle ->> LLM: API 정보 기반 명세 생성 (세마포어 제어)
            LLM -->> HeavyThrottle: API 명세 반환
            HeavyThrottle -->> Ingress: API 명세 반환
        else TaskType = FAST  
            Ingress ->> FastThrottle: API 명세 생성 요청
            FastThrottle ->> LLM: API 정보 기반 명세 생성 (세마포어 제어)
            LLM -->> FastThrottle: API 명세 반환
            FastThrottle -->> Ingress: API 명세 반환
        end
        Ingress ->> OASBatch: 명세 데이터 큐 추가
    end
    
    Note over OASBatch: 30초마다 OAS 배치 저장
    OASBatch ->> OASBatch: 큐에서 배치 처리
    
    Ingress -->> Client: 전체 API 명세 반환
```
