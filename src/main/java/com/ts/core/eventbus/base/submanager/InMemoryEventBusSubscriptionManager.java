package com.ts.core.eventbus.base.submanager;

import com.ts.core.eventbus.base.SubscriptionInfo;
import com.ts.core.eventbus.base.abstracts.IEventBusSubscriptionManager;
import com.ts.core.eventbus.base.abstracts.IIntegrationEventHandler;
import com.ts.core.eventbus.base.events.IntegrationEvent;
import com.ts.core.eventbus.base.pubsub.pub.RabbitMqOnRemovedEventHandlerPub;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class InMemoryEventBusSubscriptionManager implements IEventBusSubscriptionManager {
    private final Map<String, List<SubscriptionInfo>> handlers;
    private final List<Class<?>> eventTypes;

    public boolean isEmpty() {
        return handlers.keySet().isEmpty();
    }

    private final Function<String, String> eventNameGetter;

    public InMemoryEventBusSubscriptionManager(Function<String, String> eventNameGetter) {
        this.handlers = new HashMap<>();
        this.eventTypes = new ArrayList<>();
        this.eventNameGetter = eventNameGetter;

    }

    public <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void addSubscription(Class<T> eventType, Class<TH> handlerType) {
        String eventName = getEventKey(eventType);
        addSubscription(eventName, handlerType);

        if (!eventTypes.contains(eventType)) {
            eventTypes.add(eventType);
        }
    }

    private void addSubscription(String eventName,Class<?> handlerType) {
        if (!hasSubscriptionsForEvent(eventName)) {
            handlers.put(eventName, new ArrayList<>());
        }

        if (handlers.get(eventName).stream().anyMatch(sub -> sub.getHandlerType() == handlerType)) {
            throw new IllegalArgumentException("Handler Type " + handlerType.getName() + " already registered for " + eventName);
        }

        handlers.get(eventName).add(SubscriptionInfo.typed(handlerType));
    }

    public void clear() {
        handlers.clear();
    }

    public <T extends IntegrationEvent> String getEventKey(Class<T> event) {
        String eventName = event.getSimpleName();
        return eventNameGetter.apply(eventName);
    }

    public Class<?> getEventTypeByName(String eventName) {
        return eventTypes.stream().filter(type -> type.getSimpleName().equals(eventName)).findFirst().orElse(null);
    }

    public <T extends IntegrationEvent> List<SubscriptionInfo> getHandlersForEvent(Class<T> eventType) {
        String key = getEventKey(eventType);
        return getHandlersForEvent(key);
    }

    public List<SubscriptionInfo> getHandlersForEvent(String eventName) {
        return handlers.get(eventName);
    }

    public <T extends IntegrationEvent> boolean hasSubscriptionsForEvent(Class<T> tClass) {
        String key = getEventKey(tClass);
        return hasSubscriptionsForEvent(key);
    }

    public boolean hasSubscriptionsForEvent(String eventName) {
        return handlers.containsKey(eventName);
    }

    public <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void removeSubscription(Class<T> eventType, Class<TH> handlerType) {
        SubscriptionInfo handlerToRemove = findSubscriptionToRemove(eventType, handlerType);
        String eventName = getEventKey(eventType);
        removeHandler(eventName, handlerToRemove);
    }

    private <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> SubscriptionInfo findSubscriptionToRemove(Class<T> eventType, Class<TH> handlerType) {
        if (!hasSubscriptionsForEvent(eventType.getName())) {
            return null;
        }

        return handlers.get(eventType.getName())
                .stream()
                .filter(sub -> sub.getHandlerType() == handlerType)
                .findFirst()
                .orElse(null);
    }

    private void removeHandler(String eventName, SubscriptionInfo subsToRemove) {
        if (subsToRemove != null) {
            handlers.get(eventName).remove(subsToRemove);

            if (handlers.get(eventName).isEmpty()) {
                handlers.remove(eventName);
                Class<?> eventTypeToRemove = getEventTypeByName(eventName);

                if (eventTypeToRemove != null) {
                    eventTypes.remove(eventTypeToRemove);
                }

                raiseOnEvenHandlerRemoved(eventName);
            }
        }
    }
    @Override
    public <T> void raiseOnEvenHandlerRemoved(String eventName, T ... consumables) {
        RabbitMqOnRemovedEventHandlerPub.getInstance().raiseEvent(this,eventName,consumables);
    }
}
