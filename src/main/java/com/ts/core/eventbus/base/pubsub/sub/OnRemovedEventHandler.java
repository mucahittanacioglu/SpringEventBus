package com.ts.core.eventbus.base.pubsub.sub;

import java.util.EventListener;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface OnRemovedEventHandler extends EventListener {
    public <T> void handle(Object source, String eventName, T ... consumables);
}
