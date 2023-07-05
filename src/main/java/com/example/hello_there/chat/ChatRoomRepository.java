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

    // 꼭 inviter와 participant가 일치할 필요는 없고, 이미 존재하는 쌍인지만 판별
    @Query("SELECT c FROM ChatRoom c WHERE (c.inviter.id = :inviterId AND c.participant.id = :participantId) OR (c.inviter.id = :participantId AND c.participant.id = :inviterId)")
    Optional<ChatRoom> findChatRoomsByInviterAndParticipant(@Param("inviterId") Long inviterId, @Param("participantId") Long participantId);
}
