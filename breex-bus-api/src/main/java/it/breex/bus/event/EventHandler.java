package it.breex.bus.event;

public interface EventHandler<T extends Event<?>> {

	void process(T event);

}
