package it.breex.eventbus.impl;

import it.breex.eventbus.MultiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchEventManager extends AbstractEventManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String nodeId = UUID.randomUUID().toString();
	private final MultiMap<String, String> eventsPerNode = new MultiMapImpl<>();
	private final Map<String, EventHandler> eventHandlers = new HashMap<>();

	@Override
	public String getLocalNodeId() {
		return nodeId;
	}

	@Override
	public <I, O> void publish(String eventName, EventResponse<O> eventResponse, I args) {
		EventId eventId = new EventId(nodeId, eventName, randomUniqueId());
		EventData<I> eventData = new EventData<>(eventId, args);
		logger.debug("Publish event. Event name: [{}], sender id: [{}]", eventId.eventName, eventId.nodeId);

		EventData<O> responseEventData = processRequest(eventData, eventHandlers.get(eventName));
		processResponse(responseEventData, eventResponse);
	}

	@Override
	public <I, O> void register(String eventName, EventHandler<I, O> eventHandler) {
		eventHandlers.put(eventName, eventHandler);
		eventsPerNode.put(eventName, nodeId);
	}

}
