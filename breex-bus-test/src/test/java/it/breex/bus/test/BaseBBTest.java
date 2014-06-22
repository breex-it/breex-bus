/*******************************************************************************
 * Copyright 2013 Francesco Cina'
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.breex.bus.test;

import it.breex.bus.test.config.SpringConfig;
import it.breex.bus.test.config.TestCaseConfig;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RunWith(Parameterized.class)
public abstract class BaseBBTest {

	private final static DecimalFormat TIME_FORMAT = new DecimalFormat("####,###.###", new DecimalFormatSymbols(Locale.US));

	public static ApplicationContext CONTEXT = null;

	private Date testStartDate;
	private final TestCaseConfig testCaseConfig;

	public BaseBBTest(final String testName, final TestCaseConfig testCaseConfig) {
		this.testCaseConfig = testCaseConfig;
	}

	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> generateData() {
		if (CONTEXT == null) {
			CONTEXT = new AnnotationConfigApplicationContext(SpringConfig.class); //$NON-NLS-1$
		}

		List<Object[]> parameters = new ArrayList<Object[]>();
		for (Entry<String, TestCaseConfig> tccEntry : CONTEXT.getBeansOfType(TestCaseConfig.class).entrySet()) {
			TestCaseConfig tcc = tccEntry.getValue();
			parameters.add(new Object[] { tcc.getName(), tcc }); //$NON-NLS-1$
		}
		return parameters;
	}

	@Rule
	public final TestName name = new TestName();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Before
	public void setUpBeforeTest() {

		testStartDate = new Date();

		getLogger().info("==================================================================="); //$NON-NLS-1$
		getLogger().info("BEGIN TEST " + name.getMethodName()); //$NON-NLS-1$
		getLogger().info("==================================================================="); //$NON-NLS-1$

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

	public TestCaseConfig getTestCaseConfig() {
		return testCaseConfig;
	}

}
