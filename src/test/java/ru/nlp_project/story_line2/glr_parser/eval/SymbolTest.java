package ru.nlp_project.story_line2.glr_parser.eval;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class SymbolTest {

  private Symbol testable;

  @Test
  public void testToString() {
    testable = new Symbol("A", SymbolTypes.NonTerminal);
    assertEquals("A", testable.toString());
    testable.setSymbolType(SymbolTypes.LiteralString);
    assertEquals("'A'", testable.toString());

    testable.setSymbolType(SymbolTypes.Terminal);
    assertEquals("A", testable.toString());
  }

  @Test
  public void testSetSymbolType() {
    testable = new Symbol("A", SymbolTypes.Terminal);
    assertTrue(testable.isTerm());
    assertEquals(SymbolTypes.Terminal, testable.getSymbolType());

    testable.setSymbolType(SymbolTypes.NonTerminal);
    assertFalse(testable.isTerm());
    assertEquals(SymbolTypes.NonTerminal, testable.getSymbolType());

    testable.setSymbolType(SymbolTypes.LiteralString);
    assertTrue(testable.isTerm());
    assertEquals(SymbolTypes.LiteralString, testable.getSymbolType());
  }

  @Test
  public void testEOIEquals() {
    assertEquals(Symbol.EOI, Symbol.createEOISymbol());
  }

  @Test
  public void testCaluclateHash() {
    assertEquals(Symbol.EOI.hashCode(), Symbol.createEOISymbol().hashCode());
  }
  
  
  @Test
  public void testCompareEqualSymbols() {
    Symbol s1 = new Symbol("Person", SymbolTypes.NonTerminal);
    Symbol s2 = new Symbol("Person", SymbolTypes.NonTerminal);
    assertEquals(s1, s2);
  }
}
