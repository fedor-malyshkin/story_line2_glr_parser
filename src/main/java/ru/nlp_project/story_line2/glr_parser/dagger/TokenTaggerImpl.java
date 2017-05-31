package ru.nlp_project.story_line2.glr_parser.dagger;

import java.util.List;

import javax.inject.Inject;

import ru.nlp_project.story_line2.glr_parser.IConfigurationManager;
import ru.nlp_project.story_line2.glr_parser.ITokenTagger;
import ru.nlp_project.story_line2.glr_parser.Token;

public class TokenTaggerImpl implements ITokenTagger {
	@Inject
	public IConfigurationManager configurationManager;

	@Inject
	public TokenTaggerImpl() {
		super();
	}


	@Override
	public void processTokens(List<Token> tokens) {
		// TODO Auto-generated method stub

	}

}
