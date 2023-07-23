package com.example.hello_there.user_fee.dto;

import com.example.hello_there.user_fee.UserFee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetUserFeeRes {
    private Long id;
    private int feeYear;
    private int feeMonth;
    private double cost;
    private boolean paymentCheck;

    public static GetUserFeeRes mapEntityToResponse(Optional<UserFee> userFee){
        return new GetUserFeeRes(
                userFee.get().getUserId(),
                userFee.get().getFeeYear(),
                userFee.get().getFeeMonth(),
                userFee.get().getCost(),
                userFee.get().isPaymentCheck()
        );
    }
}
