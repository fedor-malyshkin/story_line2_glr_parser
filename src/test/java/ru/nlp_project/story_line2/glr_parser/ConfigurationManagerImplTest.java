package ru.nlp_project.story_line2.glr_parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
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
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"ru/nlp_project/story_line2/glr_parser/ConfigurationManagerImplTest_testReadMasterConfiguration.yaml");
		File tempFile = File.createTempFile("temp", ".yaml");
		FileUtils.copyInputStreamToFile(resourceAsStream, tempFile);
		System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
				tempFile.toURI().toString());

		testable = new ConfigurationManagerImpl();
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
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"ru/nlp_project/story_line2/glr_parser/ConfigurationManagerImplTest_testReadDictionaryConfiguration.yaml");
		File tempFile = File.createTempFile("temp", ".yaml");
		FileUtils.copyInputStreamToFile(resourceAsStream, tempFile);
		testable.parentFile = null;
		testable.getMasterConfiguration().dictionaryFile = tempFile.toString();
		DictionaryConfiguration dictionaryConfiguration = testable.getDictionaryConfiguration();

		assertNotNull(dictionaryConfiguration);

	}

	@Test
	public void testReadHierarchyConfiguration() throws IOException, ConfigurationException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"ru/nlp_project/story_line2/glr_parser/ConfigurationManagerImplTest_testReadHierarchyConfiguration.yaml");
		File tempFile = File.createTempFile("temp", ".yaml");
		FileUtils.copyInputStreamToFile(resourceAsStream, tempFile);
		testable.parentFile = null;
		testable.getMasterConfiguration().hierarchyFile = tempFile.toString();
		HierarchyConfiguration hierarchyConfiguration = testable.getHierarchyConfiguration();

		assertNotNull(hierarchyConfiguration);
		assertTrue(hierarchyConfiguration.hierarchies.containsKey("men"));
		assertEquals("[Mary Smith, Susan Williams]",
				hierarchyConfiguration.hierarchies.get("women").toString());
	}


	@Test
	public void testReadFactConfiguration() throws IOException, ConfigurationException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"ru/nlp_project/story_line2/glr_parser/ConfigurationManagerImplTest_testReadFactConfiguration.yaml");
		File tempFile = File.createTempFile("temp", ".yaml");
		FileUtils.copyInputStreamToFile(resourceAsStream, tempFile);
		testable.parentFile = null;
		testable.getMasterConfiguration().factFile = tempFile.toString();
		FactConfiguration factConfiguration = testable.getFactConfiguration();

		assertNotNull(factConfiguration);
	}
}
