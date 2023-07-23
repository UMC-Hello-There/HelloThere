package com.example.hello_there.user_fee;

import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.house.House;
import com.example.hello_there.house.dto.GetHouseRes;
import com.example.hello_there.house.vo.GeoPoint;
import com.example.hello_there.user_fee.dto.GetUserFeeRes;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-fees")
@RequiredArgsConstructor
public class UserFeeController {
    private final UserFeeService userFeeService;

    /** 이달의 관리비 조회(홈 화면) **/
    @GetMapping("")
    public BaseResponse<GetUserFeeRes> getUserFeeCurrent(@RequestParam("houseId") Long houseId, @RequestParam("feeYear") int feeYear, @RequestParam("feeMonth") int feeMonth) {
        Optional<UserFee> userFeeCurrent = userFeeService.getUserFeeCurrent(houseId, feeYear, feeMonth);
        GetUserFeeRes res = GetUserFeeRes.mapEntityToResponse(userFeeCurrent);
        return new BaseResponse<>(res);
    }
}
