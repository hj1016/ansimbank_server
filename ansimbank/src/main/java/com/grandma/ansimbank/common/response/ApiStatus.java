package com.grandma.ansimbank.common.response;

public enum ApiStatus {
    SUCCESS("success"),
    FAIL("fail"),
    ERROR("error");

    private final String status;
    ApiStatus(String s) { this.status = s; }
    public String getStatus() { return status; }
}
