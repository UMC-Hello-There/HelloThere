package com.example.hello_there.user_notice;

import com.example.hello_there.user_chatroom.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNoticeRepository extends JpaRepository<UserNotice, Long> {
    @Query("select un from UserNotice un where un.user.id = :userId")
    List<UserNotice> findUserNoticeByUserId(@Param("userId") Long userId);
}
