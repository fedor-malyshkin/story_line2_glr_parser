package ru.nlp_project.story_line2.glr_parser.keywords;

import ru.nlp_project.story_line2.glr_parser.Token;

public class PlainKeywordEntrance implements IKeywordEntrance {
  /**
   * Номер первого токена, с которого начинается покрытие для данного вхождения.
   */
  int from;
  /**
  *Concrete position in keyword set. 
  */
  int keywordPos;
  /**
   * Идентификатор набора ключевых слов.
   */
  int keywordSet;
  /**
   * Длинна вхождения (в кол-ве токенов).
   */
  int length;
  /**
   * Индекс главного слова (0....) в выбранном вхождении.
   */
  int mainWordNdx;
  /**
   * Токен, соотвествующий главному слову и содержащий лексемы, соотвествующие ограничениям, 
   * которые возлагались на ключевое слово (gramm, gramm-X, agr).
   */
  Token mainWordToken;

  public PlainKeywordEntrance(int from, int length, int keywordSet,
      int keywordPos, int mainWordNdx, Token mainWordToken) {
    super();
    this.mainWordNdx = mainWordNdx;
    this.from = from;
    this.length = length;
    this.keywordSet = keywordSet;
    this.keywordPos = keywordPos;
    this.mainWordToken = mainWordToken;
  }

  @Override
  public int getFrom() {
    return from;
  }

  @Override
  public int getLength() {
    return length;

  }

  public int getKeywordPos() {
    return keywordPos;
  }

  public int getKeywordSet() {
    return keywordSet;
  }

  public int getMainWordNdx() {
    return mainWordNdx;
  }


  @Override
  public String toString() {
    return "<" + from + ";" + length + ";" + keywordSet + ";" + keywordPos
        + "(" + mainWordNdx + ")>";
  }

  public Token getMainWordToken() {
    return mainWordToken;
  }

}