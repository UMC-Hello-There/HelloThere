package com.example.hello_there.device;

import com.example.hello_there.user.User;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UtilService utilService;

    public String saveDevice(String token, Long userId) {
        User user = utilService.findByUserIdWithValidation(userId);
        Device device = new Device.DeviceBuilder()
                .token(token)
                .user(user)
                .build();
        deviceRepository.save(device);
        return "디바이스 저장이 완료되었습니다.";
    }

    public String updateDevice(String token, Long userId) {
        Device device = utilService.findDeviceByTokenWithValidation(token);
        User user = utilService.findByUserIdWithValidation(userId);
        device.setToken(token);
        deviceRepository.save(device);
        return "디바이스 토큰 업데이트가 완료되었습니다.";
    }
}
