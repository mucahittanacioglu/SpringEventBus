package com.ts.core.eventbus.base.pubsub.pub;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.function.Consumer;

public abstract class IPublisher<T extends EventListener> {
    private final List<T> listeners = new ArrayList<>();

    public List<T> getListeners(){
        return this.listeners;
    }
    public void addOnEventRemovedListener(T listener) {
        this.listeners.add(listener);
    }

    public void removeOnEventRemovedListener(T listener) {
        this.listeners.remove(listener);
    }

    public abstract <TH> void raiseEvent(Object source, String eventName,TH ... consumables);
}
