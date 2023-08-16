package com.ts.core.eventbus.base.abstracts;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ts.core.eventbus.EventBusConfig;
import com.ts.core.eventbus.base.SubscriptionInfo;
import com.ts.core.eventbus.base.events.IntegrationEvent;
import com.ts.core.eventbus.base.pubsub.pub.RabbitMqOnRemovedEventHandlerPub;
import com.ts.core.eventbus.base.submanager.InMemoryEventBusSubscriptionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class BaseEventBus implements IEventBus {

    protected final ApplicationContext applicationContext;
    protected final IEventBusSubscriptionManager subscriptionManager;
    protected EventBusConfig eventBusConfig;


    protected BaseEventBus( EventBusConfig eventBusConfig,ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.eventBusConfig = eventBusConfig;
        this.subscriptionManager = new InMemoryEventBusSubscriptionManager(this::processEventName);
    }



    public String processEventName(String eventName) {
        if (eventBusConfig.isDeleteEventPrefix()) {
            eventName = eventName.replaceFirst(eventBusConfig.getEventNamePrefix(), "");
        }

        if (eventBusConfig.isDeleteEventSuffix()) {
            eventName = eventName.replaceFirst(eventBusConfig.getEventNameSuffix() + "$", "");
        }

        return eventName;
    }

    public void close() {
        eventBusConfig = null;
        subscriptionManager.clear();
    }

    public String getSubName(String eventName) {
        return eventBusConfig.getSubscriberClientAppName() + "." + processEventName(eventName);
    }

    public CompletableFuture<Boolean> processEvent(String eventName, String message) {
        eventName = processEventName(eventName);
        var processed = false;
        Gson gson = new Gson();

        if (subscriptionManager.hasSubscriptionsForEvent(eventName)) {
            List<SubscriptionInfo> subscriptions = subscriptionManager.getHandlersForEvent(eventName);

            for (SubscriptionInfo subscription : subscriptions) {
                 var handler = (IIntegrationEventHandler<IntegrationEvent>) applicationContext.getBean(subscription.getHandlerType());

                if (handler != null) {
                    try {
                        var eventType = subscriptionManager.getEventTypeByName(
                                String.format("%s%s%s", eventBusConfig.getEventNamePrefix(), eventName, eventBusConfig.getEventNameSuffix())
                        );

                        Object integrationEvent = gson.fromJson(message,eventType);
                        Future<Void> result = handler.handle((IntegrationEvent) integrationEvent);
                        result.get();

                    } catch (Exception e) {
                        // Handle exception
                        e.printStackTrace();
                    }
                }
            }
            processed = true;
        }

        return CompletableFuture.completedFuture(processed);
    }

    public abstract void publish(IntegrationEvent event);

    public abstract <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void subscribe(Class<T> event,Class<TH> handler);

    public abstract <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void unsubscribe(Class<T> event,Class<TH> handler);
}
