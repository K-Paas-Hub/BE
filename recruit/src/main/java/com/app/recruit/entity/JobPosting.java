package com.app.recruit.entity;

import com.app.recruit.entity.enums.DeadlineType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_jobposting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;
    private String jobRegion;
    private String jobExperience;
    private String jobEducation;
    private String salary;

    private LocalDate jobUploadDate;

    @Enumerated(EnumType.STRING)   // enum을 문자열로 DB에 저장
    private DeadlineType deadlineType;
    private LocalDate jobDeadline;
    private String jobStatus;

    @Column(name = "post_url", length = 1000)
    private String postLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private JobPostingDetail detail;

    /** 직무분야 N:M 관계 */
    @ManyToMany
    @JoinTable(
            name = "tbl_jobposting_category",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    /** 고용형태 N:M 관계 */
    @ManyToMany
    @JoinTable(
            name = "tbl_jobposting_employment_type",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "employment_type_id")
    )
    private Set<EmploymentType> employmentTypes = new HashSet<>();

    private JobPosting(String jobTitle, String jobRegion, String jobExperience,
                       String jobEducation, String salary, LocalDate jobUploadDate,
                       DeadlineType deadlineType, LocalDate jobDeadline, String jobStatus,
                       Company company, String postLink) {
        this.jobTitle = jobTitle;
        this.jobRegion = jobRegion;
        this.jobExperience = jobExperience;
        this.jobEducation = jobEducation;
        this.salary = salary;
        this.jobUploadDate = jobUploadDate;
        this.deadlineType = deadlineType;
        this.jobDeadline = jobDeadline;
        this.jobStatus = jobStatus;
        this.company = company;
        this.postLink = postLink;
    }

    public static JobPosting create(String jobTitle, String jobRegion, String jobExperience,
                                    String jobEducation, String salary, LocalDate jobUploadDate,
                                    DeadlineType deadlineType, LocalDate jobDeadline, String jobStatus,
                                    Company company, String postLink) {
        return new JobPosting(jobTitle, jobRegion, jobExperience, jobEducation, salary,
                jobUploadDate, deadlineType, jobDeadline, jobStatus, company, postLink);
    }

    // 비즈니스 메서드
    public void close() {
        this.jobStatus = "closed";
    }
}
