package com.example.hello_there.board.photo.dto;

import lombok.Data;

@Data
public class GetS3Res {
    private String imgUrl;
    private String fileName;

    public GetS3Res(String imgUrl, String fileName) {
        this.imgUrl = imgUrl;
        this.fileName = fileName;
    }
}
