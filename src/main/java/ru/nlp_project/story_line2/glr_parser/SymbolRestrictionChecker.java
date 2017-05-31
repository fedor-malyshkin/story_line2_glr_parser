package ru.nlp_project.story_line2.glr_parser;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.IInOrderWalkProcessor;
import ru.nlp_project.story_line2.glr_parser.SymbolExtData.GUBlock;
import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.GrammarKeywordToken;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl.PlainKeywordToken;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordEntranceDetector;
import ru.nlp_project.story_line2.morph.GrammemeEnum;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.Grammemes;

/**
 * Класс проверки помет-ограничений на токены (лексемы в их составе) и соотвествующие им символы.
 * <p/>
 * Код предназначен для проверки соотвествия условиям как для токенов (сигнатура
 * "matchXXXX(SymbolExtData, SymbolExt, Token)", вызывается из реализации {@link IGLRTokenMatcher})
 * так и для нетерминалов (сигнатура "matchXXXX(SymbolExtData, SymbolExt, ParseNodeTree)",
 * вызывается из {@link ParseTreeValidator#validateTree(SentenceProcessingContext, ParseTreeNode)} )
 * <p/>
 * ВНИМАНИЕ: в данном классе по максимуму сконцентриррованы все проверки помет и помет-ограничений
 * для минимизации их разбредания по коду парсера.
 * 
 * @author fedor
 *
 */
/**
 * @author fedor
 *
 */
public class SymbolRestrictionChecker {

	private static Map<String, Pattern> regExpMap = new HashMap<>();
	private IHierarchyManager hierarchyManager;

	public SymbolRestrictionChecker(IHierarchyManager hierarchyManager) {
		this.hierarchyManager = hierarchyManager;
	}

	private LinkedList<ParseTreeNode> collectTerminalNodes(ParseTreeNode node) {
		LinkedList<ParseTreeNode> result = new LinkedList<>();
		node.walkInOrder(new IInOrderWalkProcessor() {
			@Override
			public void processNonTerminalNode(ParseTreeNode node) {}

			@Override
			public void processTerminalNode(ParseTreeNode node) {
				if (node.symbol != Symbol.EPSILON)
					result.add(node);
			}
		});
		return result;
	}

	protected Pattern getRegExpPattern(String regExp) {
		Pattern pattern = regExpMap.get(regExp);
		if (pattern == null) {
			pattern = Pattern.compile(regExp);
			regExpMap.put(regExp, pattern);
		}
		return pattern;
	}

	/**
	 * 
	 * Проверка условий выполнения ограничений.
	 * 
	 * @param symbol символ грамматики
	 * @param token токен (для терминала, в обратном случе - null)
	 * @param node узел (для нетерминала, в обратном случе - null)
	 * @return успешность проверки
	 */
	public boolean match(Symbol symbol, Token token, ParseTreeNode node) {
		// простой не расширенный символ совпадает в данном случае сразу
		// (т.к. все было проверено ранее при постройке дерва)
		if (symbol.getClass() == Symbol.class)
			return true;
		// при наличии расширенных данных - все нужно проверить
		SymbolExt symbolExt = (SymbolExt) symbol;
		return match(symbolExt, token, node);
	}

	/**
	 * Проверка условий выполнения ограничений.
	 * 
	 * @param symbolExt
	 * @param token токен (для терминала, в обратном случе - null)
	 * @param node узел (для нетерминала, в обратном случе - null)
	 * @return успешность проверки
	 */
	public boolean match(SymbolExt symbolExt, Token token, ParseTreeNode node) {
		// простой расширенный символ без ограничений совпадает сразу
		// (т.к. все было проверено ранее при постройке дерва)
		if (symbolExt.getExtDatas().size() == 0)
			return true;

		Set<Entry<SymbolExtDataTypes, SymbolExtData>> entrySet =
				symbolExt.getExtDatasMap().entrySet();

		boolean match = true;
		for (Entry<SymbolExtDataTypes, SymbolExtData> entry : entrySet) {
			if (token != null && !entry.getKey().isApplyToTerminal())
				throw new IllegalStateException(
						"Restiriction is not applicable to terminal: " + entry.getKey());
			else if (node != null && !entry.getKey().isApplyToNonTerminal())
				throw new IllegalStateException(
						"Restiriction is not applicable to non-terminal: " + entry.getKey());
			match &= match(entry.getKey(), entry.getValue(), symbolExt, token, node);
		}
		return match;
	}

	public boolean match(SymbolExtDataTypes symbolExtDataType, SymbolExtData symbolExtData,
			SymbolExt symbolExt, Token token, ParseTreeNode node) {
		switch (symbolExtDataType) {
			case h_reg1:
				return matchHReg1(symbolExtData, symbolExt, token, node);
			case h_reg2:
				return matchHReg2(symbolExtData, symbolExt, token, node);

			case l_reg:
				return matchLReg(symbolExtData, symbolExt, token, node);

			case quoted:
				return matchQuoted(symbolExtData, symbolExt, token, node);

			case l_quoted:
				return matchLQuoted(symbolExtData, symbolExt, token, node);

			case r_quoted:
				return matchRQuoted(symbolExtData, symbolExt, token, node);

			case fw:
				return matchFW(symbolExtData, symbolExt, token, node);

			case mw:
				return matchMW(symbolExtData, symbolExt, token, node);

			case lat:
				return matchLat(symbolExtData, symbolExt, token, node);

			case no_hom:
				return matchNoHom(symbolExtData, symbolExt, token, node);

			case cut:
				return true;
			case rt:
				return true;
			case dict:
				return matchDict(symbolExtData, symbolExt, token, node);

			case kwtype:
				return matchKWType(symbolExtData, symbolExt, token, node);
			case kwtypef:
				return matchKWTypeF(symbolExtData, symbolExt, token, node);
			case kwtypel:
				return matchKWTypeL(symbolExtData, symbolExt, token, node);

			case kwset:
				return matchKWSet(symbolExtData, symbolExt, token, node);

			case kwsetf:
				return matchKWSetF(symbolExtData, symbolExt, token, node);
			case kwsetl:
				return matchKWSetF(symbolExtData, symbolExt, token, node);

			case rx:
				return matchRx(symbolExtData, symbolExt, token, node);

			case rxf:
				return matchRxF(symbolExtData, symbolExt, token, node);

			case rxl:
				return matchRxL(symbolExtData, symbolExt, token, node);

			case gram:
				return matchGram(symbolExtData.getGrammValue(), symbolExt, token, node);

			case gu:
				return matchGU(symbolExtData.getGuValue(), symbolExt, token, node);

			case gnc_agr:
			case nc_agr:
			case c_agr:
			case gn_agr:
			case gc_agr:
			case fem_c_agr:
			case after_num_agr:
			case sp_agr:
			case fio_agr:
			case geo_agr:
				return true;
			default:
				throw new IllegalStateException("Unknown restriction: " + symbolExtDataType);
		}
	}

	private boolean matchDict(SymbolExtData symbolExtData, SymbolExt symbolExt,
			ParseTreeNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	boolean matchDict(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		// TODO Auto-generated method stub
		return false;

	}

	private boolean matchDict(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchDict(symbolExtData, symbolExt, token);
		else
			return matchDict(symbolExtData, symbolExt, node);

	}

	boolean matchFW(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		LinkedList<ParseTreeNode> terminalNodes = collectTerminalNodes(node);
		return matchFW(symbolExtData, symbolExt, terminalNodes.getFirst().token);
	}

	boolean matchFW(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		if (token.getClass() == PlainKeywordToken.class) {
			PlainKeywordToken pkt = (PlainKeywordToken) token;
			Token first = pkt.originalTokens.getFirst();
			return matchFW(symbolExtData, symbolExt, first);
		} else if (token.getClass() == GrammarKeywordToken.class) {
			GrammarKeywordToken gkt = (GrammarKeywordToken) token;
			Token first = gkt.originalTokens.getFirst();
			return matchFW(symbolExtData, symbolExt, first);
		} else if (token.getClass() == Token.class) {
			return token.getFrom() == 0;
		} else
			throw new IllegalStateException("Unknown token class: " + token.getClass());

	}

	private boolean matchFW(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchFW(symbolExtData, symbolExt, token);
		else
			return matchFW(symbolExtData, symbolExt, node);

	}

	boolean matchGram(Grammemes grammValue, SymbolExt symbolExt, ParseTreeNode node) {
		return matchGram(grammValue, symbolExt, node.token);
	}

	/**
	 * Хотя бы одна лексема должна содежать все граммемы.
	 * 
	 * @param grammemes
	 * @param symbolExt
	 * @param token
	 * @return
	 */
	boolean matchGram(Grammemes grammemes, SymbolExt symbolExt, Token token) {
		for (Lexeme l : token.getLexemesListCopy()) {
			if (l.getGrammemes().hasAll(grammemes))
				return true;
		}
		return false;
	}

	private boolean matchGram(Grammemes grammValue, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchGram(grammValue, symbolExt, token);
		else
			return matchGram(grammValue, symbolExt, node);
	}

	/**
	 * Хотя бы одна лексема должна содежать все граммемы.
	 * <p/>
	 * Специальные метод для {@link PlainKeywordEntranceDetector}.
	 * 
	 * @param token
	 * @param grammemes
	 * @return
	 */
	public static boolean matchGram(Token token, Set<GrammemeEnum> grammemes) {
		for (Lexeme l : token.getLexemesListCopy()) {
			if (l.getGrammemes().hasAll(grammemes))
				return true;
		}
		return false;
	}

	boolean matchGU(List<GUBlock> guValue, SymbolExt symbolExt, ParseTreeNode node) {
		return matchGU(guValue, symbolExt, node.token);
	}

	/**
	 * 
	 * Помета GU (grammar union) предоставляет более широкие возможности использования граммем в
	 * грамматиках. В своей самой простой форме GU=["nom,pl"] эта помета аналогична помете gram: она
	 * проверяет грамматические характеристики (в примере выше — «именительный падеж множественного
	 * числа») у каждого омонима и если находится омоним, удовлетворяющий этому условию, то правило
	 * срабатывает.
	 * 
	 * Граммемы записываются через запятую в квадратных скобках [ ]. Отрицание отдельных граммем в
	 * этой записи запрещено. Отрицание ~ перед квадратными скобками означает, что пересечение
	 * перечисленных граммем с граммемами каждого омонима пусто, т.е. если интерпретация хотя бы
	 * одного омонима подойдет по множество граммем в квадратных скобках, то правило с таким
	 * ограничением не сработает.
	 * 
	 * Амперсанд & перед квадратными скобками означает, что парсер будет рассматривать граммемы не у
	 * каждого омонима по отдельности, а одновременно у объединения грамматических признаков всех
	 * омонимов.
	 * 
	 * Кроме того помета GU позволяет записывать дизъюнкцию нескольких таких условий. Через
	 * вертикальную черту | («или») можно записать несколько разных списков граммем и правило
	 * сработает в том случае, если слово удовлетворяет хотя бы одному из перечисленных условий.
	 * 
	 * 
	 * @param list
	 * @param symbolExt
	 * @param token
	 * @return
	 */
	boolean matchGU(List<GUBlock> guList, SymbolExt symbolExt, Token token) {
		boolean guMatch = false;
		Iterator<GUBlock> guIter = guList.iterator();
		while (guIter.hasNext()) {
			GUBlock gu = guIter.next();
			if (!gu.combined)
			// вариант когда хотя бы какая-нибудь лексема должна подходить под условия
			{
				for (Lexeme l : token.getLexemesListCopy()) {
					if (l.getGrammemes().hasAll(gu.grammemesSet))
						guMatch |= true;
				}
				guMatch |= false;
			} else
			// вариант когда все лексемы должны подходить под условия
			{
				Set<GrammemeEnum> set = EnumSet.noneOf(GrammemeEnum.class);
				for (Lexeme l : token.getLexemesListCopy())
					set.addAll(l.getGrammemes().getGrammemes());

				if (set.containsAll(gu.grammemesSet))
					guMatch |= true;
				else
					guMatch |= false;
			}
		} // while (guIter.hasNext())
		return guMatch;
	}

	private boolean matchGU(List<GUBlock> guValue, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchGU(guValue, symbolExt, token);
		else
			return matchGU(guValue, symbolExt, node);

	}

	boolean matchHReg1(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchHReg1(symbolExtData, symbolExt, node.token);
	}

	boolean matchHReg1(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return token.isHReg1();
	}

	private boolean matchHReg1(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchHReg1(symbolExtData, symbolExt, token);
		else
			return matchHReg1(symbolExtData, symbolExt, node);

	}

	boolean matchHReg2(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchHReg2(symbolExtData, symbolExt, node.token);
	}

	boolean matchHReg2(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return token.isHReg2();
	}

	private boolean matchHReg2(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchHReg2(symbolExtData, symbolExt, token);
		else
			return matchHReg2(symbolExtData, symbolExt, node);

	}

	boolean matchKWSet(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchKWSet(symbolExtData, symbolExt, node.token);
	}

	boolean matchKWSet(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		Set<String> kwsetValue = symbolExtData.getKwSetValue();
		return hierarchyManager.isAnyParent(kwsetValue, token.getKwName());
	}

	private boolean matchKWSet(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchKWSet(symbolExtData, symbolExt, token);
		else
			return matchKWSet(symbolExtData, symbolExt, node);
	}

	boolean matchKWSetF(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		LinkedList<ParseTreeNode> terminalNodes = collectTerminalNodes(node);
		return matchKWSet(symbolExtData, symbolExt, terminalNodes.getFirst());
	}

	boolean matchKWSetF(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		// глубоко копать (в grammar/plainkeyword-token) не надо т.к. там нет внутри
		// keyword
		return matchKWSet(symbolExtData, symbolExt, token);
	}

	private boolean matchKWSetF(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchKWSetF(symbolExtData, symbolExt, token);
		else
			return matchKWSetF(symbolExtData, symbolExt, node);

	}

	boolean matchKWSetL(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		LinkedList<ParseTreeNode> terminalNodes = collectTerminalNodes(node);
		return matchKWSet(symbolExtData, symbolExt, terminalNodes.getFirst());
	}

	boolean matchKWSetL(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		// глубоко копать (в grammar/plainkeyword-token) не надо т.к. там нет внутри
		// keyword
		return matchKWSet(symbolExtData, symbolExt, token);
	}

	private boolean matchKWType(SymbolExtData symbolExtData, SymbolExt symbolExt,
			ParseTreeNode node) {
		return matchKWType(symbolExtData, symbolExt, node.token);
	}

	boolean matchKWType(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return hierarchyManager.isParent(symbolExtData.getKwTypeValue(), token.getKwName());
	}

	private boolean matchKWType(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchKWType(symbolExtData, symbolExt, token);
		else
			return matchKWType(symbolExtData, symbolExt, node);

	}

	boolean matchKWTypeF(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		LinkedList<ParseTreeNode> terminalNodes = collectTerminalNodes(node);
		return matchKWType(symbolExtData, symbolExt, terminalNodes.getFirst().token);
	}

	boolean matchKWTypeF(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		// глубоко копать (в grammar/plainkeyword-token) не надо т.к. там нет внутри
		// keyword
		return matchKWType(symbolExtData, symbolExt, token);
	}

	private boolean matchKWTypeF(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchKWTypeF(symbolExtData, symbolExt, token);
		else
			return matchKWTypeF(symbolExtData, symbolExt, node);
	}

	boolean matchKWTypeL(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		LinkedList<ParseTreeNode> terminalNodes = collectTerminalNodes(node);
		return matchKWType(symbolExtData, symbolExt, terminalNodes.getLast().token);
	}

	boolean matchKWTypeL(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		// глубоко копать (в grammar/plainkeyword-token) не надо т.к. там нет внутри
		// keyword
		return matchKWType(symbolExtData, symbolExt, token);
	}

	private boolean matchKWTypeL(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchKWTypeL(symbolExtData, symbolExt, token);
		else
			return matchKWTypeL(symbolExtData, symbolExt, node);
	}

	private boolean matchLat(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchLat(symbolExtData, symbolExt, node.token);
	}

	boolean matchLat(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return token.isLat();
	}

	private boolean matchLat(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchLat(symbolExtData, symbolExt, token);
		else
			return matchLat(symbolExtData, symbolExt, node);
	}

	boolean matchLQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchLQuoted(symbolExtData, symbolExt, node.token);
	}

	boolean matchLQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return token.isLQuoted();
	}

	private boolean matchLQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchLQuoted(symbolExtData, symbolExt, token);
		else
			return matchLQuoted(symbolExtData, symbolExt, node);

	}

	boolean matchLReg(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchLReg(symbolExtData, symbolExt, node.token);
	}

	boolean matchLReg(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return token.isLReg();
	}

	private boolean matchLReg(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchLReg(symbolExtData, symbolExt, token);
		else
			return matchLReg(symbolExtData, symbolExt, node);

	}

	boolean matchMW(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		if (token.getClass() == PlainKeywordToken.class
				|| token.getClass() == GrammarKeywordToken.class)
			return true;
		return false;
	}

	private boolean matchMW(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchMW(symbolExtData, symbolExt, token);
		else
			throw new IllegalStateException(
					String.format("%s is not appplicable on non-termianls (%s)",
							symbolExt.toString(), node.toString()));
	}

	boolean matchNoHom(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchNoHom(symbolExtData, symbolExt, node.token);
	}

	/**
	 * Символ должно состоять из омонимов с одной частью речи.
	 * 
	 * @param symbolExtData
	 * @param symbolExt
	 * @param token
	 * @return
	 */
	boolean matchNoHom(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		if (token.getLexemesListCopy().size() == 0)
			return true;
		GrammemeEnum pos = token.getLexemesListCopy().getFirst().getPOS();
		Iterator<Lexeme> iter = token.getLexemesIterator();
		while (iter.hasNext()) {
			if (pos != iter.next().getPOS())
				return false;
		}
		return true;
	}

	private boolean matchNoHom(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchNoHom(symbolExtData, symbolExt, token);
		else
			return matchNoHom(symbolExtData, symbolExt, node);

	}

	boolean matchQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchQuoted(symbolExtData, symbolExt, node.token);
	}

	boolean matchQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return token.isLQuoted();
	}

	private boolean matchQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchQuoted(symbolExtData, symbolExt, token);
		else
			return matchQuoted(symbolExtData, symbolExt, node);

	}

	boolean matchRQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchRQuoted(symbolExtData, symbolExt, node.token);
	}

	boolean matchRQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		return token.isRQuoted();
	}

	private boolean matchRQuoted(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchRQuoted(symbolExtData, symbolExt, token);
		else
			return matchRQuoted(symbolExtData, symbolExt, node);

	}

	boolean matchRx(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		return matchRx(symbolExtData, symbolExt, node.token);
	}

	boolean matchRx(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		Pattern pattern = getRegExpPattern(symbolExtData.getRxValue());
		Matcher matcher = pattern.matcher(token.getValue());
		return matcher.matches();
	}

	private boolean matchRx(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchRx(symbolExtData, symbolExt, token);
		else
			return matchRx(symbolExtData, symbolExt, node);

	}

	boolean matchRxF(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		LinkedList<ParseTreeNode> terminalNodes = collectTerminalNodes(node);
		return matchRxL(symbolExtData, symbolExt, terminalNodes.getFirst().token);
	}

	boolean matchRxF(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		if (token.getClass() == PlainKeywordToken.class) {
			PlainKeywordToken pkt = (PlainKeywordToken) token;
			Token first = pkt.originalTokens.getFirst();
			return matchRxF(symbolExtData, symbolExt, first);
		} else if (token.getClass() == GrammarKeywordToken.class) {
			GrammarKeywordToken gkt = (GrammarKeywordToken) token;
			Token first = gkt.originalTokens.getFirst();
			return matchRxF(symbolExtData, symbolExt, first);
		} else if (token.getClass() == Token.class) {
			return matchRx(symbolExtData, symbolExt, token);
		} else
			throw new IllegalStateException("Unknown token class: " + token.getClass());
	}

	private boolean matchRxF(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchRxF(symbolExtData, symbolExt, token);
		else
			return matchRxF(symbolExtData, symbolExt, node);

	}

	boolean matchRxL(SymbolExtData symbolExtData, SymbolExt symbolExt, ParseTreeNode node) {
		LinkedList<ParseTreeNode> terminalNodes = collectTerminalNodes(node);
		return matchRxL(symbolExtData, symbolExt, terminalNodes.getLast().token);
	}

	boolean matchRxL(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token) {
		if (token.getClass() == PlainKeywordToken.class) {
			PlainKeywordToken pkt = (PlainKeywordToken) token;
			Token last = pkt.originalTokens.getLast();
			return matchRxL(symbolExtData, symbolExt, last);
		} else if (token.getClass() == GrammarKeywordToken.class) {
			GrammarKeywordToken gkt = (GrammarKeywordToken) token;
			Token last = gkt.originalTokens.getLast();
			return matchRxL(symbolExtData, symbolExt, last);
		} else if (token.getClass() == Token.class) {
			return matchRx(symbolExtData, symbolExt, token);
		} else
			throw new IllegalStateException("Unknown token class: " + token.getClass());
	}

	private boolean matchRxL(SymbolExtData symbolExtData, SymbolExt symbolExt, Token token,
			ParseTreeNode node) {
		if (token != null)
			return matchRxL(symbolExtData, symbolExt, token);
		else
			return matchRxL(symbolExtData, symbolExt, node);
	}

	/**
	 * Удалить неиспользуемые лексемы.
	 * 
	 * @param token
	 * @param grammemes
	 */
	public void removeUnmatchingLexemesGram(Grammemes grammemes, SymbolExt symbolExt, Token token) {
		Iterator<Lexeme> iterator = token.getLexemesIterator();
		while (iterator.hasNext()) {
			Lexeme l = iterator.next();
			if (!l.grammemes.hasAll(grammemes))
				iterator.remove();
		}
	}

	/**
	 * Удалить неиспользуемые лексемы.
	 * 
	 * @param token
	 * @param grammemes
	 */
	public static void removeUnmatchingLexemesGram(Token token, Set<GrammemeEnum> grammemes) {
		Iterator<Lexeme> iterator = token.getLexemesIterator();
		while (iterator.hasNext()) {
			Lexeme l = iterator.next();
			if (!l.grammemes.hasAll(grammemes))
				iterator.remove();
		}
	}


	public void removeUnmatchingLexemesGU(List<GUBlock> guList, SymbolExt symbolExt, Token token) {
		// если хотя бы одно вхождение в guList требует проверки всех лексем -
		// ничего не делаем
		Iterator<GUBlock> guIter = guList.iterator();
		while (guIter.hasNext())
			if (guIter.next().combined)
				return;

		Iterator<Lexeme> lexIter = token.getLexemesIterator();
		while (lexIter.hasNext()) {
			Lexeme lex = lexIter.next();
			guIter = guList.iterator();
			boolean matchAny = false;
			while (guIter.hasNext()) {
				GUBlock gu = guIter.next();
				if (lex.getGrammemes().hasAll(gu.grammemesSet))
					matchAny = true;
			}
			if (!matchAny)
				lexIter.remove();
		}
	}

	/**
	 * 
	 * Рассчитать показатель схожести граммем.
	 * 
	 * 
	 * @param grammemes
	 * @param grammemes2
	 * @param agrType
	 * @return
	 */
	public static int calculateMatchScore(Grammemes grammemes1, Grammemes grammemes2,
			SymbolExtDataTypes agreement) {
		int[] scores = Arrays.copyOf(baseScores, 14);
		if (agreement != null) {
			switch (agreement) {
				case gnc_agr:
					scores[GrammemeUtils.GNDR_NDX] = 3;
					scores[GrammemeUtils.NMBR_NDX] = 3;
					scores[GrammemeUtils.CASE_NDX] = 3;
					break;
				case nc_agr:
					scores[GrammemeUtils.NMBR_NDX] = 3;
					scores[GrammemeUtils.CASE_NDX] = 3;
					break;
				case c_agr:
					scores[GrammemeUtils.CASE_NDX] = 3;
					break;
				case gn_agr:
					scores[GrammemeUtils.GNDR_NDX] = 3;
					scores[GrammemeUtils.NMBR_NDX] = 3;
					break;
				case gc_agr:
					scores[GrammemeUtils.GNDR_NDX] = 3;
					scores[GrammemeUtils.CASE_NDX] = 3;
					break;
				default:
					throw new IllegalStateException("Unknown agreement: " + agreement);
			}
		}
		int result = 0;
		Grammemes intersect = GrammemeUtils.intersect(grammemes1, grammemes2);
		Set<Integer> intersectIndecies = intersect.getGrammemes().stream().map(t -> t.getIndex()/100)
				.collect(Collectors.toSet());


		for (int i = 0; i < 14; i++) {
			if (intersectIndecies.contains(i) || grammemes1.getByGrammemeGroupIndex(i) == null
					|| grammemes2.getByGrammemeGroupIndex(i) == null)
				result += scores[i];
			else
				result -= scores[i];
		}
		return result;
	}

	private static int[] baseScores;

	static {
		baseScores = new int[14];
		IntStream.range(0, 14).forEach(i -> baseScores[i] = 1);
		baseScores[GrammemeUtils.GNDR_NDX] = 2;
		baseScores[GrammemeUtils.NMBR_NDX] = 2;
		baseScores[GrammemeUtils.CASE_NDX] = 2;
	}

}
