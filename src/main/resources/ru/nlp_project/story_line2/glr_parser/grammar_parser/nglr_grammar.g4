grammar nglr_grammar;

options {
	language=Java;
	//charVocabulary='\u0000'..'\uFFFE'; 
}


@parser::header {
package ru.nlp_project.story_line2.glr_parser.grammar_parser;
}
        
@lexer::header {
package ru.nlp_project.story_line2.glr_parser.grammar_parser;
}

            
@parser::members {
  private String convertTokenListToString(List<Token> valLit) {
	  String result = "";
    for (Token t: valLit)
	    result+=t.getText();
    return result;
  }
}


nglrGrammar
    :  (nglrGrammarDirective)* (projection)+;

// S -> Adj Verb 'одинарная'
projection
	:lhs=nonTerminal ARROW 
        (regularExpression | nonRegularExpression )+ 
        SEMICOLUMN;	

// something in RE format
// MoscowWord<rt,gnc—agr[1]>*, MoscowWord<rt,gnc—agr[1]>
regularExpression
    : (nonRegularExpression quant=regQuantifier|
            regularGroupExpression quant=regQuantifier?);

// (entry1|entry2)
regularGroupExpression
    : LEFT_BRACKET nonRegularExpression 
        (VERTICAL_BAR nonRegularExpression)* RIGHT_BRACKET;

// nonterminal
// 'москва'<rt,gnc—agr[1]>
// MoscowWord<rt,gnc—agr[1]>
nonRegularExpression 
    : literalStringExt | nonTerminalExt ;

regQuantifier returns [String ret]
    : val=(STAR|QUESTION|PLUS) {$ret = $val.text;};

// SomeArg
nonTerminal
	:name=ID_NAME;

// Adj<gnc—agr[1]> MoscowWord<rt,gnc—agr[1]>
// Adj<gnc—agr[1]>{Stuff.Field1, Stuff.Field1 = Foo.Bar} MoscowWord<rt,gnc—agr[1]>{Stuff.Field1='болото'}
nonTerminalExt
    :name=ID_NAME extData? interpData?;

// 'москва' <h—reg1>
literalStringExt
	:value=literalString ext=extData? ;

literalString returns [String ret]
//	:SINGLE_QUOTE valLit=ID_NAME {$ret=$valLit.text;} SINGLE_QUOTE ;
	:SINGLE_QUOTE valLit+=(ID_NAME|HYPHEN|UNDERSCORE|NUMBER|COMMA|POINT|BACKSLASH|QUESTION|PLUS|STAR|COLON|AMPERSAND)+ {$ret=convertTokenListToString($valLit);}  SINGLE_QUOTE ;


// <rt,gnc—agr[1]>
extData 
    :LEFT_ANGLE_BRACKET 
        (extDataLabel|extDataParam|extDataArray)
        (COMMA (extDataLabel|extDataParam|extDataArray))*
        RIGHT_ANGLE_BRACKET;

// ограничивающие/дополняющие параметры в виде параметров (extData="value")
// kwtype="животные_центральной_африки"
// gram="gen,pl"
// kwtype="статья1" 

extDataParam
    :name=extDataParamName EQUAL value=extDataParamValue;

extDataParamName returns [String ret]
    : valLit+=(ID_NAME|HYPHEN)+ {$ret=convertTokenListToString($valLit);}  ;

// "some-val"
// "им\." (дополнительные символы для регулярных выражений)
extDataParamValue returns [String ret]
    :DOUBLE_QUOTE valLit+=(ID_NAME|HYPHEN|UNDERSCORE|NUMBER|COMMA|POINT|BACKSLASH|QUESTION|PLUS|STAR|COLON|CARET|LEFT_BRACKET|RIGHT_BRACKET|LEFT_SQUARE_BRACKET|RIGHT_SQUARE_BRACKET|LEFT_CURVE_BRACKET|RIGHT_CURVE_BRACKET|VERTICAL_BAR)+ {$ret=convertTokenListToString($valLit);}  DOUBLE_QUOTE;

// ограничивающие/дополняющие параметры в виде меток (extDataLabel)
// no-hom
// h-reg1
// l-quoted
extDataLabel
    :name=extDataLabelName;

extDataLabelName returns [String ret]
    :valLit+=(ID_NAME|HYPHEN)+ {$ret=convertTokenListToString($valLit);};

// ограничивающие/дополняющие параметры в виде массивов (extData[])
// gnc-arg=[1]
// kwset=["статья_1","статья_2",…,"статья_n"] 
// kwsetf=["статья1"]
// GU=[nom,pl]
// GU=[(sg,ins)|&(nom,acc,gen,dat,ins)]
extDataArray
    :name=extDataArrayName EQUAL LEFT_SQUARE_BRACKET value=extDataArrayValue RIGHT_SQUARE_BRACKET;

// gnc-arg
extDataArrayName returns [String ret]
    :valLit+=(ID_NAME|HYPHEN)+ {$ret=convertTokenListToString($valLit);};

// [1]
// some value
extDataArrayValue returns [String ret]
    :valLit+=(ID_NAME|HYPHEN|NUMBER|LEFT_BRACKET|RIGHT_BRACKET|AMPERSAND|VERTICAL_BAR|DOUBLE_QUOTE|COMMA)+ {$ret=convertTokenListToString($valLit);};

// {Stuff.Field1, Stuff.Field1 = Foo.Bar}
// {Stuff.Field1='болото'}
interpData
    : LEFT_CURVE_BRACKET interpDataEntry (COMMA interpDataEntry)* RIGHT_CURVE_BRACKET;

// Stuff.Field1
// Stuff.Field1 = Foo.Bar
// Stuff.Field1='болото'
// Stuff.Field1 <no-norm>
// Stuff.Field1 = Foo.Bar <no-norm>
interpDataEntry
    : nameVal+=(ID_NAME|POINT)+ (EQUAL (SINGLE_QUOTE literalVal=ID_NAME SINGLE_QUOTE| factVal+=(ID_NAME|POINT)+))? 
    (LEFT_ANGLE_BRACKET paramName=extDataLabelName RIGHT_ANGLE_BRACKET)?;

// описание параметров грамматики
nglrGrammarDirective
    : (
            nglrGrammarDirectiveInclude|
            nglrGrammarDirectiveGrammarRoot|
            nglrGrammarDirectiveGrammarKWSet|
            nglrGrammarDirectiveFilter|
            nglrGrammarDirectiveNoInterpretation
        );

// #ROOT_SYMBOL RootSymbol;
nglrGrammarDirectiveGrammarRoot
    : HASH 'ROOT_SYMBOL' val=ID_NAME SEMICOLUMN;

// #INCLUDE  "./inc.file";
nglrGrammarDirectiveInclude
    : HASH 'INCLUDE' DOUBLE_QUOTE valLit+=(ID_NAME|HYPHEN|COMMA|POINT|BACKSLASH|SLASH)+ DOUBLE_QUOTE SEMICOLUMN;

// #GRAMMAR_KWSET ["artucle_name1", "article_anme2"];
nglrGrammarDirectiveGrammarKWSet
    :HASH 'GRAMMAR_KWSET' LEFT_SQUARE_BRACKET valLit+=nglrGrammarDirectiveGrammarKWSetValues (COMMA valLit+=nglrGrammarDirectiveGrammarKWSetValues)* RIGHT_SQUARE_BRACKET SEMICOLUMN;

nglrGrammarDirectiveGrammarKWSetValues returns [String ret]
    :DOUBLE_QUOTE valLit+=(ID_NAME|HYPHEN|COMMA)+ {$ret=convertTokenListToString($valLit);}  DOUBLE_QUOTE;

// #FILTER some...;
nglrGrammarDirectiveFilter
    :HASH 'FILTER' val=ID_NAME SEMICOLUMN;

// #NO_INTERPRETATION;
nglrGrammarDirectiveNoInterpretation
    :HASH 'NO_INTERPRETATION' SEMICOLUMN;

HASH
    :'#';
HYPHEN
    :'-';
UNDERSCORE
    :'_';
QUESTION
    :'?';
STAR
    :'*';
DOUBLE_QUOTE
    :'"';
EQUAL
    :'=';
AMPERSAND
    :'&';
ARROW
	:	'->';
SEMICOLUMN
	:	';';	
SINGLE_QUOTE
	:	'\'';
VERTICAL_BAR
	:	'|';
PLUS
	:	'+';
LEFT_CURVE_BRACKET
	:	'{';
RIGHT_CURVE_BRACKET
	:	'}';
COMMA	
	:',';
POINT
    :'.';
LEFT_BRACKET
	:	'(';
RIGHT_BRACKET
	:	')';
LEFT_ANGLE_BRACKET
	:	'<';
RIGHT_ANGLE_BRACKET
	:	'>';
LEFT_SQUARE_BRACKET
	:	'[';
RIGHT_SQUARE_BRACKET
	:	']';
TILDE
	:	'~';
EXCLAMATION
	:	'!';
BACKSLASH 	
	:	'\\';
SLASH 	
	:	'/';
DOUBLE_SLASH
    : '//';
COLON
	:	':';
CARET
	:	'^';
NUMBER
	:	DIGIT+;
fragment	
DIGIT
	:	'0'..'9' ; 	
fragment
CHAR_ENG_SEQ
	:	(CHAR_ENG)+;
	
fragment 
CHAR_ENG
	:	('a'..'z'|'A'..'Z');

WS  
    :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        )+ -> skip;

fragment 
UPPER_CHAR_RUS
	:	('\u0410'..'\u042F');
fragment 
LOWER_CHAR_RUS
	:	('\u0430'..'\u044F');

fragment
CHAR_RUS_SEQ
	:	(UPPER_CHAR_RUS|LOWER_CHAR_RUS)+;
ID_NAME
    :(CHAR_RUS_SEQ|NUMBER|CHAR_ENG_SEQ|UNDERSCORE)+;

NEW_LINE
    :  '\r'? '\n';

ML_COMMENT
    :   '/*' .*? '*/' -> skip;

SL_COMMENT
    :   DOUBLE_SLASH .*? NEW_LINE+  -> skip;




