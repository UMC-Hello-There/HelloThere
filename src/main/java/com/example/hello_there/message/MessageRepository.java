package com.example.hello_there.message;

import com.example.hello_there.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("select m from Message m where m.chatRoom.chatRoomId = :roomId")
    List<Message> findMessagesByRoomId(@Param("roomId") String roomId);

    @Modifying
    @Query("delete from Message m where m.chatRoom.chatRoomId = :roomId")
    void deleteMessageByRoomId(@Param("roomId") String roomId);
}
