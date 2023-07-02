package com.example.hello_there.user.dto;

import com.example.hello_there.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class GetUserRes {
    private Long userId;
    private String email;
    private String nickName;
    private boolean gender;
    private String birth;
    private boolean isManager;
    private UserStatus status;
}