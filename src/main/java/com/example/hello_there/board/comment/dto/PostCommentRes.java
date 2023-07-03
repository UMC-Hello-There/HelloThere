package com.example.hello_there.board.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostCommentRes {
    private String nickName;
    private String reply;
}
