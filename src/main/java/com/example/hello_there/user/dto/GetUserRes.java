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
    private String email;
    private String nickName;
    private String signupPurpose;
    private String address;
    private UserStatus status;

    public GetUserRes(User user) {
        this.userId = user.getId();
        this.getS3Res = (user.getProfile() != null)
                ? new GetS3Res(user.getProfile().getProfileUrl(), user.getProfile().getProfileFileName())
                : null;
        this.nickName = user.getNickName();
        this.email = user.getEmail();
        this.address = (user.getHouse() != null)
                ? String.format("%s %s %s", user.getHouse().getCity(), user.getHouse().getDistrict(),
                user.getHouse().getHouseName())
                : "";
        this.status = user.getStatus();
    }
}