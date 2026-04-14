package com.dingco.pulse.api.controller;

import com.dingco.pulse.api.dto.*;
import com.dingco.pulse.api.service.AnalyticsService;
import com.dingco.pulse.api.service.VideoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoQueryService videoQueryService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<List<VideoSummaryResponse>> getVideos(
            @RequestParam(defaultValue = "latest") String sort) {
        return ResponseEntity.ok(videoQueryService.getVideoList(sort));
    }

    @GetMapping("/{videoId}/sentiment")
    public ResponseEntity<SentimentDetailResponse> getSentiment(@PathVariable String videoId) {
        return ResponseEntity.ok(analyticsService.getSentimentDetail(videoId));
    }

    @GetMapping("/{videoId}/keywords")
    public ResponseEntity<List<KeywordResponse>> getKeywords(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "20") int limit) {
        if (limit < 1 || limit > 50) {
            limit = 20;
        }
        return ResponseEntity.ok(analyticsService.getKeywords(videoId, limit));
    }

    @GetMapping("/{videoId}/requests")
    public ResponseEntity<List<ContentRequestResponse>> getRequests(@PathVariable String videoId) {
        return ResponseEntity.ok(analyticsService.getContentRequests(videoId));
    }
}
