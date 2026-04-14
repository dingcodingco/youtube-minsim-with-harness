package com.dingco.pulse.analyzer;

import com.dingco.pulse.domain.Sentiment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    private Long commentId;
    private Sentiment sentiment;
    private double confidence;
    private List<String> keywords;
    private String requestTopic;
    private String suggestedTitle;
}
