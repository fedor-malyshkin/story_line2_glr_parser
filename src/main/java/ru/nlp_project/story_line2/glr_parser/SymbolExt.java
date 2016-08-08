package ru.nlp_project.story_line2.glr_parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;

public class SymbolExt extends Symbol {

  public static List<SymbolExt> cloneListExt(List<SymbolExt> in) {
    List<SymbolExt> result = new LinkedList<SymbolExt>();
    for (SymbolExt el : in)
      result.add((SymbolExt) el.clone());
    return result;
  }

  private List<SymbolExtData> extDatas;

  private List<SymbolInterpData> interpDatas;

  private Map<SymbolExtDataTypes, SymbolExtData> extDatasMap;

  public SymbolExt(String value, SymbolTypes type,
      List<SymbolExtData> extDatas) {
    super(value, type);
    if (extDatas != null) {
      this.extDatas = new LinkedList<SymbolExtData>(extDatas);
      // sort to compare it between SymbolExt later
      Collections.sort(this.extDatas);
    } else
      this.extDatas = Collections.emptyList();

    this.interpDatas = Collections.emptyList();
    // create map of ext data for conventient processing
    createExtDatasMap();
  }

  public SymbolExt(String value, SymbolTypes type, List<SymbolExtData> extDatas,
      List<SymbolInterpData> interpDatas) {
    super(value, type);
    if (extDatas != null) {
      this.extDatas = new LinkedList<SymbolExtData>(extDatas);
      // sort to compare it between SymbolExt later
      Collections.sort(this.extDatas);
    } else
      this.extDatas = Collections.emptyList();
    // create map of ext data for conventient processing
    createExtDatasMap();

    if (interpDatas != null)
      this.interpDatas = new LinkedList<SymbolInterpData>(interpDatas);
    else
      this.interpDatas = Collections.emptyList();
  }

  @Override
  public Symbol clone() {
    // this.extDatas- не копируется
    SymbolExt result = new SymbolExt(this.value, this.symbolType, this.extDatas,
        this.interpDatas);
    result.eoi = this.eoi;
    result.epsilon = this.epsilon;
    return result;
  }

  /** 
   * Принцип сравнения простой:
   * <ul>
   * <li>если аргумент экземпляр неизвестного класса - исключение</li>
   * <li>если нет расширенных данных и экземпляр класса {@link Symbol}- передать управление {@link Symbol#compareToInt} </li>
   * <li>если есть расширенные данные и экземпляр класса {@link Symbol}- больше</li>
   * <li>если есть расширенные данные - выполнить глубокое сравнение</li>
   * </ul>
   */
  @Override
  public int compareTo(Symbol obj) {
    if (obj.getClass() == Symbol.class) {
      Symbol other = (Symbol) obj;
      if (extDatas.isEmpty())
        return -1 * other.compareToInt(this);
      else
        return 1;
    } else if (obj.getClass() == SymbolExt.class) {
      SymbolExt other = (SymbolExt) obj;
      return compareToInt(other);
    } else
      throw new IllegalStateException("Unknown class: " + obj.getClass());
  }

  /**
   * 
   * Класс для фактического сравнения объектов на больше/меньше.
   * ВАЖНО: Тут не производится проверка классов.
  */
  public int compareToInt(SymbolExt other) {
    if (super.compareToInt(other) != 0)
      return super.compareToInt(other);

    if (extDatas.size() != other.getExtDatas().size())
      return extDatas.size() - other.getExtDatas().size();

    for (int i = 0; i < extDatas.size(); i++) {
      int r = 0;
      if ((r = extDatas.get(i).compareTo(other.getExtDatas().get(i))) != 0)
        return r;
    }
    return 0;

  }

  private void createExtDatasMap() {
    extDatasMap =
        new HashMap<SymbolExtDataTypes, SymbolExtData>(extDatas.size());
    extDatas.forEach(d -> extDatasMap.put(d.getType(), d));
  }

  /** 
   * Принцип сравнения простой:
   * <ul>
   * <li>если аргумент экземпляр неизвестного класса - исключение</li>
   * <li>если нет расширенных данных и экземпляр класса {@link Symbol}- передать управление {@link Symbol#equalsInt} </li>
   * <li>если есть расширенные данные и экземпляр класса {@link Symbol}- не равны</li>
   * <li>если есть расширенные данные - выполнить глубокое сравнение</li>
   * </ul>
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (obj.getClass() == Symbol.class) {
      Symbol other = (Symbol) obj;
      if (extDatas.isEmpty())
        return other.equalsInt(this);
      else
        return false;
    } else if (obj.getClass() == SymbolExt.class) {
      SymbolExt other = (SymbolExt) obj;
      return equalsInt(other);
    } else
      throw new IllegalStateException("Unknown class: " + obj.getClass());
  }

  /**
   * 
   * Класс для фактического сравнения объектов на эквивалентность.
   * ВАЖНО: Тут не производится проверка классов.
  
    * <ol>
    * <li>если разные длины {@link #extDatas} - не равны </li>
    * <li>если одинаковые длины {@link #extDatas} - сравнивать каждый элемент</li>
    * </ol>
  
   * @param other
   * @return
   */
  public boolean equalsInt(SymbolExt other) {
    if (!super.equalsInt(other))
      return false;

    if (extDatas.size() != other.getExtDatas().size())
      return false;

    for (int i = 0; i < extDatas.size(); i++)
      if (!extDatas.get(i).equals(other.getExtDatas().get(i)))
        return false;

    return true;
  }

  public List<SymbolExtData> getExtDatas() {
    return extDatas;
  }

  public Map<SymbolExtDataTypes, SymbolExtData> getExtDatasMap() {
    return extDatasMap;
  }

  public List<SymbolInterpData> getInterpDatas() {
    return interpDatas;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result =
        result + (extDatas.isEmpty() ? 0 : ListUtils.hashCodeForList(extDatas));
    result = result
        + (interpDatas.isEmpty() ? 0 : ListUtils.hashCodeForList(interpDatas));
    return result;
  }

  @Override
  public String toString() {
    String res = super.toString();
    if (extDatas != null && extDatas.size() > 0) {
      res += "<";
      res += StringUtils.join(extDatas, ", ");
      res += ">";
    }
    if (interpDatas != null && interpDatas.size() > 0) {
      res += "{";
      res += StringUtils.join(interpDatas, ", ");
      res += "}";
    }
    return res;
  }
}
