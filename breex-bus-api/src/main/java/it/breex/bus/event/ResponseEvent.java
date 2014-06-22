package it.breex.bus.event;

public interface EventResponse<O> {

	void receive(O response);

}
