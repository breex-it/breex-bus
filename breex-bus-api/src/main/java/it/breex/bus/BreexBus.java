package it.breex.bus;

import it.breex.bus.event.EventHandler;
import it.breex.bus.event.EventResponse;

public interface BreexBus {

	<I, O> void publish(String eventName, EventResponse<O> eventResponse, I args);

	<I, O> void register(String eventName, EventHandler<I, O> eventHandler);

}
