package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import ru.nlp_project.story_line2.config.ConfigurationException;
import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.DictionaryConfiguration;
import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.DictionaryConfigurationEntry;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager;

/**
 * 
 * Основной класс для работы с газеттиром/словарем, парсинг, пополнение ключевых слов, передача
 * данных в менеджер грамматик, вычисление зависимостей и т.д...
 * 
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 *
 */
public class DictionaryManagerImpl implements IDictionaryManager {


	@Inject
	public DictionaryManagerImpl() {
		super();
	}

	public class DictionaryEntry {
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

	private static final String KEY_ENTRY_TYPE_FILE = "file";
	private static final String KEY_ENTRY_TYPE_GRAMMAR = "grammar";
	private static final String KEY_ENTRY_TYPE_LIST = "list";

	public void initialize() {
		readDictionaryFile();
		grammarManager.analyseUsedKewordSets();
	}

	@Inject
	public IConfigurationManager configurationManager;
	private Map<String, DictionaryEntry> dicEntryMap = new HashMap<String, DictionaryEntry>();
	@Inject
	public IGrammarManager grammarManager;
	@Inject
	public IKeywordManager keywordManager;


	public Map<String, DictionaryEntry> getDictionaryEntryMap() {
		return dicEntryMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.IDictionaryManager#processArticle(ru.nlp_project.
	 * story_line2.glr_parser.SentenceProcessingContext, java.util.List)
	 */
	@Override
	public void processArticle(SentenceProcessingContext context, List<Token> tokens) {
		DictionaryEntry dictionaryEntry = dicEntryMap.get(context.getArticle());
		if (dictionaryEntry == null || dictionaryEntry.type != DictionaryEntryTypes.GRAMMAR)
			throw new IllegalStateException(
					"Wrong article type/unknown article: " + context.getArticle());
		grammarManager.processArticle(context, tokens);
	}

	protected void readDictionaryFile() {
		DictionaryConfiguration configuration = configurationManager.getDictionaryConfiguration();

		for (DictionaryConfigurationEntry entry : configuration.dictionaryEntries) {
			if (entry.name == null)
				throw new IllegalStateException("Article with no name: " + entry.toString());
			if (dicEntryMap.containsKey(entry.name))
				throw new IllegalStateException("Double article name: " + entry.name);

			if (entry.type.equalsIgnoreCase(KEY_ENTRY_TYPE_LIST)) {
				DictionaryEntry dicEntry = readKeywordsListDictionaryEntry(entry.name, entry);
				dicEntryMap.put(entry.name, dicEntry);
			} else if (entry.type.equalsIgnoreCase(KEY_ENTRY_TYPE_FILE)) {
				DictionaryEntry dicEntry = readFileDictionaryEntry(entry.name, entry);
				dicEntryMap.put(entry.name, dicEntry);
			} else if (entry.type.equalsIgnoreCase(KEY_ENTRY_TYPE_GRAMMAR)) {
				DictionaryEntry dicEntry = readGrammarDictionaryEntry(entry.name, entry);
				dicEntryMap.put(entry.name, dicEntry);
			}
		}
	}

	private DictionaryEntry readFileDictionaryEntry(String name,
			DictionaryConfigurationEntry entry) {
		if (entry.keywordsFile == null)
			throw new IllegalStateException(String.format(
					"Не указан параметр 'keywords_file' для набора ключевых слов '%s'", name));
		String keywordsFile = entry.keywordsFile;
		List<String> keywords;
		try {
			InputStream is = configurationManager.getSiblingInputStream(keywordsFile);
			keywords = IOUtils.readLines(is);
			IOUtils.closeQuietly(is);
		} catch (IOException | ConfigurationException e) {
			throw new IllegalStateException(
					String.format("Ошибка при конфигурировании ключевых слов '%s' из словаря '%s'",
							name, keywordsFile),
					e);
		}
		for (int i = 0; i < keywords.size(); i++)
			keywords.set(i, keywords.get(i).trim());
		keywordManager.addKeywordSet(name, keywords, entry.options);

		DictionaryEntry result = new DictionaryEntry(name, DictionaryEntryTypes.FILE);
		return result;
	}

	private DictionaryEntry readGrammarDictionaryEntry(String name,
			DictionaryConfigurationEntry entry) {
		try {
			grammarManager.loadGrammar(name, entry.grammarFile);
		} catch (IOException | ConfigurationException e) {
			throw new IllegalStateException(
					String.format("Ошибка при конфигурировании грамматики '%s' из словаря '%s'",
							name, entry.grammarFile));
		}
		DictionaryEntry result = new DictionaryEntry(name, DictionaryEntryTypes.GRAMMAR);
		return result;
	}

	private DictionaryEntry readKeywordsListDictionaryEntry(String name,
			DictionaryConfigurationEntry entry) {
		List<String> keywords = entry.keywords;
		String options = entry.options;
		keywordManager.addKeywordSet(name, keywords, options);
		return new DictionaryEntry(name, DictionaryEntryTypes.LIST);
	}

}
