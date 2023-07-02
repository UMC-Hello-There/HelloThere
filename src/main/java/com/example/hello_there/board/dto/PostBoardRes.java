package com.example.hello_there.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostBoardRes {
    private String nickName;
    private String title;
    private String content;
}
