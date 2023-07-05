package com.example.hello_there.chat;

import com.example.hello_there.chat.dto.GetChatRoomRes;
import com.example.hello_there.chat.dto.PostPersonalChatRoomReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UtilService utilService;

    @Transactional(rollbackFor = Exception.class)
    public Long joinChatRoom(PostPersonalChatRoomReq postPersonalChatRoomReq) throws BaseException {
        if(postPersonalChatRoomReq.getInviterId().equals(postPersonalChatRoomReq.getParticipantId())) {
            // 자기 자신과의 채팅방 기능은 제공하지 않는다.
            throw new BaseException(CANNOT_CREATE_ROOM);
        }
        // 대화 상대와 이미 채팅방이 존재하는 경우를 판별.
        // inviter-participant 쌍의 채팅방이 이미 있다면 새로운 채팅방을 만들 필요가 없다.
        ChatRoom chatRoom = chatRoomRepository.findChatRoomsByInviterAndParticipant(
                postPersonalChatRoomReq.getInviterId(),
                postPersonalChatRoomReq.getParticipantId()).orElse(null);
        if(chatRoom == null) { // 처음 대화하는 경우
            User participant = utilService.findByUserIdWithValidation(postPersonalChatRoomReq.getParticipantId());
            User inviter = utilService.findByUserIdWithValidation(postPersonalChatRoomReq.getInviterId());
            chatRoom = ChatRoom.builder()
                    .participant(participant)
                    .inviter(inviter)
                    .build();
        }
        chatRoomRepository.save(chatRoom);
        return chatRoom.getChatRoomId();
    }

    @Transactional(readOnly = true)
    public List<GetChatRoomRes> getChatRooms(Long userId) throws BaseException{
        try{
            List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserId(userId);
            List<GetChatRoomRes> getChatRoomRes = chatRooms.stream()
                    .map(chatRoom -> new GetChatRoomRes(chatRoom.getChatRoomId(), chatRoom.getInviter().getId(),
                            chatRoom.getInviter().getNickName(), chatRoom.getParticipant().getId(),
                            chatRoom.getParticipant().getNickName(), chatRoom.getMessageList()))
                    .collect(Collectors.toList());
            return getChatRoomRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public GetChatRoomRes getChatRoomsById(Long chatRoomId) throws BaseException {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId).orElse(null);
        if(chatRoom == null) {
            throw new BaseException(NONE_EXIST_ROOM);
        }
        User inviter = chatRoom.getInviter();
        User participant = chatRoom.getParticipant();
        GetChatRoomRes getChatRoomDetailRes = new GetChatRoomRes(chatRoom.getChatRoomId(), inviter.getId(),
                inviter.getNickName(), participant.getId(), participant.getNickName(), chatRoom.getMessageList());
        return getChatRoomDetailRes;
    }
}