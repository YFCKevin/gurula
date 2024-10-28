package com.yfckevin.memberservice.enums;

public enum Role {
    ADMIN(1, "管理員"),
    USER(2, "用戶");
    private int value;
    private String label;

    private Role() {
    }

    private Role(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    }


