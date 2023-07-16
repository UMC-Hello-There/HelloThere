package com.example.hello_there.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 groupId 최대값 조회 null 이면 0L 반환
    @Query("SELECT coalesce(max(c.groupId),0) from Comment " +
            "c WHERE c.board.boardId = :boardId")
    Long findGroupIdByBoardId(@Param("boardId") Long boardId);

    // 댓글+대댓글 전체 조회 (페이징처리), count 쿼리 분리
    @Query( value = "SELECT c FROM Comment c " +
            " join fetch c.user u " +
            " left join fetch c.parent p" +
            " WHERE c.board.boardId = :boardId" +
            " ORDER BY c.groupId asc, c.createDate asc",
            countQuery = "SELECT count(c.commentId) FROM Comment c")
    List<Comment> findCommentsByBoardIdForList(@Param("boardId") Long boardId);

    @Query("SELECT c FROM Comment c WHERE c.board.boardId = :boardId")
    List<Comment> findCommentsByBoardId(@Param("boardId") Long boardId);

    @Query("SElECT c FROM Comment c WHERE c.commentId = :commentId")
    Optional<Comment> findCommentById(@Param("commentId") Long commentId);

    @Modifying
    @Query("delete from Comment c where c.board.boardId = :boardId")
    void deleteCommentsByBoardId(@Param("boardId") Long boardId);

    @Modifying
    @Query("delete from Comment c where c.commentId = :commentId")
    void deleteCommentByCommentId(@Param("commentId") Long commentId);
}