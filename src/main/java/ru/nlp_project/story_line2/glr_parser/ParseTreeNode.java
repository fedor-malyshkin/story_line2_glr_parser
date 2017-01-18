package ru.nlp_project.story_line2.glr_parser;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;

public class ParseTreeNode {
	/**
	 * Level-order walker.
	 * 
	 * @author fedor
	 *
	 */
	public interface ICrossLevelOrderWalkProcessor {
		void nextLevel();

		void processNode(ParseTreeNode node);
	}

	public interface IInOrderWalkProcessor {

		void processTerminalNode(ParseTreeNode node);

		void processNonTerminalNode(ParseTreeNode node);

	}

	/**
	 * Order-level walker.
	 * 
	 * @author fedor
	 *
	 */
	public interface ILevelOrderWalkProcessor {
		void nextLevel(int level);

		void processNode(ParseTreeNode node, int level);
	}

	public interface IPostOrderWalkLeafFirstProcessor {
		void processNode(ParseTreeNode node) throws Exception;
	}

	/**
	 * Дочерние узлы.
	 */
	LinkedList<ParseTreeNode> children = new LinkedList<ParseTreeNode>();
	/**
	 * Начало значения терминала (для терминалов).
	 */
	int from;
	/**
	 * Признак наличия пометы "rt".
	 */
	boolean hasRt = false;
	/**
	 * Длина в символах (для терминалов).
	 */
	int length;
	/**
	 * Родительский узел.
	 */
	ParseTreeNode parent;
	/**
	 * Номер проекции.
	 */
	int prjPos = -1;
	/**
	 * Main word position (some for nonterminals, 0 for terminals);
	 */
	int rtPos;
	/**
	 * Сивол грамматики (дле терминалов/нетерминалов)
	 */
	Symbol symbol;
	/**
	 * Конкретный токен (для терминалов).
	 */
	Token token;
	/**
	 * Lenght of node in tokens (1 for single token)
	 */
	int tokenLength;

	/**
	 * Start of node in tokens (0 for first token)
	 */
	int tokenFrom;

	boolean isTerminal = true;

	private ParseTreeNode() {}

	public ParseTreeNode(int from, int length, Token token, Symbol symbol) {
		this.from = from;
		this.length = length;
		// use cloned version, because modify it in separate trees
		if (token != null)
			this.token = token.clone();
		this.symbol = symbol;
	}

	public ParseTreeNode(SPPFNode node, ParseTreeNode parent) {
		this.parent = parent;
		this.from = node.getFrom();
		this.length = node.getLength();
		// use cloned version, because modify it in separate trees
		if (node.getToken() != null)
			this.token = node.getToken().clone();
		this.symbol = node.getSymbol();
	}

	public void addChild(ParseTreeNode child) {
		this.isTerminal = false;
		child.parent = this;
		this.children.add(child);
	}

	public ParseTreeNode clone() {
		ParseTreeNode result = new ParseTreeNode();
		result.from = this.from;
		result.length = this.length;
		// use cloned version, because modify it in separate trees
		if (this.token != null)
			result.token = this.token.clone();
		result.symbol = this.symbol;

		result.tokenFrom = this.tokenFrom;
		result.tokenLength = this.tokenLength;
		result.prjPos = this.prjPos;
		result.hasRt = this.hasRt;
		result.rtPos = this.rtPos;

		result.isTerminal = this.isTerminal;
		return result;
	}

	/**
	 * Получить лексмеы токена узла. ВНИМАНИЕ: возвращется копия списка с настоящими узлами -
	 * поэтому изменения в список фактически не вносятся. А вот манипуляции с объектами списка
	 * отражаются на реальном содержании объекта.
	 * 
	 * При необходимости осуществлять манипуляции со списком -
	 * {@link ParseTreeNode#getLexemesIterator()}.
	 * 
	 * @return
	 */
	public LinkedList<Lexeme> getLexemesListCopy() {
		return token.getLexemesListCopy();
	}

	/**
	 * Получить итератор по настоящему списку лексем.
	 * 
	 * ВНИМАНИЕ: операци вставки и уадления находят фактическое отражение в состоянии объекта.
	 * 
	 * @return
	 */
	public ListIterator<Lexeme> getLexemesIterator() {
		return token.getLexemesIterator();
	}

	/**
	 * 
	 * Получить узел главного слова дерева анализа.
	 * 
	 * 
	 * @param includeNonTerminal если true - то могут быть возвращены нетерминалы.
	 * @return
	 */
	public ParseTreeNode getMainWordNode(boolean includeNonTerminal) {
		if (this.isTerminal)
			return this;
		else if (includeNonTerminal && this.hasRt)
			return this;
		else
			return this.children.get(this.rtPos).getMainWordNode(includeNonTerminal);
	}

	public boolean hasLexemes() {
		return token.hasLexemes();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (token != null)
			sb.append(String.format("<%s('%s'), %d,%d (%d,%d)>", symbol, token.getValue(),
					tokenFrom, tokenLength, from, length));
		else
			sb.append(String.format("<%s(), %d,%d (%d,%d)>", symbol, tokenFrom, tokenLength, from,
					length));

		if (children.size() > 0)
			sb.append("->" + children.toString());
		return sb.toString();
	}

	/**
	 * 
	 * Walk tree in level-order and visit each node on same level (left-right, top-down)
	 * (non-recursive method). Проход через дерево сверху-вниз по уровням (пересекающим все ветви
	 * дерева).
	 * 
	 * See: <a href="https://en.wikipedia.org/wiki/Tree_traversal">wiki page</a>
	 * 
	 * @throws Exception
	 * 
	 * 
	 */
	public void walkCrossLevelOrder(ICrossLevelOrderWalkProcessor processor) {
		int currLvlCounter = 1;
		int nextLvlCounter = 0;
		ParseTreeNode currNode = this;
		Deque<ParseTreeNode> nextNodes = new LinkedList<ParseTreeNode>();

		processor.processNode(currNode);
		currLvlCounter--;

		Iterator<ParseTreeNode> iterator = currNode.children.iterator();
		while (iterator.hasNext()) {
			nextNodes.addFirst(iterator.next());
			nextLvlCounter++;
		}

		if (currLvlCounter == 0) {
			currLvlCounter = nextLvlCounter;
			nextLvlCounter = 0;
			processor.nextLevel();
		}

		while (!nextNodes.isEmpty()) {
			currNode = nextNodes.pollLast();

			processor.processNode(currNode);
			currLvlCounter--;

			iterator = currNode.children.iterator();
			while (iterator.hasNext()) {
				nextNodes.addFirst(iterator.next());
				nextLvlCounter++;
			}

			if (currLvlCounter == 0) {
				currLvlCounter = nextLvlCounter;
				nextLvlCounter = 0;
				processor.nextLevel();
			}

		}

	}

	public void walkInOrder(IInOrderWalkProcessor processor) {
		walkInOrder(false, processor);
	}

	/**
	 * 
	 * Walk tree in in-order and visit each node (left-right, top-down) (non-recursive method).
	 * 
	 * 
	 * See: <a href="https://en.wikipedia.org/wiki/Tree_traversal">wiki page</a>
	 * 
	 * @param enableCut - enable "cut" restriction processing
	 * @param processor
	 */
	public void walkInOrder(boolean enableCut, IInOrderWalkProcessor processor) {
		class ParseTreeNodeVisitData {
			boolean processed = false;
			ParseTreeNode node = null;

			public ParseTreeNodeVisitData(ParseTreeNode node, boolean processed) {
				super();
				this.node = node;
				this.processed = processed;
			}

		}
		Deque<ParseTreeNodeVisitData> parentStack = new LinkedList<ParseTreeNodeVisitData>();

		if (!enableCut || !hasCutRestriction(this))
			parentStack.addFirst(new ParseTreeNodeVisitData(this, false));

		while (!parentStack.isEmpty()) {
			ParseTreeNodeVisitData data = parentStack.pollFirst();
			ParseTreeNode currNode = data.node;
			if (data.processed) {
				if (currNode.isTerminal)
					processor.processTerminalNode(currNode);
				else
					processor.processNonTerminalNode(currNode);

				continue;
			}
			// сам родительский как уже отработанный
			parentStack.addFirst(new ParseTreeNodeVisitData(currNode, true));
			// потом дети как неотработанные (кроме терминалов)
			ListIterator<ParseTreeNode> iterator =
					currNode.children.listIterator(currNode.children.size());
			while (iterator.hasPrevious()) {
				ParseTreeNode node = iterator.previous();
				// пока это терминалы и не наткнулись ни на один нетерминал...
				if (!enableCut || !hasCutRestriction(node))
					parentStack.addFirst(new ParseTreeNodeVisitData(node, node.isTerminal));
			}
		}
	}

	private boolean hasCutRestriction(ParseTreeNode node) {
		if (!SymbolExt.class.isAssignableFrom(node.symbol.getClass()))
			return false;
		if (node.symbol == Symbol.EPSILON)
			return false;
		SymbolExt symbolExt = (SymbolExt) node.symbol;
		return symbolExt.getExtDatasMap().containsKey(SymbolExtDataTypes.cut);
	}

	/**
	 * 
	 * Walk tree in level-order and visit each node on same level (left-right, top-down)
	 * (non-recursive method). Проход через дерево сверху-вниз по уровням (не пересекающим одну
	 * ветвь дерева).
	 * 
	 * See: <a href="https://en.wikipedia.org/wiki/Tree_traversal">wiki page</a>
	 * 
	 * @throws Exception
	 * 
	 * 
	 */
	public void walkLevelOrder(ILevelOrderWalkProcessor processor) {
		class ParseTreeNodeVisitData {
			int level = 0;
			ParseTreeNode node;

			public ParseTreeNodeVisitData(ParseTreeNode node, int level) {
				super();
				this.node = node;
				this.level = level;
			}
		}
		ParseTreeNodeVisitData data;
		int level = 0;
		ParseTreeNode currNode = this;
		Deque<ParseTreeNodeVisitData> nextNodes = new LinkedList<ParseTreeNodeVisitData>();
		processor.processNode(currNode, level);
		processor.nextLevel(level);
		nextNodes.addLast(new ParseTreeNodeVisitData(currNode, level + 1));

		while (!nextNodes.isEmpty()) {
			data = nextNodes.pollFirst();
			currNode = data.node;
			level = data.level;
			// don't process empty children's nodes
			if (currNode.children.size() == 0)
				continue;

			Iterator<ParseTreeNode> iterator = currNode.children.iterator();
			while (iterator.hasNext()) {
				ParseTreeNode child = iterator.next();
				processor.processNode(child, level);
				nextNodes.addLast(new ParseTreeNodeVisitData(child, level + 1));
			}
			processor.nextLevel(level);
		}

	}

	/**
	 * 
	 * Walk tree in post-order and visit each node (left-right, down-top) (non-recursive method).
	 * При обходе в первую очередь обрабатываются конечные узлы (листья), потом их родители. Т.е.
	 * при посещении любого узла, ты уже можешь быть уверен, что в дочерних узлах уже был.
	 * 
	 * See: <a href="https://en.wikipedia.org/wiki/Tree_traversal">wiki page</a>
	 * 
	 * @throws Exception
	 * 
	 * 
	 */
	public void walkPostOrderLeafFirst(IPostOrderWalkLeafFirstProcessor processor)
			throws Exception {
		class ParseTreeNodeVisitData {
			Iterator<ParseTreeNode> iter;
			ParseTreeNode node;

			public ParseTreeNodeVisitData(ParseTreeNode node) {
				this.node = node;
				this.iter = node.children.iterator();
			}
		}

		Deque<ParseTreeNodeVisitData> visitStack = new LinkedList<ParseTreeNodeVisitData>();

		ParseTreeNodeVisitData currData = new ParseTreeNodeVisitData(this);
		while (!visitStack.isEmpty() || currData != null) {
			if (currData != null) {
				visitStack.addFirst(currData);
				currData = currData.iter.hasNext()
						? new ParseTreeNodeVisitData(currData.iter.next()) : null;
			} else {
				ParseTreeNodeVisitData pollData = visitStack.peekFirst();
				if (pollData.iter.hasNext()) {
					currData = new ParseTreeNodeVisitData(pollData.iter.next());
				} else {
					// remove processed node from stack
					visitStack.pollFirst();
					// process node
					processor.processNode(pollData.node);
				} // if (pollData.iter.hasNext()) {
			} // if (currNode != null) {
		} // while (!visitStack.isEmpty() || currNode != null) {
	}

}
