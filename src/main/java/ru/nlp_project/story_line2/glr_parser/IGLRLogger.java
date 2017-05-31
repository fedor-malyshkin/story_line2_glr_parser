package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.nlp_project.story_line2.glr_parser.GLRParser.Sentence;
import ru.nlp_project.story_line2.glr_parser.GrammarManagerImpl.GrammarDirectiveTypes;
import ru.nlp_project.story_line2.glr_parser.ParseTreeValidator.ParseTreeValidationException;
import ru.nlp_project.story_line2.glr_parser.eval.Grammar;
import ru.nlp_project.story_line2.glr_parser.keywords.IKeywordEntrance;

public interface IGLRLogger {

	default void detectedOptimalKwEntrances(SentenceProcessingContext context,
			List<? extends IKeywordEntrance> optimalCoverage) {}

	default void detectedParseTree(SentenceProcessingContext context, ParseTreeNode parseTreeNode,
			ParseTreeNode userRootSymbolNode, boolean validated,
			ParseTreeValidationException exception) {}

	default void detectedUnoptimizedKwEntrances(SentenceProcessingContext context,
			List<? extends IKeywordEntrance> optimalCoverage) {}

	default void endArticleProcessing(SentenceProcessingContext context) {

	}

	default void endFactProcessing(SentenceProcessingContext context) {

	}

	default void endParseTreeProcessing(SentenceProcessingContext context,
			Collection<ParseTreeNode> trees) {}

	default void endSentenceProcessing(Sentence sentence, List<String> articles) {

	}

	default void error(String message, Exception e) {}

	default void grammarHasDirectives(SentenceProcessingContext context,
			Map<GrammarDirectiveTypes, Object> directivesMap) {}

	default void grammarHasGrammarKeywords(SentenceProcessingContext context,
			Collection<String> keywords) {}

	default void grammarHasPlainKeywords(SentenceProcessingContext context,
			Collection<String> plainKeywords) {}

	default void startArticleProcessing(SentenceProcessingContext context) {}

	default void startFactProcessing(SentenceProcessingContext context) {

	}

	default void startGrammarProcessing(SentenceProcessingContext context, Grammar grammar) {}

	default void startParseTreeProcessing(SentenceProcessingContext context,
			Collection<ParseTreeNode> trees) {}

	default void startSentenceProcessing(Sentence sentence, List<String> articles) {

	}

	default void tokenNamesGenerated(List<Token> tokens) {

	}

	default void tokensGenerated(List<Token> tokens) {

	}

	default void tokensModified(SentenceProcessingContext context, List<Token> tokens) {

	}

	default void tokensTaggerProcessed(List<Token> tokens) {}

}
