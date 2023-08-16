package com.ts.core.eventbus.base.pubsub.pub;

import com.ts.core.eventbus.base.pubsub.sub.RabbitMQOnRemovedEventHandler;
import org.springframework.stereotype.Component;

import java.util.EventListener;
import java.util.function.Consumer;

@Component
public class RabbitMqOnRemovedEventHandlerPub extends IPublisher<RabbitMQOnRemovedEventHandler> {

    private static RabbitMqOnRemovedEventHandlerPub instance;

    @Override
    public <T> void raiseEvent(Object source, String eventName, T ... consumables) {

        for (RabbitMQOnRemovedEventHandler listener : super.getListeners()) {
            listener.handle(source,eventName,consumables);
        }
    }
    public static synchronized RabbitMqOnRemovedEventHandlerPub getInstance() {
        if (instance == null) {
            instance = new RabbitMqOnRemovedEventHandlerPub();
        }
        return instance;
    }

}
