package it.breex.bus.impl;


public class SynchBreexBus extends BreexBusImpl {

	public SynchBreexBus() {
		super(new SynchEventManager());
	}

}
