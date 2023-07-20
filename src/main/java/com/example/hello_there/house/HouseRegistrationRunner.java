package com.example.hello_there.house;

import com.example.hello_there.house.dto.PostHouseReq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HouseRegistrationRunner implements ApplicationRunner {

    @Value("${bupyeong.url}")
    private String bupyeongUrl;

    @Value("${dongjak.url}")
    private String dongjakUrl;

    @Value("${gwanak.url}")
    private String gwanakUrl;

    @Value("${songpa.url}")
    private String songpaUrl;

    private final HouseService houseService;

    /** 아파트 자동 등록 **/
    @Override
    public void run(ApplicationArguments args) {
        try {
            // 인천시 부평구 아파트
            List<PostHouseReq> postHouseReqList = new ArrayList<>();

            // 인천광역시 부평구 정보
            postHouseReqList.addAll(houseService.getHouseInfoByRegion("인천광역시", "부평구", bupyeongUrl));
            // 서울특별시 동작구 정보
            postHouseReqList.addAll(houseService.getHouseInfoByRegion("서울특별시", "동작구", dongjakUrl));
            // 서울특별시 관악구 정보
            postHouseReqList.addAll(houseService.getHouseInfoByRegion("서울특별시", "관악구", gwanakUrl));
            // 서울특별시 송파구 정보
            postHouseReqList.addAll(houseService.getHouseInfoByRegion("서울특별시", "송파구", songpaUrl));

            for(PostHouseReq postHouseReq : postHouseReqList) {
                houseService.createHouse(postHouseReq); // 아파트 자동 등록
            }
        } catch (IllegalCharsetNameException | UnsupportedCharsetException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}