package it.breex.eventbus.impl.jms;

import it.breex.eventbus.impl.BreexBusImpl;

import javax.jms.ConnectionFactory;


public class JmsBreexBus extends BreexBusImpl {

	public JmsBreexBus(ConnectionFactory jmsConnectionFactory) {
		super(new JmsEventManager(jmsConnectionFactory));
	}

}
