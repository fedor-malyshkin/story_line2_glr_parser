package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ru.nlp_project.story_line2.glr_parser.NameFinderImpl.FIOEntry;
import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.IInOrderWalkProcessor;
import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.ILevelOrderWalkProcessor;
import ru.nlp_project.story_line2.glr_parser.ParseTreeValidator.NonTerminalToken;
import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.FIOKeywordToken;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.GrammarKeywordToken;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.PlainKeywordToken;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.GrammemeUtils.GrammemeEnum;
import ru.nlp_project.story_line2.morph.Grammemes;

/**
 * Преобразование дерева парсинга в текстовое представление.
 * 
 * <p>При сериализации учитываются Agreement'ы ({@link SymbolAgreement}) в которые 
 * входит главное слово, влияя этим на склонение входящих в них других слов. При
 * отсуствии вхождения - остальные слова остаются такими как ранее.</p>
 * 
 * <p>Кроме того естественно учитываются токены для plain-keywords.</p>
 * <p>При осуществелении сериализации дерева делаются определенные предположения о 
 * состоянии дерева разбора (реализованные на более раннних этапах анализа):</p>
 * <ol>
 * <li>У каждого узла (терминал/нетерминал) определно главное слово и его 
 * местоположение (выставвлены флаги hasRt и rtPos)</li>
 * <li>У каждого узла (терминал/нетерминал) остальось минимальное кол-во лексем, 
 * соотвествующее пометам ограничениям ранних этапаов анализа</li>
 * <li>У каждого узла (терминал/нетерминал) создан объект-токен (нетерминал) или 
 * имеется фактическим имевшийся (терминал)</li>
 * </ol>
 * 
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 *
 */
public class ParseTreeSerializer {
  public static ParseTreeSerializer newInstance(ITokenManager tokenManager) {
    ParseTreeSerializer result = new ParseTreeSerializer();
    result.tokenManager = tokenManager;
    return result;
  }

  private ITokenManager tokenManager;

  protected ParseTreeSerializer() {
  }

  private void assignAllLexemes(ParseTreeNode node) {
    if (node.isTerminal)
      assignAllLexemes(node.token);
    else
      node.children.forEach(c -> assignAllLexemes(c));
  }

  /**
   * Присвоить все лексемы токену. С учетом того, что они могут быть и составными.
   * В последующем останутся лишь подходящие (согласующиеся) формы лесем.
   * 
   * @param token
   */
  private void assignAllLexemes(Token token) {
    // если незнакомое слово -- нет образцов для применения
    if (token.getLexemesListCopy().size() == 0)
      return;
    // для идентификации замены - используем первую из сохранившихся лексем
    Lexeme lexeme = token.getLexemesListCopy().get(0);
    if (token.getClass() == PlainKeywordToken.class) {
      PlainKeywordToken pkt = (PlainKeywordToken) token;
      pkt.originalTokens.forEach(t -> assignAllLexemes(t));
    } else if (token.getClass() == GrammarKeywordToken.class) {
      GrammarKeywordToken gkt = (GrammarKeywordToken) token;
      gkt.originalTokens.forEach(t -> assignAllLexemes(t));
    } else if (token.getClass() == FIOKeywordToken.class) {
      // в отличии от остальных комбинированных токенов
      // в данном случае все возможные лексемы присваиваем непосредственно
      // самому токену, а не дочерним
      FIOKeywordToken fkt = (FIOKeywordToken) token;
      Lexeme first = fkt.getLexemesListCopy().peekFirst();
      tokenManager.assignAllLexemes(first.getId(), token);
    } else if (token.getClass() == Token.class
        || token.getClass() == NonTerminalToken.class) {
      tokenManager.assignAllLexemes(lexeme.getId(), token);
    } else
      throw new IllegalStateException(
          "Unknown token class: " + token.getClass());
  }

  /**
   * Присвоить текстовые слова главному слову (в слюбом случае) и 
   * членам Agreement'ов.
   * 
   * @param agreements
   * @param mainWordNode
   */
  private void assignTextValuesToAgreements(
      Collection<SymbolAgreement> agreements, ParseTreeNode mainWordNode) {
    if (null == mainWordNode)
      return;

    Grammemes m_gramm;
    if (mainWordNode.getLexemesListCopy().size() > 0) {
      Lexeme lexeme = mainWordNode.getLexemesListCopy().get(0);
      m_gramm = new Grammemes(lexeme.getGrammemes().clone());
      m_gramm.setCase(GrammemeEnum.nomn);
    } else {
      m_gramm = new Grammemes();
      GrammemeUtils.fillGrammemesByCSVMyTags("nomn, noun, sing", m_gramm);
    }

    final Grammemes grammemes = new Grammemes(m_gramm);
    // main word
    assignAllLexemes(mainWordNode.token);
    leaveMaxRelatedLexeme(null, mainWordNode.token, grammemes);
    // agreemnts
    // Получить у морфАнализера по идентификатору все граммемы епждой ноды
    // Получить максимально совпадающую граммему (по грамматическим признакам)
    // Найденной значение присвоить текстовой переменной
    agreements.forEach(agr -> agr.getNodes().forEach(n -> assignAllLexemes(n)));
    agreements.forEach(agr -> agr.getNodes()
        .forEach(n -> leaveMaxRelatedLexeme(agr, n, grammemes)));
  }

  /**
   * Собрать согласования дерева. При этом учитывается ранее определенный 
   * минимальный уровень ниже которого они (Agreements) не учитываются.
   * 
   * @param node
   * @param topLvl ранее определенный минимальный уровень 
   * @return 
   * @throws Exception
   */
  private Collection<SymbolAgreement> collectAgreements(ParseTreeNode node,
      int topLvl) throws Exception {
    Map<String, SymbolAgreement> agrMap = new HashMap<>();

    node.walkLevelOrder(new ILevelOrderWalkProcessor() {
      @Override
      public void nextLevel(int level) {
      }

      @Override
      public void processNode(ParseTreeNode node, int level) {
        // если не значение по умолчанию, но опустились уже ниже - ничего не
        // делать
        if (level > topLvl)
          return;

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
            SymbolAgreement agr = getAgreement(agrMap, entry, level);
            agr.addNode(node);
            break;
          default:
            // do nothing in any other case
            break;
          }
        }
      }
    });
    return agrMap.values();
  }

  private Collection<ParseTreeNode> collectTerminalNodes(ParseTreeNode node)
      throws Exception {
    List<ParseTreeNode> result = new LinkedList<>();
    node.walkInOrder(true, new IInOrderWalkProcessor() {
      @Override
      public void processNonTerminalNode(ParseTreeNode node) {
      }

      @Override
      public void processTerminalNode(ParseTreeNode node) {
        if (node.symbol != Symbol.EPSILON)
          result.add(node);
      }
    });
    return result;
  }

  private String createAgreementKey(
      Entry<SymbolExtDataTypes, SymbolExtData> entry, int level) {
    SymbolExtData value = entry.getValue();
    return "" + level + entry.getKey() + value.getArrayValue();
  }

  /**
   * Определить минимальный уровнеь на котором находятся согласования. 
   * 
   * @param node
   * @return
   * @throws Exception
   */
  private int detectAgreementsTopLevel(ParseTreeNode node) throws Exception {
    AtomicInteger topLvl = new AtomicInteger(Integer.MAX_VALUE);
    node.walkLevelOrder(new ILevelOrderWalkProcessor() {
      @Override
      public void nextLevel(int level) {
      }

      @Override
      public void processNode(ParseTreeNode node, int level) {
        // если не значение по умолчанию, но опустились
        // уже ниже - ничего не делать
        if (level > topLvl.get())
          return;

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
            topLvl.set(level);
            break;
          default:
            // do nothing in any other case
            break;
          }
        }
      }
    });
    return topLvl.get();
  }

  private String generateNodesPresentaion(Collection<ParseTreeNode> nodes) {
    return nodes.stream().map(n -> generateTokenPresentaion(n.token))
        .collect(Collectors.joining(" "));
  }

  private String generateTokenPresentaion(Token token) {
    if (token.getClass() == PlainKeywordToken.class) {
      PlainKeywordToken pkt = (PlainKeywordToken) token;
      return pkt.originalTokens.stream().map(t -> generateTokenPresentaion(t))
          .collect(Collectors.joining(" "));
    } else if (token.getClass() == GrammarKeywordToken.class) {
      GrammarKeywordToken gkt = (GrammarKeywordToken) token;
      return gkt.originalTokens.stream().map(t -> generateTokenPresentaion(t))
          .collect(Collectors.joining(" "));
    } else if (token.getClass() == FIOKeywordToken.class) {
      FIOKeywordToken fkt = (FIOKeywordToken) token;
      // т.к. присваивали непосредственно токену и с него же собирали
      // неподходящие...
      Optional<Lexeme> first = fkt.getLexemesListCopy().stream().findFirst();
      if (first.isPresent()) {
        Lexeme lexeme = first.get();
        Optional<FIOEntry> first2 =
            fkt.getFIOKeywordEntrance().getFIOs().stream()
                .filter(fio -> fio.getGrammemes().has(
                    lexeme.getGrammemes().getByIndex(GrammemeUtils.GNDR_NDX)))
                .findFirst();
        if (first2.isPresent())
          return first2.get().serialize(lexeme.getGrammemes());
      }
      return fkt.getValue();
    } else if (token.getClass() == Token.class) {
      return token.getValue();
    } else
      throw new IllegalStateException(
          "Unknown token class: " + token.getClass());

  }

  private SymbolAgreement getAgreement(Map<String, SymbolAgreement> map,
      Entry<SymbolExtDataTypes, SymbolExtData> entry, int level) {
    String key = createAgreementKey(entry, level);
    SymbolAgreement agr = map.get(key);
    if (agr == null) {
      agr = new SymbolAgreement(entry.getKey());
      map.put(key, agr);
    }
    return agr;
  }

  private void leaveMaxRelatedLexeme(SymbolAgreement agr, ParseTreeNode node,
      Grammemes grammemes) {
    if (node.isTerminal)
      leaveMaxRelatedLexeme(agr, node.token, grammemes);
    else
      node.children.forEach(c -> leaveMaxRelatedLexeme(agr, c, grammemes));
  }

  /**
   * Оставить лексемы с максимальным совпадением.
   * <p></p>
   * Алгоритм:
   * <ol>
   * <li>Найти с максимальным совпадением</li>
   * <li>Очистить список лексем</li>
   * <li>Вернуть лексему с максимальным совпадением</li>
   * </ol>
   * @param agr 
   * 
   * @param token
   * @param grammemes
   */
  private void leaveMaxRelatedLexeme(SymbolAgreement agr, Token token,
      Grammemes grammemes) {
    if (token.getClass() == PlainKeywordToken.class) {
      PlainKeywordToken pkt = (PlainKeywordToken) token;
      pkt.originalTokens.forEach(t -> leaveMaxRelatedLexeme(agr, t, grammemes));
    } else if (token.getClass() == GrammarKeywordToken.class) {
      GrammarKeywordToken gkt = (GrammarKeywordToken) token;
      gkt.originalTokens.forEach(t -> leaveMaxRelatedLexeme(agr, t, grammemes));
    } else if (token.getClass() == Token.class
        || token.getClass() == NonTerminalToken.class
        || token.getClass() == FIOKeywordToken.class) {
      int prevMatch = Integer.MIN_VALUE;
      Lexeme maxMatch = null;
      Iterator<Lexeme> iter = token.getLexemesIterator();
      while (iter.hasNext()) {
        Lexeme end = iter.next();
        int matchScore = SymbolRestrictionChecker.matchScore(end.getGrammemes(),
            grammemes, agr != null ? agr.getAgrType() : null);
        if (matchScore > prevMatch) {
          maxMatch = end;
          prevMatch = matchScore;
        }
      }
      token.removeLexemes(true, true);
      token.addLexeme(maxMatch);
      // если нашли - что-то более подходящее, иначе оставляем что есть
      if (maxMatch != null)
        token.value = maxMatch.getValue();
    } else
      throw new IllegalStateException(
          "Unknown token class: " + token.getClass());

  }

  private void removeNotContainingAgreements(
      Collection<SymbolAgreement> agreements, ParseTreeNode mainWordNode) {
    Iterator<SymbolAgreement> iter = agreements.iterator();
    while (iter.hasNext()) {
      SymbolAgreement agreement = iter.next();
      if (!agreement.getNodes().contains(mainWordNode))
        iter.remove();
    }
  }

  /**
   * 
   * Выполнить сериализацию дерева.
   * 
   *  В данном процессе используется граммема/главный дочерний узел (с пометой 
   *  "rt") корневого узла (ранее было определено в процессе валидации 
   *  дерева в {@link ParseTreeValidator}).
   *  
   *  Алгоритм вкратце такой^
   *  <ol>
   *  <li>Пройти по дереву level-order с отсечкой по уровню и собрать все 
   *  используемые agreement(с набрами (set) узлов в agreement)
   *  (при сборке agreement'ов ограничиться лишь одним уровнем, самым первым 
   *  на котором они встретились) (при этом важно не смешивать Agreement'ы 
   *  разных ветвей и сохранять в них главное слово их местного уровня)</li>
   *  <li>Определить главный узел</li>
   *  <li>Определить agreement'ы в которые он входит - в treeTokensSerForm их 
   *  сеарилизовать с учетом их типа (при этом анализируем возиожность 
   *  существования в виде токена {@link PlainKeywordToken} и 
   *  {@link GrammarKeywordToken})</li>
   *  <li>Сериализовать оставшиеся токены 
   *  (при этом анализируем возиожность существования в виде токена 
   *  {@link PlainKeywordToken} и {@link GrammarKeywordToken})</li>
   *  </ol>
   * 
   * @param node
   * @return
   * @throws Exception 
   */
  public String serialize(ParseTreeNode node, boolean normalize) {
    try {
      // определеить минимальный уровень для сбора agreemnt'ов
      int topLvl = detectAgreementsTopLevel(node);
      // собрать agreemnt'ы
      Collection<SymbolAgreement> agreements = null;
      if (topLvl != Integer.MAX_VALUE)
        agreements = collectAgreements(node, topLvl);
      else
        agreements = Collections.emptyList();
      // получить главное слово дерева
      ParseTreeNode mainWordNode = node.getMainWordNode(true);
      // удалить agreemnt'ы не содержащие главного слова
      removeNotContainingAgreements(agreements, mainWordNode);
      // собрать все терминальные узлы ()
      Collection<ParseTreeNode> nodes = collectTerminalNodes(node);
      if (normalize)
        // сформировать текстовые представления в узлах с учетом
        // нормализации главного слова
        assignTextValuesToAgreements(agreements, mainWordNode);
      // сформировать остаточное представление
      return generateNodesPresentaion(nodes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

}
