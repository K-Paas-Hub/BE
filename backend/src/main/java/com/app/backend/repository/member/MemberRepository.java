package com.app.backend.repository.member;

import com.app.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberEmail(String memberEmail);
    Optional<Member> findByMemberPhone(String memberPhone);
    Optional<Member> findByMemberEmailAndMemberName(String memberEmail, String memberName);
}
