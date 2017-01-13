package ru.nlp_project.story_line2.glr_parser;

import java.util.List;


public interface IDictionaryManager {
	void initialize();
	/**
	   * Выполнить обработку статье.
	   * 
	   * ОБработка возможна только для записей типа "грамматика".
	   * 
	   * @param context
	   * @param tokens
	   * @return
	   */
	void processArticle(SentenceProcessingContext context, List<Token> tokens);

}
