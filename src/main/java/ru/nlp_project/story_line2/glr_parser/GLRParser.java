package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.nlp_project.story_line2.glr_parser.dagger.ApplicationComponent;
import ru.nlp_project.story_line2.glr_parser.dagger.ApplicationModule;
import ru.nlp_project.story_line2.glr_parser.dagger.DaggerApplicationComponent;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager;
import ru.nlp_project.story_line2.token.ISentenceDetectorListener;
import ru.nlp_project.story_line2.token.SentenceDetector;

/**
 * Основной класс обработки входящей грамматики и текста.
 * 
 * О много поточности: много поточность определяется посредством запуска параллельно обработки
 * каждого предложения в {@link #processText(List, String)}. При этом необходимо учитывать, что все
 * компоненты анализа (те что создаются, конфигурируются и инициализируются в методе
 * {@link #initialize(boolean, IGLRLogger, IFactListener)}) не должны хранить своё состояние внутри
 * себя - лишь передавать структуры с состоянием между вызовами (т.е. быть stateless). <br/>
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 */
public class GLRParser {

	class DefaultFactListener implements IFactListener {

	}

	class DefaultGLRLogger implements IGLRLogger {

	}

	public class Sentence {

		public int getIndex() {
			return index;
		}

		public int getLength() {
			return length;
		}

		public int getStart() {
			return start;
		}

		public String getValue() {
			return value;
		}

		int index;
		int length;
		int start;
		String value;

		public Sentence(int index, int start, int length, String value) {
			super();
			this.index = index;
			this.start = start;
			this.length = length;
			this.value = value;
		}

		@Override
		public String toString() {
			return "[(" + index + ") " + start + ":" + length + " '" + value + "']";
		}



	}

	@Deprecated
	public static boolean invalidatedMorphDB = false;

	public static GLRParser newInstance(String configurationPath, boolean initMorph)
			throws IOException {
		return newInstance(configurationPath, null, null, initMorph, true);
	}

	public static GLRParser newInstance(String configurationPath, IGLRLogger logger2,
			IFactListener factListener2, boolean initMorph, boolean multiThread)
			throws IOException {
		GLRParser result = new GLRParser(multiThread);
		result.initialize(configurationPath, logger2, factListener2, initMorph);
		return result;
	}


	@Inject
	public IConfigurationManager configurationManager;
	@Inject
	public IDictionaryManager dictionaryManager;
	@Inject
	public IGrammarManager grammarManager;
	@Inject
	public IHierarchyManager hierarchyManager;
	@Inject
	public IInterpreter interpreter;
	@Inject
	public IKeywordManager keywordManager;
	@Inject
	public SentenceDetector sentenceDetector;
	@Inject
	public ITokenManager tokenManager;
	@Inject
	public ISentenceProcessorPool sentenceProcessorPool;
	@Inject
	public INameFinder nameFinder;
	private boolean multiThread;
	public static ApplicationComponent builder;

	public void shutdown() {
		tokenManager.shutdown();
	}

	private GLRParser(boolean multiThread) {
		this.multiThread = multiThread;
	}

	private synchronized void initialize(String configurationPath, IGLRLogger logger2,
			IFactListener factListener2, boolean initMorph) throws IOException {
		IGLRLogger logger = new DefaultGLRLogger();
		IFactListener factListener = new DefaultFactListener();
		if (logger2 != null)
			logger = logger2;
		if (factListener2 != null)
			factListener = factListener2;

		// inject components
		ApplicationModule applicationModule =
				new ApplicationModule(configurationPath, factListener, logger, initMorph);
		builder = DaggerApplicationComponent.builder().applicationModule(applicationModule).build();
		builder.inject(this);


		// configurationManager.initialize();
		// initialize components (configurationManager was initialized
		// in ApplicationModule.provideConfigurationManager)
		grammarManager.initialize();
		hierarchyManager.initialize();
		interpreter.initialize();
		keywordManager.initialize();
		tokenManager.initialize();
		sentenceProcessorPool.initialize();
		nameFinder.initialize();
		dictionaryManager.initialize();

	}

	/**
	 * 
	 * Выполнить обработку текста с использование конкретных статей.
	 * 
	 * @param articles список статей
	 * @param text
	 * @throws IOException
	 */
	public void processText(final List<String> articles, final String text) throws IOException {

		final List<Sentence> sentences = new ArrayList<Sentence>();
		sentenceDetector.process(text, new ISentenceDetectorListener() {
			int si = 0;

			@Override
			public void detectSentence(int start, int end) {
				sentences.add(new Sentence(si++, start, end - start, text.substring(start, end)));
			}

		});
		if (articles.isEmpty())
			articles.addAll(configurationManager.getMasterConfiguration().articles);

		if (multiThread)
			sentences.parallelStream()
					.forEach((s) -> sentenceProcessorPool.processSentence(articles, s));
		else
			sentences.stream().forEach((s) -> sentenceProcessorPool.processSentence(articles, s));
	}

	/**
	 * Выполнить обработку текста.
	 * 
	 * @param text
	 * @throws IOException
	 */
	public void processText(final String text) throws IOException {
		processText(new ArrayList<String>(), text);
	}


}
