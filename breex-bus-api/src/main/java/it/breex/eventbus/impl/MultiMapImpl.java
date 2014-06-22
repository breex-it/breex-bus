package it.breex.eventbus.impl;

import it.breex.eventbus.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiMapImpl<K, V> implements MultiMap<K, V> {

	private final Map<K, Collection<V>> map = new ConcurrentHashMap<>();

	@Override
	public Collection<V> get(K key) {
		return map.get(key);
	}

	@Override
	public void put(K key, V value) {
		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<V>());
		}
		map.get(key).add(value);
	}

	@Override
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

}
