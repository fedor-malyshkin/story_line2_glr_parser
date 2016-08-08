package ru.nlp_project.story_line2.glr_parser.eval;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.map.LazyMap;
import org.junit.Before;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.HierarchyManager;
import ru.nlp_project.story_line2.glr_parser.SymbolExt;
import ru.nlp_project.story_line2.glr_parser.SymbolExtData;
import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.GrammemeUtils.GrammemeEnum;
import ru.nlp_project.story_line2.morph.Grammemes;

public class FullMorphTokenMatcherTest {

  private FullMorphTokenMatcher testable;
  private Map<Symbol, List<Symbol>> baseNTToNTExtDataMap;
  private Map<Symbol, List<ActionRecord>> actionTableEntry;

  @Before
  public void setUp() throws Exception {
    Factory<List<Symbol>> factoryListSymb = new Factory<List<Symbol>>() {
      @Override
      public List<Symbol> create() {
        return new ArrayList<Symbol>();
      }
    };

    actionTableEntry = new HashMap<Symbol, List<ActionRecord>>();
    baseNTToNTExtDataMap =
        LazyMap.lazyMap(new HashMap<Symbol, List<Symbol>>(), factoryListSymb);
    testable = new FullMorphTokenMatcher(HierarchyManager.newInstance(null));
  }

  /**
   * Проверка определения комбинации граммем.
   * Проверка: 
   * gramm=[accs, plur], rt, h-reg1
   * Результат:
   * - someVal (noun, accs)
   * 
   */
  @Test
  public void testGetActionTableRecords_ForSimpleCase() {
    // simple symbol
    Symbol symbol = new Symbol("val", SymbolTypes.Terminal);

    // symbol ext
    SymbolExtData symbolExtData1 = SymbolExtData.makeLabelExtData("rt");
    SymbolExtData symbolExtData2 =
        SymbolExtData.makeParamExtData("gram", "accs, plur");
    SymbolExtData symbolExtData3 = SymbolExtData.makeLabelExtData("h-reg1");
    List<SymbolExtData> symbolExtDatas =
        Arrays.asList(symbolExtData1, symbolExtData2, symbolExtData3);
    SymbolExt symbolEx =
        new SymbolExt("val", SymbolTypes.Terminal, symbolExtDatas);

    actionTableEntry.put(symbolEx, Arrays.asList(new ActionRecord()));
    baseNTToNTExtDataMap.put(symbol, Arrays.asList(symbolEx));

    Token token = new Token(0, 10, "someVal", TokenTypes.WORD);
    Grammemes grms = new Grammemes();
    GrammemeUtils.setTag(GrammemeEnum.noun, grms);
    GrammemeUtils.setTag(GrammemeEnum.accs, grms);

    token.addLexeme("", "lemm", "base", grms, true);
    Collection<ActionRecord> collection = testable
        .getActionTableRecords(actionTableEntry, baseNTToNTExtDataMap, token);
    assertTrue(collection.size() == 0);
  }

  
  @Test
  public void testGetActionTableRecords_LiteralString() {
    // simple symbol
    Symbol symbol = new Symbol("lemm", SymbolTypes.LiteralString);

    // symbol ext
    SymbolExt symbolEx =
        new SymbolExt("lemm", SymbolTypes.LiteralString, Collections.emptyList());

    actionTableEntry.put(symbolEx, Arrays.asList(new ActionRecord()));
    baseNTToNTExtDataMap.put(symbol, Arrays.asList(symbolEx));

    Token token = new Token(0, 10, "val", TokenTypes.WORD);
    Grammemes grms = new Grammemes();
    GrammemeUtils.setTag(GrammemeEnum.noun, grms);
    GrammemeUtils.setTag(GrammemeEnum.accs, grms);
    token.addLexeme("", "lemm", "base", grms, true);
    Collection<ActionRecord> collection = testable
        .getActionTableRecords(actionTableEntry, baseNTToNTExtDataMap, token);
    assertTrue(collection.size() > 0);
  }
  
}
