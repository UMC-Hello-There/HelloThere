package com.example.hello_there.chat;

import com.example.hello_there.chat.dto.GetChatRoomDetailRes;
import com.example.hello_there.chat.dto.GetChatRoomRes;
import com.example.hello_there.chat.dto.PostChatRoomReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.message.dto.PostMessageRes;
import com.example.hello_there.message.Message;
import com.example.hello_there.message.MessageService;
import com.example.hello_there.message.dto.PostMessageReq;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    @MessageMapping("/chat/send")
    public BaseResponse<String> chat(PostMessageReq postMessageReq) {
        try {
            Message message = messageService.sendMessage(postMessageReq);
            messagingTemplate.convertAndSend("/topic/chat/" + message.getReceiver().getId(), message);
            String result = "메시지가 전송되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PostMapping("/chat/room")
    public BaseResponse<String> JoinChatRoom(@RequestBody PostChatRoomReq postChatRoomReq) {
        try {
            Long roomId = chatRoomService.joinChatRoom(postChatRoomReq);
            String result = roomId + "번 채팅방에 입장하셨습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @GetMapping("/chat/room")
    public BaseResponse<List<GetChatRoomRes>> getChatRoomList(@RequestParam Long memberId) {
        try {
            return new BaseResponse<>(chatRoomService.getChatRooms(memberId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @GetMapping("/chat/room/{roomId}")
    public BaseResponse<GetChatRoomDetailRes> getChatRoomDetail(@PathVariable Long chatRoomId) {
        try {
            return new BaseResponse<>(chatRoomService.getChatRoomsDetail(chatRoomId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}

