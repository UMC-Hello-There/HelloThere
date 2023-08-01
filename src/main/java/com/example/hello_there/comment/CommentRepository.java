package com.example.hello_there.comment;

import com.example.hello_there.board.Board;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 groupId 최대값 조회 null 이면 0L 반환
    @Query("SELECT coalesce(max(c.groupId),0) from Comment " +
            "c WHERE c.board.boardId = :boardId")
    Long findGroupIdByBoardId(@Param("boardId") Long boardId);

    // 댓글+대댓글 전체 조회
    @Query("SELECT distinct c FROM Comment c " +
            " JOIN FETCH c.user u " +
            " LEFT JOIN FETCH c.likeComments cl" +
            " LEFT JOIN FETCH c.parent p" +
            " WHERE c.board.boardId = :boardId" +
            " ORDER BY c.groupId asc, c.createDate asc")
    List<Comment> findCommentsByBoardIdForList(@Param("boardId") Long boardId);

    // 자식 댓글이 있는지 확인
    boolean existsByParentCommentId(Long commentId);

    @Query("SELECT c FROM Comment c WHERE c.board.boardId = :boardId")
    List<Comment> findCommentsByBoardId(@Param("boardId") Long boardId);

    // 특정 게시글의 댓글수 카운트
    Long countByBoardBoardId(Long boardId);

    @Modifying
    @Query("delete from Comment c where c.board.boardId = :boardId")
    void deleteCommentsByBoardId(@Param("boardId") Long boardId);

    //== For MyPage ==//
    @Query("select distinct c.board from Comment c where c.user.id = :userId")
    List<Board> findCommentedBoardsByUserId(@Param("userId") Long userId);
}