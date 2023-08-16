package com.ts.core.eventbus.factory;

import com.ts.core.eventbus.EventBusConfig;
import com.ts.core.eventbus.base.RabbitMQ.EventBusRabbitMQ;
import com.ts.core.eventbus.base.abstracts.IEventBus;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class EventBusFactory {

    public static IEventBus create(EventBusConfig config, ApplicationContext applicationContext) throws IOException, TimeoutException {
        return new EventBusRabbitMQ(config,applicationContext);
    }
}
