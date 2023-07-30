package com.example.hello_there.user.user_setting.dto;

import com.example.hello_there.user.user_setting.UserSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingRes {

    private Boolean commentCheck;   //댓글 알림 설정
    private Boolean recommentCheck; //대댓글 알림 설정
    private Boolean messageCheck;   //쪽지 알림 설정
    private Boolean goodBoardCheck; //인기 게시물 선정 알림 설정

    public static UserSettingRes fromEntity(UserSetting userSetting){
        return new UserSettingRes(
                userSetting.isCommentCheck(),
                userSetting.isRecommentCheck(),
                userSetting.isMessageCheck(),
                userSetting.isBestBoardCheck()
        );
    }
}
