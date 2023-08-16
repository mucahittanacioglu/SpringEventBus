package com.ts.core.eventbus.base;

public class SubscriptionInfo {

    private final Class<?> handlerType;

    public SubscriptionInfo(Class<?> handlerType) {
        if (handlerType == null) {
            throw new IllegalArgumentException("Handler type cannot be null");
        }
        this.handlerType = handlerType;
    }

    public static SubscriptionInfo typed(Class<?> handlerType) {
        return new SubscriptionInfo(handlerType);
    }

    public Class<?> getHandlerType() {
        return this.handlerType;
    }
}
