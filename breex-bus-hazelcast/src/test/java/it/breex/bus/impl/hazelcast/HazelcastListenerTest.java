package it.breex.bus.impl.hazelcast;

import it.breex.bus.BaseTest;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

public class HazelcastListenerTest extends BaseTest {

	@Resource
	private HazelcastInstance hazelcastInstanceOne;

	@Test
	public void testHazelcastQueueListener() throws InterruptedException {

		IQueue<String> queue = hazelcastInstanceOne.getQueue(UUID.randomUUID().toString());

		queue.addItemListener(new ItemListener<String>() {
			@Override
			public void itemRemoved(ItemEvent<String> item) {
			}

			@Override
			public void itemAdded(ItemEvent<String> item) {
				getLogger().info("Added: " + item.getItem());
			}
		}, true);

		queue.addItemListener(new ItemListener<String>() {

			@Override
			public void itemRemoved(ItemEvent<String> item) {
			}

			@Override
			public void itemAdded(ItemEvent<String> item) {
				getLogger().info("Added: " + item.getItem());
			}
		}, true);

		queue.add("hello world");

		Thread.sleep(500);

	}
}
