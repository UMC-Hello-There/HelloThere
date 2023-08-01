package com.example.hello_there.chat_room;

import com.example.hello_there.text_message.TextMessage;
import com.example.hello_there.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    @Query("select c from ChatRoom c " +
            "INNER JOIN UserChatRoom uc ON c.chatRoomId = uc.chatRoom.chatRoomId " +
            "where uc.user.id = :userId and c.roomName Like %:text%")
    List<ChatRoom> findChatRoomByRoomName(@Param("userId") Long userId, @Param("text") String text);

    @Query("select c from ChatRoom c " +
            "INNER JOIN UserChatRoom uc ON c.chatRoomId = uc.chatRoom.chatRoomId " +
            "where uc.user.id = :userId and uc.user.nickName Like %:text%")
    List<ChatRoom> findChatRoomByNickName(@Param("userId") Long userId, @Param("text") String text);

    @Query("select c from ChatRoom c " +
            "INNER JOIN UserChatRoom uc ON c.chatRoomId = uc.chatRoom.chatRoomId " +
            "where uc.user.id = :userId")
    List<ChatRoom> findChatRoomByUserId(@Param("userId") Long userId);

    @Query("select c from ChatRoom c where c.chatRoomId = :chatRoomId")
    Optional<ChatRoom> findChatRoomById(@Param("chatRoomId") String chatRoomId);

    @Modifying
    @Query("delete from ChatRoom c where c.chatRoomId = :chatRoomId")
    void deleteChatRoomById(@Param("chatRoomId") String chatRoomId);
}
