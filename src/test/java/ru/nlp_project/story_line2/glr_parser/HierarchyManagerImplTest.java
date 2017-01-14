package ru.nlp_project.story_line2.glr_parser;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HierarchyManagerImplTest {

	private HierarchyManagerImpl testable;

	@Before
	public void setUp() {
		testable = new HierarchyManagerImpl();
	}

	@Test(expected = IllegalStateException.class)
	public void testAddRecursionEntry() {
		testable.addConfigurationEntry("e1", Arrays.asList("v1", "v2", "v3"));
		testable.addConfigurationEntry("v2", Arrays.asList("sv1", "sv2"));
		testable.addConfigurationEntry("sv2", Arrays.asList("e1", "zv1"));
	}

	@Test(expected = IllegalStateException.class)
	public void testAddRecursionEntry_Var1() {
		testable.addConfigurationEntry("e1", Arrays.asList("v1", "v2", "v3"));
		testable.addConfigurationEntry("v2", Arrays.asList("v1", "v2"));
	}

	@Test
	public void testMatch_TheSameValue() {
		testable.addConfigurationEntry("e1", Arrays.asList("v1", "v2", "v3"));
		testable.addConfigurationEntry("v2", Arrays.asList("sv1", "sv2"));

		assertTrue(testable.isParent("e1", "e1"));
		assertTrue(testable.isAnyParent(Arrays.asList("not-ex", "e1"), "e1"));

	}

	@Test
	public void testMatch_WrongValue() {
		testable.addConfigurationEntry("e1", Arrays.asList("v1", "v2", "v3"));
		testable.addConfigurationEntry("v2", Arrays.asList("sv1", "sv2"));

		assertFalse(testable.isParent("e1", "sv1-NE"));
		assertFalse(testable.isParent("e1-NE", "sv1"));
		assertFalse(testable.isAnyParent(Arrays.asList("not-ex", "-not-ex-e1"), "sv1"));
	}

	@Test
	public void testMatch_ExistingValue() {
		testable.addConfigurationEntry("e1", Arrays.asList("v1", "v2", "v3"));
		testable.addConfigurationEntry("v2", Arrays.asList("sv1", "sv2"));

		assertTrue(testable.isParent("e1", "sv1"));
		assertTrue(testable.isAnyParent(Arrays.asList("not-ex", "e1"), "sv1"));
	}
}
