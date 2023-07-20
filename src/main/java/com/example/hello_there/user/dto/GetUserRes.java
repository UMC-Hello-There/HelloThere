package com.example.hello_there.user.dto;

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
    private String email;
    private String nickName;
    private String signupPurpose;
    private String address;
    private UserStatus status;

    public GetUserRes(User user){
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickName = user.getNickName();
        this.signupPurpose = user.getSignupPurpose();
        if (user.getHouse() != null) {
            this.address = user.getHouse().getCity() + " " +
                    user.getHouse().getDistrict() + " " +
                    user.getHouse().getHouseName();
        } else {
            this.address = "";
        }
        this.status = user.getStatus();
    }
}