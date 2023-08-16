package com.ts.core.eventbus.base.events;


import java.util.Date;
import java.util.UUID;

public class IntegrationEvent {

    private final UUID id;
    private final Date createdDate;

    public IntegrationEvent() {
        this.id = UUID.randomUUID();
        this.createdDate = new Date();
    }
    private Object source;
    public Object getSource(){
        return this.source;
    }
    public IntegrationEvent(UUID id, Date createdDate,Object source) {
        this.id = id;
        this.createdDate = createdDate;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }
}
