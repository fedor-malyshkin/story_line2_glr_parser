package ru.nlp_project.story_line2.glr_parser;

import java.util.List;


public interface IInterpreter {
	void initialize();
	
	/**
	   * Main method.
	   * 
	   * Process each entrance (and associated parse tree), extract interpretation
	   * instructions, process instructions (check some pre-conditions) and generate
	   * facts.
	   * 
	   * @param sentenceStart
	   * @param grammarEntrances
	   * @param tokens
	   */
	void processEntrances(SentenceProcessingContext context,
			List<GrammarKeywordEntrance> grammarEntrances, List<Token> tokens);

}
