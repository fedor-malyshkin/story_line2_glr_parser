package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode;
import ru.nlp_project.story_line2.glr_parser.eval.SPPFDecomposer.ISPPFTreeWalker;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class SPPFDecomposerTest {

  class SPPFTreeWalkerPrint implements ISPPFTreeWalker {
    List<String> str = new ArrayList<String>();

    @Override
    public void visitRootNode(SPPFNode node) {
      str.add(String.format("%d-visitRootNode(%s)", counter, node.toString()));

    }

    @Override
    public void addChildren(List<SPPFNode> children, int projectionPos) {
      str.add(String.format("%d-addChildren(%s,%d)", counter,
          children.toString(), projectionPos));
    }

    @Override
    public void processChildren(int childrenPos, SPPFNode node) {
      str.add(String.format("%d-processChildren(%d, %s)", counter, childrenPos,
          node.toString()));
    }

    @Override
    public void finishNode() {
      str.add(String.format("%d-finishNode()", counter));
    }

    @Override
    public List<ISPPFTreeWalker> fork(int copyCount) {
      str.add(String.format("%d-fork(%d)", counter, copyCount));
      List<ISPPFTreeWalker> result = new LinkedList<ISPPFTreeWalker>();
      for (int i = 0; i < copyCount; i++)
        result.add(new SPPFTreeWalkerPrint(++counterField, str));
      return result;
    }

    int counter = 0;

    public SPPFTreeWalkerPrint(int counter) {
      super();
      this.counter = counter;
    }

    public SPPFTreeWalkerPrint(int counter, List<String> str) {
      super();
      this.counter = counter;
      this.str = str;
    }

  }

  int counterField = 0;

  private SPPFDecomposer testable;

  @Before
  public void setUp() {
    testable = new SPPFDecomposer();
  }

  @Test
  public void testWalkSPPF_SingleNode() {
    counterField = 0;
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFTreeWalkerPrint walkerPrint = new SPPFTreeWalkerPrint(counterField);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("X", SymbolTypes.NonTerminal), 0, 1);
    testable.walkSPPF(root, walkerPrint);
    assertEquals("[0-visitRootNode(<'X', 0, 1>), 0-finishNode()]",
        walkerPrint.str.toString());
  }

  @Test
  public void testWalkSPPF_NoForks() {
    counterField = 0;
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFTreeWalkerPrint walkerPrint = new SPPFTreeWalkerPrint(counterField);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("X", SymbolTypes.NonTerminal), 0, 1);

    List<SPPFNode> singletonList = Collections.singletonList(rnglrAnalyser
        .createSPPFNode(new Symbol("c", SymbolTypes.NonTerminal), 0, 1));
    root.addChildren(singletonList, -1);

    testable.walkSPPF(root, walkerPrint);
    assertEquals("[0-visitRootNode(<'X', 0, 1>->[[<'c', 0, 1>](-1)]), "
        + "0-addChildren([<'c', 0, 1>],-1), "
        + "0-processChildren(0, <'c', 0, 1>), 0-finishNode(), "
        + "0-finishNode()]", walkerPrint.str.toString());
  }

  @Test
  public void testWalkSPPF_MoreLevels() {
    counterField = 0;
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFTreeWalkerPrint walkerPrint = new SPPFTreeWalkerPrint(counterField);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("R", SymbolTypes.NonTerminal), 0, 1);

    SPPFNode c11 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c12 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-2", SymbolTypes.NonTerminal), 0, 1);
    List<SPPFNode> singletonList = Arrays.asList(c11, c12);
    root.addChildren(singletonList, -1);
    SPPFNode c21 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c22 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-2", SymbolTypes.NonTerminal), 0, 1);
    singletonList = Arrays.asList(c21, c22);
    c12.addChildren(singletonList, -1);

    testable.walkSPPF(root, walkerPrint);
    assertEquals(
        "[0-visitRootNode(<'R', 0, 1>->[[<'c1-1', 0, 1>, <'c1-2', 0, 1>->[[<'c2-1', 0, 1>, <'c2-2', 0, 1>](-1)]](-1)]), "
            + "0-addChildren([<'c1-1', 0, 1>, <'c1-2', 0, 1>->[[<'c2-1', 0, 1>, <'c2-2', 0, 1>](-1)]],-1), "
            + "0-processChildren(0, <'c1-1', 0, 1>), 0-finishNode(), "
            + "0-processChildren(1, <'c1-2', 0, 1>->[[<'c2-1', 0, 1>, <'c2-2', 0, 1>](-1)]), "
            + "0-addChildren([<'c2-1', 0, 1>, <'c2-2', 0, 1>],-1), "
            + "0-processChildren(0, <'c2-1', 0, 1>), 0-finishNode(), "
            + "0-processChildren(1, <'c2-2', 0, 1>), 0-finishNode(), "
            + "0-finishNode(), 0-finishNode()]",
        walkerPrint.str.toString());
  }

  @Test
  public void testWalkSPPF_2LevelWithFork() {
    counterField = 0;
    RNGLRAnalyser rnglrAnalyser =
        new RNGLRAnalyser(null, null, null, null, null);
    SPPFTreeWalkerPrint walkerPrint = new SPPFTreeWalkerPrint(counterField);
    SPPFNode root = rnglrAnalyser
        .createSPPFNode(new Symbol("R", SymbolTypes.NonTerminal), 0, 1);

    SPPFNode c11 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c12 = rnglrAnalyser
        .createSPPFNode(new Symbol("c1-2", SymbolTypes.NonTerminal), 0, 1);
    List<SPPFNode> singletonList = Arrays.asList(c11, c12);
    root.addChildren(singletonList, -1);

    SPPFNode c21 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode c22 = rnglrAnalyser
        .createSPPFNode(new Symbol("c2-2", SymbolTypes.NonTerminal), 0, 1);
    singletonList = Arrays.asList(c21, c22);
    c12.addChildren(singletonList, -1);

    SPPFNode cn21 = rnglrAnalyser
        .createSPPFNode(new Symbol("cn2-1", SymbolTypes.NonTerminal), 0, 1);
    SPPFNode cn22 = rnglrAnalyser
        .createSPPFNode(new Symbol("cn2-2", SymbolTypes.NonTerminal), 0, 1);
    singletonList = Arrays.asList(cn21, cn22);
    c12.addChildren(singletonList, -1);

    testable.walkSPPF(root, walkerPrint);
    assertEquals(
        "[0-visitRootNode(<'R', 0, 1>->[[<'c1-1', 0, 1>, <'c1-2', 0, 1>->[[<'c2-1', 0, 1>, <'c2-2', 0, 1>](-1), [<'cn2-1', 0, 1>, <'cn2-2', 0, 1>](-1)]](-1)]), "
            + "0-addChildren([<'c1-1', 0, 1>, <'c1-2', 0, 1>->[[<'c2-1', 0, 1>, <'c2-2', 0, 1>](-1), [<'cn2-1', 0, 1>, <'cn2-2', 0, 1>](-1)]],-1), "
            + "0-processChildren(0, <'c1-1', 0, 1>), 0-finishNode(), "
            + "0-processChildren(1, <'c1-2', 0, 1>->[[<'c2-1', 0, 1>, <'c2-2', 0, 1>](-1), [<'cn2-1', 0, 1>, <'cn2-2', 0, 1>](-1)]), "
            + "0-fork(1), 1-addChildren([<'cn2-1', 0, 1>, <'cn2-2', 0, 1>],-1), "
            + "0-addChildren([<'c2-1', 0, 1>, <'c2-2', 0, 1>],-1), "
            + "0-processChildren(0, <'c2-1', 0, 1>), 0-finishNode(), "
            + "0-processChildren(1, <'c2-2', 0, 1>), 0-finishNode(), "
            + "0-finishNode(), 0-finishNode(), "
            + "1-processChildren(0, <'cn2-1', 0, 1>), 1-finishNode(), "
            + "1-processChildren(1, <'cn2-2', 0, 1>), 1-finishNode(), "
            + "1-finishNode(), 1-finishNode()]",
        walkerPrint.str.toString());
  }

}
