package com.dingco.pulse.analyzer;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.dingco.pulse.domain.Comment;
import com.dingco.pulse.domain.Sentiment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("!demo")
public class ClaudeSentimentAnalyzer implements SentimentAnalyzer {

    @Value("${anthropic.api-key}")
    private String apiKey;

    @Value("${anthropic.model:claude-sonnet-4-20250514}")
    private String modelName;

    private AnthropicClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (!"demo".equals(apiKey)) {
            this.client = AnthropicOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
        }
    }

    @Override
    public List<AnalysisResult> analyze(List<Comment> comments) {
        if (client == null || comments.isEmpty()) {
            return List.of();
        }

        String commentsText = comments.stream()
                .map(c -> String.format("[ID:%d] %s", c.getId(), c.getContent()))
                .collect(Collectors.joining("\n"));

        String prompt = buildPrompt(commentsText);

        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(modelName)
                    .maxTokens(4096)
                    .addUserMessage(prompt)
                    .build();

            Message response = client.messages().create(params);

            String responseText = response.content().stream()
                    .filter(ContentBlock::isText)
                    .map(block -> block.asText().text())
                    .collect(Collectors.joining());

            return parseResponse(responseText, comments);

        } catch (Exception e) {
            log.error("Claude API call failed: {}", e.getMessage());
            return defaultResults(comments);
        }
    }

    private String buildPrompt(String commentsText) {
        return """
                다음 유튜브 댓글들을 분석해주세요. 각 댓글을 5가지 카테고리 중 하나로 분류하세요.

                카테고리:
                - POSITIVE: 칭찬, 감사, 호의적
                - NEGATIVE: 비판, 불만
                - NEUTRAL: 단순 정보, 감정 없음
                - REQUEST: 콘텐츠 요청 ("~해주세요", "~다뤄주세요")
                - QUESTION: 질문

                REQUEST인 경우 요청 주제(topic)와 추천 영상 제목(suggestedTitle)도 생성하세요.

                JSON 배열로만 응답해주세요:
                [{"commentId":"...", "sentiment":"...", "confidence":0.0~1.0, \
                "keywords":["..."], "requestTopic":null, "suggestedTitle":null}]

                댓글 목록:
                """ + commentsText;
    }

    private List<AnalysisResult> parseResponse(String responseText, List<Comment> comments) {
        List<AnalysisResult> results = new ArrayList<>();

        try {
            // Extract JSON array from response (may contain extra text)
            String jsonStr = extractJsonArray(responseText);
            JsonNode root = objectMapper.readTree(jsonStr);

            if (root.isArray()) {
                for (JsonNode node : root) {
                    try {
                        String commentIdStr = node.get("commentId").asText();
                        Long commentId = Long.parseLong(commentIdStr);

                        Sentiment sentiment = Sentiment.valueOf(node.get("sentiment").asText().toUpperCase());
                        double confidence = node.get("confidence").asDouble();

                        // If confidence < 0.7, default to NEUTRAL
                        if (confidence < 0.7) {
                            sentiment = Sentiment.NEUTRAL;
                        }

                        List<String> keywords = new ArrayList<>();
                        JsonNode keywordsNode = node.get("keywords");
                        if (keywordsNode != null && keywordsNode.isArray()) {
                            for (JsonNode kw : keywordsNode) {
                                keywords.add(kw.asText());
                            }
                        }

                        String requestTopic = node.has("requestTopic") && !node.get("requestTopic").isNull()
                                ? node.get("requestTopic").asText() : null;
                        String suggestedTitle = node.has("suggestedTitle") && !node.get("suggestedTitle").isNull()
                                ? node.get("suggestedTitle").asText() : null;

                        results.add(AnalysisResult.builder()
                                .commentId(commentId)
                                .sentiment(sentiment)
                                .confidence(confidence)
                                .keywords(keywords)
                                .requestTopic(requestTopic)
                                .suggestedTitle(suggestedTitle)
                                .build());
                    } catch (Exception e) {
                        log.warn("Failed to parse individual analysis result: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Claude response: {}", e.getMessage());
            return defaultResults(comments);
        }

        return results;
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private List<AnalysisResult> defaultResults(List<Comment> comments) {
        return comments.stream()
                .map(c -> AnalysisResult.builder()
                        .commentId(c.getId())
                        .sentiment(Sentiment.NEUTRAL)
                        .confidence(0.0)
                        .keywords(List.of())
                        .build())
                .collect(Collectors.toList());
    }
}
