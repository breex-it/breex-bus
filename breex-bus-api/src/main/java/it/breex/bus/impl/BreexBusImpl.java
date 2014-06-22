package it.breex.bus.impl;

import it.breex.bus.BreexBus;
import it.breex.bus.EventManager;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;
import it.breex.bus.event.ResponseEvent;

public class BreexBusImpl implements BreexBus {

	// private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final EventManager eventManager;

	public BreexBusImpl(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	@Override
	public <I, O> void publish(String eventName, EventHandler<ResponseEvent<O>> eventHandler, I message) {
		eventManager.publish(eventName, eventHandler, message);
	}

	@Override
	public <I, O> void register(String eventName, EventHandler<RequestEvent<I, O>> eventHandler) {
		eventManager.register(eventName, eventHandler);
	}


}
