package com.example.hello_there.login.google.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetGoogleUserRes {
    private String email;
    private String nickName;
}
