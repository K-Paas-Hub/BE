package com.app.recruit.repository;

import com.app.recruit.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // 회사명으로 검색하고 싶으면 메서드 추가 가능
    boolean existsByCompanyName(String companyName);
    Optional<Company> findByCompanyName(String companyName);
}
