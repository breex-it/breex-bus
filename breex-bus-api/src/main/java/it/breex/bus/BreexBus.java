package it.breex.bus;

import it.breex.bus.impl.EventHandler;
import it.breex.bus.impl.EventResponse;

public interface BreexBus {

	<I, O> void publish(String eventName, EventResponse<O> eventResponse, I args);

	<I, O> void register(String eventName, EventHandler<I, O> eventHandler);

}
