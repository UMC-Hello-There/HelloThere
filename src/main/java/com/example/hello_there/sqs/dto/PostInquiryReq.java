package com.example.hello_there.sqs.dto;

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
