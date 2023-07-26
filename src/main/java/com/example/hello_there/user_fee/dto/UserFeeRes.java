package com.example.hello_there.user_fee.dto;

import com.example.hello_there.user_fee.UserFee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserFeeRes {
    private Long id;
    private int feeYear;
    private int feeMonth;
    private double cost;
    private boolean paymentCheck;

    public static UserFeeRes mapEntityToResponse(UserFee userFee){
        return new UserFeeRes(
                userFee.getId(),
                userFee.getFeeYear(),
                userFee.getFeeMonth(),
                userFee.getCost(),
                userFee.isPaymentCheck()
        );
    }
}
