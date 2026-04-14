package com.dingco.pulse.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSummaryResponse {

    private String videoId;
    private String title;
    private LocalDateTime publishedAt;
    private int commentCount;
    private double sentimentScore;
    private List<String> topKeywords;
}
