package com.example.hello_there.board;

import com.example.hello_there.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("select b from Board b where b.boardId = :boardId")
    Optional<Board> findBoardById(@Param("boardId") Long boardId);

    @Query("select b from Board b where b.title = :title")
    List<Board> findBoardByTitle(@Param("title") String title);

    @Query("select b from Board b where b.title = :title and b.user.id =:userId")
    List<Board> findBoardByTitle(@Param("title") String title, @Param("userId") Long userId);

    @Query("select b from Board b where b.user.id = :id")
    List<Board> findBoardByUserId(@Param("id") Long id);

    @Query("select b from Board b")
    List<Board> findBoards();

    @Modifying
    @Query("UPDATE Board b SET b.view = b.view + 1 WHERE b.boardId = :boardId")
    void incrementViewsCountById(@Param("boardId") Long boardId);

    @Modifying
    @Query("delete from Board b where b.boardId = :boardId")
    void deleteBoard(@Param("boardId") Long boardId);
}

