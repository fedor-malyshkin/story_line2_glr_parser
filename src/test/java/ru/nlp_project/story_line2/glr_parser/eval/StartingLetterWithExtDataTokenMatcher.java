package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;

public class StartingLetterWithExtDataTokenMatcher implements IGLRTokenMatcher {

  @Override
  public Collection<ActionRecord> getActionTableRecords(
      Map<Symbol, List<ActionRecord>> actionTableEntry,
      Map<Symbol, List<Symbol>> baseNTToNTExtDataMap, Token token) {
    if (token.getType() == TokenTypes.EOI) {
      Symbol symbol = Symbol.EOI;
      return actionTableEntry.get(symbol);
    }

    List<ActionRecord> result = new ArrayList<ActionRecord>();

    actionTableEntry.entrySet().stream()
        .filter((e) -> e.getKey().getValue().startsWith(token.getValue()))
        .map((e) -> e.getValue()).forEach((l) -> result.addAll(l));

    return result;
  }


}
