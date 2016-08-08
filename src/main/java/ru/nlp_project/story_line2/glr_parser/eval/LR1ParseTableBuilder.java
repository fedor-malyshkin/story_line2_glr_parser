package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.list.LazyList;
import org.apache.commons.collections4.map.LazyMap;

public class LR1ParseTableBuilder {

  /**
   * Copare firstly by length, after that by value of each position.
   * 
   * @author fedor
   *
   */
  class ListSymbolComparator implements Comparator<List<Symbol>> {

    @Override
    public int compare(List<Symbol> o1, List<Symbol> o2) {
      int res = Integer.compare(o1.size(), o2.size());
      if (res != 0)
        return res;
      int pos = 0;
      while (res == 0 && pos < o1.size()) {
        res = o1.get(pos).compareTo(o2.get(pos));
        pos++;
      }
      return res;
    }
  }

  /**
   * Объет "переход из состояния в состояние".
   * 
   * @author fedor
   *
   */
  protected class StateTransition {
    /**
     * ... из какого состояния?
     */
    int fromStateN = 0;
    /**
     * .. метка перехадо.
     */
    Symbol label;

    /**
     * в какое сотояние?
     */
    int toStateN = 0;

    public StateTransition(int fromStateN, Symbol label, int toStateN) {
      super();
      this.fromStateN = fromStateN;
      this.label = label;
      this.toStateN = toStateN;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      StateTransition other = (StateTransition) obj;

      if (fromStateN != other.fromStateN)
        return false;
      if (label == null) {
        if (other.label != null)
          return false;
      } else if (!label.equals(other.label))
        return false;
      if (toStateN != other.toStateN)
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + fromStateN;
      result = prime * result + ((label == null) ? 0 : label.hashCode());
      result = prime * result + toStateN;
      return result;
    }

    @Override
    public String toString() {
      return fromStateN + "-(" + label + ")->" + toStateN;
    }

  }

  private Factory<Map<Symbol, List<ActionRecord>>> factoryListSLA =
      new Factory<Map<Symbol, List<ActionRecord>>>() {
        @Override
        public Map<Symbol, List<ActionRecord>> create() {
          return new TreeMap<Symbol, List<ActionRecord>>();
        }

      };

  private Factory<List<Symbol>> factoryListSymb = new Factory<List<Symbol>>() {
    @Override
    public List<Symbol> create() {
      return new ArrayList<Symbol>();
    }
  };

  private Grammar grammar = null;
  private int maxStateN;
  private Map<List<Symbol>, Integer> nullablePartsMap;
  /**
   * ACTION таблица проиндексированная для каждого состояния и символа со значениями ;
   */
  private List<Map<Symbol, List<ActionRecord>>> rn2ActionTable;
  private List<List<LR1Point>> states = new ArrayList<List<LR1Point>>();
  private List<StateTransition> stateTransitions;
  /**
   * Список, содержащий соответствующие состоянию списки сиволов и соотвествующих им символов с расширенными данными.
   * Например для состояния 1 возможны такие записи: {A=[A<h-reg1>, A<gnc-agr>]}.
   * 
   * BaseNonterminalsToNonterminalsExtData mapping
   */
  private List<Map<Symbol, List<Symbol>>> baseNTToNTExtData;

  public List<Map<Symbol, List<Symbol>>> getBaseNTToNTExtData() {
    return baseNTToNTExtData;
  }

  public LR1ParseTableBuilder(Grammar grammar2) {
    this.grammar = grammar2;
  }

  /**
   * Рассчитать замыкание (closure) для имеющихся в нем уже элементов.
   * 
   * Алгоритм (см.  Aho, Sethi, Ullman "Compilers: Principles, Techniques, and Tools")
   * <pre>
   * foreach [A->(alpha)*B(beta), a] from closure
   * foreach [B->(gamma)] from grammar
   * foreach b from FIRST((beta)a)
   * add [B->*(gamma), b] to closure
   * </pre>
   * 
   * Доп. инфо: http://binarysculpting.com/2012/02/04/computing-lr1-closure/
   * 
   * Расчет closure with epsilon rules - http://www.cs.sfu.ca/~anoop/teaching/CMPT-379-Fall-2013/LR.pdf
   * 
   * @param closure входное замыкание, которое потом пополнится
   */
  protected void calculateClosure(Set<LR1Point> closure) {
    int size = -1;
    Set<LR1Point> tempCl = new TreeSet<LR1Point>();
    // выполняем пока увеличивается
    while (size != closure.size()) {
      size = closure.size();
      Iterator<LR1Point> iterator = closure.iterator();
      while (iterator.hasNext()) {
        LR1Point point = iterator.next();
        int pos = point.pos;
        Projection projection =
            point.grammar.getProjections().get(point.projPos);

        if (pos >= projection.body.size())
          continue;

        // extract B
        Symbol B = projection.body.get(pos);
        // продолжаем работу лишь для нетерминалов
        if (!B.isNonTerm())
          continue;
        // пропускаем epsilon
        while (B.isEpsilon() && pos < projection.body.size())
          B = projection.body.get(++pos);

        // если это нетерминал - получаем его дочерние проекции
        // (как для нетерминал без раширенных данных)
        List<Integer> projectionPositions =
            grammar.getNonTerminalProjectionsMap().get(B.cloneBase());

        // extract "(beta)"
        List<Symbol> beta = new ArrayList<>(
            projection.body.subList(pos + 1, projection.body.size()));
        beta.add(point.las);
        Collection<Symbol> lases = calculateFirst(beta);

        // добавить итоговые новые элементы для closure
        for (Symbol las : lases) {
          // пропускаем epsilon
          if (las.isEpsilon())
            continue;

          for (int projPos : projectionPositions) {
            int pos2 = 0;
            Projection projection2 =
                point.grammar.getProjections().get(projPos);

            // extract B
            Symbol s = projection2.body.get(pos2);
            // пропускаем epsilon
            while (s.isEpsilon() && pos2 < projection2.body.size()) {
              pos2++;
              if (pos2 >= projection2.body.size())
                continue;
              s = projection2.body.get(pos2);
            }

            LR1Point newPoint = new LR1Point(grammar, projPos, pos2, las);
            tempCl.add(newPoint);

            // add necessary new points (Experimental part)
            LR1Point tempPoint = newPoint;
            while (tempPoint != null) {
              tempPoint =
                  generateLR1PointWithNullableTerminalsToClosure(tempPoint);
              if (tempPoint != null)
                tempCl.add(tempPoint);
            } // while (tempPoint != null) {

          } // for (int projPos : projectionPositions) {

        } // for (Symbol las : lases) {

      } // while (iterator.hasNext()) {

      closure.addAll(tempCl);
    } // while (size != closure.size()) {
  }

  /**
   * Реализация ф-ии FIRST.
   * 
   * @param symbols строка сиволов на входе
   * @return терминал, рассчитанный в соответствии с алгоритмом.
   */
  protected Collection<Symbol> calculateFirst(List<Symbol> symbols) {
    Set<Symbol> processedNonTerminals = new TreeSet<Symbol>();
    return calculateFirst(symbols, processedNonTerminals);
  }

  private Collection<Symbol> calculateFirst(List<Symbol> symbols,
      Set<Symbol> processedNonTerminals) {
    Set<Symbol> result = new TreeSet<Symbol>();
    // для каждого элемента последовательности сиволов (терминалов или
    // нетерминалов)
    /*
     * Проверка "непроходимость". Например в случае последовательности "S S EOI"
     * начало "S S" может быть пропущено полностью (при наличии проекции S->ε)
     */
    boolean breakTrough = true;

    for (Symbol symbol : symbols) {
      // FIRST(t) = t
      if (symbol.isTerm() || symbol.isEOI() || symbol.isEpsilon()) {
        result.add(symbol);
        return result;
      }
      // FIRST (nt) = FIRST (nt's RHS)
      if (!processedNonTerminals.contains(symbol)) {
        List<Integer> projNumbers =
            grammar.getNonTerminalProjectionsMap().get(symbol.cloneBase());
        processedNonTerminals.add(symbol);
        Set<Symbol> tempResult = new TreeSet<Symbol>();
        for (Integer projNumber : projNumbers) {
          Projection projection = grammar.getProjections().get(projNumber);
          tempResult
              .addAll(calculateFirst(projection.body, processedNonTerminals));
        }
        result.addAll(tempResult);
        // если один из возможных вариантов EPSILON - продолжаем набор, т.к. это
        // означает, что могли пройти ничего не собрав
        if (tempResult.contains(Symbol.EPSILON))
          continue;
        else
          breakTrough = false;
        // суда попадаем, только в случае если результат не содержит EPSILON
        // если набрали конкретных сиволов - из предыдущих результатоа
        // убираем ESPLON - т.к. в случае встречи с ним, мы идем далее и сюда не
        // попадаем.
        //
        // Но если попали сюда - значит наткнулись на нетерминал, который
        // генерирует конечные терминалы и не позволяет в итоговом результатае
        // сохранить EPSILON - EPSILON пропадает (убираем).
        if (tempResult.size() > 0)
          result.remove(Symbol.EPSILON);
      }
      // если что-то набрали () -- это и возвращаем. В обратном случае
      // продолжаем поиски
      if (result.size() > 0 && !breakTrough)
        return result;
    } // for (Symbol symbol : symbols) {
    return result;
  }

  /**
   * @param closure входное замыкание
   * @param eatSymbol один из символов грамматики
   * @return результирующий closure
   */
  protected Set<LR1Point> calculateGoto(Collection<LR1Point> closure,
      Symbol eatSymbol) {
    Set<LR1Point> result = new TreeSet<LR1Point>();

    for (LR1Point p : closure) {
      Projection projection = grammar.get(p.projPos);
      // проверка --- на неперепрыгивание пункта
      if (p.pos + 1 > projection.body.size())
        continue;
      // проверка -- следующий сивол совпадает с принимаемым на вход?
      // (т.е. работаем только по форме ch [A->(alpha)*aB(beta), b] если
      // входное значение a)
      if (!projection.body.get(p.pos).equals(eatSymbol))
        continue;

      result.add(new LR1Point(grammar, p.projPos, p.pos + 1, p.las));
    }

    calculateClosure(result);
    return result;
  }

  public void calculateRN2ActionTable() {
    rn2ActionTable = LazyList.lazyList(
        new ArrayList<Map<Symbol, List<ActionRecord>>>(), factoryListSLA);
    // trasitions
    for (StateTransition st : stateTransitions) {
      List<ActionRecord> actions = new ArrayList<ActionRecord>();
      // insert in every cell of table
      rn2ActionTable.get(st.fromStateN).put(st.label, actions);
      actions.add(ActionRecord.makeShiftActionRecord(st.toStateN));
    }

    indexStatesNullableParts();

    // accept states
    for (int i = 0; i < states.size(); i++) {
      List<LR1Point> list = states.get(i);
      calculateRN2Reductions(rn2ActionTable.get(i), list);
    }

    // process case S=*=>EPSILON -- need add acc to T(0, EOI)
    Symbol rootSymbol = grammar.getRootSymbol();
    Collection<Symbol> first =
        calculateFirst(Collections.singletonList(rootSymbol));
    if (first.isEmpty()
        || (first.size() == 1 && first.contains(Symbol.EPSILON))) {
      // get actions
      List<ActionRecord> actions = rn2ActionTable.get(0).get(Symbol.EOI);
      // check for null
      if (null == actions) {
        actions = new ArrayList<ActionRecord>();
        rn2ActionTable.get(0).put(Symbol.EOI, actions);
      }
      actions.add(ActionRecord.makeAcceptActionRecord());
    }

    fillBaseNTToNTExtData();
  }

  protected void calculateRN2Reductions(
      Map<Symbol, List<ActionRecord>> actionTableEntry, List<LR1Point> state) {

    // accept states
    Symbol rootSymbol = grammar.getRootSymbol();
    List<Integer> rootPrjs =
        grammar.getNonTerminalProjectionsMap().get(rootSymbol);

    for (LR1Point point : state) {
      List<Symbol> body = grammar.get(point.projPos).getBody();
      List<Symbol> tail = body.subList(point.pos, body.size());
      boolean nullable = isNullable(tail);

      if (nullable) {
        // get actions
        List<ActionRecord> actions = actionTableEntry.get(point.las);
        // check for null
        if (null == actions) {
          actions = new ArrayList<ActionRecord>();
          actionTableEntry.put(point.las, actions);
        }
        ActionRecord act = null;
        /*
         * In the RN table a reduction (A ::= a*y, b) is written r(A, m, f),
         * where |a| = m and f=I (y) if m!=0 and f=I(A) if m==0.
         */
        // case S->ab*;
        if (tail.size() == 0) {
          // case S->EPSILON*
          if (body.size() == 1 && body.contains(Symbol.EPSILON)) {
            Integer index = nullablePartsMap.get(Collections
                .singletonList(grammar.get(point.projPos).getHead()));
            int ndx = index == null ? 0 : index.intValue();
            act = ActionRecord.makeRN2ReduceActionRecord(
                grammar.get(point.projPos).getHead(), 0, ndx, point.projPos);
          } else
            act = ActionRecord.makeRN2ReduceActionRecord(
                grammar.get(point.projPos).getHead(), point.pos, 0,
                point.projPos);
        } else {
          Integer index = nullablePartsMap.get(tail);
          int ndx = index == null ? 0 : index.intValue();
          act = ActionRecord.makeRN2ReduceActionRecord(
              grammar.get(point.projPos).getHead(), point.pos, ndx,
              point.projPos);
        }

        actions.add(act);

        // process case S -> S*
        if (rootPrjs.contains(point.projPos) && point.pos == body.size())
          act.accept = true;
      }
    }

  }

  /**
   * Заполнить таблицу соотвествия.
   */
  private void fillBaseNTToNTExtData() {
    baseNTToNTExtData = new ArrayList<Map<Symbol, List<Symbol>>>();
    for (int i = 0; i < rn2ActionTable.size(); i++) {
      Map<Symbol, List<Symbol>> newMapRec =
          LazyMap.lazyMap(new TreeMap<Symbol, List<Symbol>>(), factoryListSymb);
      baseNTToNTExtData.add(newMapRec);
      Map<Symbol, List<ActionRecord>> rec = rn2ActionTable.get(i);
      for (Symbol s : rec.keySet())
        // проверяем основные условия....
        // обрабатываем только сиволы с пометами (расщиренные)
        if (s.isNonTerm() || s.getClass() != Symbol.class) {
          Symbol baseSymbol = s.cloneBase();
          // add mapping if not exists (auto-create list - thaks to lazyMap)
          List<Symbol> list = newMapRec.get(baseSymbol);
          list.add(s);
        }
    }
  }

  protected void calculateRNReductions(
      Map<Symbol, List<ActionRecord>> actionTableEntry, List<LR1Point> state) {

    // accept states
    Symbol rootSymbol = grammar.getRootSymbol();
    List<Integer> rootPrjs =
        grammar.getNonTerminalProjectionsMap().get(rootSymbol);

    for (LR1Point point : state) {
      List<Symbol> body = grammar.get(point.projPos).getBody();
      List<Symbol> tail = body.subList(point.pos, body.size());
      boolean nullable = isNullable(tail);

      if (nullable) {
        // get actions
        List<ActionRecord> actions = actionTableEntry.get(point.las);
        // check for null
        if (null == actions) {
          actions = new ArrayList<ActionRecord>();
          actionTableEntry.put(point.las, actions);
        }

        // вариант, когда тело до точки состоит только из epsilon [A->ε*]
        ActionRecord act = null;
        if (body.size() == 1 && body.contains(Symbol.EPSILON))
          act = ActionRecord.makeRNReduceActionRecord(
              grammar.get(point.projPos).getHead(), 0);
        else
          // in all other cases
          act = ActionRecord.makeRNReduceActionRecord(
              grammar.get(point.projPos).getHead(), point.pos);

        actions.add(act);

        // process case S -> S*
        if (rootPrjs.contains(point.projPos) && point.pos == body.size())
          act.accept = true;
      }
    }

  }

  /**
   *  Построение таблицы LR1-парсинга.
   *  
   *  Алгоритм построения таблицы следующий:
  * <ol>
  * <li>собрать все терминалы и нетерминлы грамматики;</li>
  * <li>формируем CLOSURE для первой проекции, добавляем полеченный набор состояний во множество {@link ParseTableBuilder#states}</li>
  * <li>для каждого сформированного состояния и каждого терминала/нетерминала вычисляем GOTO 
  * (могут формироваться новые состояни или пустые(ошибочные состояния)) - таким образом сможем заполнить GOTO-таблицу</li>
  * <li>для каждого сформированного состояния проверяем наличие приемочных значения и соответствующим образом заполняем ACTION-таблицу</li>
  * </ol>
   */
  public void calculateStates() {
    states = new ArrayList<List<LR1Point>>();
    stateTransitions = new ArrayList<StateTransition>(10);
    maxStateN = -1;
    // 1
    Set<Symbol> symbols = grammar.getAllSymbols();
    // 2
    Set<LR1Point> closureSet = new TreeSet<LR1Point>();

    // Инициализировать первоначальное значение closure данными по пунктам
    // первоначальных проекций
    List<Integer> rootProjectionsI =
        grammar.getNonTerminalProjectionsMap().get(grammar.getRootSymbol());
    if (rootProjectionsI.isEmpty())
      throw new IllegalStateException(
          "Некорректное значение 'root_symbol'. Необходима проверка грамматики.");
    for (int i : rootProjectionsI) {
      // process case "S->EPSILON;"
      if (grammar.get(i).getBody().size() == 1
          && grammar.get(i).getBody().contains(Symbol.EPSILON))
        closureSet.add(new LR1Point(grammar, i, 1, Symbol.EOI));
      else
        closureSet.add(new LR1Point(grammar, i, 0, Symbol.EOI));
    }

    calculateClosure(closureSet);
    int number1 = getStateNumberByClosure(closureSet);
    if (number1 > maxStateN)
      maxStateN = number1;

    // 3 (тут maxStateN постепенно увеличивается...)
    for (int i = 0; i <= maxStateN; i++) {
      for (Symbol symbol : symbols) {
        List<LR1Point> state = states.get(i);
        Set<LR1Point> gotoSet = calculateGoto(state, symbol);
        // если множество непустое заполняем так или сяк
        if (gotoSet.size() > 0) {
          // если множество непустое заполняем так или сяк
          int number = getStateNumberByClosure(gotoSet);
          if (number > maxStateN)
            maxStateN = number;

          stateTransitions.add(new StateTransition(i, symbol, number));
        }
      }
    }
  }

  /**
   * 
   * Genertae new states from points with nullable terminals. Experimental part
   * 
   * Ex1: "E -> *FG, b" (FIRST(F)={EPSILON}, FIRST(G)={g, EPSILON}) generates "E -> F*G, b"
   * 
   * Ex2: "E -> F*G, b" (FIRST(F)={EPSILON}, FIRST(G)={g, EPSILON}) generates "E -> FG*, b" 
   * (don't work for lookup symbol 'g' - enters in FIRST)
   * 
   * 
   * @param closureSet
   * @param point
   */
  protected LR1Point
      generateLR1PointWithNullableTerminalsToClosure(final LR1Point point) {

    LR1Point newPoint =
        new LR1Point(point.grammar, point.projPos, point.pos, point.las);
    Projection projection =
        newPoint.grammar.getProjections().get(point.projPos);

    // skip projection forms A -> BC*
    if (newPoint.pos >= projection.body.size())
      return null;

    Symbol s = projection.body.get(newPoint.pos);
    Collection<Symbol> possibleTerms =
        calculateFirst(Collections.singletonList(s));

    if ((possibleTerms.contains(Symbol.EPSILON) && possibleTerms.size() == 1)
        || (possibleTerms.contains(Symbol.EPSILON)
            && !possibleTerms.contains(newPoint.las))) {
      newPoint.pos++;
      return newPoint;
    }
    return null;
  }

  protected List<LR1Point> getAcceptingPoints(Collection<LR1Point> list) {
    List<LR1Point> result = new ArrayList<LR1Point>();
    for (LR1Point point : list) {
      // добавляем лишь в случае если сама точка пункта находится за
      // последним символом тела проекции
      if (point.pos == grammar.getProjections().get(point.projPos).body.size())
        result.add(point);
    }
    return result;
  }

  protected Grammar getGrammar() {
    return grammar;
  }

  protected Map<List<Symbol>, Integer> getNullablePartsMap() {
    return nullablePartsMap;
  }

  public List<Map<Symbol, List<ActionRecord>>> getRN2ActionTable() {
    return rn2ActionTable;
  }

  /**
   * Получить номер состояния из CLOSURE за счет поддержания набора состояний.
   * Если находим, то даем имеющийся номер, если не находим, то даем новый и добавлям.
   * 
   * @param closure
   * @return
   */
  protected int getStateNumberByClosure(Collection<LR1Point> closure) {
    List<LR1Point> sortList = new ArrayList<LR1Point>(closure);
    Collections.sort(sortList);
    int result = states.indexOf(sortList);
    if (-1 == result) {
      result = states.size();
      states.add(sortList);
    }
    return result;
  }

  public List<List<LR1Point>> getStates() {
    return states;
  }

  protected List<StateTransition> getStateTransitions() {
    return stateTransitions;
  }

  private void indexStatesNullableParts() {
    Set<List<Symbol>> nullablePartsSet =
        new TreeSet<List<Symbol>>(new ListSymbolComparator());
    Map<Symbol, Boolean> isNullableNonterminalMap =
        grammar.getIsNullableNonterminalMap();
    for (Map.Entry<Symbol, Boolean> entry : isNullableNonterminalMap
        .entrySet()) {
      if (entry.getValue()) {
        List<Symbol> l = new ArrayList<Symbol>();
        l.add(entry.getKey());
        nullablePartsSet.add(l);
      }
    }

    // accept states
    for (int i = 0; i < states.size(); i++) {
      List<LR1Point> state = states.get(i);

      for (LR1Point point : state) {
        List<Symbol> body = grammar.get(point.projPos).getBody();
        List<Symbol> tail = body.subList(point.pos, body.size());

        // not concern this case
        if (tail.size() == 0)
          continue;
        // not concern this case
        if (tail.size() == 1 && tail.contains(Symbol.EPSILON))
          continue;

        boolean nullable = isNullable(tail);

        if (nullable)
          nullablePartsSet.add(tail);
      }
    }

    ArrayList<List<Symbol>> nullableParts =
        new ArrayList<List<Symbol>>(nullablePartsSet);
    Collections.sort(nullableParts, new Comparator<List<Symbol>>() {
      @Override
      public int compare(List<Symbol> o1, List<Symbol> o2) {
        return Integer.compare(o1.size(), o2.size());
      }
    });

    nullablePartsMap = new HashMap<List<Symbol>, Integer>();
    for (int i = 0; i < nullableParts.size(); i++) {
      nullablePartsMap.put(nullableParts.get(i), i + 1);
    }

  }

  protected boolean isNullable(List<Symbol> args) {
    boolean res = true;
    for (Symbol smb : args) {
      if (smb.isEOI())
        continue;
      if (smb.isEpsilon()) {
        res &= true;
        continue;
      }

      if (smb.isTerm())
        return false;

      res &= grammar.getIsNullableNonterminalMap().get(smb.cloneBase());
    }
    return res;
  }

}
