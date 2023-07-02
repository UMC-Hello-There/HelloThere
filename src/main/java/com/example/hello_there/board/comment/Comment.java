package com.example.hello_there.board.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false)
    private String boardType; // 자유게시판, 갈등관리 게시판 등

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String reply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
    public void updateComment(String content) {
        this.reply = content;
    }
}

