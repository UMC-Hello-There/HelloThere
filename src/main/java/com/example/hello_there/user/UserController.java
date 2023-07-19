package com.example.hello_there.user;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.dto.*;
import com.example.hello_there.user.profile.ProfileService;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.utils.ValidationRegex.*;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final ProfileService profileService;
    private final JwtProvider jwtProvider;
    private final UtilService utilService;

    /**
     * 회원 가입
     */
    @PostMapping("/create")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq){
        if(!isRegexEmail(postUserReq.getEmail())) return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
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
            if(!isRegexEmail(postLoginReq.getEmail())) return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            return new BaseResponse<>(userService.login(postLoginReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PostMapping("/log-out") // Redis가 켜져있어야 동작한다.
    public BaseResponse<String> logoutUser() {
        try {
            Long UserId = jwtService.getLogoutUserIdx(); // 토큰 만료 상황에서 로그아웃을 시도하면 0L을 반환
            if (UserId == 0L) { // 로그아웃 요청은 access token이 만료되더라도 재발급할 필요가 없음.
                User user = tokenRepository.findUserByAccessToken(jwtService.getJwt()).orElse(null);
                if (user != null) {
                    Token token = tokenRepository.findTokenByUserId(user.getId()).orElse(null);
                    tokenRepository.deleteTokenByAccessToken(token.getAccessToken());
                    String result = "로그아웃 되었습니다.";
                    return new BaseResponse<>(result);
                }
                else {
                    return new BaseResponse<>(INVALID_JWT);
                }
            }
            else { // 토큰이 만료되지 않은 경우
                User logoutUser = utilService.findByUserIdWithValidation(UserId);
                String result = userService.logout(logoutUser);
                return new BaseResponse<>(result);
            }
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 회원 조회
     * nickname이 파라미터에 없을 경우 모두 조회
     */
    @GetMapping("Read")
    public BaseResponse<List<GetUserRes>> getUsers(@RequestParam(required = false) String nickName){
        if (nickName == null) { // query string인 nickname이 없을 경우, 그냥 전체 유저정보를 불러온다.
            return new BaseResponse<>(userService.getMembers());
        }
        // query string인 nickname이 있을 경우, 조건을 만족하는 유저정보들을 불러온다.
        return new BaseResponse<>(userService.getUsersByNickname(nickName));
    }


    /**
     * 멤버 닉네임 변경
     */
    @PatchMapping("/update")
    public BaseResponse<String> modifyUserName(@RequestParam String nickName) {
        // PostMan에서 Headers에 Authorization필드를 추가하고, 로그인할 때 받은 jwt 토큰을 입력해야 실행이 됩니다.
        try {
            Long userId = jwtService.getUserIdx();
            User user = utilService.findByUserIdWithValidation(userId);
            PatchUserReq patchUserReq = new PatchUserReq(user.getId(), nickName);
            userService.modifyUserNickName(patchUserReq);
            String result = "회원정보가 수정되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 멤버 프로필 변경
     */
    @PatchMapping("/update-profile")
    public BaseResponse<String> modifyMemberProfile(@RequestPart(value = "image", required = false) MultipartFile multipartFile) {
        try {
            Long memberId = jwtService.getUserIdx();
            return new BaseResponse<>(userService.modifyProfile(memberId, multipartFile));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @DeleteMapping("/delete")
    public BaseResponse<String> deleteUser(){
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(userService.deleteUser(userId));
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }
}

