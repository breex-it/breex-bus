package it.breex.eventbus.impl;

public interface EventResponse<O> {

	void receive(O response);

}
