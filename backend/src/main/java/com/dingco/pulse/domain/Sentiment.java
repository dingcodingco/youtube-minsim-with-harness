package com.dingco.pulse.domain;

public enum Sentiment {
    POSITIVE,   // 긍정: 칭찬, 감사, 호의적
    NEGATIVE,   // 부정: 비판, 불만
    NEUTRAL,    // 중립: 단순 정보, 감정 없음
    REQUEST,    // 요청: "~해주세요", "~다뤄주세요"
    QUESTION    // 질문: "~인가요?", "~어떻게"
}
