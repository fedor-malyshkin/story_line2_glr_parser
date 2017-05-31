package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.LinkedList;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.SymbolExt;
import ru.nlp_project.story_line2.morph.GrammemeEnum;

/**
 * Символы грамматики (либо терминалы, либо нетерминалы).
 * 
 * @author fedor
 *
 */
public class Symbol implements Comparable<Symbol> {
  public enum SymbolTypes {
    Terminal(0), LiteralString(1), NonTerminal(2), Epsilon(3), EOI(4);
    private int index;

    SymbolTypes(int i) {
      this.index = i;
    }

    public int getIndex() {
      return index;
    }

  }

  public final static Symbol EOI = Symbol.createEOISymbol();
  public final static Symbol EPSILON = Symbol.createEpsilonSymbol();

  public static Symbol createEOISymbol() {
    Symbol result = new Symbol("EOI", SymbolTypes.EOI);
    result.epsilon = false;
    result.eoi = true;
    return result;
  }

  public static Symbol createEpsilonSymbol() {
    Symbol result = new Symbol("EPSILON", SymbolTypes.Epsilon);
    result.epsilon = true;
    result.eoi = false;
    return result;
  }

  /**
   * "End Of Input" symbol type.
   */
  protected boolean eoi = false;
  /**
   * "Epsilon" symbol type.
   */
  protected boolean epsilon = false;
  protected GrammemeEnum grammeme = null;
  protected SymbolTypes symbolType;
  protected String value = null;

  public Symbol(String value, SymbolTypes type) {
    this.value = value;
    this.symbolType = type;
  };

  @Override
  public int compareTo(Symbol obj) {
    if (obj.getClass() == Symbol.class) {
      return compareToInt(obj);
    } else if (obj.getClass() == SymbolExt.class) {
      SymbolExt other = (SymbolExt) obj;
      return -1 * other.compareTo(this);
    } else
      throw new IllegalStateException("Unknown class: " + obj.getClass());
  }

  public GrammemeEnum getGrammeme() {
    return grammeme;
  }

  public SymbolTypes getSymbolType() {
    return symbolType;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (eoi ? 1231 : 1237);
    result = prime * result + (epsilon ? 1231 : 1237);
    result =
        prime * result + ((symbolType == null) ? 0 : symbolType.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /** 
   * Принцип сравнения простой:
   * <ul>
   * <li>если аргумент экземпляр неизвестного класса - исключение</li>
    <li>если экземпляр класса {@link SymbolExt}- передать управление {@link SymbolExt#equalsInt} </li>
   * </ul>
   */

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;

    if (obj.getClass() == Symbol.class) {
      return equalsInt((Symbol) obj);
    } else if (obj.getClass() == SymbolExt.class) {
      SymbolExt other = (SymbolExt) obj;
      return other.equals(this);
    } else
      throw new IllegalStateException("Unknown class: " + obj.getClass());

  }

  public boolean isEOI() {
    return eoi;
  }

  public boolean isEpsilon() {
    return epsilon;
  }

  public void setGrammeme(GrammemeEnum grammeme) {
    this.grammeme = grammeme;
  }

  public void setSymbolType(SymbolTypes symbolType) {
    this.symbolType = symbolType;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    if (eoi)
      return "EOI";
    if (epsilon)
      return "EPSILON";

    switch (this.symbolType) {
    case LiteralString:
      return "'" + value + "'";
    case Terminal:
      return value;
    case NonTerminal:
      return value;
    default:
      return value + "[UNKN]";
    }

  }

  public boolean isTerm() {
    return symbolType.getIndex() < 2;
  }

  public boolean isNonTerm() {
    return SymbolTypes.NonTerminal == symbolType;
  }

  public Symbol clone() {
    Symbol result = new Symbol(this.value, this.symbolType);
    result.eoi = this.eoi;
    result.epsilon = this.epsilon;
    return result;
  }

  public static List<Symbol> cloneList(List<Symbol> input) {
    List<Symbol> result = new LinkedList<Symbol>();
    for (Symbol el : input)
      result.add(el.clone());
    return result;
  }

  /**
   * 
   * Класс для фактического сравнения объектов на эквивалентность.
   * ВАЖНО: Тут не производится проверка классов.
   * 
   * @param symbolExt
   * @return
   */
  public boolean equalsInt(Symbol other) {
    if (symbolType != other.symbolType)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (eoi != other.eoi)
      return false;
    if (epsilon != other.epsilon)
      return false;

    return true;
  }

  /**
   * 
   * Класс для фактического сравнения объектов на больше/меньше.
   * ВАЖНО: Тут не производится проверка классов.
  */
  public int compareToInt(Symbol other) {
    if (symbolType.getIndex() - other.symbolType.getIndex() != 0)
      return symbolType.getIndex() - other.symbolType.getIndex();

    if (value.compareTo(other.value) != 0)
      return value.compareTo(other.value);

    if (eoi)
      return Boolean.compare(eoi, other.eoi);
    if (epsilon)
      return Boolean.compare(epsilon, other.epsilon);
    if (other.eoi)
      return -1;
    if (other.epsilon)
      return -1;

    return 0;
  }

  /**
   * Создание клона базового объекта Symbol (без реализации данного метода 
   * наследуемыми классами), т.е. всегда получаем экземпляр базового класса 
   * (удобен для сравнения и поиска сиволов без ограничений-помет).
   * 
   * @return
   */
  public Symbol cloneBase() {
    Symbol result = new Symbol(this.value, this.symbolType);
    result.eoi = this.eoi;
    result.epsilon = this.epsilon;
    return result;
  }

}
