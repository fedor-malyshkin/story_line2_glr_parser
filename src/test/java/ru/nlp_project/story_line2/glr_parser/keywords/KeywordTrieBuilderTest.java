package ru.nlp_project.story_line2.glr_parser.keywords;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class KeywordTrieBuilderTest {

  private PlainKeywordTrieBuilder testable;

  @Before
  public void setUp() throws Exception {
    testable = new PlainKeywordTrieBuilder(0, "");
  }

  /*
  Проверка построения дерева при добавлении одного слова.
  */
  @Test
  public void testAddWordformBase() {
    testable.addWord("слово", testable.newKeywordInfo());
    PlainKeywordTrieNode root_node = testable.getTrie().rootKeywordTrieNode;
    assertNotNull(root_node);
    assertEquals('с', root_node.kov);
    assertEquals("слово", root_node.m.key);
    assertEquals(1, root_node.m.keywordInfos.size());
    assertFalse(root_node.m.internal());
  }

  @Test
  public void testAddSameWord() {
    testable.addWord("слово", testable.newKeywordInfo());
    testable.addWord("слово", testable.newKeywordInfo());
    PlainKeywordTrieNode root_node = testable.getTrie().rootKeywordTrieNode;
    assertNotNull(root_node);
    assertEquals("слово", root_node.m.key);
    assertEquals(2, root_node.m.keywordInfos.size());
    // assertEquals("слово", root_node.m.keywordInfos.get(0));
  }

  /*
  Проверка построения дерева при добавлении двух слов с общими окончаниями.
  */
  @Test
  public void test_add_wordform_base_w_split() {
    testable.addWord("слово", testable.newKeywordInfo());
    testable.addWord("словоблуд", testable.newKeywordInfo());
    PlainKeywordTrieNode root_node = testable.getTrie().rootKeywordTrieNode;
    assertNotNull(root_node);
    assertEquals("словоблуд", root_node.m.m.m.m.m.r.m.key);
  }

  /*
    Проверка построения дерева при добавлении двух слов с общими окончаниями, но
    в обратном порядке - сначала длинное слово, потом короткое.
  */
  @Test
  public void test_add_wordform_base_w_split_long_word_first() {
    testable.addWord("словоблуд", testable.newKeywordInfo());
    testable.addWord("слово", testable.newKeywordInfo());
    testable.addWord("словоблуд", testable.newKeywordInfo());
    PlainKeywordTrieNode root_node = testable.getTrie().rootKeywordTrieNode;
    assertNotNull(root_node);
    assertEquals("слово", root_node.m.m.m.m.m.l.m.key);
    assertEquals("словоблуд", root_node.m.m.m.m.m.m.key);
    assertEquals(2, root_node.m.m.m.m.m.m.keywordInfos.size());
  }
}
