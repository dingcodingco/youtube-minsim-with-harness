package com.dingco.pulse;

import com.dingco.pulse.analyzer.AnalysisResult;
import com.dingco.pulse.analyzer.KeywordExtractor;
import com.dingco.pulse.analyzer.SentimentAnalyzer;
import com.dingco.pulse.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final KeywordExtractor keywordExtractor;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Loading demo data from demo-comments.json...");

        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("demo-comments.json");

        try (InputStream is = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode videosNode = root.get("videos");

            int totalVideos = 0;
            int totalComments = 0;
            List<Comment> allComments = new ArrayList<>();

            for (JsonNode videoNode : videosNode) {
                String videoId = videoNode.get("videoId").asText();
                String title = videoNode.get("title").asText();
                String publishedAtStr = videoNode.get("publishedAt").asText();

                Video video = Video.builder()
                        .youtubeVideoId(videoId)
                        .title(title)
                        .channelId("demo-channel")
                        .publishedAt(LocalDateTime.parse(publishedAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .commentCount(0)
                        .build();

                video = videoRepository.save(video);
                totalVideos++;

                JsonNode commentsNode = videoNode.get("comments");
                int videoCommentCount = 0;

                for (JsonNode commentNode : commentsNode) {
                    String commentId = commentNode.get("id").asText();
                    String author = commentNode.get("author").asText();
                    String content = commentNode.get("content").asText();
                    int likeCount = commentNode.get("likeCount").asInt(0);
                    String commentPublishedAt = commentNode.get("publishedAt").asText();

                    Comment comment = Comment.builder()
                            .video(video)
                            .youtubeCommentId(commentId)
                            .authorName(author)
                            .content(content)
                            .likeCount(likeCount)
                            .publishedAt(LocalDateTime.parse(commentPublishedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .analyzed(false)
                            .build();

                    comment = commentRepository.save(comment);
                    allComments.add(comment);
                    videoCommentCount++;
                    totalComments++;
                }

                video.setCommentCount(videoCommentCount);
                video.setLastCollectedAt(LocalDateTime.now());
                videoRepository.save(video);
            }

            log.info("Loaded {} videos, {} comments from demo data", totalVideos, totalComments);

            // Run sentiment analysis on all comments
            log.info("Running sentiment analysis on demo comments...");
            List<AnalysisResult> results = sentimentAnalyzer.analyze(allComments);

            Map<Long, Comment> commentMap = allComments.stream()
                    .collect(Collectors.toMap(Comment::getId, Function.identity()));

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

            // Extract keywords
            log.info("Extracting keywords from analysis results...");
            keywordExtractor.extractAndSave(results, commentMap);

            log.info("Demo data loading complete: {} videos, {} comments analyzed", totalVideos, totalComments);
        }
    }
}
