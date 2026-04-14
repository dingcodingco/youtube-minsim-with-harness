package com.dingco.pulse.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelOverviewResponse {

    private long totalComments;
    private double avgSentiment;
    private VideoScoreSummary topPositiveVideo;
    private VideoScoreSummary topNegativeVideo;
    private List<String> hottestKeywords;
    private List<PendingRequest> pendingRequests;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoScoreSummary {
        private String videoId;
        private String title;
        private double sentimentScore;
        private int commentCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingRequest {
        private String topic;
        private int count;
        private String suggestedTitle;
    }
}
