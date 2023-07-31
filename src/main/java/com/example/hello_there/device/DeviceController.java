package com.example.hello_there.device;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final JwtService jwtService;

    /**
     * 프론트엔드 측에서 디바이스 토큰을 DB에 저장하기 위해 사용하는 API
     */
    @PostMapping("/device-token")
    public BaseResponse<String> saveDeviceToken() {
        try {
            Long userId = jwtService.getUserIdx();
            String token = deviceService.getDeviceToken();
            return new BaseResponse<>(deviceService.saveDevice(token, userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 프론트엔드 측에서 디바이스 토큰 갱신을 반영하기 위해 사용하는 API
     */
    @PatchMapping("/device-token")
    public BaseResponse<String> updateDeviceToken() {
        try {
            Long userId = jwtService.getUserIdx();
            String token = deviceService.getDeviceToken();
            return new BaseResponse<>(deviceService.updateDevice(token, userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
