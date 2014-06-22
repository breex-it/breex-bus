package it.breex.bus.impl;

import static org.junit.Assert.assertEquals;
import it.breex.bus.BaseTest;
import it.breex.bus.BreexBus;
import it.breex.bus.event.EventData;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.EventResponse;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ASynchBreexBusImplTest extends BaseTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testEventReply() throws InterruptedException {
		final BreexBus eventBus = new AsynchBreexBus(Executors.newFixedThreadPool(10));

		final String eventName = "testEvent-" + UUID.randomUUID().toString();

		eventBus.register(eventName, new EventHandler<String, String>() {
			@Override
			public String process(EventData<String> eventData) {
				logger.debug("message: [{}]", eventData.args);
				return eventData.args + eventData.args;
			}
		});

		int threadsQuantity = 1000;
		final CountDownLatch countDownLatch = new CountDownLatch(threadsQuantity);

		Date now = new Date();

		for (int i=0; i<threadsQuantity; i++) {
			new Thread(new Runnable() {
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
			}).start();
		}
		countDownLatch.await();
		logger.info("execution time for [{}] events: {}ms", threadsQuantity, new Date().getTime() - now.getTime());
	}

}
