package com.app.recruit.dto;

import com.app.recruit.entity.Company;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyDto {
    private String companyName;
    private String companyType;
    private String industry;
    private String website;
    private String address;
    private String introduce;

    public static CompanyDto fromEntity(Company company) {
        return CompanyDto.builder()
                .companyName(company.getCompanyName())
                .companyType(company.getCompanyType())
                .industry(company.getIndustry())
                .website(company.getWebsite())
                .address(company.getAddress())
                .introduce(company.getIntroduce())
                .build();
    }
}
