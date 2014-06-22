package it.breex.bus.impl.jms;

import static org.junit.Assert.assertEquals;
import it.breex.bus.BaseTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;

public class JMSQueueTest extends BaseTest {

	@Resource
	private ConnectionFactory jmsConnectionFactory;

	@Test
	public void testJmsQueueConfig() throws InterruptedException, JMSException {

		Connection connection = jmsConnectionFactory.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue queue = session.createQueue(UUID.randomUUID().toString());
		MessageProducer messageProducer = session.createProducer(queue);


		final StringBuilder stringBuilder = new StringBuilder();
		final CountDownLatch countDownLatch = new CountDownLatch(2);
		MessageConsumer messageConsumer = session.createConsumer(queue);
		messageConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				TextMessage textMessage = (TextMessage) message;
				try {
					getLogger().info("Received Message: [{}]", textMessage.getText());
					stringBuilder.append(textMessage.getText());
					countDownLatch.countDown();
				} catch (JMSException e) {
					getLogger().error(e.getMessage(), e);
				}
			}
		});

		connection.start();

		Message worldMessage = session.createTextMessage("World");
		messageProducer.send(worldMessage);
		messageProducer.send(worldMessage);

		countDownLatch.await(5, TimeUnit.SECONDS);
		assertEquals("WorldWorld", stringBuilder.toString());

		session.close();
		connection.close();
	}

	@Test
	public void testJmsQueueWithCorrelationId() throws InterruptedException, JMSException {

		final Connection connection = jmsConnectionFactory.createConnection();
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		final Queue queue = session.createQueue(UUID.randomUUID().toString());
		final MessageProducer messageProducer = session.createProducer(queue);

		final CountDownLatch countDownLatch = new CountDownLatch(20);

		MessageConsumer messageConsumerOne = session.createConsumer(queue, "JMSCorrelationID='correlationIdOne'");
		messageConsumerOne.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				TextMessage textMessage = (TextMessage) message;
				try {
					getLogger().info("ConsumerOne - Received Message: [{}]", textMessage.getText());
					assertEquals("correlationIdOne", textMessage.getJMSCorrelationID());
					countDownLatch.countDown();
				} catch (JMSException e) {
					getLogger().error(e.getMessage(), e);
				}
			}
		});

		MessageConsumer messageConsumerTwo = session.createConsumer(queue, "JMSCorrelationID='correlationIdTwo'");
		messageConsumerTwo.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				TextMessage textMessage = (TextMessage) message;
				try {
					getLogger().info("ConsumerTwo - Received Message: [{}]", textMessage.getText());
					assertEquals("correlationIdTwo", textMessage.getJMSCorrelationID());
					countDownLatch.countDown();
				} catch (JMSException e) {
					getLogger().error(e.getMessage(), e);
				}
			}
		});

		connection.start();

		for (int i = 0; i < (countDownLatch.getCount() / 2); i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Message helloMessageOne = session.createTextMessage("Hello One");
						helloMessageOne.setJMSCorrelationID("correlationIdOne");
						messageProducer.send(helloMessageOne);

						Message helloMessageTwo = session.createTextMessage("Hello Two");
						helloMessageTwo.setJMSCorrelationID("correlationIdTwo");
						messageProducer.send(helloMessageTwo);
					} catch (JMSException e) {
						getLogger().error(e.getMessage(), e);
					}
				}
			}).start();
		}

		countDownLatch.await(10, TimeUnit.SECONDS);
		assertEquals(0l, countDownLatch.getCount());

		session.close();
		connection.close();
	}

	@Test
	public void testJmsQueueRequestReplay() throws InterruptedException, JMSException {

		final Connection connection = jmsConnectionFactory.createConnection();
		connection.start();
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		final Queue queue = session.createQueue(UUID.randomUUID().toString());
		final Queue replayQueue = session.createTemporaryQueue();

		// The message consumer
		final CountDownLatch countDownLatch = new CountDownLatch(2);
		MessageConsumer messageConsumer = session.createConsumer(queue);
		final MessageProducer replyMessageProcucer = session.createProducer(replayQueue);
		messageConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				TextMessage textMessage = (TextMessage) message;
				try {
					getLogger().info("Received Message: [{}]", textMessage.getText());
					String replyText = "reply to '" + textMessage.getText() + "'";
					replyMessageProcucer.send(session.createTextMessage(replyText));
					countDownLatch.countDown();
				} catch (JMSException e) {
					getLogger().error(e.getMessage(), e);
				}
			}
		});

		// The replay consumer
		final StringBuilder stringBuilder = new StringBuilder();
		MessageConsumer replayMessageConsumer = session.createConsumer(replayQueue);
		replayMessageConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				TextMessage textMessage = (TextMessage) message;
				try {
					getLogger().info("Replay received: [{}]", textMessage.getText());
					stringBuilder.append(textMessage.getText());
					countDownLatch.countDown();
				} catch (JMSException e) {
					getLogger().error(e.getMessage(), e);
				}
			}
		});

		MessageProducer messageProducer = session.createProducer(queue);
		Message worldMessage = session.createTextMessage("Hello World");
		messageProducer.send(worldMessage);

		countDownLatch.await(5, TimeUnit.SECONDS);
		assertEquals("reply to 'Hello World'", stringBuilder.toString());

		session.close();
		connection.close();
	}
}
