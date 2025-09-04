package com.app.backend.controller.dto;

import com.app.backend.entity.Member;
import com.app.backend.entity.type.Authority;
import com.app.backend.entity.type.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public enum OAuthAttributes {
    KAKAO("kakao", (attributes) -> {
        // kakao는 kakao_account에 유저정보가 있다. (email)
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        // kakao_account안에 또 profile이라는 JSON객체가 있다. (nickname, profile_image)
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        MemberProfile memberProfile = new MemberProfile();
        memberProfile.setName((String) kakaoProfile.get("nickname"));
        memberProfile.setEmail((String) kakaoAccount.get("email"));
        memberProfile.setPhone((String) kakaoAccount.get("phone_number"));
        memberProfile.setProfileImageUrl((String) kakaoProfile.get("profile_image_url")); // 프로필 이미지 URL 설정
        return memberProfile;
    });

    private final String registrationId;
    private final Function<Map<String, Object>, MemberProfile> of;

    OAuthAttributes(String registrationId, Function<Map<String, Object>, MemberProfile> of) {
        this.registrationId = registrationId;
        this.of = of;
    }

    public static MemberProfile extract(String registrationId, Map<String, Object> attributes) {
        return Arrays.stream(values())
                .filter(provider -> registrationId.equals(provider.registrationId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .of.apply(attributes);
    }
}
