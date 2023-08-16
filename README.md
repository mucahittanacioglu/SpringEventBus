An event bus package to use spring project. It is Spring Version of used EventBus package on [this](https://www.youtube.com/watch?v=ZFjqzUNluV8&list=PLRp4oRsit1bzd6v_1zwNjdBOnGNuvHjWy) Microservice project tutorial

There is only RabbitMQ implementation with basic sub-pub,however you can improve current implementation or create for other services by implementing [IEventBus](src/main/java/com/ts/core/eventbus/base/abstracts/IEventBus.java)

# Usage
## 1- Initialization
Create instance using EventBusFactory with given config its by default uses rabbitmq configs :
```java 
@Configuration
public class Config {
    @Autowired
    ApplicationContext ap;

    @Bean
    public IEventBus getEventBus() throws IOException, TimeoutException {
        
        // Config
        EventBusConfig config = new EventBusConfig();
        config.setConnectionRetryCount(5);
        config.setDefaultTopicName("MyMicroServiceProject");
        config.setEventNameSuffix("IntegrationEvent");
        config.setSubscriberClientAppName("OrderService");
        config.setEventBusType(EventBusType.RabbitMQ);
        
        // Create EventBus    
        return EventBusFactory.create(config, ap);         
    }
}
```
Define prefix and suffix of Events, and need to ***name Events in conformity with these rules*** to be able to correctly route messages.
## 2- Publish Event
To publish event need to create event class named according to suffix,prefix and need to inherit [IntegrationEvent](src/main/java/com/ts/core/eventbus/base/events/IntegrationEvent.java).
```java
public class CityCreatedIntegrationEvent extends IntegrationEvent {
    public City city;
}
```
After that publish it using event-bus:
```java
@Service
public class CityManager implements ICityService {

    @Qualifier("cityCrudRepository")
    @Autowired
    private ICityDal cityDal;
    @Autowired
    IEventBus eventBus;

    @Override
    public void add(City city) {
        this.cityDal.add(city);
        eventBus.publish(new CityCreatedIntegrationEvent(city));
    }
}
```

## 3- Create And Subscribe Queue
Subscribers creates queues and routingKey with current configurations defined as given Events class name without defined prefix and suffix.
```java
 eventBus.subscribe(CityCreatedIntegrationEvent.class, CityIntegrationEventHandler.class);
```
This will create Queue as OrderService.CityCreated under MyMicroServiceProject exchange.

To subscribe you need to create an EventHandler class for event you want to sub and need same event class you published before.
```java
public class CityCreatedIntegrationEvent extends IntegrationEvent {
    private City city;
}

@Component
public class CityIntegrationEventHandler implements IIntegrationEventHandler<CityCreatedIntegrationEvent> {
    @Override
    public Future<Void> handle(CityCreatedIntegrationEvent event) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        return (Future<Void>) executorService.submit(() -> System.out.println("CityIntegration event Handled for city: "+event.toString()));

    }
}
```
EventHandler class must inherit [IIntegrationEventHandler](src/main/java/com/ts/core/eventbus/base/abstracts/IIntegrationEventHandler.java) and register it to application context;

When event arrives to subscriber service's Queue it will be consumed with execution of the provided ***'handle()'*** method in EventHandler.