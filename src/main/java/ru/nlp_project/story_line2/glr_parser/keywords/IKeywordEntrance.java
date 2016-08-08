package ru.nlp_project.story_line2.glr_parser.keywords;

/**
 * Keyword entrance into text (could be plain keywordentrance/grammar found keyword entrance).
 * 
 * @author fedor
 *
 */
public interface IKeywordEntrance {

  /**
   * Номер первого токена, с которого начинается покрытие для данного вхождения.
   */
  int getFrom();

  /**
   * Длинна вхождения (в кол-ве токенов).
   */
  int getLength();

}
