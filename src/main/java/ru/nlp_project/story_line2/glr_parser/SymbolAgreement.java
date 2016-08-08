package ru.nlp_project.story_line2.glr_parser;

import java.util.ArrayList;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;

/**
 * Объект для хранения информации о согласовании символов.
 * 
 * @author fedor
 *
 */
public class SymbolAgreement {
  public SymbolAgreement(SymbolExtDataTypes agrType) {
    super();
    this.agrType = agrType;
    this.nodes = new ArrayList<>();
  }

  /**
   * Тим согласования.
   */
  private SymbolExtDataTypes agrType;
  /**
   * Символы в связке согласования.
   */
  private List<ParseTreeNode> nodes;

  public SymbolExtDataTypes getAgrType() {
    return agrType;
  }

  public List<ParseTreeNode> getNodes() {
    return nodes;
  }

  public void addNode(ParseTreeNode node) {
    nodes.add(node);
  }

  @Override
  public String toString() {
    return agrType + " (" + nodes.size() + ") ";
  }

}
