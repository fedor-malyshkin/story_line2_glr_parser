package ru.nlp_project.story_line2.glr_parser.keywords;

import java.util.List;

/**
 * 
 * MULTITHREAD_SAFE: FALSE
 * @author fedor
 *
 */
class PlainKeywordTrieBuilder {

  class KeywordInfo {
    /**
     * Имеется продолжение (для многословных ключей)?
     */
    boolean hasContinue;
    /**
     * Идентификатор ключе в наборе.
     */
    int keywordPos;
    /**
     * Идентификатор набора ключей (статья)
     */
    int keywordSet;
    /**
     * Многословный ключ? 
     */
    boolean multiword;
    /**
     * Номер части многословного ключа (для многословных ключей)
     */
    int multiwordPart;
    LookupOptions options;

    public KeywordInfo(int keywordSet, int kewordPos, boolean multiword,
        boolean hasContinue, int multiwordPart, LookupOptions options) {
      super();
      this.keywordSet = keywordSet;
      this.keywordPos = kewordPos;
      this.multiword = multiword;
      this.hasContinue = hasContinue;
      this.multiwordPart = multiwordPart;
      this.options = options;
    }

    @Override
    public String toString() {
      return "KeywordInfo [keywordSet=" + keywordSet + ", keywordPos="
          + keywordPos + ", multiword=" + multiword + ", multiwordPart="
          + multiwordPart + ", hasContinue=" + hasContinue + ", lookupOptions="
          + options + "]";
    }

  }

  private int keywordSetId;
  private String keywordSetName;
  private PlainKeywordTrie trie;

  public PlainKeywordTrieBuilder(int keywordSetId, String keywordSetName) {
    super();
    this.keywordSetId = keywordSetId;
    this.keywordSetName = keywordSetName;
    this.trie = new PlainKeywordTrie();
  }

  // добавить очередную основу словоформы
  void addWord(String word, KeywordInfo kwi) {
    if (word.startsWith("!")) {
      word = word.substring(1, word.length());
      kwi.options.exactForm = true;
    }

    PlainKeywordTrieNode root_node = trie.rootKeywordTrieNode;
    if (null == root_node) {
      root_node = createExternalTrieNodeBaseContainer(kwi, word.toLowerCase(),
          0);
      trie.rootKeywordTrieNode = root_node;
    } else {
      insertTrieNodeBase(root_node, kwi, word.toLowerCase(), 0);
    }
  }

  PlainKeywordTrieNode createExternalTrieNodeBaseContainer(KeywordInfo kwi,
      String key, int key_order) {
    char i = key.length() > key_order ? key.charAt(key_order) : '\0';
    PlainKeywordTrieNode result = new PlainKeywordTrieNode(
        newExternalTrieNodeBase(key, kwi), i);
    return result;
  }

  PlainKeywordTrie getTrie() {
    return trie;
  }

  // добавить новую запись в дерево основ
  void insertTrieNodeBase(PlainKeywordTrieNode h, KeywordInfo kwi, String key,
      int key_order) {
    if (!h.internal()) {
      PlainKeywordTrieNode other_h = h.clone();
      PlainKeywordTrieNode temp = splitExistingTrieNodeBase(
          newExternalTrieNodeBase(key, kwi), other_h, key_order);
      h.assign(temp);
      return;
    }

    char ui = key.length() > key_order ? key.charAt(key_order) : '\0';
    char h_kov = h.kov;

    if (ui < h_kov) {
      // printf("i < h->kov_\n");
      if (null == h.l)
        h.l = createExternalTrieNodeBaseContainer(kwi, key, key_order);
      else
        insertTrieNodeBase(h.l, kwi, key, key_order);
      return;
    }
    if (ui == h_kov) {
      // printf("i == h->kov_\n");
      if (null == h.m)
        h.m = createExternalTrieNodeBaseContainer(kwi, key, key_order + 1);
      else
        insertTrieNodeBase(h.m, kwi, key, key_order + 1);
      return;
    }
    if (ui > h_kov) {
      // printf("i > h->kov_\n");
      if (null == h.r)
        h.r = createExternalTrieNodeBaseContainer(kwi, key, key_order);
      else
        insertTrieNodeBase(h.r, kwi, key, key_order);
      return;
    }

    throw new IllegalStateException();
  }

  // создать новый "внешний" узел в дереве основ
  PlainKeywordTrieNode newExternalTrieNodeBase(String base, KeywordInfo kwi) {
    PlainKeywordTrieNode result = new PlainKeywordTrieNode(base, kwi, '\0');
    result.internal = false;
    return result;
  }

  public KeywordInfo newKeywordInfo() {
    return new KeywordInfo(0, 0, false, false, 0, new LookupOptions());
  }

  // base tree
  // разделить существующую запись в дереве основ
  PlainKeywordTrieNode splitExistingTrieNodeBase(PlainKeywordTrieNode new_node,
      PlainKeywordTrieNode existing_node, int key_order) {
    // в случае если основы слов в узлах дерева равны - в существующий,
    // добавить информацию о словоформе
    if (new_node.key.equals(existing_node.key)) {
      List<KeywordInfo> wfs = new_node.keywordInfos;
      existing_node.keywordInfos.addAll(wfs);
      return existing_node;
    }

    char new_kov = new_node.keyLength > key_order ? new_node.key
        .charAt(key_order) : '\0';
    char existing_kov = existing_node.keyLength > key_order ? existing_node.key
        .charAt(key_order) : '\0';

    PlainKeywordTrieNode t = new PlainKeywordTrieNode(null, existing_kov);
    if (new_kov < existing_kov) {
      t.m = existing_node;
      t.l = new PlainKeywordTrieNode(new_node, new_kov);
    }
    if (new_kov == existing_kov) {
      t.m = splitExistingTrieNodeBase(new_node, existing_node, key_order + 1);
    }
    if (new_kov > existing_kov) {
      t.m = existing_node;
      t.r = new PlainKeywordTrieNode(new_node, new_kov);
    }
    return t;

  }

  protected KeywordInfo createSinglewordKewordInfo(int entryPos,
      LookupOptions options) {
    return new KeywordInfo(keywordSetId, entryPos, false, false, 0, options);
  }

  public KeywordInfo createMultiwordKewordInfo(int entryPos,
      boolean hasContinue, int multiwordPart, LookupOptions options) {
    return new KeywordInfo(keywordSetId, entryPos, true, hasContinue,
        multiwordPart, options);
  }

}
