package ru.nlp_project.story_line2.glr_parser;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.GrammemeUtils.GrammemeEnum;
import ru.nlp_project.story_line2.morph.Grammemes;

public class SymbolExtData implements Comparable<SymbolExtData> {

  static public class GUBlock {
    boolean combined = false;
    Set<GrammemeEnum> grammemesSet;

    public GUBlock(boolean combined, Set<GrammemeEnum> grammemesSet) {
      super();
      this.combined = combined;
      this.grammemesSet = new TreeSet<GrammemeEnum>(grammemesSet);
    }

    public GUBlock(Set<GrammemeEnum> grammemesSet) {
      super();
      this.grammemesSet = new TreeSet<GrammemeEnum>(grammemesSet);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      GUBlock other = (GUBlock) obj;
      if (combined != other.combined)
        return false;
      if (grammemesSet == null) {
        if (other.grammemesSet != null)
          return false;
      } else if (!grammemesSet.equals(other.grammemesSet))
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (combined ? 1231 : 1237);
      result = prime * result
          + ((grammemesSet == null) ? 0 : grammemesSet.hashCode());
      return result;
    }

    @Override
    public String toString() {
      if (this.combined)
        return "&" + "(" + StringUtils.join(grammemesSet, ",") + ")";
      else
        return "(" + StringUtils.join(grammemesSet, ",") + ")";
    }
  }

  public enum SymbolExtDataTypes {
    // Label 0-99
    h_reg1(0, true, true, true, true), 
    h_reg2(1, true, true, true, true),  
    l_reg(2, true, true, true, true), 
    quoted(3, true, true, true, true), 
    l_quoted(4, true, true, true, true), 
    r_quoted(5, true, true, true, true), 
    fw(6, true, false, false, true), 
    mw(7, true, false, false, true), 
    lat(8, true, true, true, true), 
    no_hom(9, true, false, false, false), 
    cut(10, true, true, false, false), 
    rt(11, true, true, false, false), 
    dict(12,true, false, true, true),

    // Param 100-199
    kwtype(100, true, true, true, true), 
    kwtypef(101, true, true, true, true), 
    kwtypel(102, true, true, true, true), 
    gram(103, true, true, false, true), 
    gram_ex(104, true, true, false, true), 
    rx(105, true, true, false, true), 
    rxf(106, true, true, false, true), 
    rxl(107, true, true, false, true),

    // Array 200-299
    gu(200, true, true, false, true), 
    kwset(201, true, true, false, true), 
    kwsetf(202, true, true, false, true), 
    kwsetl(203, true, true, false, true), 
    gnc_agr(204, true, true, false, true), 
    nc_agr(205, true, true, false, true), 
    c_agr(206, true, true, false, true), 
    gn_agr(207, true, true, false, true), 
    gc_agr(208, true, true, false, true), 
    fem_c_agr(209, true, true, false, true), 
    after_num_agr(210, true, true, false, false), 
    sp_agr(211, true, true, false, true), 
    fio_agr(212, true, true, false, true), 
    geo_agr(213, true,true, false, false);                                                                                                               

    /**
     * Внутренний не меняющийся индекс.
     */
    private int index;
    /**
     * Применяется к терминалам.
     */
    private boolean applyToT = false;

    /**
     * Применяется к нетерминалам.
     */
    private boolean applyToNT = false;
    /**
     * может использоваться в фильтрах.
     */
    private boolean canBeUsedInFilter = false;
    /**
     * может быть негативным.
     */
    private boolean canBeNegative = false;

    SymbolExtDataTypes(int i) {
      this.index = i;
    }

    SymbolExtDataTypes(int i, boolean applyToT, boolean applyToNT) {
      this.index = i;
      this.applyToT = applyToT;
      this.applyToNT = applyToNT;
    }

    SymbolExtDataTypes(int i, boolean applyToT, boolean applyToNT,
        boolean canBeUsedInFilter, boolean canBeNegative) {
      this.index = i;
      this.applyToT = applyToT;
      this.applyToNT = applyToNT;
      this.canBeUsedInFilter = canBeUsedInFilter;
      this.canBeNegative = canBeNegative;
    }

    public int getIndex() {
      return index;
    }

    public boolean isApplyToNonTerminal() {
      return applyToNT;
    }

    public boolean isApplyToTerminal() {
      return applyToT;
    }

    public boolean isCanBeNegative() {
      return canBeNegative;
    }

    public boolean isCanBeUsedInFilter() {
      return canBeUsedInFilter;
    }
  }

  private static Grammemes extractGrammemesListValues(String paramValue) {
    String val = StringUtils.removeEnd(paramValue, "\"");
    val = StringUtils.removeStart(val, "\"");
    val = val.trim();
    Grammemes result = new Grammemes();
    try {
      GrammemeUtils.fillGrammemesByCSVMyTags(val, result, true);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Incorrect grammeme value.", e);
    }
    return result;
  }

  private static List<GUBlock> extractGUValues(String arrayValue) {
    List<GUBlock> result = new LinkedList<GUBlock>();
    String[] strings = StringUtils.split(arrayValue, "|");
    for (String str : strings) {
      boolean combined = str.trim().startsWith("&");
      String val = StringUtils.removeStart(str.trim(), "&");
      val = StringUtils.removeEnd(val, "\"");
      val = StringUtils.removeStart(val, "\"");
      val = StringUtils.removeStart(val, "(");
      val = StringUtils.removeEnd(val, ")");
      Set<GrammemeEnum> guSet =
          GrammemeUtils.createGrammemesSetByCSVMyTags(val);
      result.add(new GUBlock(combined, guSet));
    }
    return result;
  }

  private static String extractStringValue(String paramValue,
      boolean toLowerCase) {
    String val = StringUtils.removeEnd(paramValue, "\"");
    val = StringUtils.removeStart(val, "\"");
    if (toLowerCase)
      return val.trim().toLowerCase();
    else
      return val.trim();
  }

  private static List<String> extractStringValues(String arrayValue,
      boolean toLowerCase) {
    List<String> result = new LinkedList<String>();
    String[] strings = StringUtils.split(arrayValue, ",");
    for (String str : strings) {
      String val = StringUtils.removeEnd(str.trim(), "\"");
      val = StringUtils.removeStart(val, "\"");
      if (toLowerCase)
        result.add(val.trim().toLowerCase());
      else
        result.add(val.trim());
    }
    return result;
  }

  public static boolean has(List<SymbolExtData> extDatas,
      SymbolExtDataTypes symbolExtDataType) {
    for (SymbolExtData ed : extDatas)
      if (ed.type == symbolExtDataType)
        return true;
    return false;
  }

  public static SymbolExtData makeArrayExtData(String arrayName,
      String arrayValue) {
    SymbolExtData result = null;
    switch (arrayName.toLowerCase()) {
    /*
     * gu(200), kwset(201), kwsetf(202), gnc_agr(203), nc_agr(204), c_agr(205),
     * gn_agr( 206), fem_c_agr(207), after_num_agr(208), sp_agr(209),
     * fio_agr(210), geo_agr( 211);
     */
    case "gu":
      result = new SymbolExtData(SymbolExtDataTypes.gu);
      result.guValue = extractGUValues(arrayValue);
      break;
    case "kwset":
      result = new SymbolExtData(SymbolExtDataTypes.kwset);
      result.kwSetValue = new HashSet<>(extractStringValues(arrayValue, true));
      break;
    case "kwsetf":
      result = new SymbolExtData(SymbolExtDataTypes.kwsetf);
      result.kwSetValue = new HashSet<>(extractStringValues(arrayValue, true));
      break;
    case "kwsetl":
      result = new SymbolExtData(SymbolExtDataTypes.kwsetl);
      result.kwSetValue = new HashSet<>(extractStringValues(arrayValue, true));
      break;

    case "gnc-agr":
      result = new SymbolExtData(SymbolExtDataTypes.gnc_agr);
      break;
    case "fem-c-agr":
      result = new SymbolExtData(SymbolExtDataTypes.fem_c_agr);
      break;
    case "nc-agr":
      result = new SymbolExtData(SymbolExtDataTypes.nc_agr);
      break;
    case "gc-agr":
      result = new SymbolExtData(SymbolExtDataTypes.gc_agr);
      break;
    case "c-agr":
      result = new SymbolExtData(SymbolExtDataTypes.c_agr);
      break;
    case "gn-agr":
      result = new SymbolExtData(SymbolExtDataTypes.gn_agr);
      break;
    case "after-num-agr":
      result = new SymbolExtData(SymbolExtDataTypes.after_num_agr);
      break;
    case "sp-agr":
      result = new SymbolExtData(SymbolExtDataTypes.sp_agr);
      break;
    case "fio-agr":
      result = new SymbolExtData(SymbolExtDataTypes.fio_agr);
      break;
    case "geo-agr":
      result = new SymbolExtData(SymbolExtDataTypes.geo_agr);
      break;
    default:
      throw new IllegalStateException("Unknown array name: " + arrayName);
    }

    result.arrayName = arrayName;
    result.arrayValue = arrayValue;
    return result;
  }

  public static SymbolExtData makeLabelExtData(String labelName) {
    SymbolExtDataTypes labelType = SymbolExtDataTypes.rt;
    switch (labelName.toLowerCase()) {
    /*
     * h_reg1(0), h_reg2(1), l_reg(2), quoted(3), l_quoted(4), r_quoted(5),
     * fw(6), mw( 7), lat(8), no_hom(9), cut(10), rt(11), dict(12)
     */
    case "rt":
      labelType = SymbolExtDataTypes.rt;
      break;
    case "h-reg1":
      labelType = SymbolExtDataTypes.h_reg1;
      break;
    case "h-reg2":
      labelType = SymbolExtDataTypes.h_reg2;
      break;
    case "l-reg":
      labelType = SymbolExtDataTypes.l_reg;
      break;
    case "quoted":
      labelType = SymbolExtDataTypes.quoted;
      break;
    case "l-quoted":
      labelType = SymbolExtDataTypes.l_quoted;
      break;
    case "r-quoted":
      labelType = SymbolExtDataTypes.r_quoted;
      break;
    case "fw":
      labelType = SymbolExtDataTypes.fw;
      break;
    case "mw":
      labelType = SymbolExtDataTypes.mw;
      break;
    case "lat":
      labelType = SymbolExtDataTypes.lat;
      break;
    case "no-hom":
      labelType = SymbolExtDataTypes.no_hom;
      break;
    case "cut":
      labelType = SymbolExtDataTypes.cut;
      break;
    case "dict":
      labelType = SymbolExtDataTypes.dict;
      break;
    default:
      throw new IllegalStateException("Unknown label: " + labelName);
    }
    SymbolExtData result = new SymbolExtData(labelType);
    result.labelName = labelName;
    return result;
  }

  public static SymbolExtData makeParamExtData(String paramName,
      String paramValue) {
    SymbolExtData result = null;
    switch (paramName.toLowerCase()) {
    case "gram":
      result = new SymbolExtData(SymbolExtDataTypes.gram);
      result.grammValue = extractGrammemesListValues(paramValue);
      break;
    case "gram-ex":
      result = new SymbolExtData(SymbolExtDataTypes.gram_ex);
      result.grammValue = extractGrammemesListValues(paramValue);
      break;
    case "kwtype":
      result = new SymbolExtData(SymbolExtDataTypes.kwtype);
      result.kwTypeValue = extractStringValue(paramValue, true);
      break;
    case "kwtypef":
      result = new SymbolExtData(SymbolExtDataTypes.kwtypef);
      result.kwTypeValue = extractStringValue(paramValue, true);
      break;
    case "kwtypel":
      result = new SymbolExtData(SymbolExtDataTypes.kwtypel);
      result.kwTypeValue = extractStringValue(paramValue, true);
      break;
    case "rx":
      result = new SymbolExtData(SymbolExtDataTypes.rx);
      result.rxValue = extractStringValue(paramValue, false);
      break;
    case "rxf":
      result = new SymbolExtData(SymbolExtDataTypes.rxf);
      result.rxValue = extractStringValue(paramValue,false);
      break;
    case "rxl":
      result = new SymbolExtData(SymbolExtDataTypes.rxl);
      result.rxValue = extractStringValue(paramValue,false);
      break;
    default:
      throw new IllegalStateException("Unknown param name: " + paramName);
    }
    result.paramName = paramName;
    result.paramValue = paramValue;
    return result;
  }

  private String rxValue;

  private String arrayName;
  private String arrayValue;
  private Grammemes grammValue;
  private List<GUBlock> guValue;
  private Set<String> kwSetValue;
  private String kwTypeValue;
  private String labelName;
  private String paramName;
  private String paramValue;
  private SymbolExtDataTypes type;

  private SymbolExtData(SymbolExtDataTypes type) {
    super();
    this.type = type;
  }

  @Override
  public int compareTo(SymbolExtData o) {
    int r = type.getIndex() - o.getType().getIndex();
    if (r != 0)
      return r;

    // Label 0-99
    if (type.getIndex() >= 0 && type.getIndex() < 100)
      return labelName.compareTo(o.getLabelName());

    // Param 100-199
    // kwtype(100), gram(101), label(102),
    if (type.getIndex() >= 100 && type.getIndex() < 200)
      return paramValue.compareTo(o.getParamValue());
    // Array 200-299
    // gu(200), kwset(201), kwsetf(202), gnc_agr(203), nc_agr(204), c_agr(
    if (type.getIndex() >= 200 && type.getIndex() < 300)
      return arrayValue.compareTo(o.getArrayValue());

    return 0;

  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SymbolExtData other = (SymbolExtData) obj;
    if (type != other.type)
      return false;
    // Label 0-99
    if (type.getIndex() >= 0 && type.getIndex() < 100) {
      if (labelName == null) {
        if (other.labelName != null)
          return false;
      } else if (!labelName.equals(other.labelName))
        return false;
    }
    // Param 100-199
    // kwtype(100), gram(101), label(102),
    if (type.getIndex() >= 100 && type.getIndex() < 200) {
      if (grammValue == null) {
        if (other.grammValue != null)
          return false;
      } else if (!grammValue.equals(other.grammValue))
        return false;

      if (kwTypeValue == null) {
        if (other.kwTypeValue != null)
          return false;
      } else if (!kwTypeValue.equals(other.kwTypeValue))
        return false;

      if (paramValue == null) {
        if (other.paramValue != null)
          return false;
      } else if (!paramValue.equals(other.paramValue))
        return false;
    }

    // Array 200-299
    // gu(200), kwset(201), kwsetf(202), gnc_agr(203), nc_agr(204), c_agr(
    if (type.getIndex() >= 200 && type.getIndex() < 300) {
      if (arrayValue == null) {
        if (other.arrayValue != null)
          return false;
      } else if (!arrayValue.equals(other.arrayValue))
        return false;
      if (guValue == null) {
        if (other.guValue != null)
          return false;
      } else if (!guValue.equals(other.guValue))
        return false;

      if (kwSetValue == null) {
        if (other.kwSetValue != null)
          return false;
      } else if (!kwSetValue.equals(other.kwSetValue))
        return false;
    }

    return true;
  }

  public String getArrayName() {
    return arrayName;
  }

  public String getArrayValue() {
    return arrayValue;
  }

  public Grammemes getGrammValue() {
    return grammValue;
  }

  public List<GUBlock> getGuValue() {
    return guValue;
  }

  public Set<String> getKwSetValue() {
    return kwSetValue;
  }

  public String getKwTypeValue() {
    return kwTypeValue;
  }

  public String getLabelName() {
    return labelName;
  }

  public String getParamName() {
    return paramName;
  }

  public String getParamValue() {
    return paramValue;
  }

  public String getRxValue() {
    return rxValue;
  }

  public SymbolExtDataTypes getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((arrayName == null) ? 0 : arrayName.hashCode());
    result =
        prime * result + ((arrayValue == null) ? 0 : arrayValue.hashCode());
    result =
        prime * result + ((grammValue == null) ? 0 : grammValue.hashCode());
    result = prime * result + ((guValue == null) ? 0 : guValue.hashCode());
    result =
        prime * result + ((kwTypeValue == null) ? 0 : kwTypeValue.hashCode());
    result =
        prime * result + ((kwSetValue == null) ? 0 : kwSetValue.hashCode());
    result = prime * result + ((labelName == null) ? 0 : labelName.hashCode());
    result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
    result =
        prime * result + ((paramValue == null) ? 0 : paramValue.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public String toString() {
    if (type.getIndex() >= 0 && type.getIndex() < 100)
      return labelName;

    if (type.getIndex() >= 100 && type.getIndex() < 200)
      return paramName + "=\"" + paramValue + "\"";

    if (type.getIndex() >= 200 && type.getIndex() < 300)
      return arrayName + "=[" + arrayValue + "]";

    return type.getIndex() + "";
  }

}
