package com.app.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinForm {
    private String memberEmail;
    private String memberPhone;
    private String memberName;
    private String profileImageUrl;
}
