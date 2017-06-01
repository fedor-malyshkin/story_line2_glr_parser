package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.nlp_project.story_line2.morph.GrammemeEnum;


public class TokenTaggerImplTest {

	private static GLRParser glrParser;

	@BeforeClass
	public static void setUpClass() throws IOException {
		String parserConfigDir = TestFixtureBuilder
				.unzipToTempDir("ru/nlp_project/story_line2/glr_parser/TokenTaggerImplTest.zip");
		glrParser = GLRParser.newInstance("file://" + parserConfigDir + "/glr-config.yaml", true);
	}

	private TokenTaggerImpl testable;
	private TokenManagerImpl tokenManager;

	@Before
	public void setUp() {
		tokenManager = (TokenManagerImpl) glrParser.tokenManager;
		testable = (TokenTaggerImpl) glrParser.tokenTagger;
	}

	@Test
	public void testSimpleTagging() {
		List<Token> tokens = tokenManager.splitIntoTokens("Мы стали чемпионами.", true);
		Assertions.assertThat(tokens.get(1).kwWord).isTrue();
		// token 2 may be noun
		Assertions.assertThat(tokens.get(1).lexemes.stream()
				.anyMatch(l -> l.grammemes.getPOS() == GrammemeEnum.noun)).isTrue();
		// token 2 may be verb
		Assertions.assertThat(tokens.get(1).lexemes.stream()
				.anyMatch(l -> l.grammemes.getPOS() == GrammemeEnum.verb)).isTrue();
		testable.processTokens(tokens);
		// token 1 = npro
		Assertions.assertThat(tokens.get(0).lexemes.stream()
				.allMatch(l -> l.grammemes.getPOS() == GrammemeEnum.npro)).isTrue();
		// token 2 = verb
		Assertions.assertThat(tokens.get(1).lexemes.stream()
				.allMatch(l -> l.grammemes.getPOS() == GrammemeEnum.verb)).isTrue();
		// token 3 = noun
		Assertions.assertThat(tokens.get(2).lexemes.stream()
				.allMatch(l -> l.grammemes.getPOS() == GrammemeEnum.noun)).isTrue();

	}

	
	
	@Test
	public void testTaggingWithPunt() {
		List<Token> tokens = tokenManager.splitIntoTokens("\"Мы стали чемпионами\" - сказал он.", true);
		Assertions.assertThat(tokens.get(0).kwWord).isFalse();
		Assertions.assertThat(tokens.get(2).kwWord).isTrue();
		// token 'stali' may be noun
		Assertions.assertThat(tokens.get(2).lexemes.stream()
				.anyMatch(l -> l.grammemes.getPOS() == GrammemeEnum.noun)).isTrue();
		// token 'stali' may be verb
		Assertions.assertThat(tokens.get(2).lexemes.stream()
				.anyMatch(l -> l.grammemes.getPOS() == GrammemeEnum.verb)).isTrue();
		testable.processTokens(tokens);
		// token 'mi' = npro
		Assertions.assertThat(tokens.get(1).lexemes.stream()
				.allMatch(l -> l.grammemes.getPOS() == GrammemeEnum.npro)).isTrue();
		// token 'stali' = verb
		Assertions.assertThat(tokens.get(2).lexemes.stream()
				.allMatch(l -> l.grammemes.getPOS() == GrammemeEnum.verb)).isTrue();
		// token 'chempionami' = noun
		Assertions.assertThat(tokens.get(3).lexemes.stream()
				.allMatch(l -> l.grammemes.getPOS() == GrammemeEnum.noun)).isTrue();

	}

}
