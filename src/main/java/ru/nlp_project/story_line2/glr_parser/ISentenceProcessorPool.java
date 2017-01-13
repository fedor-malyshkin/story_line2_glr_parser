package ru.nlp_project.story_line2.glr_parser;

import java.util.List;

import ru.nlp_project.story_line2.glr_parser.GLRParser.Sentence;


public interface ISentenceProcessorPool {
	default void initialize(){}

	List<Token> generateTokens(String text);

	List<Token> generateTokens(String sentenceText, boolean addMorphInfo);

	void processSentence(List<String> articles, Sentence sentence);

}
