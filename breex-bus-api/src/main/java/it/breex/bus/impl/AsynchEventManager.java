package it.breex.bus.impl;

import it.breex.bus.event.AbstractResponseEvent;
import it.breex.bus.event.EventData;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class AsynchEventManager extends AbstractEventManager {

	private final String nodeId = UUID.randomUUID().toString();
	private final ExecutorService executorService;

	public AsynchEventManager(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public String getLocalNodeId() {
		return nodeId;
	}

	@Override
	protected <I> void prepareRequest(final EventData<I> eventData) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				processRequest(eventData);
			}
		});
	}

	@Override
	protected <I, O> void registerCallback(String eventName, EventHandler<RequestEvent<I, O>> eventHandler) {
	}

	@Override
	protected <I, O> void prepareResponse(EventData<I> requestEventData, final EventData<O> responseEventData) {
		AbstractResponseEvent<I, O> responseEvent = new AbstractResponseEvent<I, O>(responseEventData) {
		};
		processResponse(responseEvent, getResponseHandlers().remove(requestEventData.getId()));
	}

}
