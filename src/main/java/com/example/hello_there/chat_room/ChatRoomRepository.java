package com.example.hello_there.chat_room;

import org.hibernate.annotations.ManyToAny;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    @Query("select c from ChatRoom c where c.chatRoomId = :chatRoomId")
    Optional<ChatRoom> findChatRoomById(@Param("chatRoomId") String chatRoomId);

    @Modifying
    @Query("delete from ChatRoom c where c.chatRoomId = :chatRoomId")
    void deleteChatRoomById(@Param("chatRoomId") String chatRoomId);
}
