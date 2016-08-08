package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManager;
import ru.nlp_project.story_line2.token.ISentenceDetectorListener;
import ru.nlp_project.story_line2.token.SentenceDetector;

/**
 * Основной класс обработки входящей грамматики и текста.
 * 
 * О много поточности: много поточность определяется посредством запуска
 * параллельно обработки каждого предложения в {@link #processText(List, String)}.
 * При этом необходимо учитывать, что все компоненты анализа (те что создаются,
 * конфигурируются и инициализируются в методе 
 * {@link #initialize(boolean, IGLRLogger, IFactListener)}) не должны хранить 
 * своё состояние внутри себя - лишь передавать структуры с состоянием между 
 * вызовами (т.е. быть stateless).
 * <br/>
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 */
public class GLRParser {

  class DefaultFactListener implements IFactListener {

  }

  class DefaultGLRLogger implements IGLRLogger {

  }

  public class Sentence {

    public int getIndex() {
      return index;
    }

    public int getLength() {
      return length;
    }

    public int getStart() {
      return start;
    }

    public String getValue() {
      return value;
    }

    int index;
    int length;
    int start;
    String value;

    public Sentence(int index, int start, int length, String value) {
      super();
      this.index = index;
      this.start = start;
      this.length = length;
      this.value = value;
    }

  }

  @Deprecated
  public static boolean invalidatedMorphDB = false;

  public static GLRParser newInstance(String configFile, boolean initMorph)
      throws IOException {
    return newInstance(configFile, null, null, initMorph, true);
  }

  public static GLRParser newInstance(String configFile, IGLRLogger logger2,
      IFactListener factListener2, boolean initMorph, boolean multiThread)
      throws IOException {
    GLRParser result = new GLRParser(configFile, multiThread);
    result.initialize(logger2, factListener2, initMorph);
    return result;
  }

  public static GLRParser newInstance(String configFile, IGLRLogger logger2,
      IFactListener factListener2, boolean initMorph) throws IOException {
    return newInstance(configFile, logger2, factListener2, initMorph, true);
  }

  private String configFile;
  private ConfigurationReader configurationReader;
  private DictionaryManager dictionaryManager;
  private GrammarManager grammarManager;
  private HierarchyManager hierarchyManager;
  private Interpreter interpreter;
  private KeywordManager keywordManager;
  private SentenceDetector sentenceDetector;
  private TokenManager tokenManager;
  private SentenceProcessorPool sentenceProcessorPool;
  private NameFinder nameFinder;
  private boolean multiThread;

  public void shutdown() {
    tokenManager.shutdown();
  }

  private GLRParser(String configFile, boolean multiThread) {
    this.multiThread = multiThread;
    this.configFile = configFile;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public DictionaryManager getDictionaryManager() {
    return dictionaryManager;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public GrammarManager getGrammarManager() {
    return grammarManager;
  }

  @Deprecated
  public HierarchyManager getHierarchyManager() {
    return hierarchyManager;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public Interpreter getInterpreter() {
    return interpreter;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public KeywordManager getKeywordManager() {
    return keywordManager;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public NameFinder getNameFinder() {
    return nameFinder;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public SentenceProcessorPool getSentenceProcessorPool() {
    return sentenceProcessorPool;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public TokenManager getTokenManager() {
    return tokenManager;
  }

  private synchronized void initialize(IGLRLogger logger2,
      IFactListener factListener2, boolean initMorph) throws IOException {
    configurationReader = ConfigurationReader.newInstance(configFile);

    String sentenceData =
        configurationReader.getConfigurationMain().sentenceData;
    sentenceDetector = SentenceDetector
        .newInstance(configurationReader.getInputStream(sentenceData));

    // hierarchyManager
    hierarchyManager = HierarchyManager.newInstance(configurationReader);

    // keywords
    keywordManager = KeywordManager.newInstance(configurationReader);

    // token manager
    tokenManager = TokenManager.newInstance(configurationReader, keywordManager,
        initMorph);

    // name finder
    nameFinder = NameFinder.newInstance(configurationReader, tokenManager);

    // interpreter
    interpreter = Interpreter.newInstance(configurationReader, tokenManager);

    // grammar manager
    grammarManager = GrammarManager.newInstance(configurationReader,
        keywordManager, interpreter, tokenManager, hierarchyManager);

    // dictionary manager
    dictionaryManager = DictionaryManager.newInstance(configurationReader,
        keywordManager, grammarManager);
    grammarManager.analyseUsedKewordSets();

    IGLRLogger logger = new DefaultGLRLogger();
    IFactListener factListener = new DefaultFactListener();
    if (logger2 != null)
      logger = logger2;
    if (factListener2 != null)
      factListener = factListener2;

    // sentence processor pool
    sentenceProcessorPool = SentenceProcessorPool.newInstance(dictionaryManager,
        tokenManager, nameFinder, keywordManager);
    sentenceProcessorPool.setFactListener(factListener);
    sentenceProcessorPool.setLogger(logger);
  }

  /**
   * 
   * Выполнить обработку текста с использование конкретных статей.
   * 
   * @param articles
   *          список статей
   * @param text
   * @throws IOException
   */
  public void processText(final List<String> articles, final String text)
      throws IOException {

    final List<Sentence> sentences = new ArrayList<Sentence>();
    sentenceDetector.process(text, new ISentenceDetectorListener() {
      int si = 0;

      @Override
      public void detectSentence(int start, int end) {
        sentences.add(
            new Sentence(si++, start, end - start, text.substring(start, end)));
      }

    });
    if (articles.isEmpty())
      articles.addAll(configurationReader.getConfigurationMain().articles);

    if (multiThread)
      sentences.parallelStream()
          .forEach((s) -> sentenceProcessorPool.processSentence(articles, s));
    else
      sentences.stream()
          .forEach((s) -> sentenceProcessorPool.processSentence(articles, s));
  }

  /**
   * Выполнить обработку текста.
   * 
   * @param text
   * @throws IOException
   */
  public void processText(final String text) throws IOException {
    processText(new ArrayList<String>(), text);
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public void setGrammarManager(GrammarManager grammarManager) {
    this.grammarManager = grammarManager;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public void setInterpreter(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public void setKeywordManager(KeywordManager keywordManager) {
    this.keywordManager = keywordManager;
  }

  /**
   * Testing purposes only (not for production!!!).
   * 
   * @param dictionaryManager
   */
  @Deprecated
  public void setTokenManager(TokenManager tokenManager) {
    this.tokenManager = tokenManager;
  }

}
