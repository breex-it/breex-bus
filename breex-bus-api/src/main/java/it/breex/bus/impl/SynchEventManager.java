package it.breex.bus.impl;

import it.breex.bus.event.AbstractResponseEvent;
import it.breex.bus.event.EventData;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;

import java.util.UUID;

public class SynchEventManager extends AbstractEventManager {

	private final String nodeId = UUID.randomUUID().toString();

	@Override
	public String getLocalNodeId() {
		return nodeId;
	}

	@Override
	protected <I> void prepareRequest(final EventData<I> eventData) {
		processRequest(eventData);
	}

	@Override
	protected <I, O> void prepareResponse(EventData<I> requestEventData, final EventData<O> responseEventData) {
		AbstractResponseEvent<I, O> responseEvent = new AbstractResponseEvent<I, O>(responseEventData) {
		};
		processResponse(responseEvent, getResponseHandlers().remove(requestEventData.getId()));
	}

	@Override
	protected <I, O> void registerCallback(String eventName, EventHandler<RequestEvent<I, O>> eventHandler) {
	}

}
