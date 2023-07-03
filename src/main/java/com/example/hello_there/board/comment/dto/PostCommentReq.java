package com.example.hello_there.board.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentReq {
    private Long boardId;

    @NotBlank(message = "댓글을 입력하세요.")
    @Size(min=1, max=100, message = "댓글의 길이는 1~50글자까지 입력 가능합니다.")
    private String reply;
}
