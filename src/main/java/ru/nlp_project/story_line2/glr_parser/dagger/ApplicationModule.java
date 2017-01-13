package ru.nlp_project.story_line2.glr_parser.dagger;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.nlp_project.story_line2.glr_parser.ConfigurationReader;
import ru.nlp_project.story_line2.glr_parser.DictionaryManagerImpl;
import ru.nlp_project.story_line2.glr_parser.GrammarManagerImpl;
import ru.nlp_project.story_line2.glr_parser.HierarchyManagerImpl;
import ru.nlp_project.story_line2.glr_parser.IDictionaryManager;
import ru.nlp_project.story_line2.glr_parser.IFactListener;
import ru.nlp_project.story_line2.glr_parser.IGLRLogger;
import ru.nlp_project.story_line2.glr_parser.IGrammarManager;
import ru.nlp_project.story_line2.glr_parser.IHierarchyManager;
import ru.nlp_project.story_line2.glr_parser.IInterpreter;
import ru.nlp_project.story_line2.glr_parser.INameFinder;
import ru.nlp_project.story_line2.glr_parser.ISentenceProcessorPool;
import ru.nlp_project.story_line2.glr_parser.ITokenManager;
import ru.nlp_project.story_line2.glr_parser.InterpreterImpl;
import ru.nlp_project.story_line2.glr_parser.NameFinderImpl;
import ru.nlp_project.story_line2.glr_parser.SentenceProcessorPoolImpl;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager;
import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManagerImpl;
import ru.nlp_project.story_line2.token.SentenceDetector;

@Module
public class ApplicationModule {
	ConfigurationReader configurationReader;
	boolean initMorph;
	IFactListener factListener;
	IGLRLogger logger;

	public ApplicationModule(ConfigurationReader configurationReader, IFactListener factListener,
			IGLRLogger logger, boolean initMorph) {
		super();
		this.configurationReader = configurationReader;
		this.factListener = factListener;
		this.logger = logger;
		this.initMorph = initMorph;
	}

	@Provides
	@Singleton
	ConfigurationReader provideConfigurationReader() {
		return configurationReader;
	}

	@Provides
	@Singleton
	IDictionaryManager provideDictionaryManager(DictionaryManagerImpl instance) {
		return instance;
	}

	@Provides
	@Singleton
	IFactListener provideFactListener() {
		return factListener;
	}

	@Provides
	@Singleton
	IGLRLogger provideGLRLogger() {
		return logger;
	}

	@Provides
	@Singleton
	IGrammarManager provideGrammarManager(GrammarManagerImpl instance) {
		return instance;
	}

	@Provides
	@Singleton
	IHierarchyManager provideHierarchyManager(HierarchyManagerImpl instance) {
		return instance;
	}

	@Provides
	@Singleton
	IInterpreter provideInterpreter(InterpreterImpl interpreterImpl) {
		return interpreterImpl;
	}

	@Provides
	@Singleton
	IKeywordManager provideKeywordManager(KeywordManagerImpl instance) {
		return instance;
	}

	@Provides
	@Singleton
	INameFinder provideNameFinder(NameFinderImpl instance) {
		return instance;
	}


	@Provides
	@Singleton
	SentenceDetector provideSentenceDetector() {
		String sentenceData = configurationReader.getConfigurationMain().sentenceData;
		try {
			SentenceDetector instance =
					SentenceDetector.newInstance(configurationReader.getInputStream(sentenceData));
			return instance;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Provides
	@Singleton
	ISentenceProcessorPool provideSentenceProcessorPool(SentenceProcessorPoolImpl instance) {
		return instance;
	}

	@Provides
	@Singleton
	ITokenManager provideTokenManager(TokenManagerImpl instance) {
		instance.initMorph = initMorph;
		return instance;
	}



}
