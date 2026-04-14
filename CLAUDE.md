# YooBe Pulse — 유튜브 민심 분석기

## 프로젝트 개요

유튜브 채널 댓글을 AI로 분석하여 시청자 민심을 대시보드로 시각화하는 서비스.
- Backend: Java 21 + Spring Boot 3.3 + JPA + H2/PostgreSQL
- Frontend: React 18 + TypeScript + Vite + Tailwind + Recharts
- 외부 API: YouTube Data API v3, Anthropic Claude API (Java SDK)
- 상세 요구사항: `youtube-pulse-prd.md`

## 하네스: YooBe Pulse

**목표:** PRD 기반으로 유튜브 민심 분석기 전체 서비스를 에이전트 팀으로 구현한다.

**트리거:** 유튜브 민심, 댓글 분석, YooBe Pulse, 대시보드 구현, 감정 분석, 키워드 분석 관련 작업 요청 시 `youtube-pulse-orchestrator` 스킬을 사용하라. 단순 질문은 직접 응답 가능.

**변경 이력:**
| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-04-14 | 초기 구성 | 전체 | 하네스 신규 구축 |
