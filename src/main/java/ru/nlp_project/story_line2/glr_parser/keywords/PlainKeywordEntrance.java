package ru.nlp_project.story_line2.glr_parser.keywords;

import ru.nlp_project.story_line2.glr_parser.Token;

public class PlainKeywordEntrance implements IKeywordEntrance {
	/**
	 * Номер первого токена, с которого начинается покрытие для данного вхождения.
	 */
	int from;

	/**
	 * Concrete position in keyword set.
	 */
	int keywordPos;
	/**
	 * Идентификатор набора ключевых слов.
	 */
	int keywordSet;
	/**
	 * Длинна вхождения (в кол-ве токенов).
	 */
	int length;
	/**
	 * Индекс главного слова (0....) в выбранном вхождении.
	 */
	int mainWordNdx;
	/**
	 * Токен, соотвествующий главному слову и содержащий лексемы, соотвествующие ограничениям,
	 * которые возлагались на ключевое слово (gramm, gramm-X, agr).
	 */
	Token mainWordToken;
	/**
	 * Лемма для подстановки при обнаружении вхождения слова (может быть null).
	 */
	String substitutionlemm;
	public PlainKeywordEntrance(int from, int length, int keywordSet, int keywordPos,
			int mainWordNdx, Token mainWordToken) {
		this(from, length, keywordSet, keywordPos, mainWordNdx, mainWordToken, null);
	}

	public PlainKeywordEntrance(int from, int length, int keywordSet, int keywordPos,
			int mainWordNdx, Token mainWordToken, String lemm) {
		super();
		this.mainWordNdx = mainWordNdx;
		this.from = from;
		this.length = length;
		this.keywordSet = keywordSet;
		this.keywordPos = keywordPos;
		this.mainWordToken = mainWordToken;
		this.substitutionlemm = lemm;
	}


	@Override
	public int getFrom() {
		return from;
	}

	public int getKeywordPos() {
		return keywordPos;
	}

	public int getKeywordSet() {
		return keywordSet;
	}

	public String getSubstitutionLemm() {
		return substitutionlemm;
	}

	@Override
	public int getLength() {
		return length;

	}

	public int getMainWordNdx() {
		return mainWordNdx;
	}


	public Token getMainWordToken() {
		return mainWordToken;
	}

	@Override
	public String toString() {
		return "<" + from + ";" + length + ";" + keywordSet + ";" + keywordPos + "(" + mainWordNdx
				+ ")>";
	}

}
