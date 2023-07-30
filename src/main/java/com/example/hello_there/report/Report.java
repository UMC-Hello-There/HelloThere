package com.example.hello_there.report;

import com.example.hello_there.user.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private String reason; // 신고 사유

    @Column(nullable = true)
    private Long boardId;

    @Column(nullable = true)
    private Long commentId;

    @Column(nullable = true)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter; // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id")
    private User reported; // 신고를 당한 사람

    public Report createReport(String reason, Long boardId, Long commentId, Long messageId, User reporter, User reported) {
        Report report = new Report(); // 새로운 Report 객체 생성
        report.setReason(reason); // 신고 이유 설정
        // 게시글, 댓글, 채팅방 ID를 설정하고, null인 경우 0L로 설정
        report.setBoardId(boardId == null ? 0L : boardId);
        report.setCommentId(commentId == null ? 0L : commentId);
        report.setMessageId(messageId == null ? 0L : messageId);
        report.setReporter(reporter); // 신고자 설정
        report.setReported(reported); // 신고 대상 설정

        return report;
    }
}
