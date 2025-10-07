package com.app.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_resume")
@Getter
@Setter
@NoArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "licence_id")
    private Long licenceId;

    @Column(name = "id2")
    private Long id2;

    @Column(name = "resume_name")
    private String resumeName;

    @Column(name = "resume_email")
    private String resumeEmail;

    @Column(name = "resume_phone")
    private String resumePhone;

    @Column(name = "resume_nationality")
    private String resumeNationality;

    @Column(name = "resume_visa_type")
    private String resumeVisaType;

    @Column(name = "resume_school")
    private String resumeSchool;

    @Column(name = "resume_experience")
    private String resumeExperience;

    @Column(name = "resume_whoami")
    private String resumeWhoami;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeJob> resumeJobs = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Licence> licences = new ArrayList<>();
}