package ru.nlp_project.story_line2.glr_parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class HierarchyManagerTest {

  private HierarchyManagerImpl testable;

  @Before
  public void setUp() {
    testable = new HierarchyManagerImpl();
  }

  @Test
  public void testReadConfigurationFile() throws IOException {
    InputStream stream1 =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/HierarchyManagerTest.json");

    testable.readConfigurationFile(stream1);
    assertEquals(
        "{parent-2=[p1, p2, p3], parent1=[p1, p2, p3, z1, z2], parent_3=[u1, u2, u3]}",
        testable.getHierarchiesMap().toString());
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
    assertFalse(
        testable.isAnyParent(Arrays.asList("not-ex", "-not-ex-e1"), "sv1"));
  }

  @Test
  public void testMatch_ExistingValue() {
    testable.addConfigurationEntry("e1", Arrays.asList("v1", "v2", "v3"));
    testable.addConfigurationEntry("v2", Arrays.asList("sv1", "sv2"));

    assertTrue(testable.isParent("e1", "sv1"));
    assertTrue(testable.isAnyParent(Arrays.asList("not-ex", "e1"), "sv1"));
  }
}
