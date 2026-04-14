package com.dingco.pulse.analyzer;

import com.dingco.pulse.domain.Comment;
import com.dingco.pulse.domain.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pulse.analyzer.enabled", havingValue = "true")
public class AnalysisScheduler {

    private static final int BATCH_SIZE = 20;
    private static final int MAX_BATCHES = 5;

    private final CommentRepository commentRepository;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final KeywordExtractor keywordExtractor;

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void analyzeComments() {
        log.info("Starting scheduled comment analysis...");

        List<Comment> unanalyzed = commentRepository.findByAnalyzedFalse(
                PageRequest.of(0, BATCH_SIZE * MAX_BATCHES));

        if (unanalyzed.isEmpty()) {
            log.info("No unanalyzed comments found.");
            return;
        }

        log.info("Found {} unanalyzed comments to process", unanalyzed.size());

        // Process in batches of 20
        for (int i = 0; i < unanalyzed.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, unanalyzed.size());
            List<Comment> batch = unanalyzed.subList(i, end);

            try {
                processBatch(batch);
            } catch (Exception e) {
                log.error("Failed to process batch starting at index {}: {}", i, e.getMessage());
            }
        }

        log.info("Scheduled comment analysis completed.");
    }

    private void processBatch(List<Comment> batch) {
        Map<Long, Comment> commentMap = batch.stream()
                .collect(Collectors.toMap(Comment::getId, Function.identity()));

        List<AnalysisResult> results = sentimentAnalyzer.analyze(batch);

        for (AnalysisResult result : results) {
            Comment comment = commentMap.get(result.getCommentId());
            if (comment != null) {
                comment.setSentiment(result.getSentiment());
                comment.setConfidence(result.getConfidence());
                comment.setAnalyzed(true);
                if (result.getRequestTopic() != null) {
                    comment.setRequestTopic(result.getRequestTopic());
                }
                if (result.getSuggestedTitle() != null) {
                    comment.setSuggestedTitle(result.getSuggestedTitle());
                }
                commentRepository.save(comment);
            }
        }

        keywordExtractor.extractAndSave(results, commentMap);
    }
}
