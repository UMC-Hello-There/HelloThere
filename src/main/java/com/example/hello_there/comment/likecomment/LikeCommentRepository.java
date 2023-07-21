package com.example.hello_there.comment.likecomment;

import com.example.hello_there.comment.Comment;
import com.example.hello_there.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeCommentRepository extends JpaRepository<LikeComment, Long> {
    Boolean existsByUserAndComment(User user, Comment comment);

    @Modifying
    Long deleteByUserAndComment(User user, Comment comment);

    @Query("select lc.comment.commentId from LikeComment lc where lc.user.id = :userId and lc.comment.board.boardId = :boardId")
    List<Long> findByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);
}
