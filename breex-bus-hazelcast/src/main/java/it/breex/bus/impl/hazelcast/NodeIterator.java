package it.breex.bus.impl.hazelcast;

import it.breex.bus.util.RoundRobinIterator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MultiMap;

public class NodeIterator {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<String, RoundRobinIterator<String>> iterators = new ConcurrentHashMap<>();
	private final MultiMap<String, String> nodeEventsMap;

	public NodeIterator(MultiMap<String, String> nodeEventsMap) {
		this.nodeEventsMap = nodeEventsMap;

	}

	public synchronized String next(String eventName) {
		RoundRobinIterator<String> iterator = iterators.get(eventName);
		if (iterator == null) {
			synchronized (iterators) {
				RoundRobinIterator<String> iteratorSynch = iterators.get(eventName);
				if (iteratorSynch == null) {
					refreshIterators();
				}
			}
			iterator = iterators.get(eventName);
		}
		return iterator.next();
	}

	public synchronized void refreshIterators() {
		logger.debug("refreshing iterators");
		iterators.clear();
		for (String key : nodeEventsMap.keySet()) {
			iterators.put(key, new RoundRobinIterator<String>(nodeEventsMap.get(key)));
		}
	}

}
