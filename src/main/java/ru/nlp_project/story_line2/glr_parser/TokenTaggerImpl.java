package ru.nlp_project.story_line2.glr_parser;

import java.util.List;

import javax.inject.Inject;

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


	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}
	
	

}
