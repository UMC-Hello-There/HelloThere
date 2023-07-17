package com.example.hello_there.management;

import com.example.hello_there.board.BoardService;
import com.example.hello_there.board.dto.PostBoardReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.management.dto.GetManagementRes;
import com.example.hello_there.management.vo.GeoPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/managements")
public class ManagementController {

    private final ManagementService managementService;
    private final JwtService jwtService;

    /** GPS 위치 인증을 위한 근처 아파트 조회 **/
    @GetMapping("/spatial/radius")
    public BaseResponse<List<GetManagementRes>> findByRadius(@RequestParam("lng") Double lng, @RequestParam("lat") Double lat, @RequestParam("radius") Double radius) {
        try {
            Long userId = jwtService.getUserIdx();
            List<Management> managementList = managementService.findByRadius(new GeoPoint(lng, lat), radius);
            List<GetManagementRes> res =
                    managementList.stream().map(it->GetManagementRes.mapEntityToResponse(it)).collect(Collectors.toList());
            return new BaseResponse<>(res);
        }
        catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
