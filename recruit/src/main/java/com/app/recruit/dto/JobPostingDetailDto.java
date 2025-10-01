package com.app.recruit.dto;

import com.app.recruit.entity.JobPostingDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JobPostingDetailDto {
    private List<String> tasks;
    private List<String> requirements;
    private List<String> preferred;
    private List<String> conditions;
    private List<String> procedure;
    private String welfare; // JSON 그대로
    private String workAddress;
    private String subway;
    private List<String> detailImages;

    public static JobPostingDetailDto fromEntity(JobPostingDetail detail, ObjectMapper mapper) {
        try {
            return JobPostingDetailDto.builder()
                    .tasks(mapper.readValue(detail.getTasks(), new TypeReference<>() {}))
                    .requirements(mapper.readValue(detail.getRequirements(), new TypeReference<>() {}))
                    .preferred(mapper.readValue(detail.getPreferred(), new TypeReference<>() {}))
                    .conditions(mapper.readValue(detail.getConditions(), new TypeReference<>() {}))
                    .procedure(mapper.readValue(detail.getProcedure(), new TypeReference<>() {}))
                    .welfare(detail.getWelfare())
                    .workAddress(detail.getWorkAddress())
                    .subway(detail.getSubway())
                    .detailImages(mapper.readValue(detail.getDetailImages(), new TypeReference<>() {}))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("JobPostingDetailDto 변환 실패", e);
        }
    }
}
