package com.dingco.pulse.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentDetailResponse {

    private String videoId;
    private String title;
    private long totalAnalyzed;
    private Map<String, Double> distribution;
    private Map<String, List<SampleComment>> sampleComments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SampleComment {
        private String author;
        private String content;
        private int likeCount;
    }
}
