package ru.nlp_project.story_line2.glr_parser;

import java.util.List;

import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.GrammarKeywordToken;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.PlainKeywordToken;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.morph.SurnameAnalysisResult;


public interface ITokenManager {
	void initialize();

	void assignAllLexemes(String opencorporaId, Token token);

	/**
	 * Выполнить анализ поданной коллекции токенов и изменить её с учетом имеющихся вхождений
	 * различных типов вхождений {@link IKeywordEntrance} и заменой их на соответствующи экземпляры
	 * {@link PlainKeywordToken} и {@link GrammarKeywordToken}.
	 * 
	 * При создании {@link PlainKeywordToken} и {@link GrammarKeywordToken} - необходимые данные о
	 * лексеммах копируются с главных слов.
	 * 
	 * !!! - Делается важное допущение о том. что entrance отсортированы по возрастанию.
	 * 
	 * Пример: "Ел морковку у африканского слона 29 декабря 2011 года." После применения kwtype’ов
	 * наша грамматика получит следующий текст: "Ел морковку у африканского_слона
	 * 29_декабря_2011_года."
	 * 
	 * 
	 * @param tokens
	 * @param keywordEntrances
	 */
	void modifyTokensByKeywords(List<Token> tokens,
			List<? extends IKeywordEntrance> keywordEntrances);

	/**
	 * 
	 * Осуществить разделение входящего текста на токены. В последующим выполнить анализ теста и
	 * окружения для дополнения расширенной информацией.
	 * 
	 * @param text текст для разбиения
	 * @param addMorphInfo выполнить пополнение морфологической информаией.
	 * @return
	 */
	List<Token> splitIntoTokens(String text, boolean addMorphInfo);

	List<SurnameAnalysisResult> predictAsSurnameAndFillLexemes(Token token);

	void shutdown();

}
