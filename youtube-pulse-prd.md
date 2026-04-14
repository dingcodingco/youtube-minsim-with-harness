# PRD: YooBe Pulse — 유튜브 민심 분석기

**Version**: 1.0
**Author**: 딩코딩코
**Last Updated**: 2026-04-14

---

## 1. 프로젝트 개요

### 1.1 한 줄 요약
내 유튜브 채널의 댓글을 AI로 분석해서 **시청자가 진짜 원하는 콘텐츠가 뭔지** 대시보드로 보여주는 서비스.

### 1.2 배경
유튜브 크리에이터는 다음 콘텐츠를 기획할 때 댓글을 일일이 읽으며 감을 잡는다. 이 과정은 시간이 많이 들고, 주관적이며, 정량적 근거가 없다. YooBe Pulse는 이 과정을 자동화한다.

### 1.3 목표
- 특정 유튜브 채널의 댓글을 자동 수집
- AI로 댓글의 감정(긍정/부정/요청 등)을 분류
- 키워드 트렌드, 콘텐츠 요청 사항을 정량화
- 직관적인 대시보드로 민심의 흐름을 시각화
- API 키 없이도 Demo 모드로 즉시 체험 가능

### 1.4 타겟 사용자
- 유튜브 크리에이터 (1인 채널 ~ 중소 채널)
- 콘텐츠 기획자
- 커뮤니티 매니저

---

## 2. 기술 스택

| 레이어 | 기술 | 비고 |
|--------|------|------|
| **Backend** | Java 21 + Spring Boot 3.3 | 모놀리스, 모듈형 패키지 구조 |
| **ORM** | Spring Data JPA + Hibernate | |
| **DB** | H2 (로컬/데모), PostgreSQL (프로덕션) | |
| **외부 API** | YouTube Data API v3 | 댓글 수집 |
| **AI 분석** | Anthropic Claude API (Java SDK) | 감정 분류, 키워드 추출 |
| **Frontend** | React 18 + TypeScript + Vite | SPA |
| **스타일링** | Tailwind CSS | 다크 모드 기본 |
| **차트** | Recharts | 파이, 라인, 바 차트 |
| **E2E 테스트** | Playwright | |
| **단위 테스트** | JUnit 5 + Mockito | |
| **빌드** | Gradle (백엔드), npm (프론트엔드) | |

---

## 3. 시스템 아키텍처

### 3.1 전체 구조

```
┌──────────────────────────────────────────────────────────────┐
│                     Frontend (React)                          │
│   감정 온도계 · 감정 분포 차트 · 트렌드 · 키워드 · 요청 랭킹     │
├──────────────────────────────────────────────────────────────┤
│                    Backend API (Spring Boot)                   │
│                REST Endpoints · DTO · 에러 처리                 │
├──────────────────┬───────────────────────────────────────────┤
│  Collector 모듈   │              Analyzer 모듈                 │
│  YouTube API v3  │         Claude API 감정 분석                │
│  댓글 수집/저장    │      키워드 추출 · 요청 감지                 │
├──────────────────┴───────────────────────────────────────────┤
│                     Database (H2 / PostgreSQL)                │
│           videos · comments · keywords                        │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 모듈 구조 (패키지 = 에이전트 경계)

```
youtube-pulse/
├── backend/
│   └── src/main/java/com/dingco/pulse/
│       ├── PulseApplication.java
│       ├── collector/           ← Collector 에이전트 영역
│       │   ├── YouTubeApiClient.java
│       │   ├── CommentCollectorService.java
│       │   ├── CollectorScheduler.java
│       │   └── dto/
│       │       └── YouTubeCommentDto.java
│       ├── analyzer/            ← Analyzer 에이전트 영역
│       │   ├── SentimentAnalyzer.java          (인터페이스)
│       │   ├── ClaudeSentimentAnalyzer.java     (prod 구현체)
│       │   ├── SimpleSentimentAnalyzer.java     (demo 구현체)
│       │   ├── KeywordExtractor.java
│       │   ├── ContentRequestDetector.java
│       │   └── AnalysisScheduler.java
│       ├── api/                 ← Backend API 에이전트 영역
│       │   ├── controller/
│       │   │   ├── VideoController.java
│       │   │   ├── AnalyticsController.java
│       │   │   └── CollectController.java
│       │   ├── dto/
│       │   │   ├── VideoSummaryResponse.java
│       │   │   ├── SentimentDetailResponse.java
│       │   │   ├── KeywordResponse.java
│       │   │   ├── ContentRequestResponse.java
│       │   │   ├── TrendPointResponse.java
│       │   │   ├── ChannelOverviewResponse.java
│       │   │   └── CollectionResultResponse.java
│       │   ├── service/
│       │   │   ├── VideoQueryService.java
│       │   │   └── AnalyticsService.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       ├── VideoNotFoundException.java
│       │       └── ErrorResponse.java
│       ├── domain/              ← 공유 도메인 (Backend API 소유)
│       │   ├── Video.java
│       │   ├── Comment.java
│       │   ├── Keyword.java
│       │   ├── Sentiment.java           (enum)
│       │   ├── VideoRepository.java
│       │   ├── CommentRepository.java
│       │   └── KeywordRepository.java
│       └── config/
│           ├── WebConfig.java           (CORS 등)
│           └── SchedulingConfig.java
│
├── frontend/
│   ├── package.json
│   └── src/
│       ├── App.tsx
│       ├── api/
│       │   └── pulseApi.ts              ← API 클라이언트 (fetch 래퍼)
│       ├── types/
│       │   └── pulse.ts                 ← API 응답 타입 정의
│       ├── hooks/
│       │   ├── useOverview.ts
│       │   ├── useVideos.ts
│       │   ├── useSentiment.ts
│       │   └── useTrend.ts
│       └── components/
│           ├── layout/
│           │   ├── Header.tsx
│           │   └── Sidebar.tsx
│           ├── dashboard/
│           │   ├── SentimentGauge.tsx    ← 감정 온도계
│           │   ├── SentimentPieChart.tsx ← 감정 분포 도넛
│           │   ├── TrendLineChart.tsx   ← 시간별 트렌드
│           │   ├── KeywordRanking.tsx   ← 키워드 바 차트
│           │   └── ContentRequestTable.tsx ← 요청 랭킹 테이블
│           ├── VideoSelector.tsx         ← 영상 선택 드롭다운
│           └── common/
│               ├── LoadingSpinner.tsx
│               └── EmptyState.tsx
│
├── e2e/                                 ← QA 에이전트 영역
│   ├── package.json
│   ├── playwright.config.ts
│   └── tests/
│       ├── dashboard-load.spec.ts
│       ├── video-filter.spec.ts
│       ├── sentiment-display.spec.ts
│       ├── keyword-ranking.spec.ts
│       └── empty-state.spec.ts
│
└── docs/
    ├── api-contract.md
    ├── data-schema.md
    └── bugs/                            ← QA 버그 리포트
```

---

## 4. 데이터 모델

### 4.1 ERD

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│    Video     │       │     Comment      │       │   Keyword    │
├──────────────┤       ├──────────────────┤       ├──────────────┤
│ id (PK)      │1    N │ id (PK)          │       │ id (PK)      │
│ youtubeVideoId│◄─────│ video_id (FK)    │       │ video_id (FK)│
│ title        │       │ youtubeCommentId │       │ word         │
│ channelId    │       │ authorName       │       │ count        │
│ publishedAt  │       │ content (TEXT)   │       │ sentiment    │
│ commentCount │       │ likeCount        │       │ createdAt    │
│ createdAt    │       │ publishedAt      │       └──────────────┘
│ updatedAt    │       │ sentiment (ENUM) │              ▲
└──────────────┘       │ confidence       │              │
        │              │ analyzed (bool)  │              │
        │              │ requestTopic     │              │
        │              │ suggestedTitle   │              │
        │              │ createdAt        │              │
        │              └──────────────────┘              │
        │                                                │
        └────────────────────────────────────────────────┘
                           1 : N
```

### 4.2 Entity 상세

#### Video
| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | |
| youtubeVideoId | String(20) | UNIQUE, NOT NULL | YouTube 영상 ID |
| title | String(500) | NOT NULL | 영상 제목 |
| channelId | String(50) | NOT NULL | 채널 ID |
| publishedAt | LocalDateTime | NOT NULL | 영상 게시일 |
| commentCount | int | DEFAULT 0 | 수집된 댓글 수 |
| lastCollectedAt | LocalDateTime | NULLABLE | 마지막 수집 시점 |
| createdAt | LocalDateTime | NOT NULL | |
| updatedAt | LocalDateTime | NOT NULL | |

#### Comment
| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | |
| video | Video | FK(ManyToOne, LAZY) | |
| youtubeCommentId | String(50) | UNIQUE, NOT NULL | YouTube 댓글 ID (중복 방지) |
| authorName | String(200) | NOT NULL | 댓글 작성자 |
| content | String(TEXT) | NOT NULL | 댓글 내용 |
| likeCount | int | DEFAULT 0 | 좋아요 수 |
| publishedAt | LocalDateTime | NOT NULL | 댓글 작성일 |
| sentiment | Sentiment(ENUM) | NULLABLE | 분석 결과 (분석 전 null) |
| confidence | Double | NULLABLE | 분석 신뢰도 (0.0~1.0) |
| analyzed | boolean | DEFAULT false | 분석 완료 여부 |
| requestTopic | String(200) | NULLABLE | REQUEST일 때 요청 주제 |
| suggestedTitle | String(300) | NULLABLE | REQUEST일 때 추천 영상 제목 |
| createdAt | LocalDateTime | NOT NULL | |

#### Keyword
| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | |
| video | Video | FK(ManyToOne, LAZY) | |
| word | String(100) | NOT NULL | 키워드 |
| count | int | NOT NULL | 출현 빈도 |
| sentiment | Sentiment(ENUM) | NULLABLE | 키워드의 주된 감정 |
| createdAt | LocalDateTime | NOT NULL | |

#### Sentiment (Enum)
```java
public enum Sentiment {
    POSITIVE,   // 긍정: 칭찬, 감사, 호의적
    NEGATIVE,   // 부정: 비판, 불만
    NEUTRAL,    // 중립: 단순 정보, 감정 없음
    REQUEST,    // 요청: "~해주세요", "~다뤄주세요"
    QUESTION    // 질문: "~인가요?", "~어떻게"
}
```

### 4.3 데이터 흐름

```
[YouTube API] 
    → Collector: 댓글 수집, Comment(analyzed=false) 저장
    → Analyzer: analyzed=false인 댓글 배치 조회
    → Claude API: 감정 분류 + 키워드 추출
    → Analyzer: Comment.sentiment/confidence 업데이트, analyzed=true
    → Analyzer: Keyword 테이블에 키워드 저장/갱신
    → API: Comment + Keyword 테이블 조회 → DTO 변환 → JSON 응답
    → Frontend: API 호출 → 차트 렌더링
```

---

## 5. API 스펙

### 5.1 기본 규칙
- Base URL: `http://localhost:8080/api`
- Content-Type: `application/json`
- 에러 응답: `{ "code": "VIDEO_NOT_FOUND", "message": "영상을 찾을 수 없습니다" }`

### 5.2 엔드포인트

#### GET /api/analytics/overview
채널 전체 민심 요약.

**Response 200:**
```json
{
  "totalComments": 1523,
  "avgSentiment": 72.5,
  "topPositiveVideo": {
    "videoId": "abc123",
    "title": "하네스 엔지니어링이 뭔데?",
    "sentimentScore": 89.2,
    "commentCount": 245
  },
  "topNegativeVideo": {
    "videoId": "def456",
    "title": "개발자 면접 후기",
    "sentimentScore": 45.1,
    "commentCount": 87
  },
  "hottestKeywords": ["하네스", "Claude Code", "실전", "Cursor"],
  "pendingRequests": [
    {
      "topic": "하네스 엔지니어링 심화",
      "count": 5,
      "suggestedTitle": "하네스 엔지니어링 심화편 — Skills와 Hooks 마스터하기"
    }
  ]
}
```

---

#### GET /api/videos
영상 목록 + 각 영상의 감정 요약.

**Query Parameters:**
- `sort` (optional): `latest` | `mostComments` | `bestSentiment` | `worstSentiment` (default: `latest`)

**Response 200:**
```json
[
  {
    "videoId": "abc123",
    "title": "하네스 엔지니어링이 뭔데?",
    "publishedAt": "2026-04-10T09:00:00",
    "commentCount": 245,
    "sentimentScore": 89.2,
    "topKeywords": ["하네스", "Claude Code", "에이전트"]
  }
]
```

---

#### GET /api/videos/{videoId}/sentiment
특정 영상의 감정 분포.

**Response 200:**
```json
{
  "videoId": "abc123",
  "title": "하네스 엔지니어링이 뭔데?",
  "totalAnalyzed": 230,
  "distribution": {
    "positive": 52.1,
    "negative": 8.7,
    "neutral": 21.3,
    "request": 12.6,
    "question": 5.3
  },
  "sampleComments": {
    "positive": [
      { "author": "개발새발", "content": "이런 영상 정말 감사합니다!", "likeCount": 23 }
    ],
    "negative": [
      { "author": "코딩왕", "content": "설명이 너무 빨라요", "likeCount": 5 }
    ],
    "request": [
      { "author": "주니어맨", "content": "Cursor vs Claude Code 비교 영상도 해주세요!", "likeCount": 41 }
    ]
  }
}
```

**Response 404:**
```json
{ "code": "VIDEO_NOT_FOUND", "message": "해당 영상을 찾을 수 없습니다" }
```

---

#### GET /api/videos/{videoId}/keywords
특정 영상의 키워드 랭킹.

**Query Parameters:**
- `limit` (optional): 1~50 (default: 20)

**Response 200:**
```json
[
  {
    "word": "하네스",
    "count": 47,
    "sentiment": "POSITIVE"
  },
  {
    "word": "프롬프트",
    "count": 23,
    "sentiment": "NEUTRAL"
  }
]
```

---

#### GET /api/videos/{videoId}/requests
시청자 콘텐츠 요청 목록.

**Response 200:**
```json
[
  {
    "topic": "Cursor vs Claude Code 비교",
    "count": 8,
    "sampleComments": [
      { "author": "주니어맨", "content": "Cursor랑 Claude Code 비교 영상 해주세요!", "likeCount": 41 }
    ],
    "suggestedTitle": "Cursor vs Claude Code — 2026년 어떤 걸 써야 할까?"
  },
  {
    "topic": "하네스 엔지니어링 심화",
    "count": 5,
    "sampleComments": [
      { "author": "시니어곰", "content": "하네스 심화편도 부탁드려요 Skills 부분이요", "likeCount": 12 }
    ],
    "suggestedTitle": "하네스 엔지니어링 심화편 — Skills와 Hooks 마스터하기"
  }
]
```

---

#### GET /api/analytics/trend
감정 트렌드 (시간축).

**Query Parameters:**
- `videoId` (optional): 특정 영상만 필터. 없으면 채널 전체.
- `from` (optional): ISO date (default: 30일 전)
- `to` (optional): ISO date (default: 오늘)

**Response 200:**
```json
[
  { "date": "2026-04-01", "sentimentScore": 71.2, "commentCount": 34 },
  { "date": "2026-04-02", "sentimentScore": 75.8, "commentCount": 42 },
  { "date": "2026-04-03", "sentimentScore": 68.1, "commentCount": 28 }
]
```

---

#### POST /api/collect/{videoId}
특정 영상의 댓글을 수동으로 수집.

**Response 200:**
```json
{
  "videoId": "abc123",
  "newComments": 23,
  "totalComments": 245,
  "status": "COMPLETED"
}
```

**Response 400 (videoId 형식 오류):**
```json
{ "code": "INVALID_VIDEO_ID", "message": "유효하지 않은 영상 ID입니다" }
```

---

## 6. 프론트엔드 요구사항

### 6.1 페이지 구성

단일 페이지 대시보드 (SPA). 상단에 채널 전체 요약, 아래에 영상별 상세.

```
┌─────────────────────────────────────────────────────┐
│  Header: "YooBe Pulse 🎯"         [영상 선택 ▼]     │
├──────────────┬──────────────────────────────────────┤
│              │                                      │
│  감정 온도계  │     감정 분포 도넛 차트                 │
│  (게이지)    │     (POSITIVE/NEGATIVE/...)           │
│   72.5       │                                      │
│              │                                      │
├──────────────┴──────────────────────────────────────┤
│                                                     │
│          감정 트렌드 라인 차트 (30일)                  │
│                                                     │
├────────────────────┬────────────────────────────────┤
│                    │                                │
│  키워드 랭킹        │   시청자 요청 콘텐츠 테이블       │
│  (수평 바 차트)     │   topic · count · suggestedTitle│
│                    │                                │
└────────────────────┴────────────────────────────────┘
```

### 6.2 컴포넌트 요구사항

#### SentimentGauge (감정 온도계)
- 0~100 반원 게이지
- 색상 그라데이션: 0~30 빨강, 30~60 노랑, 60~100 초록
- 중앙에 큰 숫자 표시
- 아래에 "총 {N}개 댓글 기준" 텍스트

#### SentimentPieChart (감정 분포)
- Recharts PieChart (도넛)
- 색상: POSITIVE=#22c55e, NEGATIVE=#ef4444, NEUTRAL=#6b7280, REQUEST=#3b82f6, QUESTION=#a855f7
- 각 세그먼트에 퍼센트 라벨
- 클릭 시 해당 감정의 샘플 댓글 3개 표시 (툴팁 또는 하단 영역)

#### TrendLineChart (감정 트렌드)
- Recharts LineChart
- X축: 날짜, Y축: sentimentScore (0~100)
- 보조 Y축: commentCount (바 차트 겹치기)
- 기간 필터: 7일 / 14일 / 30일 토글

#### KeywordRanking (키워드 랭킹)
- 수평 바 차트 (Top 10)
- 바 색상: 키워드의 sentiment에 따라 변경
- 키워드 클릭 시 해당 키워드가 포함된 댓글 목록 표시 (선택 기능)

#### ContentRequestTable (콘텐츠 요청 테이블)
- 테이블 컬럼: 순위, 주제, 요청 수, 추천 제목
- 요청 수 기준 내림차순 정렬
- 행 클릭 시 관련 댓글 펼치기

#### VideoSelector (영상 선택)
- 드롭다운: "전체 채널" + 개별 영상 목록
- 각 영상 옆에 감정 점수 뱃지 (색상 코딩)
- 선택 시 하위 모든 컴포넌트 데이터 갱신

### 6.3 디자인 가이드
- **다크 모드 기본**: 배경 #0f172a, 카드 #1e293b, 텍스트 #f1f5f9
- 폰트: 시스템 폰트 (Pretendard 선호, 없으면 sans-serif)
- 카드 기반 레이아웃: 각 컴포넌트를 rounded-xl 카드에 배치
- 반응형: 모바일에서 1열, 데스크톱에서 2열 그리드
- 로딩 상태: 스켈레톤 UI
- 빈 데이터: "아직 분석된 댓글이 없습니다 🔍" 메시지

---

## 7. Collector 모듈 요구사항

### 7.1 YouTube Data API v3 연동

**API 엔드포인트:** `https://www.googleapis.com/youtube/v3/commentThreads`

**필요 파라미터:**
```
part=snippet
videoId={videoId}
maxResults=100
pageToken={nextPageToken}
order=time
key={API_KEY}
```

**설정:** `application.yml`
```yaml
youtube:
  api-key: ${YOUTUBE_API_KEY:demo}
  max-results-per-page: 100
```

### 7.2 수집 로직
- `youtubeCommentId`로 중복 체크 후 새 댓글만 저장
- 대댓글은 1차 범위에서 제외 (최상위 댓글만 수집)
- `analyzed = false`로 저장 (Analyzer가 나중에 분석)
- Video의 `commentCount`, `lastCollectedAt` 갱신

### 7.3 스케줄링
- `@Scheduled(fixedDelay = 3600000)` — 매시간 등록된 모든 영상 수집
- 스케줄러 on/off: `pulse.collector.enabled=true`

### 7.4 Demo 모드
- `@Profile("demo")`에서는 YouTube API 호출 대신 `demo-comments.json` 로딩
- `CommandLineRunner`로 앱 시작 시 자동 로딩

---

## 8. Analyzer 모듈 요구사항

### 8.1 감정 분석

**인터페이스:**
```java
public interface SentimentAnalyzer {
    List<AnalysisResult> analyze(List<Comment> comments);
}
```

**프로덕션 구현체 (ClaudeSentimentAnalyzer):**
- Anthropic Java SDK 사용
- 배치 처리: 최대 20개 댓글을 한 번의 API 호출로 분석
- 응답 파싱: JSON 형식으로 요청하여 구조화된 결과 수신
- confidence 0.7 미만 → NEUTRAL로 기본 분류
- REQUEST 분류 시 requestTopic + suggestedTitle 함께 생성

**Claude API 프롬프트:**
```
다음 유튜브 댓글들을 분석해주세요. 각 댓글을 5가지 카테고리 중 하나로 분류하세요.

카테고리:
- POSITIVE: 칭찬, 감사, 호의적
- NEGATIVE: 비판, 불만
- NEUTRAL: 단순 정보, 감정 없음
- REQUEST: 콘텐츠 요청 ("~해주세요", "~다뤄주세요")
- QUESTION: 질문

REQUEST인 경우 요청 주제(topic)와 추천 영상 제목(suggestedTitle)도 생성하세요.

JSON 배열로만 응답해주세요:
[{"commentId":"...", "sentiment":"...", "confidence":0.0~1.0, 
  "keywords":["..."], "requestTopic":null, "suggestedTitle":null}]

댓글 목록:
{comments}
```

**Demo 구현체 (SimpleSentimentAnalyzer):**
- API 호출 없이 키워드 매칭 기반 간이 분류
- "좋아", "감사", "최고", "대박" → POSITIVE
- "별로", "아쉬", "실망", "노잼" → NEGATIVE
- "해주세요", "다뤄주세요", "부탁" → REQUEST
- "인가요", "어떻게", "뭔가요" → QUESTION
- 그 외 → NEUTRAL

### 8.2 키워드 추출
- Claude API 분석 결과의 keywords 필드 활용
- 동일 키워드는 count 누적
- Video별로 Keyword 테이블에 저장

### 8.3 스케줄링
- `@Scheduled(fixedDelay = 300000)` — 5분마다 미분석 댓글 배치 처리
- 한 번에 최대 100개 댓글 처리 (5 batch × 20개)
- `pulse.analyzer.enabled=true`

---

## 9. Demo 모드 스펙

API 키 없이 전체 기능을 체험할 수 있어야 한다.

### 9.1 실행 방법
```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=demo'
cd frontend && npm run dev
```

### 9.2 더미 데이터 (demo-comments.json)
```json
{
  "videos": [
    {
      "videoId": "demo-video-001",
      "title": "하네스 엔지니어링이 뭔데?",
      "publishedAt": "2026-04-10T09:00:00",
      "comments": [
        { "id": "c001", "author": "개발새발", "content": "이런 영상 정말 감사합니다! 하네스 개념이 확 와닿았어요", "likeCount": 23, "publishedAt": "2026-04-10T10:30:00" },
        { "id": "c002", "author": "주니어맨", "content": "Cursor vs Claude Code 비교 영상도 해주세요!", "likeCount": 41, "publishedAt": "2026-04-10T11:00:00" },
        { "id": "c003", "author": "코딩왕", "content": "설명이 너무 빨라요 좀 천천히 해주시면 좋겠어요", "likeCount": 5, "publishedAt": "2026-04-10T12:00:00" },
        { "id": "c004", "author": "시니어곰", "content": "하네스 심화편도 부탁드려요 Skills 부분이요", "likeCount": 12, "publishedAt": "2026-04-10T14:00:00" },
        { "id": "c005", "author": "풀스택러", "content": "Spring Boot에 적용하는 방법이 궁금합니다", "likeCount": 8, "publishedAt": "2026-04-10T15:30:00" }
      ]
    },
    {
      "videoId": "demo-video-002",
      "title": "AI 시대 개발자 생존전략",
      "publishedAt": "2026-04-05T09:00:00",
      "comments": [
        { "id": "c101", "author": "이직준비", "content": "현실적인 조언 감사합니다 용기가 생겼어요", "likeCount": 56, "publishedAt": "2026-04-05T10:00:00" },
        { "id": "c102", "author": "비전공자", "content": "비전공자인데 너무 막막합니다", "likeCount": 15, "publishedAt": "2026-04-05T11:30:00" },
        { "id": "c103", "author": "5년차", "content": "이력서 작성 팁 영상도 만들어주세요!", "likeCount": 32, "publishedAt": "2026-04-05T13:00:00" },
        { "id": "c104", "author": "대학생A", "content": "포트폴리오 관련 내용도 다뤄주세요", "likeCount": 27, "publishedAt": "2026-04-06T09:00:00" }
      ]
    },
    {
      "videoId": "demo-video-003",
      "title": "Claude Code 처음 쓰는 법",
      "publishedAt": "2026-03-28T09:00:00",
      "comments": [
        { "id": "c201", "author": "뉴비개발", "content": "와 이거 진짜 신세계네요 바로 써봤습니다", "likeCount": 44, "publishedAt": "2026-03-28T10:00:00" },
        { "id": "c202", "author": "자바장인", "content": "IntelliJ에서 쓸 수 있는 방법은 없나요?", "likeCount": 19, "publishedAt": "2026-03-28T12:00:00" },
        { "id": "c203", "author": "코린이", "content": "저도 따라해봤는데 잘 안 돼요 ㅠㅠ", "likeCount": 3, "publishedAt": "2026-03-29T08:00:00" },
        { "id": "c204", "author": "프론트엔드", "content": "React 프로젝트에서 쓰는 법도 알려주세요!", "likeCount": 38, "publishedAt": "2026-03-29T10:00:00" },
        { "id": "c205", "author": "CTO", "content": "팀에 도입하려면 어떤 절차가 좋을까요?", "likeCount": 22, "publishedAt": "2026-03-30T14:00:00" },
        { "id": "c206", "author": "감사맨", "content": "최고의 Claude Code 튜토리얼입니다 구독 박았습니다", "likeCount": 61, "publishedAt": "2026-03-30T16:00:00" }
      ]
    }
  ]
}
```

영상 3개, 댓글 총 15개. 감정이 골고루 분포되어 있어 차트가 다채롭게 나온다.

---

## 10. 비기능 요구사항

### 10.1 성능
- API 응답 시간: 500ms 이내 (H2 기준)
- 프론트 초기 로딩: 3초 이내
- Analyzer 배치: 20개 댓글 분석에 5초 이내 (Claude API 응답 시간 의존)

### 10.2 에러 처리
- 모든 API 에러는 `{ "code": "...", "message": "..." }` 형식
- YouTube API 장애 시: 수집 실패 로그 + 다음 스케줄에서 재시도
- Claude API 장애 시: 분석 스킵 + analyzed=false 유지
- 프론트: 각 API 호출에 try-catch + 사용자 친화적 에러 메시지

### 10.3 CORS
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST");
    }
}
```

### 10.4 환경 변수
```yaml
# application.yml
youtube:
  api-key: ${YOUTUBE_API_KEY:demo}
  
anthropic:
  api-key: ${ANTHROPIC_API_KEY:demo}
  model: claude-sonnet-4-20250514

pulse:
  collector:
    enabled: ${COLLECTOR_ENABLED:false}
  analyzer:
    enabled: ${ANALYZER_ENABLED:false}
```

Demo 모드에서는 스케줄러 비활성, API 키 "demo" 기본값.

---

## 11. 수용 기준 (Acceptance Criteria)

### 11.1 전체 파이프라인
- [ ] Demo 모드에서 앱 시작 시 더미 데이터 자동 로딩
- [ ] 로딩된 댓글이 SimpleSentimentAnalyzer로 분석됨
- [ ] 분석 결과가 API를 통해 조회됨
- [ ] 대시보드에 차트가 정상 렌더링됨

### 11.2 API
- [ ] api-contract.md의 모든 엔드포인트가 구현됨
- [ ] 각 응답이 정의된 스키마와 정확히 일치
- [ ] 존재하지 않는 videoId → 404 + ErrorResponse
- [ ] 유효하지 않은 파라미터 → 400 + ErrorResponse

### 11.3 Frontend
- [ ] 감정 온도계가 avgSentiment를 정확히 표시
- [ ] 영상 선택 시 하위 컴포넌트 데이터 갱신
- [ ] 빈 데이터 상태에서 에러 없이 빈 상태 메시지 표시
- [ ] 모바일 뷰포트(375px)에서 레이아웃 깨지지 않음

### 11.4 테스트
- [ ] 백엔드: 각 API 엔드포인트 양성 + 음성 테스트
- [ ] E2E: 대시보드 로딩, 영상 필터, 빈 상태 시나리오
- [ ] 데이터 정합성: 수집 수 = DB 행 수 = API 응답 수
