package it.breex.bus.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsynchEventManager extends AbstractEventManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String nodeId = UUID.randomUUID().toString();
	private final Map<String, EventHandler> eventHandlers = new HashMap<>();
	private final ExecutorService executorService;

	public AsynchEventManager(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public String getLocalNodeId() {
		return nodeId;
	}

	@Override
	public <I, O> void publish(final String eventName, final EventResponse<O> eventResponse, final I args) {
		EventId eventId = new EventId(nodeId, eventName, randomUniqueId());
		final EventData<I> eventData = new EventData<>(eventId, args);
		logger.debug("Publish event. Event name: [{}], sender id: [{}]", eventId.eventName, eventId.nodeId);
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				EventData<O> responseEventData = processRequest(eventData, eventHandlers.get(eventName));
				processResponse(responseEventData, eventResponse);
			}
		});
	}

	@Override
	public <I, O> void register(String eventName, EventHandler<I, O> eventHandler) {
		eventHandlers.put(eventName, eventHandler);
	}

}
