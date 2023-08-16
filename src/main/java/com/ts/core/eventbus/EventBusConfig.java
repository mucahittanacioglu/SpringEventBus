package com.ts.core.eventbus;

public class EventBusConfig {

    private int connectionRetryCount = 5;

    private String defaultTopicName = "MyMicroServiceProject";

    private String eventBusConnectionString = "";

    private String subscriberClientAppName = "";

    private String eventNamePrefix = "";

    private String eventNameSuffix = "IntegrationEvent";

    private EventBusType eventBusType = EventBusType.RabbitMQ;

    private Object connection;

    public int getConnectionRetryCount() {
        return connectionRetryCount;
    }

    public void setConnectionRetryCount(int connectionRetryCount) {
        this.connectionRetryCount = connectionRetryCount;
    }

    public String getDefaultTopicName() {
        return defaultTopicName;
    }

    public void setDefaultTopicName(String defaultTopicName) {
        this.defaultTopicName = defaultTopicName;
    }

    public String getEventBusConnectionString() {
        return eventBusConnectionString;
    }

    public void setEventBusConnectionString(String eventBusConnectionString) {
        this.eventBusConnectionString = eventBusConnectionString;
    }

    public String getSubscriberClientAppName() {
        return subscriberClientAppName;
    }

    public void setSubscriberClientAppName(String subscriberClientAppName) {
        this.subscriberClientAppName = subscriberClientAppName;
    }

    public String getEventNamePrefix() {
        return eventNamePrefix;
    }

    public void setEventNamePrefix(String eventNamePrefix) {
        this.eventNamePrefix = eventNamePrefix;
    }

    public String getEventNameSuffix() {
        return eventNameSuffix;
    }

    public void setEventNameSuffix(String eventNameSuffix) {
        this.eventNameSuffix = eventNameSuffix;
    }

    public EventBusType getEventBusType() {
        return eventBusType;
    }

    public void setEventBusType(EventBusType eventBusType) {
        this.eventBusType = eventBusType;
    }

    public Object getConnection() {
        return connection;
    }

    public void setConnection(Object connection) {
        this.connection = connection;
    }

    public boolean isDeleteEventPrefix() {
        return eventNamePrefix != null && !eventNamePrefix.isEmpty();
    }

    public boolean isDeleteEventSuffix() {
        return eventNameSuffix != null && !eventNameSuffix.isEmpty();
    }
}

