package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.ParseTreeValidator.ParseTreeValidationException;

public class ParseTreeValidatorTest {

  private static String parserConfigDir;
  private static GLRParser glrParser;
  private static TokenManager tokenManager;
  private static GrammarManager grammarManager;

  @SuppressWarnings("deprecation")
  @BeforeClass
  public static void setUpClass() throws IOException {
    parserConfigDir = TestFixtureBuilder.unzipToTempDir(
        "ru/nlp_project/story_line2/glr_parser/ParseTreeValidatorTest.zip");
    //ConfigurationReader configurationReader = ConfigurationReader.newInstance(parserConfigDir + "/glr-config.json");

    glrParser =
        GLRParser.newInstance(parserConfigDir + "/glr-config.json", true);
    tokenManager = glrParser.getTokenManager();
    grammarManager = glrParser.getGrammarManager();
  }

  private ParseTreeValidator testable;

  @Before
  public void setUp() throws IOException {
    testable = new ParseTreeValidator(HierarchyManager.newInstance(null));
  }

  /**
   * 
   * @throws Exception
   */
  @Test
  public void testAgreementSuccess_nc() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("розовый лиса", true);

    String grammarText = "Root->S; S->Adj<nc-agr=[1]> Noun<nc-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * 
   * @throws Exception
   */
  @Test(expected = ParseTreeValidationException.class)
  public void testAgreementFailure_nc() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("розовый кошки", true);

    String grammarText = "Root->S; S->Adj<nc-agr=[1]> Noun<nc-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAgreementSuccess_c() throws Exception {
    List<Token> tokens =
        tokenManager.splitIntoTokens("деревом топором бараном", true);

    String grammarText =
        "Root->S; S->Noun<c-agr=[1]> Noun<c-agr=[1] > Noun<c-agr=[1] >;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * @throws Exception
   */
  @Test(expected = ParseTreeValidationException.class)
  public void testAgreementFailure_c() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("дерево топором", true);

    String grammarText = "Root->S; S->Noun<c-agr=[1]> Noun<c-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * Упешная проверка согласования c-agr (несколько групп).
   * 
   * @throws Exception
   */
  @Test
  public void testAgreementSuccess_MixAgreementGroups() throws Exception {
    List<Token> tokens =
        tokenManager.splitIntoTokens("дерево топор деревом топором", true);

    String grammarText =
        "Root->S; S->Noun<c-agr=[1]> Noun<c-agr=[1]> Noun<c-agr=[2]> Noun<c-agr=[2]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * 
   * @throws Exception
   */
  @Test
  public void testAgreementSuccess_gn() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("розовая женщину", true);

    String grammarText = "Root->S; S->Adj<gn-agr=[1]> Noun<gn-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * @throws Exception
   */
  @Test(expected = ParseTreeValidationException.class)
  public void testAgreementFailure_gn() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("розовая женщин", true);

    String grammarText = "Root->S; S->Adj<gn-agr=[1]> Noun<gn-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * ['розовый' (adj {adjf,gent,plur,qual}), 'розовый' (adj {accs,adjf,anim,plur,qual}), 'розовый' (adj {adjf,loct,plur,qual})]
   * == ['розовый' (adj [{0=adj, 1=gent, 5=plur},] {adjf,gent,plur,qual}), 'розовый' (adj [{0=adj, 1=accs, 4=anim, 5=plur},] {accs,adjf,anim,plur,qual}), 'розовый' (adj [{0=adj, 1=loct, 5=plur},] {adjf,loct,plur,qual})]
   * ['женщина' (noun {accs,anim,femn,noun,sing})]
   * == ['женщина' (noun [{0=noun, 1=accs, 3=femn, 4=anim, 5=sing},] {accs,anim,femn,noun,sing})]
   * 
   * @throws Exception
   */
  @Test
  public void testAgreementSuccess_gc() throws Exception {
    // var 1 muliple numbers
    List<Token> tokens = tokenManager.splitIntoTokens("розовых женщин", true);

    String grammarText = "Root->S; S->Adj<gc-agr=[1]> Noun<gc-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
    // var 2 single
    tokens = tokenManager.splitIntoTokens("розовую женщину", true);
    userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);

  }

  /**
   * @throws Exception
   */
  @Test(expected = ParseTreeValidationException.class)
  public void testAgreementFailure_gc() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("розовую мужчину", true);

    String grammarText = "Root->S; S->Adj<gc-agr=[1]> Noun<gc-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * Упешная проверка согласования по роду, числу и падежу: gender number case.
   * 
   * @throws Exception
   */
  @Test
  public void testAgreementSuccess_gnc() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("синяя шляпа", true);

    String grammarText = "Root->S; S->Adj<gnc-agr=[1]> Noun<gnc-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

  /**
   * НЕУпешная проверка согласования по роду, числу и падежу: gender number case.
   * 
   * @throws Exception
   */
  @Test(expected = ParseTreeValidationException.class)
  public void testAgreementFailure_gnc() throws Exception {
    List<Token> tokens = tokenManager.splitIntoTokens("синий шляпа", true);

    String grammarText = "Root->S; S->Adj<gnc-agr=[1]> Noun<gnc-agr=[1]>;";
    ParseTreeNode userRootSymbolNode =
        TestFixtureBuilder.createParseTree(grammarText, tokens, grammarManager);

    testable.validateTree(
        TestFixtureBuilder.createDummySentenceProcessingContext(),
        userRootSymbolNode);
  }

}
