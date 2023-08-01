package com.example.hello_there.user_notice;

import com.example.hello_there.chat_room.ChatRoom;
import com.example.hello_there.notice.Notice;
import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserNotice { // 유저와 알림의 다대다 관계 매핑을 위한 연결 클래스
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNoticeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    public void setNotice(Notice notice){
        this.notice = notice;
    }

    public void setUser(User user){
        this.user = user;
    }
}
