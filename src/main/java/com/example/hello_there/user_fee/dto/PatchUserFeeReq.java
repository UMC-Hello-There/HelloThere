package com.example.hello_there.user_fee.dto;

import com.example.hello_there.user_fee.UserFee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PatchUserFeeReq {
    private Double cost;
    private Boolean paymentCheck;

    public UserFee updateEntity(UserFee userFee) {
        if(Objects.nonNull(cost)){
            userFee.setCost(cost);
        }

        if(Objects.nonNull(paymentCheck)){
            userFee.setPaymentCheck(paymentCheck);
        }
        return userFee;
    }
}
