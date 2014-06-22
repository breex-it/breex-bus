package it.breex.bus;

import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;
import it.breex.bus.event.ResponseEvent;

public interface EventManager {

	String getLocalNodeId();

	<I, O> void publish(String eventName, EventHandler<ResponseEvent<O>> eventHandler, I message);

	<I, O> void register(String eventName, EventHandler<RequestEvent<I, O>> eventHandler);

}
