package com.dingco.pulse.api.service;

import com.dingco.pulse.api.dto.VideoSummaryResponse;
import com.dingco.pulse.api.exception.VideoNotFoundException;
import com.dingco.pulse.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoQueryService {

    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final KeywordRepository keywordRepository;

    public List<VideoSummaryResponse> getVideoList(String sort) {
        List<Video> videos = videoRepository.findAll();

        List<VideoSummaryResponse> responses = videos.stream()
                .map(this::toVideoSummary)
                .collect(Collectors.toList());

        // Sort based on parameter
        switch (sort != null ? sort : "latest") {
            case "mostComments":
                responses.sort(Comparator.comparingInt(VideoSummaryResponse::getCommentCount).reversed());
                break;
            case "bestSentiment":
                responses.sort(Comparator.comparingDouble(VideoSummaryResponse::getSentimentScore).reversed());
                break;
            case "worstSentiment":
                responses.sort(Comparator.comparingDouble(VideoSummaryResponse::getSentimentScore));
                break;
            case "latest":
            default:
                responses.sort(Comparator.comparing(VideoSummaryResponse::getPublishedAt).reversed());
                break;
        }

        return responses;
    }

    public Video findByYoutubeVideoId(String youtubeVideoId) {
        return videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .orElseThrow(() -> new VideoNotFoundException(youtubeVideoId));
    }

    public double calculateSentimentScore(Long videoId) {
        long totalAnalyzed = commentRepository.countByVideoIdAndAnalyzedTrue(videoId);
        if (totalAnalyzed == 0) {
            return 0.0;
        }

        long positiveCount = commentRepository.countByVideoIdAndSentiment(videoId, Sentiment.POSITIVE);

        // Sentiment Score = (POSITIVE count / total analyzed) * 100
        return Math.round((double) positiveCount / totalAnalyzed * 1000.0) / 10.0;
    }

    private VideoSummaryResponse toVideoSummary(Video video) {
        double sentimentScore = calculateSentimentScore(video.getId());

        List<String> topKeywords = keywordRepository.findByVideoIdOrderByCountDesc(video.getId())
                .stream()
                .limit(3)
                .map(Keyword::getWord)
                .collect(Collectors.toList());

        return VideoSummaryResponse.builder()
                .videoId(video.getYoutubeVideoId())
                .title(video.getTitle())
                .publishedAt(video.getPublishedAt())
                .commentCount(video.getCommentCount())
                .sentimentScore(sentimentScore)
                .topKeywords(topKeywords)
                .build();
    }
}
