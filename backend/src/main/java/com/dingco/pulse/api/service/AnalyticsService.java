package com.dingco.pulse.api.service;

import com.dingco.pulse.api.dto.*;
import com.dingco.pulse.api.exception.VideoNotFoundException;
import com.dingco.pulse.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final KeywordRepository keywordRepository;
    private final VideoQueryService videoQueryService;

    public ChannelOverviewResponse getOverview() {
        List<Video> videos = videoRepository.findAll();
        long totalComments = commentRepository.countByAnalyzedTrue();

        // Calculate average sentiment across all videos
        double avgSentiment = 0.0;
        if (totalComments > 0) {
            long positiveCount = commentRepository.countBySentiment(Sentiment.POSITIVE);
            avgSentiment = Math.round((double) positiveCount / totalComments * 1000.0) / 10.0;
        }

        // Find top positive and negative videos
        ChannelOverviewResponse.VideoScoreSummary topPositive = null;
        ChannelOverviewResponse.VideoScoreSummary topNegative = null;
        double bestScore = -1;
        double worstScore = 101;

        for (Video video : videos) {
            double score = videoQueryService.calculateSentimentScore(video.getId());
            long analyzedCount = commentRepository.countByVideoIdAndAnalyzedTrue(video.getId());

            if (analyzedCount == 0) continue;

            if (score > bestScore) {
                bestScore = score;
                topPositive = ChannelOverviewResponse.VideoScoreSummary.builder()
                        .videoId(video.getYoutubeVideoId())
                        .title(video.getTitle())
                        .sentimentScore(score)
                        .commentCount(video.getCommentCount())
                        .build();
            }

            if (score < worstScore) {
                worstScore = score;
                topNegative = ChannelOverviewResponse.VideoScoreSummary.builder()
                        .videoId(video.getYoutubeVideoId())
                        .title(video.getTitle())
                        .sentimentScore(score)
                        .commentCount(video.getCommentCount())
                        .build();
            }
        }

        // Hottest keywords across all videos
        List<String> hottestKeywords = new ArrayList<>();
        Map<String, Integer> globalKeywordCounts = new HashMap<>();
        for (Video video : videos) {
            List<Keyword> keywords = keywordRepository.findByVideoIdOrderByCountDesc(video.getId());
            for (Keyword kw : keywords) {
                globalKeywordCounts.merge(kw.getWord(), kw.getCount(), Integer::sum);
            }
        }
        hottestKeywords = globalKeywordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Pending requests
        List<ChannelOverviewResponse.PendingRequest> pendingRequests = new ArrayList<>();
        List<Comment> allRequestComments = commentRepository.findByAnalyzedTrue().stream()
                .filter(c -> c.getSentiment() == Sentiment.REQUEST && c.getRequestTopic() != null)
                .collect(Collectors.toList());

        Map<String, List<Comment>> requestsByTopic = allRequestComments.stream()
                .collect(Collectors.groupingBy(Comment::getRequestTopic));

        for (Map.Entry<String, List<Comment>> entry : requestsByTopic.entrySet()) {
            String suggestedTitle = entry.getValue().stream()
                    .filter(c -> c.getSuggestedTitle() != null)
                    .map(Comment::getSuggestedTitle)
                    .findFirst()
                    .orElse(entry.getKey() + " 관련 영상");

            pendingRequests.add(ChannelOverviewResponse.PendingRequest.builder()
                    .topic(entry.getKey())
                    .count(entry.getValue().size())
                    .suggestedTitle(suggestedTitle)
                    .build());
        }

        pendingRequests.sort(Comparator.comparingInt(ChannelOverviewResponse.PendingRequest::getCount).reversed());

        return ChannelOverviewResponse.builder()
                .totalComments(totalComments)
                .avgSentiment(avgSentiment)
                .topPositiveVideo(topPositive)
                .topNegativeVideo(topNegative)
                .hottestKeywords(hottestKeywords)
                .pendingRequests(pendingRequests)
                .build();
    }

    public SentimentDetailResponse getSentimentDetail(String youtubeVideoId) {
        Video video = videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .orElseThrow(() -> new VideoNotFoundException(youtubeVideoId));

        long totalAnalyzed = commentRepository.countByVideoIdAndAnalyzedTrue(video.getId());

        // Build distribution
        Map<String, Double> distribution = new LinkedHashMap<>();
        for (Sentiment sentiment : Sentiment.values()) {
            long count = commentRepository.countByVideoIdAndSentiment(video.getId(), sentiment);
            double percentage = totalAnalyzed > 0
                    ? Math.round((double) count / totalAnalyzed * 1000.0) / 10.0
                    : 0.0;
            distribution.put(sentiment.name().toLowerCase(), percentage);
        }

        // Build sample comments (3 per category)
        Map<String, List<SentimentDetailResponse.SampleComment>> sampleComments = new LinkedHashMap<>();
        for (Sentiment sentiment : Sentiment.values()) {
            List<Comment> samples = commentRepository.findByVideoIdAndSentiment(
                    video.getId(), sentiment, PageRequest.of(0, 3));

            List<SentimentDetailResponse.SampleComment> sampleList = samples.stream()
                    .map(c -> SentimentDetailResponse.SampleComment.builder()
                            .author(c.getAuthorName())
                            .content(c.getContent())
                            .likeCount(c.getLikeCount())
                            .build())
                    .collect(Collectors.toList());

            if (!sampleList.isEmpty()) {
                sampleComments.put(sentiment.name().toLowerCase(), sampleList);
            }
        }

        return SentimentDetailResponse.builder()
                .videoId(video.getYoutubeVideoId())
                .title(video.getTitle())
                .totalAnalyzed(totalAnalyzed)
                .distribution(distribution)
                .sampleComments(sampleComments)
                .build();
    }

    public List<KeywordResponse> getKeywords(String youtubeVideoId, int limit) {
        Video video = videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .orElseThrow(() -> new VideoNotFoundException(youtubeVideoId));

        return keywordRepository.findByVideoIdOrderByCountDesc(video.getId()).stream()
                .limit(limit)
                .map(kw -> KeywordResponse.builder()
                        .word(kw.getWord())
                        .count(kw.getCount())
                        .sentiment(kw.getSentiment() != null ? kw.getSentiment().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public List<ContentRequestResponse> getContentRequests(String youtubeVideoId) {
        Video video = videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .orElseThrow(() -> new VideoNotFoundException(youtubeVideoId));

        List<Comment> requestComments = commentRepository.findRequestCommentsWithTopic(
                video.getId(), Sentiment.REQUEST);

        // Group by topic
        Map<String, List<Comment>> byTopic = requestComments.stream()
                .collect(Collectors.groupingBy(Comment::getRequestTopic));

        List<ContentRequestResponse> responses = new ArrayList<>();
        for (Map.Entry<String, List<Comment>> entry : byTopic.entrySet()) {
            List<SentimentDetailResponse.SampleComment> samples = entry.getValue().stream()
                    .limit(3)
                    .map(c -> SentimentDetailResponse.SampleComment.builder()
                            .author(c.getAuthorName())
                            .content(c.getContent())
                            .likeCount(c.getLikeCount())
                            .build())
                    .collect(Collectors.toList());

            String suggestedTitle = entry.getValue().stream()
                    .filter(c -> c.getSuggestedTitle() != null)
                    .map(Comment::getSuggestedTitle)
                    .findFirst()
                    .orElse(entry.getKey() + " 관련 영상");

            responses.add(ContentRequestResponse.builder()
                    .topic(entry.getKey())
                    .count(entry.getValue().size())
                    .sampleComments(samples)
                    .suggestedTitle(suggestedTitle)
                    .build());
        }

        responses.sort(Comparator.comparingInt(ContentRequestResponse::getCount).reversed());
        return responses;
    }

    public List<TrendPointResponse> getTrend(String youtubeVideoId, LocalDate from, LocalDate to) {
        // Default date range: last 30 days
        if (from == null) {
            from = LocalDate.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Comment> comments;
        if (youtubeVideoId != null && !youtubeVideoId.isEmpty()) {
            Video video = videoRepository.findByYoutubeVideoId(youtubeVideoId)
                    .orElseThrow(() -> new VideoNotFoundException(youtubeVideoId));
            comments = commentRepository.findAnalyzedCommentsByVideoAndBetween(
                    video.getId(), fromDateTime, toDateTime);
        } else {
            comments = commentRepository.findAnalyzedCommentsBetween(fromDateTime, toDateTime);
        }

        // Group by date
        Map<LocalDate, List<Comment>> byDate = comments.stream()
                .collect(Collectors.groupingBy(c -> c.getPublishedAt().toLocalDate()));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        List<TrendPointResponse> trend = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            List<Comment> dayComments = byDate.getOrDefault(current, List.of());

            double sentimentScore = 0.0;
            if (!dayComments.isEmpty()) {
                long positiveCount = dayComments.stream()
                        .filter(c -> c.getSentiment() == Sentiment.POSITIVE)
                        .count();
                sentimentScore = Math.round((double) positiveCount / dayComments.size() * 1000.0) / 10.0;
            }

            trend.add(TrendPointResponse.builder()
                    .date(current.format(dateFormatter))
                    .sentimentScore(sentimentScore)
                    .commentCount(dayComments.size())
                    .build());

            current = current.plusDays(1);
        }

        return trend;
    }
}
