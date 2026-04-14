package com.dingco.pulse.collector;

import com.dingco.pulse.collector.dto.YouTubeCommentDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class YouTubeApiClient {

    private static final String COMMENT_THREADS_URL =
            "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&videoId={videoId}&maxResults={maxResults}&order=time&key={apiKey}";

    private static final String COMMENT_THREADS_URL_WITH_PAGE =
            "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&videoId={videoId}&maxResults={maxResults}&order=time&key={apiKey}&pageToken={pageToken}";

    @Value("${youtube.api-key}")
    private String apiKey;

    @Value("${youtube.max-results-per-page:100}")
    private int maxResultsPerPage;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public YouTubeApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<YouTubeCommentDto> fetchComments(String videoId) {
        if ("demo".equals(apiKey)) {
            log.info("Demo mode: skipping YouTube API call for videoId={}", videoId);
            return Collections.emptyList();
        }

        List<YouTubeCommentDto> allComments = new ArrayList<>();
        String pageToken = null;

        try {
            do {
                String responseBody;
                if (pageToken == null) {
                    responseBody = restTemplate.getForObject(
                            COMMENT_THREADS_URL, String.class,
                            videoId, maxResultsPerPage, apiKey);
                } else {
                    responseBody = restTemplate.getForObject(
                            COMMENT_THREADS_URL_WITH_PAGE, String.class,
                            videoId, maxResultsPerPage, apiKey, pageToken);
                }

                if (responseBody == null) {
                    break;
                }

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode items = root.get("items");

                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        JsonNode snippet = item.path("snippet").path("topLevelComment").path("snippet");

                        YouTubeCommentDto dto = YouTubeCommentDto.builder()
                                .commentId(item.path("snippet").path("topLevelComment").path("id").asText())
                                .authorName(snippet.path("authorDisplayName").asText("Unknown"))
                                .content(snippet.path("textOriginal").asText(""))
                                .likeCount(snippet.path("likeCount").asInt(0))
                                .publishedAt(parseDateTime(snippet.path("publishedAt").asText()))
                                .build();

                        allComments.add(dto);
                    }
                }

                pageToken = root.has("nextPageToken") ? root.get("nextPageToken").asText() : null;

            } while (pageToken != null);

        } catch (RestClientException e) {
            log.error("YouTube API call failed for videoId={}: {}", videoId, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to parse YouTube API response for videoId={}: {}", videoId, e.getMessage());
        }

        log.info("Fetched {} comments for videoId={}", allComments.size(), videoId);
        return allComments;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}, using now()", dateTimeStr);
            return LocalDateTime.now();
        }
    }
}
