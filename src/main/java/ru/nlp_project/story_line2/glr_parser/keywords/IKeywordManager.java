package ru.nlp_project.story_line2.glr_parser.keywords;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.nlp_project.story_line2.glr_parser.Token;


public interface IKeywordManager {
	default void initialize(){}
	
	/**
	 * Добавить набор ключевых слов (совместно с опциями) из
	 * {@link ru.nlp_project.story_line2.glr_parser.DictionaryManagerImpl}.
	 * 
	 * 
	 * @param keywordSetName имя набора
	 * @param keywords сами слова
	 * @param entry ассоциативный массив записи из словаря
	 * @param optionsRaw опции для всего набора ключевых слов (глобальные)
	 */
	void addKeywordSet(String keywordSetName, List<String> keywords, Map<String, Object> entry,
			String optionsRaw);

	/**
	 * Рассчитать оптимальное покрытие токенов (предложение) ключевыми словами (с целью максимизации
	 * покрытия).
	 * 
	 * <ul>
	 * <li>На вход подается отсортированный в порядке убывания список вхождений ключевых слов в
	 * предложение и кол-во токенов в предложении.</li>
	 * <li>Проход начинается с хвоста предложения к началу, при этом формируются все возможные
	 * варианты покрытия.</li>
	 * <li>Вариант покрытия - это возможная комибинация всех имеющихся отрезков, построенная таким
	 * образом, что они не перекрываются.</li>
	 * <li>По завершении выполняется поиск максимального и он выбирается в качестве
	 * оптимального.</li>
	 * <li>На текущий момент промежуточное удаление заранее не выигрышных вариантов покрытия не
	 * делается (хотя вся эта информация есть), т.к. их вычисление достаточно трудоемкая процедура и
	 * вполне возможно просто обменять время вычисления на излишнюю память на период
	 * вычисления.</li>
	 * </ul>
	 * 
	 * WARNING: очень дорогой алгоритм по по треблению к памяти - для 21 кол-во временных объектов
	 * может равняться 2 млн.!!!
	 * 
	 * @param kwes
	 * @param tokensLength
	 * @return
	 */
	List<? extends IKeywordEntrance> calculateOptimalKeywordsCoverage(
			List<? extends IKeywordEntrance> kwes, int tokensLength);

	/**
	 * Определить вхождения ключевых слов.
	 * 
	 * @param usedKeywordSets список наборов ключевых слов
	 * @param tokens токены для анализа
	 * @return список вхождений
	 */
	List<PlainKeywordEntrance> detectPlainKeywordEntrances(Collection<String> usedKeywordSets,
			List<Token> tokens);

	/**
	 * Определить вхождения ключевых слов.
	 * 
	 * @param keywordSetName наборов ключевых слов
	 * @param tokens токены для анализа
	 * @return список вхождений
	 */
	List<PlainKeywordEntrance> detectPlainKeywordEntrances(String keywordSetName,
			List<Token> tokens);

	Map<String, Object> getArgsByKeywordSetName(String keywordSetName);

	String getKeywordSetsNameByIndex(int index);

	/**
	 * Простая фильтрация вхождений.
	 * 
	 * 1) Сортировка по уменьшению площади покрытия (DESC). 2) Удаление сегментов, которые полностью
	 * входят в большие. 3) ПОвторять до тех пор пока не получится ничего удалять.
	 * 
	 * @param entrances
	 */
	void simpleCoverageFiltering(List<? extends IKeywordEntrance> entrances);

}
