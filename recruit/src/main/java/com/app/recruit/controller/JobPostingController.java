package com.app.recruit.controller;

import com.app.recruit.dto.JobPostingDetailDto;
import com.app.recruit.dto.JobPostingDto;
import com.app.recruit.service.JobPostingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruit/jobs")
@RequiredArgsConstructor
@Tag(name = "JobPosting API", description = "채용 공고 및 상세 조회 API")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    /** 전체 공고 조회 */
    @Operation(summary = "전체 공고 조회", description = "DB에 저장된 모든 채용 공고 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 전체 공고 조회 완료")
    })
    @GetMapping
    public List<JobPostingDto> getAllJobs() {
        return jobPostingService.getAllJobs();
    }

    /** 특정 공고 조회 */
    @Operation(summary = "특정 공고 조회", description = "공고 ID를 기준으로 특정 채용 공고를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 특정 공고 조회 완료"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 공고를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public JobPostingDto getJobById(@PathVariable Long id) {
        return jobPostingService.getJobById(id);
    }

    /** 특정 공고 상세 조회 */
    @Operation(summary = "특정 공고 상세 조회", description = "공고 ID를 기준으로 해당 공고의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 공고 상세 조회 완료"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 상세 정보를 찾을 수 없음")
    })
    @GetMapping("/{id}/detail")
    public JobPostingDetailDto getJobDetail(@PathVariable Long id) throws Exception {
        return jobPostingService.getJobDetail(id);
    }
}
