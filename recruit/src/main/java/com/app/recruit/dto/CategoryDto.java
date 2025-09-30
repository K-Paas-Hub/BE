package com.app.recruit.dto;

import com.app.recruit.entity.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDto {
    private String categoryName;

    public static CategoryDto fromEntity(Category category) {
        return CategoryDto.builder()
                .categoryName(category.getCategoryName())
                .build();
    }
}
