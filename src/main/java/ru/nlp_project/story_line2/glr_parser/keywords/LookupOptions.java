package ru.nlp_project.story_line2.glr_parser.keywords;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManagerImpl.OptionAgrConverter;
import ru.nlp_project.story_line2.glr_parser.keywords.KeywordManagerImpl.OptionGrammConverter;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.GrammemeUtils.GrammemeEnum;
import ru.nlp_project.story_line2.morph.Grammemes;

@Parameters()
class LookupOptions {
  /**
   * Помета описывает согласование между двумя словами в ключе. Возможны два типа согласования:
  * по роду, числу и падежу (agr=gnc_agr);
  * по падежу (agr=c_agr).
   */
  @Parameter(names = "-agr", converter = OptionAgrConverter.class)
  SymbolExtDataTypes agr = null;
  @Parameter(names = "-exact_form")
  boolean exactForm = false;

  /**
   * Значения этого поля — лемма, которая заменяет обнаруженное слово или группу слов. 
   * Например, "-lemm="Россия"" заменяет лемму группы "российская федерация" на единственную лемму - "Россия". 
   */
  @Parameter(names = "-lemm")
  String lemm;
  /**
   * Значения этого поля — граммемы, которые применяются к ключу. 
   * Например, "-gram="sg"" означает, что статье соответствуют только формы ключа в единственном числе. 
   */
  @Parameter(names = "-gramm", converter = OptionGrammConverter.class)
  Set<GrammemeEnum> gramm;
  /**
   * R какому из слов многословного ключа применяется помета gram. 
   * "-gramm-1="re,rewrwer,dsada""
   */
  Map<Integer, Set<GrammemeEnum>> grammTree;
  @DynamicParameter(names = "-gramm-", assignment = "=")
  Map<String, String> grammTreeRaw = new HashMap<>();

  @Parameter(names = "-main_word")
  int mainWord = 0;
  @Parameter(names = "-upper_case")
  boolean upperCase = false;

  void convertGrammTree() {
    grammTree = new HashMap<>();
    for (Map.Entry<String, String> entr : grammTreeRaw.entrySet()) {
      // remove '"' at beginning
      String val = StringUtils.removeStart(entr.getValue(), "\"");
      // remove '"' at end
      val = StringUtils.removeEnd(val, "\"");

      Grammemes gramm = new Grammemes();
      GrammemeUtils.fillGrammemesByCSVMyTags(val, gramm, true);
      grammTree.put(Integer.parseInt(entr.getKey()),
          EnumSet.copyOf(gramm.getMainGrammems()));
    }
  }
}