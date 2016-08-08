// Generated from src/main/resources/ru/nlp_project/story_line2/glr_parser/grammar_parser/NGLR.g4 by ANTLR 4.3

package ru.nlp_project.story_line2.glr_parser.grammar_parser;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NGLRParser}.
 */
public interface NGLRListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NGLRParser#literalStringExt}.
	 * @param ctx the parse tree
	 */
	void enterLiteralStringExt(@NotNull NGLRParser.LiteralStringExtContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#literalStringExt}.
	 * @param ctx the parse tree
	 */
	void exitLiteralStringExt(@NotNull NGLRParser.LiteralStringExtContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#regularGroupExpression}.
	 * @param ctx the parse tree
	 */
	void enterRegularGroupExpression(@NotNull NGLRParser.RegularGroupExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#regularGroupExpression}.
	 * @param ctx the parse tree
	 */
	void exitRegularGroupExpression(@NotNull NGLRParser.RegularGroupExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveGrammarKWSet}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammarDirectiveGrammarKWSet(@NotNull NGLRParser.NglrGrammarDirectiveGrammarKWSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveGrammarKWSet}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammarDirectiveGrammarKWSet(@NotNull NGLRParser.NglrGrammarDirectiveGrammarKWSetContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nonTerminalExt}.
	 * @param ctx the parse tree
	 */
	void enterNonTerminalExt(@NotNull NGLRParser.NonTerminalExtContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nonTerminalExt}.
	 * @param ctx the parse tree
	 */
	void exitNonTerminalExt(@NotNull NGLRParser.NonTerminalExtContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#literalString}.
	 * @param ctx the parse tree
	 */
	void enterLiteralString(@NotNull NGLRParser.LiteralStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#literalString}.
	 * @param ctx the parse tree
	 */
	void exitLiteralString(@NotNull NGLRParser.LiteralStringContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#regularExpression}.
	 * @param ctx the parse tree
	 */
	void enterRegularExpression(@NotNull NGLRParser.RegularExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#regularExpression}.
	 * @param ctx the parse tree
	 */
	void exitRegularExpression(@NotNull NGLRParser.RegularExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveFilter}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammarDirectiveFilter(@NotNull NGLRParser.NglrGrammarDirectiveFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveFilter}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammarDirectiveFilter(@NotNull NGLRParser.NglrGrammarDirectiveFilterContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammar}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammar(@NotNull NGLRParser.NglrGrammarContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammar}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammar(@NotNull NGLRParser.NglrGrammarContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveNoInterpretation}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammarDirectiveNoInterpretation(@NotNull NGLRParser.NglrGrammarDirectiveNoInterpretationContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveNoInterpretation}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammarDirectiveNoInterpretation(@NotNull NGLRParser.NglrGrammarDirectiveNoInterpretationContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataLabel}.
	 * @param ctx the parse tree
	 */
	void enterExtDataLabel(@NotNull NGLRParser.ExtDataLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataLabel}.
	 * @param ctx the parse tree
	 */
	void exitExtDataLabel(@NotNull NGLRParser.ExtDataLabelContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammarDirective}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammarDirective(@NotNull NGLRParser.NglrGrammarDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammarDirective}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammarDirective(@NotNull NGLRParser.NglrGrammarDirectiveContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataParamName}.
	 * @param ctx the parse tree
	 */
	void enterExtDataParamName(@NotNull NGLRParser.ExtDataParamNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataParamName}.
	 * @param ctx the parse tree
	 */
	void exitExtDataParamName(@NotNull NGLRParser.ExtDataParamNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveGrammarRoot}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammarDirectiveGrammarRoot(@NotNull NGLRParser.NglrGrammarDirectiveGrammarRootContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveGrammarRoot}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammarDirectiveGrammarRoot(@NotNull NGLRParser.NglrGrammarDirectiveGrammarRootContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataLabelName}.
	 * @param ctx the parse tree
	 */
	void enterExtDataLabelName(@NotNull NGLRParser.ExtDataLabelNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataLabelName}.
	 * @param ctx the parse tree
	 */
	void exitExtDataLabelName(@NotNull NGLRParser.ExtDataLabelNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#projection}.
	 * @param ctx the parse tree
	 */
	void enterProjection(@NotNull NGLRParser.ProjectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#projection}.
	 * @param ctx the parse tree
	 */
	void exitProjection(@NotNull NGLRParser.ProjectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataParam}.
	 * @param ctx the parse tree
	 */
	void enterExtDataParam(@NotNull NGLRParser.ExtDataParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataParam}.
	 * @param ctx the parse tree
	 */
	void exitExtDataParam(@NotNull NGLRParser.ExtDataParamContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataArray}.
	 * @param ctx the parse tree
	 */
	void enterExtDataArray(@NotNull NGLRParser.ExtDataArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataArray}.
	 * @param ctx the parse tree
	 */
	void exitExtDataArray(@NotNull NGLRParser.ExtDataArrayContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nonTerminal}.
	 * @param ctx the parse tree
	 */
	void enterNonTerminal(@NotNull NGLRParser.NonTerminalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nonTerminal}.
	 * @param ctx the parse tree
	 */
	void exitNonTerminal(@NotNull NGLRParser.NonTerminalContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataArrayName}.
	 * @param ctx the parse tree
	 */
	void enterExtDataArrayName(@NotNull NGLRParser.ExtDataArrayNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataArrayName}.
	 * @param ctx the parse tree
	 */
	void exitExtDataArrayName(@NotNull NGLRParser.ExtDataArrayNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveInclude}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammarDirectiveInclude(@NotNull NGLRParser.NglrGrammarDirectiveIncludeContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveInclude}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammarDirectiveInclude(@NotNull NGLRParser.NglrGrammarDirectiveIncludeContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#interpDataEntry}.
	 * @param ctx the parse tree
	 */
	void enterInterpDataEntry(@NotNull NGLRParser.InterpDataEntryContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#interpDataEntry}.
	 * @param ctx the parse tree
	 */
	void exitInterpDataEntry(@NotNull NGLRParser.InterpDataEntryContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nonRegularExpression}.
	 * @param ctx the parse tree
	 */
	void enterNonRegularExpression(@NotNull NGLRParser.NonRegularExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nonRegularExpression}.
	 * @param ctx the parse tree
	 */
	void exitNonRegularExpression(@NotNull NGLRParser.NonRegularExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#interpData}.
	 * @param ctx the parse tree
	 */
	void enterInterpData(@NotNull NGLRParser.InterpDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#interpData}.
	 * @param ctx the parse tree
	 */
	void exitInterpData(@NotNull NGLRParser.InterpDataContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveGrammarKWSetValues}.
	 * @param ctx the parse tree
	 */
	void enterNglrGrammarDirectiveGrammarKWSetValues(@NotNull NGLRParser.NglrGrammarDirectiveGrammarKWSetValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#nglrGrammarDirectiveGrammarKWSetValues}.
	 * @param ctx the parse tree
	 */
	void exitNglrGrammarDirectiveGrammarKWSetValues(@NotNull NGLRParser.NglrGrammarDirectiveGrammarKWSetValuesContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataArrayValue}.
	 * @param ctx the parse tree
	 */
	void enterExtDataArrayValue(@NotNull NGLRParser.ExtDataArrayValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataArrayValue}.
	 * @param ctx the parse tree
	 */
	void exitExtDataArrayValue(@NotNull NGLRParser.ExtDataArrayValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#regQuantifier}.
	 * @param ctx the parse tree
	 */
	void enterRegQuantifier(@NotNull NGLRParser.RegQuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#regQuantifier}.
	 * @param ctx the parse tree
	 */
	void exitRegQuantifier(@NotNull NGLRParser.RegQuantifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extData}.
	 * @param ctx the parse tree
	 */
	void enterExtData(@NotNull NGLRParser.ExtDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extData}.
	 * @param ctx the parse tree
	 */
	void exitExtData(@NotNull NGLRParser.ExtDataContext ctx);

	/**
	 * Enter a parse tree produced by {@link NGLRParser#extDataParamValue}.
	 * @param ctx the parse tree
	 */
	void enterExtDataParamValue(@NotNull NGLRParser.ExtDataParamValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link NGLRParser#extDataParamValue}.
	 * @param ctx the parse tree
	 */
	void exitExtDataParamValue(@NotNull NGLRParser.ExtDataParamValueContext ctx);
}