package ru.nlp_project.story_line2.glr_parser.grammar_parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import ru.nlp_project.story_line2.glr_parser.GrammarManager.GrammarDirectiveTypes;
import ru.nlp_project.story_line2.glr_parser.Interpreter;
import ru.nlp_project.story_line2.glr_parser.SymbolExt;
import ru.nlp_project.story_line2.glr_parser.SymbolExtData;
import ru.nlp_project.story_line2.glr_parser.SymbolInterpData;
import ru.nlp_project.story_line2.glr_parser.SymbolRE;
import ru.nlp_project.story_line2.glr_parser.SymbolRE.RETypes;
import ru.nlp_project.story_line2.glr_parser.eval.Grammar;
import ru.nlp_project.story_line2.glr_parser.eval.Projection;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataArrayContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataArrayNameContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataArrayValueContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataLabelContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataLabelNameContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataParamContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataParamNameContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ExtDataParamValueContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.InterpDataContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.InterpDataEntryContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.LiteralStringContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.LiteralStringExtContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarDirectiveContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarDirectiveFilterContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarDirectiveGrammarKWSetContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarDirectiveGrammarKWSetValuesContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarDirectiveGrammarRootContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarDirectiveIncludeContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NglrGrammarDirectiveNoInterpretationContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NonRegularExpressionContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NonTerminalContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.NonTerminalExtContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.ProjectionContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.RegQuantifierContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.RegularExpressionContext;
import ru.nlp_project.story_line2.glr_parser.grammar_parser.NGLRParser.RegularGroupExpressionContext;

/**
 * Основной построитель грамматики, реализующий необходимые обработчики парсера грамматики.
 * 
 * Кроме того, содежит в себе все классы для построеиния объектной модели грамматики.
 * Построение происходит в момент инициализации и чтения конфигурационных файлов - 
 * поэтому не является проблемой для многопоточного использования 
 * (кроме того при исполнении сам не используется).
 *
 * @author fedor
 *
 */
public class NGLRGrammarBuilder implements NGLRListener {
  private Grammar grammar = null;

  private List<SymbolExtData> currExtDatas;
  private List<SymbolInterpData> currInterpDatas;
  private Map<ParseTree, SymbolExt> symbolsMap =
      new IdentityHashMap<ParseTree, SymbolExt>();
  private Map<GrammarDirectiveTypes, Object> grammarDirectives;

  @Override
  public void enterLiteralStringExt(LiteralStringExtContext ctx) {
  }

  @Override
  public void exitLiteralStringExt(LiteralStringExtContext ctx) {
    SymbolExt smb = new SymbolExt(ctx.value.ret.toLowerCase(),
        SymbolTypes.LiteralString, currExtDatas, currInterpDatas);
    symbolsMap.put(ctx, smb);
    // clear "currExtDatas"
    currExtDatas = new LinkedList<SymbolExtData>();
    // clear InterpDatas
    currInterpDatas = new LinkedList<SymbolInterpData>();
  }

  @Override
  public void enterExtData(ExtDataContext ctx) {
    currExtDatas = new LinkedList<SymbolExtData>();
  }

  @Override
  public void exitExtData(ExtDataContext ctx) {
  }

  public NGLRGrammarBuilder() {
    grammar = new Grammar();
    grammarDirectives = new HashMap<GrammarDirectiveTypes, Object>();
  }

  public Grammar getResult() {
    return grammar;
  }

  @Override
  public void visitTerminal(TerminalNode node) {

  }

  @Override
  public void visitErrorNode(ErrorNode node) {

  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
  }

  @Override
  public void enterNglrGrammar(NglrGrammarContext ctx) {
    grammar = new Grammar();
  }

  @Override
  public void exitNglrGrammar(NglrGrammarContext ctx) {

  }

  @Override
  public void enterProjection(ProjectionContext ctx) {
  }

  @Override
  public void enterExtDataArrayName(ExtDataArrayNameContext ctx) {
  }

  @Override
  public void exitExtDataArrayName(ExtDataArrayNameContext ctx) {
  }

  @Override
  public void enterExtDataLabel(ExtDataLabelContext ctx) {
  }

  @Override
  public void exitExtDataLabel(ExtDataLabelContext ctx) {
    SymbolExtData result = SymbolExtData.makeLabelExtData(ctx.name.ret);
    currExtDatas.add(result);
  }

  @Override
  public void enterExtDataArrayValue(ExtDataArrayValueContext ctx) {
  }

  @Override
  public void exitExtDataArrayValue(ExtDataArrayValueContext ctx) {
  }

  @Override
  public void enterExtDataParamName(ExtDataParamNameContext ctx) {

  }

  @Override
  public void exitExtDataParamName(ExtDataParamNameContext ctx) {
  }

  @Override
  public void enterExtDataLabelName(ExtDataLabelNameContext ctx) {
  }

  @Override
  public void exitExtDataLabelName(ExtDataLabelNameContext ctx) {
  }

  @Override
  public void enterExtDataParamValue(ExtDataParamValueContext ctx) {
  }

  @Override
  public void exitExtDataParamValue(ExtDataParamValueContext ctx) {

  }

  @Override
  public void enterExtDataParam(ExtDataParamContext ctx) {
  }

  @Override
  public void exitExtDataParam(ExtDataParamContext ctx) {
    SymbolExtData result =
        SymbolExtData.makeParamExtData(ctx.name.ret, ctx.value.ret);
    currExtDatas.add(result);

  }

  @Override
  public void enterExtDataArray(ExtDataArrayContext ctx) {
  }

  @Override
  public void exitExtDataArray(ExtDataArrayContext ctx) {
    SymbolExtData result =
        SymbolExtData.makeArrayExtData(ctx.name.ret, ctx.value.ret);
    currExtDatas.add(result);

  }

  @Override
  public void enterLiteralString(LiteralStringContext ctx) {
  }

  @Override
  public void exitLiteralString(LiteralStringContext ctx) {
  }

  @Override
  public void enterRegularGroupExpression(RegularGroupExpressionContext ctx) {
  }

  @Override
  public void exitRegularGroupExpression(RegularGroupExpressionContext ctx) {
    // multi value
    List<SymbolExt> alternations = getChildrenSymbols(ctx);
    SymbolRE symbolRE = new SymbolRE(RETypes.Group, alternations, null);
    symbolsMap.put(ctx, symbolRE);
  }

  protected List<SymbolExt> getChildrenSymbols(ParserRuleContext ctx) {
    List<SymbolExt> alternations = new LinkedList<SymbolExt>();
    int l = ctx.getChildCount();
    for (int i = 0; i < l; i++) {
      ParseTree parseTree = ctx.getChild(i);
      SymbolExt symbol = symbolsMap.get(parseTree);
      if (null != symbol)
        alternations.add(symbol);
    }
    return alternations;
  }

  protected SymbolExt getChildSymbol(ParserRuleContext ctx) {
    int l = ctx.getChildCount();
    for (int i = 0; i < l; i++) {
      SymbolExt symbol = symbolsMap.get(ctx.getChild(i));
      if (null != symbol)
        return symbol;
    }
    return null;
  }

  @Override
  public void enterRegularExpression(RegularExpressionContext ctx) {
  }

  @Override
  public void exitRegularExpression(RegularExpressionContext ctx) {
    // single value
    SymbolExt symbol = getChildSymbol(ctx);
    SymbolRE symbolRE = null;
    if (SymbolRE.class.isAssignableFrom(symbol.getClass())) {
      // re (group)
      symbolRE = (SymbolRE) symbol;
      if (ctx.quant != null)
        symbolRE.setQuantifier(ctx.quant.ret);
    } else {
      // not re
      symbolRE = new SymbolRE(RETypes.SingleElement, (SymbolExt) symbol, null);
      if (ctx.quant != null)
        symbolRE.setQuantifier(ctx.quant.ret);
    }
    symbolsMap.put(ctx, symbolRE);
  }

  @Override
  public void enterRegQuantifier(RegQuantifierContext ctx) {
  }

  @Override
  public void exitRegQuantifier(RegQuantifierContext ctx) {
  }

  @Override
  public void exitProjection(ProjectionContext ctx) {
    // multi value

    Symbol head = symbolsMap.get(ctx.lhs);
    List<SymbolExt> body = getChildrenSymbols(ctx);
    // удалить head
    body.remove(0);
    grammar.getProjections().add(new Projection(head, body));
  }

  @Override
  public void enterNonTerminalExt(NonTerminalExtContext ctx) {
  }

  @Override
  public void exitNonTerminalExt(NonTerminalExtContext ctx) {
    SymbolExt smb = new SymbolExt(ctx.name.getText(), SymbolTypes.NonTerminal,
        currExtDatas, currInterpDatas);
    symbolsMap.put(ctx, smb);
    // clear "currExtDatas"
    currExtDatas = new LinkedList<SymbolExtData>();
    // clear InterpDatas
    currInterpDatas = new LinkedList<SymbolInterpData>();
  }

  @Override
  public void enterNonRegularExpression(NonRegularExpressionContext ctx) {
  }

  @Override
  public void exitNonRegularExpression(NonRegularExpressionContext ctx) {
    // single value
    SymbolExt childSymbol = getChildSymbol(ctx);
    symbolsMap.put(ctx, childSymbol);
  }

  @Override
  public void enterNonTerminal(NonTerminalContext ctx) {

  }

  @Override
  public void exitNonTerminal(NonTerminalContext ctx) {
    SymbolExt smb =
        new SymbolExt(ctx.name.getText(), SymbolTypes.NonTerminal, null);
    symbolsMap.put(ctx, smb);
  }

  public Map<GrammarDirectiveTypes, Object> getGrammarDirectives() {
    return grammarDirectives;
  }

  @Override
  public void enterNglrGrammarDirectiveGrammarKWSet(
      NglrGrammarDirectiveGrammarKWSetContext ctx) {

  }

  @Override
  public void exitNglrGrammarDirectiveGrammarKWSet(
      NglrGrammarDirectiveGrammarKWSetContext ctx) {
    ArrayList<String> list2 = new ArrayList<String>();
    for (NglrGrammarDirectiveGrammarKWSetValuesContext kwSetCtx : ctx.valLit)
      list2.add(kwSetCtx.ret);
    grammarDirectives.put(GrammarDirectiveTypes.KWSET, list2);
  }

  @Override
  public void
      enterNglrGrammarDirectiveFilter(NglrGrammarDirectiveFilterContext ctx) {
  }

  @Override
  public void
      exitNglrGrammarDirectiveFilter(NglrGrammarDirectiveFilterContext ctx) {
    grammarDirectives.put(GrammarDirectiveTypes.FILTER, Boolean.TRUE);
  }

  @Override
  public void enterNglrGrammarDirectiveNoInterpretation(
      NglrGrammarDirectiveNoInterpretationContext ctx) {

  }

  @Override
  public void exitNglrGrammarDirectiveNoInterpretation(
      NglrGrammarDirectiveNoInterpretationContext ctx) {
    grammarDirectives.put(GrammarDirectiveTypes.NO_INTERP, Boolean.TRUE);
  }

  @Override
  public void enterNglrGrammarDirective(NglrGrammarDirectiveContext ctx) {
  }

  @Override
  public void exitNglrGrammarDirective(NglrGrammarDirectiveContext ctx) {
  }

  @Override
  public void enterNglrGrammarDirectiveGrammarRoot(
      NglrGrammarDirectiveGrammarRootContext ctx) {
  }

  @Override
  public void exitNglrGrammarDirectiveGrammarRoot(
      NglrGrammarDirectiveGrammarRootContext ctx) {
    grammarDirectives.put(GrammarDirectiveTypes.ROOT, ctx.val.getText());
  }

  @Override
  public void
      enterNglrGrammarDirectiveInclude(NglrGrammarDirectiveIncludeContext ctx) {

  }

  @SuppressWarnings("unchecked")
  @Override
  public void
      exitNglrGrammarDirectiveInclude(NglrGrammarDirectiveIncludeContext ctx) {
    String incFile = convertTokenListToString(ctx.valLit);
    List<String> listInclude =
        (List<String>) grammarDirectives.get(GrammarDirectiveTypes.INCLUDE);
    if (null == listInclude) {
      listInclude = new ArrayList<String>();
      grammarDirectives.put(GrammarDirectiveTypes.INCLUDE, listInclude);
    }
    listInclude.add(incFile);
  }

  private String convertTokenListToString(List<Token> valLit) {
    String result = "";
    for (Token t : valLit)
      result += t.getText();
    return result;
  }

  @Override
  public void enterNglrGrammarDirectiveGrammarKWSetValues(
      NglrGrammarDirectiveGrammarKWSetValuesContext ctx) {
  }

  @Override
  public void exitNglrGrammarDirectiveGrammarKWSetValues(
      NglrGrammarDirectiveGrammarKWSetValuesContext ctx) {
  }

  @Override
  public void enterInterpDataEntry(InterpDataEntryContext ctx) {
  }

  @Override
  public void exitInterpDataEntry(InterpDataEntryContext ctx) {
    SymbolInterpData result = null;
    String factFull = convertTokenListToString(ctx.nameVal);

    String factName = SymbolInterpData.extracFactName(factFull);
    String fieldName = SymbolInterpData.extracFieldName(factFull);

    String paramValue = ctx.paramName != null ? ctx.paramName.getText() : "";
    // check
    if (!paramValue.isEmpty() && !Interpreter.PARAM_NO_NORM.equals(paramValue))
      throw new IllegalArgumentException(
          String.format("Unknown interpreter parameter '%s'. Valid only '%s'.",
              paramValue, Interpreter.PARAM_NO_NORM));

    if (ctx.factVal != null && ctx.factVal.size() > 0) {
      String fromFactFull = convertTokenListToString(ctx.nameVal);
      result = new SymbolInterpData(factName, fieldName,
          SymbolInterpData.extracFactName(fromFactFull),
          SymbolInterpData.extracFieldName(fromFactFull), paramValue);
    } else if (ctx.literalVal != null) {
      result = new SymbolInterpData(factName, fieldName,
          ctx.literalVal.getText(), paramValue);
    } else
      result = new SymbolInterpData(factName, fieldName, paramValue);

    currInterpDatas.add(result);
  }

  @Override
  public void enterInterpData(InterpDataContext ctx) {
    currInterpDatas = new LinkedList<SymbolInterpData>();
  }

  @Override
  public void exitInterpData(InterpDataContext ctx) {
  }

}
