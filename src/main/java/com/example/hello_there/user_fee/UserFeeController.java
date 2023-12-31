package com.example.hello_there.user_fee;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.user_fee.dto.UserFeeRes;
import com.example.hello_there.user_fee.dto.PatchUserFeeReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-fees")
@RequiredArgsConstructor
public class UserFeeController {
    private final UserFeeService userFeeService;
    private final JwtService jwtService;

    /** 이달의 관리비 조회(홈 화면) **/
    @GetMapping("")
    public BaseResponse<UserFeeRes> getUserFeeCurrent(@RequestParam("feeYear") int feeYear, @RequestParam("feeMonth") int feeMonth) {
        try{
            Long userId = jwtService.getUserIdx();
            UserFee userFeeCurrent = userFeeService.getUserFeeCurrent(userId, feeYear, feeMonth);
            UserFeeRes res = UserFeeRes.mapEntityToResponse(userFeeCurrent);
            return new BaseResponse<>(res);
        }
        catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 이달의 관0리비 조회(위젯 또는 하단 탭 접근)
     * TODO feeYear과 feeMonth가 null 로 입력되었을 경우에 대한 처리
     * **/
    @GetMapping("/detail")
    public BaseResponse<List<UserFeeRes>> getUserFeeCustom(@RequestParam("feeYear") int feeYear, @RequestParam("feeMonth") int feeMonth) {
        try{
            Long userId = jwtService.getUserIdx();
            List<UserFee> userFeeCustomList = userFeeService.getUserFeeCustom(userId, feeYear, feeMonth);
            List<UserFeeRes> res =
                    userFeeCustomList.stream().map(it-> UserFeeRes.mapEntityToResponse(it)).collect(Collectors.toList());
            return new BaseResponse<>(res);
        }
        catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PatchMapping("/{userFeeId}")
    public BaseResponse<UserFeeRes> modifyUserFee(@PathVariable Long userFeeId, @RequestBody PatchUserFeeReq patchUserFeeReq){
        UserFee userFeeCurrent = userFeeService.updateUserFee(userFeeId, patchUserFeeReq);
        UserFeeRes res = UserFeeRes.mapEntityToResponse(userFeeCurrent);
        return new BaseResponse<>(res);
    }


}
