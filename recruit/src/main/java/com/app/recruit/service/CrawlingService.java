package com.app.recruit.service;

import com.app.recruit.entity.*;
import com.app.recruit.repository.*;
import com.app.recruit.util.etc.CompanyCrawler;
import com.app.recruit.util.etc.JobListCrawler;
import com.app.recruit.util.etc.JobCrawler;
import com.app.recruit.dto.JobPostingDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingDetailRepository jobPostingDetailRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 공고 목록 크롤링 → DB 저장 (중복 방지 포함) */
    public List<JobPostingDto> crawlAndSaveJobs(String keyword, int pages) throws Exception {
        List<JobListCrawler.JobPosting> crawled = JobListCrawler.crawlJobPosts(keyword, pages);
        List<JobPosting> savedEntities = new ArrayList<>();

        for (JobListCrawler.JobPosting j : crawled) {
            // 1. 회사 정보 크롤링 및 저장/조회
            CompanyCrawler.CompanyInfo compInfo = CompanyCrawler.crawlCompanyInfo(j.company, j.companyInfo);
            Company company = companyRepository.findByCompanyName(compInfo.name)
                    .orElseGet(() -> companyRepository.save(
                            Company.builder()
                                    .companyName(compInfo.name)
                                    .companyType(compInfo.type)
                                    .industry(compInfo.industry)
                                    .website(compInfo.website)
                                    .address(compInfo.address)
                                    .introduce(compInfo.introduce)
                                    .build()
                    ));

            // 2. 공고 중복 여부 확인 후 저장/조회
            JobPosting jobPosting = jobPostingRepository
                    .findByJobTitleAndCompany_Id(j.title, company.getId())
                    .orElseGet(() -> jobPostingRepository.save(
                            JobPosting.create(
                                    j.title,
                                    j.location,
                                    j.career,
                                    j.education,
                                    j.salaryRange,
                                    j.postedDate,
                                    j.deadlineType,
                                    j.deadline,
                                    j.status,
                                    company,
                                    j.postLink
                            )
                    ));

            // 3. 고용형태 처리 (정규직, 계약직 → EmploymentType N:M 저장)
            if (j.employmentType != null && !j.employmentType.isBlank()) {
                String[] types = j.employmentType.split("[,·/|]"); // 여러 구분자 대응
                for (String type : types) {
                    String cleanType = type.trim();
                    if (cleanType.isEmpty()) continue;

                    EmploymentType empType = employmentTypeRepository.findByEmploymentName(cleanType)
                            .orElseGet(() -> employmentTypeRepository.save(
                                    EmploymentType.builder()
                                            .employmentName(cleanType)
                                            .build()
                            ));

                    jobPosting.getEmploymentTypes().add(empType);
                }
            }

            // 4. 직무분야 → Category 매핑
            if (j.jobSectors != null) {
                for (String sectorGroup : j.jobSectors) {
                    // 리스트 안 문자열을 "," 기준으로 분리
                    String[] sectors = sectorGroup.split(",");

                    for (String rawSector : sectors) {
                        String sector = rawSector.trim(); // 앞뒤 공백 제거 (중간 공백 유지)
                        if (sector.isBlank()) continue;

                        // "외" 단독이거나 "외" 로 끝나면 "외" 제거
                        if (sector.equals("외")) continue;
                        if (sector.endsWith(" 외")) {
                            sector = sector.substring(0, sector.length() - 2);
                        }

                        final String finalSector = sector; // 람다에서 사용될 변수는 final로 고정
                        Category category = categoryRepository.findByCategoryName(sector)
                                .orElseGet(() -> categoryRepository.save(
                                        Category.builder()
                                                .categoryName(finalSector)
                                                .build()
                                ));

                        jobPosting.getCategories().add(category);
                    }
                }
            }

            savedEntities.add(jobPosting);
        }

        // JPA flush (카테고리 매핑 저장)
        jobPostingRepository.saveAll(savedEntities);

        // Entity → DTO 변환
        return savedEntities.stream()
                .map(JobPostingDto::fromEntity)
                .toList();
    }

    /** 회사 정보 크롤링 → DB 저장 (중복 방지 포함) */
    public String crawlAndSaveCompany(Long jobId) {
        // 1. JobPosting 찾기
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        // 2. 기존 회사 정보
        Company existingCompany = jobPosting.getCompany();

        // 3. 회사 정보 크롤링
        CompanyCrawler.CompanyInfo crawled = CompanyCrawler.crawlCompanyInfo(
                existingCompany.getCompanyName(),
                existingCompany.getWebsite() // 필요하다면 postLink나 다른 필드 사용
        );

        // 4. DB 반영 (업데이트 or 신규)
        Company company = companyRepository.findByCompanyName(crawled.name)
                .orElseGet(() -> companyRepository.save(
                        Company.builder()
                                .companyName(crawled.name)
                                .companyType(crawled.type)
                                .industry(crawled.industry)
                                .website(crawled.website)
                                .address(crawled.address)
                                .introduce(crawled.introduce)
                                .build()
                ));

        // 5. JobPosting에 회사 다시 연결 (새로 생성된 경우 대비)
        jobPostingRepository.save(jobPosting);

        return "회사 [" + crawled.name + "] 저장 완료";
    }

    /** 공고 상세 크롤링 → DB 저장 (있으면 업데이트) */
    public void crawlAndSaveJobDetail(Long jobId) throws Exception {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        // DB에 저장된 postLink 활용
        String url = jobPosting.getPostLink();
        JobCrawler.JobDetail crawled = JobCrawler.crawlJobDetailAjax(jobPosting.getJobTitle(), url);

        // JSON 직렬화
        String tasksJson = objectMapper.writeValueAsString(crawled.tasks);
        String requirementsJson = objectMapper.writeValueAsString(crawled.requirements);
        String preferredJson = objectMapper.writeValueAsString(crawled.preferred);
        String conditionsJson = objectMapper.writeValueAsString(crawled.conditions);
        String procedureJson = objectMapper.writeValueAsString(crawled.procedure);
        String detailImagesJson = objectMapper.writeValueAsString(crawled.detailImages);

        JobPostingDetail detail = jobPostingDetailRepository.findByJobPosting_Id(jobPosting.getId())
                .map(existing -> {
                    existing.update(tasksJson, requirementsJson, preferredJson,
                            conditionsJson, procedureJson, crawled.welfare,
                            detailImagesJson, crawled.workAddress, crawled.subway);
                    return existing;
                })
                .orElseGet(() -> JobPostingDetail.builder()
                        .jobPosting(jobPosting)
                        .tasks(tasksJson)
                        .requirements(requirementsJson)
                        .preferred(preferredJson)
                        .conditions(conditionsJson)
                        .procedure(procedureJson)
                        .welfare(crawled.welfare)
                        .detailImages(detailImagesJson)
                        .workAddress(crawled.workAddress)
                        .subway(crawled.subway)
                        .build());

        jobPostingDetailRepository.save(detail);
    }
}
