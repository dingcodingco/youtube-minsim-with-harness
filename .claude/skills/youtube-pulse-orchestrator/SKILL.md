---
name: youtube-pulse-orchestrator
description: "YooBe Pulse(유튜브 민심 분석기) 프로젝트의 에이전트 팀을 조율하는 오케스트레이터. 유튜브 민심, 댓글 분석, YooBe Pulse, 대시보드, 감정 분석, 키워드 분석 관련 구현 요청 시 이 스킬을 사용하라. 후속 작업: 결과 수정, 부분 재실행, 업데이트, 보완, 다시 실행, 특정 모듈만 다시 구현, 이전 결과 개선, 버그 수정 요청 시에도 반드시 이 스킬을 사용."
---

# YooBe Pulse Orchestrator

유튜브 민심 분석기(YooBe Pulse)의 에이전트 팀을 조율하여 전체 서비스를 구현하는 통합 스킬.

## 실행 모드: 에이전트 팀

## 에이전트 구성

| 팀원 | 에이전트 정의 | 역할 | 출력 |
|------|-------------|------|------|
| architect | `.claude/agents/architect.md` | 프로젝트 구조, Gradle/npm, domain 엔티티, config | `backend/`, `frontend/`, `e2e/` scaffolding |
| backend-dev | `.claude/agents/backend-dev.md` | collector + analyzer + api 모듈 | Spring Boot 모듈 전체 |
| frontend-dev | `.claude/agents/frontend-dev.md` | React SPA 대시보드 | 대시보드 컴포넌트 + 훅 |
| qa-engineer | `.claude/agents/qa-engineer.md` | E2E + 단위 테스트 + 경계면 검증 | 테스트 + QA 리포트 |

## PRD 참조

모든 에이전트는 프로젝트 루트의 `youtube-pulse-prd.md`를 기준 문서로 사용한다. PRD의 섹션별 담당:
- **Section 2-3**: architect (기술 스택, 아키텍처, 모듈 구조)
- **Section 4**: architect (데이터 모델, ERD)
- **Section 5**: backend-dev (API 스펙)
- **Section 6**: frontend-dev (프론트엔드 요구사항)
- **Section 7**: backend-dev (Collector 모듈)
- **Section 8**: backend-dev (Analyzer 모듈)
- **Section 9**: backend-dev + architect (Demo 모드)
- **Section 10**: 전체 (비기능 요구사항)
- **Section 11**: qa-engineer (수용 기준)

## 워크플로우

### Phase 0: 컨텍스트 확인

기존 산출물 존재 여부를 확인하여 실행 모드를 결정한다:

1. `_workspace/` 디렉토리 존재 여부 확인
2. `backend/`, `frontend/` 디렉토리 존재 여부 확인
3. 실행 모드 결정:
   - **`_workspace/` 미존재 + 소스 미존재** → **초기 실행**. Phase 1로 진행
   - **소스 존재 + 사용자가 부분 수정 요청** → **부분 재실행**. 해당 에이전트만 재호출
   - **소스 존재 + 사용자가 새 입력 제공** → **새 실행**. 기존 `_workspace/`를 `_workspace_{timestamp}/`로 이동
4. 부분 재실행 시: 이전 산출물 경로를 에이전트 프롬프트에 포함

### Phase 1: 준비

1. `youtube-pulse-prd.md` 읽기 — 전체 요구사항 확인
2. `_workspace/` 디렉토리 생성
3. 사용자 입력 분석 — 특별 요청 사항 확인 (특정 모듈만 구현, 스택 변경 등)

### Phase 2: 팀 구성 및 Scaffolding

1. 에이전트 팀 생성:
   ```
   TeamCreate(
     team_name: "youtube-pulse-team",
     members: [
       {
         name: "architect",
         agent_type: "general-purpose",
         model: "opus",
         prompt: ".claude/agents/architect.md의 역할에 따라 YooBe Pulse 프로젝트를 scaffolding하라.
                  PRD: youtube-pulse-prd.md를 읽고 Section 2-4, 9를 기준으로 작업하라.
                  완료 후 _workspace/01_architect_structure.md에 생성 파일 목록과 빌드 결과를 기록하라.
                  빌드(./gradlew build, npm install)가 성공해야 한다.
                  완료되면 backend-dev와 frontend-dev에게 시작 가능함을 알려라."
       },
       {
         name: "backend-dev",
         agent_type: "general-purpose",
         model: "opus",
         prompt: ".claude/agents/backend-dev.md의 역할에 따라 백엔드를 구현하라.
                  PRD: youtube-pulse-prd.md의 Section 5, 7, 8, 9를 기준으로 작업하라.
                  architect의 scaffolding 완료 알림을 기다린 후 시작하라.
                  구현 완료 후 _workspace/02_backend_implementation.md에 구현 목록을 기록하라.
                  API 구현 완료 시 frontend-dev에게 알리고, 전체 완료 시 qa-engineer에게 알려라."
       },
       {
         name: "frontend-dev",
         agent_type: "general-purpose",
         model: "opus",
         prompt: ".claude/agents/frontend-dev.md의 역할에 따라 프론트엔드를 구현하라.
                  PRD: youtube-pulse-prd.md의 Section 6을 기준으로 작업하라.
                  architect의 scaffolding 완료 알림을 기다린 후 시작하라.
                  API 타입은 frontend/src/types/pulse.ts를 참조하라.
                  구현 완료 후 _workspace/03_frontend_implementation.md에 구현 목록을 기록하라.
                  완료 시 qa-engineer에게 알려라."
       },
       {
         name: "qa-engineer",
         agent_type: "general-purpose",
         model: "opus",
         prompt: ".claude/agents/qa-engineer.md의 역할에 따라 품질을 검증하라.
                  PRD: youtube-pulse-prd.md의 Section 11을 기준으로 수용 기준을 확인하라.
                  backend-dev와 frontend-dev 모두 완료 알림을 받은 후 시작하라.
                  경계면 교차 비교를 최우선으로 수행하라 (API 응답 shape ↔ 프론트 타입).
                  결과를 _workspace/04_qa_report.md에 기록하라.
                  심각 버그 발견 시 해당 에이전트에게 즉시 보고하라."
       }
     ]
   )
   ```

2. 작업 등록:
   ```
   TaskCreate(tasks: [
     // architect 작업
     { title: "프로젝트 구조 생성", description: "Gradle/npm 프로젝트 scaffolding", assignee: "architect" },
     { title: "도메인 엔티티 생성", description: "Video, Comment, Keyword JPA 엔티티 + Repository", assignee: "architect" },
     { title: "Config 및 데모 데이터", description: "application.yml, CORS, demo-comments.json", assignee: "architect" },
     { title: "프론트 타입 정의", description: "types/pulse.ts TypeScript 타입", assignee: "architect" },

     // backend-dev 작업 (architect 완료 후)
     { title: "Collector 모듈", description: "YouTubeApiClient, CommentCollectorService, Scheduler", assignee: "backend-dev", depends_on: ["도메인 엔티티 생성"] },
     { title: "Analyzer 모듈", description: "SentimentAnalyzer, Claude/Simple 구현체, KeywordExtractor", assignee: "backend-dev", depends_on: ["도메인 엔티티 생성"] },
     { title: "API 모듈", description: "Controller, Service, DTO, Exception", assignee: "backend-dev", depends_on: ["도메인 엔티티 생성"] },
     { title: "Demo 데이터 로더", description: "CommandLineRunner, demo-comments.json 파싱", assignee: "backend-dev", depends_on: ["Config 및 데모 데이터"] },

     // frontend-dev 작업 (architect 완료 후)
     { title: "API 클라이언트 + 훅", description: "pulseApi.ts, useOverview, useVideos 등", assignee: "frontend-dev", depends_on: ["프론트 타입 정의"] },
     { title: "대시보드 컴포넌트", description: "Gauge, PieChart, LineChart, KeywordRanking, RequestTable", assignee: "frontend-dev", depends_on: ["API 클라이언트 + 훅"] },
     { title: "레이아웃 + 앱 조합", description: "Header, App.tsx, 반응형 레이아웃", assignee: "frontend-dev", depends_on: ["대시보드 컴포넌트"] },

     // qa-engineer 작업 (backend + frontend 완료 후)
     { title: "경계면 교차 비교", description: "API 응답 shape ↔ Frontend 타입 정의 1:1 대조", assignee: "qa-engineer", depends_on: ["API 모듈", "API 클라이언트 + 훅"] },
     { title: "JUnit 백엔드 테스트", description: "API 엔드포인트 양성/음성, Service 단위 테스트", assignee: "qa-engineer", depends_on: ["API 모듈", "Demo 데이터 로더"] },
     { title: "Playwright E2E 테스트", description: "대시보드 로딩, 영상 필터, 빈 상태", assignee: "qa-engineer", depends_on: ["레이아웃 + 앱 조합"] },
     { title: "QA 리포트 작성", description: "전체 검증 결과 요약", assignee: "qa-engineer", depends_on: ["경계면 교차 비교", "JUnit 백엔드 테스트", "Playwright E2E 테스트"] }
   ])
   ```

### Phase 3: 구현 (팀 자체 조율)

팀원들은 공유 작업 목록에서 작업을 요청하고 독립적으로 수행한다.

**작업 순서:**
1. **architect** → 프로젝트 구조 + 도메인 + config + 타입 정의 생성
2. architect 완료 알림 후 → **backend-dev** + **frontend-dev** 병렬 시작
3. backend-dev + frontend-dev 완료 알림 후 → **qa-engineer** 시작

**팀원 간 통신 규칙:**
- architect → backend-dev, frontend-dev: scaffolding 완료 시 "시작해도 됩니다" SendMessage
- backend-dev → frontend-dev: API 구현 완료 시 엔드포인트 목록 전달
- frontend-dev → backend-dev: 타입 불일치 발견 시 즉시 보고
- backend-dev, frontend-dev → qa-engineer: 모듈 완료 시 검증 요청
- qa-engineer → backend-dev, frontend-dev: 버그 발견 시 구체적 내용 전달

**리더 모니터링:**
- 팀원 유휴 알림 시 상태 확인
- architect가 10분 이상 지연 시 진행 상황 문의
- 팀원 간 충돌(API shape 불일치 등) 발생 시 PRD 기준으로 중재

**산출물 저장:**

| 팀원 | 출력 경로 |
|------|----------|
| architect | `_workspace/01_architect_structure.md` |
| backend-dev | `_workspace/02_backend_implementation.md` |
| frontend-dev | `_workspace/03_frontend_implementation.md` |
| qa-engineer | `_workspace/04_qa_report.md` |

### Phase 4: 통합 검증

1. 모든 팀원의 작업 완료 대기 (TaskGet으로 상태 확인)
2. QA 리포트 읽기 (`_workspace/04_qa_report.md`)
3. 심각 버그가 있으면:
   - 해당 에이전트에게 수정 요청 (SendMessage)
   - 수정 완료 후 qa-engineer에게 재검증 요청
4. Demo 모드 실행 검증:
   - `cd backend && ./gradlew bootRun --args='--spring.profiles.active=demo'`
   - `cd frontend && npm run dev`
   - 브라우저에서 대시보드 동작 확인

### Phase 5: 정리

1. 팀원들에게 종료 요청 (SendMessage)
2. 팀 정리 (TeamDelete)
3. `_workspace/` 보존 (중간 산출물은 삭제하지 않음)
4. 사용자에게 결과 요약 보고:
   - 구현 완료 모듈 목록
   - QA 결과 요약 (통과율, 주요 발견 사항)
   - Demo 모드 실행 방법
   - 후속 작업 제안

## 데이터 흐름

```
[리더] → TeamCreate → [architect] ─── scaffolding 완료 ───→ [backend-dev]
                                                          → [frontend-dev]
                                                               │
                          ┌── API shape 조율 (SendMessage) ────┘
                          ↓
                     [backend-dev] ─── 구현 완료 ───→ [qa-engineer]
                     [frontend-dev] ── 구현 완료 ───→ [qa-engineer]
                                                         │
                          ┌── 버그 보고 (SendMessage) ────┘
                          ↓
                     [backend-dev/frontend-dev] ── 수정 ──→ [qa-engineer: 재검증]
                                                              │
                                                              ↓
                                                    [리더: 최종 보고]
```

## 에러 핸들링

| 상황 | 전략 |
|------|------|
| architect 빌드 실패 | architect에게 재시도 요청. 2회 실패 시 리더가 직접 개입 |
| backend-dev API 구현 지연 | frontend-dev는 mock 데이터로 진행, 후에 실제 API 연결 |
| frontend-dev 차트 렌더링 오류 | Recharts 문서 확인 요청, 대안 컴포넌트 제안 |
| qa-engineer 경계면 불일치 다수 | 리더가 PRD 기준으로 정답 판정, 수정 에이전트 지정 |
| 팀원 1명 중지 | 리더가 감지 → 재시작 시도 → 실패 시 다른 팀원에게 작업 재할당 |
| 팀원 과반 실패 | 사용자에게 알리고 진행 여부 확인 |

## 테스트 시나리오

### 정상 흐름
1. 사용자가 "YooBe Pulse 구현해줘" 요청
2. Phase 0: `_workspace/` 미존재 → 초기 실행
3. Phase 1: PRD 읽기, `_workspace/` 생성
4. Phase 2: 4명 팀 구성, 16개 작업 등록
5. Phase 3: architect → backend-dev + frontend-dev (병렬) → qa-engineer
6. Phase 4: QA 통과, Demo 모드 정상 동작
7. Phase 5: 팀 정리, 사용자에게 결과 보고
8. 예상 결과: `backend/`, `frontend/`, `e2e/` 완성, Demo 모드 실행 가능

### 에러 흐름
1. Phase 3에서 qa-engineer가 API 응답 shape 불일치 발견
2. qa-engineer → backend-dev: "GET /api/videos 응답에 topKeywords 필드 누락" 보고
3. backend-dev: VideoSummaryResponse에 topKeywords 추가 → 재구현
4. qa-engineer: 재검증 → 통과
5. 최종 보고서에 "경계면 불일치 1건 수정됨" 기록

### 부분 재실행 흐름
1. 사용자가 "프론트엔드만 다시 구현해줘" 요청
2. Phase 0: `_workspace/` 존재 + 부분 수정 요청 → 부분 재실행
3. frontend-dev만 재호출 (기존 backend 유지)
4. qa-engineer가 변경된 부분의 경계면만 재검증
