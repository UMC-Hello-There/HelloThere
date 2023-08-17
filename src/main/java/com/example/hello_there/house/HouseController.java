package com.example.hello_there.house;

import com.example.hello_there.house.dto.GetHouseRes;
import com.example.hello_there.house.dto.PatchHouseReq;
import com.example.hello_there.house.dto.PostHouseReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.house.vo.GeoPoint;
import com.example.hello_there.login.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/houses")
public class HouseController {

    private final HouseService houseService;
    private final JwtService jwtService;

    /**
     * 아파트 정보 가져오기
     */
    @GetMapping("")
    public BaseResponse<GetHouseRes> getHouseInfo(@RequestParam Long houseId) {
        GetHouseRes res = GetHouseRes.mapEntityToResponse(houseService.getHouseInfo(houseId));
        return new BaseResponse<>(res);
    }

    /**
     * 유저의 아파트 정보 설정하기
     */
    @PatchMapping("")
    public BaseResponse<GetHouseRes> setHouseInfo(@RequestParam Long houseId) {
        try {
            Long userId = jwtService.getUserIdx();
            GetHouseRes res = GetHouseRes.mapEntityToResponse(houseService.setHouseInfo(userId, houseId));
            return new BaseResponse<>(res);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저의 아파트 등록하기 -> User의 house 등록이 아님 확인 필요
     */
    @PostMapping("/register")
    public BaseResponse<String> createHouse(@RequestBody PostHouseReq postHouseReq) {
        House house = houseService.createHouse(postHouseReq);
        return new BaseResponse<>(house.getCity() + " " + house.getDistrict() +
                " " + house.getHouseName() + " 등록이 완료되었습니다.");
    }

    /** GPS 위치 인증을 위한 근처 아파트 조회 **/
    @GetMapping("/spatial/radius")
    public BaseResponse<List<GetHouseRes>> findByRadius(@RequestParam("lng") Double lng, @RequestParam("lat") Double lat, @RequestParam("radius") Double radius) {
        List<House> houseList = houseService.findByRadius(new GeoPoint(lng, lat), radius);
        List<GetHouseRes> res =
                houseList.stream().map(it-> GetHouseRes.mapEntityToResponse(it)).collect(Collectors.toList());
        return new BaseResponse<>(res);
    }
}
