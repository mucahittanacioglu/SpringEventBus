package com.ts.core.eventbus.base.abstracts;

import com.ts.core.eventbus.base.SubscriptionInfo;
import com.ts.core.eventbus.base.events.IntegrationEvent;

import java.util.List;

public interface IEventBusSubscriptionManager {

        boolean isEmpty();

        <T extends IntegrationEvent,TH extends IIntegrationEventHandler<T>> void addSubscription(Class<T> eventType, Class<TH> handlerType);

        <T extends IntegrationEvent,TH extends IIntegrationEventHandler<T>> void removeSubscription(Class<T> eventType, Class<TH> handlerType);

        <T extends IntegrationEvent> boolean hasSubscriptionsForEvent(Class<T> eventType);

        boolean hasSubscriptionsForEvent(String eventName);

        Class<?> getEventTypeByName(String eventName);

        void clear();

        <T extends IntegrationEvent> List<SubscriptionInfo> getHandlersForEvent(Class<T> eventType);

        List<SubscriptionInfo> getHandlersForEvent(String eventName);

        <T extends IntegrationEvent> String getEventKey(Class<T> eventType);

        <T> void raiseOnEvenHandlerRemoved(String eventName, T ... consumables);


}
