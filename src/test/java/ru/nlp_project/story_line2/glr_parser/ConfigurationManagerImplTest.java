package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.config.ConfigurationException;
import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.DictionaryConfiguration;
import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.FactConfiguration;
import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.HierarchyConfiguration;

public class ConfigurationManagerImplTest {

	private ConfigurationManagerImpl testable;

	@Before
	public void setUp() throws IOException {
		testable = new ConfigurationManagerImpl("classpath://"
				+ "ru/nlp_project/story_line2/glr_parser/ConfigurationManagerImplTest_testReadMasterConfiguration.yaml");
		testable.initialize();

	}

	@Test
	public void testReadMasterConfiguration() throws IOException {
		assertNotNull(testable.getMasterConfiguration().articles);
		assertEquals("[art1, art2]", testable.getMasterConfiguration().articles.toString());
		assertNotNull(testable.getMasterConfiguration().morphZipDB);
		assertEquals("classpath:ru/nlp_project/story_line2/morph/dict.story_line2.zip",
				testable.getMasterConfiguration().morphZipDB);
		assertEquals("facts.fact", testable.getMasterConfiguration().factFile);
	}

	@Test
	public void testReadDictionaryConfiguration() throws IOException, ConfigurationException {
		testable.getMasterConfiguration().dictionaryFile =
				"ConfigurationManagerImplTest_testReadDictionaryConfiguration.yaml";
		DictionaryConfiguration dictionaryConfiguration = testable.getDictionaryConfiguration();

		assertNotNull(dictionaryConfiguration);

	}

	@Test
	public void testReadHierarchyConfiguration() throws IOException, ConfigurationException {
		testable.getMasterConfiguration().hierarchyFile =
				"ConfigurationManagerImplTest_testReadHierarchyConfiguration.yaml";
		HierarchyConfiguration hierarchyConfiguration = testable.getHierarchyConfiguration();

		assertNotNull(hierarchyConfiguration);
		assertTrue(hierarchyConfiguration.hierarchies.containsKey("men"));
		assertEquals("[Mary Smith, Susan Williams]",
				hierarchyConfiguration.hierarchies.get("women").toString());
	}


	@Test
	public void testReadFactConfiguration() throws IOException, ConfigurationException {
		testable.getMasterConfiguration().factFile =
				"ConfigurationManagerImplTest_testReadFactConfiguration.yaml";
		FactConfiguration factConfiguration = testable.getFactConfiguration();

		assertNotNull(factConfiguration);
	}
}
