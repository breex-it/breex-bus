package it.breex.bus.impl.jms;

import static org.junit.Assert.assertEquals;
import it.breex.bus.BaseTest;
import it.breex.bus.BreexBus;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;
import it.breex.bus.event.ResponseEvent;
import it.breex.bus.impl.BreexBusImpl;

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

		eventBus.register(eventName, new EventHandler<RequestEvent<String, String>>() {

			@Override
			public void process(RequestEvent<String, String> event) {
				String value = event.getEventData().getMessage();
				getLogger().debug("message: [{}]", value);
				event.reply(value + value);
			}
		});

		int threadsQuantity = 1;
		final CountDownLatch countDownLatch = new CountDownLatch(threadsQuantity);

		Date now = new Date();

		for (int i=0; i<threadsQuantity; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					final String message = "hello world [" + new Random().nextInt() + "]";
					eventBus.publish(eventName, new EventHandler<ResponseEvent<String>>() {

						@Override
						public void process(ResponseEvent<String> event) {
							getLogger().debug("Received reply : [{}]", event.getEventData().getMessage());
							assertEquals(message + message, event.getEventData().getMessage());
							countDownLatch.countDown();
						}
					}, message);
				}
			}).start();
		}
		countDownLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0l, countDownLatch.getCount());
		getLogger().info("execution time for [{}] events: {}ms", threadsQuantity, new Date().getTime() - now.getTime());
	}

}
