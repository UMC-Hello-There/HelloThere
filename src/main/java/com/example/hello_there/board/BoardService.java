package com.example.hello_there.board;

import com.example.hello_there.board.comment.Comment;
import com.example.hello_there.board.comment.CommentRepository;
import com.example.hello_there.board.dto.DeleteBoardReq;
import com.example.hello_there.board.dto.GetBoardRes;
import com.example.hello_there.board.dto.PatchBoardReq;
import com.example.hello_there.board.dto.PostBoardReq;
import com.example.hello_there.board.photo.PostPhoto;
import com.example.hello_there.board.photo.PostPhotoRepository;
import com.example.hello_there.board.photo.PostPhotoService;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user.dto.GetUserRes;
import com.example.hello_there.utils.S3Service;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final CommentRepository commentRepository;

    @Transactional
    public void save(Board board) {
        boardRepository.save(board);
    }

    @Transactional
    public String createBoard(Long userId, PostBoardReq postBoardReq, List<MultipartFile> multipartFiles) throws BaseException {
        try {
            User user = utilService.findByUserIdWithValidation(userId);
            Board board = Board.builder()
                    .title(postBoardReq.getTitle())
                    .content(postBoardReq.getContent())
                    .boardType(postBoardReq.getBoardType())
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
        } catch (BaseException exception) {
            throw new BaseException(INVALID_JWT);
        }
    }

    @Transactional
    public List<GetBoardRes> getBoards() throws BaseException{
        try{
            List<Board> boards = boardRepository.findBoards();
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            board.getUser().getNickName(), board.getTitle(), board.getContent()))
                    .collect(Collectors.toList());
            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public List<GetBoardRes> getBoardById(Long boardId) throws BaseException{
        try {
            Board board = utilService.findByBoardIdWithValidation(boardId);
            User user = board.getUser();
            List<GetBoardRes> getBoardRes = new ArrayList<>(); // boardId는 유일하지만 반환형식을 맞추기 위해 List 사용
            getBoardRes.add(new GetBoardRes(boardId, board.getBoardType(), user.getNickName(), board.getTitle(), board.getContent()));
            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public String deleteBoard(DeleteBoardReq deleteBoardReq) throws BaseException {
        Board deleteBoard = utilService.findByBoardIdWithValidation(deleteBoardReq.getBoardId());
        Long boardId = deleteBoard.getBoardId();
        User writer = deleteBoard.getUser();
        User visitor = utilService.findByUserIdWithValidation(deleteBoardReq.getUserId());
        if(writer.getId() == visitor.getId()) {
            // S3에 업로드된 파일을 삭제하는 명령
            List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(boardId);
            if(!allByBoardId.isEmpty()){
                postPhotoService.deleteAllPostPhotos(allByBoardId);
                // PostPhotoRepository에서 삭제하는 명령
                List<Long> ids = postPhotoService.findAllId(deleteBoard.getBoardId());
                postPhotoService.deleteAllPostPhotoByBoard(ids);
                // 아래의 JPQL 쿼리로 한 번에 PostPhoto들을 삭제하는 것도 가능.
                // postPhotoRepository.deletePostPhotoByBoardId(deleteBoardReq.getBoardId());
            }
            // 댓글이 있는 경우 댓글 먼저 삭제해야 함.
            List<Comment> comments = commentRepository.findCommentsByBoardId(boardId);
            if(!comments.isEmpty()){
                commentRepository.deleteCommentsByBoardId(boardId);
            }
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
    public String modifyBoard(Long userId, PatchBoardReq patchBoardReq,
                              List<MultipartFile> multipartFiles) throws BaseException {
        try {
            Long boardId = patchBoardReq.getBoardId();
            Board board = utilService.findByBoardIdWithValidation(boardId);
            User writer = board.getUser();
            User visitor = utilService.findByUserIdWithValidation(userId);
            if(writer.getId() == visitor.getId()){
                board.updateBoard(patchBoardReq.getBoardType(), patchBoardReq.getTitle(), patchBoardReq.getContent());
                //사진 업데이트, 지우고 다시 저장
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

