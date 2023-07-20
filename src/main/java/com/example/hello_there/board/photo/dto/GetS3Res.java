package com.example.hello_there.board.photo.dto;

import com.example.hello_there.user.User;
import com.example.hello_there.user.dto.PostUserRes;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
@AllArgsConstructor
public class GetS3Res {
    private String imgUrl;
    private String fileName;
}
