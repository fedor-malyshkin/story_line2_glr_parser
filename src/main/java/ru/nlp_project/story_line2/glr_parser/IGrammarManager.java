package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.util.List;

import ru.nlp_project.story_line2.config.ConfigurationException;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;


public interface IGrammarManager {
	default void initialize() {}

	/**
	 * Выполнить анализ грамматик для определения зависимых keyword'ов. Обязательный для выполнения
	 * метод перед использованием.
	 * 
	 * WARN: Методел должен и может быть вызван после иницициализации менеджера словарей
	 * ({@link IDictionaryManager}) (т.к. он загружеает в менеджера словарей необходимые данные).
	 *
	 */
	void analyseUsedKewordSets();

	List<GrammarKeywordEntrance> processArticle(SentenceProcessingContext context,
			List<Token> tokens);

	/**
	 * Отработать статью грамматики.
	 * 
	 * С результатами работы грамматик на предыдущих уровнях обходимся как со всеми
	 * {@link IKeywordEntrance} (т.е. вычислять оптимальное покрытие, что приводит к тому, что
	 * найденные деревья с меньшим покрытием - игнорируются, из чего следует необходимость
	 * несмешивать деревьяс разными назначениями в одной грамматике), потом все привязывать к
	 * токенам ({@link Token}) и отдавать основной грамматике.
	 * 
	 * 
	 * Результаты основной грамматики в виде {@link IKeywordEntrance} передавать интерпретатору для
	 * извлечения фактов.
	 * 
	 * @param context
	 * @param tokens
	 * @return
	 */
	List<GrammarKeywordEntrance> processArticle(SentenceProcessingContext context,
			List<Token> tokens, boolean interpretate);


	/**
	 * 
	 * Вызывается при прочтении конфигурационного файла (т.е. достаточно заранее до вызова
	 * парсинга).
	 * 
	 * При этом не вся информация о статьях может иметься в наличии.
	 * 
	 * @param articleName
	 * @param grammarPath
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	void loadGrammar(String articleName, String grammarPath)
			throws IOException, ConfigurationException;

}
