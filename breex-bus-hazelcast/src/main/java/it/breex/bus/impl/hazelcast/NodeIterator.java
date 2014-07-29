package it.breex.bus.impl.hazelcast;

import it.breex.bus.util.RoundRobinIterator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MultiMap;

public class NodeIterator<KEY, VALUE> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<KEY, RoundRobinIterator<VALUE>> iterators = new ConcurrentHashMap<>();
	private final MultiMap<KEY, VALUE> nodeEventsMap;

	public NodeIterator(MultiMap<KEY, VALUE> nodeEventsMap) {
		this.nodeEventsMap = nodeEventsMap;
		this.nodeEventsMap.addEntryListener(new EntryListener<KEY, VALUE>() {

			@Override
			public void entryUpdated(EntryEvent<KEY, VALUE> event) {
				refreshIterators();
			}

			@Override
			public void entryRemoved(EntryEvent<KEY, VALUE> event) {
				refreshIterators();
			}

			@Override
			public void entryEvicted(EntryEvent<KEY, VALUE> event) {
				refreshIterators();
			}

			@Override
			public void entryAdded(EntryEvent<KEY, VALUE> event) {
				refreshIterators();
			}
		}, false);

	}

	public VALUE next(KEY eventName) {
		RoundRobinIterator<VALUE> iterator = iterators.get(eventName);
		if (iterator == null) {
			synchronized (iterators) {
				if (iterators.get(eventName) == null) {
					refreshIterators();
				}
			}
			iterator = iterators.get(eventName);
			if (iterator == null) {
				return null;
			}
		}
		return iterator.next();
	}

	public void refreshIterators() {
		synchronized (iterators) {
			logger.debug("refreshing iterators");
			iterators.clear();
			for (KEY key : nodeEventsMap.keySet()) {
				iterators.put(key, new RoundRobinIterator<VALUE>(nodeEventsMap.get(key)));
			}
		}
	}

}
