package com.example.hello_there.chat_room;

import com.example.hello_there.chat_room.dto.*;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;
    private final JwtService jwtService;
    private final UtilService utilService;

    // 채팅방 생성
    @PostMapping("/room")
    public BaseResponse<String> CreateChatRoom(@RequestBody PostChatRoomReq postChatRoomReq) {
        try {
            Long userId = jwtService.getUserIdx();
            ChatRoom chatRoom = chatRoomService.createChatRoom(userId, postChatRoomReq);
            String result = chatRoom.getChatRoomId() + "번 채팅방을 생성하였습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 내가 속한 채팅방 리스트 반환
    @GetMapping("/room")
    public BaseResponse<List<GetChatRoomRes>> getChatRoomList() {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(chatRoomService.getChatRoomListById(userId));

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 채팅에 참여한 유저 리스트 반환, 채팅방 안에서 호출
    @GetMapping("/room/{roomId}")
    public BaseResponse<List<GetUserRes>> getUserList(@PathVariable String roomId) {
        try {
            return new BaseResponse<>(chatRoomService.getUserListById(roomId));

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }


    // 채팅방 secretChk가 false인 경우 바로 채팅방으로 리다이렉트
    // 채팅방 secretChk가 true인 경우, 비밀번호 확인창이 열린다.
    // 비밀번호가 일치하는 경우에만 채팅방으로 리다이렉트, 불일치할 경우 재시도
    @PostMapping("/room/pwd")
    public BaseResponse<Boolean> confirmPwd(@RequestBody PwdCheckReq pwdCheckReq){
        try {
            ChatRoom chatRoom = utilService.findChatRoomByChatRoomIdWithValidation(pwdCheckReq.getRoomId());
            if(chatRoom.isSecretChk() == false) {
                return new BaseResponse<>(true);
                // 또는 채팅방 ID를 Path Variable로 넣어서 페이지 리다이렉트를 반환해도 된다.
            }
            // 입력받은 roomPwd를 복호화된 DB의 패스워드와 비교해서 맞으면 true, 아니면 false
            return new BaseResponse<>(chatRoomService.confirmPwd(pwdCheckReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 채팅방에 입장할 때 고정적으로 아래의 API를 호출
    // (최대 인원 수 제한을 고려하여) 채팅방 입장 가능여부 확인
    // false면 입장 불가를 알리고, true면, /room/pwd로 redirect
    @GetMapping("/chkAvailable/{roomId}")
    public BaseResponse<Boolean> chkAvailable(@PathVariable String roomId){
        try {
            return new BaseResponse<>(chatRoomService.checkAvailableRoom(roomId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 본인을 제외한 현재 로그인한 유저의 리스트를 반환
    @GetMapping("/")
    public BaseResponse<List<GetLoginUserRes>> goChatRoom() {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(chatRoomService.getLoginUsers(userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 원래 있던 채팅방에 다시 입장할 때 화면에 나와야 할 정보를 반환
    @GetMapping("/room/enter/{roomId}")
    public BaseResponse<List<GetChatRoomDetailRes>> getChatRoomDetails(@PathVariable String roomId) {
        try {
            return new BaseResponse<>(chatRoomService.getChatRoomDetails(roomId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 채팅방 나가기
    @DeleteMapping("/room/{roomId}")
    public BaseResponse<String> deleteChatRoom(@PathVariable String roomId){
        // roomId 기준으로 chatRoom 삭제, 해당 채팅룸 안에 있는 사진, 메시지 삭제
        try {
            Long userId = jwtService.getUserIdx();
            chatRoomService.deleteChatRoom(userId, roomId);
            User user = utilService.findByUserIdWithValidation(userId);
            String result = user.getNickName() + "님이 " + roomId + "번 채팅방을 나갔습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}

