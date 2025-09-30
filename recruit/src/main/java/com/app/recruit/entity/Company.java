package com.app.recruit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// 회사 정보
@Entity
@Table(name = "tbl_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String companyType;
    private String industry;
    private String website;
    private String address;

    @Lob private String introduce;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobPosting> jobPostings = new ArrayList<>();

    @Builder
    public Company(String companyName, String companyType, String industry,
                   String website, String address, String introduce) {
        this.companyName = companyName;
        this.companyType = companyType;
        this.industry = industry;
        this.website = website;
        this.address = address;
        this.introduce = introduce;
    }

    // 비즈니스 메서드
    public void changeIndustry(String newIndustry) {
        this.industry = newIndustry;
    }
}
