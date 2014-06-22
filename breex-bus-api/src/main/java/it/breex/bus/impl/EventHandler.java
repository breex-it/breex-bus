package it.breex.bus.impl;

public interface EventHandler<I,O> {

	O process(EventData<I> eventData);

}
