package ru.nlp_project.story_line2.glr_parser;

import ru.nlp_project.story_line2.glr_parser.InterpreterImpl.Fact;

public interface IFactListener {

  default void factExtracted(SentenceProcessingContext context, Fact fact) {
  }
}
