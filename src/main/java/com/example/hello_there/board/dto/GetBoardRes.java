package com.example.hello_there.board.dto;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetBoardRes {
    private Long boardId;
    private BoardType boardType;
    private String createDate; // ex) 2023-07-04
    private String createTime; // ex) 3분 전
    private String nickName;
    private String title;
    private String content;
    private Long view;
    private Long CommentCount; // 댓글 수
}
