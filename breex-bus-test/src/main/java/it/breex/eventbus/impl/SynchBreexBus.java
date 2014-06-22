package it.breex.eventbus.impl;


public class SynchBreexBus extends BreexBusImpl {

	public SynchBreexBus() {
		super(new SynchEventManager());
	}

}
