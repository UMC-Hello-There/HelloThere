package com.example.hello_there.user_fee;

import com.example.hello_there.utils.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity(name = "user_fees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFee extends BaseTimeEntity {
    @Id @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO 연관관계가 필요한 경우 맺을 예정
    @Column(name="user_id")
    private Long userId;

    //TODO 연관관계가 필요한 경우 맺을 예정
    @Column(name="house_id")
    private Long houseId;

    @Column(name="fee_year")
    private int feeYear;    //관리비 월 컬럼 ex) 7

    @Column(name="fee_month")
    private int feeMonth;   //관리비 년도 컬럼 ex) 2023

    @Column
    private double cost;    //관리비

    @Column(name="payment_check")
    private boolean paymentCheck;   //납부 여부

    @Builder
    public UserFee(Long userId, Long houseId, int feeYear, int feeMonth, double cost, boolean paymentCheck) {
        this.userId = userId;
        this.houseId = houseId;
        this.feeYear = feeYear;
        this.feeMonth = feeMonth;
        this.cost = cost;
        this.paymentCheck = paymentCheck;
    }
}
