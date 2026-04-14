package com.dingco.pulse.collector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeCommentDto {

    private String commentId;
    private String authorName;
    private String content;
    private int likeCount;
    private LocalDateTime publishedAt;
}
