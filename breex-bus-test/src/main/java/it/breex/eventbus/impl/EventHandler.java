package it.breex.eventbus.impl;

public interface EventHandler<I,O> {

	O process(EventData<I> eventData);

}
