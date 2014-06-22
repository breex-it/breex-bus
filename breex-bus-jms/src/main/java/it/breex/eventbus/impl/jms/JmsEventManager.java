package it.breex.eventbus.impl.jms;

import it.breex.eventbus.impl.AbstractEventManager;
import it.breex.eventbus.impl.EventData;
import it.breex.eventbus.impl.EventHandler;
import it.breex.eventbus.impl.EventId;
import it.breex.eventbus.impl.EventResponse;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsEventManager extends AbstractEventManager {

	private final static String DEFAULT_REQUEST_QUEUE = "breexDefaulRequestQueue";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String nodeId = UUID.randomUUID().toString();
	private final ConcurrentMap<String, EventResponse> responseHandlers = new ConcurrentHashMap<>();
	private final boolean transacted = false;
	private final int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
	private final Connection jmsConnection;
	private final Session session;
	private final Queue requestQueue;
	private final MessageProducer requestMessageProducer;
	private final Queue responseQueue;
	private final MessageProducer responseMessageProducer;

	public JmsEventManager(ConnectionFactory jmsConnectionFactory) {
		try {
			jmsConnection = jmsConnectionFactory.createConnection();
			jmsConnection.start();
			session = jmsConnection.createSession(transacted, acknowledgeMode);
			requestQueue = session.createQueue(DEFAULT_REQUEST_QUEUE);
			requestMessageProducer = session.createProducer(requestQueue);
			responseQueue = session.createTemporaryQueue();
			responseMessageProducer = session.createProducer(null);

			session.createConsumer(responseQueue).setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					try {
						EventData<?> eventData = (EventData<?>) ((ObjectMessage) message).getObject();
						logger.debug("Event Response received. Event name: [{}], sender id: [{}]", eventData.eventId.eventName,
								eventData.eventId.nodeId);
						EventResponse eventResponse = responseHandlers.remove(eventData.eventId.eventId);
						processResponse(eventData, eventResponse);
					} catch (JMSException e) {
						new RuntimeException(e);
					}
				}
			});

		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getLocalNodeId() {
		return nodeId;
	}

	@Override
	public <I,O> void register(final String eventName, final EventHandler<I,O> eventHandler) {

		logger.debug("Registering event. Event name: [{}]", eventName);
		MessageConsumer eventConsumer;
		try {
			eventConsumer = session.createConsumer(requestQueue, "JMSCorrelationID='" + eventName + "'");
			eventConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					EventData<I> requestEventData;
					try {
						requestEventData = (EventData<I>) ((ObjectMessage) message).getObject();
						logger.debug("Received event. Event name: [{}] CorrelationID: [{}]", requestEventData.eventId.eventName,
								message.getJMSCorrelationID());
						EventData<O> responseEventData = processRequest(requestEventData, eventHandler);
						Message responseMessage = session.createObjectMessage(responseEventData);
						responseMessage.setJMSCorrelationID(message.getJMSCorrelationID());
						responseMessageProducer.send(message.getJMSReplyTo(), responseMessage);
					} catch (JMSException e) {
						new RuntimeException(e);
					}
				}
			});
		} catch (JMSException e) {
			new RuntimeException(e);
		}
	}

	@Override
	public <I,O> void publish(String eventName, EventResponse<O> eventResponse, I args ) {
		EventId eventId = new EventId(nodeId, eventName, UUID.randomUUID().toString());
		EventData<I> eventData = new EventData<>(eventId, args);
		logger.debug("Publish event. Event name: [{}], sender id: [{}]", eventId.eventName, eventId.nodeId);
		responseHandlers.put(eventId.eventId, eventResponse);
		try {
			ObjectMessage message = session.createObjectMessage(eventData);
			message.setJMSCorrelationID(eventName);
			message.setJMSReplyTo(responseQueue);
			requestMessageProducer.send(message);
		} catch (JMSException e) {
			new RuntimeException(e);
		}
	}

}
