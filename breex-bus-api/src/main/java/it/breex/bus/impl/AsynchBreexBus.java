package it.breex.bus.impl;

import java.util.concurrent.ExecutorService;

public class AsynchBreexBus extends BreexBusImpl {

	public AsynchBreexBus(ExecutorService executorService) {
		super(new AsynchEventManager(executorService));
	}

}
