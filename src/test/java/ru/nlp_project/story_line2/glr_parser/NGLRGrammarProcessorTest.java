package ru.nlp_project.story_line2.glr_parser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.eval.Grammar;

public class NGLRGrammarProcessorTest {

  private NGLRGrammarProcessor testable;

  @Before
  public void setUp() {
    testable = new NGLRGrammarProcessor();
  }

  @Test
  public void testSimpleGrammar() {
    String grTest = "S -> Noun Adj;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->noun adj;, " + "R_0->T_1;, " + "T_2->EPSILON;, " + "T_1->S T_2;, "
            + "T_1->anyWord T_1;, " + "T_2->anyWord T_2;]",
        grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammar_WithLiteralStringInUpperCase() {
    String grTest = "S -> Noun 'Пирогов';";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->noun 'пирогов';, R_0->T_1;, T_2->EPSILON;, T_1->S T_2;, T_1->anyWord T_1;, T_2->anyWord T_2;]",
        grammar.getProjections().toString());
  }

  /**
   * Тестирование грамматики с ключевыми словами в неверном регистре.
   */
  @Test
  public void testSimpleGrammarWithIncorrectKeywordsCase() {
    String grTest = "S -> Noun Adj anyWorD word aDJ;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->noun adj anyWord word adj;, R_0->T_1;, T_2->EPSILON;, "
            + "T_1->S T_2;, T_1->anyWord T_1;, T_2->anyWord T_2;]",
        grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_PlusQuantifier() {
    String grTest = "#ROOT_SYMBOL S;" + "S->Noun; L -> Noun+ Adj;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->noun;, " + "T_0->adj;, " + "L->noun T_0;, " + "T_0->noun T_0;, "
            + "R_1->T_2;, " + "T_3->EPSILON;, " + "T_2->S T_3;, "
            + "T_2->anyWord T_2;, " + "T_3->anyWord T_3;]",
        grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_PlusQuantifierOnRootProjection() {
    String grTest = "S -> Noun+ Adj;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals("[T_0->adj;, " + "S->noun T_0;, " + "T_0->noun T_0;, "
        + "R_1->T_2;, " + "T_3->EPSILON;, " + "T_2->S T_3;, "
        + "T_2->anyWord T_2;, " + "T_3->anyWord T_3;]",
        grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_QuestionQuantifier() {
    String grTest = "#ROOT_SYMBOL S;" + "S->Noun; L -> Noun? Adj;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals("[S->noun;, " + "L->adj;, " + "L->noun adj;, " + "R_0->T_1;, "
        + "T_2->EPSILON;, " + "T_1->S T_2;, " + "T_1->anyWord T_1;, "
        + "T_2->anyWord T_2;]", grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_QuestionQuantifierOnRootProjection() {
    String grTest = "S -> Noun? Adj;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->adj;, " + "S->noun adj;, " + "R_0->T_1;, " + "T_2->EPSILON;, "
            + "T_1->S T_2;, " + "T_1->anyWord T_1;, " + "T_2->anyWord T_2;]",
        grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_QuestionQuantifierWithEpsilon() {
    String grTest = "#ROOT_SYMBOL S;" + "S->Noun; L -> Noun?;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals("[S->noun;, " + "L->EPSILON;, " + "L->noun;, " + "R_0->T_1;, "
        + "T_2->EPSILON;, " + "T_1->S T_2;, " + "T_1->anyWord T_1;, "
        + "T_2->anyWord T_2;]", grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_KleeneStarQuantifier() {
    String grTest = "S -> Noun* Adj;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals("[T_0->adj;, " + "S->T_0;, " + "T_0->noun T_0;, "
        + "R_1->T_2;, " + "T_3->EPSILON;, " + "T_2->S T_3;, "
        + "T_2->anyWord T_2;, " + "T_3->anyWord T_3;]",
        grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_GroupRE() {
    String grTest = "#ROOT_SYMBOL S;" + " S -> (Noun|Adj) advb;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals("[S->noun advb;, " + "S->adj advb;, " + "R_0->T_1;, "
        + "T_2->EPSILON;, " + "T_1->S T_2;, " + "T_1->anyWord T_1;, "
        + "T_2->anyWord T_2;]", grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_GroupRE_PlusQuantifier() {
    String grTest = "#ROOT_SYMBOL S;" + "S->Adj; L -> (Noun|Adj)+ advb;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals("[S->adj;, " + "T_0->advb;, " + "L->noun T_0;, "
        + "L->adj T_0;, " + "T_0->noun T_0;, " + "T_0->adj T_0;, "
        + "R_1->T_2;, " + "T_3->EPSILON;, " + "T_2->S T_3;, "
        + "T_2->anyWord T_2;, " + "T_3->anyWord T_3;]",
        grammar.getProjections().toString());
  }

  @Test
  public void testSimpleGrammarWithRE_GroupRE_KleeneStarQuantifier() {
    String grTest = "#ROOT_SYMBOL S;" + "S -> Adj; L->(Noun|Adj)* advb;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->adj;, " + "T_0->advb;, " + "L->T_0;, " + "T_0->noun T_0;, "
            + "T_0->adj T_0;, " + "R_1->T_2;, " + "T_3->EPSILON;, "
            + "T_2->S T_3;, " + "T_2->anyWord T_2;, " + "T_3->anyWord T_3;]",
        grammar.getProjections().toString());
  }

  /**
   * Test situation when we use same RE-objects in all projections.
   */
  @Test
  public void test_BUG_WithSameREObject_2REImOneGrammar() {
    String grTest = "#ROOT_SYMBOL S;" + "S -> Adj; L->Noun? Adj? advb;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->adj;, " + "L->advb;, " + "L->noun advb;, " + "L->adj advb;, "
            + "L->noun adj advb;, " + "R_0->T_1;, " + "T_2->EPSILON;, "
            + "T_1->S T_2;, " + "T_1->anyWord T_1;, " + "T_2->anyWord T_2;]",
        grammar.getProjections().toString());
  }

  /**
   * Test situation when we use same RE-objects in all projections.
   */
  @Test
  public void test_REObject_WirhGroupOperator() {
    String grTest = "#ROOT_SYMBOL S;" 
        + "S -> Adj; L->Noun? ( Adj| advb )?;";

    testable.parseGrammar(grTest);
    testable.expandGrammar();

    Grammar grammar = testable.getGrammar();
    assertEquals(
        "[S->adj;, "
        + "L->EPSILON;, "
        + "L->noun;, "
        + "L->adj;, "
        + "L->advb;, "
        + "L->noun adj;, "
        + "L->noun advb;, "
        + "R_0->T_1;, "
        + "T_2->EPSILON;, "
        + "T_1->S T_2;, "
        + "T_1->anyWord T_1;, "
        + "T_2->anyWord T_2;]",
        grammar.getProjections().toString());
  }
  
  /**
   * Typed "kwset[type1,\"статья1\"]" instead "kwset=[type1,\"статья1\"]"
   */
  @Test(expected = IllegalStateException.class)
  public void testIncorrectGrammar01() {
    String grammarText =
        "T-> Word<kwtype=\"статья1\",label=\"статья1\" , gram=\"sing  ,noun\"> "
            + "Word<h-reg1,h-reg2,l-reg,quoted,l-quoted,r-quoted,fw,mw,lat,no-hom, cut,rt,dict> "
            + "Word<GU=[&(nomn,accs)|(sing,noun)],kwset[type1,\"статья1\"],kwsetf=[type1,\"статья1\"], gnc-agr=[1]> ;";

    testable.parseGrammar(grammarText);
  }

}
