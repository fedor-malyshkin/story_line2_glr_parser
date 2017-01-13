package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.NameFinderImpl.FIOEntry;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManagerImpl;


public interface INameFinder {
	default void initialize(){}
	
	/**
	 * Определить вхождения имен/фамилий в текст.
	 * 
	 * Алгоритм в целом таков:
	 * <ol>
	 * <li>определяется - допустимо ли слово для поиска? {@link #isName(Token)}</li>
	 * <li>применяются шаблоны {@link #templates} и получаются потенциальные результаты (проверяются
	 * частные случаи)</li>
	 * <li>проверяются частные случаи</li>
	 * </ol>
	 * 
	 * @param tokens
	 * @return
	 */
	Collection<FIOEntry> detectFIOKeywordEntrances(List<Token> tokens);

	/**
	 * Осуществить предварительную обработку токенов, предназначенную для склейки в один токен
	 * инициалов, фамилий через тире и т.д.
	 * 
	 * @param tokens
	 */
	void preprocessTokens(List<Token> tokens);


	/**
	 * Выполнить объединение обнаруженных ранее имен в стандартный класс для keword'ов
	 * ({@link IKeywordEntrance}) с объединение вхождений в разнах формах, но покрывающих один и тот
	 * же фарагмент.
	 * 
	 * Шаги:
	 * <ol>
	 * <li>сортируем по по возрастанию первого токена и по возрастанию последнего токена</li>
	 * <li>идем с хвоста (итерированием), удаляем неполностью входящие и объединяем входящие</li>
	 * <li>при обнаружении нового начала - начинаем новую запись</li>
	 * <li>все перекрытия потом срежутся в
	 * {@link KeywordManagerImpl#calculateOptimalKeywordsCoverage(List, int)}</li>
	 * </ol>
	 * 
	 * 
	 * @param detectedFIO
	 * @return
	 */
	Collection<FIOKeywordEntrance> combineFoundNames(Collection<FIOEntry> detectedFIO);
}
