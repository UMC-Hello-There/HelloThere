package com.example.hello_there.house.dto;

import com.example.hello_there.house.vo.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // 해당 클래스에 대한 접근자 생성
@AllArgsConstructor
@NoArgsConstructor
public class PostHouseReq {
    private String city;
    private String district;
    private String streetAddress;
    private String numberAddress;
    private String houseName;
    private GeoPoint location;
}
