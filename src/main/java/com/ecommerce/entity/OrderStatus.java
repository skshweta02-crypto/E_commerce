package com.ecommerce.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == next) {
            return true;
        }
        switch (this) {
            case PENDING:
                return next == CONFIRMED || next == CANCELLED;
            case CONFIRMED:
                return next == PROCESSING || next == CANCELLED;
            case PROCESSING:
                return next == SHIPPED || next == CANCELLED;
            case SHIPPED:
                return next == DELIVERED;
            case DELIVERED:
            case CANCELLED:
            default:
                return false;
        }
    }
}
