package com.example.hello_there.board;

import com.example.hello_there.board.dto.DeleteBoardReq;
import com.example.hello_there.board.dto.PatchBoardReq;
import com.example.hello_there.board.dto.PostBoardReq;
import com.example.hello_there.board.photo.PostPhoto;
import com.example.hello_there.board.photo.PostPhotoRepository;
import com.example.hello_there.board.photo.PostPhotoService;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.S3Service;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final UtilService utilService;
    private final S3Service s3Service;
    private final PostPhotoService postPhotoService;
    public void save(Board board) {
        BoardType boardType = board.getBoardType();
        if (boardType == BoardType.FREE_BOARD) {
            board.updateBoardType(BoardType.FREE_BOARD);
            boardRepository.save(board);
        } else if (boardType == BoardType.CONFLICT_BOARD) {
            board.updateBoardType(BoardType.FREE_BOARD);
            boardRepository.save(board);
        }
    }

    @Transactional
    public String createBoard(Long userId, PostBoardReq postBoardReq, List<MultipartFile> multipartFiles) throws BaseException {
        User user = utilService.findByUserIdWithValidation(userId);
        Board board = Board.builder()
                .title(postBoardReq.getTitle())
                .content(postBoardReq.getContent())
                .photoList(new ArrayList<>())
                .user(user)
                // .commentList(new ArrayList<>())
                .build();
        save(board);

        if(multipartFiles != null) {
            List<GetS3Res> getS3ResList = s3Service.uploadFile(multipartFiles);
            postPhotoService.saveAllPostPhotoByBoard(getS3ResList, board);
        }

        return "boardId: " + board.getBoardId() + "인 게시글을 생성했습니다.";
    }

    @Transactional
    public Board getBoard(Long boardId) throws BaseException{
        Board board = utilService.findByBoardIdWithValidation(boardId);
        return board;
    }

    @Transactional
    public String deleteBoard(DeleteBoardReq deleteBoardReq) throws BaseException {
        Board deleteBoard = utilService.findByBoardIdWithValidation(deleteBoardReq.getBoardId());
        User writer = deleteBoard.getUser();
        User visitor = utilService.findByUserIdWithValidation(deleteBoardReq.getUserId());
        if(writer.getId() == visitor.getId()) {
            // S3에 업로드된 파일을 삭제하는 명령
            List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(deleteBoard.getBoardId());
            postPhotoService.deleteAllPostPhotos(allByBoardId);
            // PostPhotoRepository에서 삭제하는 명령
            List<Long> ids = postPhotoService.findAllId(deleteBoard.getBoardId());
            postPhotoService.deleteAllPostPhotoByBoard(ids);
            // 아래의 JPQL 쿼리로 한 번에 PostPhoto들을 삭제하는 것도 가능.
            // postPhotoRepository.deletePostPhotoByBoardId(deleteBoardReq.getBoardId());

            // 게시글을 삭제하는 명령
            boardRepository.deleteBoard(deleteBoard.getBoardId());
            String result = "요청하신 게시글에 대한 삭제가 완료되었습니다.";
            return result;
        }
        else {
            throw new BaseException(MEMBER_WITHOUT_PERMISSION);
        }
    }

    @Transactional
    public String modifyBoard(Long userId, Long boardId, PatchBoardReq patchBoardReq,
                              List<MultipartFile> multipartFiles) throws BaseException {
        try {
            Board board = utilService.findByBoardIdWithValidation(boardId);
            User writer = board.getUser();
            User visitor = utilService.findByUserIdWithValidation(userId);
            if(writer.getId() == visitor.getId()){
                board.updateBoard(patchBoardReq.getTitle(), patchBoardReq.getContent());
                //사진 업데이트, 지우고 다시 저장!
                List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(boardId);
                postPhotoService.deleteAllPostPhotos(allByBoardId);
                List<Long> ids = postPhotoService.findAllId(board.getBoardId());
                postPhotoService.deleteAllPostPhotoByBoard(ids);

                if(multipartFiles != null) {
                    List<GetS3Res> getS3ResList = s3Service.uploadFile(multipartFiles);
                    postPhotoService.saveAllPostPhotoByBoard(getS3ResList, board);
                }

                return "boardId " + board.getBoardId() + "의 게시글을 수정했습니다.";
            }
            else {
                throw new BaseException(MEMBER_WITHOUT_PERMISSION);
            }
        } catch(BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }
}

