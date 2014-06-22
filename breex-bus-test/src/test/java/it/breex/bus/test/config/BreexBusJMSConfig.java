package it.breex.bus.test.config;

import it.breex.bus.BreexBus;
import it.breex.bus.impl.jms.JmsBreexBus;

import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.connection.CachingConnectionFactory;

@Configuration
@ImportResource("classpath:jms-broker-config.xml")
public class BreexBusJMSConfig {

	@Bean(name = "JMSBreexBus")
	@DependsOn("jmsBroker")
	public TestCaseConfig getTCC() {

		List<BreexBus> buses = new ArrayList<BreexBus>();

		for (int i = 0; i < 4; i++) {
			CachingConnectionFactory ccf = new CachingConnectionFactory();
			ccf.setTargetConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:31313"));
			ccf.setCacheProducers(false);
			ccf.setSessionCacheSize(100);

			buses.add(new JmsBreexBus(ccf));
		}

		TestCaseConfig tcc = new TestCaseConfig();
		tcc.setName("JMSBreexBus");
		tcc.setBus(buses);

		return tcc;
	}
}
