package it.breex.bus.impl.hazelcast;

import it.breex.bus.impl.AbstractEventManager;
import it.breex.bus.impl.EventData;
import it.breex.bus.impl.EventHandler;
import it.breex.bus.impl.EventId;
import it.breex.bus.impl.EventResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.MultiMap;

public class HazelcastEventManager extends AbstractEventManager {

	private final String NODES_MAP = "cluster-nodes";
	private final String REQUEST_QUEUE_PREFIX = "request-";
	private final String RESPONSE_QUEUE_PREFIX = "response-";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String nodeId = UUID.randomUUID().toString();
	private final HazelcastInstance hazelcastInstance;
	private final Map<String, EventHandler> eventHandlers = new HashMap<>();
	private final Map<String, EventResponse> responseHandlers = new ConcurrentHashMap<>();
	private final MultiMap<String, String> nodeEventsMap;
	private final NodeIterator nodeIterator;

	public HazelcastEventManager(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;

		// nodeId = hazelcastInstance.getCluster().getLocalMember().getUuid();
		nodeEventsMap = this.hazelcastInstance.getMultiMap(NODES_MAP);
		nodeIterator = new NodeIterator(nodeEventsMap);
		nodeEventsMap.addEntryListener(new EntryListener<String, String>() {

			@Override
			public void entryUpdated(EntryEvent<String, String> event) {
				refreshIterator();
			}

			@Override
			public void entryRemoved(EntryEvent<String, String> event) {
				refreshIterator();
			}

			@Override
			public void entryEvicted(EntryEvent<String, String> event) {
				refreshIterator();
			}

			@Override
			public void entryAdded(EntryEvent<String, String> event) {
				refreshIterator();
			}
		}, false);

		refreshIterator();
	}

	@Override
	public String getLocalNodeId() {
		return nodeId;
	}

	@Override
	public <I,O> void register(final String eventName, final EventHandler<I,O> eventHandler) {

		logger.debug("registering event [{}]", eventName);
		eventHandlers.put(eventName, eventHandler);

		MessageListener<EventData<I>> requestMessageListener = new MessageListener<EventData<I>>() {
			@Override
			public void onMessage(Message<EventData<I>> message) {
				EventData<I> requestEventData = message.getMessageObject();
				EventData<O> responseEventData = processRequest(requestEventData, eventHandler);
				hazelcastInstance.getTopic(getResponseTopicName(eventName, requestEventData.eventId.nodeId)).publish(responseEventData);
			}
		};
		ITopic<EventData<I>> requestTopic = hazelcastInstance.getTopic(getRequestTopicName(eventName, nodeId));
		requestTopic.addMessageListener(requestMessageListener);

		MessageListener<EventData<O>> responseMessageListener = new MessageListener<EventData<O>>() {
			@Override
			public void onMessage(Message<EventData<O>> message) {
				EventData<O> eventData = message.getMessageObject();
				logger.debug("Event Response received. Event name: [{}], sender id: [{}]", eventData.eventId.eventName, eventData.eventId.nodeId);
				EventResponse<O> eventResponse = responseHandlers.remove(eventData.eventId.eventId);
				processResponse(eventData, eventResponse);
			}
		};
		ITopic<EventData<O>> responseTopic = hazelcastInstance.getTopic(getResponseTopicName(eventName, nodeId));
		responseTopic.addMessageListener(responseMessageListener);

		nodeEventsMap.put(eventName, nodeId);
	}

	@Override
	public <I,O> void publish(String eventName, EventResponse<O> eventResponse, I args ) {
		EventId eventId = new EventId(nodeId, eventName, UUID.randomUUID().toString());
		EventData<I> eventData = new EventData<>(eventId, args);
		logger.debug("Publish event. Event name: [{}], sender id: [{}]", eventId.eventName, eventId.nodeId);
		String randomNodeId = nodeIterator.next(eventName);
		responseHandlers.put(eventId.eventId, eventResponse);
		hazelcastInstance.getTopic(getRequestTopicName(eventName, randomNodeId)).publish(eventData);
	}


	private synchronized void refreshIterator() {
		nodeIterator.refreshIterators();
	}

	private String getRequestTopicName(String eventName, String nodeId) {
		return REQUEST_QUEUE_PREFIX + nodeId + "-" + eventName;
	}

	private String getResponseTopicName(String eventName, String nodeId) {
		return RESPONSE_QUEUE_PREFIX + nodeId + "-" + eventName;
	}

}
