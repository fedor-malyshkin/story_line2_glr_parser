package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.list.LazyList;

import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;

/**
 * RNGLR analyser (based on Tomita's algorithm 1 - algorithm 1e).
 * 
 * @author fedor
 * 
 * It works according to the paper Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006. 
 *
 */
public class RNGLRAnalyserAlg1e {
  protected class ArrayListMapNodeFactory implements
      Factory<Map<Integer, GSSNode>> {
    @Override
    public Map<Integer, GSSNode> create() {
      return new HashMap<Integer, GSSNode>();
    }

  }

  protected class GSS {
    /**
     * U states. Array with Map (key - dfa state, value - node itself).
     */
    private List<Map<Integer, GSSNode>> uStates = null;

    GSS() {
      this.uStates = LazyList.lazyList(new ArrayList<Map<Integer, GSSNode>>(),
          new ArrayListMapNodeFactory());
    }

    GSSNode addNode(int uState, int dfaState) {
      GSSNode result = new GSSNode(uState, dfaState);
      uStates.get(uState).put(dfaState, result);
      return result;
    }

    void clearUState(int uState) {
      uStates.get(uState).clear();
    }

    void clearUStates() {
      uStates.clear();
    }

    GSSEdge getEdge(GSSNode fromNode, GSSNode toNode) {
      GSSNode fromN = getNode(fromNode.uState, fromNode.dfaState);
      GSSNode toN = getNode(toNode.uState, toNode.dfaState);
      if (fromN == null || toN == null)
        return null;
      for (GSSEdge edge : fromN.edges)
        if (edge.successor.equals(toN))
          return edge;
      // nothing if not found
      return null;
    }

    GSSNode getNode(int uState, int dfaState) {
      return uStates.get(uState).get(dfaState);
    }

    private GSSEdge getOrCreateEdge(GSSNode fromNode, GSSNode toNode) {
      GSSNode fromN = getNode(fromNode.uState, fromNode.dfaState);
      GSSNode toN = getNode(toNode.uState, toNode.dfaState);
      if (fromN == null || toN == null)
        return null;
      // if exist - return existing
      for (GSSEdge edge : fromN.edges)
        if (edge.successor.equals(toN))
          return edge;

      // if not exist - return new
      GSSEdge edge = new GSSEdge(fromN, toN);
      fromN.edges.add(edge);
      return edge;
    }

    /**
     * Сформировать список узлов доступных из @fromNode, но доступных точно по дистанции distance.
     * 
     * @param fromNode
     * @param distance
     * @return
     */
    Collection<GSSNode> getReachableNodes(GSSNode fromNode, int distance) {
      class ReachedNode {
        int dist;
        GSSNode node;

        ReachedNode(GSSNode node, int dist) {
          super();
          this.node = node;
          this.dist = dist;
        }

        Collection<ReachedNode> createReachedNodesFromEdge(
            Collection<GSSEdge> edges, int distance) {
          List<ReachedNode> result = new ArrayList<ReachedNode>();
          for (GSSEdge edge : edges)
            result.add(new ReachedNode(edge.successor, distance + 1));
          return result;
        }

        @Override
        public String toString() {
          return "ReachedNode [node=" + node + ", dist=" + dist + "]";
        }
      }

      List<GSSNode> result = new ArrayList<GSSNode>();
      if (distance == 0) {
        result.add(fromNode);
        return result;
      }

      ReachedNode trn = new ReachedNode(null, 0);
      Queue<ReachedNode> tempQueue = new ArrayDeque<ReachedNode>();
      GSSNode fromN = getNode(fromNode.uState, fromNode.dfaState);
      tempQueue.addAll(trn.createReachedNodesFromEdge(fromN.edges, 0));

      while (!tempQueue.isEmpty()) {
        ReachedNode rn = tempQueue.poll();
        if (rn.dist == distance)
          result.add(rn.node);
        if (rn.dist < distance)
          tempQueue.addAll(rn
              .createReachedNodesFromEdge(rn.node.edges, rn.dist));
      }
      return result;
    }

    Collection<GSSNode> getSuccessors(GSSNode v) {
      GSSNode node = uStates.get(v.uState).get(v.dfaState);
      if (node == null)
        return Collections.emptyList();
      ArrayList<GSSNode> result = new ArrayList<GSSNode>();
      for (GSSEdge edge : node.edges)
        result.add(edge.successor);
      return result;
    }

    Collection<GSSNode> getUState(int uState) {
      return uStates.get(uState).values();
    }

    @Override
    public String toString() {
      return uStates.toString();
    }
  }

  protected class GSSEdge {
    GSSNode predcessor = null;
    GSSNode successor = null;

    GSSEdge(GSSNode from, GSSNode to) {
      super();
      this.predcessor = from;
      this.successor = to;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      GSSEdge other = (GSSEdge) obj;
      if (predcessor == null) {
        if (other.predcessor != null)
          return false;
      } else if (!predcessor.equals(other.predcessor))
        return false;
      if (successor == null) {
        if (other.successor != null)
          return false;
      } else if (!successor.equals(other.successor))
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((predcessor == null) ? 0 : predcessor.hashCode());
      result = prime * result
          + ((successor == null) ? 0 : successor.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "Edg[" + successor.uState + "." + successor.dfaState + "<-"
          + predcessor.uState + "." + predcessor.dfaState + "]";
    }

  }

  protected class GSSNode {
    protected int dfaState = 0;
    protected Set<GSSEdge> edges = new HashSet<GSSEdge>();
    protected int uState = 0;

    GSSNode(int uState, int dfaState) {
      super();
      this.uState = uState;
      this.dfaState = dfaState;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      GSSNode other = (GSSNode) obj;
      if (dfaState != other.dfaState)
        return false;
      if (uState != other.uState)
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + dfaState;
      result = prime * result + uState;
      return result;
    }

    @Override
    public String toString() {
      return "N[" + uState + "." + dfaState + "]";
    }

  }

  /**
   * Reduce data/instruction.
   * 
   * @author fedor
   *
   */
  protected class ReduceInst {
    GSSNode fromNode = null;
    int prjIndex = 0;

    ReduceInst(GSSNode fromNode, int prjIndex) {
      super();
      this.fromNode = fromNode;
      this.prjIndex = prjIndex;
    }

    @Override
    public String toString() {
      return "[" + fromNode.uState + "." + fromNode.dfaState + " r" + prjIndex
          + "]";
    }

  }

  protected class RNReduceInst extends ReduceInst {
    int reductionSymbolCount;
    Symbol symbol;

    public RNReduceInst(GSSNode fromNode, Symbol symbol,
        int reductionSymbolCount) {
      super(fromNode, 0);
      this.symbol = symbol;
      this.reductionSymbolCount = reductionSymbolCount;
    }

    @Override
    public String toString() {
      return "[" + fromNode.uState + "." + fromNode.dfaState + " r(" + symbol
          + ", " + reductionSymbolCount + "]";
    }
  }

  protected class ShiftInst {
    GSSNode fromNode = null;
    int toDFAState = 0;

    ShiftInst(GSSNode fromNode, int toDFAState) {
      super();
      this.fromNode = fromNode;
      this.toDFAState = toDFAState;
    }

    @Override
    public String toString() {
      return "[" + fromNode.uState + "." + fromNode.dfaState + " s"
          + toDFAState + "]";
    }

  }

  protected List<Map<Symbol, List<ActionRecord>>> actionTable;
  private Queue<GSSNode> ASet;
  protected Grammar grammar;
  private GSS gss;
  protected Queue<ShiftInst> QSet;
  private Queue<ReduceInst> RSet;
  protected List<List<LR1Point>> states;
  protected IGLRTokenMatcher tokenMatcher;

  public RNGLRAnalyserAlg1e(Grammar grammar,
      List<Map<Symbol, List<ActionRecord>>> actionTable,
      List<List<LR1Point>> states, IGLRTokenMatcher tokenMatcher) {
    this.tokenMatcher = tokenMatcher;
    this.grammar = grammar;
    this.states = states;
    this.actionTable = actionTable;

    this.ASet = new ArrayDeque<GSSNode>();
    this.RSet = new ArrayDeque<ReduceInst>();
    this.QSet = new ArrayDeque<ShiftInst>();
  }

  private void actor(int i, List<Token> tokens) {
    // remove v from A, and let h be the label of v
    while (!ASet.isEmpty()) {
      GSSNode v = ASet.poll();
      Collection<ActionRecord> actionRecords = tokenMatcher
          .getActionTableRecords(actionTable.get(v.dfaState), null, tokens.get(i));

      if (null == actionRecords)
        continue;

      for (ActionRecord actionRecord : actionRecords) {
        // if pk ∈ T(h,ai) add (v,k) to Q
        if (actionRecord.shift)
          QSet.add(new ShiftInst(v, actionRecord.shiftState));

        // for each entry rj ∈ T(h,ai)
        if (actionRecord.reduce) {
          Projection prj = grammar.get(actionRecord.reduceProjection);
          // if the length of j is 0 add (v, j) to R
          if (prj.body.size() == 0)
            RSet.add(new ReduceInst(v, actionRecord.reduceProjection));
          // else add (u, j) to R, for each successor node u of v
          else {
            Collection<GSSNode> us = gss.getSuccessors(v);
            for (GSSNode u : us)
              RSet.add(new ReduceInst(u, actionRecord.reduceProjection));
          }
        } // if (actionRecord.reduce) {

        // for each entry rj ∈ T(h,ai)
        if (actionRecord.rnReduce) {
          // if the length of j is 0 add (v, j) to R
          if (actionRecord.rnReductionSymbolCount == 0)
            RSet.add(new RNReduceInst(v, actionRecord.rnReductionSymbol,
                actionRecord.rnReductionSymbolCount));
          // else add (u, j) to R, for each successor node u of v
          else {
            Collection<GSSNode> us = gss.getSuccessors(v);
            for (GSSNode u : us)
              RSet.add(new RNReduceInst(u, actionRecord.rnReductionSymbol,
                  actionRecord.rnReductionSymbolCount));
          }
        } // if (actionRecord.rnReduce) {

      } // for (ActionRecord actionRecord : actionRecords) {
    }
  }

  List<Integer> getDFAAcceptingStates() {
    List<Integer> result = new ArrayList<Integer>();
    List<Integer> rootSymbolPrjs = grammar.getNonTerminalProjectionsMap().get(
        grammar.getRootSymbol());
    for (int i = 0; i < states.size(); i++) {
      List<LR1Point> lr1List = states.get(i);
      for (LR1Point point : lr1List) {
        if (!rootSymbolPrjs.contains(point.projPos))
          continue;

        // is point of type "S->ABc*"?
        if (point.pos == grammar.get(point.projPos).body.size())
          result.add(i);
      }
    }
    return result;
  }

  private void parseSymbol(int i, List<Token> tokens) {
    // A=Ui , Ui+1=∅
    ASet.clear();
    ASet.addAll(gss.getUState(i));
    gss.clearUState(i + 1);

    // while A !=∅ or R != ∅ {if A != ∅ do ACTOR(i) elsedo REDUCER(i)}
    while (!ASet.isEmpty() || !RSet.isEmpty()) {
      if (!ASet.isEmpty())
        actor(i, tokens);
      else
        reduce(i, tokens);
    }

    // do SHIFTER(i) }
    shift(i, tokens);
  }

  private void processReduce(int uState, List<Token> tokens, ReduceInst ri,
      GSSNode u) {
    // let m be the length of the righthand side of rule j and let X be the
    // symbol on the lefthand side of rule j
    Projection prj = grammar.get(ri.prjIndex);
    int m = prj.body.size();
    Symbol X = prj.head;

    // if m=0 {
    if (m == 0) {
      // let k be the label of u and let pl ∈ T(k, X)
      List<ActionRecord> actionRecords = actionTable.get(u.dfaState).get(X);
      for (ActionRecord actionRecord : actionRecords) {
        if (actionRecord.shift) {
          int l = actionRecord.shiftState;
          // if there is no node in Ui labelled l then create a new node,v, in
          // the GSS, labelled l and add v to Ui and to A
          GSSNode v = gss.getNode(uState, l);
          if (v == null) {
            v = gss.addNode(uState, l);
            ASet.add(v);
          }
          // if there does not exist an edge (v,u) in the GSS {
          GSSEdge edge = gss.getEdge(v, u);
          if (null == edge) {
            // create an edge (v,u) in the GSS
            gss.getOrCreateEdge(v, u);
            // if v not in A forall rk ∈T (l,ai), with (length k) != 0, add
            // (u,k) to R
            if (!ASet.contains(v)) {
              Collection<ActionRecord> actionRecords2 = tokenMatcher
                  .getActionTableRecords(actionTable.get(l), null, tokens.get(uState));
              for (ActionRecord actionRecord2 : actionRecords2) {
                if (actionRecord2.reduce) {
                  Projection prj2 = grammar.get(actionRecord2.reduceProjection);
                  if (prj2.body.size() != 0)
                    RSet.add(new ReduceInst(u, actionRecord2.reduceProjection));
                } // if (actionRecord2.reduce) {
              } // for (ActionRecord actionRecord2 : actionRecords2) {
            } // if (!ASet.contains(v)) {
          } // if (null == edge) {
        } // if (actionRecord.shift) {
      } // for (ActionRecord actionRecord : actionRecords) {
    } else {
      // for each node w which can be reached from u along a path of length
      // m−1 do{
      Collection<GSSNode> nodes = gss.getReachableNodes(u, m - 1);
      for (GSSNode w : nodes) {
        // let k be the label of w and let pl ∈T (k, X)
        List<ActionRecord> actionRecords = actionTable.get(w.dfaState).get(X);
        if (null == actionRecords)
          continue;
        for (ActionRecord actionRecord : actionRecords) {
          if (actionRecord.shift) {
            int l = actionRecord.shiftState;
            // if there is no node in Ui labelled l then create a new node,v,
            // in the GSS labelled l and add v to Ui and to A
            GSSNode v = gss.getNode(uState, l);
            if (v == null) {
              v = gss.addNode(uState, l);
              ASet.add(v);
            }

            // if there does not exist an edge (v,w) in the GSS {
            GSSEdge edge = gss.getEdge(v, w);
            if (null == edge) {
              // create an edge (v,w) in the GSS
              gss.getOrCreateEdge(v, w);
              // if v not in A forall rk ∈T (l,ai), with (length k) != 0,
              // add (w,k) to R
              if (!ASet.contains(v)) {
                Collection<ActionRecord> actionRecords2 = tokenMatcher
                    .getActionTableRecords(actionTable.get(l),null,
                        tokens.get(uState));
                for (ActionRecord actionRecord2 : actionRecords2) {
                  if (actionRecord2.reduce) {
                    Projection prj2 = grammar
                        .get(actionRecord2.reduceProjection);
                    if (prj2.body.size() != 0)
                      RSet.add(new ReduceInst(w, actionRecord2.reduceProjection));
                  } // if (actionRecord2.reduce) {
                } // for (ActionRecord actionRecord2 : actionRecords2) {
              } // if (!ASet.contains(v)) {
            } // if (null == edge) {
          }
        } // for (ActionRecord actionRecord : actionRecords) {
      } // for (Node node : nodes) {
    } // if (m == 0) {
  }

  private void processRNReduce(int uState, List<Token> tokens, RNReduceInst ri,
      GSSNode u) {
    // let m be the length of the righthand side of rule j and let X be the
    // symbol on the lefthand side of rule j
    int m = ri.reductionSymbolCount;
    Symbol X = ri.symbol;

    // if m=0 {
    if (m == 0) {
      // let k be the label of u and let pl ∈ T(k, X)
      List<ActionRecord> actionRecords = actionTable.get(u.dfaState).get(X);
      for (ActionRecord actionRecord : actionRecords) {
        if (actionRecord.shift) {
          int l = actionRecord.shiftState;
          // if there is no node in Ui labelled l then create a new node,v, in
          // the GSS, labelled l and add v to Ui and to A
          GSSNode v = gss.getNode(uState, l);
          if (v == null) {
            v = gss.addNode(uState, l);
            ASet.add(v);
          }
          // if there does not exist an edge (v,u) in the GSS {
          GSSEdge edge = gss.getEdge(v, u);
          if (null == edge) {
            // create an edge (v,u) in the GSS
            gss.getOrCreateEdge(v, u);
            // if v not in A forall rk ∈T (l,ai), with (length k) != 0, add
            // (u,k) to R
            if (!ASet.contains(v)) {
              Collection<ActionRecord> actionRecords2 = tokenMatcher
                  .getActionTableRecords(actionTable.get(l), null, tokens.get(uState));
              for (ActionRecord actionRecord2 : actionRecords2) {
                if (actionRecord2.rnReduce) {
                  if (actionRecord2.rnReductionSymbolCount != 0)
                    RSet.add(new RNReduceInst(u,
                        actionRecord2.rnReductionSymbol,
                        actionRecord2.rnReductionSymbolCount));
                } // if (actionRecord2.reduce) {
              } // for (ActionRecord actionRecord2 : actionRecords2) {
            } // if (!ASet.contains(v)) {
          } // if (null == edge) {
        } // if (actionRecord.shift) {
      } // for (ActionRecord actionRecord : actionRecords) {
    } else {
      // for each node w which can be reached from u along a path of length
      // m−1 do{
      Collection<GSSNode> nodes = gss.getReachableNodes(u, m - 1);
      for (GSSNode w : nodes) {
        // let k be the label of w and let pl ∈T (k, X)
        List<ActionRecord> actionRecords = actionTable.get(w.dfaState).get(X);
        if (null == actionRecords)
          continue;
        for (ActionRecord actionRecord : actionRecords) {
          if (actionRecord.shift) {
            int l = actionRecord.shiftState;
            // if there is no node in Ui labelled l then create a new node,v,
            // in the GSS labelled l and add v to Ui and to A
            GSSNode v = gss.getNode(uState, l);
            if (v == null) {
              v = gss.addNode(uState, l);
              ASet.add(v);
            }

            // if there does not exist an edge (v,w) in the GSS {
            GSSEdge edge = gss.getEdge(v, w);
            if (null == edge) {
              // create an edge (v,w) in the GSS
              gss.getOrCreateEdge(v, w);
              // if v not in A forall rk ∈T (l,ai), with (length k) != 0,
              // add (w,k) to R
              if (!ASet.contains(v)) {
                Collection<ActionRecord> actionRecords2 = tokenMatcher
                    .getActionTableRecords(actionTable.get(l), null,
                        tokens.get(uState));
                for (ActionRecord actionRecord2 : actionRecords2) {
                  if (actionRecord2.rnReduce) {
                    if (actionRecord2.rnReductionSymbolCount != 0)
                      RSet.add(new RNReduceInst(w,
                          actionRecord2.rnReductionSymbol,
                          actionRecord2.rnReductionSymbolCount));
                  } // if (actionRecord2.reduce) {
                } // for (ActionRecord actionRecord2 : actionRecords2) {
              } // if (!ASet.contains(v)) {
            } // if (null == edge) {
          }
        } // for (ActionRecord actionRecord : actionRecords) {
      } // for (Node node : nodes) {
    } // if (m == 0) {
  }

  /**
   * Input a CFG whose production rules are uniquely numbered, a DFA constructed from this grammar in the form of a table T, and an input string a0...ad.
   * 
   * @param tokens
   * @return
   */
  public boolean processTokens(List<Token> tokensOriginal) {
    ArrayList<Token> tokens = new ArrayList<>(tokensOriginal);
    gss = new GSS();

    // setU0={v0},A=∅,R=∅,Q=∅,ad+1=$
    gss.clearUStates();
    // create a node v0 labelled with the start state 0 of the DFA
    gss.addNode(0, 0);
    ASet.clear();
    RSet.clear();
    QSet.clear();
    tokens.add(new Token(0, 0, null, TokenTypes.EOI));
    // for i=0 to d do PARSESYMBOL(i)
    for (int i = 0; i < tokens.size(); i++)
      parseSymbol(i, tokens);

    // let s be the final accepting state of the DFA
    Set<Integer> accStates = new HashSet<Integer>(getDFAAcceptingStates());

    // if Ud contains a node whose label is s report success else report failure
    Collection<GSSNode> ud = gss.getUState(tokens.size() - 1);
    for (GSSNode node : ud)
      if (accStates.contains(node.dfaState))
        return true;

    return false;
  }

  private void reduce(int i, List<Token> tokens) {
    // remove (u, j) from R
    while (!RSet.isEmpty()) {
      ReduceInst ri = RSet.poll();
      GSSNode u = ri.fromNode;
      if (RNReduceInst.class.isInstance(ri))
        processRNReduce(i, tokens, (RNReduceInst) ri, u);
      else
        processReduce(i, tokens, ri, u);
    } // while (iter.hasNext()) {
  }

  private void shift(int i, List<Token> tokens) {
    // while Q !=∅ {remove an element (v,k) from Q
    while (!QSet.isEmpty()) {
      ShiftInst si = QSet.poll();
      GSSNode v = si.fromNode;
      int k = si.toDFAState;

      // if there is no node,w, labelled k in Ui+1 create one
      GSSNode w = gss.getNode(i + 1, k);
      if (w == null)
        w = gss.addNode(i + 1, k);

      // if there is no edge (w,v) in the GSS create one
      GSSEdge edge = gss.getEdge(w, v);
      if (null == edge)
        gss.getOrCreateEdge(w, v);
    }
  }

}
