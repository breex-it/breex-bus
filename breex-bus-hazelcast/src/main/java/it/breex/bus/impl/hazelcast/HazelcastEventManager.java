package it.breex.bus.impl.hazelcast;

import it.breex.bus.event.AbstractResponseEvent;
import it.breex.bus.event.EventData;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;
import it.breex.bus.impl.AbstractEventManager;

import java.util.UUID;

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
	private final String nodeId = UUID.randomUUID().toString();
	private final HazelcastInstance hazelcastInstance;
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

	private synchronized void refreshIterator() {
		nodeIterator.refreshIterators();
	}

	private String getRequestTopicName(String eventName, String nodeId) {
		return REQUEST_QUEUE_PREFIX + nodeId + "-" + eventName;
	}

	private String getResponseTopicName(String eventName, String nodeId) {
		return RESPONSE_QUEUE_PREFIX + nodeId + "-" + eventName;
	}

	@Override
	protected <I, O> void prepareResponse(EventData<I> requestEventData, final EventData<O> responseEventData) {
		hazelcastInstance.getTopic(getResponseTopicName(responseEventData.getName(), requestEventData.getSenderId())).publish(responseEventData);
	}

	@Override
	protected <I> void prepareRequest(EventData<I> eventData) {
		String randomNodeId = nodeIterator.next(eventData.getName());
		hazelcastInstance.getTopic(getRequestTopicName(eventData.getName(), randomNodeId)).publish(eventData);
	}

	@Override
	protected <I, O> void registerCallback(final String eventName, final EventHandler<RequestEvent<I, O>> eventHandler) {
		MessageListener<EventData<I>> requestMessageListener = new MessageListener<EventData<I>>() {
			@Override
			public void onMessage(Message<EventData<I>> message) {
				EventData<I> requestEventData = message.getMessageObject();
				processRequest(requestEventData);
			}
		};
		ITopic<EventData<I>> requestTopic = hazelcastInstance.getTopic(getRequestTopicName(eventName, nodeId));
		requestTopic.addMessageListener(requestMessageListener);


		MessageListener<EventData<O>> responseMessageListener = new MessageListener<EventData<O>>() {
			@Override
			public void onMessage(Message<EventData<O>> message) {
				EventData<O> eventData = message.getMessageObject();
				//logger.debug("Event Response received. Event name: [{}], sender id: [{}]", eventData.eventId.eventName, eventData.eventId.nodeId);
				AbstractResponseEvent<I, O> responseEvent = new AbstractResponseEvent<I, O>(eventData) {
				};
				processResponse(responseEvent, getResponseHandlers().remove(eventData.getId()));
			}
		};
		ITopic<EventData<O>> responseTopic = hazelcastInstance.getTopic(getResponseTopicName(eventName, nodeId));
		responseTopic.addMessageListener(responseMessageListener);

		nodeEventsMap.put(eventName, nodeId);

	}

}
