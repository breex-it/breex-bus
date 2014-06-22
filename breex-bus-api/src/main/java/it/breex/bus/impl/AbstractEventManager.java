package it.breex.bus.impl;

import it.breex.bus.EventManager;
import it.breex.bus.event.AbstractRequestEvent;
import it.breex.bus.event.EventData;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;
import it.breex.bus.event.ResponseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventManager implements EventManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Map<String, EventHandler> eventHandlers = new HashMap<>();
	private final Map<String, EventHandler> responseHandlers = new ConcurrentHashMap<>();

	protected <I,O> void processRequest(final EventData<I> eventData) {

		final AbstractRequestEvent<I, O> requestEvent = new AbstractRequestEvent<I, O>(eventData) {
			@Override
			public void reply(O response) {
				final EventData<O> responseEventData = new EventData<O>(eventData.getId(), getLocalNodeId(), eventData.getName(), response);
				prepareResponse(eventData, responseEventData);
			}
		};

		getLogger().debug("Event Received. Event name: [{}], sender id: [{}]", eventData.getName(), eventData.getSenderId());
		eventHandlers.get(eventData.getName()).process(requestEvent);
	}

	protected <O> void processResponse(ResponseEvent<O> responseEvent, final EventHandler<ResponseEvent<O>> eventHandler) {
		eventHandler.process(responseEvent);
	}

	protected String randomUniqueId() {
		return UUID.randomUUID().toString();
	}

	protected abstract <I,O> void prepareResponse(final EventData<I> requestEventData, final EventData<O> responseEventData);

	@Override
	public final <I, O> void register(String eventName, EventHandler<RequestEvent<I, O>> eventHandler) {
		eventHandlers.put(eventName, eventHandler);
		registerCallback(eventName, eventHandler);
	}

	protected abstract <I, O> void registerCallback(String eventName, EventHandler<RequestEvent<I, O>> eventHandler);

	@Override
	public final <I, O> void publish(final String eventName, final EventHandler<ResponseEvent<O>> eventHandler, final I message) {
		getLogger().debug("Publish event. Event name: [{}], sender id: [{}]", eventName, getLocalNodeId());
		EventData<I> eventData = new EventData<I>(randomUniqueId(), getLocalNodeId(), eventName, message);
		responseHandlers.put(eventData.getId(), eventHandler);
		prepareRequest(eventData);
	}

	protected abstract <I> void prepareRequest(final EventData<I> eventData);

	public Map<String, EventHandler> getResponseHandlers() {
		return responseHandlers;
	}

	public Logger getLogger() {
		return logger;
	}

}
