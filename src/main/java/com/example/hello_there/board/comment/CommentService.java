package com.example.hello_there.board.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.conflict_board.ConflictBoardRepository;
import com.example.hello_there.board.free_board.FreeBoardRepository;
import com.example.hello_there.config.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final UserRepository userRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final ConflictBoardRepository confilctBoardRepository
    private final CommentRepository commentRepository;
    private final UtilService utilService;
    public PostCommentRes addComment(Long userId, PostCommentReq postCommentReq) throws BaseException {
        User user = utilService.findByUserIdWithValidation(userId);
        Board board = utilService.findByBoardIdWithValidation(postCommentReq.getBoardId());
        Comment comment;
        comment = Comment.builder()
                .nickName(user.getNickName())
                .reply(postCommentReq.getReply())
                .member(user)
                .board(board)
                .build();
        commentRepository.save(comment);
        return new PostCommentRes(user.getNickName(), postCommentReq.getReply());
    }


    /** 같은 게시글에 작성한 댓글은 한번에 모아서 출력 **/
    public List<GetCommentRes> getComments(Long memberId) throws BaseException {
        try {
            User user = utilService.findByUserIdWithValidation(userId);
            List<Comment> comments = commentRepository.findCommentsByUserId(userId);
            Map<String, List<Comment>> commentsByTitle = comments.stream()
                    .collect(Collectors.groupingBy(comment -> comment.getBoard().getTitle()));

            List<GetCommentRes> getCommentRes = new ArrayList<>();
            for (Map.Entry<String, List<Comment>> entry : commentsByTitle.entrySet()) {
                String title = entry.getKey();
                List<String> replies = entry.getValue().stream()
                        .map(Comment::getReply)
                        .collect(Collectors.toList());

                getCommentRes.add(new GetCommentRes(title, replies));
            }

            return getCommentRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
