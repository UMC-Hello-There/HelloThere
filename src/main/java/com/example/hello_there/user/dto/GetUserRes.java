package com.example.hello_there.user.dto;

import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.user.User;

import com.example.hello_there.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetUserRes {
    private Long userId;
    private GetS3Res getS3Res; // 유저 프로필 사진
    private String nickName;

    public GetUserRes(User user) {
        this.userId = user.getId();
        this.getS3Res = (user.getProfile() != null)
                ? new GetS3Res(user.getProfile().getProfileUrl(), user.getProfile().getProfileFileName())
                : null;
        this.nickName = user.getNickName();
    }
}