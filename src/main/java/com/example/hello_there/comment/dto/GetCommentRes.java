package com.example.hello_there.comment.dto;

import com.example.hello_there.board.BoardType;
import com.example.hello_there.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetCommentRes {
    private final Long commentId;    // 댓글 고유 PK
    private final Long parentId;     // 부모 댓글 PK
    private final Long groupId;      // 댓글 그룹
    private final String content;    // 댓글 내용
    private final String nickName;   // 회원 닉네임
    private final Integer likeCount; // 전체 좋아요 수
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;

    public GetCommentRes(Comment comment){
        this.commentId = comment.getCommentId();
        this.parentId = comment.getParent()
                == null ? null
                : comment.getParent().getCommentId();
        this.groupId = comment.getGroupId();
        this.content = comment.getContent();
        this.nickName = comment.getUser().getNickName();
        this.likeCount = comment.getLikeCount();
        this.createdDate = comment.getCreateDate();
        this.modifiedDate = comment.getModifiedDate();
    }
}
