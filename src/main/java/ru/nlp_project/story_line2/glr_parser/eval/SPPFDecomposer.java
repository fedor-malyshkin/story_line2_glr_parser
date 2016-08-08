package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNodeContainer;

/**
 * Декомпозер SPPF (Shared Packed Parsed Forest).
 * 
 * Код проходит по всем узлам, начиная с корневого и по дочерним слева-направо. 
 * На packed nodes требуется создать копию дерева и дальшей идти по текущему варианту и вновь созданным деревьям.
 * 
 * Код поддерживает в себе необходимую информацию о текущих состояних прохождения других вариантов 
 * деревьев виде экземпляров {@link SPPFWalkPath}.
 * Каждый заинтересованный потребитель прохождения SPPF должен реализоваывать интерфейс {@link ISPPFTreeWalker}.
 * 
 * @author fedor
 *
 */
public class SPPFDecomposer {
  public interface ISPPFTreeWalker {
    /**
     * start of process - visit root node.
     * @param node
     */
    void visitRootNode(SPPFNode node);

    /**
     * append children nodes (preserve order).
     * 
     * @param children
     */
    void addChildren(List<SPPFNode> children, int projectionPos);

    /**
     * start processing child node - go down to it.
     * 
     * Additionaly set grammar projection position which shows which children they 
     * are (used to replace symbols with their actual "interp" and "ext" datas).
     * 
     * @param childrenPos
     */
    void processChildren(int childrenPos, SPPFNode node);

    /**
     * finish node processing - go to parent.
     */
    void finishNode();

    /**
     * Create copy n copy of trees (дальшей идти по текущему варианту и вновь созданным деревьям.).
     */
    List<? extends ISPPFTreeWalker> fork(int copyCount);
  }

  private class SPPFWalkPath {
    Deque<SPPFWalkNodeInfo> nodesInfo = new ArrayDeque<SPPFWalkNodeInfo>();
    ISPPFTreeWalker walker;
    SPPFWalkNodeInfo currNodeInfo;

    public SPPFWalkPath(ISPPFTreeWalker walker, SPPFNode rootNode) {
      super();
      this.walker = walker;
      this.currNodeInfo = new SPPFWalkNodeInfo(rootNode);
    }

    private SPPFWalkPath() {
    }

    public void forkAndInitByNewChildren(List<SPPFNodeContainer> containers) {
      if (containers.size() - 1 > 0) {
        List<? extends ISPPFTreeWalker> fork = this.walker.fork(containers
            .size() - 1);
        for (int i = 1; i < containers.size(); i++) {
          SPPFWalkPath ctx = this.clone();
          ctx.walker = fork.get(i - 1);
          ctx.currNodeInfo.initByNewContainer(ctx, i, containers.get(i));
          contexts.add(ctx);
        }
      }
    }

    public SPPFWalkPath clone() {
      SPPFWalkPath result = new SPPFWalkPath();
      for (SPPFWalkNodeInfo nodeInfo : this.nodesInfo)
        result.nodesInfo.offerLast(nodeInfo.clone());
      result.walker = this.walker;
      result.currNodeInfo = this.currNodeInfo.clone();
      return result;
    }
  }

  private class SPPFWalkNodeInfo {
    SPPFNode currNode;
    int childrenContainerPos = -1;
    int childrenCount = -1;
    int currentProcessedChildrenPos = -1;

    public SPPFWalkNodeInfo(SPPFNode currNode) {
      super();
      this.currNode = currNode;
    }

    public SPPFWalkNodeInfo clone() {
      SPPFWalkNodeInfo result = new SPPFWalkNodeInfo(currNode);
      result.childrenContainerPos = this.childrenContainerPos;
      result.childrenCount = this.childrenCount;
      result.currentProcessedChildrenPos = this.currentProcessedChildrenPos;
      return result;
    }

    public void initByNewContainer(SPPFWalkPath currCtx, int containerPos,
        SPPFNodeContainer container) {
      this.childrenContainerPos = containerPos;
      this.childrenCount = container.children.size();
      currCtx.walker.addChildren(container.children, container.projectionPos);
      this.currentProcessedChildrenPos = -1;
    }

    @Override
    public String toString() {
      return "SPPFWalkNodeInfo [currNode=" + currNode
          + ", childrenContainerPos=" + childrenContainerPos
          + ", childrenCount=" + childrenCount
          + ", currentProcessedChildrenPos=" + currentProcessedChildrenPos
          + "]";
    }

    public void initiByNewChildren(SPPFWalkPath currCtx, SPPFNode childrenNode) {
      currNode = childrenNode;
      if (childrenNode.isPackedNode())
        currCtx.forkAndInitByNewChildren(childrenNode.containers);

      if (childrenNode.containers.size() > 0)
        initByNewContainer(currCtx, 0, childrenNode.containers.get(0));
      else
        makrAsAllChildrenProcessed();
    }

    private void makrAsAllChildrenProcessed() {
      currentProcessedChildrenPos = 0;
      childrenCount = 0;
    }

    public boolean hasChildren() {
      return childrenCount > 0;
    }

    public boolean isAllCildrenProcessed() {
      return (childrenCount != -1 && currentProcessedChildrenPos >= childrenCount);
    }

  }

  private ArrayDeque<SPPFWalkPath> contexts;

  public void walkSPPF(SPPFNode rootNode, ISPPFTreeWalker walker) {
    walker.visitRootNode(rootNode);
    contexts = new ArrayDeque<SPPFWalkPath>();
    SPPFWalkPath currCtx = new SPPFWalkPath(walker, rootNode);
    contexts.offerLast(currCtx);
    currCtx.currNodeInfo.initiByNewChildren(currCtx, rootNode);
    while (!contexts.isEmpty()) {
      currCtx = contexts.pollLast();
      while (true) {
        // если добрались до родительского узла корня
        // - значит обработали корень уже
        if (currCtx.currNodeInfo.currNode == null)
          break;

        if (!currCtx.currNodeInfo.hasChildren()
            || currCtx.currNodeInfo.isAllCildrenProcessed()) {
          // go up - no children
          boolean next = goUp(currCtx);
          if (!next)
            break;
        }

        // обработать текущий необработанный родительский узел
        processChildren(currCtx);

      }
    }
  }

  private void processChildren(SPPFWalkPath currCtx) {
    SPPFWalkNodeInfo currNodeInfo = currCtx.currNodeInfo;
    currNodeInfo.currentProcessedChildrenPos++;
    if (currNodeInfo.currentProcessedChildrenPos >= currNodeInfo.childrenCount)
      return;

    SPPFNodeContainer container = currNodeInfo.currNode.containers
        .get(currNodeInfo.childrenContainerPos);

    SPPFNode childrenNode = container.children
        .get(currNodeInfo.currentProcessedChildrenPos);
    
    currCtx.nodesInfo.offerLast(currNodeInfo.clone());
    currCtx.walker.processChildren(currNodeInfo.currentProcessedChildrenPos,
        childrenNode);

    // set new children datas
    currNodeInfo.initiByNewChildren(currCtx, childrenNode);
  }

  private boolean goUp(SPPFWalkPath currCtx) {
    currCtx.walker.finishNode();
    currCtx.currNodeInfo = currCtx.nodesInfo.pollLast();
    return currCtx.currNodeInfo != null;
  }
}
