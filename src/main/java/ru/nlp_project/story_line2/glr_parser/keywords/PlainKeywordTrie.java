package ru.nlp_project.story_line2.glr_parser.keywords;

class PlainKeywordTrie {

  PlainKeywordTrieNode rootKeywordTrieNode;
  private int keywordSetId;
  private String keywordSetName;

  public PlainKeywordTrie() {
  }

  public PlainKeywordTrie(int keywordSetId, String keywordSetName) {
    super();
    this.keywordSetId = keywordSetId;
    this.keywordSetName = keywordSetName;
  }

}
