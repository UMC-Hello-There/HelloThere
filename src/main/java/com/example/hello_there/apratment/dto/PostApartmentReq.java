package com.example.hello_there.apratment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor
@NoArgsConstructor
public class PostApartmentReq {
    private String city;
    private String district;
    private String streetAddress;
    private String numberAddress;
    private String apartmentName;
}
