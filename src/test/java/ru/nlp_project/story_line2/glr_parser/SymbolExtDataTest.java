package ru.nlp_project.story_line2.glr_parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;

public class SymbolExtDataTest {

  @Test
  public void testMakeKWSetArrayExtData() {
    SymbolExtData testable = SymbolExtData.makeArrayExtData("kwset",
        "\"advb\", \"pattern 1\"");
    assertEquals(SymbolExtDataTypes.kwset, testable.getType());
    assertEquals("[advb, pattern 1]", testable.getKwSetValue().toString());
  }

  @Test
  public void testMakeGUArrayExtData() {
    SymbolExtData testable = SymbolExtData.makeArrayExtData("GU",
        "&(nomn,accs)|(sing,noun)");
    assertEquals(SymbolExtDataTypes.gu, testable.getType());
    assertEquals("[&(accs,nomn), (noun,sing)]", testable.getGuValue()
        .toString());
  }

  @Test
  public void testMakeGUSingleArrayExtData() {
    SymbolExtData testable = SymbolExtData.makeArrayExtData("GU",
        "&(nomn,accs,noun, verb)");
    assertEquals(SymbolExtDataTypes.gu, testable.getType());
    assertEquals("[&(noun,verb,accs,nomn)]", testable.getGuValue().toString());
  }

  @Test
  public void testMakeLabelExtData() {
    SymbolExtData testable = SymbolExtData.makeLabelExtData("h-reg1");
    assertEquals(SymbolExtDataTypes.h_reg1, testable.getType());

  }

  @Test
  public void testMakeGrammParamExtData() {
    SymbolExtData testable = SymbolExtData.makeParamExtData("gram",
        "\"advb, plur\"");
    assertEquals(SymbolExtDataTypes.gram, testable.getType());
    assertEquals("advb, plur", testable.getGrammValue().toString());
  }

  @Test(expected = IllegalStateException.class)
  public void testMakeGrammParamExtData_InccorectGrammeme() {
    SymbolExtData testable = SymbolExtData.makeParamExtData("gram",
        "\"advb, plur2\"");
    assertEquals(SymbolExtDataTypes.gram, testable.getType());
    assertEquals("advb,plur", testable.getGrammValue().toString());
  }

  @Test
  public void testMakeKwtypeParamExtData() {
    SymbolExtData testable = SymbolExtData.makeParamExtData("kwtype",
        "\"статья 1\"");
    assertEquals(SymbolExtDataTypes.kwtype, testable.getType());
    assertEquals("статья 1", testable.getKwTypeValue());
  }

}
