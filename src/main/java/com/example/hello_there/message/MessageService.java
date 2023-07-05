package com.example.hello_there.message;

import com.example.hello_there.chat.ChatRoom;
import com.example.hello_there.chat.personal_chat.PersonalChatRoom;
import com.example.hello_there.chat.personal_chat.PersonalChatRoomRepository;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.message.dto.PostMessageReq;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final PersonalChatRoomRepository personalChatRoomRepository;
    private final MessageRepository messageRepository;
    private final UtilService utilService;

    @Transactional(rollbackFor = Exception.class)
    public Message sendPersonalMessage(Long senderId, PostMessageReq postMessageReq) throws BaseException {
        String message = postMessageReq.getMessage();
        Long receiverId = postMessageReq.getReceiverId();
        User sender = utilService.findByUserIdWithValidation(senderId);
        User receiver = utilService.findByUserIdWithValidation(receiverId);
        PersonalChatRoom personalChatRoom = personalChatRoomRepository.findChatRoomsByInviterAndParticipant(senderId, receiverId).orElse(null);
        if(personalChatRoom != null) { // 두 사람이 처음 대화하는 경우
            Message msg = Message.builder()
                    .message(message)
                    .sender(sender)
                    .chatRoom(chatRoom)
                    .receiver(receiver)
                    .build();
            messageRepository.save(msg);
            return msg;
        }
        else { // 대화를 나눈 이력이 있는 경우

        }
    }
}
