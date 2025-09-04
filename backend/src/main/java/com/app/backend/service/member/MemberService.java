package com.app.backend.service.member;

import com.app.backend.controller.dto.JoinForm;
import com.app.backend.entity.Member;

import java.util.Optional;

public interface MemberService {
    Optional<Member> getMemberByEmail(String memberEmail);
    Member saveMember(JoinForm loginForm);
    Optional<Member> getMemberByPhoneNumber(String phoneNumber);
    Optional<Member> getMemberByMemberEmailAndMemberName(String memberEmail, String memberName);
    default Member toEntity(JoinForm loginForm){
        return Member.builder()
                .memberEmail(loginForm.getMemberEmail())
                .memberName(loginForm.getMemberName())
                .memberPhone(loginForm.getMemberPhone())
                .profileImageUrl(loginForm.getProfileImageUrl())
                .build();
    }
    Optional<Member> getMemberById(Long id);
}
