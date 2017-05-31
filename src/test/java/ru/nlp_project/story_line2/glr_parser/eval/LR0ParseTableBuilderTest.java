package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.TestFixtureBuilder;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class LR0ParseTableBuilderTest {

  private LR0ParseTableBuilder testable;

  @Before
  public void setUp() throws Exception {
    testable = new LR0ParseTableBuilder();
  }

  @Test
  public void testCalculateClosure() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("A", "BcD"));
    grammar.add(TestFixtureBuilder.createProjection("B", "d"));

    Set<LR0Point> closure = testable
        .calculateClosure(new LR0Point(grammar, 0, 0));

    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    assertEquals("[B->*d, A->*BcD]", sortList.toString());
  }

  @Test
  public void testCalculateClosure_BeforeTerminal() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("A", "BcD"));
    grammar.add(TestFixtureBuilder.createProjection("B", "d"));

    Set<LR0Point> closure = testable
        .calculateClosure(new LR0Point(grammar, 0, 1));

    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    assertEquals("[A->B*cD]", sortList.toString());
  }

  @Test
  public void testCalculateClosure_MultyProjections() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("A", "BcD"));
    grammar.add(TestFixtureBuilder.createProjection("B", "d"));
    grammar.add(TestFixtureBuilder.createProjection("B", "ef"));

    Set<LR0Point> closure = testable
        .calculateClosure(new LR0Point(grammar, 0, 0));

    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    assertEquals("[B->*ef, B->*d, A->*BcD]", sortList.toString());
  }

  @Test
  public void testCalculateClosure_ProjectionWithRightLimits() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("A", "BcD"));
    grammar.add(TestFixtureBuilder.createProjection("B", "d"));
    grammar.add(TestFixtureBuilder.createProjection("D", "e"));

    Set<LR0Point> closure = testable
        .calculateClosure(new LR0Point(grammar, 0, 3));

    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    assertEquals("[A->BcD*]", sortList.toString());
  }

  @Test
  public void testCalculateGoto() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("A", "BcD"));
    grammar.add(TestFixtureBuilder.createProjection("B", "EF"));
    grammar.add(TestFixtureBuilder.createProjection("B", "GHF"));
    grammar.add(TestFixtureBuilder.createProjection("D", "e"));
    grammar.add(TestFixtureBuilder.createProjection("E", "f"));
    grammar.add(TestFixtureBuilder.createProjection("F", "hh"));

    // closure
    Set<LR0Point> closure = testable
        .calculateClosure(new LR0Point(grammar, 0, 0));
    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    assertEquals("[E->*f, B->*GHF, B->*EF, A->*BcD]", sortList.toString());
    // goto
    Set<LR0Point> goto1 = testable.calculateGoto(closure, new Symbol("E",
        SymbolTypes.NonTerminal));
    sortList = new ArrayList<LR0Point>(goto1);
    Collections.sort(sortList);
    assertEquals("[F->*hh, B->E*F]", sortList.toString());
  }

  @Test
  public void testCalculateGoto_RightLimitPoint() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("A", "BcD"));

    // closure
    Set<LR0Point> closure = testable
        .calculateClosure(new LR0Point(grammar, 0, 2));
    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    assertEquals("[A->Bc*D]", sortList.toString());
    // goto
    Set<LR0Point> goto1 = testable.calculateGoto(closure, new Symbol("D",
        SymbolTypes.NonTerminal));
    sortList = new ArrayList<LR0Point>(goto1);
    Collections.sort(sortList);
    assertEquals("[A->BcD*]", sortList.toString());
  }

  @Test
  public void testGetStateNumberByClosure() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    Set<LR0Point> closure = new HashSet<LR0Point>();
    closure.add(new LR0Point(null, 0, 0));
    int a = testable.getStateNumberByClosure(closure);
    int b = testable.getStateNumberByClosure(closure);
    closure.add(new LR0Point(null, 3, 7));
    int c = testable.getStateNumberByClosure(closure);
    assertEquals(0, a);
    assertEquals(0, b);
    assertEquals(1, c);
  }

  /*
  1. S--- >E
  2. E--- >E-T
  3. E--- >T
  4. T--- >n
  5. T--- >(E)
   */
  @Test
  public void testCalculateClosure2() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("S", "E"));
    grammar.add(TestFixtureBuilder.createProjection("E", "E-T"));
    grammar.add(TestFixtureBuilder.createProjection("E", "T"));
    grammar.add(TestFixtureBuilder.createProjection("T", "n"));
    grammar.add(TestFixtureBuilder.createProjection("T", "(E)"));

    Set<LR0Point> closure = testable
        .calculateClosure(new LR0Point(grammar, 0, 0));

    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    assertEquals("[T->*(E), T->*n, E->*T, E->*E-T, S->*E]",
        sortList.toString());
  }

  @Test
  public void testCollectAllSymbols() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("S", "E"));
    grammar.add(TestFixtureBuilder.createProjection("E", "E-T"));
    grammar.add(TestFixtureBuilder.createProjection("E", "T"));
    grammar.add(TestFixtureBuilder.createProjection("T", "n"));
    grammar.add(TestFixtureBuilder.createProjection("T", "(E)"));

    Set<Symbol> closure = testable.collectAllSymbols();

    List<Symbol> sortList = new ArrayList<Symbol>(closure);
    Collections.sort(sortList);
    assertEquals("[(, ), -, n, E, S, T]",
        sortList.toString());
  }

  @Test
  public void testCalculateParseTable_States() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("S", "E"));
    grammar.add(TestFixtureBuilder.createProjection("E", "E-T"));
    grammar.add(TestFixtureBuilder.createProjection("E", "T"));
    grammar.add(TestFixtureBuilder.createProjection("T", "n"));
    grammar.add(TestFixtureBuilder.createProjection("T", "(E)"));

    testable.calculateParseTable();
    //System.out.println(testable.states);
    assertEquals(9, testable.states.size());

  }

  @Test
  public void testGetAcceptingPoints() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("S", "E"));
    grammar.add(TestFixtureBuilder.createProjection("E", "E-T"));
    grammar.add(TestFixtureBuilder.createProjection("E", "T"));
    grammar.add(TestFixtureBuilder.createProjection("T", "n"));
    grammar.add(TestFixtureBuilder.createProjection("T", "(E)"));

    List<LR0Point> points = new ArrayList<LR0Point>();
    points.add(new LR0Point(grammar, 0, 0));
    points.add(new LR0Point(grammar, 1, 3));

    List<LR0Point> acceptingPoints = testable.getAcceptingPoints(points);
    assertEquals("[E->E-T*]", acceptingPoints.toString());

  }
  
  @Test
  public void testCalculateParseTable_ActionTable() {
    Grammar grammar = new Grammar();
    testable.grammar = grammar;

    grammar.add(TestFixtureBuilder.createProjection("S", "E"));
    grammar.add(TestFixtureBuilder.createProjection("E", "E-T"));
    grammar.add(TestFixtureBuilder.createProjection("E", "T"));
    grammar.add(TestFixtureBuilder.createProjection("T", "n"));
    grammar.add(TestFixtureBuilder.createProjection("T", "(E)"));

    testable.calculateParseTable();
    //System.out.println(testable.actionTable);
    assertEquals(9, testable.actionTable.size());

  }
}
