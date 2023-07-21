package com.example.hello_there.comment.likecomment;

import com.example.hello_there.comment.Comment;
import com.example.hello_there.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentId")
    private Comment comment;

    @Builder
    public LikeComment(User user, Comment comment){
        this.user = user;
        this.comment = comment;
        addLikeComment();
    }

    /** 연관관계 메서드**/
    public void addLikeComment(){
        comment.getLikeComments().add(this);
    }
}
