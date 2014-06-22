package it.breex.bus.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.breex.bus.BaseTest;
import it.breex.bus.util.RoundRobinIterator;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class RoundRobinIteratorTest extends BaseTest {

	@Test
	public void testRoundRobinEmptyList() {
		Collection<String> names = new ArrayList<String>();
		RoundRobinIterator<String> rri = new RoundRobinIterator<>(names);
		assertFalse(rri.hasNext());
	}

	@Test
	public void testRoundRobinList() {
		Collection<String> names = new ArrayList<String>();
		names.add("Francesco");
		names.add("Thomas");
		names.add("James");
		RoundRobinIterator<String> rri = new RoundRobinIterator<>(names);

		assertTrue(rri.hasNext());
		assertEquals("Francesco", rri.next());
		assertTrue(rri.hasNext());
		assertEquals("Thomas", rri.next());
		assertTrue(rri.hasNext());
		assertEquals("James", rri.next());
		assertTrue(rri.hasNext());
		assertEquals("Francesco", rri.next());
		assertEquals("Thomas", rri.next());
		assertEquals("James", rri.next());
		assertEquals("Francesco", rri.next());
		assertEquals("Thomas", rri.next());
		assertEquals("James", rri.next());
	}

}
