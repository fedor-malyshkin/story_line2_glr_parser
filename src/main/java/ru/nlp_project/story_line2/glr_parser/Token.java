package ru.nlp_project.story_line2.glr_parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.GrammemeUtils.GrammemeEnum;
import ru.nlp_project.story_line2.morph.Grammemes;

/**
 * 
 * Объект-токен, содержащий в себе всю необходимую информацию по каждому 
 * токену внутри предложения: тип, значение, длинна и т.д....
 * 
 * WARNING: объект после создания не должен меняться, как и 
 * вся его грамматическая часть, т.к. они многократно анализируются и 
 * проверяются в других вариантах разбора дерева. В случае необходимости 
 * замены - делайте клон и заменяйте его в коллекции.
 * 
 * @author fedor
 *
 */
public class Token {
  /**
   * ОБъект-лексема, содержащая всю грамматическую связь.
   * 
   * WARNING: объект после создания не должен меняться, т.к. он многократно анализируется и 
   * проверяются в других местах. В принципе причин для изменения также нет.
   * 
   * @author fedor
   *
   */
  public class Lexeme {
    String lexemeValue;
    boolean exactMatch;
    Grammemes grammemes;
    String id;
    String lemm;

    public Lexeme(String id, String lemm, String lexemeValue,
        Grammemes grammemes, boolean exactMatch) {
      super();
      this.id = id;
      this.lemm = lemm;
      this.lexemeValue = lexemeValue;
      this.grammemes = grammemes;
      this.exactMatch = exactMatch;
    }

    public Lexeme clone() {
      return new Lexeme(this.id, this.lemm, this.lexemeValue,
          this.grammemes.clone(), this.exactMatch);
    }

    public Grammemes getGrammemes() {
      return grammemes;
    }

    public String getId() {
      return id;
    }

    public String getLemm() {
      return lemm;
    }

    public GrammemeEnum getPOS() {
      return grammemes.getPOS();
    }

    public String getValue() {
      return lexemeValue;
    }

    public boolean has(GrammemeEnum grammeme) {
      return grammemes.has(grammeme);
    }

    public boolean hasAll(Collection<GrammemeEnum> grammemesList) {
      return grammemes.hasAll(grammemesList);
    }

    public boolean hasAllEx(Collection<GrammemeEnum> other) {
      return grammemes.hasAllEx(other);
    }

    public boolean hasEx(GrammemeEnum grammeme) {
      return grammemes.hasEx(grammeme);
    }

    public Grammemes intersect(Lexeme other) {
      if (other == null)
        return new Grammemes();
      return GrammemeUtils.intersect(this.grammemes, other.grammemes);
    }

    public boolean isCAgree(Lexeme other) {
      return grammemes.isCAgree(other.getGrammemes());
    }

    public boolean isExactMatch() {
      return exactMatch;
    }

    public boolean isGCAgree(Lexeme other) {
      return grammemes.isGCAgree(other.getGrammemes());
    }

    public boolean isGNAgree(Lexeme other) {
      return grammemes.isGNAgree(other.getGrammemes());
    }

    public boolean isGNCAgree(Lexeme other) {
      return grammemes.isGNCAgree(other.getGrammemes());
    }

    public boolean isNCAgree(Lexeme other) {
      return grammemes.isNCAgree(other.getGrammemes());
    }

    @Override
    public String toString() {
      return String.format("'%s' (%s)", lemm, grammemes);
    }

  }

  public enum TokenTypes {
    /**
     * Комбинированный токен - результат выполнения грамматики (grammar keywords)
     */
    COMBINED_GRAMMAR,
    /**
    * Комбинированный токен - результат поиска простых ключевых слов (plain keywords)
    */
    COMBINED_PLAIN,
    /**
    * ТОкен-разделитель (может быть что угодно из предопределенного массива)
    */
    DELIM, 
    /**
     * Служебный токе - конец ввода (не используется в большинстве случаев).
     */
    EOI,
    /**
    * ТОкен-пробел
    */
    SPACE,
    /**
    * Токен с текстом (обычное слово).
    */
    WORD,

    /**
     * Комбинированный токен - результат поиска имен людей (name finding)
     */
    COMBINED_FIO;
  }

  int from;
  /**
   * Первая буква слова стоит в верхнем регистре. 
   */
  boolean hReg1 = false;
  /**
   * В верхнем регистре стоит первая буква слова и как минимум еще одна буква 
   * слова, как например, в слове «МосСтрой».
   */
  boolean hReg2 = false;
  /**
   * Само значение {@link IKeywordEntrance} (в случае {@link GrammarKeywordEntrance} через него можно получить 
   * доступ к {@link ru.nlp_project.story_line2.glr_parser.InterpreterImpl.InterpretationResult} для интерпретации).
   */
  IKeywordEntrance kw = null;
  /**
   * keywords value.
   */
  String kwName = null;
  int length;
  LinkedList<Lexeme> lexemes;
  /**
   * Слово или группа слов c открывающей кавычкой перед первым символом и без
   *  закрывающей кавычки после последнего символа.
   */
  boolean lQuoted = false;
  /**
   * Слово состоит из букв латинского алфавита.
   */
  boolean lat = false;
  /**
   * Все буквы слова в нижнем регистре.       
   */
  boolean lReg = false;
  /**
   * Слово или группа слов в кавычках.
   */
  boolean quoted = false;
  /**
   * Слово или группа слов c закрывающей кавычкой после последнего символа и 
   * без закрывающей кавычки перед первым символом.
   */
  boolean rQuoted = false;

  boolean uReg;

  TokenTypes type;
  String value;
  public boolean kwColon = false;
  public boolean kwComma = false;
  public boolean kwDollar = false;
  public boolean kwHyphen = false;
  public boolean kwLBracket = false;
  public boolean kwRBracket = false;
  public boolean kwPercent = false;
  public boolean kwPlusSign = false;
  public boolean kwPunct = false;
  public boolean kwQuoteDbl = false;
  public boolean kwQuoteSng = false;
  public boolean kwWord = false;
  public boolean kwInitial = false;

  public Token(int from, int length, String value, TokenTypes type) {
    super();
    this.from = from;
    this.length = length;
    this.value = value;
    this.type = type;

    this.lexemes = new LinkedList<Lexeme>();
  }

  public void addLexeme(Lexeme lexeme) {
    this.lexemes.add(lexeme);
  }

  public void addLexeme(String id, String lemm, String lexemeValue,
      Grammemes grammemes, boolean exactMatch) {
    this.lexemes.add(new Lexeme(id, lemm, lexemeValue, grammemes, exactMatch));
  }

  public Token clone() {
    Token result = new Token(this.from, this.length, this.value, this.type);
    cloneAttributes(result);
    return result;
  }

  protected void cloneAttributes(Token result) {
    result.kwName = this.kwName;
    result.kw = this.kw;
    // qute
    result.lQuoted = this.lQuoted;
    result.rQuoted = this.rQuoted;
    result.quoted = this.quoted;
    // reg
    result.hReg1 = this.hReg1;
    result.hReg2 = this.hReg2;
    result.lQuoted = this.lQuoted;
    result.uReg = this.uReg;
    result.lat = this.lat;

    // keyword attributes
    result.kwColon = this.kwColon;
    result.kwComma = this.kwComma;
    result.kwDollar = this.kwDollar;
    result.kwHyphen = this.kwHyphen;
    result.kwLBracket = this.kwLBracket;
    result.kwRBracket = this.kwRBracket;
    result.kwPercent = this.kwPercent;
    result.kwPlusSign = this.kwPlusSign;
    result.kwPunct = this.kwPunct;
    result.kwQuoteDbl = this.kwQuoteDbl;
    result.kwQuoteSng = this.kwQuoteSng;
    result.kwWord = this.kwWord;
    result.kwInitial = this.kwInitial;

    for (Lexeme lexem : lexemes)
      result.lexemes.add(new Lexeme(lexem.id, lexem.lemm, lexem.lexemeValue,
          lexem.grammemes.clone(), lexem.exactMatch));
  }

  public int getFrom() {
    return from;
  }

  public String getKwName() {
    return kwName;
  }

  public int getLength() {
    return length;
  }

  /**
   * Получить итератор по настоящему списку лексем.
   * 
   * ВНИМАНИЕ: операци вставки и уадления находят фактическое отражение в 
   * состоянии объекта.
   * 
   * 
   * @return
   */
  public ListIterator<Lexeme> getLexemesIterator() {
    return lexemes.listIterator();
  }

  /**
   * Получить лексмеы токена узла.
   * ВНИМАНИЕ: возвращется копия списка с настоящими узлами - поэтому изменения 
   * в список фактически не вносятся. А вот манипуляции с объектами списка 
   * отражаются на реальном содержании объекта.
   * 
   * При необходимости осуществлять манипуляции со 
   * списком - {@link Token#getLexemesIterator()}.
   * 
   * @return
   */
  public LinkedList<Lexeme> getLexemesListCopy() {
    return getLexemesListCopy(false);
  }

  /**
   * 
   * Получить лексмеы токена узла.
   * 
   * ВНИМАНИЕ: возвращется копия списка с настоящими узлами - поэтому изменения 
   * в список фактически не вносятся. А вот манипуляции с объектами списка 
   * отражаются на реальном содержании объекта.
   * 
   * При необходимости осуществлять манипуляции со 
   * списком - {@link Token#getLexemesIterator()}.
   * 
   * @param includePredicted включать в выборку предсказанные лексемы или нет.
   * 
   * @return
   */
  public LinkedList<Lexeme> getLexemesListCopy(boolean includePredicted) {
    return lexemes.stream().filter(l -> l.exactMatch)
        .collect(Collectors.toCollection(LinkedList::new));
  }

  public TokenTypes getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public boolean hasLexemes() {
    return lexemes.size() > 0;
  }

  public boolean isHReg1() {
    return hReg1;
  }

  public boolean isHReg2() {
    return hReg2;
  }

  public boolean isLat() {
    return lat;
  }

  public boolean isLQuoted() {
    return lQuoted;
  }

  public boolean isLReg() {
    return lReg;
  }

  public boolean isQuoted() {
    return quoted;
  }

  public boolean isRQuoted() {
    return rQuoted;
  }

  public boolean isUReg() {
    return uReg;
  }

  /**
   * Удалить лексемы (реальные/предсказанные) из токена.
   * 
   *  ВНИМАНИЕ: производится фактическое удаление данных из токена.
   * 
   * @param exact удалть точные совпадения
   * @param notExact удалить предсказанные лексемы.
   */
  public void removeLexemes(boolean exact, boolean notExact) {
    if (exact)
      lexemes.removeIf(l -> l.exactMatch);
    if (notExact)
      lexemes.removeIf(l -> !l.exactMatch);
  }

  private String serializePOS() {
    List<String> sts = new ArrayList<String>();
    for (int i = 0; i < lexemes.size(); i++)
      sts.add(lexemes.get(i).lemm + "("
          + lexemes.get(i).grammemes.getPOS().toString() + ")");
    return StringUtils.join(sts, ",");
  }

  @Override
  public String toString() {
    if (kwName != null)
      return String.format("('%s', %d,%d, @%s, #%s, '%s')", value, from, length,
          type.toString(), serializePOS(), kwName);
    else
      return String.format("('%s', %d,%d, @%s, #%s)", value, from, length,
          type.toString(), serializePOS());
  }

}
