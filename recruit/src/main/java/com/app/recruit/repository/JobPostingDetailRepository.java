package com.app.recruit.repository;

import com.app.recruit.entity.JobPostingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPostingDetailRepository extends JpaRepository<JobPostingDetail, Long> {
    Optional<JobPostingDetail> findByJobPosting_Id(Long jobId);
}
