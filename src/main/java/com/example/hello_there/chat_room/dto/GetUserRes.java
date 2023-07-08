package com.example.hello_there.chat_room.dto;

import com.example.hello_there.board.photo.dto.GetS3Res;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor
public class GetUserRes {
    private Long userId;
    private String nickName;
}
