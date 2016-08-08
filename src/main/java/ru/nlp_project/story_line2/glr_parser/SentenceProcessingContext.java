package ru.nlp_project.story_line2.glr_parser;

import ru.nlp_project.story_line2.glr_parser.GLRParser.Sentence;

/**
 * 
 * Контект анализа предложений.
 * 
 * После разделения входящего предложения на части. Дальнейший анализ
 * осуществляется автономно для каждого предложения, при этом часть компонентов
 * анализатора являются разделяемым (общими) часть -- создаются новыми для
 * каждого предложения.
 * 
 * @author fedor
 *
 */
public class SentenceProcessingContext {

  public static SentenceProcessingContext create(String processingArticle,
      Sentence sentence, IFactListener factListener, IGLRLogger logger) {
    SentenceProcessingContext result = new SentenceProcessingContext();
    result.article = processingArticle;
    result.sentence = sentence;
    result.factListener = factListener;
    result.logger = logger;
    return result;
  }

  IFactListener factListener;
  String article;
  Sentence sentence;
  IGLRLogger logger;

  private SentenceProcessingContext() {
  }

  public IFactListener getFactListener() {
    return factListener;
  }

  public IGLRLogger getLogger() {
    return logger;
  }

  public String getArticle() {
    return article;
  }

  public Sentence getSentence() {
    return sentence;
  }

  public void setArticle(String article) {
    this.article = article;
    
  }

}
