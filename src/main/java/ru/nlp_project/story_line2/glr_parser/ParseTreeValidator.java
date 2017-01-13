package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.ILevelOrderWalkProcessor;
import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.IPostOrderWalkLeafFirstProcessor;
import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.GrammarKeywordToken;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.morph.Grammemes;

/**
 * Проверить разобранное дерево на предмет выполнения ограничений (не)терминалов, определяет главное слово дерева (и каждого узла), 
 * вычисляет итоговые граммемы (в зависимости от главного слова) каждого (не)терминала (очень важная функция, используемамя многократно 
 * в дальнейшем по коду) (итоговые граммемы хранятся в {@link ParseTreeNode#grammemes}).
 * 
 * При выполнении кода осуществляются следующие задачи:
 * <ol>
 * <li>Вычисляет главное слово {@link ParseTreeNode#rtPos} и {@link ParseTreeNode#hasRt}</li>
 * <li>Продвигаясь вверх удаляет несоотвествующие лексеммы - при отсуствии лексем у токена - выбрасывает исключение</li>
 * <li>Копирует вверх граммемы главного слова (для нетерминалов хранится в {@link ParseTreeNode#grammemes}) - 
 * потом должно использоваться при создании псевдо-токена ({@link GrammarKeywordToken})</li>
 * </ol>
 * 
 * В случае несоблюдения ограничения - выбрасываем исключение.
 *
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 *
 */
public class ParseTreeValidator {

  public static class NonTerminalToken extends Token {
    /*
     * public NonTerminalToken() { super(-1, -1, "", TokenTypes.WORD); }
     */
    private NonTerminalToken(int from, int length, String value,
        TokenTypes type) {
      super(from, length, value, type);
    }

    public void addAllLexemes(Collection<Lexeme> ls) {
      lexemes.addAll(ls);
    }

    public void addLexeme(Lexeme l) {
      lexemes.add(l);
    }

    @Override
    public Token clone() {
      NonTerminalToken result =
          new NonTerminalToken(this.from, this.length, this.value, this.type);
      cloneAttributes(result);
      return result;
    }

  }

  public class ParseTreeValidationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String article;
    private Symbol checkedSymbol;
    private SymbolExtData checkedSymbolExtData;
    private boolean isTerminal;
    private String message;
    private int prjPos;
    private Token checkedSymbolToken;

    public ParseTreeValidationException(String article, int prjPos,
        Symbol checkedSymbol, SymbolExtData checkedSymbolExtData,
        Token checkedSymbolToken, boolean isTerminal, String message) {
      this.article = article;
      this.prjPos = prjPos;
      this.checkedSymbol = checkedSymbol;
      this.checkedSymbolExtData = checkedSymbolExtData;
      this.checkedSymbolToken = checkedSymbolToken;
      this.message = message;
      this.isTerminal = isTerminal;

    }

    @Override
    public String getMessage() {
      if (isTerminal) {
        return String.format(
            "Terminal %s in article '%s' (grammar line: %d) not match '%s' restrictions (token: %s). Details: %s",
            checkedSymbol, article, prjPos, checkedSymbolExtData,
            checkedSymbolToken, message);
      } else {
        return String.format(
            "Terminal %s in article '%s' (grammar line: %d) not match '%s' restrictions. Details: %s",
            checkedSymbol, article, prjPos, checkedSymbolExtData, message);
      }
    }

  }

  private SentenceProcessingContext context;
  private SymbolRestrictionChecker checker;
  private Map<String, SymbolAgreement> agrMap;
  private List<SymbolAgreement> agrList;
  private IHierarchyManager hierarchyManager;

  public ParseTreeValidator(IHierarchyManager hierarchyManager) {
    this.hierarchyManager = hierarchyManager;
  }

  /**
   * Выполнить проверку согласований. Фактически в списке все Agreement'ы
   * находятся в обратном порядке, т.е. надо начинать анализировать снизу-вверх.
   * @throws ParseTreeValidationException 
   */
  private void checkLevelAgreements() throws ParseTreeValidationException {
    Collections.reverse(agrList);
    ListIterator<SymbolAgreement> listIterator = agrList.listIterator();
    while (listIterator.hasNext()) {
      SymbolAgreement agreement = listIterator.next();
      // если меньше 2-х узов не обрабатываем данное согласование
      if (agreement.getNodes().size() < 2)
        continue;
      removeUnmatchedLexems(agreement);
      // повторный вызов с целью приведения совпадающих лексем к единому
      // общему минимуму - возможно именно тут определится невалидность
      // согласования
      removeUnmatchedLexems(agreement);
      checkMatchLexems(agreement);
    }
  }

  /**
   * Проверить наличие подходящих лексем.
   * При отсуствии - выбросить исключение.
   * 
   * @param agreement
   */
  private void checkMatchLexems(SymbolAgreement agreement)
      throws ParseTreeValidationException {
    List<ParseTreeNode> nodes = agreement.getNodes();
    Iterator<ParseTreeNode> iterator = nodes.iterator();
    while (iterator.hasNext()) {
      ParseTreeNode node = iterator.next();
      if (!node.hasLexemes())
        throw new ParseTreeValidationException(context.getArticle(),
            node.prjPos, node.symbol, null, node.token, node.isTerminal,
            "node not match restriction of agreemen: "
                + agreement.getAgrType());
    }

  }

  protected void checkNodeRestrictions(ParseTreeNode node)
      throws ParseTreeValidationException {
    // проверка на epsilon - узел
    if (node.symbol == Symbol.EPSILON)
      return;
    // на первоначальном этапе валидации (после создания дерева из SPPF) у
    // нетерминалов нет токенов
    if (hasToken(node)) {
      // в терминальном узле не может быть дочерних узлов
      if (node.children.size() != 0)
        throw new IllegalStateException(
            "В терминальном узле не может быть дочерних узлов.");

      node.isTerminal = true;
      // set rt position
      node.rtPos = 0;
      checkSymbolPOSForTerminal(node);
      checkSymbolRestrictions(node.isTerminal, node.token, node);
    } else {
      if (node.children.size() == 0)
        throw new IllegalStateException();
      node.isTerminal = false;
      node.rtPos = detectRtPosForNonTerminal(node);

      ParseTreeNode rtChild = node.children.get(node.rtPos);
      // проверка на epsilon - узел
      if (rtChild.symbol == Symbol.EPSILON)
        return;
      ParseTreeNode fCh = node.children.getFirst();
      ParseTreeNode lCh = node.children.getLast();
      // create pseudo-token
      NonTerminalToken nonTerminalToken =
          new NonTerminalToken(fCh.from, lCh.from - fCh.from + lCh.length,
              rtChild.token.value, TokenTypes.WORD);
      rtChild.token.cloneAttributes(nonTerminalToken);
      node.token = nonTerminalToken;

      // copy grammemes from 'rt'-child
      nonTerminalToken.addAllLexemes(rtChild.getLexemesListCopy());
      checkSymbolRestrictions(node.isTerminal, node.token, node);
    }
  }

  /**
   * Выполнить проверку совпадения частей речи для терминалов, являющихся названиями частей речи
   * (и удалить несоотвествуюшие).
   * 
   * @param node
   * @throws ParseTreeValidationException
   */
  private void checkSymbolPOSForTerminal(ParseTreeNode node)
      throws ParseTreeValidationException {
    Symbol symbol = node.symbol;
    // если при парсинге не присвоена граммема - скорее всего не такой символ
    if (symbol.getGrammeme() == null)
      return;
    if (!node.isTerminal)
      return;
    Iterator<Lexeme> lexemIter = node.getLexemesIterator();
    while (lexemIter.hasNext()) {
      Lexeme lexem = lexemIter.next();

      if (!lexem.getGrammemes().matchPOS(symbol.getGrammeme()))
        lexemIter.remove();
    }
    if (!node.hasLexemes())
      throw new ParseTreeValidationException(context.getArticle(), node.prjPos,
          node.symbol, null, node.token, node.isTerminal,
          "node not match POS - " + symbol.getGrammeme().toString());
  }

  /**
   * Выполнить проверку символа на удовлетворение условиям ограничений.
   * 
   * Основные условия работы в этом методе и всех вызываемых далее:
   * <ul>
   * <li>Выполнять проверку для каждого ограничеия и по возможности выполнять лишь вызов метода (в котором выполнять все проверки и изменения);</li>
   * <li>Выполнять проверку {@link Token#lexemes} и данных {@link SymbolExt#getExtDatas()}</li>
   * <li>Не менять значения граммем ({@link Grammemes}) - т.к. фактически это один и тот же объект во всех деревьях - 
   * они предназначены лишь для чтения</li>
   * <li>При отсуствии данных удовлетворяющих условиям - выбрасывать {@link ParseTreeValidationException}</li>
   * <li>По мере проверки удалять неподходящие граммемы, минимизируя их число и тем самым достигая деомонимизации 
   * (для каждого дерева создается своя копия токенов - допустимо их измениение 
   * (см. {@link ParseTreeNode#ParseTreeNode(ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode, ParseTreeNode)}))</li>
   * </ul>
   * @param token 
   * @param isTerminal 
   * 
   * @param node
   * @throws ParseTreeValidationException
   */

  private void checkSymbolRestrictions(boolean isTerminal, Token token,
      ParseTreeNode node) throws ParseTreeValidationException {
    Symbol symbol = node.symbol;
    // простой не расширенный символ совпадает в данном случае сразу
    if (symbol.getClass() == Symbol.class)
      return;
    // при наличии расширенных данных - все нужно проверить
    SymbolExt symbolExt = (SymbolExt) symbol;
    Set<Entry<SymbolExtDataTypes, SymbolExtData>> entrySet =
        symbolExt.getExtDatasMap().entrySet();

    Token t = isTerminal ? token : null;
    ParseTreeNode n = isTerminal ? null : node;
    for (Entry<SymbolExtDataTypes, SymbolExtData> entry : entrySet) {

      if (!checker.match(entry.getKey(), entry.getValue(), symbolExt, t, n))
        throwNotMatchRestriction(node, entry);

      switch (entry.getKey()) {
      case rt:
        node.hasRt = true;
        break;
      case gram:
        checker.removeUnmatchingLexemesGram(entry.getValue().getGrammValue(),
            symbolExt, node.token);
        if (node.token.lexemes.size() == 0)
          throw new ParseTreeValidationException(context.getArticle(),
              node.prjPos, node.symbol, entry.getValue(), node.token,
              node.isTerminal,
              "node not match 'gram' restriction: " + entry.getValue());
        break;
      case gram_ex:
        checker.removeUnmatchingLexemesGramEx(entry.getValue().getGrammValue(),
            symbolExt, node.token);
        if (node.token.lexemes.size() == 0)
          throw new ParseTreeValidationException(context.getArticle(),
              node.prjPos, node.symbol, entry.getValue(), node.token,
              node.isTerminal,
              "node not match 'gram-ex' restriction: " + entry.getValue());
        break;
      case gu:
        if (!checker.matchGU(entry.getValue().getGuValue(), symbolExt,
            node.token))
          throwNotMatchRestriction(node, entry);
        checker.removeUnmatchingLexemesGU(entry.getValue().getGuValue(),
            symbolExt, node.token);
        if (node.token.lexemes.size() == 0)
          throw new ParseTreeValidationException(context.getArticle(),
              node.prjPos, node.symbol, entry.getValue(), node.token,
              node.isTerminal,
              "node not match 'gu' restriction: " + entry.getValue());
        break;
      default: /* do nothing */
        break;
      }
    }

  }

  private String
      createAgreementKey(Entry<SymbolExtDataTypes, SymbolExtData> entry) {
    SymbolExtData value = entry.getValue();
    return entry.getKey() + value.getArrayValue();
  }

  /**
   * Detect rt-aviability on children (was set on previous steps). 0 - is default value.
   * 
   * @param node
   * @return
   */
  private int detectRtPosForNonTerminal(ParseTreeNode node) {
    int i = 0;
    for (ParseTreeNode child : node.children) {
      if (child.hasRt)
        return i;
      i++;
    }
    return 0;
  }

  /**
   * Закончить создание Agreemnt'ов на этом уровне дерева.
   */
  protected void finishLevelAgreementProcessing() {
    agrMap.clear();
  }

  protected SymbolAgreement
      getAgreement(Entry<SymbolExtDataTypes, SymbolExtData> entry) {
    String key = createAgreementKey(entry);
    SymbolAgreement agr = agrMap.get(key);
    if (agr == null) {
      agr = new SymbolAgreement(entry.getKey());
      agrMap.put(key, agr);
      agrList.add(agr);
    }
    return agr;
  }

  protected boolean hasToken(ParseTreeNode node) {
    return node.token != null;
  }

  /**
   * Выполнить сбор Agreement'ов с узла.
   * 
   * @param node 
   * 
   */
  protected void processLevelAgreements(ParseTreeNode node) {
    Symbol symbol = node.symbol;
    if (symbol.getClass() == Symbol.class)
      return;
    // при наличии расширенных данных - все нужно проверить
    SymbolExt symbolExt = (SymbolExt) symbol;
    if (symbolExt.getExtDatas().size() == 0)
      return;
    Set<Entry<SymbolExtDataTypes, SymbolExtData>> entrySet =
        symbolExt.getExtDatasMap().entrySet();
    for (Entry<SymbolExtDataTypes, SymbolExtData> entry : entrySet) {
      switch (entry.getKey()) {
      case gnc_agr:
      case nc_agr:
      case c_agr:
      case gn_agr:
      case gc_agr:
      case fem_c_agr:
      case after_num_agr:
      case sp_agr:
      case fio_agr:
        SymbolAgreement agr = getAgreement(entry);
        agr.addNode(node);
        break;
      default:
        if (entry.getKey().toString().endsWith("_agr"))
          throw new IllegalStateException(
              "Unknown agreement type: " + entry.getKey());
        // do nothing in any other case
        break;
      }
    } // for

  }

  /**
   * УДалить неподходящие лексемы.
   *
   * Т.к. все участники Agreement должны согласовываться между собой взял 
   * в качестве решения такую идею: все должны согласовываться с одним общим 
   * членом - если с одним из его элементов (лексемой) не согласовываются все 
   * элементы дугого токена - он удаляется и так со всеми токенами группы.
   * 
   * В итоге должны остаться только лексемы, которые согласуются со всеми 
   * токенами группы. Или ни одного - тогда группа не валидна.
   *  
   * Алгоритм определения пересечений использовать такой (для кол-ва участников 
   * 2 и более): формировать 2 цикла (внешний и внутренний): еси после 
   * завершения внутреннего цикла не было ни одного пересечения - удаляем 
   * запись из внешенего цикла, потом меняем во внутреннем цикле кандидата 
   * для проверки и снова в случае отсуствия удаляем из внешнего. В случае 
   * если во внешнем кто-то остался - согласуется. 
   * Чистку можно использовать повторно выполняя циклы, но помещая прежний 
   * внешний внутрь и повторяя процедуру (при это меняя только во внешнем).
   * 
   * @param agreement
   */
  private void removeUnmatchedLexems(SymbolAgreement agreement) {
    List<ParseTreeNode> nodes = agreement.getNodes();
    Iterator<ParseTreeNode> nodeIter = nodes.iterator();
    // в качестве внешнего зафиксирогвали первый
    ParseTreeNode outer = nodeIter.next();

    while (nodeIter.hasNext()) {
      ParseTreeNode inner = nodeIter.next();
      Iterator<Lexeme> outLxIter = outer.getLexemesIterator();
      Iterator<Lexeme> inLxIter = inner.getLexemesIterator();
      while (outLxIter.hasNext()) {
        Lexeme outLx = outLxIter.next();
        boolean outerAnyMatch = false;
        boolean tempMatch = false;
        while (inLxIter.hasNext()) {
          Lexeme inLx = inLxIter.next();
          switch (agreement.getAgrType()) {
          case gnc_agr:
            tempMatch = outLx.getGrammemes().isGNCAgree(inLx.getGrammemes());
            break;
          case nc_agr:
            tempMatch = outLx.getGrammemes().isNCAgree(inLx.getGrammemes());
            break;
          case c_agr:
            tempMatch = outLx.getGrammemes().isCAgree(inLx.getGrammemes());
            break;
          case gn_agr:
            tempMatch = outLx.getGrammemes().isGNAgree(inLx.getGrammemes());
            break;
          case gc_agr:
            tempMatch = outLx.getGrammemes().isGCAgree(inLx.getGrammemes());
            break;
          case fem_c_agr:
          case after_num_agr:
          case sp_agr:
          case fio_agr:
          case geo_agr:
          default:
            throw new IllegalStateException(
                "Unknown agreement type: " + agreement.getAgrType());
          }
          if (tempMatch)
            outerAnyMatch |= true;
          else
            inLxIter.remove();

        } // while inner
        if (!inner.hasLexemes())
          new ParseTreeValidationException(context.getArticle(), inner.prjPos,
              inner.symbol, null, inner.token, inner.isTerminal,
              "node not match restriction of agreement: "
                  + agreement.getAgrType());
        // удалаяем из внешнего при отсуствии хотя бы какого либо совпаделия
        if (!outerAnyMatch)
          outLxIter.remove();
      } // while outer
      if (!outer.hasLexemes())
        new ParseTreeValidationException(context.getArticle(), outer.prjPos,
            outer.symbol, null, outer.token, outer.isTerminal,
            "node not match restriction of agreement: "
                + agreement.getAgrType());
    }
  }

  protected void throwNotMatchRestriction(ParseTreeNode node,
      Entry<SymbolExtDataTypes, SymbolExtData> entry)
          throws ParseTreeValidationException {
    throw new ParseTreeValidationException(context.getArticle(), node.prjPos,
        node.symbol, entry.getValue(), node.token, node.isTerminal,
        "node not match restriction: " + entry.getValue());
  }

  public void validateTree(SentenceProcessingContext context,
      ParseTreeNode root) throws Exception {
    this.context = context;
    //
    this.checker = new SymbolRestrictionChecker(hierarchyManager);
    // check terminal/non-terminal restriction
    root.walkPostOrderLeafFirst(new IPostOrderWalkLeafFirstProcessor() {
      @Override
      public void processNode(ParseTreeNode node)
          throws ParseTreeValidationException {
        checkNodeRestrictions(node);
      }
    });

    agrMap = new HashMap<String, SymbolAgreement>();
    agrList = new LinkedList<>();

    root.walkLevelOrder(new ILevelOrderWalkProcessor() {

      @Override
      public void nextLevel(int level) {
        finishLevelAgreementProcessing();
      }

      @Override
      public void processNode(ParseTreeNode node, int level) {
        processLevelAgreements(node);
      }

    });
    checkLevelAgreements();
  }

}
