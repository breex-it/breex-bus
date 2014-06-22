package it.breex.bus.test.test;

import static org.junit.Assert.assertEquals;
import it.breex.bus.BreexBus;
import it.breex.bus.impl.EventData;
import it.breex.bus.impl.EventHandler;
import it.breex.bus.impl.EventResponse;
import it.breex.bus.test.BaseBBTest;
import it.breex.bus.test.config.TestCaseConfig;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class RequestResponseTest extends BaseBBTest {

	public RequestResponseTest(String testName, TestCaseConfig testCaseConfig) {
		super(testName, testCaseConfig);
	}

	@Test
	public void testEventReply() throws InterruptedException {
		final BreexBus eventBus = getTestCaseConfig().getBuses().get(0);

		final String eventName = "testEvent-" + UUID.randomUUID().toString();

		eventBus.register(eventName, new EventHandler<String, String>() {
			@Override
			public String process(EventData<String> eventData) {
				getLogger().debug("message: [{}]", eventData.args);
				return eventData.args + eventData.args;
			}
		});

		final CountDownLatch countDownLatch = new CountDownLatch(1);

		final String message = "hello world [" + new Random().nextInt() + "]";
		eventBus.publish(eventName, new EventResponse<String>() {
			@Override
			public void receive(String response) {
				getLogger().debug("Received reply : [{}]", response);
				assertEquals(message + message, response);
				countDownLatch.countDown();
			}
		}, message);

		countDownLatch.await();
		assertEquals(0l, countDownLatch.getCount());
	}

}
