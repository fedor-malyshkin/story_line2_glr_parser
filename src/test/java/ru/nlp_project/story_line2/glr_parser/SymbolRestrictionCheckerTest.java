package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.PlainKeywordToken;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;
import ru.nlp_project.story_line2.morph.GrammemeEnum;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.Grammemes;

public class SymbolRestrictionCheckerTest {

	private SymbolRestrictionChecker testable;

	@Before
	public void setUp() throws Exception {
		testable = new SymbolRestrictionChecker(new HierarchyManagerImpl());
	}

	@Test
	public void testCalculateMatchScore_NoAgreements1() {
		Grammemes grammemes1 = new Grammemes();
		Grammemes grammemes2 = new Grammemes();
		int matchScore = SymbolRestrictionChecker.calculateMatchScore(grammemes1, grammemes2, null);
		assertTrue("matchScore > 0", matchScore > 0);
	}

	@Test
	public void testCalculateMatchScore_NoAgreements2() {
		// {0=noun, 1=nomn, 3=masc, 4=anim, 5=plur}
		Grammemes grammemes1 = new Grammemes();
		GrammemeUtils.fillGrammemesByCSVMyTags("noun, nomn, masc, anim, plur", grammemes1, true);
		// {0=noun, 1=loct, 3=masc, 4=anim, 5=plur}
		Grammemes grammemes2 = new Grammemes();
		GrammemeUtils.fillGrammemesByCSVMyTags("noun, loct, masc, anim, plur", grammemes2, true);
		int matchScoreSame =
				SymbolRestrictionChecker.calculateMatchScore(grammemes1, grammemes1, null);
		int matchScoreOther =
				SymbolRestrictionChecker.calculateMatchScore(grammemes1, grammemes2, null);
		assertTrue("matchScoreSame > matchScoreOther", matchScoreSame > matchScoreOther);
	}

	@Test
	public void testCalculateMatchScore_GN_Agreements() {
		// {0=noun, 1=nomn, 3=masc, 4=anim, 5=plur}
		Grammemes grammemes1 = new Grammemes();
		GrammemeUtils.fillGrammemesByCSVMyTags("noun, nomn, masc, anim, plur", grammemes1, true);
		// {0=noun, 1=loct, 3=masc, 4=anim, 5=plur}
		Grammemes grammemes2 = new Grammemes();
		GrammemeUtils.fillGrammemesByCSVMyTags("noun, loct, masc, anim, plur", grammemes2, true);
		int matchScore_Same_GN = SymbolRestrictionChecker.calculateMatchScore(grammemes1,
				grammemes1, SymbolExtDataTypes.gn_agr);
		int matchScore_Other_GN = SymbolRestrictionChecker.calculateMatchScore(grammemes1,
				grammemes2, SymbolExtDataTypes.gn_agr);
		int matchScore_Other_NoAgree =
				SymbolRestrictionChecker.calculateMatchScore(grammemes1, grammemes2, null);
		assertTrue("matchScore_Same_GN > matchScore_Other_GN",
				matchScore_Same_GN > matchScore_Other_GN);
		assertTrue("matchScore_Other_GN > matchScore_Other_NoAgree",
				matchScore_Other_GN > matchScore_Other_NoAgree);
	}

	/**
	 * Проверка определения комбинации граммем. Проверка: gramm=[accs, plur], rt, h-reg1 Результат:
	 * - someVal (noun, accs)
	 * 
	 */
	@Test
	public void testMatchGramm_NoMatch() {
		// symbol ext
		SymbolExtData symbolExtData1 = SymbolExtData.makeLabelExtData("rt");
		SymbolExtData symbolExtData2 = SymbolExtData.makeParamExtData("gram", "accs, plur");
		SymbolExtData symbolExtData3 = SymbolExtData.makeLabelExtData("h-reg1");
		List<SymbolExtData> symbolExtDatas =
				Arrays.asList(symbolExtData1, symbolExtData2, symbolExtData3);
		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "someVal", TokenTypes.WORD);
		Grammemes grms = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.noun, grms);
		GrammemeUtils.setTag(GrammemeEnum.accs, grms);

		token.addLexeme("", "lemm", "base", grms, true);
		assertFalse(testable.matchGram(symbolExtData2.getGrammValue(), symbolExt, token));
	}

	/**
	 * Проверка определения комбинации граммем. Проверка: gramm=[accs, plur], rt, h-reg1 Результат:
	 * - someVal (noun, accs) + someVal (noun, accs, plur) - someVal (noun, accs, sing)
	 * 
	 */
	@Test
	public void testMatchGramm() {
		// symbol ext
		SymbolExtData symbolExtData1 = SymbolExtData.makeLabelExtData("rt");
		SymbolExtData symbolExtData2 = SymbolExtData.makeParamExtData("gram", "geox");
		SymbolExtData symbolExtData3 = SymbolExtData.makeLabelExtData("h-reg1");
		List<SymbolExtData> symbolExtDatas =
				Arrays.asList(symbolExtData1, symbolExtData2, symbolExtData3);
		SymbolExt symbolExt = new SymbolExt("word", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "восток", TokenTypes.WORD);
		Grammemes grms1 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.inan, grms1);
		GrammemeUtils.setTag(GrammemeEnum.masc, grms1);
		GrammemeUtils.setTag(GrammemeEnum.noun, grms1);
		token.addLexeme("", "восток", "восток", grms1, true);

		Grammemes grms2 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.inan, grms2);
		GrammemeUtils.setTag(GrammemeEnum.masc, grms2);
		GrammemeUtils.setTag(GrammemeEnum.noun, grms2);
		GrammemeUtils.setTag(GrammemeEnum.geox, grms2);
		token.addLexeme("", "восток", "восток", grms2, true);

		assertTrue(testable.matchGram(symbolExtData2.getGrammValue(), symbolExt, token));
	}

	/**
	 * Проверка определения комбинации граммем. Проверка: gramm=[accs, plur], rt, h-reg1 Результат:
	 * - someVal (noun, accs) + someVal (noun, accs, plur) - someVal (noun, accs, sing)
	 * 
	 */
	@Test
	public void testMatchGramm_Match() {
		// symbol ext
		SymbolExtData symbolExtData1 = SymbolExtData.makeLabelExtData("rt");
		SymbolExtData symbolExtData2 = SymbolExtData.makeParamExtData("gram", "accs, plur");
		SymbolExtData symbolExtData3 = SymbolExtData.makeLabelExtData("h-reg1");
		List<SymbolExtData> symbolExtDatas =
				Arrays.asList(symbolExtData1, symbolExtData2, symbolExtData3);
		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "someVal", TokenTypes.WORD);
		Grammemes grms1 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.noun, grms1);
		GrammemeUtils.setTag(GrammemeEnum.accs, grms1);
		token.addLexeme("", "lemm", "base", grms1, true);

		Grammemes grms2 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.noun, grms2);
		GrammemeUtils.setTag(GrammemeEnum.accs, grms2);
		GrammemeUtils.setTag(GrammemeEnum.plur, grms2);
		token.addLexeme("", "lemm", "base", grms2, true);

		Grammemes grms3 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.noun, grms3);
		GrammemeUtils.setTag(GrammemeEnum.accs, grms3);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms3);
		token.addLexeme("", "lemm", "base", grms3, true);

		assertTrue(testable.matchGram(symbolExtData2.getGrammValue(), symbolExt, token));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMatchKWType() throws IOException {
		TokenManagerImpl tokenManager = new TokenManagerImpl(false);
		tokenManager.initialize();
		PlainKeywordToken pkwt = tokenManager.createDummyPlainKeywordToken();
		pkwt.kwName = "kwName_toMatch".toLowerCase();
		// symbol ext
		SymbolExtData symbolExtData2 = SymbolExtData.makeParamExtData("kwtype", "kwName_toMatch");
		List<SymbolExtData> symbolExtDatas = Arrays.asList(symbolExtData2);

		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		assertTrue(testable.matchKWType(symbolExtData2, symbolExt, pkwt));

		pkwt.kwName = "kwName_toNoMatch";
		assertFalse(testable.matchKWType(symbolExtData2, symbolExt, pkwt));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMatchKWSet() throws IOException {
		TokenManagerImpl tokenManager = new TokenManagerImpl(false);
		tokenManager.initialize();
		PlainKeywordToken pkwt = tokenManager.createDummyPlainKeywordToken();
		pkwt.kwName = "kwName_toMatch".toLowerCase();
		// symbol ext
		SymbolExtData symbolExtData2 = SymbolExtData.makeArrayExtData("kwset",
				"kwName_toMatch1, kwName_toMatch, kwName_toMatch2");
		List<SymbolExtData> symbolExtDatas = Arrays.asList(symbolExtData2);

		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		assertTrue(testable.matchKWSet(symbolExtData2, symbolExt, pkwt));

		pkwt.kwName = "kwName_toNoMatchXX";
		assertFalse(testable.matchKWSet(symbolExtData2, symbolExt, pkwt));
	}

	/**
	 * Пример без отрицания
	 * 
	 * S -> Noun<GU=[sg,acc], rt>;
	 * 
	 * Сработает следующим образом:
	 * 
	 * - табуретка // именительный падеж + табуретку // винительный падеж + стол // именительный или
	 * винительный падеж - стола // родительный падеж
	 */
	@Test
	public void testMatchGU() {
		// symbol ext
		SymbolExtData symbolExtData2 = SymbolExtData.makeArrayExtData("GU", "sing, accs");
		List<SymbolExtData> symbolExtDatas = Arrays.asList(symbolExtData2);
		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "табуретка", TokenTypes.WORD);
		token.removeLexemes(true, true);
		Grammemes grms1 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.nomn, grms1);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms1);
		token.addLexeme("", "", "", grms1, true);
		assertFalse(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms2 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.accs, grms2);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms2);
		token.addLexeme("", "", "", grms2, true);
		assertTrue(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms3 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.nomn, grms3);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms3);
		token.addLexeme("", "", "", grms3, true);
		Grammemes grms4 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.accs, grms4);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms4);
		token.addLexeme("", "", "", grms4, true);
		assertTrue(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms5 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.gent, grms5);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms5);
		token.addLexeme("", "", "", grms5, true);
		assertFalse(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));
	}

	/**
	 * Пример с отрицанием
	 * 
	 * S -> Noun<GU=~[sg,acc], rt>;
	 * 
	 * Сработает следующим образом:
	 * 
	 * + табуретка // именительный падеж - табуретку // винительный падеж - стол // именительный или
	 * винительный падеж + стола // родительный падеж
	 */
	@Test
	public void testMatchGU_Negative() {

	}

	/**
	 * Пример с амперсандом
	 * 
	 * S -> Noun<GU=&[sg,acc,nom], rt>;
	 * 
	 * Сработает следующим образом:
	 * 
	 * - табуретка // именительный падеж - табуретку // винительный падеж + стол // именительный или
	 * винительный падеж - стола // родительный падеж
	 */
	@Test
	public void testMatchGU_WithAmpersand() {
		// symbol ext
		SymbolExtData symbolExtData2 = SymbolExtData.makeArrayExtData("GU", "&(sing, accs, nomn)");
		List<SymbolExtData> symbolExtDatas = Arrays.asList(symbolExtData2);
		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "табуретка", TokenTypes.WORD);
		token.removeLexemes(true, true);
		Grammemes grms1 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.nomn, grms1);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms1);
		token.addLexeme("", "", "", grms1, true);
		assertFalse(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms2 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.accs, grms2);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms2);
		token.addLexeme("", "", "", grms2, true);
		assertFalse(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms3 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.nomn, grms3);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms3);
		token.addLexeme("", "", "", grms3, true);
		Grammemes grms4 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.accs, grms4);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms4);
		token.addLexeme("", "", "", grms4, true);
		assertTrue(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms5 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.gent, grms5);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms5);
		token.addLexeme("", "", "", grms5, true);
		assertFalse(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

	}

	/**
	 * GU=[sg,ins]|&[nom,acc,gen,dat,ins]
	 * 
	 * В этой записи требуется выполнение одного из следующих условий: ) у нетерминала есть омоним в
	 * творительном падеже единственного числа ) объединение всех граммем нетерминала включает все
	 * падежные граммемы (слово не изменяется по падежам)
	 * 
	 * - стол + столом + пальто
	 */
	@Test
	public void testMatchGU_Disjunction() {
		// symbol ext
		SymbolExtData symbolExtData2 = SymbolExtData.makeArrayExtData("GU",
				"(sing, ablt)|&(accs, nomn, gent, ablt, datv)");
		List<SymbolExtData> symbolExtDatas = Arrays.asList(symbolExtData2);
		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "стол", TokenTypes.WORD);
		token.removeLexemes(true, true);
		Grammemes grms1 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.nomn, grms1);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms1);
		token.addLexeme("", "", "", grms1, true);
		assertFalse(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms2 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.ablt, grms2);
		GrammemeUtils.setTag(GrammemeEnum.sing, grms2);
		token.addLexeme("", "", "", grms2, true);
		assertTrue(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

		token.removeLexemes(true, true);
		Grammemes grms3 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.accs, grms3);
		token.addLexeme("", "", "", grms3, true);
		Grammemes grms4 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.nomn, grms4);
		token.addLexeme("", "", "", grms4, true);
		Grammemes grms5 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.gent, grms5);
		token.addLexeme("", "", "", grms5, true);
		Grammemes grms6 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.ablt, grms6);
		token.addLexeme("", "", "", grms6, true);
		Grammemes grms7 = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.datv, grms7);
		token.addLexeme("", "", "", grms7, true);
		assertTrue(testable.matchGU(symbolExtData2.getGuValue(), symbolExt, token));

	}

	@Test
	public void testRx() {
		// symbol ext
		SymbolExtData symbolExtData2 = SymbolExtData.makeParamExtData("rx", "им\\..*");
		List<SymbolExtData> symbolExtDatas = Arrays.asList(symbolExtData2);
		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "им._Пирогова", TokenTypes.WORD);
		assertTrue(testable.matchRx(symbolExtData2, symbolExt, token));
	}


	@Test
	public void testRemoveUnmatchingLexemesGram() {
		SymbolExtData symbolExtData = SymbolExtData.makeParamExtData("gram", "geox");
		List<SymbolExtData> symbolExtDatas = Arrays.asList(symbolExtData);
		SymbolExt symbolExt = new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

		Token token = new Token(0, 10, "someVal", TokenTypes.WORD);
		Grammemes grms = new Grammemes();
		GrammemeUtils.setTag(GrammemeEnum.noun, grms);
		GrammemeUtils.setTag(GrammemeEnum.accs, grms);

		token.addLexeme("", "lemm", "base", grms, true);
		testable.removeUnmatchingLexemesGram(symbolExtData.getGrammValue(), symbolExt, token);
		assertEquals(0, token.getLexemesListCopy().size());
	}
}
