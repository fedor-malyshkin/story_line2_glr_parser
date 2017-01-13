package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.glr_parser.NameFinderImpl.FIOEntry;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager;
import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordEntrance;
import ru.nlp_project.story_line2.morph.MorphAnalyser;
import ru.nlp_project.story_line2.morph.SurnameAnalysisResult;
import ru.nlp_project.story_line2.morph.WordformAnalysisResult;

/**
 * Клаас для манипуляции с последовательностью токенов - в большинстве своем для модификации
 * последовательности на основании обнаруженных вхождений простых ключевых слов или деревьев
 * грамматик.
 * 
 * Also does initial text sequence splitting for tokens.
 *
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 *
 */
public class TokenManagerImpl implements ITokenManager {

	@Inject
	public TokenManagerImpl() {
		super();
	}

	class FIOKeywordToken extends Token {

		LinkedList<Token> originalTokens;

		private FIOKeywordToken() {
			super(0, 0, "", TokenTypes.COMBINED_FIO);
		}

		public FIOKeywordToken(int from, int length, String value, Collection<Token> tokens,
				FIOKeywordEntrance entrance, String kwName) {
			super(from, length, value, TokenTypes.COMBINED_FIO);
			this.originalTokens = new LinkedList<Token>(tokens);
			this.kw = entrance;
			this.kwName = kwName;
		}

		@Override
		public Token clone() {
			FIOKeywordToken result = new FIOKeywordToken(from, length, value, originalTokens,
					(FIOKeywordEntrance) kw, kwName);
			cloneAttributes(result);
			return result;
		}

		public FIOKeywordEntrance getFIOKeywordEntrance() {
			return (FIOKeywordEntrance) kw;
		}

	}

	/**
	 * Класс токенов на базе обнаруженных вхождений грамматик (комбинированный токен).
	 * 
	 * @author fedor
	 *
	 */
	class GrammarKeywordToken extends Token {

		LinkedList<Token> originalTokens;

		private GrammarKeywordToken() {
			super(0, 0, "", TokenTypes.COMBINED_GRAMMAR);
		}

		public GrammarKeywordToken(int from, int length, String value, Collection<Token> tokens,
				GrammarKeywordEntrance entrance, String kwName) {
			super(from, length, value, TokenTypes.COMBINED_GRAMMAR);
			this.originalTokens = new LinkedList<Token>(tokens);
			this.kw = entrance;
			this.kwName = kwName;
		}

		@Override
		public Token clone() {
			GrammarKeywordToken result = new GrammarKeywordToken(from, length, value,
					originalTokens, (GrammarKeywordEntrance) kw, kwName);
			cloneAttributes(result);
			return result;
		}

	}

	/**
	 * Класс токенов на базе обнаруженных вхождений простых ключевых слов (комбинированный токен).
	 * 
	 * @author fedor
	 *
	 */
	class PlainKeywordToken extends Token {

		LinkedList<Token> originalTokens;

		private PlainKeywordToken() {
			super(0, 0, null, TokenTypes.COMBINED_PLAIN);
		}

		public PlainKeywordToken(int from, int length, String value, Collection<Token> tokens,
				PlainKeywordEntrance entrance, String kwName) {
			super(from, length, value, TokenTypes.COMBINED_PLAIN);
			this.originalTokens = new LinkedList<Token>(tokens);
			this.kw = entrance;
			this.kwName = kwName;
		}

		@Override
		public Token clone() {
			PlainKeywordToken result = new PlainKeywordToken(from, length, value, originalTokens,
					(PlainKeywordEntrance) kw, kwName);
			cloneAttributes(result);
			return result;
		}
	}

	private static final char SIMPLE_TOKEN_SERIALIZATION_SEP = '_';
	private static MorphAnalyser morphAnalyser;
	public static String RUS_VOWELS = "уеыаоэюия";
	public static String RUS_CONSONANTS = "йцкнгшщзхфвпрлджчсмтб";
	private static char[] DELIMERS =
			{' ', ',', '.', '\'', '"', '!', '?', '-', ':', '~', '`', '@', '#', '$', '%', '^', '&',
					'*', '(', ')', '{', '}', '[', ']', '<', '>', ';', '|', '\\', '/', '«', '»'};
	private static char[] QUOTES = {'"', '`', '\'', '«', '»'};
	private static char[] LBRACKETS = {'(', '{', '[', '<'};
	private static char[] RBRACKETS = {')', '}', ']', '>'};
	private static char[] NUMBERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private static char[] WORD_DISALLOWED_SYMBOLS;

	public static MorphAnalyser getMorphAnalyser() {
		return morphAnalyser;
	}


	/**
	 * 
	 * Осуществить разделение входящего текста на фрагменты (аналогично способу в
	 * {@link #splitIntoTokens(String, boolean)}, но без токенов в результате).
	 * 
	 * @param text текст для разбиения
	 * @return
	 */
	public static List<String> splitIntoStrings(String text) {
		ArrayList<String> result = new ArrayList<>(10);
		int i = 0;
		StringBuffer sb = new StringBuffer();
		for (i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			boolean alphanumeric = StringUtils.isAlphanumeric(text.substring(i, i + 1));
			if (!alphanumeric) { // если нашли в потоке - символ, являющийся
									// разделителем
				// скинуть весь ранее накопленный текст (при наличии)
				if (sb.length() > 0)
					result.add(sb.toString());
				sb.setLength(0);

				// если разделитель - не пробел -- добавить его
				if (c != ' ')
					result.add("" + c);
			} else
				sb.append(c);
		}
		if (!StringUtils.isBlank(sb.toString()))
			result.add(sb.toString().trim());
		return result;
	}


	@Inject
	public IKeywordManager keywordManager = null;

	public boolean initMorph;
	@Inject
	public ConfigurationReader configurationReader;

	public TokenManagerImpl(boolean initMorph) {
		this.initMorph = initMorph;
	}


	protected void analyseTokenKeywords(Token token) {
		token.kwColon = token.value.equals(":");
		token.kwComma = token.value.equals(",");
		token.kwDollar = StringUtils.contains(token.value, '$');
		token.kwHyphen = StringUtils.containsOnly(token.value, '-');
		token.kwLBracket = StringUtils.containsOnly(token.value, TokenManagerImpl.LBRACKETS);
		token.kwRBracket = StringUtils.containsOnly(token.value, TokenManagerImpl.RBRACKETS);
		token.kwPercent = StringUtils.contains(token.value, '%');
		token.kwPlusSign = token.value.equals("+");
		token.kwPunct = token.value.equals(".");
		token.kwQuoteDbl = StringUtils.containsOnly(token.value, '"');
		token.kwQuoteSng = StringUtils.containsOnly(token.value, '"', '`', '\'');
		token.kwWord = !StringUtils.containsAny(token.value, WORD_DISALLOWED_SYMBOLS);
	}

	protected void analyseTokenRegistry(Token token) {
		// uReg
		if (StringUtils.isAllUpperCase(token.value) && !StringUtils.isNumeric(token.value))
			token.uReg = true;

		// lReg
		if (StringUtils.isAllLowerCase(token.value))
			token.lReg = true;
		// hReg1
		if (StringUtils.capitalize(token.value).equals(token.value)
				&& !StringUtils.isNumeric(token.value))
			token.hReg1 = true;
		// hReg2
		// все приводим к верхнему регистру - полученную строку используем как
		// множество, ищем вхождения в оригинальной строке символов из множества
		if (token.value.length() > 1) {
			String allUC = StringUtils.upperCase(token.value.substring(1));
			int lC = StringUtils.indexOfAny(token.value.substring(1), allUC);
			if (token.hReg1 && lC != -1)
				token.hReg2 = true;
		}
		// lat
		if (Pattern.matches("[A-Za-z]*", token.value))
			token.lat = true;
	}

	protected void analyseTokensKeywords(List<Token> list) {
		Iterator<Token> tokenIter = list.iterator();
		while (tokenIter.hasNext()) {
			Token token = tokenIter.next();
			analyseTokenKeywords(token);
		}
	}

	/**
	 * Выполнить анализ квотрирования токенов.
	 * 
	 * @param result
	 */
	protected void analyseTokensQuoting(List<Token> list) {
		for (int counter = 0; counter < list.size(); counter++) {
			// не обрабатываем токены-разделители
			if (list.get(counter).getType() == TokenTypes.DELIM)
				continue;
			boolean lQ = false, rQ = false;
			// l-quote
			char c = lookupTokenValueChar(list, counter - 1);
			int cPos = Arrays.binarySearch(QUOTES, c);
			if (cPos >= 0)
				lQ = true;
			// r-quote
			c = lookupTokenValueChar(list, counter + 1);
			cPos = Arrays.binarySearch(QUOTES, c);
			if (cPos >= 0)
				rQ = true;
			// checks
			if (lQ) {
				// list.remove(counter - 1);
				// counter--;
				list.get(counter).lQuoted = true;
			}
			if (rQ) {
				// list.remove(counter + 1);
				list.get(counter).rQuoted = true;
			}
			if (lQ && rQ) {
				list.get(counter).lQuoted = false;
				list.get(counter).rQuoted = false;
				list.get(counter).quoted = true;
			}
		}
	}

	protected void analyseTokensRegistry(List<Token> list) {
		for (int counter = 0; counter < list.size(); counter++)
			analyseTokenRegistry(list.get(counter));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.ITokenManager#assignAllLexemes(java.lang.String,
	 * ru.nlp_project.story_line2.glr_parser.Token)
	 */
	@Override
	public void assignAllLexemes(String opencorporaId, Token token) {
		Collection<WordformAnalysisResult> was =
				morphAnalyser.getWordformAnalysisResultsById(opencorporaId);
		token.removeLexemes(true, true);
		for (WordformAnalysisResult wa : was)
			token.addLexeme(wa.opencorporaId, wa.lemm, wa.value, wa.grammemes, wa.exactMatch);

	}

	@Deprecated
	public Token createDummyFIOKeywordToken(String value) {
		return new FIOKeywordToken(0, 0, value, Collections.emptyList(), null, null);
	}

	@Deprecated
	GrammarKeywordToken createDummyGrammarKeywordToken() {
		return new GrammarKeywordToken();
	}

	@Deprecated
	PlainKeywordToken createDummyPlainKeywordToken() {
		return new PlainKeywordToken();
	}

	@Deprecated
	public PlainKeywordToken createDummyPlainKeywordToken(String value) {
		return new PlainKeywordToken(0, 0, value, Collections.emptyList(), null, null);
	}

	/**
	 * 
	 * 
	 * @param tokens
	 * @param pkw
	 * @param diff смещение в нумерации токенов с учетом ранее созданных комбинированных токенов
	 *        (ИЗМЕОЯЕТСЯ В ТОКЕНАХ, ИСПОЛЬЗУЕТСЯ ДЛЯ ВЫБОРКИ).
	 * @return
	 */
	private Token createFIOKeywordToken(List<Token> tokens, FIOKeywordEntrance fkw, int diff) {
		List<Token> subList =
				tokens.subList(fkw.getFrom() + diff, fkw.getFrom() + fkw.getLength() + diff);

		// сделать список из всех токенов - даже если это другие
		// PlainKeywordToken/GrammarKeywordToken/FIOKeywordToken
		for (int i = 0; i < subList.size(); i++)
			unrollToken(subList, i);

		String kwName = "fio";
		// простейшая сериализация типа "Иванову_Александру"
		String serializationValue = generateSimpleTokenSerializationValue(subList);
		int length = subList.get(subList.size() - 1).getFrom()
				+ subList.get(subList.size() - 1).getLength() - subList.get(0).getFrom();
		FIOKeywordToken result = new FIOKeywordToken(subList.get(0).getFrom(), length,
				serializationValue, subList, fkw, kwName.toLowerCase());

		for (FIOEntry fio : fkw.getFIOs()) {
			// clone lexems
			result.addLexeme(fio.getLemmId(), fio.serializeCanonical(), fio.serialize(),
					fio.getGrammemes(), true);
		}

		// quotting
		boolean lQ = false, rQ = false;
		lQ = subList.get(0).lQuoted;
		lQ |= subList.get(0).quoted;
		result.lQuoted = lQ;

		rQ = subList.get(subList.size() - 1).rQuoted;
		rQ |= subList.get(subList.size() - 1).quoted;
		result.rQuoted = rQ;

		if (lQ && rQ) {
			result.lQuoted = false;
			result.rQuoted = false;
			result.quoted = true;
		}

		// registry
		analyseTokenRegistry(result);
		// keywords
		analyseTokenKeywords(result);
		return result;
	}

	/**
	 * @param tokens
	 * @param gkw
	 * @param diff смещение в нумерации токенов с учетом ранее созданных комбинированных токенов
	 *        (ИЗМЕОЯЕТСЯ В ТОКЕНАХ, ИСПОЛЬЗУЕТСЯ ДЛЯ ВЫБОРКИ).
	 * @return
	 */
	private Token createGrammarKeywordToken(List<Token> tokens, GrammarKeywordEntrance gkw,
			int diff) {
		List<Token> subList =
				tokens.subList(gkw.getFrom() + diff, gkw.getFrom() + gkw.getLength() + diff);

		// сделать список из всех токенов - даже если это другие
		// PlainKeywordToken/GrammarKeywordToken
		for (int i = 0; i < subList.size(); i++)
			unrollToken(subList, i);

		int length = subList.get(subList.size() - 1).getFrom()
				+ subList.get(subList.size() - 1).getLength() - subList.get(0).getFrom();
		String serializationValue = generateSimpleTokenSerializationValue(subList);
		GrammarKeywordToken result = new GrammarKeywordToken(subList.get(0).getFrom(), length,
				serializationValue, subList, gkw, gkw.getKwName().toLowerCase());

		// корневой узел уже обладает всеми лексемами главного слова дерева
		// (выполнено при валидации)
		ParseTreeNode mainWordNode = gkw.getParseTreeNode();
		// ... и присвоить ему оставшиеся лексемы
		for (Lexeme lexeme : mainWordNode.getLexemesListCopy())
			result.addLexeme(null, serializationValue, serializationValue, lexeme.grammemes,
					lexeme.exactMatch);

		return result;
	}

	/**
	 * 
	 * 
	 * @param tokens
	 * @param pkw
	 * @param diff смещение в нумерации токенов с учетом ранее созданных комбинированных токенов
	 *        (ИЗМЕРЯЕТСЯ В ТОКЕНАХ, ИСПОЛЬЗУЕТСЯ ДЛЯ ВЫБОРКИ).
	 * @return
	 */
	private Token createPlainKeywordToken(List<Token> tokens, PlainKeywordEntrance pkw, int diff) {
		List<Token> subList =
				tokens.subList(pkw.getFrom() + diff, pkw.getFrom() + pkw.getLength() + diff);

		// повторяем полное построение токена в случае наличия леммы для замены в ключевом слове
		// при этом все дочерние токены (даже если их было несколько) -- игнорируются
		// (и в методе "modifyTokensByKeywords" буду выброшены окончательно)
		if (null != pkw.getSubstitutionLemm()) {
			subList = new ArrayList<>();
			Token substToken = new Token(tokens.get(pkw.getFrom() + diff).getFrom(),
					pkw.getSubstitutionLemm().length(), pkw.getSubstitutionLemm(), TokenTypes.WORD);
			// TODO: т.к. фактически при указании "lemm" хотим указывать лемму для слова
			// добавить функционал поиска всех словоформ по лемме, а не по словоформе (как это
			// делается фактически сейчас)
			List<WordformAnalysisResult> analyseResults =
					morphAnalyser.analyse(pkw.getSubstitutionLemm().toLowerCase());
			for (WordformAnalysisResult wa : analyseResults)
				substToken.addLexeme(wa.opencorporaId, wa.lemm, wa.value, wa.grammemes,
						wa.exactMatch);
			subList.add(substToken);
		}

		// сделать список из всех токенов - даже если это другие
		// PlainKeywordToken/GrammarKeywordToken
		for (int i = 0; i < subList.size(); i++)
			unrollToken(subList, i);

		String kwName = keywordManager.getKeywordSetsNameByIndex(pkw.getKeywordSet());
		String serializationValue = pkw.getSubstitutionLemm();
		if (null == pkw.getSubstitutionLemm())
			serializationValue = generateSimpleTokenSerializationValue(subList);

		int length = subList.get(subList.size() - 1).getFrom()
				+ subList.get(subList.size() - 1).getLength() - subList.get(0).getFrom();
		PlainKeywordToken result = new PlainKeywordToken(subList.get(0).getFrom(), length,
				serializationValue, subList, pkw, kwName.toLowerCase());

		Token mainWordToken = pkw.getMainWordToken();
		// clone lexems
		for (Lexeme lexeme : mainWordToken.getLexemesListCopy())
			result.addLexeme(null, serializationValue, serializationValue, lexeme.grammemes,
					lexeme.exactMatch);

		// quotting
		boolean lQ = false, rQ = false;
		lQ = subList.get(0).lQuoted;
		lQ |= subList.get(0).quoted;
		result.lQuoted = lQ;

		rQ = subList.get(subList.size() - 1).rQuoted;
		rQ |= subList.get(subList.size() - 1).quoted;
		result.rQuoted = rQ;

		if (lQ && rQ) {
			result.lQuoted = false;
			result.rQuoted = false;
			result.quoted = true;
		}

		// registry
		analyseTokenRegistry(result);
		// keywords
		analyseTokenKeywords(result);
		return result;
	}

	private void generateDelimToken(List<Token> tokens, int from, int length, char delimer) {
		if (length > 0)
			tokens.add(new Token(from, length, "" + delimer, TokenTypes.DELIM));
	}

	/**
	 * Сгенерировать простейшее текстовое представление серии токенов (разделенное сепаратором,
	 * например знаком подчеркивания).
	 * 
	 * @param tokens
	 * @return
	 */
	private String generateSimpleTokenSerializationValue(List<Token> tokens) {
		return tokens.stream().map(t -> t.getValue())
				.collect(Collectors.joining(SIMPLE_TOKEN_SERIALIZATION_SEP + ""));
	}

	private void generateTextToken(List<Token> tokens, int from, int length, String text) {
		if (length > 0)
			tokens.add(new Token(from, length, text, TokenTypes.WORD));
	}

	@SuppressWarnings("deprecation")
	public void initialize() {
		Arrays.sort(DELIMERS);
		Arrays.sort(QUOTES);
		Arrays.sort(LBRACKETS);
		Arrays.sort(RBRACKETS);
		Arrays.sort(NUMBERS);
		WORD_DISALLOWED_SYMBOLS = ArrayUtils.addAll(DELIMERS, NUMBERS);
		// удалить допустимые элементы ("-" (слова, пишушищиеся через тире) и
		// "_" (комбинированные токены в которых сериализация сделана через
		// объединение с разделителем))
		WORD_DISALLOWED_SYMBOLS = ArrayUtils.removeElements(WORD_DISALLOWED_SYMBOLS, '-',
				SIMPLE_TOKEN_SERIALIZATION_SEP);
		Arrays.sort(WORD_DISALLOWED_SYMBOLS);

		try {
			if (initMorph && (GLRParser.invalidatedMorphDB || morphAnalyser == null)) {
				String morphZipDB = configurationReader.getConfigurationMain().morphZipDB;
				morphAnalyser = MorphAnalyser
						.newInstance(configurationReader.getInputStream(morphZipDB), true);
				GLRParser.invalidatedMorphDB = false;
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	/**
	 * Проверить значение токена по конкретной позиции в списке. В случае если значение укладывается
	 * в один символ - возвращаем значение. В случае если значение токен вообще тут есть -
	 * возвращаем значение. В остальных случаях возвращаем null.
	 * 
	 * @param list
	 * @param index
	 * @return
	 */
	private char lookupTokenValueChar(List<Token> list, int index) {
		try {
			Token token = list.get(index);
			if (token.value.length() != 1)
				return '\0';

			return token.value.charAt(0);
		} catch (Exception e) {
			return '\0';
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.ITokenManager#modifyTokensByKeywords(java.util.List,
	 * java.util.List)
	 */
	@Override
	public void modifyTokensByKeywords(List<Token> tokens,
			List<? extends IKeywordEntrance> keywordEntrances) {
		int diff = 0; // разница по сравнению с размером оригинального потока
						// токенов - каждый раз уменьшается при уменьшении размера
						// коллекции.

		for (IKeywordEntrance kw : keywordEntrances) {
			Token token = null;

			if (kw.getClass() == PlainKeywordEntrance.class) {
				PlainKeywordEntrance pkw = (PlainKeywordEntrance) kw;
				token = createPlainKeywordToken(tokens, pkw, diff);
			} else if (kw.getClass() == GrammarKeywordEntrance.class) {
				GrammarKeywordEntrance gkw = (GrammarKeywordEntrance) kw;
				token = createGrammarKeywordToken(tokens, gkw, diff);
			} else if (kw.getClass() == FIOKeywordEntrance.class) {
				FIOKeywordEntrance fkw = (FIOKeywordEntrance) kw;
				token = createFIOKeywordToken(tokens, fkw, diff);
			} else
				throw new IllegalStateException("Unknown keyword entrance class: " + kw.getClass());

			tokens.add(kw.getFrom() + diff, token);
			diff++;
			for (int i = 0; i < kw.getLength(); i++)
				tokens.remove(kw.getFrom() + diff);
			diff -= kw.getLength();

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.ITokenManager#predictAsSurnameAndFillLexemes(ru.
	 * nlp_project.story_line2.glr_parser.Token)
	 */
	@Override
	public List<SurnameAnalysisResult> predictAsSurnameAndFillLexemes(Token token) {
		return morphAnalyser.predictAsSurname(token.value.toLowerCase());
	}

	public void shutdown() {
		morphAnalyser.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.ITokenManager#splitIntoTokens(java.lang.String,
	 * boolean)
	 */
	@Override
	public List<Token> splitIntoTokens(String text, boolean addMorphInfo) {
		ArrayList<Token> result = new ArrayList<Token>(10);
		int last = 0;
		int i = 0;
		StringBuffer sb = new StringBuffer();
		for (i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			boolean alphanumeric = StringUtils.isAlphanumeric(text.substring(i, i + 1));
			if (!alphanumeric) { // если нашли в потоке - символ, являющийся
									// разделителем
				// скинуть весь ранее накопленный текст (при наличии)
				if (sb.length() > 0)
					generateTextToken(result, last, i - last, sb.toString());
				sb.setLength(0);

				// если разделитель - не пробел -- добавить его
				if (c != ' ')
					generateDelimToken(result, i, 1, c);
				last = i + 1;
			} else
				sb.append(c);
		}
		if (!StringUtils.isBlank(sb.toString()))
			generateTextToken(result, last, i - last, sb.toString());

		if (initMorph && addMorphInfo) {
			for (Token token : result) {
				List<WordformAnalysisResult> analyseResults =
						morphAnalyser.analyse(token.value.toLowerCase());
				for (WordformAnalysisResult wa : analyseResults)
					token.addLexeme(wa.opencorporaId, wa.lemm, wa.value, wa.grammemes,
							wa.exactMatch);
			} // for (GLRToken token : tokens) {
		}
		analyseTokensQuoting(result);
		analyseTokensRegistry(result);
		analyseTokensKeywords(result);
		return result;
	}

	/**
	 * Получить все реальные токены, с учетом того, что некоторые могут быть уже комбинированными -
	 * код это определяет и "разворачивет" их ("unroll").
	 * 
	 * @param list
	 * @param i
	 */
	private void unrollToken(List<Token> list, int i) {
		Token token = list.get(i);
		if (token.getClass() == PlainKeywordToken.class) {
			list.remove(i);
			PlainKeywordToken pkw = (PlainKeywordToken) token;
			list.addAll(i, pkw.originalTokens);
		} else if (token.getClass() == GrammarKeywordToken.class) {
			list.remove(i);
			GrammarKeywordToken gkw = (GrammarKeywordToken) token;
			list.addAll(i, gkw.originalTokens);
		} else if (token.getClass() == FIOKeywordToken.class) {
			list.remove(i);
			FIOKeywordToken fkw = (FIOKeywordToken) token;
			list.addAll(i, fkw.originalTokens);
		} else if (token.getClass() == Token.class) {
			// do nothing
		} else
			throw new IllegalStateException("Unknown token class: " + token.getClass());

	}
}
