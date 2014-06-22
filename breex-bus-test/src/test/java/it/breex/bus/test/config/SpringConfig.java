/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.breex.bus.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 *
 * @author ufo
 */
@Configuration
//@ComponentScan(basePackages = "it.breex.bus")
@PropertySource("classpath:spring-context.properties")
@Import({
	BreexBusAsynchConfig.class,
	BreexBusHazelcastConfig.class,
	BreexBusJMSConfig.class,
	BreexBusSynchConfig.class
})
public class SpringConfig {

	//This is needed to enable the @Value annotation to resolve properly their values
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
