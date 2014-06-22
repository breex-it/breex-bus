package it.breex.bus.impl.jms;

import it.breex.bus.impl.BreexBusImpl;

import javax.jms.ConnectionFactory;


public class JmsBreexBus extends BreexBusImpl {

	public JmsBreexBus(ConnectionFactory jmsConnectionFactory) {
		super(new JmsEventManager(jmsConnectionFactory));
	}

}
