package com.example.hello_there.user_fee;

import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserFeeService {
    private final UserFeeRepository userFeeRepository;

    /*
     * [UserFee] 입력받은 정보의 관리비 반환
     * 해당 년월의 관리비가 존재하지 않을 경우 데이터를 생성하여 반환해줌
     */
    public UserFee getUserFeeCurrent(Long userId, Long houseId, int feeYear, int feeMonth){
        Optional<UserFee> userFee = userFeeRepository.findByUserIdAndHouseIdAndFeeYearAndFeeMonth(userId, houseId, feeYear, feeMonth);
        return userFee.orElseGet(() -> createDefaultUserFee(userId, houseId, feeYear, feeMonth));
    }

    public UserFee createDefaultUserFee(Long userId, Long houseId, int feeYear, int feeMonth){
        UserFee userFee = UserFee.builder()
                .userId(userId)
                .houseId(houseId)
                .feeYear(feeYear)
                .feeMonth(feeMonth)
                .build();
        return userFeeRepository.save(userFee);
    }

    /*
     * [List<UserFee>] 입력받은 정보의 최근 3개월 관리비 반환
     */
    public List<UserFee> getUserFeeCustom(Long userId, Long houseId, int feeYear, int feeMonth) {
        Pageable pageable = PageRequest.of(0, 3);
        return userFeeRepository.findByUserIdAndHouseIdAndFeeYearAndFeeMonthLessThanEqualOrderByFeeYearDescFeeMonthDesc(userId, houseId, feeYear, feeMonth, pageable);
    }
}
