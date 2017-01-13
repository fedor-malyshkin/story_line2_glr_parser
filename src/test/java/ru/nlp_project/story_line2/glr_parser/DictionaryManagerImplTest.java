package ru.nlp_project.story_line2.glr_parser;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager;

public class DictionaryManagerImplTest {

	@Test
	public void testReadDictionaryFile() throws IOException {
		DictionaryManagerImpl testable = new DictionaryManagerImpl();
		IKeywordManager keywordManager = mock(IKeywordManager.class);
		IGrammarManager grammarManager = mock(IGrammarManager.class);
		ConfigurationReader configurationReader = mock(ConfigurationReader.class);
		testable.configurationReader = configurationReader;
		testable.grammarManager = grammarManager;
		testable.keywordManager = keywordManager;

		InputStream grmIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"ru/nlp_project/story_line2/glr_parser/DictionaryManagerTest.testReadDictionaryFile.kws");
		when(configurationReader.getInputStream(eq("grammar_file.grm"))).thenReturn(grmIS);

		InputStream kwIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"ru/nlp_project/story_line2/glr_parser/DictionaryManagerTest.testReadDictionaryFile.kws");
		when(configurationReader.getInputStream(eq("keywords_file.kws"))).thenReturn(kwIS);

		InputStream stream1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"ru/nlp_project/story_line2/glr_parser/DictionaryManagerTest.testReadDictionaryFile.dict");

		testable.readDictionaryFile(stream1);
		verify(grammarManager).loadGrammar(eq("name0"), eq("grammar_file.grm"));

		assertEquals(3, testable.getDictionaryEntryMap().size());

	}

}
