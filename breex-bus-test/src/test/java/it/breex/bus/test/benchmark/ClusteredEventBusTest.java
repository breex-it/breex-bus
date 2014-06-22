package it.breex.bus.test.benchmark;

import static org.junit.Assert.assertEquals;
import it.breex.bus.BreexBus;
import it.breex.bus.event.EventHandler;
import it.breex.bus.event.RequestEvent;
import it.breex.bus.event.ResponseEvent;
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

	class HelloEventHandler implements EventHandler<RequestEvent<String, String>> {
		@Override
		public void process(RequestEvent<String, String> event) {
			String message = event.getEventData().getMessage();
			getLogger().debug("message: [{}]", message);
			event.reply(message + message);
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
			eventBus.publish(eventName, new EventHandler<ResponseEvent<String>>() {

				@Override
				public void process(ResponseEvent<String> event) {
					getLogger().debug("Received reply : [{}]", event.getEventData().getMessage());
					assertEquals(message + message, event.getEventData().getMessage());
					countDownLatch.countDown();
				}
			}, message);

		}
	}
}
