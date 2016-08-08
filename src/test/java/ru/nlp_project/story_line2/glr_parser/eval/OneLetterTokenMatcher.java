package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class OneLetterTokenMatcher implements IGLRTokenMatcher {

  /* (non-Javadoc)
   * @see ru.nlp_project.story_line2.glr_parser.ITokenMatcher#getActionTableRecords(java.util.Map, ru.nlp_project.story_line2.glr_parser.Token)
   */
  @Override
  public Collection<ActionRecord> getActionTableRecords(
      Map<Symbol, List<ActionRecord>> actionTableEntry,
      Map<Symbol, List<Symbol>> baseNTToNTExtDataMap, Token token) {

    Symbol symbol = new Symbol(token.getValue(), SymbolTypes.Terminal);
    
    if (token.getType() == TokenTypes.EOI)
      symbol = Symbol.EOI;
    
    return actionTableEntry.get(symbol);
  }

}
