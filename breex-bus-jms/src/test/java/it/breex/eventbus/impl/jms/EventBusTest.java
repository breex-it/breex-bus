package it.breex.eventbus.impl.jms;

import static org.junit.Assert.assertEquals;
import it.breex.eventbus.BaseTest;
import it.breex.eventbus.BreexBus;
import it.breex.eventbus.impl.BreexBusImpl;
import it.breex.eventbus.impl.EventData;
import it.breex.eventbus.impl.EventHandler;
import it.breex.eventbus.impl.EventResponse;
import it.breex.eventbus.impl.jms.JmsEventManager;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBusTest extends BaseTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private ConnectionFactory jmsConnectionFactory;

	@Test
	public void testEventReplyOnSingleBus() throws InterruptedException {
		final BreexBus eventBus = new BreexBusImpl(new JmsEventManager(jmsConnectionFactory));

		final String eventName = "testEvent-" + UUID.randomUUID().toString();

		eventBus.register(eventName, new HelloEventHandler());

		int threadsQuantity = 1000;
		final CountDownLatch countDownLatch = new CountDownLatch(threadsQuantity);

		Date now = new Date();

		for (int i=0; i<threadsQuantity; i++) {
			new Thread(new AsynchHelloSender(eventBus, eventName, countDownLatch)).start();
		}

		countDownLatch.await(10, TimeUnit.SECONDS);
		assertEquals(0l, countDownLatch.getCount());
		logger.info("execution time for [{}] events: {}ms", threadsQuantity, new Date().getTime() - now.getTime());

	}

	class HelloEventHandler implements EventHandler<String, String> {
		@Override
		public String process(EventData<String> eventData) {
			logger.debug("message: [{}]", eventData.args);
			return eventData.args + eventData.args;
		}
	}

	class AsynchHelloSender implements Runnable {

		private final String eventName;
		private final BreexBus eventBus;
		private final CountDownLatch countDownLatch;

		AsynchHelloSender(BreexBus eventBus, String eventName, CountDownLatch countDownLatch) {
			this.eventBus = eventBus;
			this.eventName = eventName;
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void run() {
			final String message = "hello world [" + new Random().nextInt() + "]";
			eventBus.publish(eventName, new EventResponse<String>() {
				@Override
				public void receive(String response) {
					logger.debug("Received reply : [{}]", response);
					assertEquals(message + message, response);
					countDownLatch.countDown();
				}
			}, message);
		}
	}
}
