package ru.nlp_project.story_line2.glr_parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.GrammarManager.GrammarDirectiveTypes;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Grammar;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class GrammarManagerTest {

  private static ConfigurationReader configurationReader;
  private static String parserConfigDir;
  private static GLRParser glrParser;
  private static TokenManager tokenManager;
  private static HierarchyManager hierarchyManager;

  @SuppressWarnings("deprecation")
  @BeforeClass
  public static void setUpClass() throws IOException {
    parserConfigDir = TestFixtureBuilder.unzipToTempDir(
        "ru/nlp_project/story_line2/glr_parser/GrammarManagerTest.zip");
    configurationReader =
        ConfigurationReader.newInstance(parserConfigDir + "/glr-config.json");

    glrParser =
        GLRParser.newInstance(parserConfigDir + "/glr-config.json", true);
    tokenManager = glrParser.getTokenManager();
    hierarchyManager = glrParser.getHierarchyManager();
  }

  private GrammarManager testable;

  @Before
  public void setUp() {
    testable = GrammarManager.newInstance(configurationReader, null, null, null,
        hierarchyManager);
  }

  private List<ParseTreeNode> extractParseTreeNodes(List<Token> tokens,
      Grammar grammar, Collection<ParseTreeNode> trees) {
    List<ParseTreeNode> result = new ArrayList<ParseTreeNode>();
    for (ParseTreeNode tree : trees) {
      testable.calculateParseTreeNodeCoverages(tokens, tree);
      // extract user root node
      ParseTreeNode userRootSymbolNode = testable
          .extractGrammarUserRootSymbolNode(tree, grammar.getUserRootSymbol());
      result.add(userRootSymbolNode);
    }
    return result;

  }

  @Test
  public void testAnalyseUsedKewordSets() throws IOException {
    testable.loadGrammar("1testArticle", "1main_grammar_file.grm");
    testable.loadGrammar("2testArticle", "2main_grammar_file.grm");

    testable.analyseUsedKewordSets();
    assertEquals("[]",
        testable.getUsedGrammarKeywords("1testArticle").toString());

    List<String> list =
        new ArrayList<String>(testable.getUsedPlainKeywords("1testArticle"));
    Collections.sort(list);
    assertEquals("[1animals, 1cats, 1животные]", list.toString());

    assertEquals("[1testArticle]",
        testable.getUsedGrammarKeywords("2testArticle").toString());
    assertEquals("[2animals]",
        testable.getUsedPlainKeywords("2testArticle").toString());
  }

  @Test
  public void testCreateParseTrees_2LevelWithFork() {
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("R", SymbolTypes.NonTerminal), 0, 1);

    SPPFNode c11 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c12 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-2", SymbolTypes.NonTerminal), 0, 1);
    List<SPPFNode> singletonList = Arrays.asList(c11, c12);
    root.addChildren(singletonList, -1);

    SPPFNode c21 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c22 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-2", SymbolTypes.NonTerminal), 0, 1);
    singletonList = Arrays.asList(c21, c22);
    c12.addChildren(singletonList, -1);

    SPPFNode cn21 = rnglrAnalyser
        .createSPPFNode(new Symbol("cn2-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode cn22 = rnglrAnalyser
        .createSPPFNode(new Symbol("cn2-2", SymbolTypes.NonTerminal), 0, 1);
    singletonList = Arrays.asList(cn21, cn22);
    c12.addChildren(singletonList, -1);
    Collection<ParseTreeNode> parseTrees =
        testable.createParseTrees(root, null);
    assertEquals(
        "[<R(), 0,0 (0,1)>->[<c1-1(), 0,0 (0,1)>, <c1-2(), 0,0 (0,1)>->[<c2-1(), 0,0 (0,1)>, <c2-2(), 0,0 (0,1)>]], "
            + "<R(), 0,0 (0,1)>->[<c1-1(), 0,0 (0,1)>, <c1-2(), 0,0 (0,1)>->[<cn2-1(), 0,0 (0,1)>, <cn2-2(), 0,0 (0,1)>]]]",
        parseTrees.toString());
  }

  @Test
  public void testCreateParseTrees_MoreLevels() {
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("R", SymbolTypes.NonTerminal), 0, 1);

    SPPFNode c11 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c12 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-2", SymbolTypes.NonTerminal), 0, 1);
    List<SPPFNode> singletonList = Arrays.asList(c11, c12);
    root.addChildren(singletonList, -1);
    SPPFNode c21 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c22 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-2", SymbolTypes.NonTerminal), 0, 1);
    singletonList = Arrays.asList(c21, c22);
    c12.addChildren(singletonList, -1);
    Collection<ParseTreeNode> parseTrees =
        testable.createParseTrees(root, null);
    assertEquals(
        "[<R(), 0,0 (0,1)>->[<c1-1(), 0,0 (0,1)>, <c1-2(), 0,0 (0,1)>->[<c2-1(), 0,0 (0,1)>, <c2-2(), 0,0 (0,1)>]]]",
        parseTrees.toString());
  }

  @Test
  public void testCreateParseTrees_SimpleTree() {
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("X", SymbolTypes.NonTerminal), 0, 1);
    Collection<ParseTreeNode> parseTrees =
        testable.createParseTrees(root, null);
    assertEquals("[<X(), 0,0 (0,1)>]", parseTrees.toString());
  }

  @Test
  public void testLoadGrammar() throws IOException {
    testable.loadGrammar("1testArticle", "1main_grammar_file.grm");
    testable.loadGrammar("1testArticle", "1main_grammar_file.grm");
    assertEquals(
        "Root->adj advb<kwtype=\"1animals\", gnc-agr=[1]>;\n"
            + "Root1->adj<rt, kwtype=\"1животные\"> advb;\n"
            + "Root2->adj advb;\n" + "R_0->T_1;\n" + "T_2->EPSILON;\n"
            + "T_1->Root T_2;\n" + "T_1->anyWord T_1;\n" + "T_2->anyWord T_2;",
        testable.getGrammar("1testArticle").toString());
    TreeMap<GrammarDirectiveTypes, Object> dirs =
        new TreeMap<GrammarDirectiveTypes, Object>(
            testable.getGrammarDirectives("1testArticle"));
    assertEquals(
        "{INCLUDE=[1some_add_file1.grm, ./1some_add_file2.grm], KWSET=[1cats], ROOT=Root}",
        dirs.toString());
  }

  @Test
  public void testCalculateParseTreeNodeCoverages() {
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("R", SymbolTypes.NonTerminal), 0, 1);
    Token t1 = new Token(1, 10, "t1-1", TokenTypes.WORD);
    SPPFNode c11 = rnglrAnalyser.createSPPFNode(t1, 0, 1);
    SPPFNode c12 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-2", SymbolTypes.NonTerminal), 0, 1);
    List<SPPFNode> singletonList = Arrays.asList(c11, c12);
    root.addChildren(singletonList, -1);
    Token t2 = new Token(11, 10, "t2-1", TokenTypes.WORD);
    SPPFNode c21 = rnglrAnalyser.createSPPFNode(t2, 0, 1);
    Token t3 = new Token(21, 20, "t2-2", TokenTypes.WORD);
    SPPFNode c22 = rnglrAnalyser.createSPPFNode(t3, 0, 1);
    singletonList = Arrays.asList(c21, c22);
    c12.addChildren(singletonList, -1);
    Collection<ParseTreeNode> parseTrees =
        testable.createParseTrees(root, null);
    ParseTreeNode parseTreeNode = parseTrees.iterator().next();
    testable.calculateParseTreeNodeCoverages(Arrays.asList(t1, t2, t3),
        parseTreeNode);
    assertEquals(
        "[<R(), 0,3 (1,40)>->[<null('t1-1'), 0,1 (1,10)>, <c1-2(), 1,2 (11,30)>->[<null('t2-1'), 1,1 (11,10)>, <null('t2-2'), 2,1 (21,20)>]]]",
        parseTrees.toString());
  }

  @Test
  public void testExtractGrammarUserRootSymbolNode() {
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("R", SymbolTypes.NonTerminal), 0, 1);

    SPPFNode c11 = rnglrAnalyser
        .createSPPFNode(new Token(1, 10, "t1-1", TokenTypes.WORD), 0, 1);
    SPPFNode c12 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-2", SymbolTypes.NonTerminal), 0, 1);
    List<SPPFNode> singletonList = Arrays.asList(c11, c12);
    root.addChildren(singletonList, -1);
    SPPFNode c21 = rnglrAnalyser
        .createSPPFNode(new Token(11, 10, "t2-1", TokenTypes.WORD), 0, 1);
    SPPFNode c22 = rnglrAnalyser
        .createSPPFNode(new Token(21, 20, "t2-2", TokenTypes.WORD), 0, 1);
    singletonList = Arrays.asList(c21, c22);
    c12.addChildren(singletonList, -1);
    Collection<ParseTreeNode> parseTrees =
        testable.createParseTrees(root, null);
    ParseTreeNode parseTreeNode = parseTrees.iterator().next();
    ParseTreeNode symbolNode = testable.extractGrammarUserRootSymbolNode(
        parseTreeNode, new Symbol("c1-2", SymbolTypes.NonTerminal));
    assertEquals(
        "<c1-2(), 0,0 (0,1)>->[<null('t2-1'), 0,0 (0,1)>, <null('t2-2'), 0,0 (0,1)>]",
        symbolNode.toString());

    symbolNode = testable.extractGrammarUserRootSymbolNode(parseTreeNode,
        new Symbol("S", SymbolTypes.NonTerminal));
    assertNull(symbolNode);
  }

  @Test
  public void testCreateTreeWithTokenClones() {
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("R", SymbolTypes.NonTerminal), 0, 1);

    SPPFNode c11 = rnglrAnalyser
        .createSPPFNode(new Token(1, 10, "t1-1", TokenTypes.WORD), 0, 1);
    SPPFNode c12 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-2", SymbolTypes.NonTerminal), 0, 1);
    List<SPPFNode> singletonList = Arrays.asList(c11, c12);
    root.addChildren(singletonList, -1);
    SPPFNode c21 = rnglrAnalyser
        .createSPPFNode(new Token(11, 10, "t2-1", TokenTypes.WORD), 0, 1);
    SPPFNode c22 = rnglrAnalyser
        .createSPPFNode(new Token(21, 20, "t2-2", TokenTypes.WORD), 0, 1);
    singletonList = Arrays.asList(c21, c22);
    c12.addChildren(singletonList, -1);
    List<ParseTreeNode> parseTrees = testable.createParseTrees(root, null);
    ParseTreeNode parseTreeNode = parseTrees.get(0);
    Token token = parseTreeNode.children.get(0).token;

    assertNotEquals(token, c11.getToken());

  }

  /**
   * Проверка простого анализа.
   */
  @Test
  public void testCreateParseTrees_SimpleText() {
    List<Token> tokens = tokenManager.splitIntoTokens("синий лось", true);

    String grammarText = "Root->S; S->Adj Noun; S->anyWord anyWord;";
    Grammar grammar = TestFixtureBuilder.parseGrammar(grammarText, "Root");
    RNGLRAnalyser analyser = testable.createAnalyser(grammar);
    assertTrue(analyser.processTokens(tokens));
    SPPFNode sppfNode = analyser.getRootNode();

    Collection<ParseTreeNode> trees =
        testable.createParseTrees(sppfNode, grammar);
    assertEquals(2, trees.size());
    assertEquals(
        "<T_1(), 0,0 (0,0)>->[<Root(), 0,0 (0,0)>->[<S(), 0,0 (0,0)>->[<adj('синий'), 0,0 (0,5)>, <noun('лось'), 0,0 (6,4)>]], <T_2(), 0,0 (0,0)>->[<EPSILON(), 0,0 (0,0)>]]\n"
            + "<T_1(), 0,0 (0,0)>->[<Root(), 0,0 (0,0)>->[<S(), 0,0 (0,0)>->[<anyWord('синий'), 0,0 (0,5)>, <anyWord('лось'), 0,0 (6,4)>]], <T_2(), 0,0 (0,0)>->[<EPSILON(), 0,0 (0,0)>]]",
        trees.stream().map(ParseTreeNode::toString).sorted()
            .collect(Collectors.joining("\n")));

  }

  /**
   * Проверка простого анализа. С учетом применения ограничений помет на нетерминалах.
   */
  @Test
  public void testCreateParseTrees_SimpleText_WithExtData_OnNonterminals() {
    List<Token> tokens = tokenManager.splitIntoTokens("синий лось", true);

    String grammarText =
        "Root->(T|P); S1->Adj Noun;  T->S1<h-reg1>; P->S1<h-reg2>;";
    Grammar grammar = TestFixtureBuilder.parseGrammar(grammarText, "Root");
    RNGLRAnalyser analyser = testable.createAnalyser(grammar);
    assertTrue(analyser.processTokens(tokens));
    SPPFNode sppfNode = analyser.getRootNode();

    Collection<ParseTreeNode> trees =
        testable.createParseTrees(sppfNode, grammar);
    assertEquals(2, trees.size());
    assertEquals(
        "[<T_1(), 0,0 (0,0)>->[<Root(), 0,0 (0,0)>->[<T(), 0,0 (0,0)>->[<S1<h-reg1>(), 0,0 (0,0)>->[<adj('синий'), 0,0 (0,5)>, <noun('лось'), 0,0 (6,4)>]]], <T_2(), 0,0 (0,0)>->[<EPSILON(), 0,0 (0,0)>]], "
            + "<T_1(), 0,0 (0,0)>->[<Root(), 0,0 (0,0)>->[<P(), 0,0 (0,0)>->[<S1<h-reg2>(), 0,0 (0,0)>->[<adj('синий'), 0,0 (0,5)>, <noun('лось'), 0,0 (6,4)>]]], <T_2(), 0,0 (0,0)>->[<EPSILON(), 0,0 (0,0)>]]]",
        trees.toString());

    trees = extractParseTreeNodes(tokens, grammar, trees);
    assertEquals(2, trees.size());
    assertEquals(
        "[<Root(), 0,2 (0,10)>->[<T(), 0,2 (0,10)>->[<S1<h-reg1>(), 0,2 (0,10)>->[<adj('синий'), 0,1 (0,5)>, <noun('лось'), 1,1 (6,4)>]]], "
            + "<Root(), 0,2 (0,10)>->[<P(), 0,2 (0,10)>->[<S1<h-reg2>(), 0,2 (0,10)>->[<adj('синий'), 0,1 (0,5)>, <noun('лось'), 1,1 (6,4)>]]]]",
        trees.toString());

  }

  /**
   * Проверка простого анализа.
   */
  @Test
  public void testCreateParseTrees_UnknownPOS() {
    List<Token> tokens = tokenManager.splitIntoTokens("синий кз.", true);

    String grammarText = "Root->S; S->Adj unknownPOS; ";
    Grammar grammar = TestFixtureBuilder.parseGrammar(grammarText, "Root");
    RNGLRAnalyser analyser = testable.createAnalyser(grammar);
    assertTrue(analyser.processTokens(tokens));
    SPPFNode sppfNode = analyser.getRootNode();

    Collection<ParseTreeNode> trees =
        testable.createParseTrees(sppfNode, grammar);
    assertEquals(1, trees.size());
    assertEquals(
        "<T_1(), 0,0 (0,0)>->[<Root(), 0,0 (0,0)>->[<S(), 0,0 (0,0)>->"
            + "[<adj('синий'), 0,0 (0,5)>, <unknownPOS('кз'), 0,0 (6,2)>]], "
            + "<T_2(), 0,0 (2,0)>->[<anyWord('.'), 0,0 (8,1)>, "
            + "<T_2(), 0,0 (0,0)>->[<EPSILON(), 0,0 (0,0)>]]]",
        trees.stream().map(ParseTreeNode::toString).sorted()
            .collect(Collectors.joining("\n")));
  }

  /**
   * Проверка простого анализа с literalSymbol'ами.
   */
  @Test
  public void testCreateParseTrees_LiteralSymbol() {
    List<Token> tokens =
        tokenManager.splitIntoTokens("синим покрывалом ветер покрывал", true);

    String grammarText = "Root->S; S->Adj 'покрывало'; ";
    Grammar grammar = TestFixtureBuilder.parseGrammar(grammarText, "Root");
    RNGLRAnalyser analyser = testable.createAnalyser(grammar);
    assertTrue(analyser.processTokens(tokens));
    SPPFNode sppfNode = analyser.getRootNode();

    Collection<ParseTreeNode> trees =
        testable.createParseTrees(sppfNode, grammar);
    assertEquals(1, trees.size());
    assertEquals(
        "<T_1(), 0,0 (0,0)>->[<Root(), 0,0 (0,0)>->[<S(), 0,0 (0,0)>->"
            + "[<adj('синим'), 0,0 (0,5)>, <'покрывало'('покрывалом'), 0,0 (6,10)>]], "
            + "<T_2(), 0,0 (2,0)>->[<anyWord('ветер'), 0,0 (17,5)>, "
            + "<T_2(), 0,0 (3,0)>->[<anyWord('покрывал'), 0,0 (23,8)>, "
            + "<T_2(), 0,0 (0,0)>->[<EPSILON(), 0,0 (0,0)>]]]]",
        trees.stream().map(ParseTreeNode::toString).sorted()
            .collect(Collectors.joining("\n")));
  }

  /**
   * Проверка простого анализа с с некоторыми ключевыми словами.
   * keywords:
  Word
  UnknownPOS
  SimConjAnd
  QuoteDbl
  QuoteSng
  LBracket
  RBracket
  Hyphen
  Punct
  CommaColon
  Percent
  Dollar
  PlusSign
   * 
   */
  @Test
  public void testCreateParseTrees_ForSomeKeywords() {
    List<Token> tokens = tokenManager.splitIntoTokens(
        "синим сильно кз \" ' (<[{ ]}>) - . ,  :  % $ +", true);

    String grammarText = "Root->S; S->anyWord  (word)+  unknownPOS  "
        + "quoteDbl  quoteSng  (lBracket)*  (rBracket )* hyphen  punct  comma "
        + "colon  percent  dollar  plusSign  ;";
    Grammar grammar = TestFixtureBuilder.parseGrammar(grammarText, "Root");
    RNGLRAnalyser analyser = testable.createAnalyser(grammar);
    assertTrue(analyser.processTokens(tokens));
    SPPFNode sppfNode = analyser.getRootNode();

    Collection<ParseTreeNode> trees =
        testable.createParseTrees(sppfNode, grammar);
    assertEquals(1, trees.size());
    assertEquals("<T_4(), 0,0 (0,0)>->[<Root(), 0,0 (0,0)>->[<S(), 0,0 (0,0)>->"
        + "[<anyWord('синим'), 0,0 (0,5)>, " + "<word('сильно'), 0,0 (6,6)>, "
        + "<T_0(), 0,0 (2,0)>->[<unknownPOS('кз'), 0,0 (13,2)>, <quoteDbl('\"'), 0,0 (16,1)>, <quoteSng('''), 0,0 (18,1)>, "
        + "<T_1(), 0,0 (5,0)>->[<lBracket('('), 0,0 (20,1)>, <T_1(), 0,0 (6,0)>->[<lBracket('<'), 0,0 (21,1)>, <T_1(), 0,0 (7,0)>->[<lBracket('['), 0,0 (22,1)>, <T_1(), 0,0 (8,0)>->[<lBracket('{'), 0,0 (23,1)>, "
        + "<T_1(), 0,0 (9,0)>->[<T_2(), 0,0 (9,0)>->[<rBracket(']'), 0,0 (25,1)>, <T_2(), 0,0 (10,0)>->[<rBracket('}'), 0,0 (26,1)>, <T_2(), 0,0 (11,0)>->[<rBracket('>'), 0,0 (27,1)>, <T_2(), 0,0 (12,0)>->[<rBracket(')'), 0,0 (28,1)>, "
        + "<T_2(), 0,0 (13,0)>->[<hyphen('-'), 0,0 (30,1)>, <punct('.'), 0,0 (32,1)>, <comma(','), 0,0 (34,1)>, "
        + "<colon(':'), 0,0 (37,1)>, <percent('%'), 0,0 (40,1)>, "
        + "<dollar('$'), 0,0 (42,1)>, <plusSign('+'), 0,0 (44,1)>]]]]]]]]]]]]], "
        + "<T_5(), 0,0 (0,0)>->[<EPSILON(), 0,0 (0,0)>]]",
        trees.stream().map(ParseTreeNode::toString).sorted()
            .collect(Collectors.joining("\n")));
  }

}
