package com.app.backend.service.member;

import com.app.backend.controller.dto.JoinForm;
import com.app.backend.entity.Member;
import com.app.backend.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMemberByEmail(String memberEmail) {
        return memberRepository.findByMemberEmail(memberEmail);
    }

    @Override
    @Transactional
    public Member saveMember(JoinForm loginForm) {
        Member member = toEntity(loginForm);
        return memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMemberByPhoneNumber(String phoneNumber) {
        return memberRepository.findByMemberPhone(phoneNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMemberByMemberEmailAndMemberName(String memberEmail, String memberName) {
        return memberRepository.findByMemberEmailAndMemberName(memberEmail, memberName);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }
}
