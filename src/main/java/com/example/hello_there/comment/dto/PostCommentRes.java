package com.example.hello_there.comment.dto;

import com.example.hello_there.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostCommentRes {
    private final Long commentId;    // 댓글 고유 PK
    private final Long parentId;     // 부모 댓글 PK
    private final Long groupId;      // 댓글 그룹
    private final String content;    // 댓글 내용
    private final String nickName;   // 회원 닉네임
    private final Integer likeCount; // 전체 좋아요 수
    private final LocalDateTime createdDate;

    public PostCommentRes(Comment comment){
        this.commentId = comment.getCommentId();
        this.parentId = comment.getParent()
                == null ? null
                : comment.getParent().getCommentId();
        this.groupId = comment.getGroupId();
        this.content = comment.getContent();
        this.nickName = comment.getUser().getNickName();
        this.likeCount = comment.getLikeCount();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdDate = comment.getCreateDate().format(formatter);
    }
}
