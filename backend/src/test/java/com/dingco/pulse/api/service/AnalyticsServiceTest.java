package com.dingco.pulse.api.service;

import com.dingco.pulse.api.dto.*;
import com.dingco.pulse.api.exception.VideoNotFoundException;
import com.dingco.pulse.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private VideoQueryService videoQueryService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Video testVideo;

    @BeforeEach
    void setUp() {
        testVideo = Video.builder()
                .id(1L)
                .youtubeVideoId("demo-video-001")
                .title("테스트 영상")
                .channelId("demo-channel")
                .publishedAt(LocalDateTime.of(2026, 4, 10, 9, 0))
                .commentCount(5)
                .build();
    }

    // ========== getSentimentDetail ==========

    @Test
    @DisplayName("getSentimentDetail - 감정 분포 퍼센트 합이 100%")
    void getSentimentDetail_distributionSumsTo100() {
        given(videoRepository.findByYoutubeVideoId("demo-video-001")).willReturn(Optional.of(testVideo));
        given(commentRepository.countByVideoIdAndAnalyzedTrue(1L)).willReturn(10L);

        // 3 positive, 2 negative, 2 neutral, 2 request, 1 question = 10 total
        given(commentRepository.countByVideoIdAndSentiment(1L, Sentiment.POSITIVE)).willReturn(3L);
        given(commentRepository.countByVideoIdAndSentiment(1L, Sentiment.NEGATIVE)).willReturn(2L);
        given(commentRepository.countByVideoIdAndSentiment(1L, Sentiment.NEUTRAL)).willReturn(2L);
        given(commentRepository.countByVideoIdAndSentiment(1L, Sentiment.REQUEST)).willReturn(2L);
        given(commentRepository.countByVideoIdAndSentiment(1L, Sentiment.QUESTION)).willReturn(1L);

        // Mock empty sample comments
        for (Sentiment s : Sentiment.values()) {
            given(commentRepository.findByVideoIdAndSentiment(eq(1L), eq(s), any(PageRequest.class)))
                    .willReturn(List.of());
        }

        SentimentDetailResponse result = analyticsService.getSentimentDetail("demo-video-001");

        Map<String, Double> dist = result.getDistribution();
        double sum = dist.values().stream().mapToDouble(Double::doubleValue).sum();

        assertThat(sum).isBetween(99.0, 101.0); // allow rounding tolerance
        assertThat(dist.get("positive")).isEqualTo(30.0);
        assertThat(dist.get("negative")).isEqualTo(20.0);
        assertThat(dist.get("neutral")).isEqualTo(20.0);
        assertThat(dist.get("request")).isEqualTo(20.0);
        assertThat(dist.get("question")).isEqualTo(10.0);
    }

    @Test
    @DisplayName("getSentimentDetail - 분석된 댓글 0개일 때 모든 분포 0.0")
    void getSentimentDetail_noAnalyzedComments_allZero() {
        given(videoRepository.findByYoutubeVideoId("demo-video-001")).willReturn(Optional.of(testVideo));
        given(commentRepository.countByVideoIdAndAnalyzedTrue(1L)).willReturn(0L);

        for (Sentiment s : Sentiment.values()) {
            given(commentRepository.countByVideoIdAndSentiment(1L, s)).willReturn(0L);
            given(commentRepository.findByVideoIdAndSentiment(eq(1L), eq(s), any(PageRequest.class)))
                    .willReturn(List.of());
        }

        SentimentDetailResponse result = analyticsService.getSentimentDetail("demo-video-001");

        result.getDistribution().values().forEach(v -> assertThat(v).isEqualTo(0.0));
        assertThat(result.getTotalAnalyzed()).isEqualTo(0);
    }

    @Test
    @DisplayName("getSentimentDetail - 존재하지 않는 비디오는 예외 발생")
    void getSentimentDetail_unknownVideo_throwsException() {
        given(videoRepository.findByYoutubeVideoId("nonexistent")).willReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getSentimentDetail("nonexistent"))
                .isInstanceOf(VideoNotFoundException.class);
    }

    @Test
    @DisplayName("getSentimentDetail - 샘플 댓글 최대 3개 포함")
    void getSentimentDetail_includesSampleComments() {
        given(videoRepository.findByYoutubeVideoId("demo-video-001")).willReturn(Optional.of(testVideo));
        given(commentRepository.countByVideoIdAndAnalyzedTrue(1L)).willReturn(5L);

        given(commentRepository.countByVideoIdAndSentiment(1L, Sentiment.POSITIVE)).willReturn(5L);
        for (Sentiment s : Sentiment.values()) {
            if (s != Sentiment.POSITIVE) {
                given(commentRepository.countByVideoIdAndSentiment(1L, s)).willReturn(0L);
            }
        }

        List<Comment> positiveComments = List.of(
                Comment.builder().authorName("A").content("좋아요!").likeCount(10).build(),
                Comment.builder().authorName("B").content("최고!").likeCount(5).build()
        );
        given(commentRepository.findByVideoIdAndSentiment(eq(1L), eq(Sentiment.POSITIVE), any(PageRequest.class)))
                .willReturn(positiveComments);
        for (Sentiment s : Sentiment.values()) {
            if (s != Sentiment.POSITIVE) {
                given(commentRepository.findByVideoIdAndSentiment(eq(1L), eq(s), any(PageRequest.class)))
                        .willReturn(List.of());
            }
        }

        SentimentDetailResponse result = analyticsService.getSentimentDetail("demo-video-001");

        assertThat(result.getSampleComments()).containsKey("positive");
        assertThat(result.getSampleComments().get("positive")).hasSize(2);
        assertThat(result.getSampleComments().get("positive").get(0).getAuthor()).isEqualTo("A");
    }

    // ========== getOverview ==========

    @Test
    @DisplayName("getOverview - 비디오 없으면 기본값 반환")
    void getOverview_noVideos_returnsDefaults() {
        given(videoRepository.findAll()).willReturn(List.of());
        given(commentRepository.countByAnalyzedTrue()).willReturn(0L);
        given(commentRepository.findByAnalyzedTrue()).willReturn(List.of());

        ChannelOverviewResponse result = analyticsService.getOverview();

        assertThat(result.getTotalComments()).isEqualTo(0);
        assertThat(result.getAvgSentiment()).isEqualTo(0.0);
        assertThat(result.getTopPositiveVideo()).isNull();
        assertThat(result.getTopNegativeVideo()).isNull();
        assertThat(result.getHottestKeywords()).isEmpty();
        assertThat(result.getPendingRequests()).isEmpty();
    }

    // ========== getKeywords ==========

    @Test
    @DisplayName("getKeywords - 키워드 목록 반환 및 sentiment가 null이면 null")
    void getKeywords_returnsList() {
        given(videoRepository.findByYoutubeVideoId("demo-video-001")).willReturn(Optional.of(testVideo));
        given(keywordRepository.findByVideoIdOrderByCountDesc(1L)).willReturn(List.of(
                Keyword.builder().word("하네스").count(5).sentiment(Sentiment.POSITIVE).build(),
                Keyword.builder().word("기타").count(2).sentiment(null).build()
        ));

        List<KeywordResponse> result = analyticsService.getKeywords("demo-video-001", 20);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getWord()).isEqualTo("하네스");
        assertThat(result.get(0).getSentiment()).isEqualTo("POSITIVE");
        assertThat(result.get(1).getSentiment()).isNull();
    }

    // ========== getContentRequests ==========

    @Test
    @DisplayName("getContentRequests - 요청 주제별 그룹화 및 정렬")
    void getContentRequests_groupsAndSorts() {
        given(videoRepository.findByYoutubeVideoId("demo-video-001")).willReturn(Optional.of(testVideo));

        Comment req1 = Comment.builder()
                .id(1L).authorName("A").content("비교 영상 해주세요!").likeCount(10)
                .sentiment(Sentiment.REQUEST).requestTopic("비교 영상")
                .suggestedTitle("비교 영상 관련 영상")
                .video(testVideo).build();
        Comment req2 = Comment.builder()
                .id(2L).authorName("B").content("비교 영상 부탁!").likeCount(5)
                .sentiment(Sentiment.REQUEST).requestTopic("비교 영상")
                .video(testVideo).build();
        Comment req3 = Comment.builder()
                .id(3L).authorName("C").content("심화편 만들어주세요").likeCount(3)
                .sentiment(Sentiment.REQUEST).requestTopic("심화편")
                .suggestedTitle("심화편 관련 영상")
                .video(testVideo).build();

        given(commentRepository.findRequestCommentsWithTopic(1L, Sentiment.REQUEST))
                .willReturn(List.of(req1, req2, req3));

        List<ContentRequestResponse> result = analyticsService.getContentRequests("demo-video-001");

        assertThat(result).hasSize(2);
        // "비교 영상" has 2 comments, should come first
        assertThat(result.get(0).getTopic()).isEqualTo("비교 영상");
        assertThat(result.get(0).getCount()).isEqualTo(2);
        assertThat(result.get(0).getSuggestedTitle()).isEqualTo("비교 영상 관련 영상");
        assertThat(result.get(1).getTopic()).isEqualTo("심화편");
        assertThat(result.get(1).getCount()).isEqualTo(1);
    }

    // ========== getTrend ==========

    @Test
    @DisplayName("getTrend - 날짜별 감정 점수 계산")
    void getTrend_calculatesDailyScores() {
        LocalDate from = LocalDate.of(2026, 4, 10);
        LocalDate to = LocalDate.of(2026, 4, 10);

        List<Comment> comments = List.of(
                Comment.builder().sentiment(Sentiment.POSITIVE)
                        .publishedAt(LocalDateTime.of(2026, 4, 10, 10, 0)).build(),
                Comment.builder().sentiment(Sentiment.POSITIVE)
                        .publishedAt(LocalDateTime.of(2026, 4, 10, 11, 0)).build(),
                Comment.builder().sentiment(Sentiment.NEGATIVE)
                        .publishedAt(LocalDateTime.of(2026, 4, 10, 12, 0)).build()
        );

        given(commentRepository.findAnalyzedCommentsBetween(any(), any())).willReturn(comments);

        List<TrendPointResponse> result = analyticsService.getTrend(null, from, to);

        assertThat(result).hasSize(1);
        // 2 positive out of 3 = 66.7%
        assertThat(result.get(0).getSentimentScore()).isEqualTo(66.7);
        assertThat(result.get(0).getCommentCount()).isEqualTo(3);
        assertThat(result.get(0).getDate()).isEqualTo("2026-04-10");
    }

    @Test
    @DisplayName("getTrend - 댓글 없는 날짜도 0으로 포함")
    void getTrend_includesEmptyDays() {
        LocalDate from = LocalDate.of(2026, 4, 10);
        LocalDate to = LocalDate.of(2026, 4, 12);

        given(commentRepository.findAnalyzedCommentsBetween(any(), any())).willReturn(List.of());

        List<TrendPointResponse> result = analyticsService.getTrend(null, from, to);

        assertThat(result).hasSize(3); // 10, 11, 12
        result.forEach(point -> {
            assertThat(point.getSentimentScore()).isEqualTo(0.0);
            assertThat(point.getCommentCount()).isEqualTo(0);
        });
    }
}
