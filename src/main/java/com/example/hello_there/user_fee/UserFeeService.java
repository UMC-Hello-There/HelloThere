package com.example.hello_there.user_fee;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserFeeService {
    private final UserFeeRepository userFeeRepository;

    public Optional<UserFee> getUserFeeCurrent(Long houseId, int feeYear, int feeMonth){
        //return userFeeRepository.findByHouseIdANDByFeeYearANDByFeeMonth(houseId, feeYear, feeMonth);
        return userFeeRepository.findById(houseId);
    }
}
