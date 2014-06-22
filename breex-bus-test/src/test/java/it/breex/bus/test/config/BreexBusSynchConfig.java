package it.breex.bus.test.config;

import it.breex.bus.BreexBus;
import it.breex.bus.impl.SynchBreexBus;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BreexBusSynchConfig {

	@Bean(name = "SynchBreexBus")
	public TestCaseConfig getTCC() {
		TestCaseConfig tcc = new TestCaseConfig();
		tcc.setName("SynchBreexBus");
		tcc.setBus(Arrays.asList(new BreexBus[] { new SynchBreexBus() }));
		return tcc;
	}
}
