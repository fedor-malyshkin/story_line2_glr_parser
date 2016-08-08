package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.List;

/**
 * LR(0)-пункт (см.  Aho, Sethi, Ullman "Compilers: Principles, Techniques, and Tools")
 * или "Item: (см. Dick Grune "Parsing Techniques: A Practical Guide") 
 * 
 * @author fedor
 *
 */
class LR0Point implements Comparable<LR0Point> {
  Grammar grammar = null;
  int projPos = 0;
  int pos = 0;

  public LR0Point(Grammar grammar, int projPos, int pos) {
    this.grammar = grammar;
    this.projPos = projPos;
    this.pos = pos;
  }

  @Override
  public String toString() {
    Projection proj = grammar.get(projPos);
    return String.format("%s->%s", proj.head, serializeList(proj.body));
  }

  public String serializeList(List<Symbol> list) {
    String res = "";
    int i ;
    for ( i = 0; i < list.size(); i++) {
      if (i == pos)
        res += "*";
      Symbol s = list.get(i);
      res += s.getValue();
    }
    if (i == pos)
      res += "*";
    return res;
  }

  @Override
  public int compareTo(LR0Point o) {
    if (o.projPos == projPos)
      return o.pos - pos;
    return o.projPos - projPos;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + pos;
    result = prime * result + projPos;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LR0Point other = (LR0Point) obj;
    if (pos != other.pos)
      return false;
    if (projPos != other.projPos)
      return false;
    return true;
  }

}
