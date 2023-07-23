package com.example.hello_there.board.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeBoardRepository extends JpaRepository<LikeBoard, Long> {
    Boolean existsByBoard_BoardIdAndUserId(Long boardId, Long userId);
    Optional<LikeBoard> findByBoard_BoardIdAndUserId(Long boardId, Long userId);
    Long countByBoardBoardId(Long board);
}
