package com.example.hello_there.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostInquiryReq {
    private String phoneNumber;
    private String content;
}
