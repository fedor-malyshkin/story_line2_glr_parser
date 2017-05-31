package ru.nlp_project.story_line2.glr_parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.EnumUtils;

import ru.nlp_project.story_line2.glr_parser.GrammarManagerImpl.GrammarDirectiveTypes;
import ru.nlp_project.story_line2.glr_parser.SymbolRE.RETypes;
import ru.nlp_project.story_line2.glr_parser.SymbolTable.SymbolTableEntryTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Grammar;
import ru.nlp_project.story_line2.glr_parser.eval.Projection;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRGrammarBuilder;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRLexer;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.ThrowISEErrorListener;
import ru.nlp_project.story_line2.morph.GrammemeEnum;

/**
 * 
 * Обработчик грамматик, реализующий все основные опреации над объектной моделью грамматики.
 * 
 * Использует промежуточную объектную модель (из экземпляров {@link NGLRGrammarBuilder}}) для 
 * её обработки и построения полноценной модели для GLR-анализатора.
 * 
 *  MULTITHREAD_SAFE: FALSE
 * 
 * @author fedor
 *
 */
public class NGLRGrammarProcessor {

  private Grammar grammar;
  private Map<GrammarDirectiveTypes, Object> grammarDirectives;
  protected Map<String, GrammemeEnum> grammemeEnumMap = new HashMap<>();
  private SymbolTable symbolTable;
  private int tempCounter;

  public NGLRGrammarProcessor() {
    grammemeEnumMap = EnumUtils.getEnumMap(GrammemeEnum.class);
  }

  /**
   * Заполнить таблицу символов ключевыми словами.
   */
  protected void addToSymbolTableProjectionHeads() {
    for (Projection prj : grammar.getProjections()) {
      symbolTable.markAsProjectionHead(prj.getHead());
      prj.getHead().setSymbolType(SymbolTypes.NonTerminal);
    } // for (Projection prj : grammar.getProjections()) {
  }

  /**
   * Добавить по краям корневых выражений символы с "*" квантификатором.
   */
  private void appendAnyTokenWithKleeneOnBorderToRootProjections() {
    if (grammar.getRootSymbol() == null)
      throw new IllegalStateException("root_symbol is null.");

    for (Projection prj : grammar.getProjections()) {
      if (!prj.getHead().equals(grammar.getRootSymbol()))
        continue;
      // skip rules form "S->epsilon"
      if (prj.getBody().contains(Symbol.EPSILON) && prj.getBody().size() == 1)
        continue;
      prj.getBody().add(0, makeAnyTokenWithKleeneStarQuantifier());
      prj.getBody().add(makeAnyTokenWithKleeneStarQuantifier());
    } // for (Projection prj : grammar.getProjections()) {
  }

  private void checkRootValueForCorrectness() {
    List<Integer> projs =
        grammar.getNonTerminalProjectionsMap().get(grammar.getRootSymbol());
    if (projs == null || projs.isEmpty())
      throw new IllegalStateException("Incorrect 'root_symbol' value");
  }

  /**
   * Проверить таблицу символов.
   */
  protected void checkSymbolTable() {
    // TODO Auto-generated method stub
  }

  private void capitalizeKeywords() {
    for (Projection prj : grammar.getProjections()) {
      for (Symbol smb : prj.getBody()) {
        // all keywords to lowercase
        String capitalizedKeyword = symbolTable
            .getNormalCapitalizedKeyword(smb.getValue().toLowerCase());
        if (capitalizedKeyword != null)
          smb.setValue(capitalizedKeyword);
      } // for (Symbol smb: prj.getBody()) {
    }
  }

  protected void expandGrammar() {
    // initialize
    symbolTable = new SymbolTable();
    symbolTable.initialize();
    tempCounter = 0;

    processRegularExpressions();
    // process root symbol directives
    String rootSymbolName =
        (String) grammarDirectives.get(GrammarDirectiveTypes.ROOT);
    grammar.prepareGrammar(rootSymbolName);

    // add new root (in obligatory order: append new root, after it
    // kleene starts to it: "NewRoot -> .* OldRoot .*")
    wrapNewRoot();

    // again
    appendAnyTokenWithKleeneOnBorderToRootProjections();
    processRegularExpressions();

    capitalizeKeywords();
    // re-prepare frammar after modifications
    grammar.prepareGrammar(rootSymbolName);
    addToSymbolTableProjectionHeads();
    setSymbolTerminalMark();
    checkSymbolTable();
    setSymbolGrammemeValue();
    checkRootValueForCorrectness();
  }

  public void expandGrammar(Grammar grammar,
      Map<GrammarDirectiveTypes, Object> grammarDirectives) {
    this.grammar = grammar;
    this.grammarDirectives = grammarDirectives;
    expandGrammar();
  }

  private Symbol generateTemporalNonTerminalSymbol() {
    return new Symbol("T_" + tempCounter++, SymbolTypes.NonTerminal);
  }

  public Grammar getGrammar() {
    return grammar;
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  public Map<GrammarDirectiveTypes, Object> getGrammarDirectives() {
    return grammarDirectives;
  }

  private Symbol makeAnyTokenWithKleeneStarQuantifier() {
    SymbolRE result = new SymbolRE(RETypes.SingleElement,
        new SymbolExt(SymbolTable.KW_ANY_WORD, SymbolTypes.Terminal, null),
        "*");
    return result;
  }

  /**
   * Выполнить первоначальный парсинг текста для построения внутренней объектной формы грамматики.
   * 
   * @param grammarText
   */
  public void parseGrammar(String grammarText) {
    NGLRGrammarBuilder grammarBuilder = new NGLRGrammarBuilder();
    // createaCharStreamthatreadsfromstandardinput
    ANTLRInputStream input = new ANTLRInputStream(grammarText);
    // createalexerthatfeedsoffofinputCharStream
    NGLRLexer lexer = new NGLRLexer(input);
    // createabufferoftokenspulledfromthelexer
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    // createaparserthatfeedsoffthetokensbuffer
    NGLRParser parser = new NGLRParser(tokens);
    // error listeners
    parser.removeErrorListeners();
    parser.addErrorListener(new ThrowISEErrorListener());

    ParseTree tree = parser.nglrGrammar();// begin parsing at init rule

    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(grammarBuilder, tree);

    grammar = grammarBuilder.getResult();
    grammarDirectives = grammarBuilder.getGrammarDirectives();

  }

  /**
   * Выполнить обработку регулярных выражений в грамматике. 
   * Преобразовать в стандартные выражения CFG.
   * 
   * Схема такова, что обрабатывается проекция за проекцией при этом вначале обрабатываются кванторы, 
   * потом раскрываются типы RE (при этом дальнейшая обработка проекции тут же прекращается) - потом по кругу. 
   * При этом всегда формируются новые проекции, а старые, 
   * где был обнаружен квантор или RE помечается на удаление (LHS = null)
   */
  private void processRegularExpressions() {
    List<Projection> projections = grammar.getProjections();
    next_proj: for (int i = 0; i < projections.size(); i++) {
      Projection projection = projections.get(i);
      // prj modification flag (has RE)
      for (int j = 0; j < projection.getBody().size(); j++) {
        Symbol smb = projection.getBody().get(j);
        if (SymbolRE.class.isInstance(smb)) {
          SymbolRE re = (SymbolRE) smb;
          if (re.getQuantifier() != null) {
            processRegularExpressionsQuantifier(re, i, j);
            continue next_proj;
          }
          switch (re.getReSymbolType()) {
          case SingleElement:
            processRegularExpressionsSingleElement(re, i, j);
            continue next_proj;
          case Group:
            processRegularExpressionsGroupElement(re, i, j);
            continue next_proj;
          default:
            throw new IllegalStateException(
                "Not implemented type of regular expression.");
          }
        } // if (RESymbol.class.isInstance(smb)) {
      } // for (int j = 0; j < projection.getBody().size(); j++) {
    } // while (prjIter.hasNext()) {

    // remove all projections with null heads
    Iterator<Projection> prjIter = grammar.getProjections().iterator();
    while (prjIter.hasNext()) {
      Projection projection = prjIter.next();
      if (projection.getHead() == null)
        prjIter.remove();
    }
  }

  /*
   * P->b (R1|R2|..|Rn) a
   * ------------
   * P->b R1 a
   * P->b R2 a
   * ....
   * P->b Rn a
   * 
   */
  private void processRegularExpressionsGroupElement(SymbolRE symbolRE,
      int prjIndx, int prjPos) {
    List<Symbol> body = grammar.get(prjIndx).getBody();
    Symbol head = grammar.get(prjIndx).getHead();
    List<Symbol> b = new ArrayList<Symbol>(body.subList(0, prjPos));
    List<Symbol> a =
        new ArrayList<Symbol>(body.subList(prjPos + 1, body.size()));

    // remove original projection
    grammar.get(prjIndx).setHead(null);

    // create add-al projections
    for (SymbolExt symb : symbolRE.getElements()) {
      ArrayList<Symbol> tempBody = new ArrayList<Symbol>(Symbol.cloneList(b));
      tempBody.add(symb.clone());
      tempBody.addAll(Symbol.cloneList(a));
      if (tempBody.size() == 0)
        tempBody.add(Symbol.EPSILON);
      grammar.add(new Projection(head, tempBody));
    }
  }

  /*
   * P->b R* a
   * ------------
   * P->b T
   * T->R T
   * T->a
   * 
   */
  private void processRegularExpressionsKleeneStarQuantifier(SymbolRE symbolRE,
      int prjIndx, int prjPos) {
    List<Symbol> body = grammar.get(prjIndx).getBody();
    List<Symbol> b = new ArrayList<Symbol>(body.subList(0, prjPos));
    List<Symbol> a =
        new ArrayList<Symbol>(body.subList(prjPos + 1, body.size()));

    ArrayList<Symbol> tempBody = new ArrayList<Symbol>(Symbol.cloneList(a));
    if (tempBody.size() == 0)
      tempBody.add(Symbol.EPSILON);

    Symbol T = generateTemporalNonTerminalSymbol();
    // T->a
    grammar.add(new Projection(T, tempBody));
    // T-> R T
    tempBody = new ArrayList<Symbol>();
    symbolRE.setQuantifier(null);
    tempBody.add(symbolRE);
    tempBody.add(T);
    grammar.add(new Projection(T, tempBody));

    // P-> b T
    tempBody = new ArrayList<Symbol>(Symbol.cloneList(b));
    tempBody.add(T);
    if (tempBody.size() == 0)
      tempBody.add(Symbol.EPSILON);

    grammar.add(new Projection(grammar.get(prjIndx).getHead(), tempBody));

    // remove original projection (set head to null)
    grammar.get(prjIndx).setHead(null);
  }

  /*
   * P->b R+ a
   * ------------
   * P->b R T
   * T->R T
   * T->a
   * 
   */
  private void processRegularExpressionsPlusQuantifier(SymbolRE symbolRE,
      int prjIndx, int prjPos) {
    List<Symbol> body = grammar.get(prjIndx).getBody();
    Symbol head = grammar.get(prjIndx).getHead();
    List<Symbol> b = new ArrayList<Symbol>(body.subList(0, prjPos));
    List<Symbol> a =
        new ArrayList<Symbol>(body.subList(prjPos + 1, body.size()));

    Symbol T = generateTemporalNonTerminalSymbol();
    // P->b R T
    ArrayList<Symbol> tempBody = new ArrayList<Symbol>(Symbol.cloneList(b));
    symbolRE.setQuantifier(null);
    tempBody.add(symbolRE);
    tempBody.add(T);
    grammar.add(new Projection(head, tempBody));

    // T->R T
    tempBody = new ArrayList<Symbol>();
    tempBody.add(symbolRE.clone());
    tempBody.add(T);
    grammar.add(new Projection(T, tempBody));

    // T->a
    tempBody = new ArrayList<Symbol>(Symbol.cloneList(a));
    if (tempBody.size() == 0)
      tempBody.add(Symbol.EPSILON);
    grammar.add(new Projection(T, tempBody));

    // remove original projection (set head to null)
    grammar.get(prjIndx).setHead(null);
  }

  private void processRegularExpressionsQuantifier(SymbolRE symbolRE,
      int prjIndx, int prjPos) {
    switch (symbolRE.getQuantifier()) {
    case "*":
      processRegularExpressionsKleeneStarQuantifier(symbolRE, prjIndx, prjPos);
      break;
    case "?":
      processRegularExpressionsQuestionQuantifier(symbolRE, prjIndx, prjPos);
      break;
    case "+":
      processRegularExpressionsPlusQuantifier(symbolRE, prjIndx, prjPos);
      break;

    default:
      throw new IllegalStateException("Not implemented");
    }

  }

  /*
   * P->b R? a
   * ------------
   * P->b R a
   * P->b a
   * 
   */
  private void processRegularExpressionsQuestionQuantifier(SymbolRE symbolRE,
      int prjIndx, int prjPos) {
    List<Symbol> body = grammar.get(prjIndx).getBody();
    Symbol head = grammar.get(prjIndx).getHead();
    List<Symbol> b = new ArrayList<Symbol>(body.subList(0, prjPos));
    List<Symbol> a =
        new ArrayList<Symbol>(body.subList(prjPos + 1, body.size()));
    // P->b R a
    ArrayList<Symbol> tempBody = new ArrayList<Symbol>(Symbol.cloneList(b));
    symbolRE.setQuantifier(null);
    tempBody.add(symbolRE);
    tempBody.addAll(Symbol.cloneList(a));
    grammar.add(new Projection(head, tempBody));

    // P->b a
    tempBody = new ArrayList<Symbol>(Symbol.cloneList(b));
    tempBody.addAll(Symbol.cloneList(a));
    if (tempBody.size() == 0)
      tempBody.add(Symbol.EPSILON);
    grammar.add(new Projection(head, tempBody));

    // remove original projection (set head to null)
    grammar.get(prjIndx).setHead(null);
  }

  /*
   * Nothing exceptional - simple put inner element instead.
   * But make new projection because there can be other RE-symbols in tail of projection (which can be leaved by unprocessed).
   */
  private void processRegularExpressionsSingleElement(SymbolRE symbolRE,
      int prjIndx, int prjPos) {
    Symbol head = grammar.get(prjIndx).getHead();
    List<Symbol> body = grammar.get(prjIndx).getBody();
    body.set(prjPos, symbolRE.getElement());
    grammar.add(new Projection(head, body));

    // remove original projection
    grammar.get(prjIndx).setHead(null);
  }

  /**
   * Присвоить значение ключевым словам.
   */
  private void setSymbolGrammemeValue() {
    for (Projection prj : grammar.getProjections()) {
      for (Symbol smb : prj.getBody()) {
        if (symbolTable.getSymbolTableValue(
            smb.getValue()) != SymbolTableEntryTypes.KeywordGrammeme)
          continue;
        GrammemeEnum gr = grammemeEnumMap.get(smb.getValue().toLowerCase());
        if (gr != null)
          smb.setGrammeme(gr);
      } // for (Symbol smb: prj.getBody()) {
    } // for (Projection prj : grammar.getProjections()) {

  }

  /**
   * Выставить соответствующие отметки: терминал/нетерминал.
   */
  private void setSymbolTerminalMark() {
    for (Projection prj : grammar.getProjections()) {
      for (Symbol smb : prj.getBody()) {
        if (smb.isEpsilon())
          continue;
        if (smb.getSymbolType() == SymbolTypes.LiteralString)
          continue;
        if (symbolTable.isTerminal(smb))
          smb.setSymbolType(SymbolTypes.Terminal);
        else
          smb.setSymbolType(SymbolTypes.NonTerminal);

      } // for (Symbol smb: prj.getBody()) {
    } // for (Projection prj : grammar.getProjections()) {

  }

  private void wrapNewRoot() {
    Symbol root = new Symbol("R_" + tempCounter++, SymbolTypes.NonTerminal);
    grammar.wrapNewRoot(root);
  }

}
