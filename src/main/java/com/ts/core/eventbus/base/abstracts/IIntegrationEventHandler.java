package com.ts.core.eventbus.base.abstracts;

import com.ts.core.eventbus.base.events.IntegrationEvent;

import java.util.EventListener;
import java.util.concurrent.Future;

public interface IIntegrationEventHandler<T extends IntegrationEvent> extends EventListener {

    Future<Void> handle(T event);
}