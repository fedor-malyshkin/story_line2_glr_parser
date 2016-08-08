package ru.nlp_project.story_line2.glr_parser.grammar_parser;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRLexer;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser;

public class NGLRParserTest {

  @Test
  public void testTree_SimpleGrammar() {
    assertEquals(
        "(nglrGrammar (projection (nonTerminal Gas) -> (nonRegularExpression (nonTerminalExt Asd)) "
            + "(nonRegularExpression (nonTerminalExt As)) (nonRegularExpression (literalStringExt "
            + "(literalString ' немного '))) ;))",
        printTree("Gas->Asd As 'немного';"));
  }

  @Test
  public void testTree_MultilineGrammar() {
    assertEquals(
        "(nglrGrammar (projection (nonTerminal G) -> (nonRegularExpression (nonTerminalExt Asd)) "
            + "(nonRegularExpression (nonTerminalExt As)) (nonRegularExpression (literalStringExt (literalString ' немного '))) ;) "
            + "(projection (nonTerminal B) -> (nonRegularExpression (nonTerminalExt Asd)) (nonRegularExpression (nonTerminalExt As)) "
            + "(nonRegularExpression (literalStringExt (literalString ' немного '))) ;))",
        printTree("G->Asd As 'немного';\n" + "B->Asd As 'немного';"));
  }

  @Test
  public void testTree_Comments() {
    assertEquals(
        "(nglrGrammar (projection (nonTerminal G) -> (nonRegularExpression (nonTerminalExt Asd)) "
            + "(nonRegularExpression (nonTerminalExt As)) (nonRegularExpression (literalStringExt (literalString ' немного '))) ;))",
        printTree(
            "G->Asd As 'немного';\n" + "/*" + "comments\n" + "*/\n" + ""));
  }

  @Test
  public void testTree_ExtDataParam() {
    assertEquals(
        "(nglrGrammar (projection (nonTerminal G) -> (nonRegularExpression (nonTerminalExt Asd)) "
            + "(nonRegularExpression (nonTerminalExt As)) (nonRegularExpression (literalStringExt (literalString ' немного ') "
            + "(extData < (extDataLabel (extDataLabelName h - reg1)) >))) ;))",
        printTree("G->Asd As 'немного'<h-reg1>;\n"));
  }

  @Test
  public void testTree_InterpDataWithExtDataParam() {
    assertEquals(
        "(nglrGrammar (projection (nonTerminal G) -> (nonRegularExpression (nonTerminalExt Asd)) "
        + "(nonRegularExpression (nonTerminalExt As (interpData { "
        + "(interpDataEntry Int . data < (extDataLabelName no - norm) >) }))) ;))",
        printTree("G->Asd As {Int.data <no-norm>};\n"));
  }

  private String printTree(String text) {
    // createaCharStreamthatreadsfromstandardinput
    ANTLRInputStream input = new ANTLRInputStream(text);
    // createalexerthatfeedsoffofinputCharStream
    NGLRLexer lexer = new NGLRLexer(input);
    // createabufferoftokenspulledfromthelexer
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    // createaparserthatfeedsoffthetokensbuffer
    NGLRParser parser = new NGLRParser(tokens);
    ParseTree tree = parser.nglrGrammar();// beginparsingatinitrule
    return tree.toStringTree(parser);// printLISP-styletree

  }
}
