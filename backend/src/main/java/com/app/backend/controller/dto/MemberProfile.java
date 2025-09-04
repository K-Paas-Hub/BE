package com.app.backend.controller.dto;

import com.app.backend.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfile {
    private String email;
    private String name;
    private String phone;
    private String profileImageUrl;

    // toMember 메서드 수정
    public Member toMember() {
        return Member.builder()
                .memberName(name)
                .memberEmail(email)
                .memberPhone(phone)
                .profileImageUrl(profileImageUrl) // 추가
                .build();
    }

}
