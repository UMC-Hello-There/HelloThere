package com.example.hello_there.message;

import com.example.hello_there.chat.ChatRoom;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.message.dto.PostMessageReq;
import com.example.hello_there.message.dto.PostMessageRes;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UtilService utilService;

    @Transactional(rollbackFor = Exception.class)
    public Message sendMessage(PostMessageReq postMessageReq) throws BaseException {
        String message = postMessageReq.getMessage();
        Long senderId = postMessageReq.getSenderId();
        Long receiverId = postMessageReq.getReceiverId();
        Long chatRoomId = postMessageReq.getChatRoomId();
        User sender = utilService.findByUserIdWithValidation(senderId);
        User receiver = utilService.findByUserIdWithValidation(receiverId);
        ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(chatRoomId);
        Message msg = Message.builder()
                        .message(message)
                        .sender(sender)
                        .chatRoom(chatRoom)
                        .receiver(receiver)
                        .build();
        messageRepository.save(msg);
        return msg;
    }
}
