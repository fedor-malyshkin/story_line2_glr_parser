package ru.nlp_project.story_line2.glr_parser.keywords;

import java.util.LinkedList;
import java.util.List;

import ru.nlp_project.story_line2.glr_parser.keywords.PlainKeywordTrieBuilder.KeywordInfo;

class PlainKeywordTrieNode {
  List<KeywordInfo> keywordInfos;
  boolean internal;
  String key;
  char kov;
  PlainKeywordTrieNode l;
  PlainKeywordTrieNode m;
  PlainKeywordTrieNode r;
  int keyLength;

  PlainKeywordTrieNode(String key, KeywordInfo keywordInfo, char kov) {
    m = l = r = null;
    this.internal = true;

    if (null != keywordInfo) {
      if (null == keywordInfos) {
        this.keywordInfos = new LinkedList<KeywordInfo>();
      }
      this.keywordInfos.add(keywordInfo);
    }

    this.kov = kov;

    if (null != key) {
      this.keyLength = key.length();
      this.key = new String(key);
    } else {
      this.key = null;
    }
  }

  PlainKeywordTrieNode(PlainKeywordTrieNode node, char kov) {
    m = l = r = null;
    this.internal = true;
    this.key = null;
    this.kov = kov;
    this.m = node;
  }

  boolean internal() {
    return internal;
  }

  private PlainKeywordTrieNode() {
  }

  protected PlainKeywordTrieNode clone() {
    PlainKeywordTrieNode result = new PlainKeywordTrieNode();
    result.keywordInfos = this.keywordInfos;
    result.internal = this.internal;
    result.key = this.key;
    result.kov = this.kov;
    result.l = this.l;
    result.m = this.m;
    result.r = this.r;
    result.keyLength = this.keyLength;
    return result;
  }

  void assign(PlainKeywordTrieNode other) {
    this.keywordInfos = other.keywordInfos;
    this.internal = other.internal;
    this.key = other.key;
    this.kov = other.kov;
    this.l = other.l;
    this.m = other.m;
    this.r = other.r;
    this.keyLength = other.keyLength;
  }

  @Override
  public String toString() {
    return "[internal=" + internal + ", key=" + key + ", keyLength="
        + keyLength + ", kov=" + kov + "]";
  }
}
