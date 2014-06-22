package it.breex.bus.test.config;

import it.breex.bus.BreexBus;
import it.breex.bus.impl.hazelcast.HazelcastBreexBus;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;

@Configuration
public class BreexBusHazelcastConfig {

	@Bean(name = "HazelcastBreexBus")
	public TestCaseConfig getTCC() {

		List<BreexBus> buses = new ArrayList<BreexBus>();

		for (int i = 0; i < 4; i++) {
			Config cfg = new Config();
			NetworkConfig network = cfg.getNetworkConfig();
			network.setPort(5701);
			network.setPortAutoIncrement(true);

			JoinConfig join = network.getJoin();
			join.getMulticastConfig().setEnabled(false);
			join.getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
			buses.add(new HazelcastBreexBus(Hazelcast.newHazelcastInstance(cfg)));
		}

		TestCaseConfig tcc = new TestCaseConfig();
		tcc.setName("HazelcastBreexBus");
		tcc.setBus(buses);

		return tcc;
	}
}
