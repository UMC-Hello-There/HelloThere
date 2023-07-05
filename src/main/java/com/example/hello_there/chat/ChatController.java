package com.example.hello_there.chat;

import com.example.hello_there.chat.dto.GetChatRoomRes;
import com.example.hello_there.chat.dto.PostPersonalChatRoomReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatRoomService chatRoomService;
    private final JwtService jwtService;
    private final UtilService utilService;

    @PostMapping("/chat/room")
    public BaseResponse<String> JoinChatRoom(@RequestParam Long participantId) {
        try {
            Long inviterId = jwtService.getUserIdx();
            PostPersonalChatRoomReq postPersonalChatRoomReq = new PostPersonalChatRoomReq(participantId, inviterId);
            Long roomId = chatRoomService.joinChatRoom(postPersonalChatRoomReq);
            User participant = utilService.findByUserIdWithValidation(participantId);
            String result = roomId + "번 채팅방에서 " + participant.getNickName() + "님과 대화 중입니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @GetMapping("/chat/room")
    public BaseResponse<List<GetChatRoomRes>> getChatRoomList() {
        try { // 자신의 채팅방 리스트만 조회할 수 있다.
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(chatRoomService.getChatRooms(userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @GetMapping("/chat/room/{roomId}")
    public BaseResponse<GetChatRoomRes> getChatRoomById(@PathVariable Long roomId) {
        try {
            return new BaseResponse<>(chatRoomService.getChatRoomsById(roomId));

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}

