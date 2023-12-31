package com.example.hello_there.chat_room;

import com.example.hello_there.chat_room.dto.*;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.sqs.SQSService;
import com.example.hello_there.text_message.TextMessage;
import com.example.hello_there.text_message.TextMessageRepository;
import com.example.hello_there.text_message.dto.AddUserReq;
import com.example.hello_there.text_message.dto.PostMessageReq;
import com.example.hello_there.report.Report;
import com.example.hello_there.report.ReportRepository;
import com.example.hello_there.report.ReportService;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserStatus;
import com.example.hello_there.user.profile.Profile;
import com.example.hello_there.user_chatroom.UserChatRoom;
import com.example.hello_there.user_chatroom.UserChatRoomRepository;
import com.example.hello_there.utils.AES128;
import com.example.hello_there.utils.Secret;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.report.ReportCount.ADD_REPORT_FOR_MESSAGE;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final TokenRepository tokenRepository;
    private final TextMessageRepository textMessageRepository;
    private final ReportService reportService;
    private final ReportRepository reportRepository;
    private final UtilService utilService;
    private final SQSService sqsService;

    @Transactional
    public ChatRoom createChatRoom(Long userId, PostChatRoomReq postChatRoomReq) throws BaseException {
        // roomName 와 roomPwd 로 chatRoom 빌드 후 return
        String pwd;
        try{
            if(postChatRoomReq.getRoomPassword() == null) {
                pwd = null;
            } else {
                pwd = new AES128(Secret.ROOM_PASSWORD_KEY).encrypt(postChatRoomReq.getRoomPassword()); // 암호화코드
            }

        }
        catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(UUID.randomUUID().toString())
                .roomName(postChatRoomReq.getRoomName())
                .roomPassword(pwd) // 채팅방 패스워드
                .secretChk(postChatRoomReq.isSecretChk()) // 채팅방 잠금 여부
                .userCount(1) // 채팅방 참여 인원수
                .maxUserCount(postChatRoomReq.getMaxUserCount()) // 최대 인원수 제한
                .textMessageList(new ArrayList<>())
                .build();
        User user = utilService.findByUserIdWithValidation(userId);
        UserChatRoom userChatRoom = new UserChatRoom();
        userChatRoom.setChatRoom(chatRoom);
        userChatRoom.setUser(user);
        chatRoomRepository.save(chatRoom);
        userChatRoomRepository.save(userChatRoom);
        return chatRoom;
    }

    // 채팅방 인원+1
    @Transactional
    public void plusUserCount(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(roomId).orElse(null);
        chatRoom.updateUserCount(chatRoom.getUserCount() + 1);
        chatRoomRepository.save(chatRoom);
    }

    // 채팅방 인원-1
    @Transactional
    public void minusUserCount(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(roomId).orElse(null);
        chatRoom.updateUserCount(chatRoom.getUserCount() - 1);
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void addUser(Long userId, String chatRoomId) throws BaseException {
        ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(chatRoomId);
        UserChatRoom userChatRoom = userChatRoomRepository.findUserChatRoomByUserIdWithRoomId(userId, chatRoomId).orElse(null);
        if(userChatRoom != null) { // 이미 채팅방에 추가된 유저인 경우
            throw new BaseException(ALREADY_EXIST_MEMBER);
        }
        User user = utilService.findByUserIdWithValidation(userId);
        userChatRoom.setUser(user);
        userChatRoom.setChatRoom(chatRoom);
        userChatRoomRepository.save(userChatRoom);
        plusUserCount(chatRoomId);
    }

    public List<GetUserRes> getUserListById(String chatRoomId) throws BaseException {
        utilService.findChatRoomByChatRoomIdWithValidation(chatRoomId);
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findUserChatRoomByRoomId(chatRoomId);
        List<GetUserRes> getUserRes = userChatRooms.stream()
                .map(userChatRoom -> {
                    Long userId = userChatRoom.getUser().getId();
                    String nickName = userChatRoom.getUser().getNickName();
                    String profileUrl = Optional.ofNullable(userChatRoom.getUser().getProfile())
                            .map(profile -> profile.getProfileUrl())
                            .orElse(""); // 프로필 URL이 null일 경우 빈 문자열로 대체

                    String profileFileName = Optional.ofNullable(userChatRoom.getUser().getProfile())
                            .map(profile -> profile.getProfileFileName())
                            .orElse(""); // 프로필 파일 이름이 null일 경우 빈 문자열로 대체

                    return new GetUserRes(userId, nickName, profileUrl, profileFileName);
                })
                .collect(Collectors.toList());
        return getUserRes;
    }

    public List<GetChatRoomRes> getChatRoomListById(Long userId) {
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findUserListByUserId(userId);

        List<GetChatRoomRes> getChatRoomRes = userChatRooms.stream()
                .map(userChatRoom -> {
                    List<TextMessage> textMessages =
                            textMessageRepository.findMessagesByRoomId(userChatRoom.getChatRoom().getChatRoomId());
                    String latestMessage = "";
                    String latestDate = "";
                    String latestTime = "";
                    int unreadCount = 0;
                    if (!textMessages.isEmpty()) {
                        TextMessage lastMessage = textMessages.get(textMessages.size() - 1);
                        latestMessage = lastMessage.getMessage();
                        latestDate = lastMessage.getSendDate();
                        latestTime = lastMessage.getSendTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                        LocalTime localTime = LocalTime.parse(latestTime, formatter);
                        latestTime = UtilService.formatTime(localTime);
                        for(TextMessage textMessage : textMessages) {
                            if(!textMessage.isRead()) {
                                unreadCount++;
                            }
                        }
                    }

                    return new GetChatRoomRes(userChatRoom.getChatRoom().getChatRoomId(),
                            userChatRoom.getChatRoom().getRoomName(), latestMessage,
                            latestDate, latestTime, unreadCount);
                })
                .collect(Collectors.toList());
        return getChatRoomRes;
    }

    public List<GetLoginUserRes> getLoginUsers(Long userId) throws BaseException {
        User me = utilService.findByUserIdWithValidation(userId);
        Long houseId = me.getHouse().getHouseId();
        List<User> users = tokenRepository.findUsersWithOutMe(userId, houseId);

        List<GetLoginUserRes> getLoginUserRes = users.stream()
                .map(user -> {
                    Profile profile = user.getProfile();
                    String profileUrl = null;
                    String profileFileName = null;
                    if (profile != null) {
                        profileUrl = profile.getProfileUrl();
                        profileFileName = profile.getProfileFileName();
                    }
                    return new GetLoginUserRes(user.getId(), user.getNickName(), profileUrl, profileFileName);
                })
                .collect(Collectors.toList());
        return getLoginUserRes;
    }

    public List<GetChatRoomDetailRes> getChatRoomDetails(String roomId) throws BaseException {
        List<GetChatRoomDetailRes> chatRoomDetailResList = new ArrayList<>(); // 유저에게 보여질 채팅방 정보
        utilService.findChatRoomByChatRoomIdWithValidation(roomId); // 채팅방 찾기
        List<TextMessage> textMessages = textMessageRepository.findMessagesByRoomId(roomId); // 채팅방에 있는 메시지 리스트를 저장

        List<String> messageList = new ArrayList<>(); // 같은 유저의 연속된 메시지를 저장하는 리스트
        for (int i = 0; i < textMessages.size(); i++) { // 채팅 기록을 역순(최근 순)으로 보여준다.
            TextMessage msg = textMessages.get(i);
            msg.setRead(true);
            User sender = msg.getSender();
            messageList.add(msg.getMessage()); // 리스트에 메시지를 add
            if (i < textMessages.size() - 1) { // ArrayIndexOutOfBoundsException에 대한 Handling
                TextMessage prevMsg = textMessages.get(i + 1); // 바로 이전 메시지
                User prevSender = prevMsg.getSender(); // 바로 이전 메시지의 발신자
                // 이전에 메시지를 보낸 유저와 이번 메시지를 보낸 유저가 같으면
                if (sender.getId().equals(prevSender.getId())) {
                    continue; // 더 이상 할 거 없다.
                }
            }
            // 이전에 메시지를 보낸 유저와 이번 메시지를 보낸 유저가 같지 않으면
            String profileFileName = null;
            String profileUrl = null;
            Profile profile = sender.getProfile();
            if (profile != null) {
                profileFileName = profile.getProfileFileName();
                profileUrl = profile.getProfileUrl();
            }

            GetChatRoomDetailRes chatRoomDetailRes = new GetChatRoomDetailRes(
                    sender.getId(), sender.getNickName(), profileFileName, profileUrl,
                    new ArrayList<>(messageList), msg.getSendDate(), msg.getSendTime());

            chatRoomDetailResList.add(chatRoomDetailRes);
            messageList.clear();
        }
        return chatRoomDetailResList;
    }

    public boolean confirmPwd(PwdCheckReq pwdCheckReq) throws BaseException {
        String roomId = pwdCheckReq.getRoomId();
        String roomPwd;
        if(pwdCheckReq.getRoomPwd() == null) {
             roomPwd = null;
        } else {
            roomPwd = pwdCheckReq.getRoomPwd();
        }
        try {
            ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(roomId);
            String decryptedPwd;
            if(chatRoom.getRoomPassword() == null) {
                decryptedPwd = null;
            }
            else {
                decryptedPwd = new AES128(Secret.ROOM_PASSWORD_KEY).decrypt(chatRoom.getRoomPassword()); // 복호화
            }

            if (decryptedPwd == null && roomPwd == null) {
                return true; // 둘 다 null이면 일치로 처리
            } else if (decryptedPwd == null || roomPwd == null) {
                return false; // 한 쪽이 null인 경우 일치하지 않음
            }
            return decryptedPwd.equals(roomPwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }
    }

    public boolean checkAvailableRoom(String roomId) throws BaseException{
        try {
            ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(roomId);
            if(chatRoom.getUserCount() + 1 > chatRoom.getMaxUserCount()) {
                return false;
            }
            return true;
        } catch (BaseException exception) {
            throw new BaseException(NONE_EXIST_ROOM);
        }
    }

    // 채팅방 나가기
    @Transactional
    public String exitChatRoom(Long userId, String roomId) throws BaseException {
        utilService.findChatRoomByChatRoomIdWithValidation(roomId);
        userChatRoomRepository.deleteUserChatRoomByUserIdWithRoomId(userId, roomId);
        minusUserCount(roomId); // 프론트의 클라이언트 코드에서 웹 소켓 연결을 끊어주어야 한다.
        ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(roomId);
        if (chatRoom.getUserCount() == 0) { // 채팅방에 아무도 안 남게 되면 Repository에서 삭제
            userChatRoomRepository.deleteUserChatRoomsByRoomId(roomId);
            textMessageRepository.deleteMessageByRoomId(roomId);
            chatRoomRepository.deleteChatRoomById(roomId);
        }
        // 만약 사진 업로드 기능을 추가한다면 S3에 올라간 파일도 삭제해주어야 함
        User user = utilService.findByUserIdWithValidation(userId);
        String result = user.getNickName() + "님이 " + roomId + "번 채팅방을 나갔습니다.";
        return result;
    }

    /** <-- test 환경을 위해 사용할 서비스, Production 환경에서는 사용하지 않는다. 다만 ChatRoom API를 테스트해보기 위해 정의하는 메서드이다.(예외처리 안함) --> **/
    @Transactional
    public String sendMessage(Long userId, PostMessageReq postMessageReq) throws BaseException {
        // 블랙 유저 검증
        reportService.checkBlackUser("message",userId);

        User user = utilService.findByUserIdWithValidation(userId);
        ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(postMessageReq.getRoomId());
        String msg = postMessageReq.getMessage();
        DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm"); // 채팅방에 초단위는 안 나오도록 formatting
        String formattedTime = LocalDateTime.now().format(formatTime);
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = LocalDateTime.now().format(formatDate);
        TextMessage textMessage = TextMessage.builder()
                .textMessageType(postMessageReq.getTextMessageType())
                .message(msg)
                .sendDate(formattedDate)
                .sendTime(formattedTime)
                .sender(user)
                .chatRoom(chatRoom)
                .build();
        textMessageRepository.save(textMessage);
        return "메시지 전송이 완료되었습니다.";
    }

    @Transactional
    public String addUserTest(AddUserReq addUserReq) throws BaseException {
        try {
            ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(addUserReq.getRoomId());
            User user = utilService.findByUserIdWithValidation(addUserReq.getUserId());
            if(!checkAvailableRoom(addUserReq.getRoomId())) {
                throw new BaseException(OVER_CAPACITY);
            }
            UserChatRoom userChatRoom = UserChatRoom.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .build();
            userChatRoomRepository.save(userChatRoom);
            plusUserCount(addUserReq.getRoomId());
            PostMessageReq postMessageReq = new PostMessageReq(TextMessage.TextMessageType.ENTER,
                    addUserReq.getRoomId(), user.getNickName() + "님이 입장하셨습니다.");
            return sendMessage(user.getId(), postMessageReq);
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }

    @Transactional
    public String reportWriter(Long reporterId, Long messageId, String reason) throws BaseException{
        // reason 값은 나중에 푸시 또는 알림 창에 왜 신고를 당했는지 보여주는 용도로 사용할 예정
        TextMessage textMessage = utilService.findByTextMessageIdWithValidation(messageId);
        User reported = textMessage.getSender(); // 게시글의 작성자
        User reporter = utilService.findByUserIdWithValidation(reporterId);
        Report report = new Report();

        // 신고 검증 (중복 신고, 자기 자신 신고)
        reportService.chkReportValidation(reporterId,reported.getId(),0L,0L,messageId);

        reportRepository.save(report.createReport(reason, null, null, messageId, reporter, reported));

        // 메세지 누적 신고 횟수에 따른 처리
        reportService.updateReport(ADD_REPORT_FOR_MESSAGE, reported);

        int cumulativeReportCount = reportService.findCumulativeReportCount(reported,1);

        LocalDateTime now = LocalDateTime.now(); // 현재 시간
        String prefix = "message";
        switch (cumulativeReportCount) {
            case 4 -> // 누적 신고 횟수 4
            {
                reportService.setReportExpiration(prefix,reported, now.plus(3, ChronoUnit.DAYS), UNABLE_TO_MESSAGE_THREE.name());
                sqsService.sendMessage(reported, 3, "채팅 금지");
            }
            case 8 -> // 누적 신고 횟수 8
            {
                reportService.setReportExpiration(prefix,reported, now.plus(5, ChronoUnit.DAYS), UNABLE_TO_MESSAGE_FIVE.name());
                sqsService.sendMessage(reported, 5, "채팅 금지");
            }
            case 12 -> // 누적 신고 횟수 12
            {
                reportService.setReportExpiration(prefix,reported, now.plus(7, ChronoUnit.DAYS), UNABLE_TO_MESSAGE_SEVEN.name());
                sqsService.sendMessage(reported, 7, "채팅 금지");
            }
            case 16 -> // 누적 신고 횟수 16
            {
                reportService.setReportExpiration(prefix,reported, now.plus(14, ChronoUnit.DAYS), UNABLE_TO_MESSAGE_FOURTEEN.name());
                sqsService.sendMessage(reported, 14, "채팅 금지");
            }
            case 20 -> // 누적 신고 횟수 20
            {
                reportService.setReportExpiration(prefix,reported, now.plus(30, ChronoUnit.DAYS), UNABLE_TO_MESSAGE_MONTH.name());
                sqsService.sendMessage(reported, 30, "채팅 금지");
            }
            case 21 -> // 누적 신고 횟수 21
            {
                reported.setStatus(UserStatus.INACTIVE); // 영구 정지
                sqsService.sendMessage(reported, -1, "영구 정지");
            }
        }

        return "메세지 작성자에 대한 신고 처리가 완료되었습니다.";
    }

    @Transactional
    public List<GetChatRoomRes> findChatRooms(Long userId, String text) {
        List<GetChatRoomRes> resultList = new ArrayList<>();

        // 채팅방 이름으로 검색
        List<ChatRoom> chatRoomsByRoomName = chatRoomRepository.findChatRoomByRoomName(userId, text);
        resultList.addAll(findChatRooms(chatRoomsByRoomName));

        // 유저 닉네임으로 검색
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findUserListByUserId(userId);
        List<ChatRoom> chatRoomsByNickName = chatRoomRepository.findChatRoomByNickName(userId, text);
        resultList.addAll(findChatRooms(chatRoomsByNickName));

        List<GetChatRoomRes> resultListWithoutDuplicate = resultList.stream()
                .distinct()
                .collect(Collectors.toList());

        return resultListWithoutDuplicate;
    }

    private List<GetChatRoomRes> findChatRooms(List<ChatRoom> chatRoomList) {
        List<GetChatRoomRes> resultList = new ArrayList<>();
        for (ChatRoom chatRoom : chatRoomList) {
            List<TextMessage> textMessages = textMessageRepository.findMessagesByRoomId(chatRoom.getChatRoomId());
            String latestMessage = "";
            String latestDate = "";
            String latestTime = "";
            int unreadCount = 0;
            if (!textMessages.isEmpty()) {
                TextMessage lastMessage = textMessages.get(textMessages.size() - 1);
                latestMessage = lastMessage.getMessage();
                latestDate = lastMessage.getSendDate();
                latestTime = lastMessage.getSendTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime localTime = LocalTime.parse(latestTime, formatter);
                latestTime = UtilService.formatTime(localTime);
                for(TextMessage textMessage : textMessages) {
                    if(!textMessage.isRead()) {
                        unreadCount++;
                    }
                }
            }
            resultList.add(new GetChatRoomRes(chatRoom.getChatRoomId(), chatRoom.getRoomName(),
                    latestMessage, latestDate, latestTime, unreadCount));
        }
        return resultList;
    }
}