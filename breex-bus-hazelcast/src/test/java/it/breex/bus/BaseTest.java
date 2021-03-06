package it.breex.bus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-context.xml" })
public abstract class BaseTest {

	private final static DecimalFormat TIME_FORMAT = new DecimalFormat("####,###.###", new DecimalFormatSymbols(Locale.US));

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Rule
	public TestName name = new TestName();
	@Autowired
	protected ApplicationContext context;

	private Date testStartDate;

	@Before
	public void setUpBeforeTest() {
		testStartDate = new Date();
		logger.info("===================================================================");
		logger.info("BEGIN TEST " + name.getMethodName());
		logger.info("===================================================================");

	}

	@After
	public void tearDownAfterTest() {
		long executionTime = new Date().getTime() - testStartDate.getTime();
		logger.info("===================================================================");
		logger.info("END TEST " + name.getMethodName());
		logger.info("execution time: " + TIME_FORMAT.format(executionTime) + " ms");
		logger.info("===================================================================");
	}

	public Logger getLogger() {
		return logger;
	}
}
