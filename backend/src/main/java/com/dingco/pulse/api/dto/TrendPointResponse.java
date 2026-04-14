package com.dingco.pulse.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointResponse {

    private String date;
    private double sentimentScore;
    private long commentCount;
}
