package ru.nlp_project.story_line2.glr_parser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ConfigurationReaderTest {

  private ConfigurationReader testable;

  @Test
  public void testRead() throws IOException {
    InputStream resourceAsStream = Thread
        .currentThread()
        .getContextClassLoader()
        .getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/ConfigurationReaderTest.json");
    File tempFile = File.createTempFile("temp", ".json");
    FileUtils.copyInputStreamToFile(resourceAsStream, tempFile);

    testable = ConfigurationReader.newInstance(tempFile.getAbsolutePath());

    assertNotNull(testable.getConfigurationMain().articles);
    assertEquals("[art1, art2]",
        testable.getConfigurationMain().articles.toString());
    assertNotNull(testable.getConfigurationMain().morphZipDB);
    assertEquals(
        "classpath:ru/nlp_project/story_line2/morph/dict.story_line2.zip",
        testable.getConfigurationMain().morphZipDB);
    assertEquals("facts.fact", testable.getConfigurationMain().factFile);
  }
}
