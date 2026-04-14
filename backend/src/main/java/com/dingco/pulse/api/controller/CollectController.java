package com.dingco.pulse.api.controller;

import com.dingco.pulse.api.dto.CollectionResultResponse;
import com.dingco.pulse.collector.CommentCollectorService;
import com.dingco.pulse.domain.CommentRepository;
import com.dingco.pulse.domain.Video;
import com.dingco.pulse.domain.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class CollectController {

    private final CommentCollectorService commentCollectorService;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;

    @PostMapping("/{videoId}")
    public ResponseEntity<CollectionResultResponse> collect(@PathVariable String videoId) {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("Video ID is required");
        }

        Video video = videoRepository.findByYoutubeVideoId(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + videoId));

        int newComments = commentCollectorService.collectComments(videoId);
        long totalComments = commentRepository.countByVideoId(video.getId());

        return ResponseEntity.ok(CollectionResultResponse.builder()
                .videoId(videoId)
                .newComments(newComments)
                .totalComments(totalComments)
                .status("COMPLETED")
                .build());
    }
}
