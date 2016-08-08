package ru.nlp_project.story_line2.glr_parser;

public class SymbolInterpData {
  public static String extracFactName(String factFull) {
    int pp = factFull.indexOf(".");
    return pp != -1 ? factFull.substring(0, pp) : null;
  }

  public static String extracFieldName(String factFull) {
    int pp = factFull.indexOf(".");
    return pp != -1 ? factFull.substring(pp + 1, factFull.length()) : null;
  }

  private String factName;
  private String fieldName;
  private String fromFactName;
  private String fromFieldName;
  private String value;
  private String param;

  public SymbolInterpData(String factName, String fieldName, String param) {
    super();
    this.factName = factName;
    this.fieldName = fieldName;
    this.param = param;
  }

  public SymbolInterpData(String factName, String fieldName, String value,
      String param) {
    super();
    this.factName = factName;
    this.fieldName = fieldName;
    this.value = value;
    this.param = param;
  }

  public SymbolInterpData(String factName, String fieldName,
      String fromFactName, String fromFieldName, String param) {
    super();
    this.factName = factName;
    this.fieldName = fieldName;
    this.fromFactName = fromFactName;
    this.fromFieldName = fromFieldName;
    this.param = param;
  }

  public String getFactName() {
    return factName;
  }

  public String getParam() {
    return param;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getFromFactName() {
    return fromFactName;
  }

  public String getFromFieldName() {
    return fromFieldName;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    if (param.isEmpty()) {
      if (fromFactName != null)
        return String.format("%s.%s=%s.%s", factName, fieldName, fromFactName,
            fromFieldName);
      else if (value != null)
        return String.format("%s.%s='%s'", factName, fieldName, value);
      else
        return String.format("%s.%s", factName, fieldName);
    } else {
      if (fromFactName != null)
        return String.format("%s.%s=%s.%s (%s)", factName, fieldName,
            fromFactName, fromFieldName, param);
      else if (value != null)
        return String.format("%s.%s='%s' (%s)", factName, fieldName, value,
            param);
      else
        return String.format("%s.%s (%s)", factName, fieldName, param);
    }
  }

}
