# 회고페이지
[자세히보기](https://steadfast-sofa-4b2.notion.site/1-SW-APIDOC43-15beeae70b85806bbaaffc011f63869f)
---
# 리팩토링 진행중
[자세히 보기](https://steadfast-sofa-4b2.notion.site/30-18beeae70b8581109d36c11ed2adf303)

# APIDOC43: 완전 자동화된 API 문서 솔루션
![image](https://github.com/user-attachments/assets/f9d609a0-9d72-4df1-8af4-36e7959b705d)

**APIDOC43**은 개발자의 생산성을 극대화하고 비즈니스 성장에 기여하는 API 문서 자동화 서비스입니다.

---

## **주요 기능**

1. **완전 자동화된 API 문서 생성**
   - 코드 분석을 통해 정확하고 최신의 API 문서를 자동으로 생성합니다.

2. **SaaS 기반 통합 관리 (진행중)**
   - 분산된 API 문서를 손쉽게 통합하고 관리할 수 있습니다.

3. **자연어 검색 (진행예정)**
   - 방대한 API 문서에서 필요한 정보를 쉽게 찾을 수 있습니다.

4. **다양한 분류체계 (진행예정)**
   - 내부용, FE, BE, 관리자 등 다양한 목적의 API를 효율적으로 관리할 수 있습니다.

5. **실시간 업데이트 (진행예정)**
   - 코드 변경 시 자동으로 문서를 최신 상태로 유지합니다.

---

## **기대 효과**
- 개발 생산성 최대 **30% 향상**
- API 문서 작성 및 유지보수 시간 **감소**
- 문서의 **정확성과 일관성 개선**
- 팀 간 **커뮤니케이션 향상**

---
## 파이프라인
![스크린샷 2024-11-28 오후 4 33 59](https://github.com/user-attachments/assets/8a1cd9f9-c01d-4516-a2bc-172914323905)

---
## 아키텍처
![스크린샷 2024-11-28 오후 4 38 00](https://github.com/user-attachments/assets/6304f3fd-2a0f-4fd0-969a-36dfd1857773)

---
## CustomRAG (API 단위 소스코드 추출 모듈) 시퀀스 다이어그램
```mermaid
sequenceDiagram
    participant APISourceDependencyService
    participant SpringJavaApiCodeClient
    participant SrcFileCollector
    participant JavaClassifiedDataGenerator
    participant JavaClassifiedDataContainer
    participant DependencyAnalyzer
    participant DependencyExplorer
    participant GitApiService

    APISourceDependencyService->>SpringJavaApiCodeClient: extractApiSourceDependencyInfo()
    SpringJavaApiCodeClient->>SrcFileCollector: collectFiles()
    SrcFileCollector-->>SpringJavaApiCodeClient: 프로젝트 소스 파일 목록 반환
    SpringJavaApiCodeClient->>JavaClassifiedDataGenerator: init(files)
    JavaClassifiedDataGenerator-->>SpringJavaApiCodeClient: JavaClassifiedDataContainer 초기화
    SpringJavaApiCodeClient->>JavaClassifiedDataContainer: getControllerClasses()
    JavaClassifiedDataContainer-->>SpringJavaApiCodeClient: Controller Class파일 반환
    loop API endpoint 별로 (데모버전 최대 3회)
        SpringJavaApiCodeClient->>DependencyAnalyzer: findDependency(controllerClassName)
        DependencyAnalyzer->>DependencyExplorer: 파라미터 의존성 추적, findClassDependencies()
        DependencyAnalyzer->>DependencyExplorer: 반환 타입 의존성 추적, findClassDependencies()
        DependencyAnalyzer->>DependencyExplorer: 메소드 바디에 있는 의존성 추적,findClassDependencies()
        DependencyExplorer->>DependencyExplorer: 클래스,인터페이스,레코드,enum 의존성 추적
        DependencyExplorer->>DependencyExplorer: 메소드 호출 의존성 추적
        DependencyExplorer-->>DependencyAnalyzer: 의존성 클래스 경로 추가
        DependencyAnalyzer-->>SpringJavaApiCodeClient: API 의존성 정보 반환
        SpringJavaApiCodeClient->>GitApiService: 소스코드 링크 생성, buildSourceCodeUrl(gitRepo, entrySrcPath)
        GitApiService-->>SpringJavaApiCodeClient: 소스 코드 링크 반환 및 추가
    end
    SpringJavaApiCodeClient->>JavaClassifiedDataContainer: 전역적으로 의존되는 소스 요청, getGlobalDependencies(userId)
    JavaClassifiedDataContainer-->>SpringJavaApiCodeClient: 전역의존 소스 경로 반환
    SpringJavaApiCodeClient-->>APISourceDependencyService: 최종 객체 APISourceDependencyInfo 객체 반환

```
---
## 문서생성 (LLM - Spring AI) 시퀀스 다이어그램
```mermaid
sequenceDiagram
actor User

User ->> GenerateOas: generate(userId, apiEntries, projectDir)
GenerateOas ->> SpringAICommandForLLM: createChatClient4o()
SpringAICommandForLLM -->> GenerateOas: ChatClient instance

GenerateOas ->> GenerateOas: findRelatedExceptionSrc(projectRootPath, chatClient4o)

loop for each APIEndpoint //비동기 generateOasPathSchemaSnippet()
GenerateOas ->> SpringAICommandForLLM: requestOasApiSnippet(client, apiEntry, exceptionFormatSrc)

SpringAICommandForLLM ->> SpringAICommandForLLM: createOasPathSection(apiEntry,srcRelationErrorFormat);
SpringAICommandForLLM ->> SpringAICommandForLLM: createOasDescriptionDetail(apiEntry,preResult);
SpringAICommandForLLM ->> SpringAICommandForLLM: validErrorResponseFormat(srcRelationErrorForamt,preResult);
SpringAICommandForLLM -->> GenerateOas: return PathAndComponents object

GenerateOas ->> GenerateOas: generatePathList
GenerateOas ->> GenerateOas: generateSchemasMap

end

GenerateOas ->> GenerateOas: merge paths(삭제예정);
GenerateOas ->> SpringAICommandForLLM: schemaIntegration(client, schemasMap) // merge schema (삭제예정) 실제 소스코드 객체와의 매핑정보를 이용하면 제거할 수 있음. ;
SpringAICommandForLLM -->> GenerateOas: 중복제거 후 통홥된 schema 반환

GenerateOas ->> SpringAICommandForLLM: Oas Info 생성 요청
SpringAICommandForLLM ->> SpringAICommandForLLM: OasInfo 생성
SpringAICommandForLLM -->> GenerateOas: OasInfo 반환

GenerateOas -->> GenerateOas: OAS model(oasInfo,path,schema) 구성
GenerateOas -->> User: OAS 반환
```

---
## 프로젝트 관리
[GitHub Project 이용](https://github.com/orgs/APIDOC43/projects/1)
---
## Developer
홍석준 : [@hoding](https://github.com/seokjun7410)

프로젝트 총괄  (기획, 아키텍처 설계, 구현, 마케팅),
- 코드파서 서버 개발(CustomRAG)
- LLM 서버 개발 및 AI 모델 통합 (Spring Boot)
 - SaaS 서버 개발 
- SSG (Static Site Generator) 서버 개발 (Node.js, EJS)
- 클라우드 인프라 구축 및 관리 (AWS)
- 사용자 인터페이스 (UI/UX) 디자인 
- 프로젝트 문서화 및 기술 문서 작성
- 사용자 피드백 수집 및 분석

