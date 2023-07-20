package com.example.hello_there.comment.dto;

import com.example.hello_there.comment.Comment;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatchCommentRes {
    private Long commentId;    // 댓글 고유 PK
    private Long parentId;     // 부모 댓글 PK
    private Long groupId;      // 댓글 그룹
    private String content;    // 댓글 내용
    private String nickName;   // 회원 닉네임
    private Integer likeCount;
    private String modifiedDate;

    public PatchCommentRes(Comment comment){
        this.commentId = comment.getCommentId();
        this.parentId = comment.getParent()
                == null ? null
                : comment.getParent().getCommentId();
        this.groupId = comment.getGroupId();
        this.content = comment.getContent();
        this.nickName = comment.getUser().getNickName();
        this.likeCount = comment.getLikeComments().size();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.modifiedDate = comment.getModifiedDate().format(formatter);
    }
}

