package com.example.hello_there.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("select c from ChatRoom c where c.chatRoomId = :chatRoomId")
    Optional<ChatRoom> findChatRoomById(@Param("chatRoomId") Long chatRoomId);

    @Query("select c from ChatRoom c where c.inviter.id = :userId or c.participant.id = :userId")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

    @Query("select c from ChatRoom c where c.inviter.id = :inviterId and c.participant.id = :participantId")
    Optional<ChatRoom> findChatRoomsByInviterAndParticipant(@Param("inviterId") Long inviterId, @Param("participantId") Long participantId);
}
