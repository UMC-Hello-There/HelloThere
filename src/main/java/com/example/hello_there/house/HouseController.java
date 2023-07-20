package com.example.hello_there.house;

import com.example.hello_there.house.dto.PatchHouseReq;
import com.example.hello_there.house.dto.PostHouseReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/houses")
public class HouseController {

    private final HouseService houseService;
    private final JwtService jwtService;

    /**
     * 유저의 아파트 정보 설정하기
     */
    @PatchMapping("/update")
    public BaseResponse<String> setHouseInfo(@RequestBody PatchHouseReq patchHouseReq) {
        // PostMan에서 Headers에 Authorization필드를 추가하고, 로그인할 때 받은 jwt 토큰을 입력해야 실행이 됩니다.
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(houseService.setHouseInfo(patchHouseReq, userId));
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
        return new BaseResponse<>(house.getCity() + " " + house.getDistrict() +
                " " + house.getHouseName() + " 등록이 완료되었습니다.");
    }
}
