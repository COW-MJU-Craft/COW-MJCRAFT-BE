package com.example.cowmjucraft.domain.sns.entity;

public enum SnsType {
    INSTAGRAM("instagram"),
    KAKAO("kakao"),
    ETC("etc");

    private final String iconKey;

    SnsType(String iconKey) {
        this.iconKey = iconKey;
    }

    public String getIconKey() {
        return iconKey;
    }
}
