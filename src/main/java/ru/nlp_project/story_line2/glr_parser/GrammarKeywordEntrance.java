package ru.nlp_project.story_line2.glr_parser;

import ru.nlp_project.story_line2.glr_parser.Interpreter.InterpretationResult;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;

public class GrammarKeywordEntrance implements IKeywordEntrance {
  private int from;

  /**
   * Результаты интерпретации {@link #parseTreeNode}}.
   */
  private InterpretationResult interpretationResult;
  private String kwName;
  private int length;
  private ParseTreeNode parseTreeNode;

  public GrammarKeywordEntrance(int from, int length, String article,
      ParseTreeNode parseTreeNode) {
    super();
    this.from = from;
    this.length = length;
    this.kwName = article;
    this.parseTreeNode = parseTreeNode;
  }

  @Override
  public int getFrom() {
    return from;
  }

  public InterpretationResult getInterpretationResult() {
    return interpretationResult;
  }

  public String getKwName() {
    return kwName;
  }

  @Override
  public int getLength() {
    return length;
  }

  public ParseTreeNode getParseTreeNode() {
    return parseTreeNode;
  }


  public void setInterpretationResult(InterpretationResult interpretationResult) {
    this.interpretationResult = interpretationResult;
  }

  @Override
  public String toString() {
    return "<" + from + ";" + length + ";" + parseTreeNode + ";"
        + interpretationResult + ">";
  }

}
