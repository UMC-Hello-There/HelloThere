package com.example.hello_there.comment.dto;

import com.example.hello_there.board.Board;
import com.example.hello_there.comment.Comment;
import com.example.hello_there.user.User;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Validated
public class PostCommentReq {
    // TODO parentId 를 PathVariable 로 굳이 받아야하나
    private Long parentId;

    @NotBlank(message = "댓글을 입력하세요.")
    @Size(min=1, max=100, message = "댓글의 길이는 1~50글자까지 입력 가능합니다.")
    private String content;

    // DTO - > Entity
    public Comment toEntity(PostCommentReq dto, Board board, User user) {
        return Comment.builder()
                .board(board)
                .user(user)
                .content(dto.getContent())
                .build();
    }

}
