package ru.nlp_project.story_line2.glr_parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.io.IOUtils;

import ru.nlp_project.story_line2.glr_parser.ParseTreeValidator.ParseTreeValidationException;
import ru.nlp_project.story_line2.glr_parser.eval.ActionRecord;
import ru.nlp_project.story_line2.glr_parser.eval.FullMorphTokenMatcher;
import ru.nlp_project.story_line2.glr_parser.eval.Grammar;
import ru.nlp_project.story_line2.glr_parser.eval.LR1ParseTableBuilder;
import ru.nlp_project.story_line2.glr_parser.eval.LR1Point;
import ru.nlp_project.story_line2.glr_parser.eval.Projection;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode;
import ru.nlp_project.story_line2.glr_parser.eval.SPPFDecomposer;
import ru.nlp_project.story_line2.glr_parser.eval.SPPFDecomposer.ISPPFTreeWalker;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManager;

/**
 * Основной класс для работы с грамматиками: парсинг, хранение дополнительной информаци по каждой грамматике и т.д...
 * 
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 *
 */
public class GrammarManager {
  public enum GrammarDirectiveTypes {
    FILTER, INCLUDE, KWSET, NO_INTERP, ROOT;
  }

  /**
   * 
   * @author fedor
   *
   */
  public class ParseTreeBuilder implements ISPPFTreeWalker {
    ParseTreeNode currNode;
    Grammar grammar;
    ParseTreeNode tree;
    List<ParseTreeBuilder> treeBuilders;

    public ParseTreeBuilder(Grammar grammar,
        List<ParseTreeBuilder> treeBuilders) {
      super();
      this.grammar = grammar;
      this.treeBuilders = treeBuilders;
    }

    public ParseTreeBuilder(Grammar grammar, ParseTreeNode tree,
        List<ParseTreeBuilder> treeBuilders) {
      super();
      this.grammar = grammar;
      this.tree = tree;
      this.treeBuilders = treeBuilders;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ru.nlp_project.story_line2.glr_parser.eval.SPPFDecomposer.ISPPFTreeWalker
     * #addChildren(java.util.List, int)
     */
    @Override
    public void addChildren(List<SPPFNode> children, int projectionPos) {
      currNode.prjPos = projectionPos;
      for (SPPFNode sppfNode : children)
        currNode.children.add(new ParseTreeNode(sppfNode, currNode));

      if (projectionPos == -1) // impossible value (value for epsilon nodes)
        return;
      // WARNING: IMPORTANT PLACE
      // replace all reduction symbols by symbols from projection body
      // with all his attributes (ext & interp datas)
      Projection projection = grammar.get(projectionPos);
      for (int i = 0; i < projection.getBody().size(); i++) {
        currNode.children.get(i).symbol = projection.getBody().get(i);
        currNode.children.get(i).prjPos = projectionPos;
      }

    }

    /**
     * Добавить клоны дочерних узлов
     * 
     * @param resultingTree
     * @param newParent
     * @param originalChild - original's tree children
     */
    private void addCloneChildren(ParseTreeBuilder resultingTree,
        ParseTreeNode newParent, ParseTreeNode originalChild) {
      // make clone of new child
      ParseTreeNode newChild = originalChild.clone();
      newChild.parent = newParent;
      newParent.children.add(newChild);

      // check pointer's for equalitiy in new tree
      if (originalChild.equals(this.currNode))
        resultingTree.currNode = newChild;

      // copy to resulting tree children from orignial tree
      for (ParseTreeNode child : originalChild.children)
        addCloneChildren(resultingTree, newChild, child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public ParseTreeBuilder clone() {
      // сделать клон корневого узла
      ParseTreeBuilder result = new ParseTreeBuilder(this.grammar,
          this.tree.clone(), this.treeBuilders);

      // set result's pointer
      if (this.tree.equals(this.currNode))
        result.currNode = result.tree;

      // склонировать дочерние узлы
      for (ParseTreeNode child : this.tree.children)
        addCloneChildren(result, result.tree, child);
      return result;
    }

    @Override
    public void finishNode() {
      currNode = currNode.parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ru.nlp_project.story_line2.glr_parser.eval.SPPFDecomposer.ISPPFTreeWalker
     * #fork(int)
     */
    @Override
    public List<? extends ISPPFTreeWalker> fork(int copyCount) {
      List<ParseTreeBuilder> result =
          new ArrayList<ParseTreeBuilder>(copyCount);
      for (int i = 0; i < copyCount; i++)
        result.add(this.clone());
      treeBuilders.addAll(result);
      return result;
    }

    public ParseTreeNode getTree() {
      return tree;
    }

    @Override
    public void processChildren(int childrenPos, SPPFNode node) {
      currNode = currNode.children.get(childrenPos);
    }

    @Override
    public void visitRootNode(SPPFNode node) {
      currNode = new ParseTreeNode(node, null);
      tree = currNode;
    }

  }

  private Map<String, Collection<String>> articleUsedGrammarKeywordsMap =
      new HashMap<String, Collection<String>>();

  private Map<String, Collection<String>> articleUsedPlainKeywordsMap =
      new HashMap<String, Collection<String>>();

  private ConfigurationReader configurationReader = null;

  private Factory<Map<GrammarDirectiveTypes, Object>> factoryMapSS =
      new Factory<Map<GrammarDirectiveTypes, Object>>() {
        public Map<GrammarDirectiveTypes, Object> create() {
          return new HashMap<GrammarDirectiveTypes, Object>();
        }
      };

  /**
   * Основное хранилище директив грамматик. Связь череp имя статьи. 
   */
  private Map<String, Map<GrammarDirectiveTypes, Object>> grammarDirectivesMap =
      LazyMap.lazyMap(
          new HashedMap<String, Map<GrammarDirectiveTypes, Object>>(),
          factoryMapSS);

  /**
   * Основное хранилище грамматик. Связь череp имя статьи. 
   */
  private Map<String, Grammar> grammarMap = new HashedMap<String, Grammar>();

  private Interpreter interpreter = null;

  private KeywordManager keywordManager;

  private TokenManager tokenManager;

  private HierarchyManager hierarchyManager;

  private GrammarManager() {
  }

  /**
   * Выполнить анализ грамматик для определения зависимых keyword'ов.
   *
   */
  @SuppressWarnings("unchecked")
  public void analyseUsedKewordSets() {
    // 1
    // for each known articles....
    for (Entry<String, Grammar> entry : grammarMap.entrySet()) {
      String article = entry.getKey();
      Set<String> plainKws = new HashSet<String>();
      Set<String> grammarKws = new HashSet<String>();

      // directives
      Map<GrammarDirectiveTypes, Object> map =
          grammarDirectivesMap.get(article);
      List<String> directives =
          (List<String>) map.get(GrammarDirectiveTypes.KWSET);
      if (directives != null)
        for (String directive : directives) {
          if (grammarMap.containsKey(directive))
            grammarKws.add(directive);
          else
            plainKws.add(directive);
        }
      // for each projection....
      for (Projection prj : entry.getValue().getProjections()) {
        // for each symbol....
        for (Symbol s : prj.getBody()) {
          if (!SymbolExt.class.isAssignableFrom(s.getClass()))
            continue;
          SymbolExt se = (SymbolExt) s;
          if (se.getExtDatas() == null)
            continue;
          // fore each ext data....
          for (SymbolExtData d : se.getExtDatas()) {
            if ("kwtype".equals(d.getParamName())) {
              if (grammarMap.containsKey(d.getParamValue()))
                grammarKws.add(d.getParamValue());
              else
                plainKws.add(d.getParamValue());
            }
            if ("kwset".equals(d.getParamName()))
              throw new IllegalStateException("NIY");
          }
        }
      }

      articleUsedGrammarKeywordsMap.put(entry.getKey(), grammarKws);
      articleUsedPlainKeywordsMap.put(entry.getKey(), plainKws);

    } // for (Entry<String, Grammar> entry : grammarMap.entrySet()) {

  }

  /**
   * Рассчитать покрытие узлами итогового текстового фрагмента (установить у терминальных узлов значения "from" и "length").
   * 
   * Рассчитать покрытие узлами итогового текстового фрагмента с учетом, что точные данные лишь у узлов с токенами (терминалами), 
   * как результат для нетерминалов приходится расчитывать покрытие на основании покрытия дочерних терминалов.
   */
  protected void calculateParseTreeNodeCoverages(int[] arrayFroms,
      ParseTreeNode parseTreeNode) {

    if (parseTreeNode.children.size() > 0) {
      int resultingFrom = Integer.MAX_VALUE;
      int resultingMaxFrom = Integer.MIN_VALUE;
      int resultingTokenFrom = Integer.MAX_VALUE;
      int resultingMaxFromLength = 0;
      int resultingTokenLength = 0;

      for (ParseTreeNode child : parseTreeNode.children) {
        // non-terminal
        if (child.token == null) {
          // recalculate values for symbols...
          calculateParseTreeNodeCoverages(arrayFroms, child);

          if (resultingFrom > child.from)
            resultingFrom = child.from;

          if (resultingTokenFrom > child.tokenFrom)
            resultingTokenFrom = child.tokenFrom;

          if (resultingMaxFrom < child.from) {
            resultingMaxFrom = child.from;
            resultingMaxFromLength = child.length;
          }

          resultingTokenLength += child.tokenLength;
        } else {
          child.from = child.token.from;
          child.length = child.token.length;
          child.tokenLength = 1;
          child.tokenFrom = Arrays.binarySearch(arrayFroms, child.token.from);

          if (resultingFrom > child.token.from)
            resultingFrom = child.token.from;

          if (resultingTokenFrom > child.tokenFrom)
            resultingTokenFrom = child.tokenFrom;

          if (resultingMaxFrom < child.token.from) {
            resultingMaxFrom = child.token.from;
            resultingMaxFromLength = child.token.length;
          }

          resultingTokenLength += child.tokenLength;
        }
      }

      parseTreeNode.from = resultingFrom;
      parseTreeNode.tokenFrom = resultingTokenFrom;
      parseTreeNode.length =
          resultingMaxFrom - resultingFrom + resultingMaxFromLength;
      parseTreeNode.tokenLength = resultingTokenLength;
    }
  }

  /**
   * Рассчитать покрытие узлами итогового текстового фрагмента (установить у терминальных узлов значения "from" и "length").
   * 
   * @param tokens
   * @param parseTreeNode
   */
  protected void calculateParseTreeNodeCoverages(List<Token> tokens,
      ParseTreeNode parseTreeNode) {
    int[] arrayFroms = new int[tokens.size()];
    int counter = 0;
    for (Token t : tokens) {
      arrayFroms[counter] = t.from;
      counter++;
    }
    calculateParseTreeNodeCoverages(arrayFroms, parseTreeNode);
  }

  private List<GrammarKeywordEntrance> convertParseTreesToEntrances(
      SentenceProcessingContext context, Collection<ParseTreeNode> trees,
      Grammar grammar, List<Token> tokens) {
    List<GrammarKeywordEntrance> result =
        new LinkedList<GrammarKeywordEntrance>();
    context.getLogger().startParseTreeProcessing(context, trees);
    for (ParseTreeNode parseTreeNode : trees) {
      GrammarKeywordEntrance entrance =
          convertParseTreeToEntrance(context, parseTreeNode, grammar, tokens);
      if (entrance != null)
        result.add(entrance);
    }
    context.getLogger().endParseTreeProcessing(context, trees);
    return result;
  }

  /**
   * Выполнить конвертацию дерева разбора во вхождение ({@link GrammarKeywordEntrance}).
   * 
   * Функция достаточно объемная по функционалу:
   * <ol>
   * <li>выполнить расчет покрытия каждым деревом нижележащих токенов;</li>
   * <li>вычленить пользовательский корневой узел из полученного дерева (т.к. фактически дерево построено с использование синтетчного узла);</li>
   * <li>выполнить валидацию дерева (выполнить проверку выполнения всевозможных ограничений, при этом так же много чего меняется в данных дерева:
   * распередляются данные касающиеся главных слов, выполняется присвоение допустимых грамматем для каждого узла и т.д.) - в случае неуспешности 
   * проверки выбрасывается специфичное исключение, говорящее о том, что никакого вхождения не будет</li>
   * </ol>
   * 
   * @param context контекст
   * @param parseTreeNode фрагмент дерева
   * @param grammar грамматика
   * @param tokens токена
   * @return вхождение с присвоенным деревом разбора или null
   */
  private GrammarKeywordEntrance convertParseTreeToEntrance(
      SentenceProcessingContext context, ParseTreeNode parseTreeNode,
      Grammar grammar, List<Token> tokens) {
    // calulate покрытие узлами итогового текстового фрагмента
    // (установить у терминальных узлов значения "from" и "length").
    calculateParseTreeNodeCoverages(tokens, parseTreeNode);

    // extract user root node
    ParseTreeNode userRootSymbolNode = extractGrammarUserRootSymbolNode(
        parseTreeNode, grammar.getUserRootSymbol());

    if (userRootSymbolNode == null)
      return null;

    // проверить ограничения всевозможных помет грамматики (при невыполнении -
    // throw исключение)
    try {
      ParseTreeValidator validator = new ParseTreeValidator(hierarchyManager);
      validator.validateTree(context, userRootSymbolNode);
    } catch (ParseTreeValidationException e) {
      context.getLogger().detectedParseTree(context, parseTreeNode,
          userRootSymbolNode, false, e);
      return null;
    } catch (Exception e) {
      context.getLogger().error(e.getMessage(), e);
    }
    // success-case
    context.getLogger().detectedParseTree(context, parseTreeNode,
        userRootSymbolNode, true, null);

    return new GrammarKeywordEntrance(userRootSymbolNode.tokenFrom,
        userRootSymbolNode.tokenLength, context.getArticle(),
        userRootSymbolNode);
  }

  /**
   * Complete SPPF parseing to select parsing trees.
   * 
   * @param sppfNode SPPF root node.
   * @param grammar grammar
   * @return list of trees
   */
  protected List<ParseTreeNode> createParseTrees(SPPFNode sppfNode,
      Grammar grammar) {
    SPPFDecomposer decomposer = new SPPFDecomposer();

    List<ParseTreeBuilder> treeBuilders =
        new LinkedList<GrammarManager.ParseTreeBuilder>();

    ParseTreeBuilder treeBuilder = new ParseTreeBuilder(grammar, treeBuilders);
    treeBuilders.add(treeBuilder);

    decomposer.walkSPPF(sppfNode, treeBuilder);
    List<ParseTreeNode> result = new LinkedList<ParseTreeNode>();
    for (ParseTreeBuilder parseTreeBuilder : treeBuilders)
      result.add(parseTreeBuilder.getTree());
    return result;
  }

  protected ParseTreeNode extractGrammarUserRootSymbolNode(
      ParseTreeNode parseTreeNode, Symbol symbol) {
    if (symbol.equals(parseTreeNode.symbol))
      return parseTreeNode;
    if (parseTreeNode.children.size() == 0)
      return null;
    for (ParseTreeNode child : parseTreeNode.children) {
      ParseTreeNode node = extractGrammarUserRootSymbolNode(child, symbol);
      if (node != null)
        return node;
    }
    return null;
  }

  public Grammar getGrammar(String articleName) {
    return grammarMap.get(articleName);
  }

  public Map<GrammarDirectiveTypes, Object>
      getGrammarDirectives(String articleName) {
    return grammarDirectivesMap.get(articleName);
  }

  public Collection<String> getUsedGrammarKeywords(String article) {
    return articleUsedGrammarKeywordsMap.get(article);
  }

  public Collection<String> getUsedPlainKeywords(String article) {
    return articleUsedPlainKeywordsMap.get(article);
  }

  /**
   * 
   * Вызывается при прочтении конфигурационного файла (т.е. достаточно заранее до вызова парсинга). 
   * 
   * Приэтом не свя информация о статьях может иметься в наличии.
   * 
   * @param articleName
   * @param grammarPath
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public void loadGrammar(String articleName, String grammarPath)
      throws IOException {
    StringWriter grammarText = readGrammarFileContent(grammarPath);
    NGLRGrammarProcessor grammarProcessor = new NGLRGrammarProcessor();
    // shallow processing
    grammarProcessor.parseGrammar(grammarText.toString());
    Grammar grammar = grammarProcessor.getGrammar();
    Map<GrammarDirectiveTypes, Object> grammarDirectives =
        grammarProcessor.getGrammarDirectives();
    List<String> includes = (List<String>) grammarProcessor
        .getGrammarDirectives().get(GrammarDirectiveTypes.INCLUDE);

    if (null == includes || includes.isEmpty()) {
    } else {
      processIncludeDirectives(grammarProcessor, includes, grammar,
          grammarDirectives);
    }
    // expand grammar
    grammarProcessor.expandGrammar(grammar, grammarDirectives);
    grammar = grammarProcessor.getGrammar();
    grammarDirectives = grammarProcessor.getGrammarDirectives();
    grammarMap.put(articleName, grammar);
    grammarDirectivesMap.put(articleName, grammarDirectives);
  }

  public List<GrammarKeywordEntrance>
      processArticle(SentenceProcessingContext context, List<Token> tokens) {
    return processArticle(context, tokens, true);
  }

  /**
   * Отработать статью грамматики.
   * 
   * С результатами работы грамматик на предыдущих уровнях обходимся как со всеми 
   * {@link IKeywordEntrance} (т.е. вычислять оптимальное покрытие,
   * что приводит к тому, что найденные деревья с меньшим покрытием - игнорируются,
   * из чего следует необходимость несмешивать деревьяс разными назначениями в одной грамматике), 
   * потом все привязывать к токенам ({@link Token}) и отдавать основной грамматике. 
   * 
   * 
   * Результаты основной грамматики в виде {@link IKeywordEntrance} передавать интерпретатору для 
   * извлечения фактов.
   * 
   * @param context
   * @param tokens
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<GrammarKeywordEntrance> processArticle(
      SentenceProcessingContext context, List<Token> tokens,
      boolean interpretate) {
    context.getLogger().startArticleProcessing(context);

    // примерно одинаковые процедуры
    // но вначале - потенциальная обработка директив...
    processGrammarDirectives(context, tokens);
    // ключевые слова статьи.....
    processArticleKeywords(context, tokens);

    // apply current grammar
    Grammar grammar = grammarMap.get(context.getArticle());
    List<? extends IKeywordEntrance> grammarEntrances =
        processGrammar(context, grammar, tokens);

    context.getLogger().detectedUnoptimizedKwEntrances(context,
        grammarEntrances);

    keywordManager.simpleCoverageFiltering(grammarEntrances);
    // long start = System.currentTimeMillis();
    // System.out.println(grammarEntrances.size());
    // calculate optimal (if exist entrances)
    List<GrammarKeywordEntrance> optimalCoverage =
        (List<GrammarKeywordEntrance>) keywordManager
            .calculateOptimalKeywordsCoverage(grammarEntrances, tokens.size());
    // long end = System.currentTimeMillis();
    // System.out.println("coverage calc: " + (end-start));

    // modify tokens in case were found other entrances (plan-kw/grammar-kw)
    if (optimalCoverage != null && !optimalCoverage.isEmpty()) {
      context.getLogger().detectedOptimalKwEntrances(context, optimalCoverage);
      tokenManager.modifyTokensByKeywords(tokens, optimalCoverage);
      context.getLogger().tokensModified(context, tokens);

      Map<GrammarDirectiveTypes, Object> directivesMap =
          grammarDirectivesMap.get(context.getArticle());
      if (!directivesMap.containsKey(GrammarDirectiveTypes.NO_INTERP)
          && interpretate)
        interpreter.processEntrances(context, optimalCoverage, tokens);

      context.getLogger().endArticleProcessing(context);
      return optimalCoverage;
    }
    context.getLogger().endArticleProcessing(context);
    return Collections.emptyList();
  }

  private void processArticleKeywords(SentenceProcessingContext context,
      List<Token> tokens) {
    // article body kws processing
    List<IKeywordEntrance> entrances = new LinkedList<IKeywordEntrance>();
    Collection<String> grammarsKws =
        articleUsedGrammarKeywordsMap.get(context.getArticle());

    if (grammarsKws != null)
      context.getLogger().grammarHasGrammarKeywords(context, grammarsKws);
    for (String grammar : grammarsKws) {
      String oldArticle = context.getArticle();
      context.setArticle(grammar);
      entrances.addAll(processArticle(context, tokens, false));
      context.setArticle(oldArticle);
    }

    Collection<String> plainKws =
        articleUsedPlainKeywordsMap.get(context.getArticle());
    if (plainKws != null) {
      context.getLogger().grammarHasPlainKeywords(context, plainKws);
      entrances
          .addAll(keywordManager.detectPlainKeywordEntrances(plainKws, tokens));
    }

    context.getLogger().detectedUnoptimizedKwEntrances(context, entrances);

    if (entrances.isEmpty())
      return;

    // calculate optimal (if exist entrances)
    List<? extends IKeywordEntrance> optimalCoverage = keywordManager
        .calculateOptimalKeywordsCoverage(entrances, tokens.size());

    // modify tokens in case were found other entrances (plan-kw/grammar-kw)
    if (optimalCoverage != null && !optimalCoverage.isEmpty()) {
      context.getLogger().detectedOptimalKwEntrances(context, optimalCoverage);
      tokenManager.modifyTokensByKeywords(tokens, optimalCoverage);
      context.getLogger().tokensModified(context, tokens);
    }
  }

  /**
   * 
   * Если в рамках директив грамматики есть перечень грамматик для
     первоначальной обработки ....
     типа #GRAMMAR_KWSET ["some_othe_grammar"]
  
   * 
   * @param context
   * @param tokens
   */
  @SuppressWarnings("unchecked")
  private void processGrammarDirectives(SentenceProcessingContext context,
      List<Token> tokens) {

    Map<GrammarDirectiveTypes, Object> directivesMap =
        grammarDirectivesMap.get(context.getArticle());
    context.getLogger().grammarHasDirectives(context, directivesMap);

    if (!directivesMap.containsKey(GrammarDirectiveTypes.KWSET))
      return;

    List<String> kwSet =
        (List<String>) directivesMap.get(GrammarDirectiveTypes.KWSET);
    List<IKeywordEntrance> entrances = new LinkedList<IKeywordEntrance>();
    for (String grammar : kwSet) {
      String oldArticle = context.getArticle();
      context.setArticle(grammar);
      entrances.addAll(processArticle(context, tokens, false));
      context.setArticle(oldArticle);
    }
    entrances.addAll(keywordManager.detectPlainKeywordEntrances(kwSet, tokens));

    context.getLogger().detectedUnoptimizedKwEntrances(context, entrances);

    if (entrances.isEmpty())
      return;
    // calculate optimal (if exist entrances)
    List<? extends IKeywordEntrance> optimalCoverage = keywordManager
        .calculateOptimalKeywordsCoverage(entrances, tokens.size());

    // modify tokens in case were found other entrances (plan-kw/grammar-kw)
    if (optimalCoverage != null && !optimalCoverage.isEmpty()) {
      context.getLogger().detectedOptimalKwEntrances(context, optimalCoverage);
      tokenManager.modifyTokensByKeywords(tokens, optimalCoverage);
      context.getLogger().tokensModified(context, tokens);
    }
  }

  private List<GrammarKeywordEntrance> processGrammar(
      SentenceProcessingContext context, Grammar grammar, List<Token> tokens) {
    context.getLogger().startGrammarProcessing(context, grammar);
    RNGLRAnalyser analyser = createAnalyser(grammar);
    boolean aRes = analyser.processTokens(tokens);
    if (!aRes)
      return Collections.emptyList();
    SPPFNode sppfNode = analyser.getRootNode();
    Collection<ParseTreeNode> trees = createParseTrees(sppfNode, grammar);
    return convertParseTreesToEntrances(context, trees, grammar, tokens);
  }

  protected RNGLRAnalyser createAnalyser(Grammar grammar) {
    LR1ParseTableBuilder lr1ParseTableBuilder =
        new LR1ParseTableBuilder(grammar);
    lr1ParseTableBuilder.calculateStates();
    lr1ParseTableBuilder.calculateRN2ActionTable();
    List<Map<Symbol, List<ActionRecord>>> actionTable =
        lr1ParseTableBuilder.getRN2ActionTable();
    List<List<LR1Point>> states = lr1ParseTableBuilder.getStates();
    RNGLRAnalyser analyser = new RNGLRAnalyser(grammar, actionTable, states,
        new FullMorphTokenMatcher(hierarchyManager), lr1ParseTableBuilder,
        lr1ParseTableBuilder.getBaseNTToNTExtData());
    return analyser;
  }

  /**
   * Вызывается в случае наличия INCLUDE-директив в корневой грамматике.
   */
  @SuppressWarnings("unchecked")
  private void processIncludeDirectives(NGLRGrammarProcessor grammarProcessor,
      List<String> includes, Grammar grammar,
      Map<GrammarDirectiveTypes, Object> grammarDirectives) throws IOException {
    for (String include : includes) {
      StringWriter grammarText = readGrammarFileContent(include);
      // shallow processing
      grammarProcessor.parseGrammar(grammarText.toString());
      Grammar grammar2 = grammarProcessor.getGrammar();
      Map<GrammarDirectiveTypes, Object> grammarDirectives2 =
          grammarProcessor.getGrammarDirectives();
      List<String> includes2 = (List<String>) grammarProcessor
          .getGrammarDirectives().get(GrammarDirectiveTypes.INCLUDE);

      // т.к. порядок вхождения проекций в CFG неважен -- просто добавляем текст
      // в конец.
      grammar.getProjections().addAll(grammar2.getProjections());

      // ==== process filter
      // ==== process filter

      // ==== process GRAMMAR_KWSET
      List<String> kwset =
          (List<String>) grammarDirectives.get(GrammarDirectiveTypes.KWSET);
      if (kwset == null) {
        kwset = new LinkedList<String>();
        grammarDirectives.put(GrammarDirectiveTypes.KWSET, kwset);
      }
      List<String> kwset2 =
          (List<String>) grammarDirectives2.get(GrammarDirectiveTypes.KWSET);
      if (kwset2 != null) {
        kwset.addAll(kwset2);
      }
      // ==== process GRAMMAR_KWSET

      if (null == includes2 || includes2.isEmpty()) {
      } else {
        processIncludeDirectives(grammarProcessor, includes2, grammar2,
            grammarDirectives2);
      }
    }
  }

  private StringWriter readGrammarFileContent(String grammarPath)
      throws IOException {
    String absolutePath = configurationReader.getAbsolutePath(grammarPath);
    FileInputStream fileInputStream = new FileInputStream(absolutePath);
    StringWriter grammarFileContent = new StringWriter();
    IOUtils.copy(fileInputStream, grammarFileContent);
    IOUtils.closeQuietly(fileInputStream);
    return grammarFileContent;
  }

  @Deprecated
  public void setConfigurationReader(ConfigurationReader configurationReader) {
    this.configurationReader = configurationReader;
  }

  @Deprecated
  public void setInterpreter(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  @Deprecated
  public void setKeywordManager(KeywordManager keywordManager) {
    this.keywordManager = keywordManager;
  }

  @Deprecated
  public void setTokenManager(TokenManager tokenManager) {
    this.tokenManager = tokenManager;
  }

  public static GrammarManager newInstance(
      ConfigurationReader configurationReader, KeywordManager keywordManager,
      Interpreter interpreter, TokenManager tokenManager,
      HierarchyManager hierarchyManager) {
    GrammarManager result = new GrammarManager();
    result.configurationReader = configurationReader;
    result.keywordManager = keywordManager;
    result.interpreter = interpreter;
    result.tokenManager = tokenManager;
    result.hierarchyManager = hierarchyManager;
    return result;
  }
}
