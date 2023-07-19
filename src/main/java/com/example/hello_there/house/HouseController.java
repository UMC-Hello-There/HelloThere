package com.example.hello_there.house;

import com.example.hello_there.house.dto.GetHouseRes;
import com.example.hello_there.house.dto.GetHouseRes;
import com.example.hello_there.house.dto.PatchHouseReq;
import com.example.hello_there.house.dto.PostHouseReq;
import com.example.hello_there.house.vo.GeoPoint;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/houses")
public class HouseController {
    private final HouseService houseService;
    private final UtilService utilService;
    private final JwtService jwtService;


    @Value("${bupyeong.url}")
    private String bupyeongUrl;

    @Value("${dongjak.url}")
    private String dongjakUrl;

    @Value("${gwanak.url}")
    private String gwanakUrl;

    @Value("${songpa.url}")
    private String songpaUrl;

    /** GPS 위치 인증을 위한 근처 아파트 조회 **/
    @GetMapping("/spatial/radius")
    public BaseResponse<List<GetHouseRes>> findByRadius(@RequestParam("lng") Double lng, @RequestParam("lat") Double lat, @RequestParam("radius") Double radius) {
        List<House> houseList = houseService.findByRadius(new GeoPoint(lng, lat), radius);
        List<GetHouseRes> res =
                houseList.stream().map(it-> GetHouseRes.mapEntityToResponse(it)).collect(Collectors.toList());
        return new BaseResponse<>(res);
    }

    /** 아파트 자동등록 **/
    @PostMapping("/auto-register")
    public BaseResponse<String> registerHouse() {
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
            return new BaseResponse<>("인천시 부평구, 서울시 동작구, 서울시 관악구, 서울시 송파구 아파트를 DB에 등록합니다.");
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 유저의 아파트 정보 설정하기
     */
    @PatchMapping("/update")
    public BaseResponse<String> modifyHouseInfo(@RequestBody PatchHouseReq patchHouseReq) {
        // PostMan에서 Headers에 Authorization필드를 추가하고, 로그인할 때 받은 jwt 토큰을 입력해야 실행이 됩니다.
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(houseService.modifyHouse(patchHouseReq, userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저의 아파트 등록하기
     */
    @PostMapping("/register")
    public BaseResponse<String> createHouse(@RequestBody PostHouseReq postHouseReq) {
        House house = houseService.createHouse(postHouseReq);
        String city = house.getCity();
        String district = house.getDistrict();
        String houseName = house.getName();
        return new BaseResponse<>(city + " " + district + houseName + " 등록이 완료되었습니다.");
    }
}
