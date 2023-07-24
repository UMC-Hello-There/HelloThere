package com.example.hello_there.user;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.dto.*;
import com.example.hello_there.user.profile.Profile;
import com.example.hello_there.user.profile.ProfileRepository;
import com.example.hello_there.user.profile.ProfileService;
import com.example.hello_there.utils.AES128;
import com.example.hello_there.utils.S3Service;
import com.example.hello_there.utils.Secret;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.utils.ValidationRegex.isRegexEmail;

@EnableTransactionManagement
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ProfileRepository profileRepository;
    private final TokenRepository tokenRepository;
    private final S3Service s3Service;
    private final JwtProvider jwtProvider;
    private final UtilService utilService;
    private final ProfileService profileService;
    private final JwtService jwtService;
    private final RedisTemplate redisTemplate;

    /**
     * 유저 생성 후 DB에 저장(회원 가입) with JWT
     */
    @Transactional
    public PostUserRes createUser(PostUserReq postUserReq) throws BaseException {
        if(!isRegexEmail(postUserReq.getEmail())) throw new BaseException(POST_USERS_INVALID_EMAIL);
        if(userRepository.findByEmailCount(postUserReq.getEmail()) >= 1) {
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }
        if(postUserReq.getPassword().isEmpty()){
            throw new BaseException(PASSWORD_CANNOT_BE_NULL);
        }
        if(!postUserReq.getPassword().equals(postUserReq.getPasswordChk())) {
            throw new BaseException(PASSWORD_MISSMATCH);
        }
        String pwd;
        try{
            pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(postUserReq.getPassword()); // 암호화코드
        }
        catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        User user = new User();
        user.createUser(postUserReq.getNickName(),postUserReq.getEmail(), pwd, null);
        userRepository.save(user);
        return new PostUserRes(user);
    }

    /**
     * 유저 로그인 with JWT
     */
    public PostLoginRes login(PostLoginReq postLoginReq) throws BaseException {
        if(!isRegexEmail(postLoginReq.getEmail())) throw new BaseException(POST_USERS_INVALID_EMAIL);
        User user = utilService.findByEmailWithValidation(postLoginReq.getEmail());
        if(user.getStatus() == UserStatus.INACTIVE) { // 유저가 영구정지 상태인 경우
            throw new BaseException(UNABLE_TO_USE);
        }
        Token existToken = tokenRepository.findTokenByUserId(user.getId()).orElse(null);
        if (existToken != null) {
            throw new BaseException(ALREADY_LOGIN);
        }

        String password;
        try {
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(user.getPassword());
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }

        if (postLoginReq.getPassword().equals(password)) {
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(user.getId());
            Token token = Token.builder()
                    .accessToken(tokenInfo.getAccessToken())
                    .refreshToken(tokenInfo.getRefreshToken())
                    .user(user)
                    .build();
            tokenRepository.save(token);
            return new PostLoginRes(user, token);
        } else {
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    /**
     * 유저 로그아웃
     */
    @Transactional
    public String logout(Long userId) throws BaseException{
        try {
            if (userId == 0L) { // 로그아웃 요청은 access token이 만료되더라도 재발급할 필요가 없음.
                User user = tokenRepository.findUserByAccessToken(jwtService.getJwt()).orElse(null);
                if (user != null) {
                    Token token = tokenRepository.findTokenByUserId(user.getId()).orElse(null);
                    tokenRepository.deleteTokenByAccessToken(token.getAccessToken());
                    return "로그아웃 되었습니다.";
                }
                else {
                    throw new BaseException(INVALID_JWT);
                }
            }
            else { // 토큰이 만료되지 않은 경우
                User logoutUser = utilService.findByUserIdWithValidation(userId);
                Token token = utilService.findTokenByUserIdWithValidation(logoutUser.getId());
                String accessToken = token.getAccessToken();
                //엑세스 토큰 남은 유효시간
                Long expiration = jwtProvider.getExpiration(accessToken);
                //Redis Cache에 저장
                redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
                //리프레쉬 토큰 삭제
                tokenRepository.deleteTokenByUserId(logoutUser.getId());
                return "로그아웃 되었습니다.";
            }
        } catch (Exception e) {
            throw new BaseException(FAILED_TO_LOGOUT);
        }

    }


    /**
     * 소셜로그 아웃
     */
    public String socialLogout(String accessToken) throws BaseException{
        // HttpHeader 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);
        // HttpHeader를 포함한 요청 객체 생성
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        // RestTemplate를 이용하여 로그아웃 요청 보내기
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://kapi.kakao.com/v1/user/unlink",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        // 응답 확인
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // 로그아웃 성공
            String result = "로그아웃 되었습니다.";
            return result;
        }
        else {
            // 로그아웃 실패
            throw new BaseException(KAKAO_ERROR);
        }
    }

    /**
     * 모든 유저 조회
     */
    public List<GetUserRes> getMembers() {
        List<User> users = userRepository.findUsers();
        return users.stream()
                .map(GetUserRes::new) // 여기서 생성자를 활용하여 User 객체를 GetUserRes로 매핑
                .collect(Collectors.toList());
    }

    /**
     * 특정 유저를 닉네임으로 조회
     */
    public List<GetUserRes> getUsersByNickname(String nickname) throws BaseException{
        List<User> users = userRepository.findUserByNickName(nickname);
        if(users.isEmpty()) {
            throw new BaseException(NONE_EXIST_NICKNAME);
        }
        return users.stream()
                .map(GetUserRes::new) // 여기서 생성자를 활용하여 User 객체를 GetUserRes로 매핑
                .collect(Collectors.toList());
    }

    /**
     * 유저 닉네임 변경
     */
    @Transactional
    public String modifyUserNickName(Long userId, String nickName) throws BaseException {
        User user = utilService.findByUserIdWithValidation(userId);
        user.setNickName(nickName);
        return "회원정보가 수정되었습니다.";
    }

    /**
     *  유저 프로필 변경
     */
    @Transactional
    public String modifyProfile(Long userId, MultipartFile multipartFile) throws BaseException {
        try {
            User user = utilService.findByUserIdWithValidation(userId);
            Profile profile = profileRepository.findProfileById(userId).orElse(null);
            if(profile == null) { // 프로필이 미등록된 사용자가 변경을 요청하는 경우
                GetS3Res getS3Res;
                if(multipartFile != null) {
                    getS3Res = s3Service.uploadSingleFile(multipartFile);
                    profileService.saveProfile(getS3Res, user);
                }
            }
            else { // 프로필이 등록된 사용자가 변경을 요청하는 경우
                // 1. 버킷에서 삭제
                profileService.deleteProfile(profile);
                // 2. Profile Repository에서 삭제
                profileService.deleteProfileById(userId);
                if(multipartFile != null) {
                    GetS3Res getS3Res = s3Service.uploadSingleFile(multipartFile);
                    profileService.saveProfile(getS3Res, user);
                }
            }
            return "프로필 수정이 완료되었습니다.";
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }

    /**
     *  유저 비밀번호 변경
     */
    @Transactional
    public String modifyPassword(Long userId, PatchPasswordReq patchPasswordReq) throws BaseException {
        try {
            User user = utilService.findByUserIdWithValidation(userId);
            if(user.getPassword() == null) { // 소셜로그인 유저
                throw new BaseException(SOCIAL_LOGIN_USER);
            }
            String password;
            try {
                password = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(user.getPassword());
            } catch (Exception ignored) {
                throw new BaseException(PASSWORD_DECRYPTION_ERROR);
            }
            // 이전 비밀번호가 일치하지 않는 경우
            if (!patchPasswordReq.getExPassword().equals(password)) {
                throw new BaseException(EX_PASSWORD_MISSMATCH);
            }
            // 이전 비밀번호와 새 비밀번호가 일치하는 경우
            if(patchPasswordReq.getNewPassword().equals(patchPasswordReq.getExPassword())) {
                throw new BaseException(CANNOT_UPDATE_PASSWORD);
            }
            // 새 비밀번호와 새 비밀번호 확인이 일치하지 않는 경우
            if(!patchPasswordReq.getNewPassword().equals(patchPasswordReq.getNewPasswordChk())) {
                throw new BaseException(PASSWORD_MISSMATCH);
            }

            String pwd;
            try{
                pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(patchPasswordReq.getNewPassword()); // 암호화코드
            }
            catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
                throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
            }
            user.setPassword(pwd);
            return "비밀번호 변경이 완료되었습니다.";
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }

    /**
     *  유저 탈퇴
     */
    @Transactional
    public String deleteUser(Long userId, String agreement) throws BaseException{
        if(!agreement.equals("계정 삭제에 동의합니다")) {
            throw new BaseException(AGREEMENT_MISMATCH);
        }
        User user = utilService.findByUserIdWithValidation(userId);
        List<Board> boards = boardRepository.findBoardByUserId(user.getId());
        if(!boards.isEmpty()){
            throw new BaseException(CANNOT_DELETE); // 게시글을 먼저 삭제해야만 유저 삭제 가능
        }
        tokenRepository.deleteTokenByUserId(user.getId());
        Profile profile = profileRepository.findProfileById(userId).orElse(null);
        if(profile != null) {
            profileService.deleteProfile(profile);
            profileRepository.deleteProfileById(userId);
        }
        userRepository.deleteUser(user.getEmail());
        String result = "요청하신 회원에 대한 삭제가 완료되었습니다.";
        return result;
    }
}
