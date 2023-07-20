package com.example.hello_there.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId; // 댓글 식별자

    @Column(nullable = false)
    private String content; // 댓글 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @Nullable
    private Comment parent;

    //댓글과 대댓글의 그룹 ID
    private Long groupId;

    @ColumnDefault("false")
    private boolean isDeleted;


    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    private List<LikeComment> likeComments = new ArrayList<>();

    @Builder
    public Comment(Board board, User user,String content, Comment parent,Long groupId) {
        this.board = board;
        this.user = user;
        this.content = content;
        this.groupId = groupId;
        this.parent = parent;
    }

    //**비즈니스 로직**//

    public void addGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void addParentComment(Comment parent){
        this.parent = parent;
    }

    public void updateComment(String content) {
        this.content = content;
    }

    public void changeIsDeleted(){ this.isDeleted = true;}
}