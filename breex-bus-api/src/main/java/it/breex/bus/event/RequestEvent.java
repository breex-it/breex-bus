package it.breex.bus.event;

public interface RequestEvent<I, O> extends Event<I> {

	void reply(O response);

}
