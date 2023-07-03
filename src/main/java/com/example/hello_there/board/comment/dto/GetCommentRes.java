package com.example.hello_there.board.comment.dto;

import com.example.hello_there.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetCommentRes {
    private BoardType boardType;
    private String boardTitle;
    private String nickName;
    private List<String> reply;
}
