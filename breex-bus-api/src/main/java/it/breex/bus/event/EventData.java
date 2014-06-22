package it.breex.bus.event;

import java.io.Serializable;

public class EventData<I> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;
	private final String senderId;
	private final String name;
	private final I message;
	private Object transportData;

	public EventData(String id, String senderId, String name, I message) {
		this.id = id;
		this.senderId = senderId;
		this.name = name;
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getName() {
		return name;
	}

	public I getMessage() {
		return message;
	}

	public Object getTransportData() {
		return transportData;
	}

	public void setTransportData(Object transportData) {
		this.transportData = transportData;
	}

}
