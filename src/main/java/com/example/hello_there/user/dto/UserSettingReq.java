package com.example.hello_there.user.dto;

import com.example.hello_there.user.user_setting.UserSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingReq {
    private Boolean commentCheck;   //댓글 알림 설정
    private Boolean recommentCheck; //대댓글 알림 설정
    private Boolean messageCheck;   //쪽지 알림 설정
    private Boolean goodBoardCheck; //인기 게시물 선정 알림 설정

    public UserSetting updateEntity(UserSetting userSetting) {
        if(Objects.nonNull(commentCheck)){
            userSetting.setCommentCheck(commentCheck);
        }

        if(Objects.nonNull(recommentCheck)){
            userSetting.setRecommentCheck(recommentCheck);
        }
        if(Objects.nonNull(messageCheck)){
            userSetting.setMessageCheck(messageCheck);
        }
        if(Objects.nonNull(goodBoardCheck)){
            userSetting.setBestBoardCheck(goodBoardCheck);
        }
        return userSetting;
    }
}
