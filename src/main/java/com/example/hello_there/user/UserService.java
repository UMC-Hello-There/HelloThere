package com.example.hello_there.user;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.jwt.JwtProvider;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;

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
    // private final BCryptPasswordEncoder bCryptPasswordEncoder; // spring security login 사용 시 필요
    private final RedisTemplate redisTemplate;

    /**
     * 유저 생성 후 DB에 저장(회원 가입) with JWT
     */
    @Transactional
    public PostUserRes createUser(PostUserReq postUserReq) throws BaseException {
        if(userRepository.findByEmailCount(postUserReq.getEmail()) >= 1) {
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }
        if(postUserReq.getPassword().isEmpty()){
            throw new BaseException(PASSWORD_CANNOT_BE_NULL);
        }
        String pwd;
        try{
            // 암호화: postUserReq에서 제공받은 비밀번호를 보안을 위해 암호화시켜 DB에 저장합니다.
            // ex) password123 -> dfhsjfkjdsnj4@!$!@chdsnjfwkenjfnsjfnjsd.fdsfaifsadjfjaf
            pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(postUserReq.getPassword()); // 암호화코드
        }
        catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try {
            User user = new User();
            user.createUser(postUserReq.getEmail(), pwd, postUserReq.getNickName(), postUserReq.isGender(), postUserReq.getBirth());
            userRepository.save(user);
            return new PostUserRes(user.getId(), user.getNickName());
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보낸다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 유저 로그인 with JWT
     */
    public PostLoginRes login(PostLoginReq postLoginReq) throws BaseException {
        User user = utilService.findByEmailWithValidation(postLoginReq.getEmail());
        Token existToken = tokenRepository.findTokenByUserId(user.getId()).orElse(null);
        if(existToken != null) { // 이미 토큰 Repository에 토큰이 존재하는 경우
            throw new BaseException(ALREADY_LOGIN);
        }
        String password; // DB에 저장된 암호화된 비밀번호를 복호화한 값을 저장하기 위한 변수
        try{
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(user.getPassword()); // 복호화
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }
        if(postLoginReq.getPassword().equals(password)){
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(user.getId());
            String accessToken = tokenInfo.getAccessToken();
            String refreshToken = tokenInfo.getRefreshToken();
            Token token = Token.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(user)
                    .build();;
            tokenRepository.save(token);
            return new PostLoginRes(user.getId(), accessToken, refreshToken);
        }
        else{
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    /**
     * 모든 회원 조회
     */
    public List<GetUserRes> getMembers() throws BaseException {
        try{
            List<User> users = userRepository.findUsers(); // User를 List로 받아 GetUserRes로 바꿔줌
            List<GetUserRes> getUserRes = users.stream()
                    .map(user -> new GetUserRes(user.getId(), user.getEmail(), user.getNickName(), user.isGender(),
                            Optional.ofNullable(user.getBirth()).map(LocalDate::toString).orElse(null),
                            user.isManager(), user.getStatus()))
                    .collect(Collectors.toList());
            return getUserRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 특정 닉네임 조회
     */
    public List<GetUserRes> getUsersByNickname(String nickname) throws BaseException {
        try{
            List<User> users = userRepository.findUserByNickName(nickname);
            List<GetUserRes> getUserRes = users.stream()
                    .map(user -> new GetUserRes(user.getId(), user.getEmail(), user.getNickName(), user.isGender(),
                            Optional.ofNullable(user.getBirth()).map(LocalDate::toString).orElse(null),
                            user.isManager(), user.getStatus()))
                    .collect(Collectors.toList());
            return getUserRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public void modifyUserNickName(PatchUserReq patchUserReq) {
        User user = userRepository.getReferenceById(patchUserReq.getUserId());
        user.updateNickName(patchUserReq.getNickName());
    }

    @Transactional
    public String deleteUser(Long userId) throws BaseException{
        User user = utilService.findByUserIdWithValidation(userId);
        List<Board> boards = boardRepository.findBoardByUserId(user.getId());
        if(!boards.isEmpty()){
            throw new BaseException(CANNOT_DELETE);
        }
        tokenRepository.deleteTokenByUserId(user.getId());
        userRepository.deleteUser(user.getEmail());
        String result = "요청하신 회원에 대한 삭제가 완료되었습니다.";
        return result;
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

    @Transactional
    public String logout(User logoutUser) throws BaseException{
        try {
            Token token = utilService.findTokenByUserIdWithValidation(logoutUser.getId());
            String accessToken = token.getAccessToken();
            //엑세스 토큰 남은 유효시간
            Long expiration = jwtProvider.getExpiration(accessToken);
            //Redis Cache에 저장
            redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
            //리프레쉬 토큰 삭제
            tokenRepository.deleteTokenByUserId(logoutUser.getId());
            String result = "로그아웃 되었습니다.";
            return result;
        } catch (Exception e) {
            throw new BaseException(FAILED_TO_LOGOUT);
        }

    }

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
}