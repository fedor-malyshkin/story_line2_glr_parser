package ru.nlp_project.story_line2.glr_parser.eval;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.glr_parser.TestFixtureBuilder;
import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;

public class RNGLRAnalyserTest {

  @AfterClass
  public static void afterClass() throws IOException {
    // FileUtils.deleteDirectory(new File(parserConfigDir));
  }

  @Before
  public void setUp() {
    grammar = new Grammar();
  }

  private Grammar grammar;
  private RNGLRAnalyser testable;
  @Rule
  public TestName testName = new TestName();

  @Test
  public void testProcessSimpleString() {
    String text = "ab";
    List<Token> glrTokens = TestFixtureBuilder.createOneLetterTokens(text);

    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ab"));

    grammar.prepareGrammar();

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();

    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new OneLetterTokenMatcher(), lr1ParseTableBuilder, baseNTToNTExtData);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals("<'S', 0, 0>->[[<'a', 0, 1>, <'b', 1, 1>](1)]",
        testable.getRootNode().toString());
  }

  @Test
  public void testProcessSimpleString2() {
    String text = "ab";
    List<Token> glrTokens = TestFixtureBuilder.createOneLetterTokens(text);

    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aB"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));

    grammar.prepareGrammar();

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();

    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new OneLetterTokenMatcher(), lr1ParseTableBuilder, baseNTToNTExtData);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals(
        "<'S', 0, 0>->[[<'a', 0, 1>, <'B', 1, 0>->[[<'b', 1, 1>](2)]](1)]",
        testable.getRootNode().toString());
  }
  
  
  /**
   * Проверка не модификации списка токенов (не добавления служебного токена
   * "new Token(0, 0, null, TokenTypes.EOI)", используемого анализатором).
   */
  @Test
  public void testProcessSimpleString_NotModifyTokensList() {
    String text = "ab";
    List<Token> glrTokens = TestFixtureBuilder.createOneLetterTokens(text);

    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aB"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));

    grammar.prepareGrammar();

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();

    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new OneLetterTokenMatcher(), lr1ParseTableBuilder, baseNTToNTExtData);
    int sizeBefore = glrTokens.size();
    assertTrue(testable.processTokens(glrTokens));
    Token eoiToken = new Token(0, 0, null, TokenTypes.EOI);
    assertFalse(glrTokens.contains(eoiToken));
    assertEquals(sizeBefore, glrTokens.size());
  }

  @Test
  public void testProcessSimpleStringWithAltEpsilonRules() {
    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aEB"));
    grammar.add(TestFixtureBuilder.createProjection("E", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("E", "e"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));

    grammar.prepareGrammar();

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();

    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new OneLetterTokenMatcher(), lr1ParseTableBuilder, baseNTToNTExtData);

    String text = "ab";
    List<Token> glrTokens = TestFixtureBuilder.createOneLetterTokens(text);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals(
        "<'S', 0, 0>->[[<'a', 0, 1>, <''[E]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <'B', 1, 0>->[[<'b', 1, 1>](4)]](1)]",
        testable.getRootNode().toString());

    text = "aeb";
    glrTokens = TestFixtureBuilder.createOneLetterTokens(text);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals(
        "<'S', 0, 0>->[[<'a', 0, 1>, <'E', 1, 0>->[[<'e', 1, 1>](3)], <'B', 2, 0>->[[<'b', 2, 1>](4)]](1)]",
        testable.getRootNode().toString());

    text = "a";
    glrTokens = TestFixtureBuilder.createOneLetterTokens(text);
    assertFalse(testable.processTokens(glrTokens));

    text = "azb";
    glrTokens = TestFixtureBuilder.createOneLetterTokens(text);
    assertFalse(testable.processTokens(glrTokens));

  }

  @Test
  public void testProcessSimpleStringWithEpsilonRules() {
    String text = "ab";
    List<Token> glrTokens = TestFixtureBuilder.createOneLetterTokens(text);

    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aEB"));
    grammar.add(TestFixtureBuilder.createProjection("E", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));

    grammar.prepareGrammar();

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();

    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new OneLetterTokenMatcher(), lr1ParseTableBuilder, baseNTToNTExtData);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals(
        "<'S', 0, 0>->[[<'a', 0, 1>, <''[E]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <'B', 1, 0>->[[<'b', 1, 1>](3)]](1)]",
        testable.getRootNode().toString());
  }

  @Test
  public void testProcessSimpleStringWithHierarchicalEpsilonRules() {
    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aEB"));
    grammar.add(TestFixtureBuilder.createProjection("E", "FG"));
    grammar.add(TestFixtureBuilder.createProjection("F", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("G", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("G", "g"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));

    grammar.prepareGrammar();

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();

    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new OneLetterTokenMatcher(), lr1ParseTableBuilder, baseNTToNTExtData);

    String text = "ab";
    List<Token> glrTokens = TestFixtureBuilder.createOneLetterTokens(text);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals(
        "<'S', 0, 0>->[[<'a', 0, 1>, <''[F, G]'', 0, 0>->[[<''[F]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <''[G]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)]](-1)], <'B', 1, 0>->[[<'b', 1, 1>](6)]](1)]",
        testable.getRootNode().toString());

    text = "agb";
    glrTokens = TestFixtureBuilder.createOneLetterTokens(text);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals(
        "<'S', 0, 0>->["
            + "[<'a', 0, 1>, <'E', 1, 0>->[[<''[F]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <'G', 1, 0>->[[<'g', 1, 1>](5)]](2)], <'B', 2, 0>->[[<'b', 2, 1>](6)]](1)]",
        testable.getRootNode().toString());

    text = "af";
    glrTokens = TestFixtureBuilder.createOneLetterTokens(text);
    assertFalse(testable.processTokens(glrTokens));

  }

  @Test
  public void testProcessTokens() {
    String text = "bb";
    List<Token> glrTokens = TestFixtureBuilder.createOneLetterTokens(text);

    grammar.add(TestFixtureBuilder.createProjection("S", "bBSS"));
    grammar.add(TestFixtureBuilder.createProjection("S", "a"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("B", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("T", "S"));

    grammar.prepareGrammar();

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();
    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new OneLetterTokenMatcher(), lr1ParseTableBuilder, baseNTToNTExtData);
    assertTrue(testable.processTokens(glrTokens));
    assertEquals(
        "<'S', 0, 0>->["
            + "[<'b', 0, 1>, <''[B]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <'S', 1, 0>->[[<'b', 1, 1>, <''[B, S, S]'', 0, 0>->[[<''[B]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <''[S]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <''[S]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)]](-1)]](0)], <''[S]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)]](0), [<'b', 0, 1>, <''[B]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <''[S]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <'S', 1, 0>->[[<'b', 1, 1>, <''[B, S, S]'', 0, 0>->[[<''[B]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <''[S]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)], <''[S]'', 0, 0>->[[<'EPSILON', 0, 0>](-1)]](-1)]](0)]](0)]",
        testable.getRootNode().toString());

  }

  @Test
  public void testProcessGrammar_WithSymbolExt2() throws IOException {

    String grammarText = "R->S;" + "S->Ar<h-reg1>;"
        + "Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]>;";
    Grammar grammar2 = TestFixtureBuilder.parseGrammar(grammarText, "R");
    grammar2.getProjections().forEach((p) -> grammar.add(p));
    grammar.prepareGrammar("R");

    String text = "ver nou";
    List<Token> glrTokens =
        TestFixtureBuilder.createWhitespaceSeparatedTokens(text);

    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<Map<Symbol, List<Symbol>>> baseNTToNTExtData =
        lr1ParseTableBuilder.getBaseNTToNTExtData();

    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();

    testable = new RNGLRAnalyser(grammar, actionTable, states,
        new StartingLetterWithExtDataTokenMatcher(), lr1ParseTableBuilder,
        baseNTToNTExtData);
    assertTrue(testable.processTokens(glrTokens));

    assertEquals(
        "<'S', 0, 0>->[[<'Ar', 0, 0>->[[<'ver', 0, 1>, <'nou', 1, 1>](2)]](1)]",
        testable.getRootNode().toString());

  }

}
