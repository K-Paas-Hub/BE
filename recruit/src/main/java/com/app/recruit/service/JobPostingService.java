package com.app.recruit.service;

import com.app.recruit.dto.JobPostingDetailDto;
import com.app.recruit.dto.JobPostingDto;
import com.app.recruit.entity.JobPosting;
import com.app.recruit.entity.JobPostingDetail;
import com.app.recruit.repository.JobPostingDetailRepository;
import com.app.recruit.repository.JobPostingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobPostingDetailRepository jobPostingDetailRepository;
    private final CrawlingService crawlingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 전체 공고 조회 */
    public List<JobPostingDto> getAllJobs() {
        return jobPostingRepository.findAll()
                .stream()
                .map(JobPostingDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** 특정 공고 조회 */
    public JobPostingDto getJobById(Long id) {
        JobPosting job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        return JobPostingDto.fromEntity(job);
    }

    /** 특정 공고 상세 조회 */
    public JobPostingDetailDto getJobDetail(Long id) throws Exception {
        Optional<JobPostingDetail> detailOpt = jobPostingDetailRepository.findByJobPosting_Id(id);

        JobPostingDetail detail = detailOpt.orElseGet(() -> {
            try {
                // 상세정보가 없으면 크롤링 수행
                crawlingService.crawlAndSaveJobDetail(id);
                // 다시 DB에서 조회
                return jobPostingDetailRepository.findByJobPosting_Id(id)
                        .orElseThrow(() -> new IllegalArgumentException("JobDetail not found"));
            } catch (Exception e) {
                throw new RuntimeException("JobDetail load failed", e);
            }
        });

        return JobPostingDetailDto.builder()
                .tasks(objectMapper.readValue(detail.getTasks(), new TypeReference<>() {}))
                .requirements(objectMapper.readValue(detail.getRequirements(), new TypeReference<>() {}))
                .preferred(objectMapper.readValue(detail.getPreferred(), new TypeReference<>() {}))
                .conditions(objectMapper.readValue(detail.getConditions(), new TypeReference<>() {}))
                .procedure(objectMapper.readValue(detail.getProcedure(), new TypeReference<>() {}))
                .welfare(detail.getWelfare())
                .workAddress(detail.getWorkAddress())
                .subway(detail.getSubway())
                .detailImages(objectMapper.readValue(detail.getDetailImages(), new TypeReference<>() {}))
                .build();
    }
}
