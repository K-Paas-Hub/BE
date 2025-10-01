package com.app.recruit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tbl_employment_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmploymentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employmentName;

    @ManyToMany(mappedBy = "employmentTypes")
    private Set<JobPosting> jobPostings = new HashSet<>();

    @Builder
    public EmploymentType(String employmentName) {
        this.employmentName = employmentName;
    }
}
