package it.breex.bus.event;

public interface EventHandler<I,O> {

	O process(EventData<I> eventData);

}
