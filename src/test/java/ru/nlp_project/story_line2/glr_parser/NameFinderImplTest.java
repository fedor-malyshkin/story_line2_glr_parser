package ru.nlp_project.story_line2.glr_parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NameFinderImplTest {
	private NameFinderImpl testable;
	private ITokenManager tokenManager;
	private static GLRParser glrParser;

	@BeforeClass
	public static void setUpClass() throws IOException {
		String parserConfigDir = TestFixtureBuilder
				.unzipToTempDir("ru/nlp_project/story_line2/glr_parser/TokenManagerImplTest.zip");
		System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
				new File(parserConfigDir + "/glr-config.yaml").toURI().toString());
		glrParser = GLRParser.newInstance(true);
	}

	@Before
	public void setUp() {
		tokenManager = glrParser.tokenManager;
		testable = (NameFinderImpl) glrParser.nameFinder;
	}

	@Test
	public void testPrestprocessTokens() {
		List<Token> tokens = tokenManager.splitIntoTokens(
				"К нам пришел Салтыков-Щедрин и Иванов П.А., вместе с Пвловым А. А., а после пришёл Медведев Пв..",
				false);
		testable.preprocessTokens(tokens);

		assertEquals("Салтыков-Щедрин", tokens.get(3).getValue());
		assertTrue(tokens.get(3).kwHyphen);

		assertEquals("П.", tokens.get(6).getValue());
		assertTrue(tokens.get(6).kwInitial);

		assertEquals("А.", tokens.get(7).getValue());
		// 3 English
		assertEquals("А.", tokens.get(12).getValue());
		assertEquals("А.", tokens.get(13).getValue());

		assertEquals("Пв.", tokens.get(19).getValue());
		assertTrue(tokens.get(19).kwInitial);
	}

	@Test
	public void testPrestprocessTokensWithNumbers() {
		List<Token> tokens =
				tokenManager.splitIntoTokens("Она затянулась на 17-20 месяцев.", false);
		testable.preprocessTokens(tokens);

		assertEquals("17", tokens.get(3).getValue());

		assertEquals("-", tokens.get(4).getValue());
		assertTrue(tokens.get(4).kwHyphen);

		assertEquals("20", tokens.get(5).getValue());
	}

	@Test
	public void testPrestprocessTokens_2HyphenWords() {
		List<Token> tokens =
				tokenManager.splitIntoTokens("К нам пришел Коко-Дель-Рей и Иванов П.А..", false);
		testable.preprocessTokens(tokens);

		assertEquals("Коко-Дель-Рей", tokens.get(3).getValue());
		assertTrue(tokens.get(3).kwHyphen);
	}

	@Test
	public void testDetectFIOKeywordEntrances() {
		List<Token> tokens = tokenManager.splitIntoTokens(
				"К нам пришел Иван и Иванов П.А., вместе с Павловым А. А., а после пришёл Медведев Пв..",
				true);

		testable.preprocessTokens(tokens);
		List<NameFinderImpl.FIOEntry> fioKeywordEntrances =
				(List<NameFinderImpl.FIOEntry>) testable.detectFIOKeywordEntrances(tokens);

		assertEquals(4, fioKeywordEntrances.size());
		assertEquals("иван", fioKeywordEntrances.get(0).serialize());
		assertEquals("иванов п. а.", fioKeywordEntrances.get(1).serialize());
		assertEquals("павловым а. а.", fioKeywordEntrances.get(2).serialize());
		assertEquals("медведев пв.", fioKeywordEntrances.get(3).serialize());
	}

	/**
	 * Исключаем повторный анализ тех же самых слов, ранее обернытх в
	 * {@link TokenManagerImpl.FIOKeywordToken}.
	 * 
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testDetectFIOKeywordEntrances_TwiceTheSame() throws IOException {
		List<Token> tokens = tokenManager.splitIntoTokens(
				"К нам пришел Иван и Иванов П.А., вместе с Павловым А. А., а после пришёл Медведев Пв..",
				true);

		testable.preprocessTokens(tokens);
		// replace first
		TokenManagerImpl tokenManager = new TokenManagerImpl(false);
		tokenManager.initialize();
		Token token = tokenManager.createDummyFIOKeywordToken("иван");
		tokens.set(3, token);

		List<NameFinderImpl.FIOEntry> fioKeywordEntrances =
				(List<NameFinderImpl.FIOEntry>) testable.detectFIOKeywordEntrances(tokens);

		assertEquals(3, fioKeywordEntrances.size());
		assertEquals("иванов п. а.", fioKeywordEntrances.get(0).serialize());
		assertEquals("павловым а. а.", fioKeywordEntrances.get(1).serialize());
		assertEquals("медведев пв.", fioKeywordEntrances.get(2).serialize());
	}

	@Test
	@Ignore
	public void testDetectFIOKeywordEntrances_ForeignNamesSurnames() {
		List<Token> tokens = tokenManager
				.splitIntoTokens("На встрече присутствовали: Беньямин Натаньяху, Катрин Денев, "
						+ "Марчелло Мастрояни, Барак Обама, Пан Ги Мун, "
						+ "Алексис Ципрас, Джон Керри, Мария Елена Боски, Микелла Витория Брамбилла,"
						+ "Маттео Ренци и Сильвио Берлускони.", true);

		testable.preprocessTokens(tokens);
		Collection<NameFinderImpl.FIOEntry> fioKeywordEntrances =
				testable.detectFIOKeywordEntrances(tokens);

		assertEquals(4, fioKeywordEntrances.size());
	}

	@Test
	@Ignore
	public void testDetectFIOKeywordEntrances_UnknowSurnames() {
		// обратить внимание на неправильное написание "Доброневского"
		List<Token> tokens = tokenManager.splitIntoTokens(
				"Передайте привет Дарье Шиховой и Анне Герман, а так же Наталье Бон и Виктору Доброневского.",
				true);
		testable.preprocessTokens(tokens);
		List<NameFinderImpl.FIOEntry> fioKeywordEntrances =
				(List<NameFinderImpl.FIOEntry>) testable.detectFIOKeywordEntrances(tokens);

		assertEquals(15, fioKeywordEntrances.size());
		assertEquals("наталье", fioKeywordEntrances.get(11).serialize());
		assertEquals("виктору", fioKeywordEntrances.get(12).serialize());
		assertEquals("доброневского", fioKeywordEntrances.get(13).serialize());
		assertEquals("доброневского", fioKeywordEntrances.get(14).serialize());

		tokens = tokenManager.splitIntoTokens(
				"Передайте привет Дарье Шиховой и Анне Герман, а так же Наталье Бон и Виктору Доброневскому.",
				true);
		testable.preprocessTokens(tokens);
		fioKeywordEntrances =
				(List<NameFinderImpl.FIOEntry>) testable.detectFIOKeywordEntrances(tokens);

		assertEquals(13, fioKeywordEntrances.size());
		assertEquals("наталье", fioKeywordEntrances.get(10).serialize());
		assertEquals("наталье", fioKeywordEntrances.get(11).serialize());
		assertEquals("доброневскому виктору", fioKeywordEntrances.get(12).serialize());
	}
}
