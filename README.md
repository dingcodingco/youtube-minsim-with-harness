# 하네스 엔지니어링이 그래서 뭐냐고 [실전]

> 같은 AI 모델을 쓰는데, 어떤 사람은 하루에 PR을 3~4개씩 머지하고, 어떤 사람은 하나 만드는 데도 하루 종일 걸립니다. 차이가 뭘까요? **하네스가 다른 겁니다.**

이 레포는 [딩코딩코](https://www.youtube.com/@%EB%94%A9%EC%BD%94%EB%94%A9%EC%BD%94) 유튜브 채널의 **하네스 엔지니어링 실전편** 영상에서 사용된 프로젝트입니다.

영상 하나로 하네스 엔지니어링의 핵심을 보여드리기 위해, **YooBe Pulse(유튜브 민심 분석기)** 서비스를 에이전트 팀이 처음부터 끝까지 구현하는 과정을 담았습니다.

## 이 레포에서 볼 수 있는 것

### 1. 하네스 구조 (`.claude/`)

```
.claude/
├── agents/                          # 에이전트 팀 정의
│   ├── architect.md                 # 프로젝트 구조 설계자
│   ├── backend-dev.md               # Spring Boot 백엔드 개발자
│   ├── frontend-dev.md              # React 대시보드 개발자
│   └── qa-engineer.md               # 통합 품질 검증자
├── skills/
│   └── youtube-pulse-orchestrator/
│       └── SKILL.md                 # 팀 전체를 조율하는 오케스트레이터
```

**에이전트(누가)** 와 **스킬(어떻게)** 을 분리해서, AI에게 "이렇게 일해"라고 구조화된 지시를 내리는 것. 그게 하네스입니다.

### 2. 에이전트 팀이 만든 결과물

| 영역 | 파일 수 | 기술 스택 |
|------|--------|----------|
| Backend | 43개 | Java 21, Spring Boot 3.3, JPA, H2 |
| Frontend | 22개 | React 18, TypeScript, Vite, Tailwind, Recharts |
| 테스트 | 64개 | JUnit 5 (34개) + Playwright E2E (30개) |

**총 105개 파일, 9,700줄** — PRD 하나와 하네스만으로 에이전트 팀이 생성했습니다.

### 3. 에이전트 팀 실행 흐름

```
architect ──→ backend-dev  ──┐
          ──→ frontend-dev ──┼──→ qa-engineer
                             │
              SendMessage로  │
              API shape 조율 ┘
```

1. **architect** — 프로젝트 scaffolding (Gradle, npm, JPA 엔티티)
2. **backend-dev + frontend-dev** — 병렬로 백엔드/프론트엔드 구현
3. **qa-engineer** — 경계면 교차 비교, 테스트 작성, 버그 3건 발견 및 수정

## 직접 실행해보기

### Demo 모드 (API 키 필요 없음)

```bash
# 1. 백엔드 (터미널 1)
cd backend
./gradlew bootRun --args='--spring.profiles.active=demo'

# 2. 프론트엔드 (터미널 2)
cd frontend
npm install
npm run dev

# 3. 브라우저에서 http://localhost:5173 접속
```

Demo 모드에서는 3개 영상, 15개 댓글의 더미 데이터로 전체 기능을 체험할 수 있습니다.

### 프로덕션 모드

```bash
# 환경 변수 설정
export YOUTUBE_API_KEY=your-youtube-api-key
export ANTHROPIC_API_KEY=your-anthropic-api-key
export COLLECTOR_ENABLED=true
export ANALYZER_ENABLED=true

cd backend
./gradlew bootRun
```

## YooBe Pulse — 유튜브 민심 분석기

내 유튜브 채널의 댓글을 AI로 분석해서 **시청자가 진짜 원하는 콘텐츠가 뭔지** 대시보드로 보여주는 서비스.

### 주요 기능

- **감정 온도계** — 채널/영상별 민심을 0~100 점수로 한눈에
- **감정 분포 차트** — 긍정/부정/중립/요청/질문 5가지 감정 분류
- **트렌드 라인** — 30일간 민심의 흐름 추적
- **키워드 랭킹** — 시청자가 가장 많이 언급하는 단어
- **콘텐츠 요청 테이블** — "이 주제 다뤄주세요!" 요청을 자동 감지하고 추천 제목까지 생성

### 기술 스택

| 레이어 | 기술 |
|--------|------|
| Backend | Java 21 + Spring Boot 3.3 + JPA + H2/PostgreSQL |
| AI 분석 | Anthropic Claude API (Java SDK) |
| 외부 API | YouTube Data API v3 |
| Frontend | React 18 + TypeScript + Vite + Tailwind CSS |
| 차트 | Recharts |
| E2E 테스트 | Playwright |
| 단위 테스트 | JUnit 5 + Mockito |

### 시스템 아키텍처

```
┌──────────────────────────────────────────────────┐
│              Frontend (React)                     │
│  감정 온도계 · 분포 차트 · 트렌드 · 키워드 · 요청  │
├──────────────────────────────────────────────────┤
│             Backend API (Spring Boot)             │
│           REST Endpoints · DTO · 에러 처리         │
├──────────────┬───────────────────────────────────┤
│  Collector   │           Analyzer                 │
│  YouTube API │      Claude API 감정 분석           │
│  댓글 수집    │   키워드 추출 · 요청 감지            │
├──────────────┴───────────────────────────────────┤
│              Database (H2 / PostgreSQL)           │
└──────────────────────────────────────────────────┘
```

## 하네스 엔지니어링이란?

AI 코딩 에이전트에게 **구조화된 작업 환경**을 제공하는 기법입니다.

단순히 "이거 만들어줘"라고 프롬프트를 던지는 게 아니라:

1. **에이전트 정의** — 누가 어떤 역할을 하는지 명확히 분리
2. **스킬 정의** — 각 에이전트가 어떻게 일하는지 절차를 구조화
3. **오케스트레이션** — 에이전트 간 협업 흐름과 통신 프로토콜 설계
4. **품질 게이트** — 경계면 교차 비교로 통합 품질 자동 검증

이 4가지를 셋업하면, AI 에이전트가 **혼자서도 팀처럼 일합니다.**

## 딩코딩코

IT 교육 콘텐츠를 만드는 채널입니다.

- [YouTube](https://www.youtube.com/@%EB%94%A9%EC%BD%94%EB%94%A9%EC%BD%94)
- [인프런](https://www.inflearn.com/users/@dingcodingco)
