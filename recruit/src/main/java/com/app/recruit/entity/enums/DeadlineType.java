package com.app.recruit.entity.enums;

public enum DeadlineType {
    DATE("날짜"),
    TODAY("오늘마감"),
    TOMORROW("내일마감"),
    ALWAYS("상시채용"),
    UNTIL_FILLED("채용시");

    private final String label;

    DeadlineType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}