package com.example.hello_there.user_fee;

import com.example.hello_there.utils.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "user_fees")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
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
    private int feeYear;

    @Column(name="fee_month")
    private int feeMonth;

    @Column
    private double cost;

    @Column(name="payment_check")
    private boolean paymentCheck;

}
