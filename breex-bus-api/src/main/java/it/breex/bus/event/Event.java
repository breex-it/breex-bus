package it.breex.bus.event;

public interface Event<I> {

	EventData<I> getEventData();

}
