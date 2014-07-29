package it.breex.bus.impl.hazelcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.breex.bus.BaseTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.junit.Test;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class NodeIteratorTest extends BaseTest  {

	@Resource
	private HazelcastInstance hazelcastInstanceOne;

	@Test
	public void test() throws InterruptedException {
		MultiMap<String, Integer> map = hazelcastInstanceOne.getMultiMap(UUID.randomUUID().toString());
		NodeIterator<String, Integer> nodeIterator = new NodeIterator<>(map);

		String eventName1 = "eventName1";
		assertNull(nodeIterator.next(eventName1));

		Integer value1 = 1;
		map.put(eventName1, 1);
		assertEquals(Integer.valueOf(1), nodeIterator.next(eventName1));
		assertEquals(Integer.valueOf(1), nodeIterator.next(eventName1));
		assertEquals(Integer.valueOf(1), nodeIterator.next(eventName1));

		CountDownLatch ccl = waitForMapChange(map);
		map.put(eventName1, 2);
		ccl.await();

		assertEquals(Integer.valueOf(1), nodeIterator.next(eventName1));
		assertEquals(Integer.valueOf(2), nodeIterator.next(eventName1));
		assertEquals(Integer.valueOf(1), nodeIterator.next(eventName1));
		assertEquals(Integer.valueOf(2), nodeIterator.next(eventName1));

		ccl = waitForMapChange(map);
		assertTrue(map.remove(eventName1, value1));
		ccl.await();

		getLogger().info("map content: " + map.get(eventName1));

		assertEquals(Integer.valueOf(2), nodeIterator.next(eventName1));
		assertEquals(Integer.valueOf(2), nodeIterator.next(eventName1));
		assertEquals(Integer.valueOf(2), nodeIterator.next(eventName1));

	}

	private <K, V> CountDownLatch waitForMapChange(MultiMap<K, V> nodeEventsMap) throws InterruptedException {
		final CountDownLatch ccl = new CountDownLatch(1);
		nodeEventsMap.addEntryListener(new EntryListener<K, V>() {

			@Override
			public void entryUpdated(EntryEvent<K, V> event) {
				getLogger().info("updated");
				ccl.countDown();
			}

			@Override
			public void entryRemoved(EntryEvent<K, V> event) {
				getLogger().info("removed");
				ccl.countDown();
			}

			@Override
			public void entryEvicted(EntryEvent<K, V> event) {
				getLogger().info("evicted");
				ccl.countDown();
			}

			@Override
			public void entryAdded(EntryEvent<K, V> event) {
				getLogger().info("added");
				ccl.countDown();
			}
		}, false);
		return ccl;
	}

}
