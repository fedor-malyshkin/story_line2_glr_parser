package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ru.nlp_project.story_line2.glr_parser.SymbolExt;
import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

/**
 * RNGLR analyser.
 * 
 * It works according to the paper Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006.
 * 
 * <b>BUG:</b> При наличии в грамматике правила типа "E->FG" (при "F-*->EPSILON" и "G-*->EPSILON") и наличии в состояния DFA LR1-point типа 
 * "E->*FG, $" (формирующих свертку/reduction типа r(E,0, index nullable part of FG)) при парсинге строки в которой 
 * нет значения для E в итоговом дереве узел заполняется epsilon-SPPF узлом nullable part FG, а не E.
 * 
 * <b>Возможное решение:</b> формировать epsilon-SPPF узлы nullable part с учетом не просто длинны, но и порождения - т.е. в нащем случа было бы,
 * не 2 отдельных узла, а родительский E и дочерний узел FG (с 2-я дочерними F и G). Потом при парсинге отслеживать именно такие ситуации и 
 * вместо epsilon-SPPF nullable part RHS подставлять epsilon-SPPF nullable part LHS.  
 * 
 * 
 * @author fedor
 *
 */
public class RNGLRAnalyser extends RNGLRAnalyserAlg1e {

  protected class GSS2 extends GSS {
    public GSS2() {
      super();
    }

    GSSEdge2 getOrCreateEdge(GSSNode fromNode, GSSNode toNode, SPPFNode node) {
      GSSNode fromN = getNode(fromNode.uState, fromNode.dfaState);
      GSSNode toN = getNode(toNode.uState, toNode.dfaState);
      if (fromN == null || toN == null)
        return null;
      // if exist - return existing
      Set<GSSEdge> tmp = fromN.edges;
      for (GSSEdge edge : tmp)
        if (edge.successor.equals(toN))
          return (GSSEdge2) edge;

      // if not exist - return new
      GSSEdge2 edge = new GSSEdge2(fromN, toN, node);
      fromN.edges.add(edge);
      return edge;
    }

  }

  protected class GSSEdge2 extends GSSEdge {
    SPPFNode sppfNode = null;

    public GSSEdge2(GSSNode from, GSSNode to, SPPFNode sppfNode) {
      super(from, to);
      this.sppfNode = sppfNode;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (!super.equals(obj))
        return false;
      if (getClass() != obj.getClass())
        return false;
      GSSEdge2 other = (GSSEdge2) obj;
      if (sppfNode == null) {
        if (other.sppfNode != null)
          return false;
      } else if (!sppfNode.equals(other.sppfNode))
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((sppfNode == null) ? 0 : sppfNode.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "Edg[" + successor.uState + "." + successor.dfaState + "<-"
          + predcessor.uState + "." + predcessor.dfaState + " <" + sppfNode
          + ">]";
    }

  }

  protected class RN2ReduceInst extends RNReduceInst {

    /**
     * f is the index of the required nullable part at the righthand end of the reduction 
     * ( f = 0 if the reduction is not right nullable)
     */
    int rn2ReductionIndexFunc;
    /**
     * SPPF node labels the first edge of the path down which the reduction is applied (if m = 0, then y = EPSILON).
     */
    SPPFNode sppfNode;
    /**
     * Projection position of original grammar where we definine such non terminal to make reduction.
     */
    int rn2ProjectionPos;

    public RN2ReduceInst(GSSNode fromNode, Symbol symbol,
        int reductionSymbolCount, int rn2ReductionIndexFunc, SPPFNode sppfNode,
        int projPos) {
      super(fromNode, symbol, reductionSymbolCount);
      this.rn2ReductionIndexFunc = rn2ReductionIndexFunc;
      this.sppfNode = sppfNode;
      this.rn2ProjectionPos = projPos;
    }

    @Override
    public String toString() {
      return "[" + fromNode.uState + "." + fromNode.dfaState + ", " + symbol
          + ", " + reductionSymbolCount + ", " + rn2ReductionIndexFunc + ", "
          + sppfNode + "(" + rn2ProjectionPos + ")]";
    }
  }

  protected class SPPF {

    private class NullablePartSPPFNodes {
      SPPFNode node;
      List<Symbol> nullablePart;

      public NullablePartSPPFNodes(List<Symbol> nullablePart, SPPFNode node) {
        super();
        this.nullablePart = nullablePart;
        this.node = node;
      }

    }

    private SPPFNode epsilonNode = null;
    private List<NullablePartSPPFNodes> functionIndexToSPPFNodeList;
    private Map<List<Symbol>, NullablePartSPPFNodes> nullablePartsToSPPFNodeMap;
    private SPPFNode rootNode = null;

    public void addChildren(SPPFNode y, List<SPPFNode> yNodes, int f,
        int projectionPos) {
      List<SPPFNode> g = new ArrayList<RNGLRAnalyser.SPPFNode>(yNodes);
      // if f = 0 let G= ( y1, . . . , ym) else let G= ( y1, . . . , ym, xf)
      if (f != 0)
        g.add(getNodeByFunctionIndex(f));
      // if G does not already belong to the family of y {
      if (!y.belongs(g, projectionPos)) {
        // if y has no children then add edges from y to each node in G
        // else
        // if the children of y are not packed nodes {
        // create a new packed node z and a tree edge from y to z
        // add tree edges from z to all the other children of y
        // remove all tree edges from y apart from the one to z }
        // create a new packed node t and a new tree edge from y to t
        // create new edges from t to each node in } }
        y.addChildren(g, projectionPos);
      }
    }

    /**
     * Create ε-SPPFs nodes.
     * 
     * Создаем объеты - "обработанные узлы" (содержащие ссылки на узлы для nullable parts (epsilon sppf node))
     * Вначале сортируем по длинне, и проходим в порядке возрастания создавая дочерние связи.
     * 
     * Пример итоговой схемы см. "Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006"
     */
    public void createEpsilonSPPFNodes() {
      this.epsilonNode = new SPPFNode(Symbol.EPSILON, 0, 0);
      functionIndexToSPPFNodeList = new ArrayList<NullablePartSPPFNodes>();
      functionIndexToSPPFNodeList.addAll(Collections.nCopies(
          lr1ParseTableBuilder.getNullablePartsMap().size() + 1,
          (NullablePartSPPFNodes) null));
      nullablePartsToSPPFNodeMap =
          new HashMap<List<Symbol>, NullablePartSPPFNodes>();
      // fill data structures
      for (Entry<List<Symbol>, Integer> entry : lr1ParseTableBuilder
          .getNullablePartsMap().entrySet()) {
        SPPFNode node = new SPPFNode(
            new Symbol(entry.getKey().toString(), SymbolTypes.LiteralString), 0,
            0);
        NullablePartSPPFNodes npSPPFnode =
            new NullablePartSPPFNodes(entry.getKey(), node);

        functionIndexToSPPFNodeList.set(entry.getValue(), npSPPFnode);
        nullablePartsToSPPFNodeMap.put(entry.getKey(), npSPPFnode);

        // add epsilon node for 1 symbol length nullable parts
        if (entry.getKey().size() == 1)
          node.addChildren(Collections.singletonList(epsilonNode), -1);

      }
      // make links
      for (int i = 1; i < functionIndexToSPPFNodeList.size(); i++) {
        NullablePartSPPFNodes npSPPFnode = functionIndexToSPPFNodeList.get(i);
        if (npSPPFnode.nullablePart.size() == 1)
          continue;
        List<SPPFNode> children = new ArrayList<RNGLRAnalyser.SPPFNode>(
            npSPPFnode.nullablePart.size());
        for (Symbol np : npSPPFnode.nullablePart)
          children.add(nullablePartsToSPPFNodeMap
              .get(Collections.singletonList(np)).node);

        npSPPFnode.node.addChildren(children, -1);
      }

    }

    public SPPFNode getEpsilonNode() {
      return epsilonNode;
    }

    public SPPFNode getNodeByFunctionIndex(int f) {
      return functionIndexToSPPFNodeList.get(f).node;
    }

    public SPPFNode getRootNode() {
      return rootNode;
    }

    public void setRootNode(SPPFNode node) {
      this.rootNode = node;
    }

  }

  public class SPPFNode implements Comparable<SPPFNode> {
    /**
     * Контейнеры для узлов (аналоги packed nodes для SPPF, но для всех услов).
     */
    List<SPPFNodeContainer> containers = new ArrayList<SPPFNodeContainer>();
    /**
     * Для случая, когда узел создан на базе токена - его старт.
     */
    int from;
    /**
     * Для случая, когда узел создан на базе сивола - он сам.
     */
    Symbol symbol;
    /**
     * Для случая, когда узел создан на базе токена - его длинна.
     */
    int length;
    /**
     * Для случая, когда узел создан на базе токена - он сам.
     */
    Token token;

    public SPPFNode(Symbol symbol, int from, int length) {
      super();
      this.symbol = symbol;
      this.from = from;
      this.length = length;
    }

    public SPPFNode(Token token, int from, int length) {
      super();
      this.token = token;
      this.from = from;
      this.length = length;
    }

    public void addChildren(List<SPPFNode> g, int projectionPos) {
      containers.add(new SPPFNodeContainer(g, projectionPos));
    }

    /**
     * TODO: проверить поведение, когда присоединяются и выполняется проверка узлов (g) с дочерней структурой
     * 
     * @param g
     * @return
     */
    public boolean belongs(List<SPPFNode> g, int projectionPos) {
      for (SPPFNodeContainer cont : containers)
        if (cont.children.equals(g) && cont.projectionPos == projectionPos)
          return true;
      return false;
    }

    @Override
    public int compareTo(SPPFNode o) {
      return Integer.compare(hashCode(), o.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SPPFNode other = (SPPFNode) obj;
      if (from != other.from)
        return false;
      if (symbol == null) {
        if (other.symbol != null)
          return false;
      } else if (!symbol.equals(other.symbol))
        return false;
      if (length != other.length)
        return false;
      if (token == null) {
        if (other.token != null)
          return false;
      } else if (!token.equals(other.token))
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + from;
      result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
      result = prime * result + length;
      result = prime * result + ((token == null) ? 0 : token.hashCode());
      return result;
    }

    public boolean isPackedNode() {
      return containers.size() > 1;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (symbol != null)
        sb.append("<'" + symbol + "', " + from + ", " + length + ">");
      else
        sb.append("<'" + token.getValue() + "', " + from + ", " + length + ">");

      if (containers.size() > 0)
        sb.append("->" + containers.toString());
      return sb.toString();
    }

    public int getFrom() {
      return from;
    }

    public Symbol getSymbol() {
      return symbol;
    }

    public int getLength() {
      return length;
    }

    public Token getToken() {
      return token;
    }

  }

  public SPPFNode createSPPFNode(Symbol symbol, int from, int length) {
    return new SPPFNode(symbol, from, length);
  }

  public SPPFNode createSPPFNode(Token token, int from, int length) {
    return new SPPFNode(token, from, length);
  }

  protected class SPPFNodeContainer {
    /**
     * Номер проекции в соотвествии с которой построены дочерние узлы.
     */
    int projectionPos;
    /*
     * Дочерние узлы в контейнере.
     */
    List<SPPFNode> children = new ArrayList<SPPFNode>();

    public SPPFNodeContainer() {
    }

    public SPPFNodeContainer(List<SPPFNode> children, int projectionPos) {
      super();
      this.children = new ArrayList<SPPFNode>(children);
      this.projectionPos = projectionPos;
    }

    @Override
    public String toString() {
      if (children.size() == 0)
        return "";
      return children.toString() + "(" + this.projectionPos + ")";
    }
  }

  private GSS2 gss2;

  private LR1ParseTableBuilder lr1ParseTableBuilder;
  private Map<SPPFNode, SPPFNode> NSet;
  private Queue<RN2ReduceInst> RSet;
  private SPPF sppf;
  private List<Map<Symbol, List<Symbol>>> baseNTToNTExtData;

  public RNGLRAnalyser(Grammar grammar,
      List<Map<Symbol, List<ActionRecord>>> actionTable,
      List<List<LR1Point>> states, IGLRTokenMatcher tokenMatcher,
      LR1ParseTableBuilder lr1ParseTableBuilder) {
    this(grammar, actionTable, states, tokenMatcher, lr1ParseTableBuilder,
        null);
  }

  public RNGLRAnalyser(Grammar grammar,
      List<Map<Symbol, List<ActionRecord>>> actionTable,
      List<List<LR1Point>> states, IGLRTokenMatcher tokenMatcher,
      LR1ParseTableBuilder lr1ParseTableBuilder,
      List<Map<Symbol, List<Symbol>>> baseNTToNTExtData) {
    super(grammar, actionTable, states, tokenMatcher);
    this.lr1ParseTableBuilder = lr1ParseTableBuilder;
    this.baseNTToNTExtData = baseNTToNTExtData;
    this.tokenMatcher = tokenMatcher;
    this.grammar = grammar;
    this.states = states;
    this.actionTable = actionTable;

    this.RSet = new ArrayDeque<RN2ReduceInst>();
    this.QSet = new ArrayDeque<ShiftInst>();
    this.NSet = new TreeMap<SPPFNode, SPPFNode>();

  }

  /**
   * find the set χ of paths of length distance (or length 0 if m = 0) from v
   * 
   * @param v
   * @param distance
   * @return
   */
  private List<List<GSSEdge2>> getGSSPathesFrom(GSSNode v, int distance) {
    class ReachedNode {
      int dist;
      GSSNode node;
      List<GSSEdge2> path;

      public ReachedNode(GSSNode node, int distance, List<GSSEdge2> path) {
        super();
        this.node = node;
        this.dist = distance;
        this.path = path;
      }
    }

    Queue<ReachedNode> reacheNodeProcessingQueue =
        new ArrayDeque<ReachedNode>();
    List<List<GSSEdge2>> result = new ArrayList<List<GSSEdge2>>();
    // special case for distance == 0
    if (distance == 0)
      result.add(new ArrayList<GSSEdge2>());
    for (GSSEdge edge : v.edges) {
      List<GSSEdge2> path = new ArrayList<GSSEdge2>();
      path.add((GSSEdge2) edge);
      reacheNodeProcessingQueue.add(new ReachedNode(edge.successor, 1, path));
    }
    while (!reacheNodeProcessingQueue.isEmpty()) {
      ReachedNode rn = reacheNodeProcessingQueue.poll();
      if (rn.dist == distance) {
        result.add(rn.path);
        continue;
      }
      if (rn.node.edges == null)
        continue;
      Iterator<GSSEdge> iter = rn.node.edges.iterator();
      // "first edge processed" flag
      while (iter.hasNext()) {
        GSSEdge2 edge = (GSSEdge2) iter.next();
        List<GSSEdge2> path = new ArrayList<GSSEdge2>(rn.path);
        path.add(edge);
        reacheNodeProcessingQueue
            .add(new ReachedNode(edge.successor, rn.dist + 1, path));
      }
    }
    return result;
  }

  private List<ActionRecord> getRN2ReduceActionRecords(int dfaState,
      Token token, IGLRTokenMatcher tokenMatcher) {
    List<ActionRecord> result = new ArrayList<ActionRecord>();
    Collection<ActionRecord> actionRecords = tokenMatcher.getActionTableRecords(
        actionTable.get(dfaState), baseNTToNTExtData.get(dfaState), token);
    if (null != actionRecords)
      for (ActionRecord ar : actionRecords) {
        if (ar.rn2Reduce)
          result.add(ar);
        if (ar.rnReduce)
          throw new IllegalStateException(
              "In action table found rnReduce entry (not rn2reduce entry)");
      }
    return result;
  }

  public SPPFNode getRootNode() {
    return sppf.getRootNode();
  }

  private List<ActionRecord> getShiftActionRecords(int dfaState, Symbol x) {
    List<ActionRecord> result = new ArrayList<ActionRecord>();
    // отработать вариант, когда следующими шагами для свертки или продвижения
    // могут быть нетерминалы с пометами, а последним шагом был нетерминал без
    // помет, а это разные символы -- поэтому заглядываем в заранее
    // подготовленный массив для каждого шага, где есть маппинг от базовых
    // сиволов к символам с пометами, котороые могут появиться на этой стадии
    if (baseNTToNTExtData != null) {
      List<Symbol> symbols = baseNTToNTExtData.get(dfaState).get(x);
      for (Symbol s : symbols) {
        if (s.getClass() == Symbol.class)
          continue;
        if (((SymbolExt) s).getExtDatas().isEmpty())
          continue;
        List<ActionRecord> actionRecords = actionTable.get(dfaState).get(s);
        if (null != actionRecords)
          actionRecords.stream().filter(ar -> ar.shift)
              .forEach(ar -> result.add(ar));
      }
    } // if (baseNTToNTExtData != null) {
    List<ActionRecord> actionRecords = actionTable.get(dfaState).get(x);
    if (null != actionRecords)
      actionRecords.stream().filter(ar -> ar.shift)
          .forEach(ar -> result.add(ar));
    return result;
  }

  /**
   * Get shift action records from action table;
   * 
   * 
   * @param dfaState
   * @param token token from input stream (tokenMatcher detect with entry of action table to use)
   * @param tokenMatcher {@link ITokenMatcher } instance
   * @return
   */
  private List<ActionRecord> getShiftActionRecords(int dfaState, Token token,
      IGLRTokenMatcher tokenMatcher) {
    List<ActionRecord> result = new ArrayList<ActionRecord>();
    Collection<ActionRecord> actionRecords = tokenMatcher.getActionTableRecords(
        actionTable.get(dfaState), baseNTToNTExtData.get(dfaState), token);
    if (null != actionRecords)
      for (ActionRecord ar : actionRecords)
        if (ar.shift)
          result.add(ar);
    return result;
  }

  /**
   * Check if action record table contains accept state.
   * 
   * @param dfaState DFA state number
   * @param symbol lookup symbol in action table
   * @param tokenMatcher {@link ITokenMatcher } instance
   * @return
   */
  protected boolean hasAccActionRecord(int dfaState, Symbol symbol,
      IGLRTokenMatcher tokenMatcher) {
    List<ActionRecord> ars = actionTable.get(0).get(symbol);
    if (null == ars)
      return false;
    for (ActionRecord ar : ars)
      if (ar.accept)
        return true;
    return false;
  }

  /*
   * Input: <ul> <li>an RN table T</li> <li>an input string a1 . . . ad</li>
   * <li>the ε-SPPFs for each nullable nonterminal</li> <li>required nullable
   * part ω</li> <li>the root nodes xI(ω) of these ε-SPPFs</li> </ul>
   */
  @Override
  public boolean processTokens(List<Token> tokensOriginal) {
    // временный массив, куда могут добавляться служебные токены (типа EOI)
    ArrayList<Token> tokens = new ArrayList<Token>(tokensOriginal);
    sppf = new SPPF();
    sppf.createEpsilonSPPFNodes();

    // if d = 0
    if (tokens.size() == 0) {
      // if acc ∈ T(0, $)
      if (hasAccActionRecord(0, Symbol.EOI, tokenMatcher)) {
        // report success and output the SPPF whose root node is xI(S)
        throw new IllegalStateException("Not implemented yet...");
      } else
        // else report failure
        return false;
    } else {
      gss2 = new GSS2();
      // create a node v0 labelled with the start state 0 of the DFA
      // set U0 = {v0}, R = ∅, Q = ∅, ad+1 = $, U1 = ∅, ..., Ud = ∅
      gss2.addNode(0, 0);
      GSSNode v0 = gss2.getNode(0, 0);
      RSet.clear();
      QSet.clear();
      tokens.add(new Token(0, 0, null, TokenTypes.EOI));
      for (int i = 1; i <= tokens.size(); i++)
        gss2.clearUState(i);

      // if pk ∈ T(0, a1) add (v0, k) to Q
      List<ActionRecord> ars =
          getShiftActionRecords(0, tokens.get(0), tokenMatcher);
      for (ActionRecord ar : ars)
        QSet.add(new ShiftInst(v0, ar.shiftState));

      // forall r(X, 0, f) ∈ T(0, a1) add (v0, X, 0, f, ε) to R
      ars = getRN2ReduceActionRecords(0, tokens.get(0), tokenMatcher);
      for (ActionRecord ar : ars) {
        RSet.add(new RN2ReduceInst(v0, ar.rnReductionSymbol,
            ar.rnReductionSymbolCount, ar.rn2ReductionIndexFunc,
            sppf.getEpsilonNode(), ar.rn2ProjectionPos));
      }

      // for i = 0 to d
      for (int i = 0; i < tokens.size(); i++) {
        // while Ui != ∅ {
        // OLD CODE: while (!gss2.getUState(i).isEmpty()) {

        // N = ∅ (contains the SPPF nodes constructed at this step)
        NSet.clear();
        // while R != ∅ do REDUCER(i)
        while (!RSet.isEmpty())
          rn2reduce(i, tokens);
        // do SHIFTER(i) }
        rn2shift(i, tokens);

      }

      // if the DFA accepting state l labels an element t ∈ Ud {
      Set<Integer> accStates = new TreeSet<Integer>(getDFAAcceptingStates());
      Collection<GSSNode> ud = gss2.getUState(tokens.size() - 1);
      for (GSSNode node : ud)
        if (accStates.contains(node.dfaState)) {
          // let root-node be the SPPF node that labels the edge (t, v0 ) in
          // the GSS, remove nodes in the SPPF not reachable from root-node and
          // report success
          for (GSSEdge edge : node.edges) {
            if (edge.successor.equals(v0)) {
              GSSEdge2 edge2 = (GSSEdge2) edge;
              sppf.setRootNode(edge2.sppfNode);
              return true;
            }
          }
          throw new IllegalStateException("Not found root node for SPPF!");
        }
      // else report failure } } }
      return false;
    }
  }

  private void rn2reduce(int uState, List<Token> tokens) {
    // remove (v, X, m, f, y) from R
    while (!RSet.isEmpty()) {
      RN2ReduceInst ri = RSet.poll();
      GSSNode v = ri.fromNode;
      Symbol X = ri.symbol;
      int m = ri.reductionSymbolCount;
      int f = ri.rn2ReductionIndexFunc;
      SPPFNode y = ri.sppfNode;
      // find the set χ of paths of length (m − 1) (or length 0 if m = 0) from v
      List<List<GSSEdge2>> pathes = getGSSPathesFrom(v, m > 0 ? m - 1 : 0);

      // if m != 0 let ym = y
      SPPFNode ym = null;
      if (m != 0)
        ym = y;
      // for each path in χ do {
      for (List<GSSEdge2> path : pathes) {
        // let ym−1, . . . , y1 be the edge labels on the path and let u be
        // the final node on the path
        GSSNode u = null;
        if (m == 1 || m == 0)
          u = v;
        else
          u = path.get(path.size() - 1).successor;

        // extract edge SPPF nodes
        List<SPPFNode> yNodes = new ArrayList<SPPFNode>();
        if (m > 0) {
          for (int i = path.size() - 1; i > -1; i--)
            yNodes.add(path.get(i).sppfNode);

          yNodes.add(ym);
        }

        // (if m = 1 then y = y1)
        if (m == 1)
          yNodes.set(yNodes.size() - 1, y);

        // let k be the label of u and let pl ∈ T(k, X)
        List<ActionRecord> shiftActionRecords =
            getShiftActionRecords(u.dfaState, X);
        for (ActionRecord sar : shiftActionRecords) {
          int l = sar.shiftState;
          // if m = 0 let z = xf
          SPPFNode z = null;
          if (m == 0)
            z = sppf.getNodeByFunctionIndex(f);
          else {
            // suppose that u ∈ Uc
            int c = u.uState;
            // if there is no node z in N labelled (X, c) create one and add z
            // to N
            z = new SPPFNode(X, c, 0);
            if (NSet.containsKey(new SPPFNode(X, c, 0)))
              z = NSet.get(z); // т.к. оригинальный узел может уже содержать
                               // дочерныие усзлы
            else
              NSet.put(z, z);
          }
          // if there is an element w ∈ Ui with label l
          GSSNode w = gss2.getNode(uState, l);
          if (w != null) {
            // if there is not an edge from (w, u) {
            GSSEdge2 edgeWU = (GSSEdge2) gss2.getEdge(w, u);
            if (null == edgeWU) {
              // create one and add z to N }
              gss2.getOrCreateEdge(w, u, z);
              // if m != 0 { forall r(B,t,f) ∈ T(l, ai+1) where t != 0, add
              // (u,B,t,f,z) to R
              if (m != 0) {
                List<ActionRecord> ras = getRN2ReduceActionRecords(l,
                    tokens.get(uState), tokenMatcher);
                for (ActionRecord ra : ras) {
                  if (ra.rnReductionSymbolCount != 0)
                    RSet.add(new RN2ReduceInst(u, ra.rnReductionSymbol,
                        ra.rnReductionSymbolCount, ra.rn2ReductionIndexFunc, z,
                        ra.rn2ProjectionPos));
                }
              }
            }
          } else {
            // else { create a new GSS node w labelled l and an edge (w, u)
            // labelled z
            w = gss2.addNode(uState, l);
            gss2.getOrCreateEdge(w, u, z);
            // if ph ∈ T(l , ai+1) add (w, h) to Q
            List<ActionRecord> sars =
                getShiftActionRecords(l, tokens.get(uState), tokenMatcher);
            for (ActionRecord sa : sars)
              QSet.add(new ShiftInst(w, sa.shiftState));
            // forall reductions r(B,0,f) ∈ T(l, ai+1) add (w,B,0,f,ε) to R
            List<ActionRecord> ras =
                getRN2ReduceActionRecords(l, tokens.get(uState), tokenMatcher);
            for (ActionRecord ra : ras) {
              if (ra.rnReductionSymbolCount == 0)
                RSet.add(new RN2ReduceInst(w, ra.rnReductionSymbol, 0,
                    ra.rn2ReductionIndexFunc, sppf.getEpsilonNode(),
                    ra.rn2ProjectionPos));
            }
            // if m != 0 { forall r(B,t,f) ∈ T(l,ai+1) where t != 0 add
            // (u,B,t,f,z) to R
            if (m != 0) {
              ras = getRN2ReduceActionRecords(l, tokens.get(uState),
                  tokenMatcher);
              for (ActionRecord ra : ras) {
                if (ra.rnReductionSymbolCount != 0)
                  RSet.add(new RN2ReduceInst(u, ra.rnReductionSymbol,
                      ra.rnReductionSymbolCount, ra.rn2ReductionIndexFunc, z,
                      ra.rn2ProjectionPos));
              }
            }
          }
          // if m != 0 ADD CHILDREN(z, y1, . . . , ym, f) } }
          if (m != 0)
            sppf.addChildren(z, yNodes, f, ri.rn2ProjectionPos);
        } // for (ActionRecord sar: shiftActionRecords) {
      } // for (List<GSSEdge2> path : pathes) {
    } // while (iter.hasNext()) {

  }

  private void rn2shift(int uState, List<Token> tokens) {
    // if i != d {
    if (uState >= tokens.size() - 1)
      return;
    // Q' = ∅ (a temporary set to hold new shifts)
    List<ShiftInst> Q2Set = new ArrayList<ShiftInst>();
    // create a new SPPF node z labelled (ai+1, i)
    Token token = tokens.get(uState);
    SPPFNode z = new SPPFNode(token, token.getFrom(), token.getLength());

    // while Q != ∅ do {
    while (!QSet.isEmpty()) {
      ShiftInst si = QSet.poll();
      // if there is w ∈ Ui+1 with label k {
      GSSNode w = gss2.getNode(uState + 1, si.toDFAState);
      if (w != null) {
        // create an edge (w, v) labelled z
        gss2.getOrCreateEdge(w, si.fromNode, z);
        // forall r(B, t, f) ∈ T(k, ai+2) where t != 0 add (v, B, t, f, z) to
        // R }
        List<ActionRecord> ras = getRN2ReduceActionRecords(si.toDFAState,
            tokens.get(uState + 1), tokenMatcher);
        for (ActionRecord ra : ras) {
          if (ra.rnReductionSymbolCount != 0)
            RSet.add(new RN2ReduceInst(si.fromNode, ra.rnReductionSymbol,
                ra.rnReductionSymbolCount, ra.rn2ReductionIndexFunc, z,
                ra.rn2ProjectionPos));
        }
      } else {
        // create a new node, w ∈ Ui+1, labelled k and an edge (w, v) labelled
        // z
        w = gss2.addNode(uState + 1, si.toDFAState);
        gss2.getOrCreateEdge(w, si.fromNode, z);

        // if ph ∈ T(k, ai+2) add (w, h) to Q'
        List<ActionRecord> sas = getShiftActionRecords(si.toDFAState,
            tokens.get(uState + 1), tokenMatcher);
        for (ActionRecord sa : sas)
          Q2Set.add(new ShiftInst(w, sa.shiftState));

        // forall r(B,t,f) ∈ T(k, ai+2) where t != 0 add (v,B,t,f,z) to R
        // forall r(B,0,f) ∈ T(k, ai+2) add (w,B,0,f,ε) to R } }
        List<ActionRecord> ras = getRN2ReduceActionRecords(si.toDFAState,
            tokens.get(uState + 1), tokenMatcher);
        for (ActionRecord ra : ras)
          // forall r(B,t,f) ∈ T(k, ai+2) where t != 0 add (v,B,t,f,z) to R
          if (ra.rnReductionSymbolCount != 0)
            RSet.add(new RN2ReduceInst(si.fromNode, ra.rnReductionSymbol,
                ra.rnReductionSymbolCount, ra.rn2ReductionIndexFunc, z,
                ra.rn2ProjectionPos));
          else
            // forall r(B,0,f) ∈ T(k, ai+2) add (w,B,0,f,ε) to R } }
            RSet.add(new RN2ReduceInst(w, ra.rnReductionSymbol, 0,
                ra.rn2ReductionIndexFunc, sppf.getEpsilonNode(),
                ra.rn2ProjectionPos));
      }
    }
    // copy Q' into Q } }
    QSet.addAll(Q2Set);
  }
}
