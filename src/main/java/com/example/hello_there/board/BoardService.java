package com.example.hello_there.board;

import com.example.hello_there.board.dto.*;
import com.example.hello_there.board.like.LikeBoard;
import com.example.hello_there.board.like.LikeBoardRepository;
import com.example.hello_there.board.photo.PostPhoto;
import com.example.hello_there.board.photo.PostPhotoRepository;
import com.example.hello_there.board.photo.PostPhotoService;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.comment.Comment;
import com.example.hello_there.comment.CommentRepository;
import com.example.hello_there.comment.dto.GetCommentRes;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.S3Service;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.utils.UtilService.*;

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
    private final LikeBoardRepository likeBoardRepository;

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
                    .view(0L)
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
    public GetBoardDetailRes getBoardByBoardId(Long userId, Long boardId) throws BaseException {
        this.boardRepository.incrementViewsCountById(boardId);
        Board board = utilService.findByBoardIdWithValidation(boardId);
        List<PostPhoto> postPhotos = postPhotoRepository.findAllByBoardId(boardId).orElse(Collections.emptyList());

        List<GetS3Res> getS3Res = postPhotos.stream()
                .map(photo -> new GetS3Res(photo.getImgUrl(), photo.getFileName()))
                .collect(Collectors.toList());
        GetS3Res profile = new GetS3Res(null, null);
        if(board.getUser().getProfile() != null) {
            profile = new GetS3Res(board.getUser().getProfile().getProfileUrl(),
                    board.getUser().getProfile().getProfileFileName());
        }
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<GetCommentRes>> responseEntity = restTemplate.exchange(
                "http://localhost:8080/boards/{boardId}/comments/{userId}",  // 호출할 API의 URL
                HttpMethod.GET,  // 요청 방법 (GET, POST 등)
                null,  // 요청에 대한 데이터 (필요에 따라 설정)
                new ParameterizedTypeReference<List<GetCommentRes>>() {},
                boardId,  // URL 경로 변수에 대한 값 (필요에 따라 설정)
                userId
        );
        List<GetCommentRes> response = new ArrayList<>();
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            response = responseEntity.getBody();
        } else {
            throw new BaseException(FAIL_TO_LOAD);
        }
        GetBoardDetailRes getBoardDetailRes = new GetBoardDetailRes(board.getBoardId(),
                board.getBoardType(), convertLocalDateTimeToLocalDate(board.getCreateDate()),
                convertLocalDateTimeToTime(board.getCreateDate()), board.getUser().getNickName(),
                profile, board.getTitle(), board.getContent(), board.getView(),
                commentRepository.countByBoardBoardId(boardId), likeBoardRepository.countByBoardBoardId(board.getBoardId()), getS3Res, response);

        return getBoardDetailRes;
    }


    /** 게시글 카테고리별 전체 조회 **/
    @Transactional
    public List<GetBoardRes> getBoardsByCategory(BoardType category) throws BaseException {
        try {
            List<Board> boards = boardRepository.findAllByBoardTypeOrderByBoardIdDesc(category);
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            convertLocalDateTimeToLocalDate(board.getCreateDate()),
                            convertLocalDateTimeToTime(board.getCreateDate()),
                            board.getUser().getNickName(), board.getTitle(), board.getContent(), board.getView(),
                            commentRepository.countByBoardBoardId(board.getBoardId()), likeBoardRepository.countByBoardBoardId(board.getBoardId())))
                    .collect(Collectors.toList());

            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    @Transactional
    public List<GetBoardRes> getBoardById(Long userId) throws BaseException{
        try {
            List<Board> boards = boardRepository.findBoardByUserId(userId);
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            convertLocalDateTimeToLocalDate(board.getCreateDate()),
                            convertLocalDateTimeToTime(board.getCreateDate()),
                            board.getUser().getNickName(), board.getTitle(), board.getContent(), board.getView(),
                            commentRepository.countByBoardBoardId(board.getBoardId()), likeBoardRepository.countByBoardBoardId(board.getBoardId())))
                    .collect(Collectors.toList());
            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public String deleteBoard(Long userId, Long boardId) throws BaseException {
        Board deleteBoard = utilService.findByBoardIdWithValidation(boardId);
        User writer = deleteBoard.getUser();
        User visitor = utilService.findByUserIdWithValidation(userId);
        if(writer.getId() == visitor.getId()) {
            // S3에 업로드된 파일을 삭제하는 명령
            List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(boardId);
            if(!allByBoardId.isEmpty()){
                postPhotoService.deleteAllPostPhotos(allByBoardId);
                postPhotoRepository.deletePostPhotoByBoardId(boardId);
            }
            // 댓글이 있는 경우 댓글 먼저 삭제해야 함.
            List<Comment> comments = commentRepository.findCommentsByBoardId(boardId);
            if(!comments.isEmpty()){
                commentRepository.deleteCommentsByBoardId(boardId);
            }
            // 게시글을 삭제하는 명령
            boardRepository.deleteBoard(boardId);
            return "요청하신 게시글에 대한 삭제가 완료되었습니다.";
        }
        else {
            throw new BaseException(USER_WITHOUT_PERMISSION);
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
                throw new BaseException(USER_WITHOUT_PERMISSION);
            }
        } catch(BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }

    @Transactional
    public String likeOrUnlikeBoard(Long userId, Long boardId) throws BaseException {
        try {
            Board board = utilService.findByBoardIdWithValidation(boardId);
            User user = utilService.findByUserIdWithValidation(userId);

            Optional<LikeBoard> likeBoardOptional = likeBoardRepository.findByBoard_BoardIdAndUserId(boardId, userId);
            if (likeBoardOptional.isPresent()) {
                // 이미 좋아요가 눌러져 있는 상태 -> 좋아요 취소
                LikeBoard likeBoard = likeBoardOptional.get();
                this.likeBoardRepository.deleteById(likeBoard.getId());
                return "게시글의 좋아요를 취소했습니다.";
            } else {
                // 이미 좋아요가 눌러져 있지 않은 상태 -> 좋아요
                this.likeBoardRepository.save(new LikeBoard(user, board));
                return "게시글에 좋아요를 눌렀습니다.";
            }
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }


}

