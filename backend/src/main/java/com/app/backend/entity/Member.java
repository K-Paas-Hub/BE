package com.app.backend.entity;

import com.app.backend.entity.type.Authority;
import com.app.backend.entity.type.StatusType;
import lombok.*;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_member")
@ToString
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String memberEmail;

    private String memberPhone;

    private String memberPassword;

    private String memberName;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes = new ArrayList<>();

    @Builder
    public Member(Long id, String memberEmail, String memberPhone, String memberPassword, String memberName, String profileImageUrl, StatusType status, Authority authority, List<Favorite> favorites, List<Resume> resumes) {
        this.id = id;
        this.memberEmail = memberEmail;
        this.memberPhone = memberPhone;
        this.memberPassword = memberPassword;
        this.memberName = memberName;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.authority = authority;
        this.favorites = favorites;
        this.resumes = resumes;
    }

    public Member update(String memberName, String memberPhoneNumber, String memberEmail, String profileImageUrl){
        this.setMemberName(memberName);
        this.setMemberPhone(memberPhoneNumber);
        this.setMemberEmail(memberEmail);
        this.setProfileImageUrl(profileImageUrl);
        return this;
    }

}
