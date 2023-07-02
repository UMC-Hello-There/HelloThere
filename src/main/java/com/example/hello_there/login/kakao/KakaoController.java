package com.example.hello_there.login.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kaKaoLoginService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    //카카오 로그인 코드
    @ResponseBody
    @PostMapping("/oauth/kakao")
    public BaseResponse<?> kakaoCallback(@RequestParam("accToken") String accessToken,
                                         @RequestParam("refToken") String refreshToken) {
        String memberEmail = kaKaoLoginService.getMemberEmail(accessToken);
        String memberNickName = kaKaoLoginService.getMemberNickname(accessToken);
        Optional<Member> findMember = memberRepository.findByEmail(memberEmail);
        if (!findMember.isPresent()) {
            Member kakaoMember = new Member();
            kakaoMember.updateEmail(memberEmail);
            kakaoMember.updateNickName(memberNickName);
            kakaoMember.updateIsSocialLogin();
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(kakaoMember.getId());
            kakaoMember.updateAccessToken(tokenInfo.getAccessToken());
            kakaoMember.updateRefreshToken(tokenInfo.getRefreshToken());
            memberRepository.save(kakaoMember);
            return new BaseResponse<>(tokenInfo);
        }

        else {
            Member member = findMember.get();
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(member.getId());
            member.updateRefreshToken(tokenInfo.getRefreshToken());
            memberRepository.save(member);
            return new BaseResponse<>(tokenInfo);
        }
    }

    // 카카오 소셜 로그아웃
    // 하지만 실제로 사용할 일은 없다. 카카오에서 받은 접근 토큰과 재발급 토큰은 모두 우리의 방식으로 다시 generate하였기 때문에
    // 카카오에서 이를 해석할 수 없다. 따라서 소셜로그인의 경우에도 Member Controller의 로그아웃 API를 사용해야 한다.
    @PostMapping("/oauth/kakao-logout")
    @ResponseBody
    public BaseResponse<?> kakaoLogout()
    {
        try{
            String accessToken = jwtService.getJwt();
            String result = memberService.socialLogout(accessToken);
            return new BaseResponse<>(result);
        } catch(Exception e){
            return new BaseResponse<>(BaseResponseStatus.KAKAO_ERROR);
        }
    }
}