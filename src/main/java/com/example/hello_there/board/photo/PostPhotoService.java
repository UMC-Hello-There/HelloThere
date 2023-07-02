package com.example.hello_there.board.photo;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.utils.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostPhotoService {

    private final PostPhotoRepository postPhotoRepository;
    private final S3Service s3Service;

    @Transactional
    public void savePostPhoto(List<PostPhoto> postPhotos){
        postPhotoRepository.saveAll(postPhotos);
    }

    /**
     *  여러 개의 PostPhoto 저장
     */
    @Transactional
    public void saveAllPostPhotoByBoard(List<GetS3Res> getS3ResList , Board board) {
        // PostPhoto 리스트를 받아옴
        List<PostPhoto> postPhotos = new ArrayList<>();
        for (GetS3Res getS3Res : getS3ResList) {
            PostPhoto newPostPhoto = PostPhoto.builder()
                    .imgUrl(getS3Res.getImgUrl())
                    .fileName(getS3Res.getFileName())
                    .build();
            postPhotos.add(newPostPhoto);
            board.addPhotoList(newPostPhoto);
        }
        savePostPhoto(postPhotos);
    }

    /**
     * 게시글과 연관된 모든 postPhoto 삭제
     */
    @Transactional
    public void deleteAllPostPhotoByBoard(List<Long> ids){
        postPhotoRepository.deleteAllByBoard(ids);
    }

    @Transactional
    public void deleteAllPostPhotos(List<PostPhoto> postPhotos){
        for (PostPhoto postPhoto : postPhotos) {
            s3Service.deleteFile(postPhoto.getFileName());
        }
    }

    /**
     * 게시글과 연관된 모든 postPhoto 의 imgUrl 조회
     */
    public List<String> findAllPhotosByPostId(Long boardId){
        return postPhotoRepository.findAllPhotos(boardId);
    }

    /**
     * 게시글와 연관된 모든 id 조회
     */
    public List<Long> findAllId(Long boardId){
        return postPhotoRepository.findAllId(boardId);
    }

    public List<PostPhoto> findAllByBoardId(Long boardId){
        return postPhotoRepository.findAllByBoardId(boardId).orElse(null);
    }

    public String findFirstByPostId(Long boardId) {
        List<PostPhoto> postPhotos = postPhotoRepository.findAllByBoardId(boardId).orElse(null);

        if(postPhotos.size() == 0) {
            return "첨부된 사진이 없습니다.";
        } else {
            return postPhotos.get(0).getImgUrl();
        }
    }
}