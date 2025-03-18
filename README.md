# 프로젝트 진행중
2025.03월 : 리팩토링 진행중

---

## APIDOC43: 완전 자동화된 API 문서 솔루션
![image](https://github.com/user-attachments/assets/f9d609a0-9d72-4df1-8af4-36e7959b705d)

**APIDOC43**은 개발자의 생산성을 극대화하고 비즈니스 성장에 기여하는 API 문서 자동화 서비스입니다.
```
├── HocsserverApplication.java   // 애플리케이션의 메인 진입점
├── api_spec_generator           // OAS (OpenAPI-spec) 생성 모듈 with LLM
├── code_parser                  // 소스 코드 파싱 및 분석 모듈 With Java-parser libarary
├── common                       // 공통 유틸리티, 도메인 모델, 공통 기능 모듈 
├── pipline_orchestrator         // 파이프라인 오케스트레이션 및 작업 조율 모듈: async
└── saas_platform                // 클라이언트 대상 SaaS 플랫폼 서비스 모듈
```
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
## Developer
홍석준 : [@hoding](https://github.com/seokjun7410)

프로젝트 총괄  (기획, 아키텍처 설계, 구현, 마케팅),
- 코드파서 서버 개발(CustomRAG)
- LLM 서버 개발 및 AI 모델 통합 (Spring Boot)
- SaaS 서버 개발 
- 클라우드 인프라 구축 및 관리 (AWS)
- 사용자 인터페이스 (UI/UX) 디자인 
- 사용자 피드백 수집 및 분석

