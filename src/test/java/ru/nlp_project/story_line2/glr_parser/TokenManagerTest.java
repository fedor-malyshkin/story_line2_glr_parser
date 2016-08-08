package ru.nlp_project.story_line2.glr_parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.TokenManager.PlainKeywordToken;
import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManager;
import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordEntrance;

public class TokenManagerTest {
  private TokenManager testable;
  private KeywordManager keywordManagerMock;
  private static GLRParser glrParser;

  @BeforeClass
  public static void setUpClass() throws IOException {
    String parserConfigDir = TestFixtureBuilder.unzipToTempDir(
        "ru/nlp_project/story_line2/glr_parser/TokenManagerTest.zip");
    glrParser =
        GLRParser.newInstance(parserConfigDir + "/glr-config.json", false);
  }

  @SuppressWarnings("deprecation")
  @Before
  public void setUp() {
    testable = glrParser.getTokenManager();
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testModifyTokensByPlainKeywords() {
    keywordManagerMock = mock(KeywordManager.class);
    testable.setKeywordManager(keywordManagerMock);

    when(keywordManagerMock.getKeywordSetsNameByIndex(eq(12)))
        .thenReturn("keywordSetName");

    List<Token> tokens =
        testable.splitIntoTokens(
            "Немного текста с кавычками - 'Госстрах', "
                + "\"Энергогарант\", `МособлСтрой`, «Горгаз», «Два слова».",
            false);

    PlainKeywordEntrance entrance1 = new PlainKeywordEntrance(1, 3, 12, 12, 1,
        new Token(2, 1, "C", TokenTypes.WORD));
    PlainKeywordEntrance entrance2 = new PlainKeywordEntrance(22, 2, 12, 12, 0,
        new Token(4, 1, "E", TokenTypes.WORD));
    testable.modifyTokensByKeywords(tokens,
        Arrays.asList(entrance1, entrance2));
    assertEquals("[('Немного', 0,7, @WORD, #), "
        + "('текста_с_кавычками', 8,18, @COMBINED_PLAIN, #, 'keywordsetname'), "
        + "('-', 27,1, @DELIM, #), (''', 29,1, @DELIM, #), ('Госстрах', 30,8, @WORD, #), "
        + "(''', 38,1, @DELIM, #), (',', 39,1, @DELIM, #), "
        + "('\"', 41,1, @DELIM, #), ('Энергогарант', 42,12, @WORD, #), ('\"', 54,1, @DELIM, #), "
        + "(',', 55,1, @DELIM, #), "
        + "('`', 57,1, @DELIM, #), ('МособлСтрой', 58,11, @WORD, #), ('`', 69,1, @DELIM, #), "
        + "(',', 70,1, @DELIM, #), "
        + "('«', 72,1, @DELIM, #), ('Горгаз', 73,6, @WORD, #), ('»', 79,1, @DELIM, #), "
        + "(',', 80,1, @DELIM, #), "
        + "('«', 82,1, @DELIM, #), ('Два_слова', 83,9, @COMBINED_PLAIN, #, 'keywordsetname'), ('»', 92,1, @DELIM, #), "
        + "('.', 93,1, @DELIM, #)]", tokens.toString());

    PlainKeywordToken pkt = (PlainKeywordToken) tokens.get(1);
    assertEquals("keywordsetname", pkt.kwName);
    assertEquals(
        "[('текста', 8,6, @WORD, #), ('с', 15,1, @WORD, #), ('кавычками', 17,9, @WORD, #)]",
        pkt.originalTokens.toString());

    // quotting
    Token token = tokens.get(20);
    assertTrue(token.isQuoted());
    assertFalse(token.isLQuoted());
    assertFalse(token.isRQuoted());
  }

  @Test
  public void testDetectTokensVariant00() {
    List<Token> tokens = testable.splitIntoTokens("", false);
    assertTrue(tokens.size() == 0);
  }

  @Test
  public void testDetectTokensVariant01() {
    List<Token> tokens = testable.splitIntoTokens(
        "\"Немножко текста на русском языке\", сказал Кузбма Прутков!", false);
    String fact = serializeTokens(tokens);
    assertEquals(
        "0-1=\", 1-8=Немножко, 10-6=текста, 17-2=на, 20-7=русском, 28-5=языке, "
            + "33-1=\", 34-1=,, 36-6=сказал, 43-6=Кузбма, 50-7=Прутков, 57-1=!",
        fact);
  }

  @Test
  public void testDetectTokensVariant02() {
    List<Token> tokens =
        testable.splitIntoTokens("Обычный текст - с точкой.", false);
    String fact = serializeTokens(tokens);
    assertEquals("0-7=Обычный, 8-5=текст, 14-1=-, 16-1=с, 18-6=точкой, 24-1=.",
        fact);
  }

  private String serializeTokens(List<Token> tokens) {
    return tokens.stream().map((t) -> String.format("%d-%d=%s", t.getFrom(),
        t.getLength(), t.getValue())).collect(Collectors.joining(", "));
  }

  @Test
  public void checkQuotes() {
    List<Token> tokens =
        testable.splitIntoTokens(
            "Немного текста с кавычками - 'Госстрах', "
                + "\"Энергогарант\", `МособлСтрой`, «Горгаз», «Два слова».",
            false);
    assertEquals(26, tokens.size());
    // 6 Госстрах
    assertTrue(tokens.get(6).isQuoted());
    assertFalse(tokens.get(6).isLQuoted());
    assertFalse(tokens.get(6).isRQuoted());
    // 18 Горгаз
    assertTrue(tokens.get(18).isQuoted());
    assertFalse(tokens.get(18).isLQuoted());
    assertFalse(tokens.get(18).isRQuoted());
    // 22 «Два
    assertFalse(tokens.get(22).isQuoted());
    assertTrue(tokens.get(22).isLQuoted());
    assertFalse(tokens.get(22).isRQuoted());
    // 23 слова»
    assertFalse(tokens.get(23).isQuoted());
    assertFalse(tokens.get(23).isLQuoted());
    assertTrue(tokens.get(23).isRQuoted());

    assertEquals(
        "0-7=Немного, 8-6=текста, 15-1=с, 17-9=кавычками, 27-1=-, 29-1=', 30-8=Госстрах, "
            + "38-1=', 39-1=,, 41-1=\", 42-12=Энергогарант, 54-1=\", 55-1=,, 57-1=`, "
            + "58-11=МособлСтрой, 69-1=`, 70-1=,, 72-1=«, 73-6=Горгаз, 79-1=», 80-1=,, "
            + "82-1=«, 83-3=Два, 87-5=слова, 92-1=», 93-1=.",
        serializeTokens(tokens));
  }

  @Test
  public void checkRegistry() {
    List<Token> tokens =
        testable.splitIntoTokens(
            "Немного текста с кавычками - 'Госстрах', "
                + "\"Энергогарант\", `МособлСтрой`, «Горгаз», «Два слова».",
            false);

    // 1 текста
    assertTrue(tokens.get(1).isLReg());
    assertFalse(tokens.get(1).isHReg1());
    assertFalse(tokens.get(1).isHReg2());

    // 6 Госстрах
    assertFalse(tokens.get(5).isLReg());
    assertTrue(tokens.get(5).isHReg1());
    assertFalse(tokens.get(5).isHReg2());

    // 14 МособлСтрой
    assertFalse(tokens.get(14).isLReg());
    assertTrue(tokens.get(14).isHReg1());
    assertTrue(tokens.get(14).isHReg2());
  }

  @Test
  public void checkRegistryWithNumbers() {
    List<Token> tokens =
        testable.splitIntoTokens("Немного - 17 тарелок.", false);

    // 2 17
    assertFalse(tokens.get(2).isLReg());
    assertFalse(tokens.get(2).isHReg1());
    assertFalse(tokens.get(2).isHReg2());
  }

  @Test
  public void checkLat() {
    List<Token> tokens =
        testable.splitIntoTokens("Немного текста in English.", false);
    // 1 текста
    assertFalse(tokens.get(1).isLat());
    // 2 in
    assertTrue(tokens.get(2).isLat());
    // 3 English
    assertTrue(tokens.get(3).isLat());
  }

}
