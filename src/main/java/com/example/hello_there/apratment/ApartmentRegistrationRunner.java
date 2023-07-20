package com.example.hello_there.apratment;

import com.example.hello_there.apratment.ApartmentService;
import com.example.hello_there.apratment.dto.PostApartmentReq;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApartmentRegistrationRunner implements ApplicationRunner {

    private final ApartmentService apartmentService;

    /** 아파트 자동등록 **/
    @Override
    public void run(ApplicationArguments args) {
        try {
            // 인천시 부평구 아파트
            List<PostApartmentReq> postApartmentReqList = apartmentService.getApartmentInfoBupyeong();

            // 서울시 동작구 아파트
            postApartmentReqList.addAll(apartmentService.getApartmentInfoDongjak());

            // 서울시 관악구 아파트
            postApartmentReqList.addAll(apartmentService.getApartmentInfoGwanak());

            // 서울시 송파구 아파트
            postApartmentReqList.addAll(apartmentService.getApartmentInfoSongpa());

            for (PostApartmentReq postApartmentReq : postApartmentReqList) {
                apartmentService.createApartment(postApartmentReq); // 아파트 자동 등록
            }
        } catch (IllegalCharsetNameException | UnsupportedCharsetException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}