package ru.nlp_project.story_line2.glr_parser.keywords;

import java.util.Collections;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordTrieBuilder.KeywordInfo;

public class PlainKeywordTrieReader {

  private PlainKeywordTrie trie;

  public PlainKeywordTrieReader(PlainKeywordTrie trie) {
    this.trie = trie;
  }

  // выполнить поиск в дереве основ
  PlainKeywordTrieNode searchTrieNodeBase(PlainKeywordTrieNode h, String key,
      int key_order) {
    // если дошли до недостроенного дерева...
    if (null == h) {
      return null;
    }

    // если находимся во внутреннем узле...
    if (h.internal()) {
      char search_kov = key.length() > key_order ? key.charAt(key_order) : '\0';
      char existing_kov = h.kov;
      if (search_kov < existing_kov) {
        return searchTrieNodeBase(h.l, key, key_order);
      }
      if (search_kov == existing_kov) {
        return searchTrieNodeBase(h.m, key, key_order + 1);
      }
      if (search_kov > existing_kov) {
        return searchTrieNodeBase(h.r, key, key_order);
      }
    } else {
      // если пришли во внешний узел...
      // сравниваем по полному совпадению...
      if (h.key == key) {
        return h;
      }
      // рассматриваем вариант совпадения основ
      if (key.equalsIgnoreCase(h.key)) {
        return h;
      }
      // во всех остальных случаях - ничего нет
      return null;
    }
    return null;
  }

  // выполнить анализ словоформы
  public List<KeywordInfo> analyse(String wordform) {
    PlainKeywordTrieNode baseNode = searchTrieNodeBase(trie.rootKeywordTrieNode,
        wordform.toLowerCase(), 0);

    if (null != baseNode)
      return Collections.unmodifiableList(baseNode.keywordInfos);

    return Collections.emptyList();
  }

}
