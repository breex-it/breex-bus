package it.breex.bus.test.config;

import it.breex.bus.BreexBus;
import it.breex.bus.impl.AsynchBreexBus;

import java.util.Arrays;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BreexBusAsynchConfig {

	@Bean(name = "AsynchBreexBus_10_Threads")
	public TestCaseConfig getTCC() {
		TestCaseConfig tcc = new TestCaseConfig();
		tcc.setName("AsynchBreexBus_10_Threads");
		tcc.setBus(Arrays.asList(new BreexBus[] { new AsynchBreexBus(Executors.newFixedThreadPool(10)) }));
		return tcc;
	}
}
