// Generated from src/main/resources/ru/nlp_project/story_line2/glr_parser/grammar_parser/NGLR.g4 by ANTLR 4.3

package ru.nlp_project.story_line2.glr_parser.grammar_parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NGLRLexer extends Lexer {
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
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'", "'\\u0012'", 
		"'\\u0013'", "'\\u0014'", "'\\u0015'", "'\\u0016'", "'\\u0017'", "'\\u0018'", 
		"'\\u0019'", "'\\u001A'", "'\\u001B'", "'\\u001C'", "'\\u001D'", "'\\u001E'", 
		"'\\u001F'", "' '", "'!'", "'\"'", "'#'", "'$'", "'%'", "'&'", "'''", 
		"'('", "')'"
	};
	public static final String[] ruleNames = {
		"T__4", "T__3", "T__2", "T__1", "T__0", "HASH", "HYPHEN", "UNDERSCORE", 
		"QUESTION", "STAR", "DOUBLE_QUOTE", "EQUAL", "AMPERSAND", "ARROW", "SEMICOLUMN", 
		"SINGLE_QUOTE", "VERTICAL_BAR", "PLUS", "LEFT_CURVE_BRACKET", "RIGHT_CURVE_BRACKET", 
		"COMMA", "POINT", "LEFT_BRACKET", "RIGHT_BRACKET", "LEFT_ANGLE_BRACKET", 
		"RIGHT_ANGLE_BRACKET", "LEFT_SQUARE_BRACKET", "RIGHT_SQUARE_BRACKET", 
		"TILDE", "EXCLAMATION", "BACKSLASH", "SLASH", "DOUBLE_SLASH", "COLON", 
		"CARET", "NUMBER", "DIGIT", "CHAR_ENG_SEQ", "CHAR_ENG", "WS", "UPPER_CHAR_RUS", 
		"LOWER_CHAR_RUS", "CHAR_RUS_SEQ", "ID_NAME", "NEW_LINE", "ML_COMMENT", 
		"SL_COMMENT"
	};


	public NGLRLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NGLR.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2+\u0122\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3"+
		"\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b"+
		"\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17"+
		"\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26"+
		"\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35"+
		"\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3\"\3#\3#\3$\3$\3%\6%\u00dc\n"+
		"%\r%\16%\u00dd\3&\3&\3\'\6\'\u00e3\n\'\r\'\16\'\u00e4\3(\3(\3)\6)\u00ea"+
		"\n)\r)\16)\u00eb\3)\3)\3*\3*\3+\3+\3,\3,\6,\u00f6\n,\r,\16,\u00f7\3-\3"+
		"-\3-\3-\6-\u00fe\n-\r-\16-\u00ff\3.\5.\u0103\n.\3.\3.\3/\3/\3/\3/\7/\u010b"+
		"\n/\f/\16/\u010e\13/\3/\3/\3/\3/\3/\3\60\3\60\7\60\u0117\n\60\f\60\16"+
		"\60\u011a\13\60\3\60\6\60\u011d\n\60\r\60\16\60\u011e\3\60\3\60\4\u010c"+
		"\u0118\2\61\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33"+
		"\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67"+
		"\359\36;\37= ?!A\"C#E$G%I&K\2M\2O\2Q\'S\2U\2W\2Y([)]*_+\3\2\4\4\2C\\c"+
		"|\5\2\13\f\17\17\"\"\u0128\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2"+
		"\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2"+
		"\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3"+
		"\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2"+
		"\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67"+
		"\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2"+
		"\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2Q\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2"+
		"\2]\3\2\2\2\2_\3\2\2\2\3a\3\2\2\2\5s\3\2\2\2\7\177\3\2\2\2\t\u0086\3\2"+
		"\2\2\13\u008e\3\2\2\2\r\u009c\3\2\2\2\17\u009e\3\2\2\2\21\u00a0\3\2\2"+
		"\2\23\u00a2\3\2\2\2\25\u00a4\3\2\2\2\27\u00a6\3\2\2\2\31\u00a8\3\2\2\2"+
		"\33\u00aa\3\2\2\2\35\u00ac\3\2\2\2\37\u00af\3\2\2\2!\u00b1\3\2\2\2#\u00b3"+
		"\3\2\2\2%\u00b5\3\2\2\2\'\u00b7\3\2\2\2)\u00b9\3\2\2\2+\u00bb\3\2\2\2"+
		"-\u00bd\3\2\2\2/\u00bf\3\2\2\2\61\u00c1\3\2\2\2\63\u00c3\3\2\2\2\65\u00c5"+
		"\3\2\2\2\67\u00c7\3\2\2\29\u00c9\3\2\2\2;\u00cb\3\2\2\2=\u00cd\3\2\2\2"+
		"?\u00cf\3\2\2\2A\u00d1\3\2\2\2C\u00d3\3\2\2\2E\u00d6\3\2\2\2G\u00d8\3"+
		"\2\2\2I\u00db\3\2\2\2K\u00df\3\2\2\2M\u00e2\3\2\2\2O\u00e6\3\2\2\2Q\u00e9"+
		"\3\2\2\2S\u00ef\3\2\2\2U\u00f1\3\2\2\2W\u00f5\3\2\2\2Y\u00fd\3\2\2\2["+
		"\u0102\3\2\2\2]\u0106\3\2\2\2_\u0114\3\2\2\2ab\7P\2\2bc\7Q\2\2cd\7a\2"+
		"\2de\7K\2\2ef\7P\2\2fg\7V\2\2gh\7G\2\2hi\7T\2\2ij\7R\2\2jk\7T\2\2kl\7"+
		"G\2\2lm\7V\2\2mn\7C\2\2no\7V\2\2op\7K\2\2pq\7Q\2\2qr\7P\2\2r\4\3\2\2\2"+
		"st\7T\2\2tu\7Q\2\2uv\7Q\2\2vw\7V\2\2wx\7a\2\2xy\7U\2\2yz\7[\2\2z{\7O\2"+
		"\2{|\7D\2\2|}\7Q\2\2}~\7N\2\2~\6\3\2\2\2\177\u0080\7H\2\2\u0080\u0081"+
		"\7K\2\2\u0081\u0082\7N\2\2\u0082\u0083\7V\2\2\u0083\u0084\7G\2\2\u0084"+
		"\u0085\7T\2\2\u0085\b\3\2\2\2\u0086\u0087\7K\2\2\u0087\u0088\7P\2\2\u0088"+
		"\u0089\7E\2\2\u0089\u008a\7N\2\2\u008a\u008b\7W\2\2\u008b\u008c\7F\2\2"+
		"\u008c\u008d\7G\2\2\u008d\n\3\2\2\2\u008e\u008f\7I\2\2\u008f\u0090\7T"+
		"\2\2\u0090\u0091\7C\2\2\u0091\u0092\7O\2\2\u0092\u0093\7O\2\2\u0093\u0094"+
		"\7C\2\2\u0094\u0095\7T\2\2\u0095\u0096\7a\2\2\u0096\u0097\7M\2\2\u0097"+
		"\u0098\7Y\2\2\u0098\u0099\7U\2\2\u0099\u009a\7G\2\2\u009a\u009b\7V\2\2"+
		"\u009b\f\3\2\2\2\u009c\u009d\7%\2\2\u009d\16\3\2\2\2\u009e\u009f\7/\2"+
		"\2\u009f\20\3\2\2\2\u00a0\u00a1\7a\2\2\u00a1\22\3\2\2\2\u00a2\u00a3\7"+
		"A\2\2\u00a3\24\3\2\2\2\u00a4\u00a5\7,\2\2\u00a5\26\3\2\2\2\u00a6\u00a7"+
		"\7$\2\2\u00a7\30\3\2\2\2\u00a8\u00a9\7?\2\2\u00a9\32\3\2\2\2\u00aa\u00ab"+
		"\7(\2\2\u00ab\34\3\2\2\2\u00ac\u00ad\7/\2\2\u00ad\u00ae\7@\2\2\u00ae\36"+
		"\3\2\2\2\u00af\u00b0\7=\2\2\u00b0 \3\2\2\2\u00b1\u00b2\7)\2\2\u00b2\""+
		"\3\2\2\2\u00b3\u00b4\7~\2\2\u00b4$\3\2\2\2\u00b5\u00b6\7-\2\2\u00b6&\3"+
		"\2\2\2\u00b7\u00b8\7}\2\2\u00b8(\3\2\2\2\u00b9\u00ba\7\177\2\2\u00ba*"+
		"\3\2\2\2\u00bb\u00bc\7.\2\2\u00bc,\3\2\2\2\u00bd\u00be\7\60\2\2\u00be"+
		".\3\2\2\2\u00bf\u00c0\7*\2\2\u00c0\60\3\2\2\2\u00c1\u00c2\7+\2\2\u00c2"+
		"\62\3\2\2\2\u00c3\u00c4\7>\2\2\u00c4\64\3\2\2\2\u00c5\u00c6\7@\2\2\u00c6"+
		"\66\3\2\2\2\u00c7\u00c8\7]\2\2\u00c88\3\2\2\2\u00c9\u00ca\7_\2\2\u00ca"+
		":\3\2\2\2\u00cb\u00cc\7\u0080\2\2\u00cc<\3\2\2\2\u00cd\u00ce\7#\2\2\u00ce"+
		">\3\2\2\2\u00cf\u00d0\7^\2\2\u00d0@\3\2\2\2\u00d1\u00d2\7\61\2\2\u00d2"+
		"B\3\2\2\2\u00d3\u00d4\7\61\2\2\u00d4\u00d5\7\61\2\2\u00d5D\3\2\2\2\u00d6"+
		"\u00d7\7<\2\2\u00d7F\3\2\2\2\u00d8\u00d9\7`\2\2\u00d9H\3\2\2\2\u00da\u00dc"+
		"\5K&\2\u00db\u00da\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd"+
		"\u00de\3\2\2\2\u00deJ\3\2\2\2\u00df\u00e0\4\62;\2\u00e0L\3\2\2\2\u00e1"+
		"\u00e3\5O(\2\u00e2\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e2\3\2\2"+
		"\2\u00e4\u00e5\3\2\2\2\u00e5N\3\2\2\2\u00e6\u00e7\t\2\2\2\u00e7P\3\2\2"+
		"\2\u00e8\u00ea\t\3\2\2\u00e9\u00e8\3\2\2\2\u00ea\u00eb\3\2\2\2\u00eb\u00e9"+
		"\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\u00ee\b)\2\2\u00ee"+
		"R\3\2\2\2\u00ef\u00f0\4\u0412\u0431\2\u00f0T\3\2\2\2\u00f1\u00f2\4\u0432"+
		"\u0451\2\u00f2V\3\2\2\2\u00f3\u00f6\5S*\2\u00f4\u00f6\5U+\2\u00f5\u00f3"+
		"\3\2\2\2\u00f5\u00f4\3\2\2\2\u00f6\u00f7\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f7"+
		"\u00f8\3\2\2\2\u00f8X\3\2\2\2\u00f9\u00fe\5W,\2\u00fa\u00fe\5I%\2\u00fb"+
		"\u00fe\5M\'\2\u00fc\u00fe\5\21\t\2\u00fd\u00f9\3\2\2\2\u00fd\u00fa\3\2"+
		"\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fc\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff"+
		"\u00fd\3\2\2\2\u00ff\u0100\3\2\2\2\u0100Z\3\2\2\2\u0101\u0103\7\17\2\2"+
		"\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2\2\u0103\u0104\3\2\2\2\u0104\u0105"+
		"\7\f\2\2\u0105\\\3\2\2\2\u0106\u0107\7\61\2\2\u0107\u0108\7,\2\2\u0108"+
		"\u010c\3\2\2\2\u0109\u010b\13\2\2\2\u010a\u0109\3\2\2\2\u010b\u010e\3"+
		"\2\2\2\u010c\u010d\3\2\2\2\u010c\u010a\3\2\2\2\u010d\u010f\3\2\2\2\u010e"+
		"\u010c\3\2\2\2\u010f\u0110\7,\2\2\u0110\u0111\7\61\2\2\u0111\u0112\3\2"+
		"\2\2\u0112\u0113\b/\2\2\u0113^\3\2\2\2\u0114\u0118\5C\"\2\u0115\u0117"+
		"\13\2\2\2\u0116\u0115\3\2\2\2\u0117\u011a\3\2\2\2\u0118\u0119\3\2\2\2"+
		"\u0118\u0116\3\2\2\2\u0119\u011c\3\2\2\2\u011a\u0118\3\2\2\2\u011b\u011d"+
		"\5[.\2\u011c\u011b\3\2\2\2\u011d\u011e\3\2\2\2\u011e\u011c\3\2\2\2\u011e"+
		"\u011f\3\2\2\2\u011f\u0120\3\2\2\2\u0120\u0121\b\60\2\2\u0121`\3\2\2\2"+
		"\16\2\u00dd\u00e4\u00eb\u00f5\u00f7\u00fd\u00ff\u0102\u010c\u0118\u011e"+
		"\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}