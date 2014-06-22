package it.breex.bus.event;

public abstract class AbstractRequestEvent<I,O> extends AbstractEvent<I> implements RequestEvent<I,O> {

	public AbstractRequestEvent(EventData<I> eventData) {
		super(eventData);
	}

}
