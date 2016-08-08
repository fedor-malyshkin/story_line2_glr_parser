package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Проекция грамматики.
 * Запись формы: A->BcD, где B, c и D - символы (терминалы или нетерминалы)
 * 
 * @author fedor
 *
 */
public class Projection {
  public void setHead(Symbol head) {
    this.head = head;
  }

  public Symbol getHead() {
    return head;
  }

  public List<Symbol> getBody() {
    return body;
  }

  Symbol head = null;
  List<Symbol> body = null;

  public Projection(Symbol head2, List<? extends Symbol> body2) {
    super();
    this.head = head2;
    this.body = new ArrayList<Symbol>(body2);
  }

  @Override
  public String toString() {
    return String.format("%s->%s;", head == null ? null : head.toString(),
        serializeBody());
  }

  private String serializeBody() {
    return StringUtils.join(body, " ");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((head == null) ? 0 : head.hashCode());
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
    Projection other = (Projection) obj;
    if (body == null) {
      if (other.body != null)
        return false;
    } else if (!body.equals(other.body))
      return false;
    if (head == null) {
      if (other.head != null)
        return false;
    } else if (!head.equals(other.head))
      return false;
    return true;
  }

}
