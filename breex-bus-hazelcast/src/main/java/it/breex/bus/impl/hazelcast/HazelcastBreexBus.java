package it.breex.bus.impl.hazelcast;

import it.breex.bus.impl.BreexBusImpl;

import com.hazelcast.core.HazelcastInstance;


public class HazelcastBreexBus extends BreexBusImpl {

	public HazelcastBreexBus(HazelcastInstance hazelcastInstance) {
		super(new HazelcastEventManager(hazelcastInstance));
	}

}
