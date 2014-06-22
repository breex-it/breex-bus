package it.breex.bus.event;

public abstract class AbstractResponseEvent<I,O> extends AbstractEvent<O> implements ResponseEvent<O> {

	public AbstractResponseEvent(EventData<O> eventData) {
		super(eventData);
	}

}
