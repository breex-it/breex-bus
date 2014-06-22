package it.breex.bus.impl.hazelcast;

import it.breex.bus.impl.BreexBusImpl;

import java.util.concurrent.ExecutorService;

import com.hazelcast.core.HazelcastInstance;


public class HazelcastBreexBus extends BreexBusImpl {

	public HazelcastBreexBus(HazelcastInstance hazelcastInstance, ExecutorService executorService) {
		super(new HazelcastEventManager(hazelcastInstance, executorService));
	}

}
