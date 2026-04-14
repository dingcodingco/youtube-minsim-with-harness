package com.dingco.pulse.analyzer;

import com.dingco.pulse.domain.Comment;
import com.dingco.pulse.domain.Sentiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("demo")
public class SimpleSentimentAnalyzer implements SentimentAnalyzer {

    private static final List<String> POSITIVE_KEYWORDS = List.of("좋아", "감사", "최고", "대박");
    private static final List<String> NEGATIVE_KEYWORDS = List.of("별로", "아쉬", "실망", "노잼");
    private static final List<String> REQUEST_KEYWORDS = List.of("해주세요", "다뤄주세요", "부탁");
    private static final List<String> QUESTION_KEYWORDS = List.of("인가요", "어떻게", "뭔가요");

    private static final Set<String> COMMON_WORDS = Set.of(
            "이", "그", "저", "것", "수", "등", "를", "을", "에", "의",
            "가", "는", "은", "도", "로", "와", "과", "한", "하", "합니다",
            "있", "없", "됩니다", "입니다", "ㅠㅠ", "ㅋㅋ", "ㅎㅎ",
            "너무", "정말", "진짜", "좀", "더", "잘"
    );

    @Override
    public List<AnalysisResult> analyze(List<Comment> comments) {
        List<AnalysisResult> results = new ArrayList<>();

        for (Comment comment : comments) {
            String content = comment.getContent();
            Sentiment sentiment = classifySentiment(content);
            List<String> keywords = extractKeywords(content);

            AnalysisResult.AnalysisResultBuilder resultBuilder = AnalysisResult.builder()
                    .commentId(comment.getId())
                    .sentiment(sentiment)
                    .confidence(0.85)
                    .keywords(keywords);

            if (sentiment == Sentiment.REQUEST) {
                String topic = extractRequestTopic(content);
                resultBuilder.requestTopic(topic);
                resultBuilder.suggestedTitle(topic + " 관련 영상");
            }

            results.add(resultBuilder.build());
        }

        log.info("SimpleSentimentAnalyzer analyzed {} comments", comments.size());
        return results;
    }

    private Sentiment classifySentiment(String content) {
        if (containsAny(content, REQUEST_KEYWORDS)) {
            return Sentiment.REQUEST;
        }
        if (containsAny(content, QUESTION_KEYWORDS)) {
            return Sentiment.QUESTION;
        }
        if (containsAny(content, POSITIVE_KEYWORDS)) {
            return Sentiment.POSITIVE;
        }
        if (containsAny(content, NEGATIVE_KEYWORDS)) {
            return Sentiment.NEGATIVE;
        }
        return Sentiment.NEUTRAL;
    }

    private boolean containsAny(String content, List<String> keywords) {
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<String> extractKeywords(String content) {
        return Arrays.stream(content.split("\\s+"))
                .filter(word -> word.length() >= 2)
                .filter(word -> !COMMON_WORDS.contains(word))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    private String extractRequestTopic(String content) {
        String topic = content;
        for (String keyword : REQUEST_KEYWORDS) {
            topic = topic.replace(keyword, "").trim();
        }
        // Remove trailing punctuation
        topic = topic.replaceAll("[!?.,~]+$", "").trim();
        if (topic.length() > 100) {
            topic = topic.substring(0, 100);
        }
        return topic.isEmpty() ? "기타 요청" : topic;
    }
}
