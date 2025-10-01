package com.app.recruit.controller;

import com.app.recruit.dto.CompanyDto;
import com.app.recruit.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruit/companies")
@RequiredArgsConstructor
@Tag(name = "Company API", description = "회사 정보 조회 API")
public class CompanyController {

    private final CompanyService companyService;

    /** 전체 회사 조회 */
    @Operation(summary = "전체 회사 조회", description = "DB에 저장된 모든 회사 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 회사 목록 반환"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping
    public List<CompanyDto> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    /** 특정 회사 조회 */
    @Operation(summary = "특정 회사 조회", description = "회사 ID를 기반으로 해당 회사를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 회사 정보 반환"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 회사 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/{id}")
    public CompanyDto getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id);
    }
}
