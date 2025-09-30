package com.app.recruit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_jobposting_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPostingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private JobPosting jobPosting;

    @Column(columnDefinition = "LONGTEXT")
    @Lob private String tasks;

    @Column(columnDefinition = "LONGTEXT")
    @Lob private String requirements;

    @Column(columnDefinition = "LONGTEXT")
    @Lob private String preferred;

    @Column(columnDefinition = "LONGTEXT")
    @Lob private String conditions;

    @Column(name = "job_procedure", columnDefinition = "LONGTEXT")  // procedure -> mysql 예약어임
    @Lob private String procedure;

    @Column(columnDefinition = "LONGTEXT")
    @Lob private String welfare; // JSON string

    @Column(columnDefinition = "LONGTEXT")
    @Lob private String detailImages; // joined URLs

    @Column(columnDefinition = "LONGTEXT")
    @Lob private String workAddress;

    private String subway;

    @Builder
    public JobPostingDetail(JobPosting jobPosting, String tasks, String requirements,
                            String preferred, String conditions, String procedure,
                            String welfare, String detailImages, String workAddress, String subway) {
        this.jobPosting = jobPosting;
        this.tasks = tasks;
        this.requirements = requirements;
        this.preferred = preferred;
        this.conditions = conditions;
        this.procedure = procedure;
        this.welfare = welfare;
        this.detailImages = detailImages;
        this.workAddress = workAddress;
        this.subway = subway;
    }

    /** 업데이트 전용 메서드 */
    public void update(String tasks, String requirements, String preferred,
                       String conditions, String procedure, String welfare,
                       String detailImages, String workAddress, String subway) {
        this.tasks = tasks;
        this.requirements = requirements;
        this.preferred = preferred;
        this.conditions = conditions;
        this.procedure = procedure;
        this.welfare = welfare;
        this.detailImages = detailImages;
        this.workAddress = workAddress;
        this.subway = subway;
    }
}
