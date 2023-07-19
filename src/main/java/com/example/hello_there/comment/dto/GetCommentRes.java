package com.example.hello_there.comment.dto;

import com.example.hello_there.board.BoardType;
import com.example.hello_there.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetCommentRes {
    private Long commentId;    // 댓글 고유 PK
    private Long parentId;     // 부모 댓글 PK
    private Long groupId;      // 댓글 그룹
    private String content;    // 댓글 내용
    private String nickName;   // 회원 닉네임
    private Integer likeCount; // 전체 좋아요 수
    private String createdDate;
    private String modifiedDate;
}
