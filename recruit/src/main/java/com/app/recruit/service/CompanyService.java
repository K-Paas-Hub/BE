package com.app.recruit.service;

import com.app.recruit.dto.CompanyDto;
import com.app.recruit.entity.Company;
import com.app.recruit.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    /** 전체 회사 조회 */
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(CompanyDto::fromEntity)
                .toList();
    }

    /** 특정 회사 조회 */
    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        return CompanyDto.fromEntity(company);
    }
}
