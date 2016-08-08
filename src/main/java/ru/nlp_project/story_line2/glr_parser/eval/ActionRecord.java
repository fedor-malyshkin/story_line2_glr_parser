package ru.nlp_project.story_line2.glr_parser.eval;

public class ActionRecord {
  public static ActionRecord makeAcceptActionRecord() {
    ActionRecord result = new ActionRecord();
    result.accept = true;
    return result;
  }

  public static ActionRecord makeErrorActionRecord() {
    ActionRecord result = new ActionRecord();
    result.accept = false;
    return result;
  }

  public static ActionRecord makeReduceActionRecord(int projectionPos) {
    ActionRecord result = new ActionRecord();
    result.reduce = true;
    result.reduceProjection = projectionPos;
    return result;
  }

  /**
  Make RN action table reduction record.
  For details see paper Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006.
   */
  public static ActionRecord makeRN2ReduceActionRecord(Symbol symbol,
      int reductionSymbolCount, int indexFunc, int projPos) {
    ActionRecord result = new ActionRecord();
    result.rn2Reduce = true;
    result.rnReductionSymbol = symbol;
    result.rnReductionSymbolCount = reductionSymbolCount;
    result.rn2ReductionIndexFunc = indexFunc;
    result.rn2ProjectionPos = projPos;
    return result;
  }

  /**
   * Make RN action table reduction record.
   * For details see paper Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006.
   * @param projectionPos
   * @return
   */
  public static ActionRecord makeRNReduceActionRecord(Symbol symbol,
      int reductionSymbolCount) {
    ActionRecord result = new ActionRecord();
    result.rnReduce = true;
    result.rnReductionSymbol = symbol;
    result.rnReductionSymbolCount = reductionSymbolCount;
    return result;
  }

  public static ActionRecord makeShiftActionRecord(int state) {
    ActionRecord result = new ActionRecord();
    result.shift = true;
    result.shiftState = state;
    return result;
  }
 
  boolean accept = false;
  boolean reduce = false;
  int reduceProjection;
  int rn2ProjectionPos;
  boolean rn2Reduce = false;
  int rn2ReductionIndexFunc;
  boolean rnReduce = false;
  Symbol rnReductionSymbol;
  int rnReductionSymbolCount;
  boolean shift = false;

  int shiftState;

  @Override
  public String toString() {
    if (shift)
      return "s" + shiftState;
    if (reduce && accept)
      return "r" + reduceProjection + "/acc";
    if (reduce)
      return "r" + reduceProjection;
    if (rnReduce && accept)
      return "r(" + rnReductionSymbol + ", " + rnReductionSymbolCount + ")/acc";
    if (rnReduce)
      return "r(" + rnReductionSymbol + ", " + rnReductionSymbolCount + ")";
    if (rn2Reduce && accept)
      return "r(" + rnReductionSymbol + ", " + rnReductionSymbolCount + ", "
          + rn2ReductionIndexFunc + "(" + rn2ProjectionPos + ")" + ")/acc";
    if (rn2Reduce)
      return "r(" + rnReductionSymbol + ", " + rnReductionSymbolCount + ", "
          + rn2ReductionIndexFunc + "(" + rn2ProjectionPos + "))";
    if (accept)
      return "acc";
    else
      return "err";
  }

}