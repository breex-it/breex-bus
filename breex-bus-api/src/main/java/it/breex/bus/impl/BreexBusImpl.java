package it.breex.bus.impl;

import it.breex.bus.BreexBus;
import it.breex.bus.EventManager;

public class BreexBusImpl implements BreexBus {

	// private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final EventManager eventManager;

	public BreexBusImpl(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	@Override
	public final <I, O> void register(final String eventName, final EventHandler<I, O> eventHandler) {
		eventManager.register(eventName, eventHandler);
	}

	@Override
	public final <I, O> void publish(String eventName, EventResponse<O> eventResponse, I args) {
		eventManager.publish(eventName, eventResponse, args);
	}

}
