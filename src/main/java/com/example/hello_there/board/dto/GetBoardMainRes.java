package com.example.hello_there.board.dto;

import com.example.hello_there.advertisement.dto.GetAdRes;
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
public class GetBoardMainRes {
    private Long boardId;
    private BoardType boardType;
    private String title;
    private GetS3Res getS3Res;
}
