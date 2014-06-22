package it.breex.bus.test.config;

import it.breex.bus.BreexBus;

import java.util.List;

public class TestCaseConfig {

	private String name;
	private List<BreexBus> bus;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return a list of distributed and connected {@link BreexBus} objects. The
	 *         buses must be able to intercommunicate. In case of not
	 *         distributed bus implementation, the list must contain only one
	 *         entry.
	 */
	public List<BreexBus> getBuses() {
		return bus;
	}

	public void setBus(List<BreexBus> bus) {
		this.bus = bus;
	}


}
