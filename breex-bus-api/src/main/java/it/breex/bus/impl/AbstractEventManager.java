package it.breex.bus.impl;

import it.breex.bus.EventManager;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventManager implements EventManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected <I, O> EventData<O> processRequest(EventData<I> requestEventData, final EventHandler<I, O> eventHandler) {
		logger.debug("Event Received. Event name: [{}], sender id: [{}]", requestEventData.eventId.eventName,
				requestEventData.eventId.nodeId);
		O response = eventHandler.process(requestEventData);
		return new EventData<>(requestEventData.eventId, response);
	}

	protected <O> void processResponse(EventData<O> eventData, EventResponse<O> eventResponse) {
		eventResponse.receive(eventData.args);
	}

	protected String randomUniqueId() {
		return UUID.randomUUID().toString();
	}
}
