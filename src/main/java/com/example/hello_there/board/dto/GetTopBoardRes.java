package com.example.hello_there.board.dto;

import com.example.hello_there.board.BoardType;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.comment.dto.GetCommentRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetTopBoardRes {
    private Long boardId;
    private BoardType boardType;
    private String title;
    private Long commentCount; // 댓글 수
    private Long likeCount; // 좋아요 수
}