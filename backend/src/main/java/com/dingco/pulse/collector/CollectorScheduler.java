package com.dingco.pulse.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pulse.collector.enabled", havingValue = "true")
public class CollectorScheduler {

    private final CommentCollectorService commentCollectorService;

    @Scheduled(fixedDelay = 3600000)
    public void collectComments() {
        log.info("Starting scheduled comment collection...");
        commentCollectorService.collectAllVideos();
        log.info("Scheduled comment collection completed.");
    }
}
