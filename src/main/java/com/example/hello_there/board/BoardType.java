package com.example.hello_there.board;

import lombok.Getter;

@Getter
public enum BoardType {
    FREE_BOARD(1), // 자유 소통 게시판
    CONFLICT_BOARD(2), // 갈등관리 소통 게시판
    SHARE_BOARD(3), // 공구, 나눔 게시판
    MARKET_PLACE_BOARD(4), // 중고 장터 게시판
    INFORMATION_BOARD(5), // 정보 게시판
    QUESTION_BOARD(6); // 질문 게시판

    private int value;

    BoardType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
