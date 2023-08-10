package com.example.hello_there.user.dto;

import com.example.hello_there.user.user_setting.UserSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserSettingMessageRes {
    private Boolean messageReceptionBlock;

    public static UserSettingMessageRes fromEntity(UserSetting userSetting) {
        return new UserSettingMessageRes(
            userSetting.isMessageReceptionBlock()
        );
    }
}
