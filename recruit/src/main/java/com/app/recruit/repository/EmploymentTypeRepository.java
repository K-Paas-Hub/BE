package com.app.recruit.repository;

import com.app.recruit.entity.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {
    Optional<EmploymentType> findByEmploymentName(String employmentName);
}
