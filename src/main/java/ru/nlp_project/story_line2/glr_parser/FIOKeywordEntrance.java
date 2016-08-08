package ru.nlp_project.story_line2.glr_parser;

import java.util.LinkedList;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.NameFinder.FIOEntry;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;

public class FIOKeywordEntrance implements IKeywordEntrance {

  private int from;
  private int length;
  private List<FIOEntry> fios = new LinkedList<>();

  public FIOKeywordEntrance(int from, int length) {
    this.from = from;
    this.length = length;
  }

  public void addFIOs(FIOEntry entry) {
    fios.add(entry);
  }

  public List<FIOEntry> getFIOs() {
    return fios;
  }

  @Override
  public int getFrom() {
    return from;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public String toString() {
    return String.format("FIOKeywordEntrance [from=%s, length=%s, fios=%s]",
        from, length, fios);
  }

}
