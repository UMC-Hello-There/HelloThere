package com.example.hello_there.text_message;

import com.example.hello_there.chat_room.ChatRoomService;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.text_message.dto.AddUserReq;
import com.example.hello_there.text_message.dto.TextMessageDto;
import com.example.hello_there.text_message.dto.PostMessageReq;
import com.example.hello_there.text_message.dto.SendMessageReq;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user_chatroom.UserChatRoomRepository;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TextMessageController {
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations template;
    private final ChatRoomService chatRoomService;
    private final UtilService utilService;
    private final JwtService jwtService;

    // localhost:8080/pub/messageDto/enterUser와 같은 URI로 WebSocket 메시지를 전송하면,
    // 해당 요청이 서버에 도달하고, enterUser 메서드가 실행된다. 이는 클라이언트가 채팅방에 입장할 때 트리거되는 메서드이다.
    @MessageMapping("/messageDto/enter-user")
    public BaseResponse<String> enterUser(@Payload TextMessageDto textMessageDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 채팅방 유저+1
            chatRoomService.plusUserCount(textMessageDto.getRoomId());

            // 채팅방에 유저 추가
            chatRoomService.addUser(textMessageDto.getSenderId(), textMessageDto.getRoomId());
            Long userId = textMessageDto.getSenderId();
            // 반환 결과를 socket session 에 user ID 로 저장
            headerAccessor.getSessionAttributes().put("userId", userId); // getSessionAttributes()는 WebSocket 세션의 속성(attribute) 맵에 접근하는 메서드
            headerAccessor.getSessionAttributes().put("roomId", textMessageDto.getRoomId());
            User user = utilService.findByUserIdWithValidation(textMessageDto.getSenderId());
            textMessageDto.setMessage(user.getNickName() + " 님이 입장하셨습니다.");
            template.convertAndSend("/sub/messageDto/room/" + textMessageDto.getRoomId(), textMessageDto);
            return new BaseResponse<>("채팅방 입장 처리를 완료하였습니다.");
        } catch (BaseException ignored) {
            return new BaseResponse<>(BaseResponseStatus.FAILED_TO_ENTER);
        }
    }

    // 메시지를 보낼 때는 /sub/chat/room/{roomId}를 대상으로 메시지를 전송
    // 클라이언트가 localhost:8080/pub/chat/sendMessage와 같은 URI로 WebSocket 메시지를 전송
    @MessageMapping("/chat/send-message")
    public void sendMessage(@Payload TextMessageDto textMessageDto) {
        template.convertAndSend("/sub/chat/room/" + textMessageDto.getRoomId(), textMessageDto);

    }

    // 유저 퇴장 시에는 EventListener 을 통해서 유저 퇴장을 확인
    // WebSocket 세션이 끊어질 때(SessionDisconnectEvent) 트리거되는 메서드
    // URI에 직접 매핑되지 않고, WebSocket 세션이 끊어질 때 자동으로 호출된다.
    @EventListener
    public void webSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // stomp 세션에 있던 user id와 roomId 를 확인해서 채팅방 유저 리스트와 room 에서 해당 유저를 삭제
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        // 채팅방 유저 -1
        chatRoomService.minusUserCount(roomId);

        // 채팅방 유저 리스트에서 User ID 유저 닉네임 조회 및 리스트에서 유저 삭제
        // 만약 웹 소켓 연결이 끊어졌을 때 아예 채팅방에서 나가게끔 만들고 싶으면 주석 해제
        // userChatRoomRepository.deleteUserChatRoomByUserIdWithRoomId(userId, roomId);

        if (userId != null) {
            User user = userRepository.findUserById(userId).orElse(null);
            TextMessageDto textMessageDto = TextMessageDto.builder()
                    .type(TextMessage.TextMessageType.LEAVE)
                    .senderId(userId)
                    .message(user.getNickName() + " 님의 연결이 끊어졌습니다.")
                    .sendTime(LocalDateTime.now().toString())
                    .build();

            template.convertAndSend("/sub/chat/room/" + roomId, textMessageDto);
        }
    }

    /** <-- test 환경을 위해 사용할 API, Production 환경에서는 사용하지 않는다. 다만 ChatRoom API를 테스트해보기 위해 정의하는 API이다. --> **/

    @PostMapping("/message/send")
    public BaseResponse<String> SendMessageTest(@RequestBody SendMessageReq sendMessageReq) {
        try {
            Long userId = jwtService.getUserIdx();
            PostMessageReq postMessageReq = new PostMessageReq(TextMessage.TextMessageType.TALK,
                    sendMessageReq.getRoomId(), sendMessageReq.getMessage());
            return new BaseResponse<>(chatRoomService.sendMessage(userId, postMessageReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }


    @PostMapping("message/add")
    public BaseResponse<String> AddUserTest(@RequestBody AddUserReq addUserReq) {
        try {
            return new BaseResponse<>(chatRoomService.addUserTest(addUserReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PostMapping("/report/{messageId}")
    public BaseResponse<String> reportWriter(
            @PathVariable Long messageId,
            @RequestParam(required = false) String reason) {
        try {
            Long reporterId = jwtService.getUserIdx();
            return new BaseResponse<>(chatRoomService.reportWriter(reporterId,messageId,reason));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
