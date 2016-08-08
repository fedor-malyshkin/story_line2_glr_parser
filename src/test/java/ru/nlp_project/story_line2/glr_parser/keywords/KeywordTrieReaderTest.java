package ru.nlp_project.story_line2.glr_parser.keywords;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordTrieBuilder.KeywordInfo;

public class KeywordTrieReaderTest {

  private PlainKeywordTrieBuilder builder;

  @Before
  public void setUp() throws Exception {
    builder = new PlainKeywordTrieBuilder(0, "");
  }

  @Test
  public void testSimpleSearch() {
    builder.addWord("делать",
        builder.createSinglewordKewordInfo(0, new LookupOptions()));
    builder.addWord("богослов",
        builder.createSinglewordKewordInfo(1, new LookupOptions()));
    builder.addWord("слово",
        builder.createSinglewordKewordInfo(2, new LookupOptions()));
    builder.addWord("бугислов",
        builder.createSinglewordKewordInfo(4, new LookupOptions()));
    builder.addWord("бугислов",
        builder.createSinglewordKewordInfo(4, new LookupOptions()));

    PlainKeywordTrieReader trieExplorer = new PlainKeywordTrieReader(
        builder.getTrie());
    List<KeywordInfo> results = trieExplorer.analyse("бугислов");

    assertEquals(2, results.size());
    assertEquals(4, results.get(0).keywordPos);
    assertEquals(4, results.get(1).keywordPos);
  }

  @Test
  public void testSimpleSearchNoMatch() {

    builder.addWord("делать",
        builder.createSinglewordKewordInfo(0, new LookupOptions()));
    builder.addWord("богослов",
        builder.createSinglewordKewordInfo(1, new LookupOptions()));
    builder.addWord("слово",
        builder.createSinglewordKewordInfo(2, new LookupOptions()));
    builder.addWord("бугислов",
        builder.createSinglewordKewordInfo(4, new LookupOptions()));

    PlainKeywordTrieReader trieExplorer = new PlainKeywordTrieReader(
        builder.getTrie());
    List<KeywordInfo> results = trieExplorer.analyse("бугислов2");
    assertEquals(0, results.size());

    results = trieExplorer.analyse("буг");
    assertEquals(0, results.size());

  }

}
