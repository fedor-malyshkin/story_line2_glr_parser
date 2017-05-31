package ru.nlp_project.story_line2.glr_parser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.ICrossLevelOrderWalkProcessor;
import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.IInOrderWalkProcessor;
import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.ILevelOrderWalkProcessor;
import ru.nlp_project.story_line2.glr_parser.ParseTreeNode.IPostOrderWalkLeafFirstProcessor;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode;

public class ParseTreeNodeTest {
  class ParseTreeNodeWithName extends ParseTreeNode {
    public ParseTreeNodeWithName(SPPFNode node, ParseTreeNode parent) {
      super(node, parent);
    }

    public ParseTreeNodeWithName(String name) {
      super(0, 0, null, null);
      this.isTerminal = true;
      this.name = name;
    }

    String name = "";

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("<%s>", name));
      return sb.toString();
    }
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testWalkLevelOrder() throws Exception {
    ParseTreeNode root = createTestTree();

    StringBuilder sb = new StringBuilder();
    root.walkLevelOrder(new ILevelOrderWalkProcessor() {

      @Override
      public void processNode(ParseTreeNode node, int level)  {
        ParseTreeNodeWithName n = (ParseTreeNodeWithName) node;
        sb.append(n.name + " (" + level + "); ");
      }

      @Override
      public void nextLevel(int level) {
        sb.append("nextLevel (" + level + "); ");
      }
    });

    assertEquals(
        "root (0); nextLevel (0); l11 (1); l12 (1); nextLevel (1); "
            + "l11_1 (2); l11_2 (2); l11_3 (2); nextLevel (2); "
            + "l12_1 (2); l12_2 (2); nextLevel (2); l11_2_1 (3); nextLevel (3); "
            + "l12_2_1 (3); nextLevel (3); l11_2_1_1 (4); nextLevel (4); ",
        sb.toString());

  }

  @Test
  public void testCrossWalkLevelOrder() throws Exception {
    ParseTreeNode root = createTestTree();

    StringBuilder sb = new StringBuilder();
    root.walkCrossLevelOrder(new ICrossLevelOrderWalkProcessor() {

      @Override
      public void processNode(ParseTreeNode node) {
        ParseTreeNodeWithName n = (ParseTreeNodeWithName) node;
        sb.append(n.name + "; ");
      }

      @Override
      public void nextLevel() {
        sb.append("nextLevel; ");
      }
    });

    assertEquals(
        "root; nextLevel; l11; l12; nextLevel; l11_1; l11_2; l11_3; l12_1; l12_2; "
            + "nextLevel; l11_2_1; l12_2_1; nextLevel; l11_2_1_1; nextLevel; ",
        sb.toString());

  }

  @Test
  public void testWalkPostOrder() throws Exception {
    ParseTreeNode root = createTestTree();

    StringBuilder sb = new StringBuilder();
    root.walkPostOrderLeafFirst(new IPostOrderWalkLeafFirstProcessor() {

      @Override
      public void processNode(ParseTreeNode node) throws Exception {
        ParseTreeNodeWithName n = (ParseTreeNodeWithName) node;
        sb.append(n.name + "; ");
      }

    });

    assertEquals(
        "l11_1; l11_2_1_1; l11_2_1; l11_2; l11_3; l11; l12_1; l12_2_1; l12_2; l12; root; ",
        sb.toString());

  }

  @Test
  public void testWalkInOrder() throws Exception {
    ParseTreeNode root = createTestTree();

    StringBuilder sb = new StringBuilder();
    root.walkInOrder(new IInOrderWalkProcessor() {

      @Override
      public void processTerminalNode(ParseTreeNode node) {
        ParseTreeNodeWithName n = (ParseTreeNodeWithName) node;
        sb.append("T-" + n.name + "; ");
      }

      @Override
      public void processNonTerminalNode(ParseTreeNode node) {
        ParseTreeNodeWithName n = (ParseTreeNodeWithName) node;
        sb.append("NT-" + n.name + "; ");
      }

    });

    assertEquals("T-l11_1; T-l11_2_1_1; NT-l11_2_1; NT-l11_2; T-l11_3; NT-l11; "
        + "T-l12_1; T-l12_2_1; NT-l12_2; NT-l12; NT-root; ", sb.toString());

  }

  private ParseTreeNode createTestTree() {
    ParseTreeNodeWithName root = new ParseTreeNodeWithName("root");

    ParseTreeNodeWithName l11 = new ParseTreeNodeWithName("l11");
    ParseTreeNodeWithName l12 = new ParseTreeNodeWithName("l12");
    root.addChild(l11);
    root.addChild(l12);

    ParseTreeNodeWithName l11_1 = new ParseTreeNodeWithName("l11_1");
    ParseTreeNodeWithName l11_2 = new ParseTreeNodeWithName("l11_2");
    ParseTreeNodeWithName l11_3 = new ParseTreeNodeWithName("l11_3");
    l11.addChild(l11_1);
    l11.addChild(l11_2);
    l11.addChild(l11_3);

    ParseTreeNodeWithName l11_2_1 = new ParseTreeNodeWithName("l11_2_1");
    l11_2.addChild(l11_2_1);

    ParseTreeNodeWithName l11_2_1_1 = new ParseTreeNodeWithName("l11_2_1_1");
    l11_2_1.addChild(l11_2_1_1);

    ParseTreeNodeWithName l12_1 = new ParseTreeNodeWithName("l12_1");
    ParseTreeNodeWithName l12_2 = new ParseTreeNodeWithName("l12_2");
    l12.addChild(l12_1);
    l12.addChild(l12_2);

    ParseTreeNodeWithName l12_2_1 = new ParseTreeNodeWithName("l12_2_1");
    l12_2.addChild(l12_2_1);

    return root;
  }

}
