package com.example.hello_there.board.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostPhotoRepository  extends JpaRepository<PostPhoto, Long> {


    @Query("select p from PostPhoto p where p.board.boardId = :boardId")
    Optional<List<PostPhoto>> findAllByBoardId(@Param("boardId") Long boardId);


    @Query("select pp.photoId from PostPhoto pp where pp.board.boardId = :boardId")
    List<Long> findAllId(@Param("boardId") Long boardId);

    @Query("select pp.imgUrl from PostPhoto pp where pp.board.boardId = :boardId")
    List<String> findAllPhotos(@Param("boardId") Long boardId);

    @Modifying
    @Query("delete from PostPhoto pp where pp.photoId in :ids")
    void deleteAllByBoard(@Param("ids") List<Long> ids);

    @Modifying
    @Query("delete from PostPhoto pp where pp.board.boardId = :boardId")
    void deletePostPhotoByBoardId(@Param("boardId") Long boardId);
}

