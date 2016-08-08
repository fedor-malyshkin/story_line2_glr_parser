package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

/**
 * Объект-грамматика для обработки.
 * 
 * @author fedor
 *
 */
public class Grammar {

  private Set<Symbol> allSymbols;
  private Factory<List<Integer>> factoryListInt = new Factory<List<Integer>>() {
    public List<Integer> create() {
      return new ArrayList<Integer>();
    }
  };

  private Map<Symbol, Boolean> isNullableNonterminalMap;
  private Map<Symbol, List<Integer>> nonTerminalProjectionsMap;
  private List<Projection> projections = null;
  private Symbol rootSymbol;
  private Symbol userRootSymbol;
  private Set<Symbol> allBaseSymbols;

  public Grammar() {
    projections = new ArrayList<Projection>();
  }

  public void add(Projection projection) {
    projections.add(projection);
  }

  /**
   * Проанализировать все проекции грамматики и собрать все символы.
   */
  void collectAllGrammarSymbols() {
    // ALL SYMBOLS
    allSymbols = new TreeSet<Symbol>();
    for (Projection proj : projections) {
      allSymbols.add(proj.head);
      allSymbols.addAll(proj.body);
    }
    allSymbols.add(Symbol.EOI);
    allSymbols.remove(Symbol.EPSILON);

    // ALL BASE SYMBOLS (without extended data)
    allBaseSymbols = new HashSet<Symbol>();
    for (Projection proj : projections) {
      allBaseSymbols.add(proj.head);
      proj.body.forEach((bs) -> allBaseSymbols.add(bs.cloneBase()));
    }
    allBaseSymbols.add(Symbol.EOI);
    allBaseSymbols.remove(Symbol.EPSILON);
  }

  /**
   * Выполнить расчет всех имеющихся nullable нетерминалов.
   * Работа выполняется только с базовыми символами - без расширенных данных.
   */
  void collectNullableNonterminals() {
    // isNullableNonterminalMap = new TreeMap<Symbol, Boolean>();
    isNullableNonterminalMap = new HashMap<Symbol, Boolean>();
    // предзаполнить
    for (Symbol smb : allBaseSymbols) {
      if (smb.isTerm() || smb.isEOI())
        continue;
      isNullableNonterminalMap.put(smb, false);
    }
    // real work...
    boolean hasChanges = true;
    while (hasChanges) {
      hasChanges = false;
      for (Symbol smb : allBaseSymbols) {
        if (smb.isTerm() || smb.isEOI())
          continue;
        boolean oldSmbNullableVal = isNullableNonterminalMap.get(smb);
        boolean newSmbNullableVal = false;

        List<Integer> prjs = nonTerminalProjectionsMap.get(smb);
        for (int i : prjs) {
          Projection projection = projections.get(i);
          boolean nullableBodyVal = true;
          for (Symbol bodyS : projection.body) {
            // откидываем все расширенные данные
            bodyS = bodyS.cloneBase();
            if (bodyS.isTerm()) {
              // если есть хотя бы один терминал -- эта проекция ни при
              // каких обстоятельствах не nullable
              nullableBodyVal = false;
              break;
            }
            if (bodyS.isEpsilon()) {
              nullableBodyVal &= true;
              continue;
            }
            // для проекции, чтобы быть признанным nullable необходимо иметь все
            // элемены числящиеся nullable или являющиеся EPSLION
            nullableBodyVal &= isNullableNonterminalMap.get(bodyS);
          } // for (Symbol bodyS : projection.body) {
          // для симола, чтобы быть признанным nullable достаточно иметь одну
          // проекцию числящуюся как nullable
          if (nullableBodyVal) {
            newSmbNullableVal = true;
            break;
          }
        }

        isNullableNonterminalMap.put(smb, newSmbNullableVal);
        // OR'ing cahnges (в итоге ходим по кругу пока что-то меняется)
        hasChanges |= (newSmbNullableVal != oldSmbNullableVal);
      }
    }
  }

  /**
   * Собрать ассоциативный массив "NonTerminal - Projections List" (фактически:
   * собрать все номера проекций в которых определены нетерминалы).
   */
  void composeNonTerminalProjectionsMap() {
    nonTerminalProjectionsMap =
        LazyMap.lazyMap(new TreeMap<Symbol, List<Integer>>(), factoryListInt);
    for (int i = 0; i < projections.size(); i++) {
      Projection projection = projections.get(i);
      List<Integer> list = nonTerminalProjectionsMap.get(projection.head);
      list.add(i);
    }
  }

  /**
   * 
   * Определить потенциальный корень грамматики. 
   * 
   * Собрать все левые символы и убрать, встречавшиеся в правой части. 
   */
  private void detectRootSymbol() {
    Set<Symbol> lhsSymbols = new HashSet<Symbol>();
    // collect lhs
    for (Projection prj : getProjections())
      lhsSymbols.add(prj.getHead());

    // remove from lhs-set by rhs inclusions
    for (Projection prj : getProjections())
      for (Symbol s : prj.getBody())
        lhsSymbols.remove(s.cloneBase());

    if (lhsSymbols.size() == 1) {
      rootSymbol = (Symbol) lhsSymbols.toArray()[0];
      rootSymbol.setSymbolType(SymbolTypes.NonTerminal);
    }
    if (lhsSymbols.size() == 0)
      throw new IllegalStateException(
          "Not found possible root symbol for grammar: '" + toString() + "'");
    if (lhsSymbols.size() > 1)
      throw new IllegalStateException(
          "More than one possible root symbol for grammar: '" + toString()
              + "': " + lhsSymbols);

  }

  public Projection get(int projPos) {
    return projections.get(projPos);
  }

  public Set<Symbol> getAllSymbols() {
    return allSymbols;
  }

  public Set<Symbol> getAllBaseSymbols() {
    return allBaseSymbols;
  }

  public Factory<List<Integer>> getFactoryListInt() {
    return factoryListInt;
  }

  public Map<Symbol, Boolean> getIsNullableNonterminalMap() {
    return isNullableNonterminalMap;
  }

  public Map<Symbol, List<Integer>> getNonTerminalProjectionsMap() {
    return nonTerminalProjectionsMap;
  }

  public List<Projection> getProjections() {
    return projections;
  }

  public Symbol getRootSymbol() {
    return rootSymbol;
  }

  public Symbol getUserRootSymbol() {
    return userRootSymbol;
  }

  public void prepareGrammar() {
    prepareGrammar(null);
  }

  public void prepareGrammar(String rootSymbolName) {
    if (rootSymbol == null)
      if (null == rootSymbolName)
        detectRootSymbol();
      else
        rootSymbol = new Symbol(rootSymbolName, SymbolTypes.NonTerminal);

    //
    replaceEpsilonSymbols();
    collectAllGrammarSymbols();
    composeNonTerminalProjectionsMap();
    collectNullableNonterminals();
  }

  /**
   * Заменить все вхождения epsilon-символов на соответствующие объекты.
   */
  void replaceEpsilonSymbols() {
    for (Projection prj : projections) {
      for (int i = 0; i < prj.body.size(); i++) {
        Symbol symbol = prj.body.get(i);
        if (symbol.getValue().equalsIgnoreCase("ε"))
          prj.body.set(i, Symbol.createEpsilonSymbol());
      }
    }
  }

  public int size() {
    return projections.size();
  }

  @Override
  public String toString() {
    return StringUtils.join(projections, "\n");
  }

  /**
   * Enclose grammar old root symbol in new root symbol.
   * 
   * @param newRootSymbol
   */
  public void wrapNewRoot(Symbol newRootSymbol) {
    this.projections
        .add(new Projection(newRootSymbol, Arrays.asList(rootSymbol)));
    this.userRootSymbol = rootSymbol;
    this.rootSymbol = newRootSymbol;
  }

}
