package com.dingco.pulse;

import com.dingco.pulse.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("demo")
class DemoDataLoaderTest {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Test
    @DisplayName("Demo 모드에서 3개 비디오가 로드된다")
    void demoDataLoads_3Videos() {
        List<Video> videos = videoRepository.findAll();
        assertThat(videos).hasSize(3);
    }

    @Test
    @DisplayName("Demo 모드에서 15개 댓글이 로드된다")
    void demoDataLoads_15Comments() {
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(15);
    }

    @Test
    @DisplayName("Demo 모드에서 모든 댓글이 분석 완료 상태이다")
    void demoDataLoads_allCommentsAnalyzed() {
        List<Comment> unanalyzed = commentRepository.findByAnalyzedFalse();
        assertThat(unanalyzed).isEmpty();

        long analyzedCount = commentRepository.countByAnalyzedTrue();
        assertThat(analyzedCount).isEqualTo(15);
    }

    @Test
    @DisplayName("Demo 모드에서 각 비디오의 commentCount가 정확하다")
    void demoDataLoads_correctCommentCounts() {
        Video video1 = videoRepository.findByYoutubeVideoId("demo-video-001").orElseThrow();
        Video video2 = videoRepository.findByYoutubeVideoId("demo-video-002").orElseThrow();
        Video video3 = videoRepository.findByYoutubeVideoId("demo-video-003").orElseThrow();

        assertThat(video1.getCommentCount()).isEqualTo(5);
        assertThat(video2.getCommentCount()).isEqualTo(4);
        assertThat(video3.getCommentCount()).isEqualTo(6);

        // Verify DB counts match entity commentCount
        assertThat(commentRepository.countByVideoId(video1.getId())).isEqualTo(5);
        assertThat(commentRepository.countByVideoId(video2.getId())).isEqualTo(4);
        assertThat(commentRepository.countByVideoId(video3.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("Demo 모드에서 모든 댓글에 감정이 할당된다")
    void demoDataLoads_allCommentsHaveSentiment() {
        List<Comment> comments = commentRepository.findAll();
        comments.forEach(comment -> {
            assertThat(comment.getSentiment())
                    .as("Comment '%s' should have a sentiment", comment.getContent())
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("Demo 모드에서 키워드가 추출된다")
    void demoDataLoads_keywordsExtracted() {
        List<Keyword> keywords = keywordRepository.findAll();
        assertThat(keywords).isNotEmpty();

        // Every keyword must have a word and count > 0
        keywords.forEach(keyword -> {
            assertThat(keyword.getWord()).isNotBlank();
            assertThat(keyword.getCount()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("Demo 모드에서 각 비디오별 키워드가 존재한다")
    void demoDataLoads_keywordsPerVideo() {
        List<Video> videos = videoRepository.findAll();

        for (Video video : videos) {
            List<Keyword> keywords = keywordRepository.findByVideoIdOrderByCountDesc(video.getId());
            assertThat(keywords)
                    .as("Video '%s' should have keywords", video.getTitle())
                    .isNotEmpty();
        }
    }

    @Test
    @DisplayName("Demo 모드에서 비디오 ID가 정확하다")
    void demoDataLoads_correctVideoIds() {
        assertThat(videoRepository.findByYoutubeVideoId("demo-video-001")).isPresent();
        assertThat(videoRepository.findByYoutubeVideoId("demo-video-002")).isPresent();
        assertThat(videoRepository.findByYoutubeVideoId("demo-video-003")).isPresent();
    }

    @Test
    @DisplayName("Demo 모드에서 REQUEST 감정의 댓글에는 requestTopic이 있다")
    void demoDataLoads_requestCommentsHaveTopic() {
        List<Comment> requestComments = commentRepository.findAll().stream()
                .filter(c -> c.getSentiment() == Sentiment.REQUEST)
                .toList();

        if (!requestComments.isEmpty()) {
            requestComments.forEach(comment -> {
                assertThat(comment.getRequestTopic())
                        .as("REQUEST comment '%s' should have a requestTopic", comment.getContent())
                        .isNotBlank();
            });
        }
    }
}
