package it.breex.bus.util;

import java.util.Collection;
import java.util.Iterator;

public class RoundRobinIterator<E> implements Iterator<E> {

	private Iterator<E> it;
	private final Collection<E> list;

	public RoundRobinIterator(Collection<E> list) {
		this.list = list;
		it = list.iterator();
	}

	@Override
	public boolean hasNext() {
		return !list.isEmpty();
	}

	@Override
	public E next() {
		if (!it.hasNext()) {
			it = list.iterator();
		}
		return it.next();
	}

	@Override
	public void remove() {
		it.remove();
	}

}
