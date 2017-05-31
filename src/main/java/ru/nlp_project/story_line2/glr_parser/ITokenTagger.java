package ru.nlp_project.story_line2.glr_parser;

import java.util.List;

public interface ITokenTagger {

	void processTokens(List<Token> tokens);

	void initialize();

}
