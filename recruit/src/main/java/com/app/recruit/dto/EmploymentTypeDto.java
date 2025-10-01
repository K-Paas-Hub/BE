package com.app.recruit.dto;

import com.app.recruit.entity.EmploymentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmploymentTypeDto {
    private String employmentName;

    public static EmploymentTypeDto fromEntity(EmploymentType type) {
        return EmploymentTypeDto.builder()
                .employmentName(type.getEmploymentName())
                .build();
    }
}
