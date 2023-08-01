package com.example.hello_there.user.user_setting.dto;

import com.example.hello_there.user.user_setting.UserSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserSettingMessageReq {
    private Boolean messageReceptionBlock;

    public UserSetting updateEntity(UserSetting userSetting) {
        if(Objects.nonNull(messageReceptionBlock)){
            userSetting.setMessageReceptionBlock(messageReceptionBlock);
        }
        return userSetting;
    }

}
