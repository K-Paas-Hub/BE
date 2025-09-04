package com.app.backend.service.auth;

import com.app.backend.controller.dto.TokenDto;
import com.app.backend.controller.dto.TokenRequestDto;
import com.app.backend.entity.Member;
import com.app.backend.entity.type.Authority;
import com.app.backend.entity.type.StatusType;
import com.app.backend.jwt.TokenProvider;
import com.app.backend.provider.MemberDetail;
import com.app.backend.repository.member.MemberRepository;
import com.app.backend.repository.refreshToken.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByMemberEmail(email);
    }

    @Override
    @Transactional
    public TokenDto socialLogin(String email, String name, String phone) {
        Optional<Member> memberOpt = memberRepository.findByMemberEmail(email);
        if (memberOpt.isPresent()) {
            return updateAndLogin(memberOpt.get(), name, phone);
        } else {
            return signupAndLogin(email, name, phone);
        }
    }

    @Transactional
    public TokenDto signupAndLogin(String email, String name, String phone) {
        Member member = Member.builder()
                .memberEmail(email)
                .memberName(name)
                .memberPhone(phone)
                .status(StatusType.ACTIVE)
                .authority(Authority.USER)
                .build();
        memberRepository.save(member);
        MemberDetail memberDetail = new MemberDetail(member);
        return tokenProvider.generateTokenDto(memberDetail);
    }

    @Override
    @Transactional
    public TokenDto updateAndLogin(Member member, String name, String phone) {
        member.update(name, phone, member.getMemberEmail(), member.getProfileImageUrl());
        memberRepository.save(member);
        MemberDetail memberDetail = new MemberDetail(member);
        return tokenProvider.generateTokenDto(memberDetail);
    }

    @Override
    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // Refresh Token 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        // Access Token에서 사용자 정보 추출
        String userEmail = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken()).getName();
        
        // 사용자 정보 조회
        Member member = memberRepository.findByMemberEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        MemberDetail memberDetail = new MemberDetail(member);
        return tokenProvider.generateTokenDto(memberDetail);
    }
}
