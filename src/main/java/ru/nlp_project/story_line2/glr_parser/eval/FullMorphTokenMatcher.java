package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;

import ru.nlp_project.story_line2.glr_parser.IHierarchyManager;
import ru.nlp_project.story_line2.glr_parser.SymbolExt;
import ru.nlp_project.story_line2.glr_parser.SymbolRestrictionChecker;
import ru.nlp_project.story_line2.glr_parser.SymbolTable;
import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;
import ru.nlp_project.story_line2.morph.GrammemeEnum;

public class FullMorphTokenMatcher implements IGLRTokenMatcher {
  public static final Map<String, Symbol> keywordsSymbolMap =
      new HashMap<String, Symbol>();
  private static Map<GrammemeEnum, Symbol> grammemesMap =
      new HashMap<GrammemeEnum, Symbol>();

  static {
    // collect keywords
    SymbolTable.getKeywords().forEach(
        s -> keywordsSymbolMap.put(s, new Symbol(s, SymbolTypes.Terminal)));

    // collect grammemes
    EnumUtils.getEnumList(GrammemeEnum.class).stream().forEach(g -> grammemesMap
        .put(g, new Symbol(g.toString(), SymbolTypes.Terminal)));

  }

  private Map<Symbol, List<Symbol>> baseNTToNTExtDataMap;
  private Map<Symbol, List<ActionRecord>> actionTableEntry;
  private Token currentToken;
  private SymbolRestrictionChecker checker = null;

  public FullMorphTokenMatcher(IHierarchyManager hierarchyManager) {
    checker = new SymbolRestrictionChecker(hierarchyManager);
  }

  /**
   * 
   * Получить действия по значению символа (с учетом наличия расширенных сиволов)
   * в таблице переходов.
   * 
   * @param symbol
   * @return
   */
  private void addActionRecordsBySymbolExt(SymbolExt symbol,
      Set<ActionRecord> list) {
    List<ActionRecord> actions = actionTableEntry.get(symbol);
    if (actions != null)
      list.addAll(actions);
  }

  private void addToListMatchingKeywordActionRecords(Set<ActionRecord> list,
      String symbolName) {
    Symbol symbol = keywordsSymbolMap.get(symbolName);
    List<Symbol> symbols = baseNTToNTExtDataMap.get(symbol);
    symbols.forEach(s -> {
      if (checker.match(s, this.currentToken, null))
        addActionRecordsBySymbolExt((SymbolExt) s, list);
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ru.nlp_project.story_line2.glr_parser.ITokenMatcher#getActionTableRecords(
   * java.util.Map, ru.nlp_project.story_line2.glr_parser.Token)
   */
  @Override
  public Collection<ActionRecord> getActionTableRecords(
      Map<Symbol, List<ActionRecord>> actionTableEntry,
      Map<Symbol, List<Symbol>> baseNTToNTExtDataMap, Token token) {
    // initialize memebers
    this.baseNTToNTExtDataMap = baseNTToNTExtDataMap;
    this.actionTableEntry = actionTableEntry;
    this.currentToken = token;

    Set<ActionRecord> result = new HashSet<ActionRecord>();

    if (currentToken.getType() != TokenTypes.EOI)
      result.addAll(processKeywords());

    // grammemes
    switch (token.getType()) {
    case COMBINED_GRAMMAR :
      // подобное применение возможно (а вот literalString к дереву маловерятно применить)
      result.addAll(processToken(token));
      break;
    case COMBINED_PLAIN:
    case COMBINED_FIO:
    case WORD:
      // надо учитывать, что в случае COMBINED_PLAIN и COMBINED_FIO 
      // (и друших возможных COMBINED_XXXX) - возможны странные поведения при
      // объединении нескольких слов: не будет искаться 'Петров_Иван' для 'Петров'  
      result.addAll(processLiteralString(token));
      result.addAll(processToken(token));
      break;
    case EOI: {
      List<ActionRecord> acts = actionTableEntry.get(Symbol.EOI);
      if (acts != null)
        result.addAll(acts);
    }
      break;
    default:
      break;
    }
    // usefull watch expression: actionTableEntry + " - " + token .getValue()+"
    // - " + result
    return result;
  }

  private boolean hasActionRecordsKeywordSymbol(String symbol) {
    Symbol symbolObj = keywordsSymbolMap.get(symbol);
    List<Symbol> extSymbols = baseNTToNTExtDataMap.get(symbolObj);
    if (extSymbols.size() > 0)
      return true;
    return false;
  }

  private boolean matchKeywordAnyWord() {
    return this.currentToken.getType() != TokenTypes.EOI;
  }

  private boolean matchKeywordColon() {
    return currentToken.kwColon;
  }

  private boolean matchKeywordComma() {
    return currentToken.kwComma;
  }

  private boolean matchKeywordDollar() {
    return currentToken.kwDollar;
  }

  private boolean matchKeywordHyphen() {
    return currentToken.kwHyphen;
  }

  private boolean matchKeywordLBracket() {
    return currentToken.kwLBracket;
  }

  private boolean matchKeywordPercent() {
    return currentToken.kwPercent;
  }

  private boolean matchKeywordPlusSign() {
    return currentToken.kwPlusSign;
  }

  private boolean matchKeywordPunct() {
    return currentToken.kwPunct;
  }

  private boolean matchKeywordQuoteDbl() {
    return currentToken.kwQuoteDbl;
  }

  private boolean matchKeywordQuoteSng() {
    return currentToken.kwQuoteSng;
  }

  private boolean matchKeywordRBracket() {
    return currentToken.kwRBracket;
  }

  private boolean matchKeywordUnknownPOS() {
    return currentToken.getLexemesListCopy().size() == 0;
  }

  private boolean matchKeywordWord() {
    return currentToken.kwWord;
  }

  private Collection<ActionRecord> processKeywords() {
    Set<ActionRecord> result = new HashSet<ActionRecord>();

    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_WORD))
      if (matchKeywordWord())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_WORD);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_UNKN_POS))
      if (matchKeywordUnknownPOS())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_UNKN_POS);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_QUOTE_DBL))
      if (matchKeywordQuoteDbl())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_QUOTE_DBL);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_QUOTE_SNG))
      if (matchKeywordQuoteSng())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_QUOTE_SNG);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_LBRACKET))
      if (matchKeywordLBracket())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_LBRACKET);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_RBRACKET))
      if (matchKeywordRBracket())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_RBRACKET);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_HYPHEN))
      if (matchKeywordHyphen())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_HYPHEN);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_PUNCT))
      if (matchKeywordPunct())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_PUNCT);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_COMMA))
      if (matchKeywordComma())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_COMMA);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_COLON))
      if (matchKeywordColon())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_COLON);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_PERCENT))
      if (matchKeywordPercent())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_PERCENT);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_DOLLAR))
      if (matchKeywordDollar())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_DOLLAR);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_PLUS_SIGN))
      if (matchKeywordPlusSign())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_PLUS_SIGN);
    if (hasActionRecordsKeywordSymbol(SymbolTable.KW_ANY_WORD)) {
      if (matchKeywordAnyWord())
        addToListMatchingKeywordActionRecords(result, SymbolTable.KW_ANY_WORD);
    }

    return result;
  }

  private Collection<ActionRecord> processLiteralString(Token token) {
    Set<ActionRecord> result = new HashSet<ActionRecord>();
    for (Lexeme lexeme : token.getLexemesListCopy()) {
      String lemm = lexeme.getLemm();
      Symbol symbol = new Symbol(lemm, SymbolTypes.LiteralString);
      List<Symbol> extSymbols = baseNTToNTExtDataMap.get(symbol);
      extSymbols.forEach(s -> {
        if (checker.match(s, token, null))
          result.addAll(actionTableEntry.get(s));
      });
    }
    return result;
  }

  /**
   * Добавить действия, соотвествующие токену.
   * Т.к. в данном коде производится определения соотвествия для терминалов 
   * после отработки других ключевых слов возможна обработка только частей речи 
   * (свертка нетерминалов в данном коде не осуществляется (см. {@link SymbolRestrictionChecker})).
   * 
   *  Шаги следующие:
   *  <ol>
   *  <li>Проходимся лексемам и определяем их части речи</li>
   *  <li>по определенным частям речи ищем соотвествующие {@link SymbolExt} в {@link #baseNTToNTExtDataMap}</li>
   *  <li>по найденным {@link SymbolExt} определяем их допустимость использования через {@link SymbolRestrictionChecker}</li>
   *  <li>в случае допустимости - добавляем в результат</li>
   *  </ol>
   */
  private Collection<ActionRecord> processToken(Token token) {
    Set<ActionRecord> result = new HashSet<ActionRecord>();
    for (Lexeme lexem : token.getLexemesListCopy()) {
      GrammemeEnum pos = lexem.getPOS();
      Symbol symbol = grammemesMap.get(pos);
      List<Symbol> extSymbols = baseNTToNTExtDataMap.get(symbol);

      extSymbols.forEach(s -> {
        if (checker.match(s, token, null))
          result.addAll(actionTableEntry.get(s));
      });
    }

    return result;
  }

}
