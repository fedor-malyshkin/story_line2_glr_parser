package ru.nlp_project.story_line2.glr_parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.InterpreterImpl.Fact;
import ru.nlp_project.story_line2.glr_parser.InterpreterImpl.TreeInterpData;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class InterpreterTest {

  class ParseTreeSerializerStub extends ParseTreeSerializer {

    public String serialize(ParseTreeNode node, boolean normalize) {
      if (normalize)
        return node == null ? "normalized-null"
            : "normalized-" + node.symbol.getValue();
      else
        return node == null ? "not-normalized-null"
            : "not-normalized-" + node.symbol.getValue();
    }
  }

  @SuppressWarnings("deprecation")
  @Before
  public void setUp() {
    testable = new InterpreterImpl();
    testable.setParseTreeSerializer(new ParseTreeSerializerStub());

  }

  private InterpreterImpl testable;

  @Test
  public void testReadFactFile() throws IOException {
    InputStream stream1 =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/InterpreterTest.fact");

    testable.readFactFile(stream1);
    assertEquals(
        "{fact1=Fact [name=fact1, fieldsMap={field1=[field1=null(NUMBER)], field2=[field2=null(STRING)]}]}",
        testable.getFactsMap().toString());
  }

  private List<SymbolInterpData> createInterpDatas(String factName,
      String fieldName) {
    List<SymbolInterpData> res = new LinkedList<SymbolInterpData>();
    res.add(new SymbolInterpData(factName, fieldName, null));
    return res;
  }

  private SymbolExt createSymbolExt(String val, int from, int length,
      List<SymbolInterpData> interpDatas) {
    SymbolExt res = new SymbolExt(val, SymbolTypes.NonTerminal,
        Collections.emptyList(), interpDatas);
    return res;
  }

  private SymbolExt createSymbolExtWInterpDatas(String val, int from,
      int length, String factName, String fieldName) {
    SymbolExt res = new SymbolExt(val, SymbolTypes.NonTerminal,
        Collections.emptyList(), createInterpDatas(factName, fieldName));
    return res;
  }

  private ParseTreeNode createParseTreeNode(String val) {
    return new ParseTreeNode(0, 100, null,
        createSymbolExtWInterpDatas(val, 0, 10, "f3", "f3"));
  }

  @Test
  public void testReadEntranceTreeSimple() throws Exception {
    Symbol symbol = createSymbolExt("test", 0, 10,
        createInterpDatas("testFact", "testField"));
    ParseTreeNode treeNode = new ParseTreeNode(0, 1, null, symbol);
    List<TreeInterpData> treeInterpDatas = testable.readEntranceTree(treeNode);
    assertEquals("[<0,0 testFact.testField>]", treeInterpDatas.toString());
  }

  /**
   * Read complex  structure like this
   * root
   * 
   * @throws IOException
   */
  @Test
  public void testReadEntranceTreeComplex() throws Exception {
    Symbol symbol = createSymbolExt("test", 0, 10, null);
    ParseTreeNode rootNode = new ParseTreeNode(0, 100, null, symbol);
    ParseTreeNode l1 = new ParseTreeNode(0, 100, null, symbol);
    rootNode.addChild(l1);
    ParseTreeNode l11 = new ParseTreeNode(0, 100, null,
        createSymbolExtWInterpDatas("test", 0, 10, "f1", "f1"));
    l1.addChild(l11);

    ParseTreeNode l12 = new ParseTreeNode(0, 100, null,
        createSymbolExtWInterpDatas("test", 0, 10, "f3", "f3"));
    l1.addChild(l12);

    ParseTreeNode l121 = new ParseTreeNode(0, 100, null, symbol);
    l12.addChild(l121);

    ParseTreeNode l1211 = new ParseTreeNode(0, 100, null,
        createSymbolExtWInterpDatas("test", 0, 10, "f2", "f2"));
    l121.addChild(l1211);
    ParseTreeNode l1212 = new ParseTreeNode(0, 100, null, symbol);
    l121.addChild(l1212);

    ParseTreeNode l2 = new ParseTreeNode(0, 100, null, symbol);
    rootNode.addChild(l2);
    ParseTreeNode l21 = new ParseTreeNode(0, 100, null, symbol);
    l2.addChild(l21);
    ParseTreeNode l22 = new ParseTreeNode(0, 100, null,
        createSymbolExtWInterpDatas("test", 0, 10, "f4", "f4"));
    l2.addChild(l22);

    List<TreeInterpData> treeInterpDatas = testable.readEntranceTree(rootNode);
    assertEquals("[<0,0 f1.f1>, <0,0 f2.f2>, <0,0 f3.f3>, <0,0 f4.f4>]",
        treeInterpDatas.toString());
  }

  @Test
  public void testGenerateFactsSimple() throws IOException {
    InputStream stream1 =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/InterpreterTest.fact");

    testable.readFactFile(stream1);
    // testable.setTokens(TestFixtureBuilder.createTokens("ABCDEFGHIJKLMNO"));
    List<TreeInterpData> treeInterpDatas =
        new ArrayList<InterpreterImpl.TreeInterpData>();
    treeInterpDatas.add(testable.newTreeInterpData(0, 3, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", null),
        createParseTreeNode("ter")));
    List<Fact> facts = testable.processTreeInterpDatas(treeInterpDatas);
    assertEquals(
        "[Fact [name=fact1, fieldsMap={field1=[field1=null(NUMBER)], field2=[field2=normalized-ter(STRING)]}]]",
        facts.toString());
  }
  
  
  @Test
  public void testGenerateFactsSimple_NoNormalization() throws IOException {
    InputStream stream1 =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/InterpreterTest.fact");

    testable.readFactFile(stream1);
    // testable.setTokens(TestFixtureBuilder.createTokens("ABCDEFGHIJKLMNO"));
    List<TreeInterpData> treeInterpDatas =
        new ArrayList<InterpreterImpl.TreeInterpData>();
    treeInterpDatas.add(testable.newTreeInterpData(0, 3, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", InterpreterImpl.PARAM_NO_NORM),
        createParseTreeNode("ter")));
    List<Fact> facts = testable.processTreeInterpDatas(treeInterpDatas);
    assertEquals(
        "[Fact [name=fact1, fieldsMap={field1=[field1=null(NUMBER)], field2=[field2=not-normalized-ter(STRING)]}]]",
        facts.toString());
  }

  @Test
  public void testGenerateFactsOverwriteFactValues() throws IOException {
    InputStream stream1 =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/InterpreterTest.fact");

    testable.readFactFile(stream1);
    // testable.setTokens(TestFixtureBuilder.createTokens("ABCDEFGHIJKLMNO"));
    List<TreeInterpData> treeInterpDatas =
        new ArrayList<InterpreterImpl.TreeInterpData>();
    treeInterpDatas.add(testable.newTreeInterpData(0, 3, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", null),
        createParseTreeNode("val1")));
    treeInterpDatas.add(testable.newTreeInterpData(0, 5, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", null),
        createParseTreeNode("val2")));
    treeInterpDatas.add(testable.newTreeInterpData(7, 3, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", null),
        createParseTreeNode("val3")));

    List<Fact> facts = testable.processTreeInterpDatas(treeInterpDatas);
    assertEquals(
        "[Fact [name=fact1, fieldsMap={field1=[field1=null(NUMBER)], field2=[field2=normalized-val2(STRING)]}], "
            + "Fact [name=fact1, fieldsMap={field1=[field1=null(NUMBER)], field2=[field2=normalized-val3(STRING)]}]]",
        facts.toString());
  }

  @Test
  public void testGenerateFactsWithOneFieldOccurence() throws IOException {
    InputStream stream1 =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(
            "ru/nlp_project/story_line2/glr_parser/InterpreterTest.fact");

    testable.readFactFile(stream1);
    // testable.setTokens(TestFixtureBuilder.createTokens("ABCDEFGHIJKLMNO"));
    List<TreeInterpData> treeInterpDatas =
        new ArrayList<InterpreterImpl.TreeInterpData>();
    treeInterpDatas.add(testable.newTreeInterpData(0, 3, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", null),
        createParseTreeNode("val1")));
    treeInterpDatas.add(testable.newTreeInterpData(0, 5, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", null),
        createParseTreeNode("val2")));
    treeInterpDatas.add(testable.newTreeInterpData(7, 3, "fact1", "field2",
        new SymbolInterpData("fact1", "field2", null),
        createParseTreeNode("val3")));
    treeInterpDatas.add(testable.newTreeInterpData(1, 2, "fact1", "field1",
        new SymbolInterpData("fact1", "field1", null),
        createParseTreeNode("val4")));
    List<Fact> facts = testable.processTreeInterpDatas(treeInterpDatas);
    assertEquals(
        "[Fact [name=fact1, fieldsMap={field1=[field1=normalized-val4(NUMBER)], field2=[field2=normalized-val2(STRING)]}], "
            + "Fact [name=fact1, fieldsMap={field1=[field1=normalized-val4(NUMBER)], field2=[field2=normalized-val3(STRING)]}]]",
        facts.toString());
  }
}
