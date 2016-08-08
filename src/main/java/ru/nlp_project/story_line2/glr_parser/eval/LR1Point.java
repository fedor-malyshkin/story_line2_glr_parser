package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * LR(1)-пункт (см.  Aho, Sethi, Ullman "Compilers: Principles, Techniques, and Tools")
 * или "Item: (см. Dick Grune "Parsing Techniques: A Practical Guide") 
 * 
 * @author fedor
 *
 */
public class LR1Point implements Comparable<LR1Point> {
  Grammar grammar = null;
  /**
   * Lookahead символ. 
   */
  Symbol las = null;
  int pos = 0;

  int projPos = 0;

  public LR1Point(Grammar grammar, int projPos, int pos, Symbol las) {
    this.grammar = grammar;
    this.projPos = projPos;
    this.pos = pos;
    this.las = las;
  }

  @Override
  public int compareTo(LR1Point o) {
    if (o.projPos != projPos)
      return o.projPos - projPos;
    if (o.pos != pos)
      return o.pos - pos;
    return o.las.compareTo(las);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LR1Point other = (LR1Point) obj;
    if (las == null) {
      if (other.las != null)
        return false;
    } else if (!las.equals(other.las))
      return false;
    if (pos != other.pos)
      return false;
    if (projPos != other.projPos)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((las == null) ? 0 : las.hashCode());
    result = prime * result + pos;
    result = prime * result + projPos;
    return result;
  }

  public LR0Point makeLR0Point() {
    return new LR0Point(grammar, this.projPos, this.pos);
  }

  public String serializeList(List<Symbol> list) {
    List<String> sts = new ArrayList<String>();
    int i;
    for (i = 0; i < list.size(); i++) {
      if (i == pos)
        sts.add("*");
      Symbol s = list.get(i);

      if (s.isEOI())
        sts.add("EOI");
      else if (s.isEpsilon())
        sts.add("EPSILON");
      else
        sts.add(s.toString());
    }
    if (i == pos)
      sts.add("*");
    return StringUtils.join(sts, " ");
  }

  @Override
  public String toString() {
    Projection proj = grammar.get(projPos);
    return String.format("%s->%s [%s]", proj.head, serializeList(proj.body),
        las == null ? null : las);
  }

}
