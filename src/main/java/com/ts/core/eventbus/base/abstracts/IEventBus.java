package com.ts.core.eventbus.base.abstracts;

import com.ts.core.eventbus.base.events.IntegrationEvent;

public interface IEventBus extends AutoCloseable {

    void publish(IntegrationEvent event);

    <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void subscribe(Class<T> event,Class<TH> handler);

    <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void unsubscribe(Class<T> event,Class<TH> handler);

    @Override
    void close();
}
