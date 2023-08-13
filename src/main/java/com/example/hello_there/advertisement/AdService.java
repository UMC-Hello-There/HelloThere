package com.example.hello_there.advertisement;

import com.example.hello_there.advertisement.dto.GetAdRes;
import com.example.hello_there.advertisement.dto.PostAdReq;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.utils.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdService {
    private final AdRepository adRepository;
    private final S3Service s3Service;

    //광고 생성
    @Transactional
    public String saveAd(MultipartFile adImg,PostAdReq postAdReq)throws BaseException {
        GetS3Res getS3Res = s3Service.uploadSingleFile(adImg);
        adRepository.save(Ad.builder()
                .adImgUrl(getS3Res.getImgUrl())
                .adImgName(getS3Res.getFileName())
                .phoneNumber(postAdReq.getPhoneNumber())
                .expireDate(postAdReq.getExpireDate())
                .district(postAdReq.getDistrict())
                .build());
        return getS3Res.getImgUrl();
    }

    // 광고 표출
    @Transactional
    public GetAdRes findAd(String district){
        Ad ad = adRepository.findAdByRandom(district);
        // 계약된 광고가 없으면 null 반환
        if(ad == null){
            return null;
        }
        // 계약기간이 종료된 광고는 삭제
        while(ad.getExpireDate().isBefore(LocalDateTime.now())){
            s3Service.deleteFile(ad.getAdImgName());
            adRepository.delete(ad);
            ad = adRepository.findAdByRandom(district);
        }
        return GetAdRes.builder()
                .adImgUrl(ad.getAdImgUrl())
                .adImgName(ad.getAdImgName())
                .phoneNumber(ad.getPhoneNumber())
                .build();
    }
}

