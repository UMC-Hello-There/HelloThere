package com.example.hello_there.user.user_setting;

import com.example.hello_there.utils.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "user_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_check", nullable = false)
    private boolean commentCheck = true;    //댓글 알림 설정

    @Column(name = "recomment_check", nullable = false)
    private boolean recommentCheck = true;  //대댓글 알림 설정

    @Column(name = "message_check", nullable = false)
    private boolean messageCheck = true;    //쪽지 알림 설정

    @Column(name = "best_board_check", nullable = false)
    private boolean bestBoardCheck = true;  //인기 게시물 선정 알림 설정

    @Column(name = "message_reception_block", nullable = false)
    private boolean messageReceptionBlock = false;  //쪽지 수신 및 발신 제한 설정

    @Builder
    public UserSetting(Long userId, boolean commentCheck, boolean recommentCheck, boolean messageCheck, boolean bestBoardCheck, boolean messageReceptionBlock) {
        this.userId = userId;
        this.commentCheck = commentCheck;
        this.recommentCheck = recommentCheck;
        this.messageCheck = messageCheck;
        this.bestBoardCheck = bestBoardCheck;
        this.messageReceptionBlock = messageReceptionBlock;
    }

    public void setCommentCheck(Boolean commentCheck) {
        this.commentCheck = commentCheck;
    }

    public void setRecommentCheck(Boolean recommentCheck) {
        this.recommentCheck = recommentCheck;
    }

    public void setMessageCheck(Boolean messageCheck) {
        this.messageCheck = messageCheck;
    }

    public void setBestBoardCheck(Boolean bestBoardCheck) {
        this.bestBoardCheck = bestBoardCheck;
    }
}
