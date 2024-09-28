package io.hhplus.tdd.point.dto;

public record PointRequest(long amount, long reqTime) {
    public PointRequest(long amount) {
        this(amount, System.currentTimeMillis());
    }
}