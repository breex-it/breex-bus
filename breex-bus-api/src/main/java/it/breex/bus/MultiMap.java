package it.breex.bus;

import java.util.Collection;

public interface MultiMap<K, V> {

	Collection<V> get(K key);

	void put(K key, V value);

	boolean containsKey(K key);

}
