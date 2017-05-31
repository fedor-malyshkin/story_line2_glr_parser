package ru.nlp_project.story_line2.glr_parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.morph.GrammemeEnum;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.Grammemes;
import ru.nlp_project.story_line2.morph.MorphAnalyser;
import ru.nlp_project.story_line2.morph.SurnameAnalysisResult;
import ru.nlp_project.story_line2.morph.WordformAnalysisResult;

/**
 * @author fedor
 *
 */
public class NameFinderImpl implements INameFinder {

	@Inject
	public NameFinderImpl() {
		super();
	}

	/**
	 * ВОзможные части FIO во вхождениях.
	 * 
	 * @author fedor
	 *
	 */
	enum EntranceFIOPartTypes {
		Name, Surname, Patronomyc, InitialName, InitialPatronomyc
	}

	/**
	 * Возможные типы вхождений FIO в текст.
	 * 
	 * @author fedor
	 */
	enum EntranceFIOTypes {
		FIO, IO, IO_In, IO_InIn, FI, IF_In, FI_In, I, F, I_In
	}

	class FIOEntry {
		private FIOTemplate foundBy;

		private Grammemes grammemes;

		private Token name;
		private Token surname;
		private Token patronomyc;

		private boolean nameInitials;
		private boolean surnameInitials;
		private boolean patronomycInitials;

		private String nameLemm;
		private String surnameLemm;
		private String patronomycLemm;

		private Lexeme nameLexeme;
		private Lexeme surnameLexeme;
		private Lexeme patronomycLexeme;

		private int lastTokenIndex = Integer.MIN_VALUE;
		private int firstTokenIndex = Integer.MAX_VALUE;

		private boolean surnamePredicted = false;

		public FIOEntry(FIOTemplate foundBy) {
			this.foundBy = foundBy;
		}

		public FIOEntry(FIOEntry entrance) {
			cloneAttributesTo(entrance);
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			FIOEntry result = new FIOEntry(this.foundBy);
			result.cloneAttributesTo(this);
			return result;
		}

		/**
		 * Осуществить копирование аттрибутов обхекта в объект-параметр.
		 * 
		 * @param other объект-параметр для копирования в него.
		 */
		private void cloneAttributesTo(FIOEntry other) {
			this.foundBy = other.foundBy;
			this.surnamePredicted = other.surnamePredicted;

			if (other.grammemes != null)
				this.grammemes = new Grammemes(other.grammemes);

			this.firstTokenIndex = other.firstTokenIndex;
			this.lastTokenIndex = other.lastTokenIndex;

			this.name = other.name;
			this.surname = other.surname;
			this.patronomyc = other.patronomyc;

			this.nameInitials = other.nameInitials;
			this.surnameInitials = other.surnameInitials;
			this.patronomycInitials = other.patronomycInitials;

			this.nameLemm = other.nameLemm;
			this.surnameLemm = other.surnameLemm;
			this.patronomycLemm = other.patronomycLemm;

			this.nameLexeme = other.nameLexeme != null ? other.nameLexeme.clone() : null;
			this.surnameLexeme = other.surnameLexeme != null ? other.surnameLexeme.clone() : null;
			this.patronomycLexeme =
					other.patronomycLexeme != null ? other.patronomycLexeme.clone() : null;
		}

		/**
		 * Получить соотвествующее представление в канонической форме (GNC), но с родом определенном
		 * при анализе.
		 * 
		 * 
		 * @param lexeme
		 * @param gramm
		 * @param predictedSurname
		 * @return
		 */
		private String getCompatibleLexemePresentation(Lexeme lexeme, Grammemes gramm,
				boolean predictedSurname) {
			// в некоторых случаях можем прийти сюда с уже срогнозированной
			// фамилией
			if (lexeme.getId() == null)
				return lexeme.lexemeValue;

			MorphAnalyser morphAnalyser = TokenManagerImpl.getMorphAnalyser();
			Collection<WordformAnalysisResult> wordformAnalysisResults =
					morphAnalyser.getWordformAnalysisResultsById(lexeme.getId());
			Iterator<WordformAnalysisResult> wfIter = wordformAnalysisResults.iterator();
			while (wfIter.hasNext()) {
				WordformAnalysisResult wf = wfIter.next();
				Grammemes grammemes2 = wf.grammemes;
				if (grammemes2.has(gramm.getByGrammemeGroupIndex(GrammemeUtils.CASE_NDX))
						&& grammemes2.has(gramm.getByGrammemeGroupIndex(GrammemeUtils.NMBR_NDX))
						&& grammemes2.has(gramm.getByGrammemeGroupIndex(GrammemeUtils.GNDR_NDX))) {

					/*
					 * if (grammemes2.has(GrammemeEnum.nomn) && grammemes2.has(GrammemeEnum.sing) &&
					 * grammemes2.has(gramm.getByGrammemeGroupIndex(GrammemeUtils.GNDR_NDX))) {
					 */
					if (!predictedSurname)
						return wf.value;
					// когда это предсказанная фамилия - всё немного не так: в лемме
					// хранится основа, а само слово с окончаниями можно вытащить
					// лишь получив граммемы
					return morphAnalyser.getPredictedSurnameForGrammeme(lexeme.getLemm(),
							wf.endingModelNumber, grammemes2);
				}

			}
			return null;
		}

		public int getFirstTokenIndex() {
			return firstTokenIndex;
		}

		public Grammemes getGrammemes() {
			return grammemes;
		}

		public int getLastTokenIndex() {
			return lastTokenIndex;
		}

		public Token getName() {
			return name;
		}

		public String getNameLemm() {
			return nameLemm;
		}

		public Lexeme getNameLexeme() {
			return nameLexeme;
		}

		public Token getPatronomyc() {
			return patronomyc;
		}

		public String getPatronomycLemm() {
			return patronomycLemm;
		}

		public Lexeme getPatronomycLexeme() {
			return patronomycLexeme;
		}

		public Token getSurname() {
			return surname;
		}

		public String getSurnameLemm() {
			return surnameLemm;
		}

		public Lexeme getSurnameLexeme() {
			return surnameLexeme;
		}

		public boolean isNameInitials() {
			return nameInitials;
		}

		public boolean isPatronomycInitials() {
			return patronomycInitials;
		}

		public boolean isSurnameInitials() {
			return surnameInitials;
		}

		public boolean isSurnamePredicted() {
			return surnamePredicted;
		}

		/**
		 * Каноничная сериализация (lemm-like)
		 * 
		 * @return
		 */
		public String serializeCanonical() {
			Grammemes grammemes2 = new Grammemes(grammemes);
			grammemes2.setCase(GrammemeEnum.nomn);
			grammemes2.setNumber(GrammemeEnum.sing);
			return serialize(grammemes2);
		}

		/**
		 * Сериализовать ФИО. Постараться вывести в ед.ч., им.род (род - соответствующей граммеме).
		 * 
		 * @return
		 */
		public String serialize() {
			return serialize(grammemes);
		}

		/**
		 * Сериализовать ФИО. Постараться вывести в ед.ч., им.род (род - соответствующей граммеме).
		 * 
		 * @return
		 */
		public String serialize(Grammemes grammemesOther) {
			StringBuffer sb = new StringBuffer();
			if (getSurname() != null && !isSurnameInitials()) {
				sb.append(surnameLexeme != null ? getCompatibleLexemePresentation(surnameLexeme,
						grammemesOther, isSurnamePredicted()) : surname.value);
				sb.append(" ");
			}
			if (getName() != null && !isNameInitials()) {
				sb.append(nameLexeme != null
						? getCompatibleLexemePresentation(nameLexeme, grammemesOther, false)
						: name.value);
				sb.append(" ");
			} else if (getName() != null && isNameInitials()) {
				sb.append(name.value);
				sb.append(" ");
			}
			if (getPatronomyc() != null && !isPatronomycInitials()) {
				sb.append(nameLexeme != null
						? getCompatibleLexemePresentation(patronomycLexeme, grammemesOther, false)
						: patronomyc.value);
			} else if (getPatronomyc() != null && isPatronomycInitials()) {
				sb.append(patronomyc.value);
			}
			return sb.toString().trim().toLowerCase();
		}

		public void setGrammemes(Grammemes grammemes) {
			this.grammemes = grammemes;
		}

		public void setName(Token token, boolean initials, int tokenIndex) {
			this.name = token;
			this.nameInitials = initials;
			if (lastTokenIndex < tokenIndex)
				lastTokenIndex = tokenIndex;
			if (firstTokenIndex > tokenIndex)
				firstTokenIndex = tokenIndex;
		}

		public void setNameLexemeAndLemm(Lexeme nameLexeme, String lemm) {
			if (lemm != null)
				this.nameLemm = lemm;
			if (nameLexeme != null)
				this.nameLexeme = nameLexeme;
		}

		public void setPatronomyc(Token token, boolean initials, int tokenIndex) {
			this.patronomyc = token;
			this.patronomycInitials = initials;

			if (lastTokenIndex < tokenIndex)
				lastTokenIndex = tokenIndex;
			if (firstTokenIndex > tokenIndex)
				firstTokenIndex = tokenIndex;
		}

		public void setPatronomycLexemeAndLemm(Lexeme patronomycLexeme, String lemm) {
			if (patronomycLexeme != null)
				this.patronomycLexeme = patronomycLexeme;
			if (lemm != null)
				this.patronomycLemm = lemm;
		}

		public void setSurname(Token token, boolean initials, int tokenIndex) {
			this.surname = token;
			this.surnameInitials = initials;

			if (lastTokenIndex < tokenIndex)
				lastTokenIndex = tokenIndex;
			if (firstTokenIndex > tokenIndex)
				firstTokenIndex = tokenIndex;
		}

		public void setSurnameLexemeAndLemm(Lexeme surnameLexeme, String surnameLemm) {
			if (surnameLexeme != null)
				this.surnameLexeme = surnameLexeme;
			if (surnameLemm != null)
				this.surnameLemm = surnameLemm;
		}

		public void setSurnamePredicted(boolean b) {
			this.surnamePredicted = true;
		}

		@Override
		public String toString() {
			return String.format(
					"FIOEntry [name(%b)=%s, surname(%b)=%s, patronomyc(%b)=%s, foundBy=%s]",
					nameInitials, name, surnameInitials, surname, patronomycInitials, patronomyc,
					foundBy);
		}

		public String getLemmId() {
			if (nameLexeme != null)
				return nameLexeme.getId();
			if (surnameLexeme != null)
				return surnameLexeme.getId();
			if (patronomycLexeme != null)
				return patronomycLexeme.getId();
			return null;
		}
	}

	/**
	 * Шаблоны потенциальных вхождений ФИО в текст.
	 * 
	 * @author fedor
	 *
	 */
	static class FIOTemplate {
		/**
		 * Части вхождения
		 */
		EntranceFIOPartTypes[] parts;
		/**
		 * Наименование типа.
		 */
		EntranceFIOTypes type;
		/**
		 * Определяет - будем ли искать фамилию из словаря справа или слева от вхождения.
		 */
		boolean checkSurnameFromDic;

		public FIOTemplate(EntranceFIOPartTypes[] parts, EntranceFIOTypes type,
				boolean checkSurnameFromDic) {
			super();
			this.parts = parts;
			this.type = type;
			this.checkSurnameFromDic = checkSurnameFromDic;
		}

		@Override
		public String toString() {
			if (checkSurnameFromDic)
				return type + ": " + Arrays.toString(parts) + " (checkSurname)";
			else
				return type + ": " + Arrays.toString(parts) + "";
		}

	}

	static List<FIOTemplate> templates = new ArrayList<FIOTemplate>();

	/**
	 * 
	 * Заполняем шаблон в соотвествии со структурой из первоисточника. Обращаю внимание, что фамилия
	 * (F) добавляется везде, где есть признак поиска фамилии. <code>
	 *SNameTemplate g_FirmedNameTemplates[g_FirmedNameTemplatesCount] =
	{
	  { {FirstName, Patronomyc}, 2, FIOname, true},
	  { {InitialName, InitialPatronomyc}, 2, FIOname, true},
	  { {FirstName, InitialPatronomyc }, 2, FIOname, true},
	  { {FirstName, InitialName, InitialPatronomyc}, 3, FIOname, false}, //Мамай В.И.
	  { {FirstName, Patronomyc}, 2, IOname, false},
	  { {FirstName, InitialPatronomyc}, 2, IOnameIn, false},
	  { {InitialName, InitialPatronomyc}, 2, IOnameInIn, false},
	  { {FirstName }, 1, FIname, true },
	  { {InitialName }, 1, IFnameIn, true},
	  { {FirstName, InitialName}, 2, FInameIn, false},    //Мамай В.
	  { {FirstName }, 1, Iname, false},
	  { {Surname}, 1, Fname, false},
	  { {InitialName }, 1, InameIn, false}
	};
	 *</code>
	 */
	static {
		// { {FirstName, Patronomyc}, 2, FIOname, true},
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name,
				EntranceFIOPartTypes.Patronomyc}, EntranceFIOTypes.FIO, true));
		// { {InitialName, InitialPatronomyc}, 2, FIOname, true},
		templates
				.add(new FIOTemplate(
						new EntranceFIOPartTypes[] {EntranceFIOPartTypes.InitialName,
								EntranceFIOPartTypes.InitialPatronomyc},
						EntranceFIOTypes.FIO, true));
		// { {FirstName, InitialPatronomyc }, 2, FIOname, true},
		templates
				.add(new FIOTemplate(
						new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name,
								EntranceFIOPartTypes.InitialPatronomyc},
						EntranceFIOTypes.FIO, true));
		// Мамай В.И.
		// { {FirstName, InitialName, InitialPatronomyc}, 3, FIOname, false},
		templates.add(new FIOTemplate(
				new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name,
						EntranceFIOPartTypes.InitialName, EntranceFIOPartTypes.InitialPatronomyc},
				EntranceFIOTypes.FIO, false));
		// { {FirstName, Patronomyc}, 2, IOname, false},
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name,
				EntranceFIOPartTypes.Patronomyc}, EntranceFIOTypes.IO, false));
		// { {FirstName, InitialPatronomyc}, 2, IOnameIn, false},
		templates
				.add(new FIOTemplate(
						new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name,
								EntranceFIOPartTypes.InitialPatronomyc},
						EntranceFIOTypes.IO_In, false));
		// { {InitialName, InitialPatronomyc}, 2, IOnameInIn, false},
		templates
				.add(new FIOTemplate(
						new EntranceFIOPartTypes[] {EntranceFIOPartTypes.InitialName,
								EntranceFIOPartTypes.InitialPatronomyc},
						EntranceFIOTypes.IO_InIn, false));
		// { {FirstName }, 1, FIname, true },
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name},
				EntranceFIOTypes.FI, true));
		// { {InitialName }, 1, IFnameIn, true},
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.InitialName},
				EntranceFIOTypes.FI_In, true));
		// Мамай В.
		// { {FirstName, InitialName}, 2, FInameIn, false},
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name,
				EntranceFIOPartTypes.InitialName}, EntranceFIOTypes.FI_In, false));
		// { {FirstName }, 1, Iname, false},
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Name},
				EntranceFIOTypes.I, false));
		// { {Surname}, 1, Fname, false},
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.Surname},
				EntranceFIOTypes.F, false));
		// { {InitialName }, 1, InameIn, false}
		templates.add(new FIOTemplate(new EntranceFIOPartTypes[] {EntranceFIOPartTypes.InitialName},
				EntranceFIOTypes.I_In, false));
	}


	@Inject
	public ITokenManager tokenManager;

	/**
	 * Проверка на единичное добавлени и отсуствие ковычек внутри.
	 * 
	 * @param tokens
	 * @param addedFioVariants
	 * @param foundName
	 * @return
	 */
	private boolean addFoundFio(List<Token> tokens, List<FIOEntry> addedFioVariants,
			FIOEntry foundName) {
		Token w1 = tokens.get(foundName.getFirstTokenIndex());
		Token w2 = tokens.get(foundName.getLastTokenIndex());
		boolean quote1 = false;
		boolean quote2 = false;

		if (w1.isLQuoted())
			quote1 = true;

		if (w2.isRQuoted())
			quote2 = true;

		boolean quoteBetween =
				((foundName.getLastTokenIndex() - foundName.getFirstTokenIndex()) > 1)
						&& (w1.isRQuoted() || w2.isLQuoted());
		if (!(quote1 && quote2) && !quoteBetween) {
			boolean add = true;
			if (addedFioVariants.size() > 0) {
				FIOEntry prevFio = addedFioVariants.get(addedFioVariants.size() - 1);
				if (prevFio == foundName && (prevFio.getGrammemes() == foundName.getGrammemes())) {
					add = false;
				}
			}

			if (add)
				addedFioVariants.add(foundName);
			return true;
		}
		return false;
	}

	private void addSingleSurname(List<Token> tokens, int tokenIndex, List<FIOEntry> foundNames) {
		Token t = tokens.get(tokenIndex);
		if (!t.getLexemesListCopy().stream().anyMatch(l -> l.getGrammemes().has(GrammemeEnum.surn)))
			return;

		FIOEntry newEntrance = new FIOEntry((FIOTemplate) null);
		newEntrance.setSurname(t, false, tokenIndex);
		Lexeme lexeme = t.getLexemesListCopy().stream()
				.filter(l -> l.getGrammemes().has(GrammemeEnum.surn)).findAny().get();
		newEntrance.setSurnameLexemeAndLemm(lexeme, lexeme.getLemm());

		List<FIOEntry> nameVariants = new LinkedList<>();
		checkFIOGrammInfo(newEntrance, nameVariants);
		Iterator<FIOEntry> nameIter = nameVariants.iterator();
		while (nameIter.hasNext()) {
			FIOEntry nameVariant = nameIter.next();
			normalizeFIOAndGrammemes(nameVariant);
			addFoundFio(tokens, foundNames, nameVariant);
		}
	}

	/**
	 * @param tokens
	 * @param tokenIndex
	 * @param fioTemplate
	 * @param foundNameVariants
	 * @return
	 */
	private boolean applyTemplates(List<Token> tokens, int tokenIndex, FIOTemplate fioTemplate,
			List<FIOEntry> foundNameVariants) {

		if (fioTemplate.parts.length + tokenIndex > tokens.size())
			return false;
		FIOEntry foundName = new FIOEntry(fioTemplate);

		for (int i = 0; i < fioTemplate.parts.length; i++) {

			if (tokens.get(tokenIndex + i).isLQuoted() && tokens.get(tokenIndex + i).isRQuoted())
				return false;

			Token t = tokens.get(tokenIndex + i);
			// повторно не включаем в анализ
			if (t.getClass().equals(FIOKeywordEntrance.class))
				return false;


			switch (fioTemplate.parts[i]) {
				case InitialName: {
					if (t.kwInitial) {
						foundName.setName(t, true, tokenIndex + i);
						continue;
					} else
						return false;
				}
				case InitialPatronomyc: {
					if (t.kwInitial) {
						foundName.setPatronomyc(t, true, tokenIndex + i);
						continue;
					} else
						return false;
				}
				case Name: {
					if (t.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.name))) {
						Lexeme lexeme = t.getLexemesListCopy().stream()
								.filter(l -> l.has(GrammemeEnum.name)).findAny().get();
						foundName.setNameLexemeAndLemm(lexeme, lexeme.getLemm());
						foundName.setName(t, false, tokenIndex + i);
						continue;
					} else
						return false;
				}
				case Patronomyc: {
					if (t.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.patr))) {
						Lexeme lexeme = t.getLexemesListCopy().stream()
								.filter(l -> l.has(GrammemeEnum.patr)).findAny().get();
						foundName.setPatronomycLexemeAndLemm(lexeme, lexeme.getLemm());
						foundName.setPatronomyc(t, false, tokenIndex + i);
						continue;
					} else
						return false;
				}
				case Surname: {
					// вначале пытаемся найти словарные фамилии
					if (t.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.surn))) {
						Lexeme lexeme = t.getLexemesListCopy().stream()
								.filter(l -> l.has(GrammemeEnum.surn)).findAny().get();
						foundName.setSurnameLexemeAndLemm(lexeme, lexeme.getLemm());
						foundName.setSurname(t, false, tokenIndex + i);
						continue;
					} else if (predictAsSurname(t)) {
						// берем даже предсказанные
						Lexeme lexeme = t.getLexemesListCopy(true).stream()
								.filter(l -> l.has(GrammemeEnum.surn)).findAny().get();
						foundName.setSurnameLexemeAndLemm(lexeme, lexeme.getLemm());
						foundName.setSurname(t, false, tokenIndex + i);
						foundName.setSurnamePredicted(true);
						continue;
					} else
						return false;
				}
				default:
					throw new IllegalStateException();
			}
		}

		if (!fioTemplate.checkSurnameFromDic) {
			if (!checkFIOGrammInfo(foundName, foundNameVariants))
				return false;
			return true;
		}

		// тут работаем в случае если требуется искать фамилию слева или справа
		boolean surnameFoundFromLeft = false, surnameFoundFromRight = false;
		int iSurnameFromLeft = tokenIndex - 1;
		int iSurnameFromRight = tokenIndex + fioTemplate.parts.length;
		boolean upperCase = tokens.get(tokenIndex).isHReg2();

		// попытаемся справа найти
		if (iSurnameFromRight < tokens.size()) {
			// allow Vladimir Putin, Vladimir PUTIN and VLADIMIR PUTIN, but not
			// VLADIMIR Putin (spike :( )
			if (tokens.get(iSurnameFromRight).getLexemesListCopy().stream()
					.anyMatch(l -> l.has(GrammemeEnum.surn))
					&& (upperCase || tokens.get(iSurnameFromRight).isHReg1())) {
				// так как может быть много омонимов-фамилий(например, Шиховой)
				Iterator<Lexeme> lexIter =
						tokens.get(iSurnameFromRight).getLexemesListCopy().iterator();
				while (lexIter.hasNext()) {
					Lexeme lexeme = lexIter.next();
					if (!lexeme.has(GrammemeEnum.surn))
						continue;
					foundName.setSurname(tokens.get(iSurnameFromRight), false, iSurnameFromRight);
					foundName.setSurnameLexemeAndLemm(lexeme, lexeme.getLemm());
					// проверяем согласования
					// ф-ция может размножить варианты фио для женских и мужских омонимов
					if (checkFIOGrammInfo(foundName, foundNameVariants)) {
						surnameFoundFromRight = true;
						break;
					}
				}
			}
		}

		// если нашли фамилию справа - дальше не ищем....
		if (surnameFoundFromRight)
			return true;

		// попытаемся слева найти
		if (iSurnameFromLeft >= 0) {
			// allow Putin Vladimir, PUTIN Vladimir and PUTIN VLADIMIR, but not Putin
			// VLADIMIR (spike :( )
			// also check for closing quote separating Surname from FIO (which is
			// forbidden)
			if (tokens.get(iSurnameFromLeft).getLexemesListCopy().stream().anyMatch(
					l -> l.has(GrammemeEnum.surn)) && !tokens.get(iSurnameFromLeft).isRQuoted()
					&& (upperCase || tokens.get(iSurnameFromLeft).isHReg1())) {
				// так как может быть много омонимов-фамилий(например, Шиховой)
				Iterator<Lexeme> lexIter =
						tokens.get(iSurnameFromLeft).getLexemesListCopy().iterator();
				while (lexIter.hasNext()) {
					Lexeme lexeme = lexIter.next();
					if (!lexeme.has(GrammemeEnum.surn))
						continue;
					foundName.setSurname(tokens.get(iSurnameFromLeft), false, iSurnameFromLeft);
					foundName.setSurnameLexemeAndLemm(lexeme, lexeme.getLemm());
					// проверяем согласования
					// ф-ция может размножить варианты фио для женских и мужских омонимов
					if (checkFIOGrammInfo(foundName, foundNameVariants)) {
						surnameFoundFromLeft = true;
						break;
					}
				}
			}
		}

		if (surnameFoundFromLeft)
			return true;

		return false;
	}

	/**
	 * //вычисляем полноценные грамеммы для построенного фио на основе //граммем, вычисленных в
	 * CheckGrammInfo
	 * 
	 * @param foundName
	 */
	private void assignGrammemes(FIOEntry foundName) {
		Grammemes fnameGrammemes = new Grammemes(), surnameGrammemes = new Grammemes();

		if (foundName.getName() != null && !foundName.isNameInitials()) {
			fnameGrammemes = GrammemeUtils.intersect(foundName.getNameLexeme().getGrammemes(),
					foundName.getGrammemes());
			fnameGrammemes.leaveOnly(Grammemes.ALL_GNC);
			fnameGrammemes.setPOS(foundName.getNameLexeme().getGrammemes().getPOS());
		}

		if (foundName.getSurnameLexeme() != null && foundName.getSurname() != null
				&& !foundName.isSurnameInitials() && foundName.getSurname().getLexemesListCopy()
						.stream().anyMatch(l -> l.has(GrammemeEnum.surn))) {
			if (!foundName.getSurnameLexeme().has(GrammemeEnum.adjs)) {
				surnameGrammemes = GrammemeUtils.intersect(
						foundName.getSurnameLexeme().getGrammemes(), foundName.getGrammemes());
				surnameGrammemes.leaveOnly(Grammemes.ALL_GNC);
				surnameGrammemes.setPOS(foundName.getSurnameLexeme().getGrammemes().getPOS());
			}
		}

		boolean useFirstName = true; // prefer first-name by default
		if (fnameGrammemes.isEmpty())
			useFirstName = false;
		else if (!surnameGrammemes.isEmpty()) {
			// consider indeclinable homonyms
			boolean ind_fname = foundName.getNameLexeme().hasAll(Grammemes.MAJOR_CASES);
			boolean ind_surname = foundName.getSurnameLexeme().hasAll(Grammemes.MAJOR_CASES);
			if (ind_fname != ind_surname) // if only one is indeclinable use
											// non-indeclinable
				useFirstName = ind_surname;
		}

		foundName.setGrammemes(useFirstName ? foundName.getNameLexeme().getGrammemes()
				: foundName.getSurnameLexeme().getGrammemes());
	}

	private boolean checkFIOAgreements(FIOEntry foundName) {
		Grammemes resGrammemes = new Grammemes(), commonNameSurnameGrammems = new Grammemes(),
				commonNamePatronomycGrammems = new Grammemes();

		// согласование между именем и фамилией
		if ((foundName.getName() != null && !foundName.isNameInitials())
				&& (foundName.getSurname() != null && !foundName.isSurnameInitials())) {
			if (!foundName.getNameLexeme().isGNCAgree(foundName.getSurnameLexeme()))
				return false;
			commonNameSurnameGrammems =
					foundName.getNameLexeme().intersect(foundName.getSurnameLexeme());
			commonNameSurnameGrammems.leaveOnly(Grammemes.ALL_GNC);
			// special case
			if (commonNameSurnameGrammems.has(GrammemeEnum.plur)
					&& !commonNameSurnameGrammems.has(GrammemeEnum.sing))
				commonNameSurnameGrammems = new Grammemes();
			resGrammemes = commonNameSurnameGrammems;
			if (commonNameSurnameGrammems.isEmpty())
				return false;
		}

		// согласование между именем и отчеством
		if ((foundName.getName() != null && !foundName.isNameInitials())
				&& (foundName.getPatronomyc() != null && !foundName.isPatronomycInitials())) {
			if (!foundName.getNameLexeme().isGNCAgree(foundName.getPatronomycLexeme()))
				return false;

			commonNamePatronomycGrammems =
					foundName.getNameLexeme().intersect(foundName.getPatronomycLexeme());
			commonNamePatronomycGrammems.leaveOnly(Grammemes.ALL_GNC);

			// special case
			if (commonNamePatronomycGrammems.has(GrammemeEnum.plur)
					&& !commonNamePatronomycGrammems.has(GrammemeEnum.sing))
				commonNamePatronomycGrammems = new Grammemes();

			resGrammemes = commonNamePatronomycGrammems;
			if (commonNamePatronomycGrammems.isEmpty())
				return false;
		}

		// пересечение граммем (именем и фамилией) и (именем и отчеством)
		if (!commonNamePatronomycGrammems.isEmpty() && !commonNameSurnameGrammems.isEmpty()) {
			resGrammemes = GrammemeUtils.intersect(commonNamePatronomycGrammems,
					commonNameSurnameGrammems);
			resGrammemes.leaveOnly(Grammemes.ALL_GNC);
			if (resGrammemes.isEmpty())
				return false;
		}

		// возьмем нужные граммемы от фамилии, когда либо вообще нет имени, либо
		// только инициал, с которого ничего не возьмешь
		if ((foundName.getName() == null || foundName.isNameInitials())
				&& (foundName.getSurname() != null && !foundName.isSurnameInitials()))
			resGrammemes = foundName.getSurnameLexeme().getGrammemes();

		// возьмем нужные граммемы от имени, когда ничего больше нет
		if ((foundName.getName() != null && !foundName.isNameInitials())
				&& (foundName.getSurname() == null || foundName.isSurnameInitials())
				&& (foundName.getPatronomyc() == null || foundName.isPatronomycInitials()))
			resGrammemes = foundName.getNameLexeme().getGrammemes();

		// когда граммемы взять неоткуда и неизвестен даже род,
		// присваиваем все возможные значения
		if ((foundName.getName() != null && foundName.isNameInitials())
				&& (foundName.getSurname() == null || foundName.isSurnameInitials())
				&& (foundName.getPatronomyc() == null || foundName.isPatronomycInitials())) {
			resGrammemes = new Grammemes();
			resGrammemes.addAll(Grammemes.MAJOR_CASES);
			resGrammemes.addAll(Arrays.asList(GrammemeEnum.sing, GrammemeEnum.masc,
					GrammemeEnum.femn, GrammemeEnum.nomn));
		}

		// only allow FIO with gSingular
		if (!resGrammemes.has(GrammemeEnum.sing))
			return false;
		// do not allow plural FIO (only indeclinable)
		if (!resGrammemes.hasAll(Grammemes.MAJOR_CASES))
			resGrammemes.remove(GrammemeEnum.plur);

		foundName.setGrammemes(resGrammemes);

		return !resGrammemes.isEmpty();
	}

	/**
	 * проверяет все возможные согласования между именем, фамилией и отчеством добаляет варианты для
	 * женского (или мужского) варианта.
	 * 
	 * @param entrance
	 * @param foundNameVariants
	 * @return
	 */
	private boolean checkFIOGrammInfo(FIOEntry entrance, List<FIOEntry> foundNameVariants) {
		List<Lexeme> nameLexems = Collections.emptyList();
		if (entrance.getName() != null)
			nameLexems = entrance.getName().getLexemesListCopy().stream()
					.filter(l -> l.has(GrammemeEnum.name)).collect(Collectors.toList());

		List<Lexeme> surnameLexems = Collections.emptyList();
		if (entrance.getSurname() != null)
			surnameLexems = entrance.getSurname().getLexemesListCopy(entrance.isSurnamePredicted())
					.stream().filter(l -> l.has(GrammemeEnum.surn)).collect(Collectors.toList());
		// name & surname
		if (nameLexems.size() > 0 && surnameLexems.size() > 0) {
			for (int i = 0; i < nameLexems.size(); i++)
				for (int j = 0; j < surnameLexems.size(); j++) {
					FIOEntry newEntrance = new FIOEntry(entrance);
					newEntrance.setNameLexemeAndLemm(nameLexems.get(i),
							nameLexems.get(i).getLemm());
					newEntrance.setSurnameLexemeAndLemm(surnameLexems.get(j),
							surnameLexems.get(j).getLemm());
					if (checkFIOAgreements(newEntrance))
						foundNameVariants.add(newEntrance);
				}
			// only name
		} else if (nameLexems.size() > 0) {
			for (int i = 0; i < nameLexems.size(); i++) {
				FIOEntry newEntrance = new FIOEntry(entrance);
				newEntrance.setNameLexemeAndLemm(nameLexems.get(i), nameLexems.get(i).getLemm());
				if (checkFIOAgreements(newEntrance))
					foundNameVariants.add(newEntrance);
			}
			// only surname
		} else if (surnameLexems.size() > 0) {
			for (int j = 0; j < surnameLexems.size(); j++) {
				FIOEntry newEntrance = new FIOEntry(entrance);
				newEntrance.setSurnameLexemeAndLemm(surnameLexems.get(j),
						surnameLexems.get(j).getLemm());
				if (checkFIOAgreements(newEntrance))
					foundNameVariants.add(newEntrance);
			}
		} else // инициалы какие-нибудь
		{
			foundNameVariants.add(entrance);
		}
		return foundNameVariants.size() > 0;
	}

	/**
	 * выберем среди конечных вариантов ФИО вокруг одной фамилии те у которых фамилия стоит как
	 * можно правее, например для ...Союза Валерия Новодворская... построится два варианта "Союза
	 * Валерий" и "Валерия Новодворская", нужно оставить только "Валерия Новодворская"
	 * 
	 * @param tokens
	 * 
	 * @param addedFioVariants
	 * @param foundNames
	 */
	private void chooseVariants(List<Token> tokens, List<FIOEntry> addedFioVariants,
			List<FIOEntry> foundNames) {
		if (addedFioVariants.size() == 0)
			return;
		if (addedFioVariants.size() == 1) {
			foundNames.add(addedFioVariants.get(0));
			return;
		}

		int iMaxRightWord = 0;
		boolean[] gleichedFios = new boolean[addedFioVariants.size()];
		Arrays.fill(gleichedFios, false);
		boolean allNotGleiched = true;
		int i = 0;
		boolean allWithoutSurname = true;
		for (; i < addedFioVariants.size(); i++) {
			FIOEntry oc = addedFioVariants.get(i);
			if (oc.getSurname() != null && !oc.isSurnameInitials()) {
				allWithoutSurname = false;
				int iW = tokens.indexOf(oc.getSurname());
				if (iMaxRightWord < iW)
					iMaxRightWord = iW;
				if (oc.getName() != null && !oc.isNameInitials()) {
					Grammemes commonGramm = new Grammemes();
					if (oc.getNameLexeme() != null) {
						commonGramm = oc.getNameLexeme().intersect(oc.getSurnameLexeme());
						commonGramm.leaveOnly(Grammemes.ALL_GNC);
					}
					gleichedFios[i] = !commonGramm.isEmpty();
					if (gleichedFios[i])
						allNotGleiched = false;
				}
			}
		}

		for (i = 0; i < addedFioVariants.size(); i++) {
			FIOEntry oc = addedFioVariants.get(i);
			if (oc.getSurname() != null && !oc.isSurnameInitials()) {
				int iW = tokens.indexOf(oc.getSurname());
				if (iMaxRightWord == iW && (allNotGleiched || gleichedFios[i]))
					foundNames.add(addedFioVariants.get(i));
			} else if (allWithoutSurname)
				foundNames.add(addedFioVariants.get(i));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.INameFinder#combineFoundNames(java.util.Collection)
	 */
	public Collection<FIOKeywordEntrance> combineFoundNames(Collection<FIOEntry> detectedFIO) {
		List<FIOKeywordEntrance> result = new LinkedList<>();
		Comparator<FIOEntry> comparator1 = new Comparator<FIOEntry>() {
			@Override
			public int compare(FIOEntry o1, FIOEntry o2) {
				if (o1.getFirstTokenIndex() != o2.getFirstTokenIndex())
					return -1 * (o1.getFirstTokenIndex() - o2.getFirstTokenIndex());
				if (o1.getLastTokenIndex() != o2.getLastTokenIndex())
					return -1 * (o1.getLastTokenIndex() - o2.getLastTokenIndex());
				return 0;
			}
		};
		List<FIOEntry> list = detectedFIO.stream().sorted(comparator1).collect(Collectors.toList());
		// идем с хвоста (итерированием), удаляем неполностью входящие и объединяем
		// входящие
		// при обнаружении нового начала - начинаем новую запись
		FIOKeywordEntrance currEntr = null;
		ListIterator<FIOEntry> listIterator = list.listIterator();
		while (listIterator.hasNext()) {
			FIOEntry entry = listIterator.next();
			int entry_len = entry.getLastTokenIndex() - entry.getFirstTokenIndex() + 1;
			int entry_from = entry.getFirstTokenIndex();
			if (currEntr == null) {
				currEntr = new FIOKeywordEntrance(entry_from, entry_len);
				result.add(currEntr);
				currEntr.addFIOs(entry);
			} else {
				if (currEntr.getFrom() == entry_from && currEntr.getLength() == entry_len) {
					currEntr.addFIOs(entry);
				} else if (currEntr.getFrom() != entry_from) {
					// создаем новую запись
					currEntr = new FIOKeywordEntrance(entry_from, entry_len);
					result.add(currEntr);
					currEntr.addFIOs(entry);
				} else {
					// если меняется только длина - участка - игнорируем
					;
				}
			}
		}

		// обязательно отсортировать по возрастанию позиции вхождения
		Comparator<FIOKeywordEntrance> comparator2 = new Comparator<FIOKeywordEntrance>() {
			@Override
			public int compare(FIOKeywordEntrance o1, FIOKeywordEntrance o2) {
				if (o1.getFrom() != o2.getFrom())
					return o1.getFrom() - o2.getFrom();
				return 0;
			}
		};
		Collections.sort(result, comparator2);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.INameFinder#detectFIOKeywordEntrances(java.util.List)
	 */
	@Override
	public Collection<FIOEntry> detectFIOKeywordEntrances(List<Token> tokens) {
		List<FIOEntry> foundNames = new LinkedList<>();
		int currWord = 0;
		for (; currWord < tokens.size();) {
			Token token = tokens.get(currWord);
			if (!isName(token)) {
				currWord++;
				continue;
			}

			boolean bAddedOnce = false;
			for (int i = 0; i < templates.size(); i++) {

				FIOTemplate template = templates.get(i);
				List<FIOEntry> foundNameVariants = new LinkedList<>();
				int iW = currWord;

				// так как мы ищем сначала фио без фамилии, а фамилию ищем и слева и
				// справа, а потом выбираем лучшую (при прочих равных - правую)
				// то мы должны пропустить фамлилию, так двигаемся по предложению слева
				// направо но если есть омоним имени, то не пропускаем
				if (token.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.surn))
						&& template.checkSurnameFromDic) {
					// но если содержит имя или или отчетсво - не пропускать (или
					// если-что-то несколняемое)
					boolean hasFirstName = false;
					Iterator<Lexeme> lexemIter = token.getLexemesListCopy().iterator();
					while (lexemIter.hasNext()) {
						Lexeme lexeme = lexemIter.next();
						if (lexeme.has(GrammemeEnum.name))
							hasFirstName = true;
						if (lexeme.has(GrammemeEnum.name) && lexeme.has(GrammemeEnum.plur))
							hasFirstName &= lexeme.hasAll(Grammemes.MAJOR_CASES);
					}
					if (!hasFirstName || !token.kwInitial)
						iW++;
				}

				if (!findFirmedName(tokens, iW, template, foundNameVariants))
					continue;

				boolean bBreak = true;
				List<FIOEntry> addedFioVariants = new LinkedList<>();
				Iterator<FIOEntry> foundVarIter = foundNameVariants.iterator();
				while (foundVarIter.hasNext()) {
					FIOEntry foundName = foundVarIter.next();
					boolean bAdd = true;
					boolean bWithSurname = false;
					switch (template.type) {
						case FIO: {
							bWithSurname = true;
							break;
						}
						case IO: {
							if (!enlargeIO(tokens, foundName))
								if (bAddedOnce)
									bAdd = false;
							break;
						}
						case FI: {
							bWithSurname = true;
							break;
						}
						case F: {
							break;
						}
						case I: {
							enlargeI(tokens, foundName);
							break;
						}
						case IO_In: {
							if (!enlargeIO_In(tokens, foundName)) {
								bBreak = false;
								bAdd = false;
								continue;
							}
							break;
						}
						case IO_InIn: {
							enlargeIO_InIn(tokens, foundName);
							break;
						}

						case IF_In: {
							bWithSurname = true;
							break;
						}
						case I_In: {
							enlargeI_In(tokens, foundName);
							break;
						}
						default:
							break;
					}

					if (!bAdd)
						continue;

					if (bWithSurname) {
						if ((foundName.getFirstTokenIndex() == currWord + 1)
								&& (currWord + 1 == iW))
							// для ситуации ...Петрову Александру Иванову жалко ...
							// Петрову - пропустили (см. выше), анализируя окружение
							// Александру выбрали Иванову, но и про Петрову не нужно
							// забывать - добавим это как фио
							// то что перед Петрову нет имени или инициала, гарантирует
							// порядок просмотра предложения слева направо
							addSingleSurname(tokens, currWord, foundNames);
					}

					normalizeFIOAndGrammemes(foundName);
					// добавить foundName -> addedFioVariants при соблюдении условий
					if (addFoundFio(tokens, addedFioVariants, foundName))
						bAddedOnce = true;

					currWord = foundName.getLastTokenIndex() + 1;
				}

				// выберем среди конечных вариантов ФИО вокруг одной фамилии те
				// у которых фамилия стоит как можно правее,
				// например для ...Союза Валерия Новодворская... построится два
				// варианта "Союза Валерий" и "Валерия Новодворская", нужно оставить
				// только "Валерия Новодворская"
				chooseVariants(tokens, addedFioVariants, foundNames);
				if (bBreak)
					break;

			} // for (int i = 0; i < templates.size(); i++) {
			currWord++; // если ничего не подошло -- что бы не зацикливаться
		} // for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {

		// отдельно рассмотрим последовательности двух подряд идущих IName и IOName
		// IName может оказаться фамилией типа Мамай Павел Александрович
		trateNamesAsSurnames(foundNames);
		return foundNames;

	}

	private boolean enlargeBySurname(List<Token> tokens, FIOEntry foundName, int nextToken,
			int prevToken) {
		boolean fromRight = false;
		boolean fromLeft = false;

		Grammemes commonGrammemsFromRight = new Grammemes(),
				commonGrammemsFromLeft = new Grammemes();

		if (nextToken < tokens.size())
			if (!tokens.get(nextToken - 1).isRQuoted()) {
				if (wordCanBeSurname(tokens, nextToken)) {
					if (predictAsNonDictionarySurnameAndFillGrammemes(foundName, tokens, nextToken,
							commonGrammemsFromRight))
						fromRight = true;
					if (predictAsForeignSurname(foundName, tokens, nextToken,
							commonGrammemsFromRight))
						fromRight = true;
				}

				else if (nameCanBeSurnameFromTheRight(foundName, tokens, nextToken))
					fromRight = true;
			}
		if (prevToken >= 0)
			if (!tokens.get(prevToken + 1).isLQuoted()) {
				boolean atTheEnd = nextToken == tokens.size() - 1;
				if (!atTheEnd)
					atTheEnd = tokens.get(nextToken).kwPunct;
				boolean diffCase = false;
				if (foundName.getName() != null && !foundName.isNameInitials())
					diffCase = foundName.getName().isHReg2() != tokens.get(prevToken).isHReg2();
				if (wordCanBeSurname(tokens, prevToken)) {
					if (predictAsNonDictionarySurnameAndFillGrammemes(foundName, tokens, nextToken,
							commonGrammemsFromRight) && !diffCase)
						fromLeft = true;
					if (predictAsForeignSurname(foundName, tokens, nextToken,
							commonGrammemsFromRight) && !diffCase)
						fromLeft = true;
				}
			}

		boolean priorityToTheRightSurname = true;
		if (!commonGrammemsFromRight.isEmpty() && commonGrammemsFromLeft.isEmpty())
			priorityToTheRightSurname = true;

		if (commonGrammemsFromRight.isEmpty() && !commonGrammemsFromLeft.isEmpty())
			priorityToTheRightSurname = false;

		if (fromRight && (priorityToTheRightSurname || !fromLeft)) {
			Token t = tokens.get(nextToken);
			foundName.setSurname(t, false, nextToken);
			foundName.setSurnamePredicted(true);
			Optional<Lexeme> lexeme = t.getLexemesListCopy().stream()
					.filter(l -> l.getGrammemes().has(GrammemeEnum.surn)).findAny();
			if (lexeme.isPresent())
				foundName.setSurnameLexemeAndLemm(lexeme.get(), lexeme.get().getLemm());
			else
				foundName.setSurnameLexemeAndLemm(null, t.getValue());

			if (!commonGrammemsFromRight.isEmpty())
				foundName.setGrammemes(commonGrammemsFromRight);
			return true;
		}

		if (fromLeft && (!priorityToTheRightSurname || !fromRight)) {
			Token t = tokens.get(prevToken);
			foundName.setSurname(t, false, prevToken);
			foundName.setSurnamePredicted(true);
			Optional<Lexeme> lexeme = t.getLexemesListCopy().stream()
					.filter(l -> l.getGrammemes().has(GrammemeEnum.surn)).findAny();
			if (lexeme.isPresent())
				foundName.setSurnameLexemeAndLemm(lexeme.get(), lexeme.get().getLemm());
			else
				foundName.setSurnameLexemeAndLemm(null, t.getValue());

			if (!commonGrammemsFromLeft.isEmpty())
				foundName.setGrammemes(commonGrammemsFromLeft);

			return true;
		}
		return false;
	}

	/**
	 * ищем несловарную фамилию для имени
	 * 
	 * @param tokens
	 * @param foundName
	 * @return
	 */
	private boolean enlargeI(List<Token> tokens, FIOEntry foundName) {
		if (foundName.getName() == null || foundName.isNameInitials())
			return false;
		int nextToken = foundName.getLastTokenIndex() + 1;
		int prevToken = foundName.getFirstTokenIndex() - 1;
		if (enlargeBySurname(tokens, foundName, nextToken, prevToken))
			return true;
		return false;
	}

	/**
	 * ищем несловарную фамилию для одиночного инициала
	 * 
	 * @param tokens
	 * @param foundName
	 * @return
	 */
	private boolean enlargeI_In(List<Token> tokens, FIOEntry foundName) {
		if (foundName.getName() == null || !foundName.isNameInitials())
			return false;
		int nextToken = foundName.getLastTokenIndex() + 1;
		int prevToken = foundName.getFirstTokenIndex() - 1;
		if (enlargeBySurname(tokens, foundName, nextToken, prevToken))
			return true;
		return false;
	}

	/**
	 * ищем несловарную фамилию для имени и отчества
	 * 
	 * @param foundName
	 * @return
	 */
	private boolean enlargeIO(List<Token> tokens, FIOEntry foundName) {
		if ((foundName.getName() == null || foundName.isNameInitials()))
			return false;
		if (foundName.getPatronomyc() == null || foundName.isPatronomycInitials())
			return false;

		int nextToken = foundName.getLastTokenIndex() + 1;
		int prevToken = foundName.getFirstTokenIndex() - 1;
		if (enlargeBySurname(tokens, foundName, nextToken, prevToken))
			return true;
		return false;
	}

	/**
	 * ищем несловарную фамилию для имени и инициала
	 * 
	 * @param tokens
	 * @param foundName
	 * @return
	 */
	private boolean enlargeIO_In(List<Token> tokens, FIOEntry foundName) {
		if ((foundName.getName() == null || foundName.isNameInitials()))
			return false;
		if (foundName.getPatronomyc() == null || !foundName.isPatronomycInitials())
			return false;

		int nextToken = foundName.getLastTokenIndex() + 1;
		if (nextToken < tokens.size()) {
			if (wordCanBeSurname(tokens, nextToken)) {
				foundName.setSurname(tokens.get(nextToken), false, nextToken);
				foundName.setSurnameLexemeAndLemm(null, tokens.get(nextToken).getValue());
				return true;

			}
		}
		return false;
	}

	/**
	 * ищем несловарную фамилию для двух инициалов
	 * 
	 * @param tokens
	 * @param foundName
	 * @return
	 */
	private boolean enlargeIO_InIn(List<Token> tokens, FIOEntry foundName) {
		if ((foundName.getName() == null || !foundName.isNameInitials()))
			return false;
		if (foundName.getPatronomyc() == null || !foundName.isPatronomycInitials())
			return false;

		int nextToken = foundName.getLastTokenIndex() + 1;
		int prevToken = foundName.getFirstTokenIndex() - 1;
		if (enlargeBySurname(tokens, foundName, nextToken, prevToken))
			return true;
		return false;
	}

	private boolean findFirmedName(List<Token> tokens, int tokenIndex, FIOTemplate fioTemplate,
			List<FIOEntry> foundNameVariants) {
		if (!applyTemplates(tokens, tokenIndex, fioTemplate, foundNameVariants))
			return false;
		/*
		 * Iterator<FIOKeywordEntrance> namesIter = foundNameVariants.iterator(); while
		 * (namesIter.hasNext()) { FIOKeywordEntrance entrance = namesIter.next(); if
		 * (entrance.getName() != null && !entrance.isNameInitials())
		 * entrance.setNameLemm(entrance.getNameLexeme().getLemm()); if (entrance.getSurname() !=
		 * null && !entrance.isSurnameInitials())
		 * entrance.setSurnameLemm(entrance.getSurnameLexeme().getLemm()); if
		 * (entrance.getPatronomyc() != null && !entrance.isPatronomycInitials())
		 * entrance.setPatronomycLemm(entrance.getPatronomycLexeme().getLemm()); }
		 */
		return true;
	}

	private boolean isName(Token t) {
		// повторно не включаем в анализ
		if (t.getClass().equals(FIOKeywordEntrance.class))
			return false;
		if (!t.hReg1 && !t.uReg)
			return false;
		if (t.kwHyphen && isPredictedHyphenSurname(t))
			return true;
		if (t.kwInitial)
			return true;
		// если даже как предсказанная фамилия ...
		if (t.getLexemesListCopy(true).stream().anyMatch(l -> l.has(GrammemeEnum.surn)))
			return true;
		if (t.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.name)))
			return true;
		if (t.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.patr)))
			return true;
		return false;
	}

	/**
	 * Try to predict double-word surname if the last part is found in dictionary. Then check if the
	 * first part is found in dictionary as surname. If it is not then try predicting it.
	 * 
	 * @param t
	 * @return
	 */
	private boolean isPredictedHyphenSurname(Token t) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean nameCanBeSurnameFromTheRight(FIOEntry foundName, List<Token> tokens,
			int index) {
		Token t = tokens.get(index);
		if (!t.isHReg1())
			return false;

		// если не имя
		if (!t.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.name)))
			return false;

		// проверить послеследующее слово
		if (index < tokens.size() - 1) {
			Token t2 = tokens.get(index + 1);
			if (t2.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.surn))
					|| t2.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.patr)))
				return false;
			if (wordCanBeSurname(tokens, index + 1))
				return false;
		}
		// проверить на согласованность
		if (foundName.getName() != null && !foundName.isNameInitials()) {
			Iterator<Lexeme> lexemeIter = t.getLexemesListCopy().iterator();
			while (lexemeIter.hasNext()) {
				Lexeme lexeme = lexemeIter.next();
				if (foundName.getNameLexeme().isGNCAgree(lexeme))
					return true;
			}
		}

		return false;
	}

	/**
	 * нормализуем женскую форму фамилии если можно (не могли сделать раньше, так как хранили лемму
	 * фамилии, а она мужская) проставляем главное слово и граммемки
	 * 
	 * @param foundName
	 */
	private void normalizeFIOAndGrammemes(FIOEntry foundName) {
		if (foundName.getGrammemes().isEmpty()) {
			Collection<GrammemeEnum> add = Grammemes.MAJOR_CASES;
			add.add(GrammemeEnum.masc);
			add.add(GrammemeEnum.sing);
			foundName.getGrammemes().addAll(add);
		}
		assignGrammemes(foundName);
	}

	private boolean predictAsForeignSurname(FIOEntry foundName, List<Token> tokens, int tokenIndex,
			Grammemes commonGrammemsFromRight) {
		Token token = tokens.get(tokenIndex);
		// если согласование не прошло, а имя неизменяемое или фамилия несловарное
		// слово, то считаем, что имя иностранное, а предсказание случайное, и
		// говорим, что все зашибись (типа Катрин Денев)
		if (foundName.getName() != null && !foundName.isNameInitials()) {
			// несловарное слово
			if (!token.hasLexemes())
				return true;
		}

		return false;
	}

	/**
	 * согласование для предсказанной фамилии.
	 * 
	 * Проверка, что слово может быть фамилией ({@link wordCanBeSurname}) уже сделано.
	 * 
	 * @param foundName
	 * @param tokens
	 * @param tokenIndex
	 * @param commonGrammemes
	 * @return
	 */
	private boolean predictAsNonDictionarySurnameAndFillGrammemes(FIOEntry foundName,
			List<Token> tokens, int tokenIndex, Grammemes commonGrammemes) {
		if (!predictAsSurname(tokens.get(tokenIndex)))
			return false;

		// предсказанных фамилий может быть несколько
		// например, Шиховой - как Шихов или как Шиховой
		// ищем согласованные с именем
		Token token = tokens.get(tokenIndex);

		// повторно не включаем в анализ
		if (token.getClass().equals(FIOKeywordEntrance.class))
			return false;


		// получаем даже спрогнозированные
		Iterator<Lexeme> lexemIter = token.getLexemesListCopy(true).iterator();
		boolean res = false;
		while (lexemIter.hasNext()) {
			Lexeme lexeme = lexemIter.next();
			if (!lexeme.has(GrammemeEnum.surn))
				continue;

			if (foundName.getName() != null && !foundName.isNameInitials()) {
				Grammemes common = foundName.getNameLexeme().intersect(lexeme);
				// проверяем, что согласуется полностью
				boolean hasGNC = common.hasAny(Grammemes.ALL_GENDERS);
				hasGNC &= common.hasAny(Grammemes.MAJOR_CASES);
				hasGNC &= common.hasAny(Grammemes.ALL_NUMBERS);
				commonGrammemes.addAll(common.getGrammemes());
				if (hasGNC) {
					res = true;
					break;
				}
			} else {
				Grammemes common = new Grammemes(lexeme.getGrammemes());
				Collection<GrammemeEnum> filter = new LinkedList<>(Grammemes.ALL_GENDERS);
				filter.add(GrammemeEnum.sing);
				filter.add(GrammemeEnum.masc);
				filter.add(GrammemeEnum.femn);
				common.leaveOnly(filter);
				commonGrammemes.addAll(common.getGrammemes());
				if (!common.isEmpty()) {
					res = true;
					break;
				}
			}
		}

		return res;
	}

	/**
	 * Пытаемся предсказать несловарную фамилию. При нахождении таковой - удаляем ранее имевшиеся
	 * лексемы, выставляем только свои. ПРи этом в лемме лексемы выставляется не лемма, а основа,
	 * используемая в дальнейшем для объединения с окончанием (по номеру модели, вытащенной из
	 * идентификатора лексемы)
	 * 
	 * @param token
	 * @return
	 */
	private boolean predictAsSurname(Token token) {
		// повторно не включаем в анализ
		if (token.getClass().equals(FIOKeywordEntrance.class))
			return false;


		if (token.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.surn)))
			return true;

		boolean predicted = false;
		List<SurnameAnalysisResult> sarList = tokenManager.predictAsSurnameAndFillLexemes(token);

		for (SurnameAnalysisResult sar : sarList)
			if (sar.grammemes.has(GrammemeEnum.surn) || sar.grammemes.has(GrammemeEnum.adj)) {
				// если первое попадание - очистить ранее имевшиеся прогнозированные
				// токены
				if (!predicted)
					token.removeLexemes(false, true);
				Grammemes grammemes = new Grammemes(sar.grammemes);
				GrammemeUtils.setTag(GrammemeEnum.surn, grammemes);
				token.addLexeme(sar.opencorporaId, sar.base, sar.value, grammemes, false);
				predicted = true;
			}
		return predicted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.INameFinder#preprocessTokens(java.util.List)
	 */
	public void preprocessTokens(List<Token> tokens) {
		LinkedList<Token> newTokens = new LinkedList<>();
		for (int i = 0; i < tokens.size(); i++) {

			Token t = tokens.get(i);
			// если слово такое "МАМОНОВ" или такое "Мамонов"
			if (t.isHReg1() || t.isUReg()) {
				// фамилии через тире
				// а так же слова Салтыков-Щедрин и Коко-Дель-Рей
				try {
					Token t2 = tokens.get(i + 1);
					Token t3 = tokens.get(i + 2);
					if (t2.kwHyphen && (t3.isHReg1() || t3.isUReg())) {
						while (t2.kwHyphen && (t3.isHReg1() || t3.isUReg())) {
							Token token = new Token(t.getFrom(),
									t3.getFrom() + t3.getLength() - t.getFrom(),
									t.getValue() + t2.getValue() + t3.getValue(), TokenTypes.WORD);
							token.cloneAttributesTo(t);
							token.kwHyphen = true;
							i += 2;
							t2 = tokens.get(i + 1);
							t3 = tokens.get(i + 2);
							t = token;
						}
						newTokens.add(t);
						continue;
					} // if (t2.kwHyphen && (t3.isHReg1() || t3.isUReg())) {
				} catch (IndexOutOfBoundsException e) {
				}
				// инициалы
				// <П.> or <Ив.> or <I.> or <Jn.>
				// фамилии через тире
				try {
					Token t2 = tokens.get(i + 1);
					if (t.getValue().length() < 3 && t2.getValue().equals(".")) {
						Token token =
								new Token(t.getFrom(), t2.getFrom() + t2.getLength() - t.getFrom(),
										t.getValue() + t2.getValue(), TokenTypes.WORD);
						token.cloneAttributesTo(t);
						newTokens.add(token);
						token.kwInitial = true;
						i += 1;
						continue;
					}
				} catch (IndexOutOfBoundsException e) {
				}

				// если попали сюда - значит ни рдно из условий не подошло
				newTokens.add(t);
			} else {
				newTokens.add(t);
			}
		}
		tokens.clear();
		tokens.addAll(newTokens);

	}

	/**
	 * хотя бы одна гласная и одна согласная должна быть
	 * 
	 * @param text
	 * @return
	 */
	private boolean textCanBeSurname(String text) {
		boolean hasVowel = false;
		boolean hasConsonants = false;

		hasVowel = StringUtils.containsAny(text.toLowerCase(), TokenManagerImpl.RUS_VOWELS);
		hasConsonants =
				StringUtils.containsAny(text.toLowerCase(), TokenManagerImpl.RUS_CONSONANTS);
		return hasVowel && hasConsonants;
	}

	private void trateNamesAsSurnames(List<FIOEntry> foundNames) {
		// TODO Auto-generated method stub

	}

	private boolean wordCanBeSurname(List<Token> tokens, int index) {

		Token token = tokens.get(index);
		// повторно не включаем в анализ
		if (token.getClass().equals(FIOKeywordEntrance.class))
			return false;

		boolean can = (token.isHReg1() || token.isHReg2()) && textCanBeSurname(token.getValue())
				&& !token.isLQuoted() && !token.kwInitial
				&& !token.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.surn))
				&& !token.getLexemesListCopy().stream().anyMatch(l -> l.has(GrammemeEnum.name))
				&& token.getValue().length() > 1 && (token.kwWord || token.kwHyphen);

		return can;
	}

}
