package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Построитель таблиц парсинга.
 * <p>

 * 
 * @author fedor
 *
 */
class LR0ParseTableBuilder {
  /**
   * ACTION таблица проиндексированная для каждого состояния со значениями либо -1 (shift), либо номер проекции для reduce, 
   * 0-я проекция -- самая главная (приводящая к принятию строки как успешно распознанной);
   */
  List<Integer> actionTable = null;
  /**
   * GOTO таблица проиндексированная для каждого состояния с номерами состояний для перехода, либо с null для ошибочных состояний.
   */
  List<Map<Symbol, Integer>> gotoTable = null;
  Grammar grammar = null;

  List<List<LR0Point>> states = new ArrayList<>();

  /**
   * Рассчет CLOSURE (см.  Aho, Sethi, Ullman "Compilers: Principles, Techniques, and Tools")
   *  
   * @param point - стартовый пункт
   * @return - перечень итоговых пунктов
   */
  Set<LR0Point> calculateClosure(LR0Point point) {
    Set<LR0Point> result = new HashSet<LR0Point>();
    result.add(point);
    int setSize = 0;
    List<LR0Point> analyzePoints = new ArrayList<LR0Point>();
    analyzePoints.add(point);
    // пока меняется множество -- ищем следующие пункты
    while (setSize != result.size()) {
      setSize = result.size();
      List<LR0Point> analyzePointsNext = new ArrayList<LR0Point>();
      for (LR0Point wp : analyzePoints) {
        // получаем следующие возможные пункты при условии получения
        // необходимого терминала/нетерминала
        List<LR0Point> points = getNextPoints(wp);
        result.addAll(points);
        analyzePointsNext.addAll(points);
      }
      // меняем пункты по которым будем осуществлять дальнейший анализ
      analyzePoints = analyzePointsNext;
    }
    // if (point.pos < projection.body.length)

    return result;
  }

  /**
   * Получить следующий список пунктов из текущего (по генерирующим проекциям)
   * 
   * @param point
   * @return
   */
  List<LR0Point> getNextPoints(LR0Point point) {
    List<LR0Point> result = new ArrayList<LR0Point>();
    Projection projection = grammar.get(point.projPos);
    // проверка - вдруг пункт в конце проекции
    if (point.pos == projection.body.size())
      return result;
    Symbol startSymbol = projection.body.get(point.pos);
    // не выполняем никакой работы для терминалов (для случаев: A->B*nC)
    if (startSymbol.isTerm())
      return result;
    for (int i = 0; i < grammar.getProjections().size(); i++) {
      if (!grammar.getProjections().get(i).head.equals(startSymbol))
        continue;
      result.add(new LR0Point(grammar, i, 0));
    }
    return result;
  }

  /**
   * Получить номер состояния из CLOSURE за счет поддержания набора состояний -- находим даем имеющийся номер, не находим -- даем новый.
   * 
   * TODO: Пересмотреть код за счет импользования "kernel state"
   * 
   * @param closure
   * @return
   */
  int getStateNumberByClosure(Collection<LR0Point> closure) {
    List<LR0Point> sortList = new ArrayList<LR0Point>(closure);
    Collections.sort(sortList);
    int result = states.indexOf(sortList);
    if (-1 == result) {
      result = states.size();
      states.add(sortList);
    }
    return result;
  }

  /**
   * Рассчет GOTO-функции (см.  Aho, Sethi, Ullman "Compilers: Principles, Techniques, and Tools")
   *  
   * @param closure - состояние из которого делается выход
   * @param eatSymbol - сивол, подаваемый на вход
   * @return - получаемое состояние
   */

  Set<LR0Point> calculateGoto(Collection<LR0Point> closure, Symbol eatSymbol) {
    Set<LR0Point> result = new HashSet<LR0Point>();
    for (LR0Point p : closure) {
      Projection projection = grammar.get(p.projPos);
      // проверка --- на неперепрыгивание пункта
      if (p.pos + 1 > projection.body.size())
        continue;
      // проверка -- следующий сивол совпадает с принимаемым на вход?
      if (!projection.body.get(p.pos).equals(eatSymbol))
        continue;

      Set<LR0Point> set = calculateClosure(new LR0Point(p.grammar, p.projPos,
          p.pos + 1));
      result.addAll(set);
    }
    return result;
  }

  /**
   *  Построение таблицы LR-парсинга.
   *  
   *  Алгоритм построения таблицы следующий:
  * <ol>
  * <li>собрать все терминалы и нетерминлы грамматики;</li>
  * <li>формируем CLOSURE для первой проекции и первого символа добавляем полеченный набор состояний во множество {@link ParseTableBuilder#states}</li>
  * <li>для каждого сформированного состояния и каждого терминала/нетерминала вычисляем GOTO 
  * (могут формироваться новые состояни или пустые(ошибочные состояния)) - таким образом сможем заполнить GOTO-таблицу</li>
  * <li>для каждого сформированного состояния проверяем наличие приемочных значения и соответствующим образом заполняем ACTION-таблицу</li>
  * </ol>
   */
  void calculateParseTable() {
    actionTable = new ArrayList<Integer>();
    gotoTable = new ArrayList<Map<Symbol, Integer>>();
    int maxStateN = -1;
    // 1
    Set<Symbol> symbols = collectAllSymbols();
    // 2
    Set<LR0Point> closure = calculateClosure(new LR0Point(grammar, 0, 0));
    int number1 = getStateNumberByClosure(closure);
    if (number1 > maxStateN) {
      maxStateN = number1;
      gotoTable.add(new HashMap<Symbol, Integer>());
    }

    // 3
    for (int i = 0; i <= maxStateN; i++) {
      for (Symbol symbol : symbols) {
        List<LR0Point> list = states.get(i);
        Set<LR0Point> gotoSet = calculateGoto(list, symbol);
        // если множество непустое заполняем так или сяк
        if (gotoSet.size() > 0) {
          int number = getStateNumberByClosure(gotoSet);
          if (number > maxStateN) {
            maxStateN = number;
            gotoTable.add(new HashMap<Symbol, Integer>());
          }
          // для текущего остояния -- указываем на какое состояние переходим при
          // получении указанного символа
          gotoTable.get(i).put(symbol, number);
        } else
          // при нулевом размере набора - неверный переход
          gotoTable.get(i).put(symbol, -1);
      }
    }
    // 4
    for (int i = 0; i < states.size(); i++) {
      List<LR0Point> list = states.get(i);
      List<LR0Point> accPoints = getAcceptingPoints(list);
      if (accPoints == null || accPoints.size() == 0) {
        actionTable.add(-1);
      } else {
        // на предмет проверки reduce/reduce
        if (accPoints.size() > 1)
          throw new IllegalStateException(
              String
                  .format(
                      "В состоянии '%s' обнарущено более одного принимающего пункта: '%s'",
                      list.toString(), accPoints.toString()));
        actionTable.add(accPoints.get(0).projPos);
      }
    }

  }

  List<LR0Point> getAcceptingPoints(Collection<LR0Point> list) {
    List<LR0Point> result = new ArrayList<LR0Point>();
    for (LR0Point point : list) {
      // добавляем лишь в случае если сама точка пункта находится за последним
      // символом тела проекции
      if (point.pos == grammar.getProjections().get(point.projPos).body.size())
        result.add(point);
    }
    return result;
  }

  Set<Symbol> collectAllSymbols() {
    Set<Symbol> result = new HashSet<Symbol>();
    for (Projection proj : grammar.getProjections()) {
      result.add(proj.head);
      result.addAll(proj.body);
    }
    return result;
  }

}
