package com.app.recruit.dto;

import com.app.recruit.entity.Category;
import com.app.recruit.entity.EmploymentType;
import com.app.recruit.entity.JobPosting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingDto {
    private String jobTitle;
    private String jobRegion;
    private String jobExperience;
    private String jobEducation;
    private String salary;
    private LocalDate jobUploadDate;
    private LocalDate jobDeadline;
    private String jobStatus;
    private String postLink;
    private String companyName;
    private List<String> categories;
    private List<String> employmentTypes;

    public static JobPostingDto fromEntity(JobPosting e) {
        return JobPostingDto.builder()
                .jobTitle(e.getJobTitle())
                .jobRegion(e.getJobRegion())
                .jobExperience(e.getJobExperience())
                .jobEducation(e.getJobEducation())
                .salary(e.getSalary())
                .jobUploadDate(e.getJobUploadDate())
                .jobDeadline(e.getJobDeadline())
                .jobStatus(e.getJobStatus())
                .postLink(e.getPostLink())
                .companyName(e.getCompany() != null ? e.getCompany().getCompanyName() : null)
                .categories(e.getCategories() != null
                        ? e.getCategories().stream().map(Category::getCategoryName).toList()
                        : null)
                .employmentTypes(e.getEmploymentTypes() != null
                        ? e.getEmploymentTypes().stream().map(EmploymentType::getEmploymentName).toList()
                        : null)
                .build();
    }
}
