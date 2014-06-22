package it.breex.bus.impl.jms;

import it.breex.bus.event.AbstractResponseEvent;
import it.breex.bus.event.EventData;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;
import it.breex.bus.impl.AbstractEventManager;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

public class JmsEventManager extends AbstractEventManager {

	private final static String DEFAULT_REQUEST_QUEUE = "breexDefaulRequestQueue";
	private final String nodeId = UUID.randomUUID().toString();
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
						getLogger().debug("Event Response received. Event name: [{}], sender id: [{}]", eventData.getName(),
								eventData.getSenderId());

						//logger.debug("Event Response received. Event name: [{}], sender id: [{}]", eventData.eventId.eventName, eventData.eventId.nodeId);
						AbstractResponseEvent responseEvent = new AbstractResponseEvent(eventData) {
						};
						processResponse(responseEvent, getResponseHandlers().remove(eventData.getId()));
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
	protected <I, O> void prepareResponse(EventData<I> requestEventData, EventData<O> responseEventData) {
		try {
			Message responseMessage = session.createObjectMessage(responseEventData);
			responseMessageProducer.send((Destination) requestEventData.getTransportData(), responseMessage);
		} catch (JMSException e) {
			new RuntimeException(e);
		}

	}

	@Override
	protected <I, O> void registerCallback(String eventName, EventHandler<RequestEvent<I, O>> eventHandler) {
		getLogger().debug("Registering event. Event name: [{}]", eventName);
		MessageConsumer eventConsumer;
		try {
			eventConsumer = session.createConsumer(requestQueue, "JMSCorrelationID='" + eventName + "'");
			eventConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					EventData<I> requestEventData;
					try {
						requestEventData = (EventData<I>) ((ObjectMessage) message).getObject();

						getLogger().debug("Received event. Event name: [{}] CorrelationID: [{}]", requestEventData.getName(),
								message.getJMSCorrelationID());
						processRequest(requestEventData);
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
	protected <I> void prepareRequest(EventData<I> eventData) {
		try {
			eventData.setTransportData(responseQueue);
			ObjectMessage message = session.createObjectMessage(eventData);
			message.setJMSCorrelationID(eventData.getName());
			message.setJMSReplyTo(responseQueue);
			requestMessageProducer.send(message);
		} catch (JMSException e) {
			new RuntimeException(e);
		}
	}

}
