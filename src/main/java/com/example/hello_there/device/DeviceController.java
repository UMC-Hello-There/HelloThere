package com.example.hello_there.device;

import com.example.hello_there.device.dto.PostDeviceReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/save/device-token")
    public BaseResponse<String> saveDeviceToken(@RequestBody PostDeviceReq postDeviceReq) {
        try {
            return new BaseResponse<>(deviceService.saveDevice(postDeviceReq.getToken(), postDeviceReq.getUserId()));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
