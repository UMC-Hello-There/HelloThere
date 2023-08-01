package com.example.hello_there.board;

import com.example.hello_there.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findAllByBoardTypeAndHouse_HouseIdOrderByBoardIdDesc(BoardType boardType, Long houseId);

    @Query("SELECT b FROM Board b WHERE (b.title LIKE %:keyword% OR b.content LIKE %:keyword%) AND b.house.houseId = :houseId ORDER BY b.boardId DESC")
    List<Board> findBoardsByTitleOrContentContainingAndHouseId(@Param("keyword") String keyword, @Param("houseId") Long houseId);

    @Query(value =
            "(SELECT * FROM board WHERE board_type = 'FREE_BOARD' AND house_id = :houseId ORDER BY board_id DESC LIMIT 1) " +
                    "UNION ALL " +
                    "(SELECT * FROM board WHERE board_type = 'CONFLICT_BOARD' AND house_id = :houseId ORDER BY board_id DESC LIMIT 1) " +
                    "UNION ALL " +
                    "(SELECT * FROM board WHERE board_type = 'SHARE_BOARD' AND house_id = :houseId ORDER BY board_id DESC LIMIT 1) " +
                    "UNION ALL " +
                    "(SELECT * FROM board WHERE board_type = 'MARKET_PLACE_BOARD' AND house_id = :houseId ORDER BY board_id DESC LIMIT 1) " +
                    "UNION ALL " +
                    "(SELECT * FROM board WHERE board_type = 'INFORMATION_BOARD' AND house_id = :houseId ORDER BY board_id DESC LIMIT 1) " +
                    "UNION ALL " +
                    "(SELECT * FROM board WHERE board_type = 'QUESTION_BOARD' AND house_id = :houseId ORDER BY board_id DESC LIMIT 1) ",
            nativeQuery = true)
    List<Board> findBoardsWithMaxBoardIdForEachBoardType(@Param("houseId") Long houseId);

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

    @Modifying
    @Query("UPDATE Board b SET b.likeCount = b.likeCount + 1 WHERE b.boardId = :boardId")
    void incrementlikesCountById(@Param("boardId") Long boardId);

    @Modifying
    @Query("UPDATE Board b SET b.likeCount = b.likeCount - 1 WHERE b.boardId = :boardId")
    void decrementlikesCountById(@Param("boardId") Long boardId);

    @Modifying
    @Query("UPDATE Board b SET b.commentCount = b.commentCount + 1 WHERE b.boardId = :boardId")
    void incrementCommentsCountById(@Param("boardId") Long boardId);

    @Modifying
    @Query("UPDATE Board b SET b.commentCount = b.commentCount - 1 WHERE b.boardId = :boardId")
    void decrementCommentsCountById(@Param("boardId") Long boardId);
}

