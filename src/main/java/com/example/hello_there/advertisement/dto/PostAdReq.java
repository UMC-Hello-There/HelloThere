package com.example.hello_there.advertisement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostAdReq {
    String phoneNumber;
//  ex)  "2024-12-15T10:11:22"
    LocalDateTime expireDate;
    String district;
}
