package com.example.hello_there.board.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId")
    List<Comment> findCommentsByUserId(@Param("userId") Long userId);

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