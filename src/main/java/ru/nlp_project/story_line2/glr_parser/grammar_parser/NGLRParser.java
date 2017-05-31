// Generated from src/main/resources/ru/nlp_project/story_line2/glr_parser/grammar_parser/NGLR.g4 by ANTLR 4.3

package ru.nlp_project.story_line2.glr_parser.grammar_parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NGLRParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__4=1, T__3=2, T__2=3, T__1=4, T__0=5, HASH=6, HYPHEN=7, UNDERSCORE=8, 
		QUESTION=9, STAR=10, DOUBLE_QUOTE=11, EQUAL=12, AMPERSAND=13, ARROW=14, 
		SEMICOLUMN=15, SINGLE_QUOTE=16, VERTICAL_BAR=17, PLUS=18, LEFT_CURVE_BRACKET=19, 
		RIGHT_CURVE_BRACKET=20, COMMA=21, POINT=22, LEFT_BRACKET=23, RIGHT_BRACKET=24, 
		LEFT_ANGLE_BRACKET=25, RIGHT_ANGLE_BRACKET=26, LEFT_SQUARE_BRACKET=27, 
		RIGHT_SQUARE_BRACKET=28, TILDE=29, EXCLAMATION=30, BACKSLASH=31, SLASH=32, 
		DOUBLE_SLASH=33, COLON=34, CARET=35, NUMBER=36, WS=37, ID_NAME=38, NEW_LINE=39, 
		ML_COMMENT=40, SL_COMMENT=41;
	public static final String[] tokenNames = {
		"<INVALID>", "'NO_INTERPRETATION'", "'ROOT_SYMBOL'", "'FILTER'", "'INCLUDE'", 
		"'GRAMMAR_KWSET'", "'#'", "'-'", "'_'", "'?'", "'*'", "'\"'", "'='", "'&'", 
		"'->'", "';'", "'''", "'|'", "'+'", "'{'", "'}'", "','", "'.'", "'('", 
		"')'", "'<'", "'>'", "'['", "']'", "'~'", "'!'", "'\\'", "'/'", "'//'", 
		"':'", "'^'", "NUMBER", "WS", "ID_NAME", "NEW_LINE", "ML_COMMENT", "SL_COMMENT"
	};
	public static final int
		RULE_nglrGrammar = 0, RULE_projection = 1, RULE_regularExpression = 2, 
		RULE_regularGroupExpression = 3, RULE_nonRegularExpression = 4, RULE_regQuantifier = 5, 
		RULE_nonTerminal = 6, RULE_nonTerminalExt = 7, RULE_literalStringExt = 8, 
		RULE_literalString = 9, RULE_extData = 10, RULE_extDataParam = 11, RULE_extDataParamName = 12, 
		RULE_extDataParamValue = 13, RULE_extDataLabel = 14, RULE_extDataLabelName = 15, 
		RULE_extDataArray = 16, RULE_extDataArrayName = 17, RULE_extDataArrayValue = 18, 
		RULE_interpData = 19, RULE_interpDataEntry = 20, RULE_nglrGrammarDirective = 21, 
		RULE_nglrGrammarDirectiveGrammarRoot = 22, RULE_nglrGrammarDirectiveInclude = 23, 
		RULE_nglrGrammarDirectiveGrammarKWSet = 24, RULE_nglrGrammarDirectiveGrammarKWSetValues = 25, 
		RULE_nglrGrammarDirectiveFilter = 26, RULE_nglrGrammarDirectiveNoInterpretation = 27;
	public static final String[] ruleNames = {
		"nglrGrammar", "projection", "regularExpression", "regularGroupExpression", 
		"nonRegularExpression", "regQuantifier", "nonTerminal", "nonTerminalExt", 
		"literalStringExt", "literalString", "extData", "extDataParam", "extDataParamName", 
		"extDataParamValue", "extDataLabel", "extDataLabelName", "extDataArray", 
		"extDataArrayName", "extDataArrayValue", "interpData", "interpDataEntry", 
		"nglrGrammarDirective", "nglrGrammarDirectiveGrammarRoot", "nglrGrammarDirectiveInclude", 
		"nglrGrammarDirectiveGrammarKWSet", "nglrGrammarDirectiveGrammarKWSetValues", 
		"nglrGrammarDirectiveFilter", "nglrGrammarDirectiveNoInterpretation"
	};

	@Override
	public String getGrammarFileName() { return "NGLR.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	  private String convertTokenListToString(List<Token> valLit) {
		  String result = "";
	    for (Token t: valLit)
		    result+=t.getText();
	    return result;
	  }

	public NGLRParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class NglrGrammarContext extends ParserRuleContext {
		public ProjectionContext projection(int i) {
			return getRuleContext(ProjectionContext.class,i);
		}
		public List<ProjectionContext> projection() {
			return getRuleContexts(ProjectionContext.class);
		}
		public NglrGrammarDirectiveContext nglrGrammarDirective(int i) {
			return getRuleContext(NglrGrammarDirectiveContext.class,i);
		}
		public List<NglrGrammarDirectiveContext> nglrGrammarDirective() {
			return getRuleContexts(NglrGrammarDirectiveContext.class);
		}
		public NglrGrammarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammar(this);
		}
	}

	public final NglrGrammarContext nglrGrammar() throws RecognitionException {
		NglrGrammarContext _localctx = new NglrGrammarContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_nglrGrammar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==HASH) {
				{
				{
				setState(56); nglrGrammarDirective();
				}
				}
				setState(61);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(63); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(62); projection();
				}
				}
				setState(65); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ID_NAME );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ProjectionContext extends ParserRuleContext {
		public NonTerminalContext lhs;
		public RegularExpressionContext regularExpression(int i) {
			return getRuleContext(RegularExpressionContext.class,i);
		}
		public TerminalNode SEMICOLUMN() { return getToken(NGLRParser.SEMICOLUMN, 0); }
		public NonRegularExpressionContext nonRegularExpression(int i) {
			return getRuleContext(NonRegularExpressionContext.class,i);
		}
		public List<NonRegularExpressionContext> nonRegularExpression() {
			return getRuleContexts(NonRegularExpressionContext.class);
		}
		public List<RegularExpressionContext> regularExpression() {
			return getRuleContexts(RegularExpressionContext.class);
		}
		public TerminalNode ARROW() { return getToken(NGLRParser.ARROW, 0); }
		public NonTerminalContext nonTerminal() {
			return getRuleContext(NonTerminalContext.class,0);
		}
		public ProjectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_projection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterProjection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitProjection(this);
		}
	}

	public final ProjectionContext projection() throws RecognitionException {
		ProjectionContext _localctx = new ProjectionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_projection);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67); ((ProjectionContext)_localctx).lhs = nonTerminal();
			setState(68); match(ARROW);
			setState(71); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(71);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(69); regularExpression();
					}
					break;

				case 2:
					{
					setState(70); nonRegularExpression();
					}
					break;
				}
				}
				setState(73); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SINGLE_QUOTE) | (1L << LEFT_BRACKET) | (1L << ID_NAME))) != 0) );
			setState(75); match(SEMICOLUMN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RegularExpressionContext extends ParserRuleContext {
		public RegQuantifierContext quant;
		public NonRegularExpressionContext nonRegularExpression() {
			return getRuleContext(NonRegularExpressionContext.class,0);
		}
		public RegQuantifierContext regQuantifier() {
			return getRuleContext(RegQuantifierContext.class,0);
		}
		public RegularGroupExpressionContext regularGroupExpression() {
			return getRuleContext(RegularGroupExpressionContext.class,0);
		}
		public RegularExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterRegularExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitRegularExpression(this);
		}
	}

	public final RegularExpressionContext regularExpression() throws RecognitionException {
		RegularExpressionContext _localctx = new RegularExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_regularExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			switch (_input.LA(1)) {
			case SINGLE_QUOTE:
			case ID_NAME:
				{
				setState(77); nonRegularExpression();
				setState(78); ((RegularExpressionContext)_localctx).quant = regQuantifier();
				}
				break;
			case LEFT_BRACKET:
				{
				setState(80); regularGroupExpression();
				setState(82);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << QUESTION) | (1L << STAR) | (1L << PLUS))) != 0)) {
					{
					setState(81); ((RegularExpressionContext)_localctx).quant = regQuantifier();
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RegularGroupExpressionContext extends ParserRuleContext {
		public NonRegularExpressionContext nonRegularExpression(int i) {
			return getRuleContext(NonRegularExpressionContext.class,i);
		}
		public TerminalNode VERTICAL_BAR(int i) {
			return getToken(NGLRParser.VERTICAL_BAR, i);
		}
		public List<NonRegularExpressionContext> nonRegularExpression() {
			return getRuleContexts(NonRegularExpressionContext.class);
		}
		public List<TerminalNode> VERTICAL_BAR() { return getTokens(NGLRParser.VERTICAL_BAR); }
		public TerminalNode LEFT_BRACKET() { return getToken(NGLRParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(NGLRParser.RIGHT_BRACKET, 0); }
		public RegularGroupExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularGroupExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterRegularGroupExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitRegularGroupExpression(this);
		}
	}

	public final RegularGroupExpressionContext regularGroupExpression() throws RecognitionException {
		RegularGroupExpressionContext _localctx = new RegularGroupExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_regularGroupExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86); match(LEFT_BRACKET);
			setState(87); nonRegularExpression();
			setState(92);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==VERTICAL_BAR) {
				{
				{
				setState(88); match(VERTICAL_BAR);
				setState(89); nonRegularExpression();
				}
				}
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(95); match(RIGHT_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonRegularExpressionContext extends ParserRuleContext {
		public LiteralStringExtContext literalStringExt() {
			return getRuleContext(LiteralStringExtContext.class,0);
		}
		public NonTerminalExtContext nonTerminalExt() {
			return getRuleContext(NonTerminalExtContext.class,0);
		}
		public NonRegularExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonRegularExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNonRegularExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNonRegularExpression(this);
		}
	}

	public final NonRegularExpressionContext nonRegularExpression() throws RecognitionException {
		NonRegularExpressionContext _localctx = new NonRegularExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_nonRegularExpression);
		try {
			setState(99);
			switch (_input.LA(1)) {
			case SINGLE_QUOTE:
				enterOuterAlt(_localctx, 1);
				{
				setState(97); literalStringExt();
				}
				break;
			case ID_NAME:
				enterOuterAlt(_localctx, 2);
				{
				setState(98); nonTerminalExt();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RegQuantifierContext extends ParserRuleContext {
		public String ret;
		public Token val;
		public TerminalNode PLUS() { return getToken(NGLRParser.PLUS, 0); }
		public TerminalNode STAR() { return getToken(NGLRParser.STAR, 0); }
		public TerminalNode QUESTION() { return getToken(NGLRParser.QUESTION, 0); }
		public RegQuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regQuantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterRegQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitRegQuantifier(this);
		}
	}

	public final RegQuantifierContext regQuantifier() throws RecognitionException {
		RegQuantifierContext _localctx = new RegQuantifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_regQuantifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			((RegQuantifierContext)_localctx).val = _input.LT(1);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << QUESTION) | (1L << STAR) | (1L << PLUS))) != 0)) ) {
				((RegQuantifierContext)_localctx).val = (Token)_errHandler.recoverInline(this);
			}
			consume();
			((RegQuantifierContext)_localctx).ret =  (((RegQuantifierContext)_localctx).val!=null?((RegQuantifierContext)_localctx).val.getText():null);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonTerminalContext extends ParserRuleContext {
		public Token name;
		public TerminalNode ID_NAME() { return getToken(NGLRParser.ID_NAME, 0); }
		public NonTerminalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonTerminal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNonTerminal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNonTerminal(this);
		}
	}

	public final NonTerminalContext nonTerminal() throws RecognitionException {
		NonTerminalContext _localctx = new NonTerminalContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_nonTerminal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104); ((NonTerminalContext)_localctx).name = match(ID_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonTerminalExtContext extends ParserRuleContext {
		public Token name;
		public ExtDataContext extData() {
			return getRuleContext(ExtDataContext.class,0);
		}
		public InterpDataContext interpData() {
			return getRuleContext(InterpDataContext.class,0);
		}
		public TerminalNode ID_NAME() { return getToken(NGLRParser.ID_NAME, 0); }
		public NonTerminalExtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonTerminalExt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNonTerminalExt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNonTerminalExt(this);
		}
	}

	public final NonTerminalExtContext nonTerminalExt() throws RecognitionException {
		NonTerminalExtContext _localctx = new NonTerminalExtContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_nonTerminalExt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106); ((NonTerminalExtContext)_localctx).name = match(ID_NAME);
			setState(108);
			_la = _input.LA(1);
			if (_la==LEFT_ANGLE_BRACKET) {
				{
				setState(107); extData();
				}
			}

			setState(111);
			_la = _input.LA(1);
			if (_la==LEFT_CURVE_BRACKET) {
				{
				setState(110); interpData();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralStringExtContext extends ParserRuleContext {
		public LiteralStringContext value;
		public ExtDataContext ext;
		public ExtDataContext extData() {
			return getRuleContext(ExtDataContext.class,0);
		}
		public LiteralStringContext literalString() {
			return getRuleContext(LiteralStringContext.class,0);
		}
		public LiteralStringExtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalStringExt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterLiteralStringExt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitLiteralStringExt(this);
		}
	}

	public final LiteralStringExtContext literalStringExt() throws RecognitionException {
		LiteralStringExtContext _localctx = new LiteralStringExtContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_literalStringExt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113); ((LiteralStringExtContext)_localctx).value = literalString();
			setState(115);
			_la = _input.LA(1);
			if (_la==LEFT_ANGLE_BRACKET) {
				{
				setState(114); ((LiteralStringExtContext)_localctx).ext = extData();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralStringContext extends ParserRuleContext {
		public String ret;
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token UNDERSCORE;
		public Token NUMBER;
		public Token COMMA;
		public Token POINT;
		public Token BACKSLASH;
		public Token QUESTION;
		public Token PLUS;
		public Token STAR;
		public Token COLON;
		public Token AMPERSAND;
		public Token _tset217;
		public List<TerminalNode> BACKSLASH() { return getTokens(NGLRParser.BACKSLASH); }
		public TerminalNode POINT(int i) {
			return getToken(NGLRParser.POINT, i);
		}
		public TerminalNode QUESTION(int i) {
			return getToken(NGLRParser.QUESTION, i);
		}
		public List<TerminalNode> STAR() { return getTokens(NGLRParser.STAR); }
		public List<TerminalNode> AMPERSAND() { return getTokens(NGLRParser.AMPERSAND); }
		public TerminalNode AMPERSAND(int i) {
			return getToken(NGLRParser.AMPERSAND, i);
		}
		public TerminalNode PLUS(int i) {
			return getToken(NGLRParser.PLUS, i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public TerminalNode BACKSLASH(int i) {
			return getToken(NGLRParser.BACKSLASH, i);
		}
		public TerminalNode STAR(int i) {
			return getToken(NGLRParser.STAR, i);
		}
		public List<TerminalNode> UNDERSCORE() { return getTokens(NGLRParser.UNDERSCORE); }
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public List<TerminalNode> QUESTION() { return getTokens(NGLRParser.QUESTION); }
		public List<TerminalNode> SINGLE_QUOTE() { return getTokens(NGLRParser.SINGLE_QUOTE); }
		public TerminalNode NUMBER(int i) {
			return getToken(NGLRParser.NUMBER, i);
		}
		public List<TerminalNode> COLON() { return getTokens(NGLRParser.COLON); }
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public TerminalNode UNDERSCORE(int i) {
			return getToken(NGLRParser.UNDERSCORE, i);
		}
		public TerminalNode SINGLE_QUOTE(int i) {
			return getToken(NGLRParser.SINGLE_QUOTE, i);
		}
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public List<TerminalNode> POINT() { return getTokens(NGLRParser.POINT); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public TerminalNode COLON(int i) {
			return getToken(NGLRParser.COLON, i);
		}
		public List<TerminalNode> PLUS() { return getTokens(NGLRParser.PLUS); }
		public List<TerminalNode> NUMBER() { return getTokens(NGLRParser.NUMBER); }
		public LiteralStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterLiteralString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitLiteralString(this);
		}
	}

	public final LiteralStringContext literalString() throws RecognitionException {
		LiteralStringContext _localctx = new LiteralStringContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_literalString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117); match(SINGLE_QUOTE);
			setState(119); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(118);
				((LiteralStringContext)_localctx)._tset217 = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << UNDERSCORE) | (1L << QUESTION) | (1L << STAR) | (1L << AMPERSAND) | (1L << PLUS) | (1L << COMMA) | (1L << POINT) | (1L << BACKSLASH) | (1L << COLON) | (1L << NUMBER) | (1L << ID_NAME))) != 0)) ) {
					((LiteralStringContext)_localctx)._tset217 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((LiteralStringContext)_localctx).valLit.add(((LiteralStringContext)_localctx)._tset217);
				}
				}
				setState(121); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << UNDERSCORE) | (1L << QUESTION) | (1L << STAR) | (1L << AMPERSAND) | (1L << PLUS) | (1L << COMMA) | (1L << POINT) | (1L << BACKSLASH) | (1L << COLON) | (1L << NUMBER) | (1L << ID_NAME))) != 0) );
			((LiteralStringContext)_localctx).ret = convertTokenListToString(((LiteralStringContext)_localctx).valLit);
			setState(124); match(SINGLE_QUOTE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataContext extends ParserRuleContext {
		public ExtDataLabelContext extDataLabel(int i) {
			return getRuleContext(ExtDataLabelContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public ExtDataArrayContext extDataArray(int i) {
			return getRuleContext(ExtDataArrayContext.class,i);
		}
		public TerminalNode RIGHT_ANGLE_BRACKET() { return getToken(NGLRParser.RIGHT_ANGLE_BRACKET, 0); }
		public TerminalNode LEFT_ANGLE_BRACKET() { return getToken(NGLRParser.LEFT_ANGLE_BRACKET, 0); }
		public List<ExtDataLabelContext> extDataLabel() {
			return getRuleContexts(ExtDataLabelContext.class);
		}
		public List<ExtDataParamContext> extDataParam() {
			return getRuleContexts(ExtDataParamContext.class);
		}
		public ExtDataParamContext extDataParam(int i) {
			return getRuleContext(ExtDataParamContext.class,i);
		}
		public List<ExtDataArrayContext> extDataArray() {
			return getRuleContexts(ExtDataArrayContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public ExtDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtData(this);
		}
	}

	public final ExtDataContext extData() throws RecognitionException {
		ExtDataContext _localctx = new ExtDataContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_extData);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126); match(LEFT_ANGLE_BRACKET);
			setState(130);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(127); extDataLabel();
				}
				break;

			case 2:
				{
				setState(128); extDataParam();
				}
				break;

			case 3:
				{
				setState(129); extDataArray();
				}
				break;
			}
			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(132); match(COMMA);
				setState(136);
				switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
				case 1:
					{
					setState(133); extDataLabel();
					}
					break;

				case 2:
					{
					setState(134); extDataParam();
					}
					break;

				case 3:
					{
					setState(135); extDataArray();
					}
					break;
				}
				}
				}
				setState(142);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(143); match(RIGHT_ANGLE_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataParamContext extends ParserRuleContext {
		public ExtDataParamNameContext name;
		public ExtDataParamValueContext value;
		public ExtDataParamNameContext extDataParamName() {
			return getRuleContext(ExtDataParamNameContext.class,0);
		}
		public TerminalNode EQUAL() { return getToken(NGLRParser.EQUAL, 0); }
		public ExtDataParamValueContext extDataParamValue() {
			return getRuleContext(ExtDataParamValueContext.class,0);
		}
		public ExtDataParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataParam(this);
		}
	}

	public final ExtDataParamContext extDataParam() throws RecognitionException {
		ExtDataParamContext _localctx = new ExtDataParamContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_extDataParam);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145); ((ExtDataParamContext)_localctx).name = extDataParamName();
			setState(146); match(EQUAL);
			setState(147); ((ExtDataParamContext)_localctx).value = extDataParamValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataParamNameContext extends ParserRuleContext {
		public String ret;
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token _tset313;
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public ExtDataParamNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataParamName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataParamName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataParamName(this);
		}
	}

	public final ExtDataParamNameContext extDataParamName() throws RecognitionException {
		ExtDataParamNameContext _localctx = new ExtDataParamNameContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_extDataParamName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(149);
				((ExtDataParamNameContext)_localctx)._tset313 = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==HYPHEN || _la==ID_NAME) ) {
					((ExtDataParamNameContext)_localctx)._tset313 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((ExtDataParamNameContext)_localctx).valLit.add(((ExtDataParamNameContext)_localctx)._tset313);
				}
				}
				setState(152); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==HYPHEN || _la==ID_NAME );
			((ExtDataParamNameContext)_localctx).ret = convertTokenListToString(((ExtDataParamNameContext)_localctx).valLit);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataParamValueContext extends ParserRuleContext {
		public String ret;
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token UNDERSCORE;
		public Token NUMBER;
		public Token COMMA;
		public Token POINT;
		public Token BACKSLASH;
		public Token QUESTION;
		public Token PLUS;
		public Token STAR;
		public Token COLON;
		public Token CARET;
		public Token LEFT_BRACKET;
		public Token RIGHT_BRACKET;
		public Token LEFT_SQUARE_BRACKET;
		public Token RIGHT_SQUARE_BRACKET;
		public Token LEFT_CURVE_BRACKET;
		public Token RIGHT_CURVE_BRACKET;
		public Token VERTICAL_BAR;
		public Token _tset339;
		public List<TerminalNode> BACKSLASH() { return getTokens(NGLRParser.BACKSLASH); }
		public TerminalNode CARET(int i) {
			return getToken(NGLRParser.CARET, i);
		}
		public TerminalNode RIGHT_BRACKET(int i) {
			return getToken(NGLRParser.RIGHT_BRACKET, i);
		}
		public List<TerminalNode> DOUBLE_QUOTE() { return getTokens(NGLRParser.DOUBLE_QUOTE); }
		public TerminalNode POINT(int i) {
			return getToken(NGLRParser.POINT, i);
		}
		public TerminalNode QUESTION(int i) {
			return getToken(NGLRParser.QUESTION, i);
		}
		public List<TerminalNode> STAR() { return getTokens(NGLRParser.STAR); }
		public TerminalNode PLUS(int i) {
			return getToken(NGLRParser.PLUS, i);
		}
		public List<TerminalNode> LEFT_CURVE_BRACKET() { return getTokens(NGLRParser.LEFT_CURVE_BRACKET); }
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public TerminalNode BACKSLASH(int i) {
			return getToken(NGLRParser.BACKSLASH, i);
		}
		public TerminalNode STAR(int i) {
			return getToken(NGLRParser.STAR, i);
		}
		public List<TerminalNode> UNDERSCORE() { return getTokens(NGLRParser.UNDERSCORE); }
		public TerminalNode VERTICAL_BAR(int i) {
			return getToken(NGLRParser.VERTICAL_BAR, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public List<TerminalNode> RIGHT_BRACKET() { return getTokens(NGLRParser.RIGHT_BRACKET); }
		public List<TerminalNode> LEFT_SQUARE_BRACKET() { return getTokens(NGLRParser.LEFT_SQUARE_BRACKET); }
		public TerminalNode RIGHT_CURVE_BRACKET(int i) {
			return getToken(NGLRParser.RIGHT_CURVE_BRACKET, i);
		}
		public TerminalNode LEFT_CURVE_BRACKET(int i) {
			return getToken(NGLRParser.LEFT_CURVE_BRACKET, i);
		}
		public List<TerminalNode> QUESTION() { return getTokens(NGLRParser.QUESTION); }
		public List<TerminalNode> RIGHT_SQUARE_BRACKET() { return getTokens(NGLRParser.RIGHT_SQUARE_BRACKET); }
		public TerminalNode NUMBER(int i) {
			return getToken(NGLRParser.NUMBER, i);
		}
		public List<TerminalNode> COLON() { return getTokens(NGLRParser.COLON); }
		public TerminalNode LEFT_SQUARE_BRACKET(int i) {
			return getToken(NGLRParser.LEFT_SQUARE_BRACKET, i);
		}
		public TerminalNode LEFT_BRACKET(int i) {
			return getToken(NGLRParser.LEFT_BRACKET, i);
		}
		public TerminalNode DOUBLE_QUOTE(int i) {
			return getToken(NGLRParser.DOUBLE_QUOTE, i);
		}
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public TerminalNode UNDERSCORE(int i) {
			return getToken(NGLRParser.UNDERSCORE, i);
		}
		public TerminalNode RIGHT_SQUARE_BRACKET(int i) {
			return getToken(NGLRParser.RIGHT_SQUARE_BRACKET, i);
		}
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public List<TerminalNode> POINT() { return getTokens(NGLRParser.POINT); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public TerminalNode COLON(int i) {
			return getToken(NGLRParser.COLON, i);
		}
		public List<TerminalNode> VERTICAL_BAR() { return getTokens(NGLRParser.VERTICAL_BAR); }
		public List<TerminalNode> LEFT_BRACKET() { return getTokens(NGLRParser.LEFT_BRACKET); }
		public List<TerminalNode> PLUS() { return getTokens(NGLRParser.PLUS); }
		public List<TerminalNode> CARET() { return getTokens(NGLRParser.CARET); }
		public List<TerminalNode> NUMBER() { return getTokens(NGLRParser.NUMBER); }
		public List<TerminalNode> RIGHT_CURVE_BRACKET() { return getTokens(NGLRParser.RIGHT_CURVE_BRACKET); }
		public ExtDataParamValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataParamValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataParamValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataParamValue(this);
		}
	}

	public final ExtDataParamValueContext extDataParamValue() throws RecognitionException {
		ExtDataParamValueContext _localctx = new ExtDataParamValueContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_extDataParamValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156); match(DOUBLE_QUOTE);
			setState(158); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(157);
				((ExtDataParamValueContext)_localctx)._tset339 = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << UNDERSCORE) | (1L << QUESTION) | (1L << STAR) | (1L << VERTICAL_BAR) | (1L << PLUS) | (1L << LEFT_CURVE_BRACKET) | (1L << RIGHT_CURVE_BRACKET) | (1L << COMMA) | (1L << POINT) | (1L << LEFT_BRACKET) | (1L << RIGHT_BRACKET) | (1L << LEFT_SQUARE_BRACKET) | (1L << RIGHT_SQUARE_BRACKET) | (1L << BACKSLASH) | (1L << COLON) | (1L << CARET) | (1L << NUMBER) | (1L << ID_NAME))) != 0)) ) {
					((ExtDataParamValueContext)_localctx)._tset339 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((ExtDataParamValueContext)_localctx).valLit.add(((ExtDataParamValueContext)_localctx)._tset339);
				}
				}
				setState(160); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << UNDERSCORE) | (1L << QUESTION) | (1L << STAR) | (1L << VERTICAL_BAR) | (1L << PLUS) | (1L << LEFT_CURVE_BRACKET) | (1L << RIGHT_CURVE_BRACKET) | (1L << COMMA) | (1L << POINT) | (1L << LEFT_BRACKET) | (1L << RIGHT_BRACKET) | (1L << LEFT_SQUARE_BRACKET) | (1L << RIGHT_SQUARE_BRACKET) | (1L << BACKSLASH) | (1L << COLON) | (1L << CARET) | (1L << NUMBER) | (1L << ID_NAME))) != 0) );
			((ExtDataParamValueContext)_localctx).ret = convertTokenListToString(((ExtDataParamValueContext)_localctx).valLit);
			setState(163); match(DOUBLE_QUOTE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataLabelContext extends ParserRuleContext {
		public ExtDataLabelNameContext name;
		public ExtDataLabelNameContext extDataLabelName() {
			return getRuleContext(ExtDataLabelNameContext.class,0);
		}
		public ExtDataLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataLabel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataLabel(this);
		}
	}

	public final ExtDataLabelContext extDataLabel() throws RecognitionException {
		ExtDataLabelContext _localctx = new ExtDataLabelContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_extDataLabel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165); ((ExtDataLabelContext)_localctx).name = extDataLabelName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataLabelNameContext extends ParserRuleContext {
		public String ret;
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token _tset410;
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public ExtDataLabelNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataLabelName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataLabelName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataLabelName(this);
		}
	}

	public final ExtDataLabelNameContext extDataLabelName() throws RecognitionException {
		ExtDataLabelNameContext _localctx = new ExtDataLabelNameContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_extDataLabelName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(167);
				((ExtDataLabelNameContext)_localctx)._tset410 = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==HYPHEN || _la==ID_NAME) ) {
					((ExtDataLabelNameContext)_localctx)._tset410 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((ExtDataLabelNameContext)_localctx).valLit.add(((ExtDataLabelNameContext)_localctx)._tset410);
				}
				}
				setState(170); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==HYPHEN || _la==ID_NAME );
			((ExtDataLabelNameContext)_localctx).ret = convertTokenListToString(((ExtDataLabelNameContext)_localctx).valLit);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataArrayContext extends ParserRuleContext {
		public ExtDataArrayNameContext name;
		public ExtDataArrayValueContext value;
		public TerminalNode RIGHT_SQUARE_BRACKET() { return getToken(NGLRParser.RIGHT_SQUARE_BRACKET, 0); }
		public TerminalNode EQUAL() { return getToken(NGLRParser.EQUAL, 0); }
		public ExtDataArrayValueContext extDataArrayValue() {
			return getRuleContext(ExtDataArrayValueContext.class,0);
		}
		public ExtDataArrayNameContext extDataArrayName() {
			return getRuleContext(ExtDataArrayNameContext.class,0);
		}
		public TerminalNode LEFT_SQUARE_BRACKET() { return getToken(NGLRParser.LEFT_SQUARE_BRACKET, 0); }
		public ExtDataArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataArray; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataArray(this);
		}
	}

	public final ExtDataArrayContext extDataArray() throws RecognitionException {
		ExtDataArrayContext _localctx = new ExtDataArrayContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_extDataArray);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(174); ((ExtDataArrayContext)_localctx).name = extDataArrayName();
			setState(175); match(EQUAL);
			setState(176); match(LEFT_SQUARE_BRACKET);
			setState(177); ((ExtDataArrayContext)_localctx).value = extDataArrayValue();
			setState(178); match(RIGHT_SQUARE_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataArrayNameContext extends ParserRuleContext {
		public String ret;
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token _tset461;
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public ExtDataArrayNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataArrayName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataArrayName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataArrayName(this);
		}
	}

	public final ExtDataArrayNameContext extDataArrayName() throws RecognitionException {
		ExtDataArrayNameContext _localctx = new ExtDataArrayNameContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_extDataArrayName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(180);
				((ExtDataArrayNameContext)_localctx)._tset461 = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==HYPHEN || _la==ID_NAME) ) {
					((ExtDataArrayNameContext)_localctx)._tset461 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((ExtDataArrayNameContext)_localctx).valLit.add(((ExtDataArrayNameContext)_localctx)._tset461);
				}
				}
				setState(183); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==HYPHEN || _la==ID_NAME );
			((ExtDataArrayNameContext)_localctx).ret = convertTokenListToString(((ExtDataArrayNameContext)_localctx).valLit);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtDataArrayValueContext extends ParserRuleContext {
		public String ret;
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token NUMBER;
		public Token LEFT_BRACKET;
		public Token RIGHT_BRACKET;
		public Token AMPERSAND;
		public Token VERTICAL_BAR;
		public Token DOUBLE_QUOTE;
		public Token COMMA;
		public Token _tset484;
		public TerminalNode NUMBER(int i) {
			return getToken(NGLRParser.NUMBER, i);
		}
		public TerminalNode RIGHT_BRACKET(int i) {
			return getToken(NGLRParser.RIGHT_BRACKET, i);
		}
		public TerminalNode LEFT_BRACKET(int i) {
			return getToken(NGLRParser.LEFT_BRACKET, i);
		}
		public List<TerminalNode> DOUBLE_QUOTE() { return getTokens(NGLRParser.DOUBLE_QUOTE); }
		public List<TerminalNode> AMPERSAND() { return getTokens(NGLRParser.AMPERSAND); }
		public TerminalNode AMPERSAND(int i) {
			return getToken(NGLRParser.AMPERSAND, i);
		}
		public TerminalNode DOUBLE_QUOTE(int i) {
			return getToken(NGLRParser.DOUBLE_QUOTE, i);
		}
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public TerminalNode VERTICAL_BAR(int i) {
			return getToken(NGLRParser.VERTICAL_BAR, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public List<TerminalNode> VERTICAL_BAR() { return getTokens(NGLRParser.VERTICAL_BAR); }
		public List<TerminalNode> LEFT_BRACKET() { return getTokens(NGLRParser.LEFT_BRACKET); }
		public List<TerminalNode> RIGHT_BRACKET() { return getTokens(NGLRParser.RIGHT_BRACKET); }
		public List<TerminalNode> NUMBER() { return getTokens(NGLRParser.NUMBER); }
		public ExtDataArrayValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extDataArrayValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterExtDataArrayValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitExtDataArrayValue(this);
		}
	}

	public final ExtDataArrayValueContext extDataArrayValue() throws RecognitionException {
		ExtDataArrayValueContext _localctx = new ExtDataArrayValueContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_extDataArrayValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(187);
				((ExtDataArrayValueContext)_localctx)._tset484 = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << DOUBLE_QUOTE) | (1L << AMPERSAND) | (1L << VERTICAL_BAR) | (1L << COMMA) | (1L << LEFT_BRACKET) | (1L << RIGHT_BRACKET) | (1L << NUMBER) | (1L << ID_NAME))) != 0)) ) {
					((ExtDataArrayValueContext)_localctx)._tset484 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((ExtDataArrayValueContext)_localctx).valLit.add(((ExtDataArrayValueContext)_localctx)._tset484);
				}
				}
				setState(190); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << DOUBLE_QUOTE) | (1L << AMPERSAND) | (1L << VERTICAL_BAR) | (1L << COMMA) | (1L << LEFT_BRACKET) | (1L << RIGHT_BRACKET) | (1L << NUMBER) | (1L << ID_NAME))) != 0) );
			((ExtDataArrayValueContext)_localctx).ret = convertTokenListToString(((ExtDataArrayValueContext)_localctx).valLit);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InterpDataContext extends ParserRuleContext {
		public InterpDataEntryContext interpDataEntry(int i) {
			return getRuleContext(InterpDataEntryContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public TerminalNode LEFT_CURVE_BRACKET() { return getToken(NGLRParser.LEFT_CURVE_BRACKET, 0); }
		public List<InterpDataEntryContext> interpDataEntry() {
			return getRuleContexts(InterpDataEntryContext.class);
		}
		public TerminalNode RIGHT_CURVE_BRACKET() { return getToken(NGLRParser.RIGHT_CURVE_BRACKET, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public InterpDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interpData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterInterpData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitInterpData(this);
		}
	}

	public final InterpDataContext interpData() throws RecognitionException {
		InterpDataContext _localctx = new InterpDataContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_interpData);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194); match(LEFT_CURVE_BRACKET);
			setState(195); interpDataEntry();
			setState(200);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(196); match(COMMA);
				setState(197); interpDataEntry();
				}
				}
				setState(202);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(203); match(RIGHT_CURVE_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InterpDataEntryContext extends ParserRuleContext {
		public Token ID_NAME;
		public List<Token> nameVal = new ArrayList<Token>();
		public Token POINT;
		public Token _tset546;
		public Token literalVal;
		public List<Token> factVal = new ArrayList<Token>();
		public Token _tset568;
		public ExtDataLabelNameContext paramName;
		public TerminalNode EQUAL() { return getToken(NGLRParser.EQUAL, 0); }
		public TerminalNode SINGLE_QUOTE(int i) {
			return getToken(NGLRParser.SINGLE_QUOTE, i);
		}
		public List<TerminalNode> POINT() { return getTokens(NGLRParser.POINT); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public TerminalNode RIGHT_ANGLE_BRACKET() { return getToken(NGLRParser.RIGHT_ANGLE_BRACKET, 0); }
		public ExtDataLabelNameContext extDataLabelName() {
			return getRuleContext(ExtDataLabelNameContext.class,0);
		}
		public TerminalNode POINT(int i) {
			return getToken(NGLRParser.POINT, i);
		}
		public TerminalNode LEFT_ANGLE_BRACKET() { return getToken(NGLRParser.LEFT_ANGLE_BRACKET, 0); }
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public List<TerminalNode> SINGLE_QUOTE() { return getTokens(NGLRParser.SINGLE_QUOTE); }
		public InterpDataEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interpDataEntry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterInterpDataEntry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitInterpDataEntry(this);
		}
	}

	public final InterpDataEntryContext interpDataEntry() throws RecognitionException {
		InterpDataEntryContext _localctx = new InterpDataEntryContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_interpDataEntry);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(205);
				((InterpDataEntryContext)_localctx)._tset546 = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==POINT || _la==ID_NAME) ) {
					((InterpDataEntryContext)_localctx)._tset546 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((InterpDataEntryContext)_localctx).nameVal.add(((InterpDataEntryContext)_localctx)._tset546);
				}
				}
				setState(208); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==POINT || _la==ID_NAME );
			setState(221);
			_la = _input.LA(1);
			if (_la==EQUAL) {
				{
				setState(210); match(EQUAL);
				setState(219);
				switch (_input.LA(1)) {
				case SINGLE_QUOTE:
					{
					setState(211); match(SINGLE_QUOTE);
					setState(212); ((InterpDataEntryContext)_localctx).literalVal = match(ID_NAME);
					setState(213); match(SINGLE_QUOTE);
					}
					break;
				case POINT:
				case ID_NAME:
					{
					setState(215); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(214);
						((InterpDataEntryContext)_localctx)._tset568 = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==POINT || _la==ID_NAME) ) {
							((InterpDataEntryContext)_localctx)._tset568 = (Token)_errHandler.recoverInline(this);
						}
						consume();
						((InterpDataEntryContext)_localctx).factVal.add(((InterpDataEntryContext)_localctx)._tset568);
						}
						}
						setState(217); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==POINT || _la==ID_NAME );
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
			}

			setState(227);
			_la = _input.LA(1);
			if (_la==LEFT_ANGLE_BRACKET) {
				{
				setState(223); match(LEFT_ANGLE_BRACKET);
				setState(224); ((InterpDataEntryContext)_localctx).paramName = extDataLabelName();
				setState(225); match(RIGHT_ANGLE_BRACKET);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NglrGrammarDirectiveContext extends ParserRuleContext {
		public NglrGrammarDirectiveGrammarKWSetContext nglrGrammarDirectiveGrammarKWSet() {
			return getRuleContext(NglrGrammarDirectiveGrammarKWSetContext.class,0);
		}
		public NglrGrammarDirectiveIncludeContext nglrGrammarDirectiveInclude() {
			return getRuleContext(NglrGrammarDirectiveIncludeContext.class,0);
		}
		public NglrGrammarDirectiveNoInterpretationContext nglrGrammarDirectiveNoInterpretation() {
			return getRuleContext(NglrGrammarDirectiveNoInterpretationContext.class,0);
		}
		public NglrGrammarDirectiveFilterContext nglrGrammarDirectiveFilter() {
			return getRuleContext(NglrGrammarDirectiveFilterContext.class,0);
		}
		public NglrGrammarDirectiveGrammarRootContext nglrGrammarDirectiveGrammarRoot() {
			return getRuleContext(NglrGrammarDirectiveGrammarRootContext.class,0);
		}
		public NglrGrammarDirectiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammarDirective; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammarDirective(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammarDirective(this);
		}
	}

	public final NglrGrammarDirectiveContext nglrGrammarDirective() throws RecognitionException {
		NglrGrammarDirectiveContext _localctx = new NglrGrammarDirectiveContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_nglrGrammarDirective);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(229); nglrGrammarDirectiveInclude();
				}
				break;

			case 2:
				{
				setState(230); nglrGrammarDirectiveGrammarRoot();
				}
				break;

			case 3:
				{
				setState(231); nglrGrammarDirectiveGrammarKWSet();
				}
				break;

			case 4:
				{
				setState(232); nglrGrammarDirectiveFilter();
				}
				break;

			case 5:
				{
				setState(233); nglrGrammarDirectiveNoInterpretation();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NglrGrammarDirectiveGrammarRootContext extends ParserRuleContext {
		public Token val;
		public TerminalNode SEMICOLUMN() { return getToken(NGLRParser.SEMICOLUMN, 0); }
		public TerminalNode ID_NAME() { return getToken(NGLRParser.ID_NAME, 0); }
		public TerminalNode HASH() { return getToken(NGLRParser.HASH, 0); }
		public NglrGrammarDirectiveGrammarRootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammarDirectiveGrammarRoot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammarDirectiveGrammarRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammarDirectiveGrammarRoot(this);
		}
	}

	public final NglrGrammarDirectiveGrammarRootContext nglrGrammarDirectiveGrammarRoot() throws RecognitionException {
		NglrGrammarDirectiveGrammarRootContext _localctx = new NglrGrammarDirectiveGrammarRootContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_nglrGrammarDirectiveGrammarRoot);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236); match(HASH);
			setState(237); match(T__3);
			setState(238); ((NglrGrammarDirectiveGrammarRootContext)_localctx).val = match(ID_NAME);
			setState(239); match(SEMICOLUMN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NglrGrammarDirectiveIncludeContext extends ParserRuleContext {
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token COMMA;
		public Token POINT;
		public Token BACKSLASH;
		public Token SLASH;
		public Token _tset646;
		public List<TerminalNode> BACKSLASH() { return getTokens(NGLRParser.BACKSLASH); }
		public TerminalNode SEMICOLUMN() { return getToken(NGLRParser.SEMICOLUMN, 0); }
		public List<TerminalNode> DOUBLE_QUOTE() { return getTokens(NGLRParser.DOUBLE_QUOTE); }
		public TerminalNode POINT(int i) {
			return getToken(NGLRParser.POINT, i);
		}
		public TerminalNode SLASH(int i) {
			return getToken(NGLRParser.SLASH, i);
		}
		public TerminalNode DOUBLE_QUOTE(int i) {
			return getToken(NGLRParser.DOUBLE_QUOTE, i);
		}
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public TerminalNode BACKSLASH(int i) {
			return getToken(NGLRParser.BACKSLASH, i);
		}
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public List<TerminalNode> POINT() { return getTokens(NGLRParser.POINT); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public List<TerminalNode> SLASH() { return getTokens(NGLRParser.SLASH); }
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public TerminalNode HASH() { return getToken(NGLRParser.HASH, 0); }
		public NglrGrammarDirectiveIncludeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammarDirectiveInclude; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammarDirectiveInclude(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammarDirectiveInclude(this);
		}
	}

	public final NglrGrammarDirectiveIncludeContext nglrGrammarDirectiveInclude() throws RecognitionException {
		NglrGrammarDirectiveIncludeContext _localctx = new NglrGrammarDirectiveIncludeContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_nglrGrammarDirectiveInclude);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(241); match(HASH);
			setState(242); match(T__1);
			setState(243); match(DOUBLE_QUOTE);
			setState(245); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(244);
				((NglrGrammarDirectiveIncludeContext)_localctx)._tset646 = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << COMMA) | (1L << POINT) | (1L << BACKSLASH) | (1L << SLASH) | (1L << ID_NAME))) != 0)) ) {
					((NglrGrammarDirectiveIncludeContext)_localctx)._tset646 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((NglrGrammarDirectiveIncludeContext)_localctx).valLit.add(((NglrGrammarDirectiveIncludeContext)_localctx)._tset646);
				}
				}
				setState(247); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << COMMA) | (1L << POINT) | (1L << BACKSLASH) | (1L << SLASH) | (1L << ID_NAME))) != 0) );
			setState(249); match(DOUBLE_QUOTE);
			setState(250); match(SEMICOLUMN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NglrGrammarDirectiveGrammarKWSetContext extends ParserRuleContext {
		public NglrGrammarDirectiveGrammarKWSetValuesContext nglrGrammarDirectiveGrammarKWSetValues;
		public List<NglrGrammarDirectiveGrammarKWSetValuesContext> valLit = new ArrayList<NglrGrammarDirectiveGrammarKWSetValuesContext>();
		public TerminalNode RIGHT_SQUARE_BRACKET() { return getToken(NGLRParser.RIGHT_SQUARE_BRACKET, 0); }
		public List<NglrGrammarDirectiveGrammarKWSetValuesContext> nglrGrammarDirectiveGrammarKWSetValues() {
			return getRuleContexts(NglrGrammarDirectiveGrammarKWSetValuesContext.class);
		}
		public TerminalNode SEMICOLUMN() { return getToken(NGLRParser.SEMICOLUMN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public TerminalNode LEFT_SQUARE_BRACKET() { return getToken(NGLRParser.LEFT_SQUARE_BRACKET, 0); }
		public NglrGrammarDirectiveGrammarKWSetValuesContext nglrGrammarDirectiveGrammarKWSetValues(int i) {
			return getRuleContext(NglrGrammarDirectiveGrammarKWSetValuesContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public TerminalNode HASH() { return getToken(NGLRParser.HASH, 0); }
		public NglrGrammarDirectiveGrammarKWSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammarDirectiveGrammarKWSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammarDirectiveGrammarKWSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammarDirectiveGrammarKWSet(this);
		}
	}

	public final NglrGrammarDirectiveGrammarKWSetContext nglrGrammarDirectiveGrammarKWSet() throws RecognitionException {
		NglrGrammarDirectiveGrammarKWSetContext _localctx = new NglrGrammarDirectiveGrammarKWSetContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_nglrGrammarDirectiveGrammarKWSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252); match(HASH);
			setState(253); match(T__0);
			setState(254); match(LEFT_SQUARE_BRACKET);
			setState(255); ((NglrGrammarDirectiveGrammarKWSetContext)_localctx).nglrGrammarDirectiveGrammarKWSetValues = nglrGrammarDirectiveGrammarKWSetValues();
			((NglrGrammarDirectiveGrammarKWSetContext)_localctx).valLit.add(((NglrGrammarDirectiveGrammarKWSetContext)_localctx).nglrGrammarDirectiveGrammarKWSetValues);
			setState(260);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(256); match(COMMA);
				setState(257); ((NglrGrammarDirectiveGrammarKWSetContext)_localctx).nglrGrammarDirectiveGrammarKWSetValues = nglrGrammarDirectiveGrammarKWSetValues();
				((NglrGrammarDirectiveGrammarKWSetContext)_localctx).valLit.add(((NglrGrammarDirectiveGrammarKWSetContext)_localctx).nglrGrammarDirectiveGrammarKWSetValues);
				}
				}
				setState(262);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(263); match(RIGHT_SQUARE_BRACKET);
			setState(264); match(SEMICOLUMN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NglrGrammarDirectiveGrammarKWSetValuesContext extends ParserRuleContext {
		public String ret;
		public Token ID_NAME;
		public List<Token> valLit = new ArrayList<Token>();
		public Token HYPHEN;
		public Token COMMA;
		public Token _tset706;
		public List<TerminalNode> HYPHEN() { return getTokens(NGLRParser.HYPHEN); }
		public TerminalNode ID_NAME(int i) {
			return getToken(NGLRParser.ID_NAME, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(NGLRParser.COMMA); }
		public TerminalNode HYPHEN(int i) {
			return getToken(NGLRParser.HYPHEN, i);
		}
		public List<TerminalNode> DOUBLE_QUOTE() { return getTokens(NGLRParser.DOUBLE_QUOTE); }
		public TerminalNode DOUBLE_QUOTE(int i) {
			return getToken(NGLRParser.DOUBLE_QUOTE, i);
		}
		public List<TerminalNode> ID_NAME() { return getTokens(NGLRParser.ID_NAME); }
		public TerminalNode COMMA(int i) {
			return getToken(NGLRParser.COMMA, i);
		}
		public NglrGrammarDirectiveGrammarKWSetValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammarDirectiveGrammarKWSetValues; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammarDirectiveGrammarKWSetValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammarDirectiveGrammarKWSetValues(this);
		}
	}

	public final NglrGrammarDirectiveGrammarKWSetValuesContext nglrGrammarDirectiveGrammarKWSetValues() throws RecognitionException {
		NglrGrammarDirectiveGrammarKWSetValuesContext _localctx = new NglrGrammarDirectiveGrammarKWSetValuesContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_nglrGrammarDirectiveGrammarKWSetValues);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266); match(DOUBLE_QUOTE);
			setState(268); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(267);
				((NglrGrammarDirectiveGrammarKWSetValuesContext)_localctx)._tset706 = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << COMMA) | (1L << ID_NAME))) != 0)) ) {
					((NglrGrammarDirectiveGrammarKWSetValuesContext)_localctx)._tset706 = (Token)_errHandler.recoverInline(this);
				}
				consume();
				((NglrGrammarDirectiveGrammarKWSetValuesContext)_localctx).valLit.add(((NglrGrammarDirectiveGrammarKWSetValuesContext)_localctx)._tset706);
				}
				}
				setState(270); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HYPHEN) | (1L << COMMA) | (1L << ID_NAME))) != 0) );
			((NglrGrammarDirectiveGrammarKWSetValuesContext)_localctx).ret = convertTokenListToString(((NglrGrammarDirectiveGrammarKWSetValuesContext)_localctx).valLit);
			setState(273); match(DOUBLE_QUOTE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NglrGrammarDirectiveFilterContext extends ParserRuleContext {
		public Token val;
		public TerminalNode SEMICOLUMN() { return getToken(NGLRParser.SEMICOLUMN, 0); }
		public TerminalNode ID_NAME() { return getToken(NGLRParser.ID_NAME, 0); }
		public TerminalNode HASH() { return getToken(NGLRParser.HASH, 0); }
		public NglrGrammarDirectiveFilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammarDirectiveFilter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammarDirectiveFilter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammarDirectiveFilter(this);
		}
	}

	public final NglrGrammarDirectiveFilterContext nglrGrammarDirectiveFilter() throws RecognitionException {
		NglrGrammarDirectiveFilterContext _localctx = new NglrGrammarDirectiveFilterContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_nglrGrammarDirectiveFilter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(275); match(HASH);
			setState(276); match(T__2);
			setState(277); ((NglrGrammarDirectiveFilterContext)_localctx).val = match(ID_NAME);
			setState(278); match(SEMICOLUMN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NglrGrammarDirectiveNoInterpretationContext extends ParserRuleContext {
		public TerminalNode SEMICOLUMN() { return getToken(NGLRParser.SEMICOLUMN, 0); }
		public TerminalNode HASH() { return getToken(NGLRParser.HASH, 0); }
		public NglrGrammarDirectiveNoInterpretationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nglrGrammarDirectiveNoInterpretation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).enterNglrGrammarDirectiveNoInterpretation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NGLRListener ) ((NGLRListener)listener).exitNglrGrammarDirectiveNoInterpretation(this);
		}
	}

	public final NglrGrammarDirectiveNoInterpretationContext nglrGrammarDirectiveNoInterpretation() throws RecognitionException {
		NglrGrammarDirectiveNoInterpretationContext _localctx = new NglrGrammarDirectiveNoInterpretationContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_nglrGrammarDirectiveNoInterpretation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(280); match(HASH);
			setState(281); match(T__4);
			setState(282); match(SEMICOLUMN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3+\u011f\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\7\2<\n\2\f\2\16\2?\13\2\3"+
		"\2\6\2B\n\2\r\2\16\2C\3\3\3\3\3\3\3\3\6\3J\n\3\r\3\16\3K\3\3\3\3\3\4\3"+
		"\4\3\4\3\4\3\4\5\4U\n\4\5\4W\n\4\3\5\3\5\3\5\3\5\7\5]\n\5\f\5\16\5`\13"+
		"\5\3\5\3\5\3\6\3\6\5\6f\n\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\5\to\n\t\3\t\5"+
		"\tr\n\t\3\n\3\n\5\nv\n\n\3\13\3\13\6\13z\n\13\r\13\16\13{\3\13\3\13\3"+
		"\13\3\f\3\f\3\f\3\f\5\f\u0085\n\f\3\f\3\f\3\f\3\f\5\f\u008b\n\f\7\f\u008d"+
		"\n\f\f\f\16\f\u0090\13\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\6\16\u0099\n\16"+
		"\r\16\16\16\u009a\3\16\3\16\3\17\3\17\6\17\u00a1\n\17\r\17\16\17\u00a2"+
		"\3\17\3\17\3\17\3\20\3\20\3\21\6\21\u00ab\n\21\r\21\16\21\u00ac\3\21\3"+
		"\21\3\22\3\22\3\22\3\22\3\22\3\22\3\23\6\23\u00b8\n\23\r\23\16\23\u00b9"+
		"\3\23\3\23\3\24\6\24\u00bf\n\24\r\24\16\24\u00c0\3\24\3\24\3\25\3\25\3"+
		"\25\3\25\7\25\u00c9\n\25\f\25\16\25\u00cc\13\25\3\25\3\25\3\26\6\26\u00d1"+
		"\n\26\r\26\16\26\u00d2\3\26\3\26\3\26\3\26\3\26\6\26\u00da\n\26\r\26\16"+
		"\26\u00db\5\26\u00de\n\26\5\26\u00e0\n\26\3\26\3\26\3\26\3\26\5\26\u00e6"+
		"\n\26\3\27\3\27\3\27\3\27\3\27\5\27\u00ed\n\27\3\30\3\30\3\30\3\30\3\30"+
		"\3\31\3\31\3\31\3\31\6\31\u00f8\n\31\r\31\16\31\u00f9\3\31\3\31\3\31\3"+
		"\32\3\32\3\32\3\32\3\32\3\32\7\32\u0105\n\32\f\32\16\32\u0108\13\32\3"+
		"\32\3\32\3\32\3\33\3\33\6\33\u010f\n\33\r\33\16\33\u0110\3\33\3\33\3\33"+
		"\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35\2\2\36\2\4\6\b\n\f"+
		"\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668\2\n\4\2\13\f\24\24\n"+
		"\2\t\f\17\17\24\24\27\30!!$$&&((\4\2\t\t((\b\2\t\f\23\32\35\36!!$&((\n"+
		"\2\t\t\r\r\17\17\23\23\27\27\31\32&&((\4\2\30\30((\6\2\t\t\27\30!\"(("+
		"\5\2\t\t\27\27((\u0125\2=\3\2\2\2\4E\3\2\2\2\6V\3\2\2\2\bX\3\2\2\2\ne"+
		"\3\2\2\2\fg\3\2\2\2\16j\3\2\2\2\20l\3\2\2\2\22s\3\2\2\2\24w\3\2\2\2\26"+
		"\u0080\3\2\2\2\30\u0093\3\2\2\2\32\u0098\3\2\2\2\34\u009e\3\2\2\2\36\u00a7"+
		"\3\2\2\2 \u00aa\3\2\2\2\"\u00b0\3\2\2\2$\u00b7\3\2\2\2&\u00be\3\2\2\2"+
		"(\u00c4\3\2\2\2*\u00d0\3\2\2\2,\u00ec\3\2\2\2.\u00ee\3\2\2\2\60\u00f3"+
		"\3\2\2\2\62\u00fe\3\2\2\2\64\u010c\3\2\2\2\66\u0115\3\2\2\28\u011a\3\2"+
		"\2\2:<\5,\27\2;:\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>A\3\2\2\2?=\3\2"+
		"\2\2@B\5\4\3\2A@\3\2\2\2BC\3\2\2\2CA\3\2\2\2CD\3\2\2\2D\3\3\2\2\2EF\5"+
		"\16\b\2FI\7\20\2\2GJ\5\6\4\2HJ\5\n\6\2IG\3\2\2\2IH\3\2\2\2JK\3\2\2\2K"+
		"I\3\2\2\2KL\3\2\2\2LM\3\2\2\2MN\7\21\2\2N\5\3\2\2\2OP\5\n\6\2PQ\5\f\7"+
		"\2QW\3\2\2\2RT\5\b\5\2SU\5\f\7\2TS\3\2\2\2TU\3\2\2\2UW\3\2\2\2VO\3\2\2"+
		"\2VR\3\2\2\2W\7\3\2\2\2XY\7\31\2\2Y^\5\n\6\2Z[\7\23\2\2[]\5\n\6\2\\Z\3"+
		"\2\2\2]`\3\2\2\2^\\\3\2\2\2^_\3\2\2\2_a\3\2\2\2`^\3\2\2\2ab\7\32\2\2b"+
		"\t\3\2\2\2cf\5\22\n\2df\5\20\t\2ec\3\2\2\2ed\3\2\2\2f\13\3\2\2\2gh\t\2"+
		"\2\2hi\b\7\1\2i\r\3\2\2\2jk\7(\2\2k\17\3\2\2\2ln\7(\2\2mo\5\26\f\2nm\3"+
		"\2\2\2no\3\2\2\2oq\3\2\2\2pr\5(\25\2qp\3\2\2\2qr\3\2\2\2r\21\3\2\2\2s"+
		"u\5\24\13\2tv\5\26\f\2ut\3\2\2\2uv\3\2\2\2v\23\3\2\2\2wy\7\22\2\2xz\t"+
		"\3\2\2yx\3\2\2\2z{\3\2\2\2{y\3\2\2\2{|\3\2\2\2|}\3\2\2\2}~\b\13\1\2~\177"+
		"\7\22\2\2\177\25\3\2\2\2\u0080\u0084\7\33\2\2\u0081\u0085\5\36\20\2\u0082"+
		"\u0085\5\30\r\2\u0083\u0085\5\"\22\2\u0084\u0081\3\2\2\2\u0084\u0082\3"+
		"\2\2\2\u0084\u0083\3\2\2\2\u0085\u008e\3\2\2\2\u0086\u008a\7\27\2\2\u0087"+
		"\u008b\5\36\20\2\u0088\u008b\5\30\r\2\u0089\u008b\5\"\22\2\u008a\u0087"+
		"\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u0089\3\2\2\2\u008b\u008d\3\2\2\2\u008c"+
		"\u0086\3\2\2\2\u008d\u0090\3\2\2\2\u008e\u008c\3\2\2\2\u008e\u008f\3\2"+
		"\2\2\u008f\u0091\3\2\2\2\u0090\u008e\3\2\2\2\u0091\u0092\7\34\2\2\u0092"+
		"\27\3\2\2\2\u0093\u0094\5\32\16\2\u0094\u0095\7\16\2\2\u0095\u0096\5\34"+
		"\17\2\u0096\31\3\2\2\2\u0097\u0099\t\4\2\2\u0098\u0097\3\2\2\2\u0099\u009a"+
		"\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b\u009c\3\2\2\2\u009c"+
		"\u009d\b\16\1\2\u009d\33\3\2\2\2\u009e\u00a0\7\r\2\2\u009f\u00a1\t\5\2"+
		"\2\u00a0\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a3"+
		"\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00a5\b\17\1\2\u00a5\u00a6\7\r\2\2"+
		"\u00a6\35\3\2\2\2\u00a7\u00a8\5 \21\2\u00a8\37\3\2\2\2\u00a9\u00ab\t\4"+
		"\2\2\u00aa\u00a9\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac\u00aa\3\2\2\2\u00ac"+
		"\u00ad\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00af\b\21\1\2\u00af!\3\2\2\2"+
		"\u00b0\u00b1\5$\23\2\u00b1\u00b2\7\16\2\2\u00b2\u00b3\7\35\2\2\u00b3\u00b4"+
		"\5&\24\2\u00b4\u00b5\7\36\2\2\u00b5#\3\2\2\2\u00b6\u00b8\t\4\2\2\u00b7"+
		"\u00b6\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\u00b7\3\2\2\2\u00b9\u00ba\3\2"+
		"\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00bc\b\23\1\2\u00bc%\3\2\2\2\u00bd\u00bf"+
		"\t\6\2\2\u00be\u00bd\3\2\2\2\u00bf\u00c0\3\2\2\2\u00c0\u00be\3\2\2\2\u00c0"+
		"\u00c1\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c3\b\24\1\2\u00c3\'\3\2\2"+
		"\2\u00c4\u00c5\7\25\2\2\u00c5\u00ca\5*\26\2\u00c6\u00c7\7\27\2\2\u00c7"+
		"\u00c9\5*\26\2\u00c8\u00c6\3\2\2\2\u00c9\u00cc\3\2\2\2\u00ca\u00c8\3\2"+
		"\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00cd\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cd"+
		"\u00ce\7\26\2\2\u00ce)\3\2\2\2\u00cf\u00d1\t\7\2\2\u00d0\u00cf\3\2\2\2"+
		"\u00d1\u00d2\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3\u00df"+
		"\3\2\2\2\u00d4\u00dd\7\16\2\2\u00d5\u00d6\7\22\2\2\u00d6\u00d7\7(\2\2"+
		"\u00d7\u00de\7\22\2\2\u00d8\u00da\t\7\2\2\u00d9\u00d8\3\2\2\2\u00da\u00db"+
		"\3\2\2\2\u00db\u00d9\3\2\2\2\u00db\u00dc\3\2\2\2\u00dc\u00de\3\2\2\2\u00dd"+
		"\u00d5\3\2\2\2\u00dd\u00d9\3\2\2\2\u00de\u00e0\3\2\2\2\u00df\u00d4\3\2"+
		"\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e5\3\2\2\2\u00e1\u00e2\7\33\2\2\u00e2"+
		"\u00e3\5 \21\2\u00e3\u00e4\7\34\2\2\u00e4\u00e6\3\2\2\2\u00e5\u00e1\3"+
		"\2\2\2\u00e5\u00e6\3\2\2\2\u00e6+\3\2\2\2\u00e7\u00ed\5\60\31\2\u00e8"+
		"\u00ed\5.\30\2\u00e9\u00ed\5\62\32\2\u00ea\u00ed\5\66\34\2\u00eb\u00ed"+
		"\58\35\2\u00ec\u00e7\3\2\2\2\u00ec\u00e8\3\2\2\2\u00ec\u00e9\3\2\2\2\u00ec"+
		"\u00ea\3\2\2\2\u00ec\u00eb\3\2\2\2\u00ed-\3\2\2\2\u00ee\u00ef\7\b\2\2"+
		"\u00ef\u00f0\7\4\2\2\u00f0\u00f1\7(\2\2\u00f1\u00f2\7\21\2\2\u00f2/\3"+
		"\2\2\2\u00f3\u00f4\7\b\2\2\u00f4\u00f5\7\6\2\2\u00f5\u00f7\7\r\2\2\u00f6"+
		"\u00f8\t\b\2\2\u00f7\u00f6\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00f7\3\2"+
		"\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00fc\7\r\2\2\u00fc"+
		"\u00fd\7\21\2\2\u00fd\61\3\2\2\2\u00fe\u00ff\7\b\2\2\u00ff\u0100\7\7\2"+
		"\2\u0100\u0101\7\35\2\2\u0101\u0106\5\64\33\2\u0102\u0103\7\27\2\2\u0103"+
		"\u0105\5\64\33\2\u0104\u0102\3\2\2\2\u0105\u0108\3\2\2\2\u0106\u0104\3"+
		"\2\2\2\u0106\u0107\3\2\2\2\u0107\u0109\3\2\2\2\u0108\u0106\3\2\2\2\u0109"+
		"\u010a\7\36\2\2\u010a\u010b\7\21\2\2\u010b\63\3\2\2\2\u010c\u010e\7\r"+
		"\2\2\u010d\u010f\t\t\2\2\u010e\u010d\3\2\2\2\u010f\u0110\3\2\2\2\u0110"+
		"\u010e\3\2\2\2\u0110\u0111\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0113\b\33"+
		"\1\2\u0113\u0114\7\r\2\2\u0114\65\3\2\2\2\u0115\u0116\7\b\2\2\u0116\u0117"+
		"\7\5\2\2\u0117\u0118\7(\2\2\u0118\u0119\7\21\2\2\u0119\67\3\2\2\2\u011a"+
		"\u011b\7\b\2\2\u011b\u011c\7\3\2\2\u011c\u011d\7\21\2\2\u011d9\3\2\2\2"+
		" =CIKTV^enqu{\u0084\u008a\u008e\u009a\u00a2\u00ac\u00b9\u00c0\u00ca\u00d2"+
		"\u00db\u00dd\u00df\u00e5\u00ec\u00f9\u0106\u0110";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}