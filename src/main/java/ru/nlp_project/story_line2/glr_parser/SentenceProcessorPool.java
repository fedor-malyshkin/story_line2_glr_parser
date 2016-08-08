package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.GLRParser.Sentence;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManager;

public class SentenceProcessorPool {
  private DictionaryManager dictionaryManager;
  private IFactListener factListener;
  private IGLRLogger logger;

  private TokenManager tokenManager;
  private NameFinder nameFinder;
  private KeywordManager keywordManager;

  private SentenceProcessorPool() {

  }

  public List<Token> generateTokens(final String text) {
    return generateTokens(text, true);
  }

  public List<Token> generateTokens(final String sentenceText,
      boolean addMorphInfo) {
    return tokenManager.splitIntoTokens(sentenceText, addMorphInfo);
  }

  public void processSentence(List<String> articles, Sentence sentence) {
    logger.startSentenceProcessing(sentence, articles);
    List<Token> tokens = tokenManager.splitIntoTokens(sentence.value, true);
    logger.tokensGenerated(tokens);
    // произвести предварительную обработку (частичную склейку) токенов
    nameFinder.preprocessTokens(tokens);
    logger.tokenNamesGenerated(tokens);
    // поиск имен - один раз для всех статей
    processNames(tokens);

    // for each article
    for (String article : articles) {
      SentenceProcessingContext context = SentenceProcessingContext
          .create(article, sentence, factListener, logger);
      dictionaryManager.processArticle(context, tokens);
    }
    logger.endSentenceProcessing(sentence, articles);
  }

  private void processNames(List<Token> tokens) {
    Collection<NameFinder.FIOEntry> detectedFIO =
        nameFinder.detectFIOKeywordEntrances(tokens);
    List<IKeywordEntrance> entrances = new LinkedList<IKeywordEntrance>();
    entrances.addAll(nameFinder.combineFoundNames(detectedFIO));

    logger.detectedUnoptimizedKwEntrances(null, entrances);

    if (entrances.isEmpty())
      return;

    // calculate optimal (if exist entrances)
    List<? extends IKeywordEntrance> optimalCoverage = keywordManager
        .calculateOptimalKeywordsCoverage(entrances, tokens.size());

    // modify tokens in case were found other entrances (plan-kw/grammar-kw)
    if (optimalCoverage != null && !optimalCoverage.isEmpty()) {
      logger.detectedOptimalKwEntrances(null, optimalCoverage);
      tokenManager.modifyTokensByKeywords(tokens, optimalCoverage);
      logger.tokensModified(null, tokens);
    }
  }

  @Deprecated
  public void setDictionaryManager(DictionaryManager dictionaryManager) {
    this.dictionaryManager = dictionaryManager;
  }

  public void setFactListener(IFactListener factListener) {
    this.factListener = factListener;
  }

  public void setLogger(IGLRLogger logger) {
    this.logger = logger;
  }

  @Deprecated
  public void setTokenManager(TokenManager tokenManager) {
    this.tokenManager = tokenManager;

  }

  public static SentenceProcessorPool newInstance(
      DictionaryManager dictionaryManager2, TokenManager tokenManager2,
      NameFinder nameFinder2, KeywordManager keywordManager) {
    SentenceProcessorPool result = new SentenceProcessorPool();
    result.dictionaryManager = dictionaryManager2;
    result.tokenManager = tokenManager2;
    result.nameFinder = nameFinder2;
    result.keywordManager = keywordManager;
    return result;
  }

}
