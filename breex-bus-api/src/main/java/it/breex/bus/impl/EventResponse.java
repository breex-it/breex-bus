package it.breex.bus.impl;

public interface EventResponse<O> {

	void receive(O response);

}
