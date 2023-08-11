package com.example.hello_there.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GetAdRes {
    private String adImgUrl;

    private String adImgName;

    private String phoneNumber;
}
