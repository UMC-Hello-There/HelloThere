package com.example.hello_there.user;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.board.dto.GetBoardRes;
import com.example.hello_there.board.like.LikeBoardRepository;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.chat_room.dto.GetChatRoomRes;
import com.example.hello_there.comment.CommentRepository;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.delete_history.DeleteHistory;
import com.example.hello_there.user.delete_history.DeleteHistoryRepository;
import com.example.hello_there.user.dto.*;
import com.example.hello_there.user.profile.Profile;
import com.example.hello_there.user.profile.ProfileRepository;
import com.example.hello_there.user.profile.ProfileService;
import com.example.hello_there.user.user_setting.UserSettingRepository;
import com.example.hello_there.user.user_setting.UserSetting;
import com.example.hello_there.user_chatroom.UserChatRoom;
import com.example.hello_there.user_notice.UserNotice;
import com.example.hello_there.user_notice.UserNoticeRepository;
import com.example.hello_there.utils.AES128;
import com.example.hello_there.utils.S3Service;
import com.example.hello_there.utils.Secret;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.utils.UtilService.convertLocalDateTimeToLocalDate;
import static com.example.hello_there.utils.UtilService.convertLocalDateTimeToTime;
import static com.example.hello_there.utils.ValidationRegex.isRegexEmail;

@EnableTransactionManagement
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ProfileRepository profileRepository;
    private final TokenRepository tokenRepository;
    private final DeleteHistoryRepository deleteHistoryRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserNoticeRepository userNoticeRepository;
    private final CommentRepository commentRepository;
    private final LikeBoardRepository likeBoardRepository;
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
        if(userRepository.existsByNickName(postUserReq.getNickName())) {
            throw new BaseException(DUPLICATED_NICKNAME);
        }
        Object redisUser = redisTemplate.opsForValue().get(postUserReq.getEmail());
        if(redisUser != null) { // 이메일이 Redis에 등록되었다는 건 탈퇴한 유저임을 의미
            throw new BaseException(DELETED_USER);
        }
        String pwd;
        try{
            pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(postUserReq.getPassword()); // 암호화 코드
        }
        catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        User user = new User();
        user.createUser(postUserReq.getNickName(),postUserReq.getEmail(), pwd, null);
        userRepository.save(user);
        userRepository.flush(); //id 생성 후 userSetting을 진행하기 위함

        UserSetting userSetting = UserSetting.builder().userId(user.getId()).build();
        userSettingRepository.save(userSetting);    //기본 UserSetting 데이터 생성
        
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
     * 닉네임 중복 확인
     */
    public Boolean nickNameChk(String nickName) throws BaseException {
        if(nickName == null || nickName.isEmpty()) {
            throw new BaseException(NICKNAME_CANNOT_BE_NULL);
        }
        if(userRepository.existsByNickName(nickName)) {
            return false;
        }
        return true;
    }

    /**
     * 유저 로그아웃
     */
    @Transactional
    public String logout(Long userId) throws BaseException {
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
     * 본인의 닉네임과 프로필 사진 조회
     */
    public GetUserRes getUsersByNickname(Long userId) throws BaseException{
        User user = utilService.findByUserIdWithValidation(userId);
        return new GetUserRes(user); // 생성자를 활용하여 User 객체를 GetUserRes로 매핑
    }

    /**
     * 유저 닉네임 변경
     */
    @Transactional
    public String modifyUserNickName(Long userId, String nickName) throws BaseException {
        User user = utilService.findByUserIdWithValidation(userId);
        if(nickName.equals(user.getNickName())) {
            throw new BaseException(CANNOT_UPDATE_NICKNAME);
        }
        if(userRepository.existsByNickName(nickName)) {
            throw new BaseException(DUPLICATED_NICKNAME);
        }
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
        List<Board> boards = boardRepository.findBoardByUserId(userId);
        if(!boards.isEmpty()){
            throw new BaseException(CANNOT_DELETE); // 게시글을 먼저 삭제해야만 유저 삭제 가능
        }
        tokenRepository.deleteTokenByUserId(userId);
        Profile profile = profileRepository.findProfileById(userId).orElse(null);
        if(profile != null) {
            profileService.deleteProfile(profile);
            profileRepository.deleteProfileById(userId);
        }
        String email = user.getEmail();
        if(!deleteHistoryRepository.existsByEmail(email)) { // 최초 탈퇴의 경우, 하루 동안 재가입이 제한된다.
            DeleteHistory deleteHistory = DeleteHistory.builder()
                    .email(email)
                    .build();
            deleteHistoryRepository.save(deleteHistory);
            long expirationMillis = TimeUnit.DAYS.toMillis(1); // 하루(24시간)를 Redis의 TTL로 설정
            redisTemplate.opsForValue().set(email, "DELETED_USER", expirationMillis, TimeUnit.MILLISECONDS);
        }
        else { // 두 번 이상 탈퇴한 경우, 30일간 재가입이 제한된다.
            long expirationMillis = TimeUnit.DAYS.toMillis(30); // 한달(30일)을 Redis의 TTL로 설정
            redisTemplate.opsForValue().set(email, "DELETED_USER", expirationMillis, TimeUnit.MILLISECONDS);
        }
        userRepository.deleteUser(userId);
        String result = "요청하신 회원에 대한 삭제가 완료되었습니다.";
        return result;
    }

    /**
     * 소셜로그인 유저의 고유 닉네임을 보장하는 메서드
     */
    @Transactional
    public String generateUniqueNickName(String baseNickName) {
        String uniqueNickName = baseNickName;
        int suffix = 1;
        // 중복된 닉네임을 찾을 때까지 반복
        while (userRepository.existsByNickName(uniqueNickName)) {
            // 유일한 닉네임을 생성하기 위해 auto increment 값을 붙임 ex) 현섭 -> 현섭1 -> 현섭2
            uniqueNickName = baseNickName + suffix;
            suffix++;
        }
        return uniqueNickName;
    }

    public Boolean emailChk(String email) {
        if(email == null || email.isEmpty()) {
            throw new BaseException(EMAIL_CANNOT_BE_NULL);
        }
        if(userRepository.existsByEmail(email)) {
            return false;
        }
        return true;
    }

    public List<GetNoticeRes> getNotice(Long userId) {
        List<UserNotice> userNotices = userNoticeRepository.findUserNoticeByUserId(userId);
        List<GetNoticeRes> getNoticeRes = userNotices.stream()
                .map(userNotice -> new GetNoticeRes(userNotice.getNotice().getNoticeId(),
                        userNotice.getNotice().getTitle(), userNotice.getNotice().getBody(),
                        userNotice.getNotice().getBoardType(),
                        convertLocalDateTimeToTime(userNotice.getNotice().getCreateDate())))
                .sorted(Comparator.comparing(GetNoticeRes::getNoticeId).reversed())
                .collect(Collectors.toList());
        return getNoticeRes;
    }

    /*
     * 마이페이지 내가 댓글 단 게시물
     */
    public List<GetBoardRes> findCommentedBoards(Long userId) {
        try {
            List<Board> boards = commentRepository.findCommentedBoardsByUserId(userId);
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            convertLocalDateTimeToLocalDate(board.getCreateDate()),
                            convertLocalDateTimeToTime(board.getCreateDate()),
                            board.getUser().getNickName(), board.getTitle(), board.getContent(), board.getView(),
                            commentRepository.countByBoardBoardId(board.getBoardId()), likeBoardRepository.countByBoardBoardId(board.getBoardId())))
                    .collect(Collectors.toList());
            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
