package com.dingco.pulse.collector;

import com.dingco.pulse.collector.dto.YouTubeCommentDto;
import com.dingco.pulse.domain.Comment;
import com.dingco.pulse.domain.CommentRepository;
import com.dingco.pulse.domain.Video;
import com.dingco.pulse.domain.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCollectorService {

    private final YouTubeApiClient youTubeApiClient;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public int collectComments(String youtubeVideoId) {
        Video video = videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + youtubeVideoId));

        List<YouTubeCommentDto> fetchedComments = youTubeApiClient.fetchComments(youtubeVideoId);

        int newCount = 0;
        for (YouTubeCommentDto dto : fetchedComments) {
            if (commentRepository.existsByYoutubeCommentId(dto.getCommentId())) {
                continue;
            }

            Comment comment = Comment.builder()
                    .video(video)
                    .youtubeCommentId(dto.getCommentId())
                    .authorName(dto.getAuthorName())
                    .content(dto.getContent())
                    .likeCount(dto.getLikeCount())
                    .publishedAt(dto.getPublishedAt())
                    .analyzed(false)
                    .build();

            commentRepository.save(comment);
            newCount++;
        }

        if (newCount > 0) {
            long totalComments = commentRepository.countByVideoId(video.getId());
            video.setCommentCount((int) totalComments);
            video.setLastCollectedAt(LocalDateTime.now());
            videoRepository.save(video);
            log.info("Collected {} new comments for video '{}' (total: {})", newCount, video.getTitle(), totalComments);
        }

        return newCount;
    }

    public void collectAllVideos() {
        List<Video> videos = videoRepository.findAll();
        for (Video video : videos) {
            try {
                collectComments(video.getYoutubeVideoId());
            } catch (Exception e) {
                log.error("Failed to collect comments for video '{}': {}", video.getTitle(), e.getMessage());
            }
        }
    }
}
