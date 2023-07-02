package com.example.hello_there.board;

import lombok.Getter;

@Getter
public enum BoardType {
    FREE_BOARD, // 자유 소통 게시판
    CONFLICT_BOARD, // 갈등관리 소통 게시판
    SHARE_BOARD, // 공구, 나눔 게시판
    MARKET_PLACE_BOARD, // 중고 장터 게시판
    INFORMATION_BOARD; // 정보 게시판
}
