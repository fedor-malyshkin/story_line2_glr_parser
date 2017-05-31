package ru.nlp_project.story_line2.glr_parser.keywords;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.SymbolRestrictionChecker;
import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordTrieBuilder.KeywordInfo;
import ru.nlp_project.story_line2.morph.GrammemeEnum;

/**
 * MULTITHREAD_SAFE: FALSE
 * 
 * @author fedor
 *
 */
public class PlainKeywordEntranceDetector {
	class AnalyseThread {
		/**
		 * Текущий номер токена в предложении.
		 */
		int currTokenPos = 0;
		/**
		 * Предыдущий номер обнаруженного многословного слова из набора (используеся для многсловных
		 * ключевых слов)
		 */
		int prevMultiwordPart = 0;
		/**
		 * Предыдущий номер ключевого слова в наборе (используеся для многсловных ключевых слов)
		 */
		int keywordPos = 0;
		/**
		 * Текущий процесс поиска - поиск многословного ключа.
		 */
		boolean multiword = false;
		/**
		 * Предыдущие набранные токены.
		 */
		LinkedList<Token> atTokens;
		private boolean processed;

		public AnalyseThread(int currTokenPos) {
			super();
			this.currTokenPos = currTokenPos;
			atTokens = new LinkedList<Token>();
			atTokens.add(tokens.get(currTokenPos));
		}

		/**
		 * Новый поток анализа вхождения ключевого слова.
		 * 
		 * 
		 * @param currTokenPos позиция с которой необходимо начать анализ
		 * @param multiword - многословное ключевое слово
		 * @param keywordPos позиция в наборе ключевых слов
		 * @param prevMultiwordPart номер предыдущей части в многословном ключевом слове
		 * @param tokens токены для анализа
		 */
		public AnalyseThread(int currTokenPos, boolean multiword, int keywordPos,
				int prevMultiwordPart, LinkedList<Token> tokens) {
			super();
			this.currTokenPos = currTokenPos;
			this.prevMultiwordPart = prevMultiwordPart;
			this.keywordPos = keywordPos;
			this.multiword = multiword;
			this.atTokens = tokens;
			this.processed = false;
		}

		@Override
		public String toString() {
			return "AnalyseThread [currTokenPos=" + currTokenPos + ", keywordPos=" + keywordPos
					+ ", multiword=" + multiword + ", prevMultiwordPart=" + prevMultiwordPart
					+ ", tokens=" + atTokens + ", processed=" + processed + "]";
		}

	}

	private PlainKeywordTrieReader reader;
	private ArrayList<PlainKeywordEntrance> results;
	private ArrayDeque<AnalyseThread> analyseThreads;
	private List<Token> tokens;

	public PlainKeywordEntranceDetector(PlainKeywordTrie trie, List<Token> tokens) {
		this.reader = new PlainKeywordTrieReader(trie);
		this.tokens = tokens;
		this.results = new ArrayList<PlainKeywordEntrance>();
		this.analyseThreads = new ArrayDeque<PlainKeywordEntranceDetector.AnalyseThread>();
	}

	/**
	 * Выполнить поиск возможных покрытий ключевыми словами.
	 * 
	 * Принцип работы:
	 * <ul>
	 * <li>Из стека извлекается текущий результат поиска и копируется для всех омонимов, базовых
	 * форм слова (лемм) и оригинальных форм (кроме того им передается пустой результат анализа для
	 * возможного начала нового вхождения);</li>
	 * <li>Каждая копия проверяется на вхождение в trie - в случае вхождения проверяется результат
	 * данных (особая проверка для многословных ключевых слов);</li>
	 * <li>В случае вхождения результат анализа изменяется и помещается в стек;</li>
	 * <li>В случае не_вхождения - ничего не делается и как результат вариант анализа выкидывается
	 * из процесса поиска;</li>
	 * <li>Действия повторяются до тех пор пока в стеке что-то есть.</li>
	 * </ul>
	 * 
	 * @param originalTokens
	 * @return
	 */
	public List<PlainKeywordEntrance> detectPlainKeywordEntrances() {
		for (int i = 0; i < tokens.size(); i++) {
			cloneSearchFor(i);
			while (!analyseThreads.isEmpty()) {
				AnalyseThread analyseThread = analyseThreads.pollLast();
				cloneSearchFor(analyseThread);
			}
		}

		return this.results;
	}

	/**
	 * Создать копии анализа для всех базовых форм омонимов (лемм) и оригинальных форм токена и
	 * выполнить поиск.
	 * 
	 * @param originalTokens
	 * @param tokens2
	 * @param analyseThread
	 */
	private void cloneSearchFor(AnalyseThread analyseThread) {
		Token token = tokens.get(analyseThread.currTokenPos);
		for (Lexeme lexem : token.getLexemesListCopy()) {
			List<PlainKeywordTrieBuilder.KeywordInfo> list = reader.analyse(lexem.getLemm());
			if (list.size() > 0 && !analyseThread.processed)
				analyseSearchResults(list, analyseThread, true);
		}
		List<PlainKeywordTrieBuilder.KeywordInfo> list = reader.analyse(token.getValue());
		if (list.size() > 0 && !analyseThread.processed)
			analyseSearchResults(list, analyseThread, false);

	}

	/**
	 * Выполнить анализ вхождения в trie для всех базовых форм омонимов (лемм) и оригинальных форм
	 * токена и выполнить поиск.
	 * 
	 * @param originalTokens
	 * @param i
	 */
	private void cloneSearchFor(int pos) {
		Token token = tokens.get(pos);
		// исключаем повторный анализ токено - ранее выделенных граммаиками/другими
		// ключевыми словами или иными способами
		if (!token.getClass().equals(Token.class))
			return;
		// поиск базовых форм (лемм)
		for (Lexeme lexem : token.getLexemesListCopy()) {
			List<PlainKeywordTrieBuilder.KeywordInfo> list = reader.analyse(lexem.getLemm().toLowerCase());
			if (list.size() > 0)
				analyseSearchResults(list, new AnalyseThread(pos), true);
		}
		// напоследок поиск оригинальной формы
		List<PlainKeywordTrieBuilder.KeywordInfo> list = reader.analyse(token.getValue().toLowerCase());
		if (list.size() > 0)
			analyseSearchResults(list, new AnalyseThread(pos), false);
	}

	/**
	 * 
	 * Осуществить анализ результатов.
	 * 
	 * Анализируем все полученные результатаы и... в случае однословного ключа - просто добавляем
	 * результат вхождения; в случае многословного ключа -
	 * 
	 * 
	 * @param analyseResults результаты совпадений с trie
	 * @param aT новый поток с данными
	 * @param lemm признак того, что при поиске использовалась лемма
	 */
	private void analyseSearchResults(List<PlainKeywordTrieBuilder.KeywordInfo> analyseResults,
			AnalyseThread aT, boolean lemm) {
		for (KeywordInfo kwi : analyseResults) {

			// если слово не проходит валидацию - данный результат нам не нужен
			if (!validateSingleWordSearchResult(kwi, aT, lemm))
				continue;

			if (kwi.multiword) {
				// multiword (create new tracking)
				if (kwi.multiwordPart == 0) {
					// clone tokens from prev analyse thread
					LinkedList<Token> newAtTokens = new LinkedList<Token>();
					for (Token t : aT.atTokens)
						newAtTokens.add(t.clone());

					AnalyseThread newAnalyseThread = new AnalyseThread(aT.currTokenPos + 1, true,
							kwi.keywordPos, 0, newAtTokens);
					analyseThreads.add(newAnalyseThread);
					continue;
				}
				// multiword (continue/finish collect mw)
				if (aT.multiword && aT.keywordPos == kwi.keywordPos
						&& aT.prevMultiwordPart == kwi.multiwordPart - 1) {

					// has to continue
					if (kwi.hasContinue) {
						// clone tokens from prev analyse thread
						LinkedList<Token> newAtTokens = new LinkedList<Token>();
						for (Token t : aT.atTokens)
							newAtTokens.add(t.clone());
						// add clone of cuurent checked token
						newAtTokens.add(tokens.get(aT.currTokenPos).clone());

						analyseThreads.add(new AnalyseThread(aT.currTokenPos + 1, true,
								aT.keywordPos, kwi.multiwordPart, newAtTokens));
					} else {
						// finish
						// clone tokens from prev analyse thread
						LinkedList<Token> newAtTokens = new LinkedList<Token>();
						for (Token t : aT.atTokens)
							newAtTokens.add(t.clone());
						// add clone of cuurent checked token
						newAtTokens.add(tokens.get(aT.currTokenPos).clone());
						aT.atTokens = newAtTokens;

						// если многословное ключевое слово не проходит валидацию - данный
						// результат нам не нужен
						if (!chechRestrictions(kwi, aT))
							continue;

						results.add(new PlainKeywordEntrance(
								aT.currTokenPos - aT.atTokens.size() + 1, aT.atTokens.size(),
								kwi.keywordSet, aT.keywordPos, kwi.options.mainWord,
								newAtTokens.get(kwi.options.mainWord), kwi.options.lemm));
						// больше не обрабатываем данный вариант анализа
						aT.processed = true;
					}

					continue;
				}
			} else { // if (kwi.multiword) {
				// single word - просто создаем результат и все
				results.add(
						new PlainKeywordEntrance(aT.currTokenPos, 1, kwi.keywordSet, kwi.keywordPos,
								0, aT.atTokens.get(kwi.options.mainWord), kwi.options.lemm));
				// больше не обрабатываем данный вариант анализа
				aT.processed = true;
			}
		}
	}

	private boolean chechRestrictions(KeywordInfo kwi, AnalyseThread analyseThread) {
		LookupOptions options = kwi.options;

		// agr - skip
		if (options.agr != null && !isAgree(analyseThread.atTokens, options.agr))
			return false;
		// exactForm - skip
		// gramm - skip;
		// grammTree; - skip
		if (options.grammTree != null)
			for (Entry<Integer, Set<GrammemeEnum>> entry : options.grammTree.entrySet()) {
				Token token = analyseThread.atTokens.get(entry.getKey());
				if (!SymbolRestrictionChecker.matchGram(token, entry.getValue()))
					return false;
				SymbolRestrictionChecker.removeUnmatchingLexemesGram(token, entry.getValue());
				if (!token.hasLexemes())
					return false;
			}
		// mainWord = 0; - skip
		// upperCase - skip
		return true;
	}

	private boolean isAgree(LinkedList<Token> tokens, SymbolExtDataTypes agr) {
		throw new IllegalStateException("NIY");
	}

	/**
	 * 
	 * Выполнить проверку соответствия данных имеющимся опциям (для одного независимого слова в
	 * отдельности, т.е. часть проверок, допустимых лишь для многословных ключевых слов --
	 * пропускаем).
	 * 
	 * 
	 * @param kwi
	 * @param analyseThread
	 * @param lemm
	 * @return
	 */
	private boolean validateSingleWordSearchResult(KeywordInfo kwi, AnalyseThread analyseThread,
			boolean lemm) {
		LookupOptions options = kwi.options;

		// agr - skip
		// exactForm
		if (options.exactForm && lemm)
			return false;
		// gramm;
		if (options.gramm != null) {
			Token token = analyseThread.atTokens.getLast();
			if (!SymbolRestrictionChecker.matchGram(token, options.gramm))
				return false;
			SymbolRestrictionChecker.removeUnmatchingLexemesGram(token, options.gramm);
			if (!token.hasLexemes())
				return false;
		}
		// grammTree; - skip
		// mainWord = 0; - skip
		// upperCase
		if (options.upperCase) {
			Token token = analyseThread.atTokens.getLast();
			if (!StringUtils.isAllUpperCase(token.getValue()))
				return false;
		}

		return true;
	}

}
