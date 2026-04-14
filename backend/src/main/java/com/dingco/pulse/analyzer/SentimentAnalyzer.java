package com.dingco.pulse.analyzer;

import com.dingco.pulse.domain.Comment;

import java.util.List;

public interface SentimentAnalyzer {

    List<AnalysisResult> analyze(List<Comment> comments);
}
