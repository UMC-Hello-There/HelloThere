package com.example.hello_there.chat;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.dto.GetBoardRes;
import com.example.hello_there.chat.dto.GetChatRoomDetailRes;
import com.example.hello_there.chat.dto.GetChatRoomRes;
import com.example.hello_there.chat.dto.PostChatRoomReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UtilService utilService;

    @Transactional(rollbackFor = Exception.class)
    public Long joinChatRoom(PostChatRoomReq postChatRoomReq) throws BaseException {
        if(postChatRoomReq.getInviterId().equals(postChatRoomReq.getParticipantId())) {
            throw new BaseException(CANNOT_CREATE_ROOM);
        }
        ChatRoom chatRoom = chatRoomRepository.findChatRoomsByInviterAndParticipant(
                postChatRoomReq.getInviterId(),
                postChatRoomReq.getParticipantId()).orElse(null);
        if(chatRoom == null) {
            User participant = utilService.findByUserIdWithValidation(postChatRoomReq.getParticipantId());
            User inviter = utilService.findByUserIdWithValidation(postChatRoomReq.getInviterId());
            chatRoom = ChatRoom.builder()
                    .participant(participant)
                    .inviter(inviter)
                    .build();
        }
        chatRoomRepository.save(chatRoom);
        return chatRoom.getChatRoomId();
    }

    @Transactional(readOnly = true)
    public List<GetChatRoomRes> getChatRooms(Long memberId) throws BaseException{
        try{
            List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserId(memberId);
            List<GetChatRoomRes> getChatRoomRes = chatRooms.stream()
                    .map(chatRoom -> new GetChatRoomRes(chatRoom.getChatRoomId(), chatRoom.getInviter(),
                            chatRoom.getParticipant()))
                    .collect(Collectors.toList());
            return getChatRoomRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public GetChatRoomDetailRes getChatRoomsDetail(Long chatRoomId) throws BaseException {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId).orElse(null);
        if(chatRoom == null) {
            throw new BaseException(NONE_EXIST_ROOM);
        }
        GetChatRoomDetailRes getChatRoomDetailRes = new GetChatRoomDetailRes(chatRoom.getChatRoomId(),
                chatRoom.getParticipant(), chatRoom.getInviter(), chatRoom.getMessageList());
        return getChatRoomDetailRes;
    }
}