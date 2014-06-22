package it.breex.eventbus;

import it.breex.eventbus.impl.EventHandler;
import it.breex.eventbus.impl.EventResponse;

public interface EventManager {

	String getLocalNodeId();

	<I, O> void publish(String eventName, EventResponse<O> eventResponse, I args);

	<I, O> void register(String eventName, EventHandler<I, O> eventHandler);
}
