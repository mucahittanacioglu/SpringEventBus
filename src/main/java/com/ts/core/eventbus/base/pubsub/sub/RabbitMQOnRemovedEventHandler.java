package com.ts.core.eventbus.base.pubsub.sub;

import com.ts.core.eventbus.base.pubsub.sub.OnRemovedEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class RabbitMQOnRemovedEventHandler implements OnRemovedEventHandler {
    private List<Consumer<?>> consumers=null;
    private List<?> consumables = null;

    public <T> RabbitMQOnRemovedEventHandler() {
        this.consumers = new ArrayList<>();
        this.consumables = new ArrayList<>();
    }

    public void setConsumables(List<?> consumables) {
        this.consumables = consumables;
    }
    public void addConsumer(Consumer<?> consumer){
        this.consumers.add(consumer);
    }

    @Override
    public <T> void handle(Object source, String eventName,T ... consumables){

        if (consumers.size() != consumables.length) {
            throw new IllegalArgumentException("The number of consumers must match the number of consumables");
        }

        for (int i = 0; i < consumers.size(); i++) {
            Consumer<T> consumer = (Consumer<T>) consumers.get(i);
            T consumable = consumables[i];
            consumer.accept(consumable);
        }


    }
}
