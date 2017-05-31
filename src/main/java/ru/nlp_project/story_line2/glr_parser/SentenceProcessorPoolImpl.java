package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import ru.nlp_project.story_line2.glr_parser.GLRParser.Sentence;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager;

public class SentenceProcessorPoolImpl implements ISentenceProcessorPool {
	@Inject
	public SentenceProcessorPoolImpl() {
		super();
	}

	@Inject
	public IDictionaryManager dictionaryManager;
	@Inject
	public IFactListener factListener;
	@Inject
	public IGLRLogger logger;
	@Inject
	public ITokenManager tokenManager;
	@Inject
	public INameFinder nameFinder;
	@Inject
	public IKeywordManager keywordManager;
	@Inject
	public ITokenTagger tokenTagger;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.ISentenceProcessorPool#generateTokens(java.lang.String)
	 */
	@Override
	public List<Token> generateTokens(final String text) {
		return generateTokens(text, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.ISentenceProcessorPool#generateTokens(java.lang.String,
	 * boolean)
	 */
	@Override
	public List<Token> generateTokens(final String sentenceText, boolean addMorphInfo) {
		return tokenManager.splitIntoTokens(sentenceText, addMorphInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.ISentenceProcessorPool#processSentence(java.util.List,
	 * ru.nlp_project.story_line2.glr_parser.GLRParser.Sentence)
	 */
	@Override
	public void processSentence(List<String> articles, Sentence sentence) {
		logger.startSentenceProcessing(sentence, articles);
		List<Token> tokens = tokenManager.splitIntoTokens(sentence.value, true);
		logger.tokensGenerated(tokens);
		// запустить таггер
		tokenTagger.processTokens(tokens);
		logger.tokensTaggerProcessed(tokens);
		// произвести предварительную обработку (частичную склейку) токенов
		nameFinder.preprocessTokens(tokens);
		logger.tokenNamesGenerated(tokens);
		// поиск имен - один раз для всех статей
		processNames(tokens);

		// for each article
		for (String article : articles) {
			SentenceProcessingContext context =
					SentenceProcessingContext.create(article, sentence, factListener, logger);
			dictionaryManager.processArticle(context, tokens);
		}
		logger.endSentenceProcessing(sentence, articles);
	}

	private void processNames(List<Token> tokens) {
		Collection<NameFinderImpl.FIOEntry> detectedFIO =
				nameFinder.detectFIOKeywordEntrances(tokens);
		List<IKeywordEntrance> entrances = new LinkedList<IKeywordEntrance>();
		entrances.addAll(nameFinder.combineFoundNames(detectedFIO));

		logger.detectedUnoptimizedKwEntrances(null, entrances);

		if (entrances.isEmpty())
			return;

		// calculate optimal (if exist entrances)
		List<? extends IKeywordEntrance> optimalCoverage =
				keywordManager.calculateOptimalKeywordsCoverage(entrances, tokens.size());

		// modify tokens in case were found other entrances (plan-kw/grammar-kw)
		if (optimalCoverage != null && !optimalCoverage.isEmpty()) {
			logger.detectedOptimalKwEntrances(null, optimalCoverage);
			tokenManager.modifyTokensByKeywords(tokens, optimalCoverage);
			logger.tokensModified(null, tokens);
		}
	}


}
