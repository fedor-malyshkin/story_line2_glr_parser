package ru.nlp_project.story_line2.glr_parser.keywords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.GLRParser;
import ru.nlp_project.story_line2.glr_parser.SentenceProcessorPool;
import ru.nlp_project.story_line2.glr_parser.TestFixtureBuilder;
import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.TokenManager;

public class KeywordsManagerTest {

	private static GLRParser glrParser;
	private static SentenceProcessorPool sentenceProcessorPool;
	private KeywordManager testable;

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUpClass() throws IOException {
		String parserConfigDir = TestFixtureBuilder
				.unzipToTempDir("ru/nlp_project/story_line2/glr_parser/KeywordsManagerTest.zip");
		glrParser = GLRParser.newInstance(parserConfigDir + "/glr-config.json", true);
		sentenceProcessorPool = glrParser.getSentenceProcessorPool();
	}

	@Before
	public void setUp() {
		testable = KeywordManager.newInstance(null);
	}

	@Test
	public void testCalculateOptimalKeywordsCoverage_Simpliest() {
		List<PlainKeywordEntrance> kwes = new ArrayList<PlainKeywordEntrance>();
		kwes.add(new PlainKeywordEntrance(0, 1, 0, 0, 0, null));

		List<? extends IKeywordEntrance> coverage =
				testable.calculateOptimalKeywordsCoverage(kwes, 1);
		Collections.reverse(coverage);
		assertEquals("[<0;1;0;0(0)>]", coverage.toString());
	}

	@Test
	public void testCalculateOptimalKeywordsCoverage_ComplexCombination() {
		List<PlainKeywordEntrance> kwes = new ArrayList<PlainKeywordEntrance>();
		kwes.add(new PlainKeywordEntrance(0, 3, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(2, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(3, 3, 0, 2, 0, null));
		kwes.add(new PlainKeywordEntrance(5, 2, 0, 3, 0, null));
		kwes.add(new PlainKeywordEntrance(7, 2, 0, 4, 0, null));
		kwes.add(new PlainKeywordEntrance(8, 1, 0, 5, 0, null));

		List<? extends IKeywordEntrance> coverage =
				testable.calculateOptimalKeywordsCoverage(kwes, 9);
		assertEquals("[<0;3;0;0(0)>, <3;3;0;2(0)>, <7;2;0;4(0)>]", coverage.toString());
	}

	@Test
	public void testCalculateOptimalKeywordsCoverage_ComplexCombination2() {
		List<PlainKeywordEntrance> kwes = new ArrayList<PlainKeywordEntrance>();
		kwes.add(new PlainKeywordEntrance(0, 3, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(2, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(3, 3, 0, 2, 0, null));
		kwes.add(new PlainKeywordEntrance(4, 3, 0, 3, 0, null));
		kwes.add(new PlainKeywordEntrance(7, 2, 0, 4, 0, null));
		kwes.add(new PlainKeywordEntrance(8, 1, 0, 5, 0, null));

		List<? extends IKeywordEntrance> coverage =
				testable.calculateOptimalKeywordsCoverage(kwes, 9);
		assertEquals("[<0;3;0;0(0)>, <4;3;0;3(0)>, <7;2;0;4(0)>]", coverage.toString());
	}

	/**
	 * Обследовать следующую странную комьинаци, приводящую к зависанию кода 0-4; 24-4; 0-2; 1-3;
	 * 1-1; 2-2; 3-1; 13-3; 14-2; 15-1; 24-3; 25-3; 24-2; 25-2; 26-2; 25-1; 25-1; 27-1; 27-1; 15-1;
	 * 3-1
	 * 
	 * tokenLength = 29
	 */
	@Test
	public void test_BUG_CalculateOptimalKeywordsCoverage_ComplexCombination3() {
		List<PlainKeywordEntrance> kwes = new ArrayList<PlainKeywordEntrance>();
		// 0-4; 24-4; 0-2;
		kwes.add(new PlainKeywordEntrance(0, 4, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(24, 4, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(0, 2, 0, 2, 0, null));
		// 1-3; 1-1; 2-2;
		kwes.add(new PlainKeywordEntrance(1, 3, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(1, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(2, 2, 0, 2, 0, null));
		// 3-1; 13-3; 14-2;
		kwes.add(new PlainKeywordEntrance(3, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(13, 3, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(14, 2, 0, 2, 0, null));
		// 15-1; 24-3; 25-3;
		kwes.add(new PlainKeywordEntrance(15, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(24, 3, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(25, 3, 0, 2, 0, null));
		// 24-2; 25-2; 26-2;
		kwes.add(new PlainKeywordEntrance(24, 2, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(25, 2, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(26, 2, 0, 2, 0, null));
		// 25-1; 25-1; 27-1;
		kwes.add(new PlainKeywordEntrance(25, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(25, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(27, 1, 0, 2, 0, null));
		// 27-1; 15-1; 3-1
		kwes.add(new PlainKeywordEntrance(27, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(15, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(3, 1, 0, 2, 0, null));

		List<? extends IKeywordEntrance> coverage =
				testable.calculateOptimalKeywordsCoverage(kwes, 29);
		assertEquals("[<0;4;0;0(0)>, <13;3;0;1(0)>, <24;4;0;1(0)>]", coverage.toString());
	}

	@Test
	public void testDetectKeywordEntrances() {
		List<Token> tokens = sentenceProcessorPool.generateTokens("Собака на сене лежала.", false);
		List<String> kws = Arrays.asList("собака", "собака на сене");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<0;1;0;0(0)>, <0;3;0;1(0)>]", coverage.toString());
	}

	/**
	 * Исключаем повторный анализ тех же самых слов, ранее обернытх в {@link PlainKeywordToken}
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testDetectKeywordEntrances_TwiceTheSame() throws IOException {
		List<Token> tokens = sentenceProcessorPool.generateTokens("Собака на сене лежала.", false);
		// replace first
		TokenManager tokenManager = TokenManager.newInstance(null, null, false);
		Token token = tokenManager.createDummyPlainKeywordToken("Собака");
		tokens.set(0, token);
		List<String> kws = Arrays.asList("собака", "на сене", "собака на сене");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<1;2;0;1(0)>]", coverage.toString());
	}

	@Test
	public void testDetectKeywordEntrances_OptionsMainWord() {
		List<Token> tokens = sentenceProcessorPool.generateTokens("Собака на сене лежала.", false);
		List<String> kws = Arrays.asList("собака", "собака на сене|-main_word=2");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> entrances =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<0;1;0;0(0)>, <0;3;0;1(2)>]", entrances.toString());
	}

	@Test
	public void testDetectKeywordEntrances_OptionsUpperCase() {
		List<Token> tokens = sentenceProcessorPool
				.generateTokens("Собака на сене лежала СОБАКА НА СЕНЕ.", false);
		List<String> kws = Arrays.asList("собака", "собака на сене|-upper_case -main_word=1");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<0;1;0;0(0)>, <4;1;0;0(0)>, <4;3;0;1(1)>]", coverage.toString());
	}

	@Test
	public void testDetectKeywordEntrances_ExclamationSymbol() {
		List<Token> tokens = sentenceProcessorPool
				.generateTokens("Собаки на сене лежали СОБАКА НА СЕНЕ лежала.", true);
		List<String> kws = Arrays.asList("собака", "!собаки на сене");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<0;1;0;0(0)>, <0;1;0;0(0)>, <0;3;0;1(0)>, <4;1;0;0(0)>, <4;1;0;0(0)>]",
				coverage.toString());
	}

	@Test
	public void testDetectKeywordEntrances_Gramm() {
		List<Token> tokens = sentenceProcessorPool
				.generateTokens("Собаки на сене лежали СОБАКА НА СЕНЕ лежала.", true);
		List<String> kws = Arrays.asList("собака|-gramm=\"noun, sing\"",
				"собака на сене|-gramm-1=\"prep, sing\"");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<0;1;0;0(0)>, <0;1;0;0(0)>, <4;1;0;0(0)>, <4;1;0;0(0)>]",
				coverage.toString());
	}

	@Test
	public void testDetectKeywordEntrances_Gramm_Stali() {
		List<Token> tokens = sentenceProcessorPool.generateTokens("солдаты стали.", true);
		List<String> kws = Arrays.asList("солдат стал|-main_word=1 -gramm-1=\"plur, verb\"");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> entrances =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<0;2;0;0(1)>]", entrances.toString());
		assertEquals("['стал' (verb [verb, past, plur, perf, intr, indc])]",
				entrances.get(0).mainWordToken.getLexemesListCopy().toString());
	}

	@Test
	public void testDetectKeywordEntrancesInTheMiddle() {
		List<Token> tokens =
				sentenceProcessorPool.generateTokens("Лежала Собака на сене долго.", false);
		List<String> kws = Arrays.asList("собака", "собака на сене");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<1;1;0;0(0)>, <1;3;0;1(0)>]", coverage.toString());
	}

	@Test
	public void testDetectKeyword_WithIgnoreCase_AndLemmChecking() {
		List<Token> tokens = sentenceProcessorPool
				.generateTokens("месяцев Саудовская Аравия и Россия провели ", true);
		List<String> kws = Arrays.asList("саудовский аравия|-main_word=1", "россия");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<1;2;0;0(1)>, <4;1;0;1(0)>, <4;1;0;1(0)>]", coverage.toString());
	}

	@Test
	public void testDetectKeywordEntrancesInTheTail() {
		List<Token> tokens = sentenceProcessorPool.generateTokens("Лежала Собака на.", false);
		List<String> kws = Arrays.asList("собака", "собака на сене");
		testable.addKeywordSet("simple-kwSet", kws, Collections.emptyMap(), "");
		List<PlainKeywordEntrance> coverage =
				testable.detectPlainKeywordEntrances("simple-kwSet", tokens);
		assertEquals("[<1;1;0;0(0)>]", coverage.toString());
	}

	@Test
	public void testParseLookupOptions() {
		LookupOptions parseOptions = testable.parseLookupOptions("-gramm =\" femn, noun\"");
		assertEquals("femn,noun", parseOptions.gramm.stream().map(g -> g.toString()).sorted()
				.collect(Collectors.joining(",")));

		parseOptions = testable
				.parseLookupOptions("-exact_form -gramm-1=  \"sing, noun\" -gramm-3=\"adj \"");
		assertTrue(parseOptions.exactForm);
		assertEquals("{1=[noun, sing], 3=[adj]}", parseOptions.grammTree.toString());
	}

	@Test
	public void testParseEntryString_NoLookupOptions() {
		List<String> strings = new LinkedList<String>();
		LookupOptions localOptions =
				testable.parseEntryString("немного текста с    лишними пробелами   ", strings);
		assertEquals("[немного, текста, с, лишними, пробелами]", strings.toString());
		assertNotNull(localOptions);
	}

	@Test
	public void testParseEntryString_LookupOptions() {
		List<String> strings = new LinkedList<String>();
		LookupOptions localOptions = testable
				.parseEntryString("немного текста | -gramm= \" noun\" -agr = gnc_agr", strings);
		assertEquals("[немного, текста]", strings.toString());
		assertNotNull(localOptions);
		assertEquals("[noun]", localOptions.gramm.toString());
		assertEquals("gnc_agr", localOptions.agr.toString());
	}



	@Test
	public void testParseEntryString_Lemm_LookupOptions() {
		List<String> strings = new LinkedList<String>();
		LookupOptions localOptions =
				testable.parseEntryString("российская федерация | -lemm= \" Россия\" ", strings);
		assertEquals("[российская, федерация]", strings.toString());
		assertNotNull(localOptions);
		assertEquals("Россия", localOptions.lemm);
	}

	@Test
	public void testSimpleCoverageFiltering() {
		List<IKeywordEntrance> kwes = new ArrayList<IKeywordEntrance>();
		// 0-4; 24-4; 0-2;
		kwes.add(new PlainKeywordEntrance(0, 4, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(24, 4, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(0, 2, 0, 2, 0, null));
		// 1-3; 1-1; 2-2;
		kwes.add(new PlainKeywordEntrance(1, 3, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(1, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(2, 2, 0, 2, 0, null));
		// 3-1; 13-3; 14-2;
		kwes.add(new PlainKeywordEntrance(3, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(13, 3, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(14, 2, 0, 2, 0, null));
		// 15-1; 24-3; 25-3;
		kwes.add(new PlainKeywordEntrance(15, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(24, 3, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(25, 3, 0, 2, 0, null));
		// 24-2; 25-2; 26-2;
		kwes.add(new PlainKeywordEntrance(24, 2, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(25, 2, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(26, 2, 0, 2, 0, null));
		// 25-1; 25-1; 27-1;
		kwes.add(new PlainKeywordEntrance(25, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(25, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(27, 1, 0, 2, 0, null));
		// 27-1; 15-1; 3-1
		kwes.add(new PlainKeywordEntrance(27, 1, 0, 0, 0, null));
		kwes.add(new PlainKeywordEntrance(15, 1, 0, 1, 0, null));
		kwes.add(new PlainKeywordEntrance(3, 1, 0, 2, 0, null));

		testable.simpleCoverageFiltering(kwes);
		assertEquals("[<0;4;0;0(0)>, <24;4;0;1(0)>, <13;3;0;1(0)>]", kwes.toString());
	}
}
