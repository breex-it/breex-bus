package it.breex.bus.event;

public abstract class AbstractEvent<I> implements Event<I> {

	private final EventData<I> eventData;

	AbstractEvent(EventData<I> eventData) {
		this.eventData = eventData;
	}

	@Override
	public EventData<I> getEventData() {
		return eventData;
	}


}
