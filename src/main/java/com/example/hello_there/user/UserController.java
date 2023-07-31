package com.example.hello_there.user;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.user.dto.*;
import com.example.hello_there.user.user_setting.UserSettingService;
import com.example.hello_there.user.user_setting.UserSetting;
import com.example.hello_there.user.user_setting.dto.UserSettingMessageReq;
import com.example.hello_there.user.user_setting.dto.UserSettingMessageRes;
import com.example.hello_there.user.user_setting.dto.UserSettingReq;
import com.example.hello_there.user.user_setting.dto.UserSettingRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserSettingService userSettingService;
    private final JwtService jwtService;

    /**
     * 회원 가입
     */
    @PostMapping("")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq){
        try {
            return new BaseResponse<>(userService.createUser(postUserReq));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/log-in")
    public BaseResponse<PostLoginRes> loginMember(@RequestBody PostLoginReq postLoginReq){
        try{
            return new BaseResponse<>(userService.login(postLoginReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/log-out") // Redis가 켜져있어야 동작한다.
    public BaseResponse<String> logoutUser() {
        try {
            Long userId = jwtService.getLogoutUserIdx(); // 토큰 만료 상황에서 로그아웃을 시도하면 0L을 반환
            return new BaseResponse<>(userService.logout(userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/nickname")
    public BaseResponse<Boolean> nickNameChk(@RequestParam String nickName) {
        try {
            return new BaseResponse<>(userService.nickNameChk(nickName));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/email")
    public BaseResponse<Boolean> emailChk(@RequestParam String email) {
        try {
            return new BaseResponse<>(userService.emailChk(email));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저 조회
     * nickname이 파라미터에 없을 경우 아파트 주민을 모두 조회
     */
    @GetMapping("")
    public BaseResponse<List<GetUserRes>> getUsers(@RequestParam(required = false) String nickName) {
        try {
            // query string인 nickname이 없을 경우 전체 아파트 주민 정보를 반환
            if (nickName == null) {
                Long userId = jwtService.getUserIdx();
                return new BaseResponse<>(userService.getMembers(userId));
            }
            // query string인 nickname이 있는 경우 해당 유저의 정보를 반환
            return new BaseResponse<>(userService.getUsersByNickname(nickName));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }

    }

    /**
     * 유저 닉네임 변경
     */
    @PatchMapping("/nickname")
    public BaseResponse<String> modifyUserName(@RequestParam String nickName) {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(userService. modifyUserNickName(userId, nickName));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저 프로필 사진 변경
     */
    @PatchMapping("/profile")
    public BaseResponse<String> modifyProfile(@RequestPart(value = "image", required = false) MultipartFile multipartFile) {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(userService.modifyProfile(userId, multipartFile));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저 비밀번호 변경
     */
    @PatchMapping("/password")
    public BaseResponse<String> modifyPassword(@RequestBody PatchPasswordReq patchPasswordReq) {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(userService.modifyPassword(userId, patchPasswordReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저 탈퇴
     */
    @DeleteMapping("")
    public BaseResponse<String> deleteUser(@RequestParam String agreement){
        // 비밀번호를 입력받아 회원 삭제를 처리하는 로직의 경우, 소셜 로그인 유저에 적용하기 어려움.
        // "계정 삭제에 동의합니다"라는 문구를 입력받는 것(띄어쓰기까지 정확히 일치)으로 회원 삭제를 처리하기로 함.
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(userService.deleteUser(userId, agreement));
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 마이페이지 알림 설정 조회
     */
    @GetMapping("/setting")
    public BaseResponse<UserSettingRes> getUserSetting() {
        try {
            Long userId = jwtService.getUserIdx();
            UserSetting userSetting = userSettingService.findByUserId(userId);
            return new BaseResponse<>(UserSettingRes.fromEntity(userSetting));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 마이페이지 알림 설정
     */
    @PatchMapping("/setting")
    public BaseResponse<UserSettingRes> patchUserSetting(@RequestBody UserSettingReq userSettingReq) {
        try {
            Long userId = jwtService.getUserIdx();
            UserSetting userSetting = userSettingService.modifyUserSetting(userId, userSettingReq);
            return new BaseResponse<>(UserSettingRes.fromEntity(userSetting));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 마이페이지 쪽지 설정 조회
     */
    @GetMapping("/setting/message")
    public BaseResponse<UserSettingMessageRes> getUserSettingMessage() {
        try {
            Long userId = jwtService.getUserIdx();
            UserSetting userSetting = userSettingService.findByUserId(userId);
            return new BaseResponse<>(UserSettingMessageRes.fromEntity(userSetting));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 마이페이지 쪽지 설정
     */
    @PatchMapping("/setting/message")
    public BaseResponse<UserSettingRes> patchUserSettingMessage(@RequestBody UserSettingMessageReq userSettingMessageReq) {
        try {
            Long userId = jwtService.getUserIdx();
            UserSetting userSetting = userSettingService.modifyUserSettingMessage(userId, userSettingMessageReq);
            return new BaseResponse<>(UserSettingRes.fromEntity(userSetting));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 알림 조회
     */
    @GetMapping("/notice")
    public BaseResponse<List<GetNoticeRes>> getNotice() {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(userService.getNotice(userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}

