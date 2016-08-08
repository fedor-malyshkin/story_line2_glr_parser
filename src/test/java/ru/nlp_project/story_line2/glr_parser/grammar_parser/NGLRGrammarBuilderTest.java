package ru.nlp_project.story_line2.glr_parser.grammar_parser;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.GrammarManager;
import ru.nlp_project.story_line2.glr_parser.GrammarManager.GrammarDirectiveTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Grammar;

public class NGLRGrammarBuilderTest {
  private NGLRGrammarBuilder testable;

  @Before
  public void setUp() {
    testable = new NGLRGrammarBuilder();
  }

  @Test
  public void testSimpleGrammar() {
    String grammar = "T->'русский_текст' SomeGrammeme;";
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals("T->'русский_текст' SomeGrammeme;", result.toString());
  }

  @Test
  public void testGrammarWithRE() {
    String grammar = "T->(AnotherGrammeme1|AnotherGrammeme2) SomeGrammeme*;";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (regularExpression "
            + "(regularGroupExpression ( (nonRegularExpression (nonTerminalExt AnotherGrammeme1)) | "
            + "(nonRegularExpression (nonTerminalExt AnotherGrammeme2)) ))) (regularExpression (nonRegularExpression "
            + "(nonTerminalExt SomeGrammeme)) (regQuantifier *)) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals("T->(AnotherGrammeme1|AnotherGrammeme2) SomeGrammeme*;",
        result.toString());
  }

  @Test
  public void testGrammarWithLiteralString() {
    String grammar = "T-> '.,:-_буковки12344&' SomeGrammeme;";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> "
            + "(nonRegularExpression (literalStringExt "
            + "(literalString ' . , : - _буковки12344 & '))) "
            + "(nonRegularExpression (nonTerminalExt SomeGrammeme)) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals("T->'.,:-_буковки12344&' SomeGrammeme;", result.toString());
  }

  @Test
  public void testGrammarWithREAndLabels() {
    String grammar =
        "T-> SomeGrammeme<gram=\"sing\", gram      =\"sing, noun\",     rt, gnc-agr=[1asd]>?;";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (regularExpression (nonRegularExpression "
            + "(nonTerminalExt SomeGrammeme (extData < (extDataParam (extDataParamName gram) = "
            + "(extDataParamValue \" sing \")) , (extDataParam (extDataParamName gram) = (extDataParamValue \" sing , noun \")) , "
            + "(extDataLabel (extDataLabelName rt)) , (extDataArray (extDataArrayName gnc - agr) = [ (extDataArrayValue 1asd) ]) >))) "
            + "(regQuantifier ?)) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals(
        "T->SomeGrammeme<rt, gram=\"sing\", gram=\"sing,noun\", gnc-agr=[1asd]>?;",
        result.toString());
  }

  @Test
  public void testGrammarWithInterpInst() {
    String grammar = "T-> SomeGrammeme {Stuff.Staff};"
        + "T-> SomeGrammeme As{A.B, C.Def='абракадабра', G.Hi=Y.L};";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt "
            + "SomeGrammeme (interpData { (interpDataEntry Stuff . Staff) }))) ;) "
            + "(projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt SomeGrammeme)) "
            + "(nonRegularExpression (nonTerminalExt As (interpData { (interpDataEntry A . B) , "
            + "(interpDataEntry C . Def = ' абракадабра ') , "
            + "(interpDataEntry G . Hi = Y . L) }))) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(2, result.size());
    assertEquals(
        "T->SomeGrammeme{Stuff.Staff};\n"
            + "T->SomeGrammeme As{A.B, C.Def='абракадабра', G.Hi=G.Hi};",
        result.toString());
  }

  @Test
  public void testGrammarWithInterpInstWithExtDataParams() {
    String grammar = "T-> SomeGrammeme {Stuff.Staff <no-norm>};"
        + "T-> SomeGrammeme As{A.B, C.Def='абракадабра', G.Hi=Y.L};";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt "
            + "SomeGrammeme (interpData { (interpDataEntry Stuff . Staff < (extDataLabelName no - norm) >) }))) ;) "
            + "(projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt SomeGrammeme)) "
            + "(nonRegularExpression (nonTerminalExt As (interpData { (interpDataEntry A . B) , "
            + "(interpDataEntry C . Def = ' абракадабра ') , (interpDataEntry G . Hi = Y . L) }))) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(2, result.size());
    assertEquals(
        "T->SomeGrammeme{Stuff.Staff (no-norm)};\n"
            + "T->SomeGrammeme As{A.B, C.Def='абракадабра', G.Hi=G.Hi};",
        result.toString());
  }

  @Test
  public void testGrammar_WithMLComments() {
    String grammar = "/*" + "some text" + "*/" + "T-> SomeGrammeme;" + "";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt SomeGrammeme)) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals("T->SomeGrammeme;", result.toString());
  }

  @Test
  public void testGrammar_WithSLComments() {
    String grammar =
        "// немного комментариев и закоменченных грамматик T-> SomeGrammeme; \n"
            + "T-> SomeGrammeme;" + "";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt SomeGrammeme)) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals("T->SomeGrammeme;", result.toString());
  }

  @Test
  public void testGrammarDirectives() {
    String grammar = "#INCLUDE \"some_file.grm\";" + "#FILTER ttest;"
        + "#GRAMMAR_KWSET [\"run01\", \"run02\"];"
        + "T-> SomeGrammeme<gram=\"sing\", gram      =\"sing, noun\",     rt, gnc-agr=[1asd]>?;";
    assertEquals(
        "(nglrGrammar (nglrGrammarDirective (nglrGrammarDirectiveInclude # INCLUDE \" some_file . grm \" ;)) "
            + "(nglrGrammarDirective (nglrGrammarDirectiveFilter # FILTER ttest ;)) "
            + "(nglrGrammarDirective (nglrGrammarDirectiveGrammarKWSet # GRAMMAR_KWSET [ (nglrGrammarDirectiveGrammarKWSetValues \" run01 \") , (nglrGrammarDirectiveGrammarKWSetValues \" run02 \") ] ;))"
            + " (projection (nonTerminal T) -> (regularExpression (nonRegularExpression (nonTerminalExt SomeGrammeme (extData < (extDataParam (extDataParamName gram) = (extDataParamValue \" sing \")) ,"
            + " (extDataParam (extDataParamName gram) = (extDataParamValue \" sing , noun \")) ,"
            + " (extDataLabel (extDataLabelName rt)) ,"
            + " (extDataArray (extDataArrayName gnc - agr) = [ (extDataArrayValue 1asd) ]) >))) (regQuantifier ?)) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals(
        "T->SomeGrammeme<rt, gram=\"sing\", gram=\"sing,noun\", gnc-agr=[1asd]>?;",
        result.toString());
    Map<GrammarDirectiveTypes, Object> grammarDirectives =
        new TreeMap<GrammarManager.GrammarDirectiveTypes, Object>(
            testable.getGrammarDirectives());
    assertEquals("{FILTER=true, INCLUDE=[some_file.grm], KWSET=[run01, run02]}",
        grammarDirectives.toString());
  }

  @Test
  public void testGrammarDirectives_WithGrammEx() {
    String grammar = "T-> SomeGrammeme<gram-ex=\"sing\">;";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (nonRegularExpression "
        + "(nonTerminalExt SomeGrammeme (extData < (extDataParam (extDataParamName gram - ex) = "
        + "(extDataParamValue \" sing \")) >))) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals("T->SomeGrammeme<gram-ex=\"sing\">;", result.toString());
  }

  @Test
  public void testGrammarRestrictions() {
    String grammar = "T-> Param<kwtype=\"статья1\", gram=\"sing  ,noun\"> "
        + "Labels<h-reg1,h-reg2,l-reg,quoted,l-quoted,r-quoted,fw,mw,lat,no-hom, cut,rt,dict> "
        + "Arrays<GU=[&(nomn,accs)|(sing,noun)],kwset=[type1,\"статья1\"],kwsetf=[type1,\"статья1\"], gnc-agr=[1], nc-agr=[2], c-agr=[3], gn-agr=[3], fem-c-agr=[1],after-num-agr=[12],sp-agr=[0], fio-agr=[123], geo-agr=[1]>;";
    assertEquals(
        "(nglrGrammar (projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt "
            + "Param (extData < (extDataParam (extDataParamName kwtype) = (extDataParamValue \" статья1 \")) , "
            + "(extDataParam (extDataParamName gram) = (extDataParamValue \" sing , noun \")) >))) "
            + "(nonRegularExpression (nonTerminalExt "
            + "Labels (extData < (extDataLabel (extDataLabelName h - reg1)) , "
            + "(extDataLabel (extDataLabelName h - reg2)) , (extDataLabel (extDataLabelName l - reg)) , "
            + "(extDataLabel (extDataLabelName quoted)) , (extDataLabel (extDataLabelName l - quoted)) , "
            + "(extDataLabel (extDataLabelName r - quoted)) , (extDataLabel (extDataLabelName fw)) , "
            + "(extDataLabel (extDataLabelName mw)) , (extDataLabel (extDataLabelName lat)) , "
            + "(extDataLabel (extDataLabelName no - hom)) , (extDataLabel (extDataLabelName cut)) , "
            + "(extDataLabel (extDataLabelName rt)) , (extDataLabel (extDataLabelName dict)) >))) "
            + "(nonRegularExpression (nonTerminalExt "
            + "Arrays (extData < (extDataArray (extDataArrayName GU) = [ (extDataArrayValue & ( nomn , accs ) | ( sing , noun )) ]) , "
            + "(extDataArray (extDataArrayName kwset) = [ (extDataArrayValue type1 , \" статья1 \") ]) , "
            + "(extDataArray (extDataArrayName kwsetf) = [ (extDataArrayValue type1 , \" статья1 \") ]) , "
            + "(extDataArray (extDataArrayName gnc - agr) = [ (extDataArrayValue 1) ]) , "
            + "(extDataArray (extDataArrayName nc - agr) = [ (extDataArrayValue 2) ]) , "
            + "(extDataArray (extDataArrayName c - agr) = [ (extDataArrayValue 3) ]) , "
            + "(extDataArray (extDataArrayName gn - agr) = [ (extDataArrayValue 3) ]) , "
            + "(extDataArray (extDataArrayName fem - c - agr) = [ (extDataArrayValue 1) ]) , "
            + "(extDataArray (extDataArrayName after - num - agr) = [ (extDataArrayValue 12) ]) , "
            + "(extDataArray (extDataArrayName sp - agr) = [ (extDataArrayValue 0) ]) , "
            + "(extDataArray (extDataArrayName fio - agr) = [ (extDataArrayValue 123) ]) , "
            + "(extDataArray (extDataArrayName geo - agr) = [ (extDataArrayValue 1) ]) >))) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(1, result.size());
    assertEquals(
        "T->Param<kwtype=\"статья1\", gram=\"sing,noun\"> "
            + "Labels<h-reg1, h-reg2, l-reg, quoted, l-quoted, r-quoted, fw, mw, lat, no-hom, cut, rt, dict> "
            + "Arrays<GU=[&(nomn,accs)|(sing,noun)], kwset=[type1,\"статья1\"], kwsetf=[type1,\"статья1\"], gnc-agr=[1], nc-agr=[2], c-agr=[3], gn-agr=[3], fem-c-agr=[1], after-num-agr=[12], sp-agr=[0], fio-agr=[123], geo-agr=[1]>;",
        result.toString());
  }

  @Test
  public void testGrammarRestrictionsRegexp() {
    String grammar = "T-> Param<rx=\"им\\.\">; T2 -> Param<rx=\"[:punct:]\">; "
        + "T2 -> Param<rx=\"[^ax]{1,2}\">; T2 -> Param<rx=\"им.*\">; "
        + "T2 -> Param<rx=\"им.?\"> ; T2 -> Param<rx=\"им+\">; ";
    assertEquals(
        "(nglrGrammar "
            + "(projection (nonTerminal T) -> (nonRegularExpression (nonTerminalExt "
            + "Param (extData < (extDataParam (extDataParamName rx) = (extDataParamValue \" им \\ . \")) >))) ;) "
            + "(projection (nonTerminal T2) -> (nonRegularExpression (nonTerminalExt "
            + "Param (extData < (extDataParam (extDataParamName rx) = (extDataParamValue \" [ : punct : ] \")) >))) ;) "
            + "(projection (nonTerminal T2) -> (nonRegularExpression (nonTerminalExt "
            + "Param (extData < (extDataParam (extDataParamName rx) = (extDataParamValue \" [ ^ ax ] { 1 , 2 } \")) >))) ;) "
            + "(projection (nonTerminal T2) -> (nonRegularExpression (nonTerminalExt "
            + "Param (extData < (extDataParam (extDataParamName rx) = (extDataParamValue \" им . * \")) >))) ;) "
            + "(projection (nonTerminal T2) -> (nonRegularExpression (nonTerminalExt "
            + "Param (extData < (extDataParam (extDataParamName rx) = (extDataParamValue \" им . ? \")) >))) ;) "
            + "(projection (nonTerminal T2) -> (nonRegularExpression (nonTerminalExt "
            + "Param (extData < (extDataParam (extDataParamName rx) = (extDataParamValue \" им + \")) >))) ;))",
        printTree(grammar));
    parse(grammar, testable);
    Grammar result = testable.getResult();
    assertEquals(6, result.size());
    assertEquals(
        "T->Param<rx=\"им\\.\">;\n" + "T2->Param<rx=\"[:punct:]\">;\n"
            + "T2->Param<rx=\"[^ax]{1,2}\">;\n" + "T2->Param<rx=\"им.*\">;\n"
            + "T2->Param<rx=\"им.?\">;\n" + "T2->Param<rx=\"им+\">;",
        result.toString());
  }

  private void parse(String text, NGLRGrammarBuilder builder) {
    // createaCharStreamthatreadsfromstandardinput
    ANTLRInputStream input = new ANTLRInputStream(text);
    // createalexerthatfeedsoffofinputCharStream
    NGLRLexer lexer = new NGLRLexer(input);
    // createabufferoftokenspulledfromthelexer
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    // createaparserthatfeedsoffthetokensbuffer
    NGLRParser parser = new NGLRParser(tokens);

    parser.removeErrorListeners();
    parser.addErrorListener(new ThrowISEErrorListener());

    ParseTree tree = parser.nglrGrammar();// beginparsingatinitrule

    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(builder, tree);
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
