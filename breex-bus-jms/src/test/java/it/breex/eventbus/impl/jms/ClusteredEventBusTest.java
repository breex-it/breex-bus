package it.breex.eventbus.impl.jms;

import static org.junit.Assert.assertEquals;
import it.breex.eventbus.BaseTest;
import it.breex.eventbus.BreexBus;
import it.breex.eventbus.impl.EventData;
import it.breex.eventbus.impl.EventHandler;
import it.breex.eventbus.impl.EventResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClusteredEventBusTest extends BaseTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private ConnectionFactory jmsConnectionFactory;
	private List<ConfigurableApplicationContext> contexts;

	int howManyNodes = 3;
	int messagesPerNode = 1000;

	@Before
	public void setUp() {
		contexts = new ArrayList<>();
		for (int i = 0; i < howManyNodes; i++) {
			contexts.add(new ClassPathXmlApplicationContext("classpath:spring-context.xml"));
		}
	}

	@After
	public void tearDown() {
		for (ConfigurableApplicationContext context : contexts) {
			context.close();
		}
	}

	@Test
	public void testEventReplyOnMultipleNodes() throws InterruptedException {
		for (int times = 0; times < 10; times++) {
			List<BreexBus> bbs = new ArrayList<>();
			bbs.add(new JmsBreexBus(jmsConnectionFactory));
			for (ConfigurableApplicationContext context : contexts) {
				bbs.add(new JmsBreexBus(context.getBean(ConnectionFactory.class)));
			}

			final String eventName = "testEvent-" + UUID.randomUUID().toString();

			List<CountDownLatch> latchs = new ArrayList<>();

			for (BreexBus bus : bbs) {
				bus.register(eventName, new HelloEventHandler());
				latchs.add(new CountDownLatch(messagesPerNode));
			}

			Date now = new Date();

			for (int i = 0; i < messagesPerNode; i++) {
				for (int j = 0; j < bbs.size(); j++) {
					new Thread(new AsynchHelloSender(bbs.get(j), eventName, latchs.get(j))).start();
				}
			}

			for (CountDownLatch countDownLatch : latchs) {
				countDownLatch.await(25, TimeUnit.SECONDS);
			}
			logger.info("execution time for [{}] events: {}ms", messagesPerNode * howManyNodes, new Date().getTime() - now.getTime());
		}
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
