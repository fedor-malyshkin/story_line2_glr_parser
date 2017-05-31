package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.TestFixtureBuilder;

public class GrammarTest {

  private Grammar testable;

  @Before
  public void setUp() {
    testable = new Grammar();
  }

  @Test
  public void testReplaceEpsilonSymbol() {
    testable.add(TestFixtureBuilder.createProjection("E", "E-T"));
    testable.add(TestFixtureBuilder.createProjection("E", "ε"));
    testable.add(TestFixtureBuilder.createProjection("E", "T"));

    testable.replaceEpsilonSymbols();
    assertEquals("E->E - T;\n" + "E->EPSILON;\n" + "E->T;",
        testable.toString());
  }

  @Test
  public void testCollectAllSymbols() {
    testable.add(TestFixtureBuilder.createProjection("S", "E"));
    testable.add(TestFixtureBuilder.createProjection("E", "E-T"));
    testable.add(TestFixtureBuilder.createProjection("E", "T"));
    testable.add(TestFixtureBuilder.createProjection("T", "n"));
    testable.add(TestFixtureBuilder.createProjection("T", "(E)"));

    testable.collectAllGrammarSymbols();

    Set<Symbol> closure = testable.getAllSymbols();

    List<Symbol> sortList = new ArrayList<Symbol>(closure);
    Collections.sort(sortList);
    assertEquals("[(, ), -, n, E, S, T, EOI]", sortList.toString());
  }

  @Test
  public void testCollectNullableNonterminals() {

    /*
     * Example from
     * "Parsing Techniques: A Practical Guide by Dick Grune, Ceriel J. Jacobs" S
     * ---> A | AB | B A ---> C B ---> D C ---> p | ε D ---> q
     * 
     */

    testable.add(TestFixtureBuilder.createProjection("S", "A"));
    testable.add(TestFixtureBuilder.createProjection("S", "B"));
    testable.add(TestFixtureBuilder.createProjection("S", "AB"));
    testable.add(TestFixtureBuilder.createProjection("A", "C"));
    testable.add(TestFixtureBuilder.createProjection("B", "D"));
    testable.add(TestFixtureBuilder.createProjection("C", "p"));
    testable.add(TestFixtureBuilder.createProjection("C", "ε"));
    testable.add(TestFixtureBuilder.createProjection("D", "q"));

    testable.replaceEpsilonSymbols();
    testable.collectAllGrammarSymbols();
    testable.composeNonTerminalProjectionsMap();
    testable.collectNullableNonterminals();

    assertEquals("A=true, B=false, C=true, D=false, S=true",
        testable.getIsNullableNonterminalMap().entrySet().stream()
            .map((e) -> String.format("%s=%s", e.getKey(), e.getValue()))
            .sorted().collect(Collectors.joining(", ")));
  }
}
