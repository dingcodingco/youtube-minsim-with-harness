package com.dingco.pulse.api.controller;

import com.dingco.pulse.api.dto.*;
import com.dingco.pulse.api.exception.VideoNotFoundException;
import com.dingco.pulse.api.service.AnalyticsService;
import com.dingco.pulse.api.service.VideoQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoQueryService videoQueryService;

    @MockBean
    private AnalyticsService analyticsService;

    // ========== GET /api/videos ==========

    @Test
    @DisplayName("GET /api/videos - 영상 목록 반환")
    void getVideos_returnsList() throws Exception {
        List<VideoSummaryResponse> videos = List.of(
                VideoSummaryResponse.builder()
                        .videoId("demo-video-001")
                        .title("하네스 엔지니어링이 뭔데?")
                        .publishedAt(LocalDateTime.of(2026, 4, 10, 9, 0))
                        .commentCount(5)
                        .sentimentScore(60.0)
                        .topKeywords(List.of("하네스", "Claude Code"))
                        .build(),
                VideoSummaryResponse.builder()
                        .videoId("demo-video-002")
                        .title("AI 시대 개발자 생존전략")
                        .publishedAt(LocalDateTime.of(2026, 4, 5, 9, 0))
                        .commentCount(4)
                        .sentimentScore(25.0)
                        .topKeywords(List.of("AI", "개발자"))
                        .build()
        );

        given(videoQueryService.getVideoList("latest")).willReturn(videos);

        mockMvc.perform(get("/api/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].videoId").value("demo-video-001"))
                .andExpect(jsonPath("$[0].title").value("하네스 엔지니어링이 뭔데?"))
                .andExpect(jsonPath("$[0].commentCount").value(5))
                .andExpect(jsonPath("$[0].sentimentScore").value(60.0))
                .andExpect(jsonPath("$[0].topKeywords", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/videos?sort=mostComments - 정렬 파라미터 전달")
    void getVideos_withSort_passesToService() throws Exception {
        given(videoQueryService.getVideoList("mostComments")).willReturn(List.of());

        mockMvc.perform(get("/api/videos").param("sort", "mostComments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========== GET /api/videos/{videoId}/sentiment ==========

    @Test
    @DisplayName("GET /api/videos/{videoId}/sentiment - 감정 분포 반환")
    void getSentiment_returnsSentimentDetail() throws Exception {
        Map<String, Double> distribution = new LinkedHashMap<>();
        distribution.put("positive", 40.0);
        distribution.put("negative", 20.0);
        distribution.put("neutral", 20.0);
        distribution.put("request", 10.0);
        distribution.put("question", 10.0);

        Map<String, List<SentimentDetailResponse.SampleComment>> sampleComments = new LinkedHashMap<>();
        sampleComments.put("positive", List.of(
                SentimentDetailResponse.SampleComment.builder()
                        .author("개발새발")
                        .content("이런 영상 정말 감사합니다!")
                        .likeCount(23)
                        .build()
        ));

        SentimentDetailResponse detail = SentimentDetailResponse.builder()
                .videoId("demo-video-001")
                .title("하네스 엔지니어링이 뭔데?")
                .totalAnalyzed(5)
                .distribution(distribution)
                .sampleComments(sampleComments)
                .build();

        given(analyticsService.getSentimentDetail("demo-video-001")).willReturn(detail);

        mockMvc.perform(get("/api/videos/demo-video-001/sentiment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoId").value("demo-video-001"))
                .andExpect(jsonPath("$.title").value("하네스 엔지니어링이 뭔데?"))
                .andExpect(jsonPath("$.totalAnalyzed").value(5))
                .andExpect(jsonPath("$.distribution.positive").value(40.0))
                .andExpect(jsonPath("$.distribution.negative").value(20.0))
                .andExpect(jsonPath("$.sampleComments.positive", hasSize(1)))
                .andExpect(jsonPath("$.sampleComments.positive[0].author").value("개발새발"));
    }

    @Test
    @DisplayName("GET /api/videos/{videoId}/sentiment - 존재하지 않는 비디오 404")
    void getSentiment_notFound_returns404() throws Exception {
        given(analyticsService.getSentimentDetail("nonexistent"))
                .willThrow(new VideoNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/videos/nonexistent/sentiment"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VIDEO_NOT_FOUND"));
    }

    // ========== GET /api/videos/{videoId}/keywords ==========

    @Test
    @DisplayName("GET /api/videos/{videoId}/keywords - 키워드 목록 반환")
    void getKeywords_returnsList() throws Exception {
        List<KeywordResponse> keywords = List.of(
                KeywordResponse.builder().word("하네스").count(5).sentiment("POSITIVE").build(),
                KeywordResponse.builder().word("프롬프트").count(3).sentiment("NEUTRAL").build()
        );

        given(analyticsService.getKeywords("demo-video-001", 20)).willReturn(keywords);

        mockMvc.perform(get("/api/videos/demo-video-001/keywords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].word").value("하네스"))
                .andExpect(jsonPath("$[0].count").value(5))
                .andExpect(jsonPath("$[0].sentiment").value("POSITIVE"));
    }

    @Test
    @DisplayName("GET /api/videos/{videoId}/keywords?limit=5 - limit 파라미터 적용")
    void getKeywords_withLimit_appliesLimit() throws Exception {
        given(analyticsService.getKeywords("demo-video-001", 5)).willReturn(List.of());

        mockMvc.perform(get("/api/videos/demo-video-001/keywords").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/videos/{videoId}/keywords?limit=100 - 범위 초과 시 기본값(20)으로 리셋")
    void getKeywords_invalidLimit_resetToDefault() throws Exception {
        given(analyticsService.getKeywords("demo-video-001", 20)).willReturn(List.of());

        mockMvc.perform(get("/api/videos/demo-video-001/keywords").param("limit", "100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/videos/{videoId}/keywords - 존재하지 않는 비디오 404")
    void getKeywords_notFound_returns404() throws Exception {
        given(analyticsService.getKeywords(eq("nonexistent"), anyInt()))
                .willThrow(new VideoNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/videos/nonexistent/keywords"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VIDEO_NOT_FOUND"));
    }

    // ========== GET /api/videos/{videoId}/requests ==========

    @Test
    @DisplayName("GET /api/videos/{videoId}/requests - 시청자 요청 목록 반환")
    void getRequests_returnsList() throws Exception {
        List<ContentRequestResponse> requests = List.of(
                ContentRequestResponse.builder()
                        .topic("Cursor vs Claude Code 비교")
                        .count(1)
                        .sampleComments(List.of(
                                SentimentDetailResponse.SampleComment.builder()
                                        .author("주니어맨")
                                        .content("Cursor vs Claude Code 비교 영상도 해주세요!")
                                        .likeCount(41)
                                        .build()
                        ))
                        .suggestedTitle("Cursor vs Claude Code 비교 관련 영상")
                        .build()
        );

        given(analyticsService.getContentRequests("demo-video-001")).willReturn(requests);

        mockMvc.perform(get("/api/videos/demo-video-001/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic").value("Cursor vs Claude Code 비교"))
                .andExpect(jsonPath("$[0].count").value(1))
                .andExpect(jsonPath("$[0].sampleComments", hasSize(1)))
                .andExpect(jsonPath("$[0].suggestedTitle").value("Cursor vs Claude Code 비교 관련 영상"));
    }

    @Test
    @DisplayName("GET /api/videos/{videoId}/requests - 존재하지 않는 비디오 404")
    void getRequests_notFound_returns404() throws Exception {
        given(analyticsService.getContentRequests("nonexistent"))
                .willThrow(new VideoNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/videos/nonexistent/requests"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VIDEO_NOT_FOUND"));
    }
}
