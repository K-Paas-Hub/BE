package com.app.recruit.repository;

import com.app.recruit.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByJobStatus(String status);
    Optional<JobPosting> findByJobTitleAndCompany_Id(String jobTitle, Long companyId);
    boolean existsByJobTitleAndCompany_Id(String jobTitle, Long companyId);
}
