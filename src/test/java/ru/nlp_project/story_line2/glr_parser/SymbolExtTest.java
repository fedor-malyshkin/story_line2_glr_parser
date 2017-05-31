package ru.nlp_project.story_line2.glr_parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class SymbolExtTest {

  /**
   * Проверка совпадения 2-х классов, при условии отсуствия расширенных данных
   */
  @Test
  public void testHashCode_Equals() {
    Symbol s1 = new Symbol("R", SymbolTypes.NonTerminal);
    SymbolExt s2 =
        new SymbolExt(new String("R"), SymbolTypes.NonTerminal, null);
    
    assertEquals(s1.hashCode(), s2.hashCode());
    assertTrue(s1.equals(s2));
    assertEquals(0, s1.compareTo(s2));
    assertTrue(s2.equals(s1));
    assertEquals(0, s2.compareTo(s1));
  }

  
  /**
   * Проверка совпадения 2-х классов, при условии отсуствия расширенных данных
   */
  @Test
  public void testHashCode_Equals2() {

    List<SymbolExtData> s1ed = new ArrayList<SymbolExtData>(10);
    s1ed.add(SymbolExtData.makeLabelExtData("h-reg1"));

    SymbolExt s1 = new SymbolExt(new String("R"), SymbolTypes.NonTerminal, s1ed);

    List<SymbolExtData> s2ed = new ArrayList<SymbolExtData>(10);
    s2ed.add(SymbolExtData.makeLabelExtData("h-reg2"));
    SymbolExt s2 =
        new SymbolExt(new String("R"), SymbolTypes.NonTerminal, s2ed);
    
    assertNotEquals(s1.hashCode(), s2.hashCode());
    assertFalse(s1.equals(s2));
    assertNotEquals(0, s1.compareTo(s2));
    assertFalse(s2.equals(s1));
    assertNotEquals(0, s2.compareTo(s1));
  }

  
  
  /**
   * Проверка на отсуствие влияния порядка указания данных помет на сравнение и сортировку. 
   */
  @Test
  public void testHashCode_Equals_SymbolExtData_Order() {
    Symbol s0 = new Symbol("R", SymbolTypes.NonTerminal);

    List<SymbolExtData> s1ed = new ArrayList<SymbolExtData>(10);
    s1ed.add(SymbolExtData.makeLabelExtData("h-reg1"));
    s1ed.add(SymbolExtData.makeLabelExtData("h-reg2"));
    s1ed.add(SymbolExtData.makeArrayExtData("nc-agr", "1"));
    SymbolExt s1 =
        new SymbolExt(new String("R"), SymbolTypes.NonTerminal, s1ed);

    List<SymbolExtData> s2ed = new ArrayList<SymbolExtData>(10);

    s2ed.add(SymbolExtData.makeArrayExtData("nc-agr", "1"));
    s2ed.add(SymbolExtData.makeLabelExtData("h-reg2"));
    s2ed.add(SymbolExtData.makeLabelExtData("h-reg1"));
    SymbolExt s2 =
        new SymbolExt(new String("R"), SymbolTypes.NonTerminal, s2ed);

    assertEquals("R<h-reg1, h-reg2, nc-agr=[1]>", s1.toString());
    assertEquals("R<h-reg1, h-reg2, nc-agr=[1]>", s2.toString());
    assertEquals(s1.toString(), s2.toString());
    assertEquals(s1, s2);


    assertEquals(s1.hashCode(), s2.hashCode());
    assertEquals(0, s1.compareTo(s2));
    assertEquals(0, s2.compareTo(s1));

    assertNotEquals(0, s0.compareTo(s2));
    assertNotEquals(s0, s2);
    assertNotEquals(0, s0.compareTo(s2));
    
    assertFalse(s0.equals(s1));
  }

}
