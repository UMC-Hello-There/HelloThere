package com.example.hello_there.board;

import com.example.hello_there.board.photo.PostPhoto;
import com.example.hello_there.user.User;
import com.example.hello_there.utils.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Board extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private BoardType boardType;

    // 멤버와 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 게시 사진과 관계매핑
    @OneToMany(mappedBy = "board", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<PostPhoto> photoList = new ArrayList<>();

    public void updateBoard(String title, String content){
        this.title = title;
        this.content = content;
    }

    public void updateBoardType(BoardType boardType){
        this.boardType = boardType;
    }

    public void addPhotoList(PostPhoto postPhoto){
        photoList.add(postPhoto);
        postPhoto.setBoard(this);
    }
}
