package com.example.hello_there.report;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reporter.id = :reporterId " +
            "AND r.reported.id = :reportedId AND r.boardId = :boardId " +
            "AND r.commentId = :commentId AND r.messageId = :messageId")
    Integer findMatchingReportsCount(@Param("reporterId") Long reporterId,
                                     @Param("reportedId") Long reportedId,
                                     @Param("boardId") Long boardId,
                                     @Param("commentId") Long commentId,
                                     @Param("messageId") Long messageId);
    @Query("SELECT r.reason FROM Report r WHERE r.reported.id = :reportedId " +
            "AND r.boardId = :boardId " + "AND r.commentId = :commentId" +
            " AND r.messageId = :messageId")
    List<String> findMatchingReportReasons(@Param("reportedId") Long reportedId,
                                           @Param("boardId") Long boardId,
                                           @Param("commentId") Long commentId,
                                           @Param("messageId") Long messageId);
}
