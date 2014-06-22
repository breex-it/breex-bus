package it.breex.bus.test.benchmark;

import static org.junit.Assert.assertEquals;
import it.breex.bus.BreexBus;
import it.breex.bus.event.EventData;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.EventResponse;
import it.breex.bus.test.BaseBBTest;
import it.breex.bus.test.config.TestCaseConfig;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ClusteredEventBusTest extends BaseBBTest {

	public ClusteredEventBusTest(String testName, TestCaseConfig testCaseConfig) {
		super(testName, testCaseConfig);
	}

	int totalMessages = 10000;
	int totalRounds = 5;

	@Test
	public void testEventReplyOnMultipleNodes() throws InterruptedException {

		for (int round = 0; round < totalRounds; round++) {
			int remainingMessages = totalMessages;
			List<BreexBus> bbs = getTestCaseConfig().getBuses();

			final String eventName = "testEvent-" + UUID.randomUUID().toString();

			for (BreexBus bus : bbs) {
				bus.register(eventName, new HelloEventHandler());
			}

			CountDownLatch countDownLatch = new CountDownLatch(totalMessages);

			Date now = new Date();

			while (remainingMessages > 0) {
				for (BreexBus bb : bbs) {
					new Thread(new AsynchHelloSender(bb, eventName, countDownLatch)).start();
					remainingMessages--;
				}
			}

			countDownLatch.await(25 * totalMessages, TimeUnit.MILLISECONDS);
			assertEquals(0l, countDownLatch.getCount());
			getLogger().info("execution time for [{}] events: {}ms", totalMessages, new Date().getTime() - now.getTime());
		}
	}

	class HelloEventHandler implements EventHandler<String, String> {
		@Override
		public String process(EventData<String> eventData) {
			getLogger().debug("message: [{}]", eventData.args);
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
					getLogger().debug("Received reply : [{}]", response);
					assertEquals(message + message, response);
					countDownLatch.countDown();
				}
			}, message);
		}
	}
}
