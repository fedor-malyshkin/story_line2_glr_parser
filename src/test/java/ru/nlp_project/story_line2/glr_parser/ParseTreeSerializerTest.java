package ru.nlp_project.story_line2.glr_parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager;
import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordEntrance;

public class ParseTreeSerializerTest {
	@Rule
	public TestName testName = new TestName();

	private ParseTreeSerializer testable;

	private static String parserConfigDir;
	private static GLRParser glrParser;

	private static ITokenManager tokenManager;

	private static GrammarManagerImpl grammarManager;

	private static IKeywordManager keywordManager;

	@BeforeClass
	public static void setUpClass() throws IOException {
		parserConfigDir = TestFixtureBuilder.unzipToTempDir(
				"ru/nlp_project/story_line2/glr_parser/ParseTreeSerializerTest.zip");
		System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
				new File(parserConfigDir + "/glr-config.yaml").toURI().toString());
		glrParser = GLRParser.newInstance(true);
		tokenManager = glrParser.tokenManager;
		grammarManager = (GrammarManagerImpl) glrParser.grammarManager;
		keywordManager = glrParser.keywordManager;

	}

	@Before
	public void setUp() {
		testable = ParseTreeSerializer.newInstance(tokenManager);
	}

	@Test
	public void checkSimpleSerialization_NoAgreements() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("синих участников", true);
		String grammarText = "Root->S; S->Adj Noun<rt>;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("синих участники", actual);
	}

	@Test
	public void checkSimpleSerialization_NC() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("синих участников", true);
		String grammarText = "Root->S; S->Adj<nc-agr=[1]> Noun<rt, nc-agr=[1]>;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("синие участники", actual);
	}

	@Test
	public void checkSimpleSerialization_NC_NoNormalization() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("синих участников", true);
		String grammarText = "Root->S; S->Adj<nc-agr=[1]> Noun<rt, nc-agr=[1]>;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, false);
		assertEquals("синих участников", actual);
	}

	@Test
	public void checkSerialization_WithCombinedGrammToken_WithNoRT() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("сильному синему участнику", true);
		String grammarText = "Root->S; S->Adj X; X->Adj<nc-agr=[1]> Noun<rt, nc-agr=[1]>;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("сильный синему участнику", actual);
	}

	@Test
	public void checkSerialization_WithCombinedGrammToken() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("сильных синих участников", true);
		String grammarText =
				"Root->S; S->Adj<nc-agr=[1]> X<rt, nc-agr=[1]>; X->Adj<nc-agr=[1]> Noun<rt, nc-agr=[1]>;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("сильные синие участники", actual);
	}

	@Test
	public void checkSerialization_WithCombinedGrammToken_NoNormalization() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("сильных синих участников", true);
		String grammarText =
				"Root->S; S->Adj<nc-agr=[1]> X<rt, nc-agr=[1]>; X->Adj<nc-agr=[1]> Noun<rt, nc-agr=[1]>;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, false);
		assertEquals("сильных синих участников", actual);
	}

	@Test
	public void checkSerialization_UnknownWords() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("сильных зурбабурба участников", true);
		String grammarText = "Root->S; S->Adj word Noun;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("сильные зурбабурба участников", actual);
	}

	@Test
	public void checkCutRestriction_ForNonTerminal() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("синие сильные люди поехали", true);
		String grammarText = "Root->S; S->Adj2<cut> Noun<rt> verb; Adj2-> (adj)+;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("люди поехали", actual);
	}

	@Test
	public void checkCutRestriction_ForTerminal() throws Exception {
		List<Token> tokens = tokenManager.splitIntoTokens("синие сильные люди поехали", true);
		String grammarText = "Root->S; S->Adj2 Noun<rt> verb; Adj2-> (adj<cut>)+;";

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("люди поехали", actual);
	}


	@Test
	public void checkLemmParameter() throws Exception {
		List<Token> tokens =
				tokenManager.splitIntoTokens("сильные люди поехали из российской федерации", true);
		String grammarText = "Root->S; S->Adj Noun<rt> verb prep Noun;";

		List<PlainKeywordEntrance> keywordEntrances =
				keywordManager.detectPlainKeywordEntrances("kwSet_w_lemms", tokens);
		if (!keywordEntrances.isEmpty()) {
			// calculate optimal (if exist entrances)
			List<? extends IKeywordEntrance> optimalCoverage = keywordManager
					.calculateOptimalKeywordsCoverage(keywordEntrances, tokens.size());
			tokenManager.modifyTokensByKeywords(tokens, optimalCoverage);
		}

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("сильные люди поехали из России", actual);
	}


	@Test
	public void checkLemmParameter_UnknownLemmm() throws Exception {
		List<Token> tokens = tokenManager
				.splitIntoTokens("сильные люди поехали из системы терминального доступа", true);
		String grammarText = "Root->S; S->Adj Noun<rt> verb prep Noun;";

		List<PlainKeywordEntrance> keywordEntrances =
				keywordManager.detectPlainKeywordEntrances("kwSet_w_lemms", tokens);
		if (!keywordEntrances.isEmpty()) {
			// calculate optimal (if exist entrances)
			List<? extends IKeywordEntrance> optimalCoverage = keywordManager
					.calculateOptimalKeywordsCoverage(keywordEntrances, tokens.size());
			tokenManager.modifyTokensByKeywords(tokens, optimalCoverage);
		}

		ParseTreeNode userRoot =
				TestFixtureBuilder.createAndValidateParseTree(grammarText, tokens, grammarManager);
		String actual = testable.serialize(userRoot, true);
		assertEquals("сильные люди поехали из СТД", actual);
	}



}
