package com.dingco.pulse.api.controller;

import com.dingco.pulse.api.dto.ChannelOverviewResponse;
import com.dingco.pulse.api.dto.TrendPointResponse;
import com.dingco.pulse.api.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("GET /api/analytics/overview - 채널 전체 개요 반환")
    void getOverview_returnsChannelOverview() throws Exception {
        ChannelOverviewResponse overview = ChannelOverviewResponse.builder()
                .totalComments(15)
                .avgSentiment(53.3)
                .topPositiveVideo(ChannelOverviewResponse.VideoScoreSummary.builder()
                        .videoId("demo-video-001")
                        .title("하네스 엔지니어링이 뭔데?")
                        .sentimentScore(60.0)
                        .commentCount(5)
                        .build())
                .topNegativeVideo(ChannelOverviewResponse.VideoScoreSummary.builder()
                        .videoId("demo-video-002")
                        .title("AI 시대 개발자 생존전략")
                        .sentimentScore(25.0)
                        .commentCount(4)
                        .build())
                .hottestKeywords(List.of("하네스", "Claude Code"))
                .pendingRequests(List.of(
                        ChannelOverviewResponse.PendingRequest.builder()
                                .topic("Cursor vs Claude Code 비교")
                                .count(1)
                                .suggestedTitle("비교 영상")
                                .build()
                ))
                .build();

        given(analyticsService.getOverview()).willReturn(overview);

        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalComments").value(15))
                .andExpect(jsonPath("$.avgSentiment").value(53.3))
                .andExpect(jsonPath("$.topPositiveVideo.videoId").value("demo-video-001"))
                .andExpect(jsonPath("$.topPositiveVideo.sentimentScore").value(60.0))
                .andExpect(jsonPath("$.topNegativeVideo.videoId").value("demo-video-002"))
                .andExpect(jsonPath("$.hottestKeywords", hasSize(2)))
                .andExpect(jsonPath("$.pendingRequests", hasSize(1)))
                .andExpect(jsonPath("$.pendingRequests[0].topic").value("Cursor vs Claude Code 비교"));
    }

    @Test
    @DisplayName("GET /api/analytics/overview - 비디오 없을 때 null 필드 반환")
    void getOverview_noVideos_returnsNullFields() throws Exception {
        ChannelOverviewResponse overview = ChannelOverviewResponse.builder()
                .totalComments(0)
                .avgSentiment(0.0)
                .topPositiveVideo(null)
                .topNegativeVideo(null)
                .hottestKeywords(List.of())
                .pendingRequests(List.of())
                .build();

        given(analyticsService.getOverview()).willReturn(overview);

        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalComments").value(0))
                .andExpect(jsonPath("$.topPositiveVideo").doesNotExist())
                .andExpect(jsonPath("$.topNegativeVideo").doesNotExist())
                .andExpect(jsonPath("$.hottestKeywords", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/analytics/trend - 기본 30일 트렌드 반환")
    void getTrend_returnsDefaultTrend() throws Exception {
        List<TrendPointResponse> trend = List.of(
                TrendPointResponse.builder()
                        .date("2026-04-01")
                        .sentimentScore(71.2)
                        .commentCount(3)
                        .build(),
                TrendPointResponse.builder()
                        .date("2026-04-02")
                        .sentimentScore(75.8)
                        .commentCount(5)
                        .build()
        );

        given(analyticsService.getTrend(isNull(), isNull(), isNull())).willReturn(trend);

        mockMvc.perform(get("/api/analytics/trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].date").value("2026-04-01"))
                .andExpect(jsonPath("$[0].sentimentScore").value(71.2))
                .andExpect(jsonPath("$[0].commentCount").value(3));
    }

    @Test
    @DisplayName("GET /api/analytics/trend?videoId=X - 특정 비디오 트렌드 필터")
    void getTrend_withVideoId_filtersCorrectly() throws Exception {
        List<TrendPointResponse> trend = List.of(
                TrendPointResponse.builder()
                        .date("2026-04-10")
                        .sentimentScore(60.0)
                        .commentCount(5)
                        .build()
        );

        given(analyticsService.getTrend(eq("demo-video-001"), isNull(), isNull())).willReturn(trend);

        mockMvc.perform(get("/api/analytics/trend")
                        .param("videoId", "demo-video-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date").value("2026-04-10"));
    }

    @Test
    @DisplayName("GET /api/analytics/trend?from=X&to=Y - 날짜 범위 트렌드")
    void getTrend_withDateRange_filtersCorrectly() throws Exception {
        List<TrendPointResponse> trend = List.of(
                TrendPointResponse.builder()
                        .date("2026-04-01")
                        .sentimentScore(50.0)
                        .commentCount(2)
                        .build()
        );

        given(analyticsService.getTrend(isNull(),
                eq(LocalDate.of(2026, 4, 1)),
                eq(LocalDate.of(2026, 4, 7)))).willReturn(trend);

        mockMvc.perform(get("/api/analytics/trend")
                        .param("from", "2026-04-01")
                        .param("to", "2026-04-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
