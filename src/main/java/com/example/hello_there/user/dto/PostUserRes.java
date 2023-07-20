package com.example.hello_there.user.dto;

import com.example.hello_there.comment.Comment;
import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostUserRes {
    private Long userId;
    private String nickName;

    public PostUserRes(User user){
        this.userId = user.getId();
        this.nickName = user.getNickName();
    }
}