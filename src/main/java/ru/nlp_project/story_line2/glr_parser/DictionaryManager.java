package ru.nlp_project.story_line2.glr_parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManager;

/**
 * 
 *  Основной класс для работы с газеттиром/словарем, парсинг, пополнение ключевых слов, передача данных в менеджер грамматик, 
 *  вычисление зависимостей и т.д...
 *  
 *  MULTITHREAD_SAFE: YES
 * @author fedor
 *
 */
public class DictionaryManager {

  public class DictionaryEntry {
    String fileAbsolutePath;
    String grammarAbsolutePath;
    String name;
    DictionaryEntryTypes type;

    public DictionaryEntry(String name, DictionaryEntryTypes type) {
      super();
      this.name = name;
      this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      DictionaryEntry other = (DictionaryEntry) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

  }

  enum DictionaryEntryTypes {
    FILE, GRAMMAR, LIST;
  }

  private static final String KEY_ENTRY_GRAMMAR_FILE = "grammar_file";
  private static final String KEY_ENTRY_KEYWORDS = "keywords";
  private static final String KEY_ENTRY_KW_FILE = "keywords_file";
  private static final String KEY_ENTRY_NAME_NAME = "name";
  private static final String KEY_ENTRY_OPTIONS = "options";
  private static final String KEY_ENTRY_TYPE_FILE = "file";
  private static final String KEY_ENTRY_TYPE_GRAMMAR = "grammar";
  private static final String KEY_ENTRY_TYPE_LIST = "list";
  private static final String KEY_ENTRY_TYPE_NAME = "type";

  public static DictionaryManager newInstance(
      ConfigurationReader configurationReader, KeywordManager keywordManager,
      GrammarManager grammarManager) throws IOException {
    DictionaryManager result = new DictionaryManager();
    result.setConfigurationReader(configurationReader);
    result.setGrammarManager(grammarManager);
    result.setKeywordManager(keywordManager);

    InputStream inputStream = configurationReader.getInputStream(
        configurationReader.getConfigurationMain().dictionaryFile);
    result.readDictionaryFile(inputStream);
    IOUtils.closeQuietly(inputStream);
    return result;
  }

  private ConfigurationReader configurationReader;

  private Map<String, DictionaryEntry> dicEntryMap =
      new HashMap<String, DictionaryEntry>();

  private GrammarManager grammarManager;
  private KeywordManager keywordManager;

  protected DictionaryManager() {
  }

  public Map<String, DictionaryEntry> getDictionaryEntryMap() {
    return dicEntryMap;
  }

  /**
   * Выполнить обработку статье.
   * 
   * ОБработка возможна только для записей типа "грамматика".
   * 
   * @param context
   * @param tokens
   * @return
   */
  public void processArticle(SentenceProcessingContext context,
      List<Token> tokens) {
    DictionaryEntry dictionaryEntry = dicEntryMap.get(context.getArticle());
    if (dictionaryEntry == null
        || dictionaryEntry.type != DictionaryEntryTypes.GRAMMAR)
      throw new IllegalStateException(
          "Wrong article type/unknown article: " + context.getArticle());
    grammarManager.processArticle(context, tokens);
  }

  @SuppressWarnings("unchecked")
  protected void readDictionaryFile(InputStream inputStream)
      throws IOException {
    JsonFactory jsonFactory = new JsonFactory();
    jsonFactory.configure(Feature.ALLOW_COMMENTS, true);
    ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    List<Map<String, Object>> entries =
        objectMapper.readValue(inputStream, ArrayList.class);
    for (Map<String, Object> entry : entries) {
      String type = (String) entry.get(KEY_ENTRY_TYPE_NAME);
      String name = (String) entry.get(KEY_ENTRY_NAME_NAME);
      if (name == null)
        throw new IllegalStateException(
            "Article with no name: " + entry.toString());
      if (dicEntryMap.containsKey(name))
        throw new IllegalStateException("Double article name: " + name);

      if (type.equalsIgnoreCase(KEY_ENTRY_TYPE_LIST)) {
        DictionaryEntry dicEntry = readListDictionaryEntry(name, entry);
        dicEntryMap.put(name, dicEntry);
      } else if (type.equalsIgnoreCase(KEY_ENTRY_TYPE_FILE)) {
        DictionaryEntry dicEntry = readFileDictionaryEntry(name, entry);
        dicEntryMap.put(name, dicEntry);
      } else if (type.equalsIgnoreCase(KEY_ENTRY_TYPE_GRAMMAR)) {
        DictionaryEntry dicEntry = readGrammarDictionaryEntry(name, entry);
        dicEntryMap.put(name, dicEntry);
      }
    }
  }

  private DictionaryEntry readFileDictionaryEntry(String name,
      Map<String, Object> entry) {
    String keywordsFile = (String) entry.get(KEY_ENTRY_KW_FILE);
    InputStream streamKW;
    List<String> keywords;
    try {
      streamKW = configurationReader.getInputStream(keywordsFile);
      keywords = IOUtils.readLines(streamKW);
    } catch (IOException e) {
      throw new IllegalStateException(String.format(
          "Ошибка при конфигурировании ключевых слов '%s' из словаря '%s'",
          name, keywordsFile));
    }
    IOUtils.closeQuietly(streamKW);
    String options = (String) entry.get(KEY_ENTRY_OPTIONS);
    for (int i = 0; i < keywords.size(); i++)
      keywords.set(i, keywords.get(i).trim());
    keywordManager.addKeywordSet(name, keywords, entry, options);

    DictionaryEntry result =
        new DictionaryEntry(name, DictionaryEntryTypes.FILE);
    try {
      result.fileAbsolutePath =
          configurationReader.getAbsolutePath(keywordsFile);
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(String.format(
          "Ошибка при конфигурировании ключевых слов '%s' из словаря '%s'",
          name, keywordsFile));
    }
    return result;
  }

  private DictionaryEntry readGrammarDictionaryEntry(String name,
      Map<String, Object> entry) {
    String grammarFile = (String) entry.get(KEY_ENTRY_GRAMMAR_FILE);
    try {
      grammarManager.loadGrammar(name, grammarFile);
    } catch (IOException e) {
      throw new IllegalStateException(String.format(
          "Ошибка при конфигурировании грамматики '%s' из словаря '%s'", name,
          grammarFile));
    }
    DictionaryEntry result =
        new DictionaryEntry(name, DictionaryEntryTypes.GRAMMAR);
    try {
      result.grammarAbsolutePath =
          configurationReader.getAbsolutePath(grammarFile);
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(String.format(
          "Ошибка при конфигурировании грамматики '%s' из словаря '%s'", name,
          grammarFile));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private DictionaryEntry readListDictionaryEntry(String name,
      Map<String, Object> entry) {
    List<String> keywords = (List<String>) entry.get(KEY_ENTRY_KEYWORDS);
    String options = (String) entry.get(KEY_ENTRY_OPTIONS);
    keywordManager.addKeywordSet(name, keywords, entry, options);
    return new DictionaryEntry(name, DictionaryEntryTypes.LIST);
  }

  public void setConfigurationReader(ConfigurationReader configurationReader) {
    this.configurationReader = configurationReader;
  }

  public void setGrammarManager(GrammarManager grammarManager) {
    this.grammarManager = grammarManager;
  }

  public void setKeywordManager(KeywordManager keywordManager) {
    this.keywordManager = keywordManager;
  }

}
