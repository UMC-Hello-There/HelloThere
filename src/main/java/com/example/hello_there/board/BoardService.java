package com.example.hello_there.board;

import com.example.hello_there.advertisement.AdService;
import com.example.hello_there.advertisement.dto.GetAdRes;
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
import com.example.hello_there.house.House;
import com.example.hello_there.report.Report;
import com.example.hello_there.report.ReportRepository;
import com.example.hello_there.report.ReportService;
import com.example.hello_there.sqs.SQSService;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserStatus;
import com.example.hello_there.utils.S3Service;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.report.ReportCount.ADD_REPORT_FOR_BOARD;
import static com.example.hello_there.utils.UtilService.convertLocalDateTimeToLocalDate;
import static com.example.hello_there.utils.UtilService.convertLocalDateTimeToTime;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final RedisTemplate redisTemplate;
    private final PostPhotoRepository postPhotoRepository;
    private final ReportRepository reportRepository;
    private final UtilService utilService;
    private final ReportService reportService;
    private final S3Service s3Service;
    private final PostPhotoService postPhotoService;
    private final CommentRepository commentRepository;
    private final LikeBoardRepository likeBoardRepository;
    private final SQSService sqsService;
    private final AdService adService;

    @Transactional
    public void save(Board board) {
        boardRepository.save(board);
    }

    @Transactional
    public String createBoard(Long userId, PostBoardReq postBoardReq, List<MultipartFile> multipartFiles) throws BaseException {
        try {
            // 블랙 유저 검증
            reportService.checkBlackUser("board",userId);

            User user = utilService.findByUserIdWithValidation(userId);
            House house = utilService.findByHouseIdWithValidation(user.getHouse().getHouseId());
            Board board = Board.builder()
                    .title(postBoardReq.getTitle())
                    .content(postBoardReq.getContent())
                    .view(0L)
                    .commentCount(0L)
                    .likeCount(0L)
                    .boardType(postBoardReq.getBoardType())
                    .photoList(new ArrayList<>())
                    .user(user)
                    .house(house)
                    // .commentList(new ArrayList<>())
                    .build();
            save(board);

            if (multipartFiles != null) {
                List<GetS3Res> getS3ResList = s3Service.uploadFile(multipartFiles);
                postPhotoService.saveAllPostPhotoByBoard(getS3ResList, board);
            }
            return "boardId: " + board.getBoardId() + "인 게시글을 생성했습니다.";
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
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
        if (board.getUser().getProfile() != null) {
            profile = new GetS3Res(board.getUser().getProfile().getProfileUrl(),
                    board.getUser().getProfile().getProfileFileName());
        }
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<GetCommentRes>> responseEntity = restTemplate.exchange(
                "http://localhost:8080/boards/{boardId}/comments/{userId}",  // 호출할 API의 URL
                HttpMethod.GET,  // 요청 방법 (GET, POST 등)
                null,  // 요청에 대한 데이터 (필요에 따라 설정)
                new ParameterizedTypeReference<List<GetCommentRes>>() {
                },
                boardId,  // URL 경로 변수에 대한 값 (필요에 따라 설정)
                userId
        );
        List<GetCommentRes> response = new ArrayList<>();
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            response = responseEntity.getBody();
        } else {
            throw new BaseException(FAIL_TO_LOAD);
        }
        // 게시글 작성 지역
        String district = board.getHouse().getDistrict();

        GetBoardDetailRes getBoardDetailRes = new GetBoardDetailRes(board.getBoardId(),
                board.getBoardType(), convertLocalDateTimeToLocalDate(board.getCreateDate()),
                convertLocalDateTimeToTime(board.getCreateDate()), board.getUser().getNickName(),
                profile, adService.findAd(district), board.getTitle(),  board.getContent(), board.getView(),
                board.getCommentCount(), board.getLikeCount(), getS3Res, response);

        return getBoardDetailRes;
    }


    /**
     * 게시글 카테고리별 전체 조회
     **/
    @Transactional
    public List<GetBoardRes> getBoardsByCategory(Long userId, BoardType category) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findAllByBoardTypeAndHouse_HouseIdOrderByBoardIdDesc(category, houseId);
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            convertLocalDateTimeToLocalDate(board.getCreateDate()),
                            convertLocalDateTimeToTime(board.getCreateDate()),
                            board.getUser().getNickName(), board.getTitle(), board.getContent(), board.getView(),
                            board.getCommentCount(), board.getLikeCount()))
                    .collect(Collectors.toList());

            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    @Transactional
    public List<GetBoardEachOneRes> getBoardsByCategoryOne(Long userId) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findBoardsWithMaxBoardIdForEachBoardType(houseId);
            List<GetBoardEachOneRes> getBoardEachOneRes = boards.stream()
                    .map(board -> new GetBoardEachOneRes(board.getBoardId(), board.getBoardType(),
                            board.getTitle()))
                    .collect(Collectors.toList());
            return getBoardEachOneRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public List<GetBoardEachOneRes> getBoardsByLikeMain(Long userId) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findBoardsByLikesMain(houseId);
            List<GetBoardEachOneRes> getBoardEachOneRes = boards.stream()
                    .map(board -> new GetBoardEachOneRes(board.getBoardId(), board.getBoardType(),
                            board.getTitle()))
                    .collect(Collectors.toList());
            return getBoardEachOneRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    @Transactional
    public List<GetTopBoardRes> getTopBoardsByCategory(Long userId, BoardType category) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findBoardsWithMostCommentsAndLikes(houseId, category); // houseId가 같고 category가 같으며 댓글이 가장 많은 게시글과 좋아요가 가장 많은 게시글 이렇게 두 개를 반환
            List<GetTopBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetTopBoardRes(board.getBoardId(), board.getBoardType(),
                            board.getTitle(), board.getCommentCount(), board.getLikeCount()))
                    .collect(Collectors.toList());
            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    @Transactional
    public List<GetBoardRes> getBoardsByLike(Long userId) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findBoardsByLikes(houseId);
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            convertLocalDateTimeToLocalDate(board.getCreateDate()),
                            convertLocalDateTimeToTime(board.getCreateDate()),
                            board.getUser().getNickName(), board.getTitle(), board.getContent(), board.getView(),
                            board.getCommentCount(), board.getLikeCount()))
                    .collect(Collectors.toList());
            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public List<GetBoardRes> getBoardById(Long userId) throws BaseException {
        try {
            List<Board> boards = boardRepository.findBoardByUserId(userId);
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            convertLocalDateTimeToLocalDate(board.getCreateDate()),
                            convertLocalDateTimeToTime(board.getCreateDate()),
                            board.getUser().getNickName(), board.getTitle(), board.getContent(), board.getView(),
                            board.getCommentCount(), board.getLikeCount()))
                    .collect(Collectors.toList());
            return getBoardRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public List<GetBoardMainRes> getboardsHometerrier(Long userId) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findBoardMainHomeTerrier(houseId);

            List<GetBoardMainRes> getBoardMainRes = boards.stream()
                    .map(board -> {
                        List<PostPhoto> postPhotos = postPhotoRepository.findAllByBoardId(board.getBoardId()).orElse(Collections.emptyList());
                        GetS3Res getS3Res = postPhotos.isEmpty() ? null :
                                new GetS3Res(postPhotos.get(0).getImgUrl(), postPhotos.get(0).getFileName());
                        return new GetBoardMainRes(board.getBoardId(), board.getBoardType(),
                                board.getTitle(), getS3Res);
                    })
                    .collect(Collectors.toList());

            return getBoardMainRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    @Transactional
    public List<GetBoardMainRes> getboardsMarket(Long userId) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findBoardMainMarket(houseId);

            List<GetBoardMainRes> getBoardMainRes = boards.stream()
                    .map(board -> {
                        List<PostPhoto> postPhotos = postPhotoRepository.findAllByBoardId(board.getBoardId()).orElse(Collections.emptyList());
                        GetS3Res getS3Res = postPhotos.isEmpty() ? null :
                                new GetS3Res(postPhotos.get(0).getImgUrl(), postPhotos.get(0).getFileName());
                        return new GetBoardMainRes(board.getBoardId(), board.getBoardType(),
                                board.getTitle(), getS3Res);
                    })
                    .collect(Collectors.toList());

            return getBoardMainRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public List<GetBoardRes> getBoardsByTitleOrContent(Long userId, String keyword) throws BaseException {
        try {
            Long houseId = utilService.findByUserIdWithValidation(userId).getHouse().getHouseId();
            List<Board> boards = boardRepository.findBoardsByTitleOrContentContainingAndHouseId(keyword, houseId);
            List<GetBoardRes> getBoardRes = boards.stream()
                    .map(board -> new GetBoardRes(board.getBoardId(), board.getBoardType(),
                            convertLocalDateTimeToLocalDate(board.getCreateDate()),
                            convertLocalDateTimeToTime(board.getCreateDate()),
                            board.getUser().getNickName(), board.getTitle(), board.getContent(), board.getView(),
                            board.getCommentCount(), board.getLikeCount()))
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
        if (writer.getId() == visitor.getId()) {
            // S3에 업로드된 파일을 삭제하는 명령
            List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(boardId);
            if (!allByBoardId.isEmpty()) {
                postPhotoService.deleteAllPostPhotos(allByBoardId);
                postPhotoRepository.deletePostPhotoByBoardId(boardId);
            }
            // 댓글이 있는 경우 댓글 먼저 삭제해야 함.
            List<Comment> comments = commentRepository.findCommentsByBoardId(boardId);
            if (!comments.isEmpty()) {
                commentRepository.deleteCommentsByBoardId(boardId);
            }
            // 게시글을 삭제하는 명령
            boardRepository.deleteBoard(boardId);
            return "요청하신 게시글에 대한 삭제가 완료되었습니다.";
        } else {
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
            if (writer.getId() == visitor.getId()) {
                board.updateBoard(patchBoardReq.getBoardType(), patchBoardReq.getTitle(), patchBoardReq.getContent());
                //사진 업데이트, 지우고 다시 저장
                List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(boardId);
                postPhotoService.deleteAllPostPhotos(allByBoardId);
                List<Long> ids = postPhotoService.findAllId(board.getBoardId());
                postPhotoService.deleteAllPostPhotoByBoard(ids);

                if (multipartFiles != null) {
                    List<GetS3Res> getS3ResList = s3Service.uploadFile(multipartFiles);
                    postPhotoService.saveAllPostPhotoByBoard(getS3ResList, board);
                }
                return "boardId " + board.getBoardId() + "의 게시글을 수정했습니다.";
            } else {
                throw new BaseException(USER_WITHOUT_PERMISSION);
            }
        } catch (BaseException exception) {
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
                // board의 좋아요 count - 1;
                this.boardRepository.decrementlikesCountById(boardId);
                return "게시글의 좋아요를 취소했습니다.";
            } else {
                // 좋아요가 눌러져 있지 않은 상태 -> 좋아요
                this.likeBoardRepository.save(new LikeBoard(user, board));
                // board의 좋아요 count + 1;
                this.boardRepository.incrementlikesCountById(boardId);
                return "게시글에 좋아요를 눌렀습니다.";
            }
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }

    @Transactional
    public String reportWriter(Long reporterId, Long boardId, String reason) throws BaseException {
        // reason 값은 나중에 푸시 또는 알림 창에 왜 신고를 당했는지 보여주는 용도로 사용할 예정
        Board board = utilService.findByBoardIdWithValidation(boardId);
        User reported = board.getUser(); // 게시글의 작성자
        User repoter = utilService.findByUserIdWithValidation(reporterId);
        Report report = new Report();

        // 신고 검증 (중복 신고, 자기 자신 신고)
        reportService.chkReportValidation(reporterId, reported.getId(),boardId,0L,0L);

        reportRepository.save(report.createReport(reason, boardId, null, null, repoter, reported));

        // 게시글 누적 신고 횟수에 따른 처리
        reportService.updateReport(ADD_REPORT_FOR_BOARD, reported);

        int cumulativeReportCount = reportService.findCumulativeReportCount(reported,5);

        LocalDateTime now = LocalDateTime.now(); // 현재 시간
        List<String> reasons = reportRepository.findMatchingReportReasons(reported.getId(), board.getBoardId(), 0L, 0L);
        String prefix = "board";
        switch (cumulativeReportCount) {
            case 4 -> // 누적 신고 횟수 4
            {
                reportService.setReportExpiration(prefix,reported, now.plus(3, ChronoUnit.DAYS), UNABLE_TO_UPLOAD_THREE.name());
                sqsService.sendMessage(reported, 3, "게시글 업로드 금지");
            }
            case 8 -> // 누적 신고 횟수 8
            {
                reportService.setReportExpiration(prefix, reported, now.plus(5, ChronoUnit.DAYS), UNABLE_TO_UPLOAD_FIVE.name());
                sqsService.sendMessage(reported, 5, "게시글 업로드 금지");
            }
            case 12 -> // 누적 신고 횟수 12
            {
                reportService.setReportExpiration(prefix, reported, now.plus(7, ChronoUnit.DAYS), UNABLE_TO_UPLOAD_SEVEN.name());
                sqsService.sendMessage(reported, 7, "게시글 업로드 금지");
            }
            case 16 -> // 누적 신고 횟수 16
            {
                reportService.setReportExpiration(prefix, reported, now.plus(14, ChronoUnit.DAYS), UNABLE_TO_UPLOAD_FOURTEEN.name());
                sqsService.sendMessage(reported, 14, "게시글 업로드 금지");
            }
            case 20 -> // 누적 신고 횟수 20
            {
                reportService.setReportExpiration(prefix,reported, now.plus(30, ChronoUnit.DAYS), UNABLE_TO_UPLOAD_MONTH.name());
                sqsService.sendMessage(reported, 30, "게시글 업로드 금지");
            }
            case 21 -> // 누적 신고 횟수 21
            {
                reported.setStatus(UserStatus.INACTIVE); // 영구 정지
                sqsService.sendMessage(reported, -1, "영구 정지");
            }
        }

        return "게시글 작성자에 대한 신고 처리가 완료되었습니다.";
    }
}