package com.app.backend.entity.type;

public enum Authority {
    USER, ADMIN;

    private static final String ROLE_PREFIX = "ROLE_";

    public String getSecurityRole(){
        return ROLE_PREFIX+name();
    }
}
