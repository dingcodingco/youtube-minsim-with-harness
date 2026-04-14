package com.dingco.pulse.analyzer;

import com.dingco.pulse.domain.Comment;
import com.dingco.pulse.domain.Keyword;
import com.dingco.pulse.domain.KeywordRepository;
import com.dingco.pulse.domain.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordExtractor {

    private final KeywordRepository keywordRepository;

    @Transactional
    public void extractAndSave(List<AnalysisResult> results, Map<Long, Comment> commentMap) {
        for (AnalysisResult result : results) {
            Comment comment = commentMap.get(result.getCommentId());
            if (comment == null || result.getKeywords() == null) {
                continue;
            }

            Video video = comment.getVideo();

            for (String word : result.getKeywords()) {
                if (word == null || word.isBlank()) {
                    continue;
                }

                Optional<Keyword> existing = keywordRepository.findByVideoIdAndWord(video.getId(), word);

                if (existing.isPresent()) {
                    Keyword keyword = existing.get();
                    keyword.setCount(keyword.getCount() + 1);
                    // Update sentiment if the new result has a more specific one
                    if (result.getSentiment() != null) {
                        keyword.setSentiment(result.getSentiment());
                    }
                    keywordRepository.save(keyword);
                } else {
                    Keyword keyword = Keyword.builder()
                            .video(video)
                            .word(word)
                            .count(1)
                            .sentiment(result.getSentiment())
                            .build();
                    keywordRepository.save(keyword);
                }
            }
        }

        log.info("Extracted keywords from {} analysis results", results.size());
    }
}
