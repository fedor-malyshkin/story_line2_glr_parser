package ru.nlp_project.story_line2.glr_parser;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.glr_parser.eval.Symbol;

/**
 * Regular Expression-символ (наследует {@link Symbol}), лишь для схожести.
 * 
 * @author fedor
 *
 */
public class SymbolRE extends SymbolExt {

  public enum RETypes {
    Group, SingleElement;
  }

  private SymbolExt element;
  private List<SymbolExt> elements;
  private String quantifier;

  private RETypes reType;

  public SymbolRE(RETypes type, List<SymbolExt> elements, String quantifier) {
    super(null, SymbolTypes.NonTerminal, null);
    this.reType = type;
    this.elements = elements;
    this.quantifier = quantifier;
  }

  public SymbolRE(RETypes type, SymbolExt element, String quantifier) {
    super(null, SymbolTypes.NonTerminal, null);
    this.reType = type;
    this.element = element;
    this.quantifier = quantifier;
  };

  public Symbol getElement() {
    return element;
  }

  public List<SymbolExt> getElements() {
    return elements;
  }

  public String getQuantifier() {
    return quantifier;
  }

  public RETypes getReSymbolType() {
    return reType;
  }

  public void setQuantifier(String quantifier) {
    this.quantifier = quantifier;
  }

  @Override
  public String toString() {
    switch (reType) {
    case SingleElement: {
      if (quantifier != null)
        return element.toString() + quantifier;
      else
        return element.toString();
    }
    case Group: {
      String res = "(";
      res += StringUtils.join(elements, "|");
      res += ")";
      if (quantifier != null)
        res += quantifier;
      return res;
    }
    default:
      return "";
    }
  }

  @Override
  public String getValue() {
    switch (reType) {
    case SingleElement: {
      return element.getValue();
    }
    case Group: {
      return elements.toString();
    }
    default:
      return "";
    }
  }

  @Override
  public Symbol clone() {
    SymbolRE result = null;
    if (reType == RETypes.Group) {
      result = new SymbolRE(RETypes.Group,
          SymbolExt.cloneListExt(this.elements),
          this.quantifier != null ? new String(this.quantifier) : null);
    } else if (reType == RETypes.SingleElement) {
      result = new SymbolRE(RETypes.SingleElement,
          (SymbolExt) this.element.clone(),
          this.quantifier != null ? new String(this.quantifier) : null);
    }
    return result;
  }

}
