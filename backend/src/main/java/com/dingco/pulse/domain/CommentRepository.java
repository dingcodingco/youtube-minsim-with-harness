package com.dingco.pulse.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByVideoId(Long videoId);

    List<Comment> findByAnalyzedFalse();

    List<Comment> findByAnalyzedFalse(Pageable pageable);

    boolean existsByYoutubeCommentId(String youtubeCommentId);

    long countByVideoId(Long videoId);

    long countByVideoIdAndAnalyzedTrue(Long videoId);

    long countByVideoIdAndSentiment(Long videoId, Sentiment sentiment);

    long countByAnalyzedTrue();

    long countBySentiment(Sentiment sentiment);

    List<Comment> findByVideoIdAndSentiment(Long videoId, Sentiment sentiment, Pageable pageable);

    List<Comment> findByVideoIdAndAnalyzedTrue(Long videoId);

    List<Comment> findByAnalyzedTrue();

    @Query("SELECT c FROM Comment c WHERE c.video.id = :videoId AND c.sentiment = :sentiment AND c.requestTopic IS NOT NULL")
    List<Comment> findRequestCommentsWithTopic(@Param("videoId") Long videoId, @Param("sentiment") Sentiment sentiment);

    @Query("SELECT c FROM Comment c WHERE c.analyzed = true AND c.publishedAt BETWEEN :from AND :to")
    List<Comment> findAnalyzedCommentsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT c FROM Comment c WHERE c.video.id = :videoId AND c.analyzed = true AND c.publishedAt BETWEEN :from AND :to")
    List<Comment> findAnalyzedCommentsByVideoAndBetween(@Param("videoId") Long videoId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
