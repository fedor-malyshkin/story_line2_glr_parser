package ru.nlp_project.story_line2.glr_parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;

import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.morph.GrammemeEnum;

/**
 * @author fedor
 *
 */
public class SymbolTable {

  static {
    buildKeywords();
  }

  public enum SymbolTableEntryTypes {

    /**
    * Ключевое слово - название граммемы.
    */
    KeywordGrammeme,
    /**
    * Ключевое слово.
    */
    KeywordTerminal,
    /**
    * Переменная - заголово проекции.
    */
    VarProjectionHead;

  }

  /**
   * Любое слово, состоящее из букв русского или латинского алфавита. 
   * Также разрешаются слова записанные через дефис. 
   * Под это определение не попадают цепочки, которые содержат знаки 
   * пунктуации (кроме дефиса), специальные ASCII символы и цепочки цифр.
   */
  public static final String KW_WORD = "word";
  /**
   * Нераспознанное морфологией слово. К этому нетерминалу не относятся 
   * несловарные слова, которые описаны в газетире. Морфологический компонент 
   * строит парадигму для таких слов, и они становятся словарными, 
   * если соответствующие статьи упоминаются в данной грамматике.
   */
  public static final String KW_UNKN_POS = "unknownPOS";
  /**
   *     Двойные кавычки.
   */
  public static final String KW_QUOTE_DBL = "quoteDbl";
  /**
   *     Одинарные кавычки.
   */
  public static final String KW_QUOTE_SNG = "quoteSng";
  /**
   *     Открывающая скобка.
   */
  public static final String KW_LBRACKET = "lBracket";
  /**
      *Закрывающая скобка.  
   */
  public static final String KW_RBRACKET = "rBracket";
  /**
   *   Тире.
   */
  public static final String KW_HYPHEN = "hyphen";
  /**
   * точка
   */
  public static final String KW_PUNCT = "punct";
  /**
   *    Запятая.
   */
  public static final String KW_COMMA = "comma";
  /**
   *    Двоеточие.
   */
  public static final String KW_COLON = "colon";
  /**
   *  Цепочка символов, включающая символ %.
   */
  public static final String KW_PERCENT = "percent";
  /**
   *   Цепочка символов, включающая символ $.
   */
  public static final String KW_DOLLAR = "dollar";
  /**
   *     Знак плюс +.
   */
  public static final String KW_PLUS_SIGN = "plusSign";
  /**
   * Любая последовательность символов без пробелов. 
   * Нужно быть острожнее с конструкцией AnyWord*, 
   * так как парсер в этом случае будет строить очень 
   * много вариантов и его работа сильно замедлится.
   */
  public static final String KW_ANY_WORD = "anyWord";

  /**
   * Набор ключевых слов (в правильном регистре).
   */
  private static Set<String> keywords;
  /**
   * Map<lower case,noram case>
   */
  private static Map<String, String> normalizeKeywordsMap;
  private Map<String, SymbolTableEntryTypes> table = new HashMap<>();

  private static void buildKeywords() {
    normalizeKeywordsMap = new HashMap<String, String>();
    keywords = new HashSet<String>();

    // builtin keywords
    keywords.add(SymbolTable.KW_WORD);
    keywords.add(SymbolTable.KW_UNKN_POS);
    keywords.add(SymbolTable.KW_QUOTE_DBL);
    keywords.add(SymbolTable.KW_QUOTE_SNG);
    keywords.add(SymbolTable.KW_LBRACKET);
    keywords.add(SymbolTable.KW_RBRACKET);
    keywords.add(SymbolTable.KW_HYPHEN);
    keywords.add(SymbolTable.KW_PUNCT);
    keywords.add(SymbolTable.KW_COMMA);
    keywords.add(SymbolTable.KW_COLON);
    keywords.add(SymbolTable.KW_PERCENT);
    keywords.add(SymbolTable.KW_DOLLAR);
    keywords.add(SymbolTable.KW_PLUS_SIGN);
    keywords.add(SymbolTable.KW_ANY_WORD);

    // normalizeKewords
    normalizeKeywordsMap.put(SymbolTable.KW_WORD.toLowerCase(),
        SymbolTable.KW_WORD);
    normalizeKeywordsMap.put(SymbolTable.KW_UNKN_POS.toLowerCase(),
        SymbolTable.KW_UNKN_POS);
    normalizeKeywordsMap.put(SymbolTable.KW_QUOTE_DBL.toLowerCase(),
        SymbolTable.KW_QUOTE_DBL);
    normalizeKeywordsMap.put(SymbolTable.KW_QUOTE_SNG.toLowerCase(),
        SymbolTable.KW_QUOTE_SNG);
    normalizeKeywordsMap.put(SymbolTable.KW_LBRACKET.toLowerCase(),
        SymbolTable.KW_LBRACKET);
    normalizeKeywordsMap.put(SymbolTable.KW_RBRACKET.toLowerCase(),
        SymbolTable.KW_RBRACKET);
    normalizeKeywordsMap.put(SymbolTable.KW_HYPHEN.toLowerCase(),
        SymbolTable.KW_HYPHEN);
    normalizeKeywordsMap.put(SymbolTable.KW_PUNCT.toLowerCase(),
        SymbolTable.KW_PUNCT);
    normalizeKeywordsMap.put(SymbolTable.KW_COMMA.toLowerCase(),
        SymbolTable.KW_COMMA);
    normalizeKeywordsMap.put(SymbolTable.KW_COLON.toLowerCase(),
        SymbolTable.KW_COLON);
    normalizeKeywordsMap.put(SymbolTable.KW_PERCENT.toLowerCase(),
        SymbolTable.KW_PERCENT);
    normalizeKeywordsMap.put(SymbolTable.KW_DOLLAR.toLowerCase(),
        SymbolTable.KW_DOLLAR);
    normalizeKeywordsMap.put(SymbolTable.KW_PLUS_SIGN.toLowerCase(),
        SymbolTable.KW_PLUS_SIGN);
    normalizeKeywordsMap.put(SymbolTable.KW_ANY_WORD.toLowerCase(),
        SymbolTable.KW_ANY_WORD);

    // add grammemes
    List<GrammemeEnum> grammemeEnumList =
        EnumUtils.getEnumList(GrammemeEnum.class);
    for (GrammemeEnum gr : grammemeEnumList) {
      keywords.add(gr.toString().toLowerCase());
      normalizeKeywordsMap.put(gr.toString().toLowerCase(), gr.toString());
    }

  }

  public static Set<String> getKeywords() {
    return keywords;
  }

  public static Map<String, String> getNormalizeKeywordsMap() {
    return normalizeKeywordsMap;
  }

  public String getNormalCapitalizedKeyword(String value) {
    return normalizeKeywordsMap.get(value.toLowerCase());
  }

  public SymbolTableEntryTypes getSymbolTableValue(String entry) {
    return table.get(entry.toLowerCase());
  }

  public void initialize() {
    buildKeywordsInstance();
  }

  private void buildKeywordsInstance() {
    // table types
    table.put(KW_WORD.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_UNKN_POS.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_QUOTE_DBL.toLowerCase(),
        SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_QUOTE_SNG.toLowerCase(),
        SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_LBRACKET.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_RBRACKET.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_HYPHEN.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_PUNCT.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_COMMA.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_COLON.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_PERCENT.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_DOLLAR.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_PLUS_SIGN.toLowerCase(),
        SymbolTableEntryTypes.KeywordTerminal);
    table.put(KW_ANY_WORD.toLowerCase(), SymbolTableEntryTypes.KeywordTerminal);

    // add grammemes
    List<GrammemeEnum> grammemeEnumList =
        EnumUtils.getEnumList(GrammemeEnum.class);
    for (GrammemeEnum gr : grammemeEnumList) {
      table.put(gr.toString().toLowerCase(),
          SymbolTableEntryTypes.KeywordGrammeme);
    }

  }

  public boolean isTerminal(Symbol s) {
    String str = s.getValue().toLowerCase();
    if (table.get(str) == null)
      throw new IllegalArgumentException(
          "В таблице символов нет значения: " + s.getValue());
    return table.get(str) == SymbolTableEntryTypes.KeywordGrammeme
        || table.get(str) == SymbolTableEntryTypes.KeywordTerminal;
  }

  public void markAsProjectionHead(Symbol s) {
    table.put(s.getValue().toLowerCase(),
        SymbolTableEntryTypes.VarProjectionHead);
  }
}
