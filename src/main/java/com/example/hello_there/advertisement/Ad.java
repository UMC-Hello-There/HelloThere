package com.example.hello_there.advertisement;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adId;

    private String adImgUrl;

    private String adImgName;

    private String district;

    // 광고주 연락처
    private String phoneNumber;

    private LocalDateTime expireDate;

    @Builder
    public Ad(String adImgUrl, String adImgName, String district, String phoneNumber, LocalDateTime expireDate) {
        this.adImgUrl = adImgUrl;
        this.adImgName = adImgName;
        this.district = district;
        this.phoneNumber = phoneNumber;
        this.expireDate = expireDate;
    }


}
