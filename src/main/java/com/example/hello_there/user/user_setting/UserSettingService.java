package com.example.hello_there.user.user_setting;

<<<<<<< HEAD
import com.example.hello_there.user.User;
import com.example.hello_there.user.dto.UserSettingMessageReq;
import com.example.hello_there.user.dto.UserSettingReq;
=======
import com.example.hello_there.user.user_setting.dto.UserSettingMessageReq;
import com.example.hello_there.user.user_setting.dto.UserSettingReq;
>>>>>>> 57dda2832b8e8a332786e756058b4838617b4ce3
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingService {
    private final UserSettingRepository userSettingRepository;
    private final UtilService utilService;


    public UserSetting findByUserId(Long userId) {
        return userSettingRepository.findByUserId(userId);
    }

    public UserSetting modifyUserSetting(Long userId, UserSettingReq userSettingReq) {
        UserSetting userSetting = userSettingRepository.findByUserId(userId);
        return userSettingRepository.save(userSettingReq.updateEntity(userSetting));
    }

    public UserSetting modifyUserSettingMessage(Long userId, UserSettingMessageReq userSettingMessageReq) {
        UserSetting userSetting = userSettingRepository.findByUserId(userId);
        return userSettingRepository.save(userSettingMessageReq.updateEntity(userSetting));
    }
}