package com.example.hello_there.board.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardType;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.BaseTimeEntity;
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
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId; // 댓글 식별자

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String reply; // 댓글 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    public void updateComment(String content) {
        this.reply = content;
    }
}

