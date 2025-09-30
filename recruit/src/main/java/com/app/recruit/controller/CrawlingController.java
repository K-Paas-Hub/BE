package com.app.recruit.controller;

import com.app.recruit.dto.JobPostingDto;
import com.app.recruit.service.CrawlingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruit/crawl")
@RequiredArgsConstructor
@Tag(name = "Crawling API", description = "사람인 공고/회사/상세정보 크롤링 API (관리자용)")
public class CrawlingController {

    private final CrawlingService crawlingService;

    /** 크롤링 실행 (관리자/내부용) */
    @Operation(summary = "공고 목록 크롤링", description = "검색 키워드와 페이지 수를 기준으로 사람인에서 공고를 크롤링하고 DB에 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 공고 크롤링 및 저장 완료"),
            @ApiResponse(responseCode = "500", description = "크롤링 중 서버 오류 발생")
    })
    @PostMapping("/jobs")
    public List<JobPostingDto> crawlJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pages) throws Exception {
        return crawlingService.crawlAndSaveJobs(keyword, pages);
    }

    @Operation(summary = "회사 정보 크롤링", description = "특정 JobPosting ID를 기반으로 회사 정보를 크롤링하고 DB에 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 회사 정보 크롤링 및 저장 완료"),
            @ApiResponse(responseCode = "404", description = "해당 JobPosting ID가 없음"),
            @ApiResponse(responseCode = "500", description = "크롤링 중 서버 오류 발생")
    })
    @PostMapping("/company")
    public String crawlCompany(@RequestParam Long jobId) throws Exception {
        return crawlingService.crawlAndSaveCompany(jobId);
    }

    @Operation(summary = "공고 상세 크롤링", description = "특정 JobPosting ID를 기반으로 상세 정보를 크롤링하고 DB에 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 상세 정보 크롤링 및 저장 완료"),
            @ApiResponse(responseCode = "404", description = "해당 JobPosting ID가 없음"),
            @ApiResponse(responseCode = "500", description = "크롤링 중 서버 오류 발생")
    })
    @PostMapping("/jobDetail")
    public void crawlJobDetail(@RequestParam Long jobId) throws Exception {
        crawlingService.crawlAndSaveJobDetail(jobId);
    }
}
