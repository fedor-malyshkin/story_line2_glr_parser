package ru.nlp_project.story_line2.glr_parser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManager;

public class DictionaryManagerTest {

  @SuppressWarnings("deprecation")
  @Test
  public void testReadDictionaryFile() throws IOException {
    DictionaryManager testable = new DictionaryManager();
    KeywordManager keywordManager = mock(KeywordManager.class);
    GrammarManager grammarManager = mock(GrammarManager.class);
    ConfigurationReader configurationReader = mock(ConfigurationReader.class);
    testable.setConfigurationReader(configurationReader);
    grammarManager.setConfigurationReader(configurationReader);
    testable.setGrammarManager(grammarManager);
    testable.setKeywordManager(keywordManager);

    InputStream grmIS = Thread
        .currentThread()
        .getContextClassLoader()
        .getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/DictionaryManagerTest.testReadDictionaryFile.kws");
    when(configurationReader.getInputStream(eq("grammar_file.grm")))
        .thenReturn(grmIS);

    InputStream kwIS = Thread
        .currentThread()
        .getContextClassLoader()
        .getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/DictionaryManagerTest.testReadDictionaryFile.kws");
    when(configurationReader.getInputStream(eq("keywords_file.kws")))
        .thenReturn(kwIS);

    InputStream stream1 = Thread
        .currentThread()
        .getContextClassLoader()
        .getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/DictionaryManagerTest.testReadDictionaryFile.dict");

    testable.readDictionaryFile(stream1);
    verify(grammarManager).loadGrammar(eq("name0"),  eq("grammar_file.grm"));

    assertEquals(3, testable.getDictionaryEntryMap().size());

  }

}
