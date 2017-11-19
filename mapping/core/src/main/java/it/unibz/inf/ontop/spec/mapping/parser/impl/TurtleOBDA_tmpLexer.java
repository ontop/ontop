// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/java/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA_tmp.g4 by ANTLR 4.7
package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.term.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TurtleOBDA_tmpLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, BASE=2, PREFIX=3, FALSE=4, TRUE=5, REFERENCE=6, LTSIGN=7, RTSIGN=8, 
		SEMI=9, PERIOD=10, COMMA=11, LSQ_BRACKET=12, RSQ_BRACKET=13, LCR_BRACKET=14, 
		RCR_BRACKET=15, LPAREN=16, RPAREN=17, QUESTION=18, DOLLAR=19, QUOTE_DOUBLE=20, 
		QUOTE_SINGLE=21, APOSTROPHE=22, UNDERSCORE=23, MINUS=24, ASTERISK=25, 
		AMPERSAND=26, AT=27, EXCLAMATION=28, HASH=29, PERCENT=30, PLUS=31, EQUALS=32, 
		COLON=33, LESS=34, GREATER=35, SLASH=36, DOUBLE_SLASH=37, BACKSLASH=38, 
		BLANK=39, BLANK_PREFIX=40, TILDE=41, CARET=42, INTEGER=43, DOUBLE=44, 
		DECIMAL=45, INTEGER_POSITIVE=46, INTEGER_NEGATIVE=47, DOUBLE_POSITIVE=48, 
		DOUBLE_NEGATIVE=49, DECIMAL_POSITIVE=50, DECIMAL_NEGATIVE=51, VARNAME=52, 
		NCNAME=53, NCNAME_EXT=54, NAMESPACE=55, PREFIXED_NAME=56, STRING_WITH_QUOTE=57, 
		STRING_WITH_QUOTE_DOUBLE=58, STRING_WITH_BRACKET=59, STRING_WITH_CURLY_BRACKET=60, 
		STRING_URI=61, WS=62;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "BASE", "PREFIX", "FALSE", "TRUE", "REFERENCE", "LTSIGN", "RTSIGN", 
		"SEMI", "PERIOD", "COMMA", "LSQ_BRACKET", "RSQ_BRACKET", "LCR_BRACKET", 
		"RCR_BRACKET", "LPAREN", "RPAREN", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", 
		"QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "MINUS", "ASTERISK", "AMPERSAND", 
		"AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "EQUALS", "COLON", "LESS", 
		"GREATER", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "BLANK", "BLANK_PREFIX", 
		"TILDE", "CARET", "ALPHA", "DIGIT", "ALPHANUM", "CHAR", "INTEGER", "DOUBLE", 
		"DECIMAL", "INTEGER_POSITIVE", "INTEGER_NEGATIVE", "DOUBLE_POSITIVE", 
		"DOUBLE_NEGATIVE", "DECIMAL_POSITIVE", "DECIMAL_NEGATIVE", "VARNAME", 
		"ECHAR", "SCHEMA", "URI_PATH", "ID_START", "ID_CORE", "ID", "NAME_START_CHAR", 
		"NAME_CHAR", "NCNAME", "NCNAME_EXT", "NAMESPACE", "PREFIXED_NAME", "STRING_WITH_QUOTE", 
		"STRING_WITH_QUOTE_DOUBLE", "STRING_WITH_BRACKET", "STRING_WITH_CURLY_BRACKET", 
		"STRING_URI", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'a'", null, null, null, null, "'^^'", "'<\"'", "'\">'", "';'", 
		"'.'", "','", "'['", "']'", "'{'", "'}'", "'('", "')'", "'?'", "'$'", 
		"'\"'", "'''", "'`'", "'_'", "'-'", "'*'", "'&'", "'@'", "'!'", "'#'", 
		"'%'", "'+'", "'='", "':'", "'<'", "'>'", "'/'", "'//'", "'\\'", "'[]'", 
		"'_:'", "'~'", "'^'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "BASE", "PREFIX", "FALSE", "TRUE", "REFERENCE", "LTSIGN", 
		"RTSIGN", "SEMI", "PERIOD", "COMMA", "LSQ_BRACKET", "RSQ_BRACKET", "LCR_BRACKET", 
		"RCR_BRACKET", "LPAREN", "RPAREN", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", 
		"QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "MINUS", "ASTERISK", "AMPERSAND", 
		"AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "EQUALS", "COLON", "LESS", 
		"GREATER", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "BLANK", "BLANK_PREFIX", 
		"TILDE", "CARET", "INTEGER", "DOUBLE", "DECIMAL", "INTEGER_POSITIVE", 
		"INTEGER_NEGATIVE", "DOUBLE_POSITIVE", "DOUBLE_NEGATIVE", "DECIMAL_POSITIVE", 
		"DECIMAL_NEGATIVE", "VARNAME", "NCNAME", "NCNAME_EXT", "NAMESPACE", "PREFIXED_NAME", 
		"STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "STRING_WITH_BRACKET", 
		"STRING_WITH_CURLY_BRACKET", "STRING_URI", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public TurtleOBDA_tmpLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "TurtleOBDA_tmp.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2@\u020a\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b"+
		"\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20"+
		"\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27"+
		"\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36"+
		"\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3&\3\'\3\'"+
		"\3(\3(\3(\3)\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\5.\u0107\n.\3/\3/\3/"+
		"\3/\5/\u010d\n/\3\60\6\60\u0110\n\60\r\60\16\60\u0111\3\61\6\61\u0115"+
		"\n\61\r\61\16\61\u0116\3\61\3\61\7\61\u011b\n\61\f\61\16\61\u011e\13\61"+
		"\3\61\3\61\5\61\u0122\n\61\3\61\7\61\u0125\n\61\f\61\16\61\u0128\13\61"+
		"\3\61\3\61\6\61\u012c\n\61\r\61\16\61\u012d\3\61\3\61\5\61\u0132\n\61"+
		"\3\61\7\61\u0135\n\61\f\61\16\61\u0138\13\61\3\61\6\61\u013b\n\61\r\61"+
		"\16\61\u013c\3\61\3\61\5\61\u0141\n\61\3\61\7\61\u0144\n\61\f\61\16\61"+
		"\u0147\13\61\5\61\u0149\n\61\3\62\6\62\u014c\n\62\r\62\16\62\u014d\3\62"+
		"\3\62\6\62\u0152\n\62\r\62\16\62\u0153\3\62\3\62\6\62\u0158\n\62\r\62"+
		"\16\62\u0159\5\62\u015c\n\62\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3"+
		"\65\3\66\3\66\3\66\3\67\3\67\3\67\38\38\38\39\39\79\u0172\n9\f9\169\u0175"+
		"\139\3:\3:\3:\3;\3;\3;\3;\3;\7;\u017f\n;\f;\16;\u0182\13;\3<\3<\3<\3<"+
		"\3<\3<\3<\3<\5<\u018c\n<\3=\3=\5=\u0190\n=\3>\3>\5>\u0194\n>\3?\3?\7?"+
		"\u0198\n?\f?\16?\u019b\13?\3@\3@\5@\u019f\n@\3A\3A\3A\3A\3A\3A\3A\3A\3"+
		"A\3A\3A\5A\u01ac\nA\3B\3B\7B\u01b0\nB\fB\16B\u01b3\13B\3C\3C\3C\3C\3C"+
		"\7C\u01ba\nC\fC\16C\u01bd\13C\3D\3D\7D\u01c1\nD\fD\16D\u01c4\13D\3D\3"+
		"D\3E\5E\u01c9\nE\3E\3E\3E\3F\3F\3F\7F\u01d1\nF\fF\16F\u01d4\13F\3F\3F"+
		"\3G\3G\3G\7G\u01db\nG\fG\16G\u01de\13G\3G\3G\3H\3H\3H\7H\u01e5\nH\fH\16"+
		"H\u01e8\13H\3H\3H\3I\3I\3I\7I\u01ef\nI\fI\16I\u01f2\13I\3I\3I\3J\3J\3"+
		"J\3J\7J\u01fa\nJ\fJ\16J\u01fd\13J\3K\3K\3K\3K\5K\u0203\nK\6K\u0205\nK"+
		"\rK\16K\u0206\3K\3K\6\u01d2\u01dc\u01e6\u01f0\2L\3\3\5\4\7\5\t\6\13\7"+
		"\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25"+
		")\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O"+
		")Q*S+U,W\2Y\2[\2]\2_-a.c/e\60g\61i\62k\63m\64o\65q\66s\2u\2w\2y\2{\2}"+
		"\2\177\2\u0081\2\u0083\67\u00858\u00879\u0089:\u008b;\u008d<\u008f=\u0091"+
		">\u0093?\u0095@\3\2\24\4\2DDdd\4\2CCcc\4\2UUuu\4\2GGgg\4\2RRrr\4\2TTt"+
		"t\4\2HHhh\4\2KKkk\4\2ZZzz\4\2NNnn\4\2VVvv\4\2WWww\17\2C\\c|\u00c2\u00d8"+
		"\u00da\u00f8\u00fa\u0301\u0372\u037f\u0381\u2001\u200e\u200f\u2072\u2191"+
		"\u2c02\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff\4\2--//\n\2$$))^^ddh"+
		"hppttvv\6\2\f\f\17\17))^^\6\2\f\f\17\17$$^^\4\2\13\13\"\"\2\u0240\2\3"+
		"\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2"+
		"\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2"+
		"\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2"+
		"\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2"+
		"\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2"+
		"I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3"+
		"\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2"+
		"\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2\u0083\3\2\2\2\2\u0085"+
		"\3\2\2\2\2\u0087\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2"+
		"\2\2\u008f\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\3\u0097"+
		"\3\2\2\2\5\u0099\3\2\2\2\7\u009e\3\2\2\2\t\u00a5\3\2\2\2\13\u00ab\3\2"+
		"\2\2\r\u00b0\3\2\2\2\17\u00b3\3\2\2\2\21\u00b6\3\2\2\2\23\u00b9\3\2\2"+
		"\2\25\u00bb\3\2\2\2\27\u00bd\3\2\2\2\31\u00bf\3\2\2\2\33\u00c1\3\2\2\2"+
		"\35\u00c3\3\2\2\2\37\u00c5\3\2\2\2!\u00c7\3\2\2\2#\u00c9\3\2\2\2%\u00cb"+
		"\3\2\2\2\'\u00cd\3\2\2\2)\u00cf\3\2\2\2+\u00d1\3\2\2\2-\u00d3\3\2\2\2"+
		"/\u00d5\3\2\2\2\61\u00d7\3\2\2\2\63\u00d9\3\2\2\2\65\u00db\3\2\2\2\67"+
		"\u00dd\3\2\2\29\u00df\3\2\2\2;\u00e1\3\2\2\2=\u00e3\3\2\2\2?\u00e5\3\2"+
		"\2\2A\u00e7\3\2\2\2C\u00e9\3\2\2\2E\u00eb\3\2\2\2G\u00ed\3\2\2\2I\u00ef"+
		"\3\2\2\2K\u00f1\3\2\2\2M\u00f4\3\2\2\2O\u00f6\3\2\2\2Q\u00f9\3\2\2\2S"+
		"\u00fc\3\2\2\2U\u00fe\3\2\2\2W\u0100\3\2\2\2Y\u0102\3\2\2\2[\u0106\3\2"+
		"\2\2]\u010c\3\2\2\2_\u010f\3\2\2\2a\u0148\3\2\2\2c\u015b\3\2\2\2e\u015d"+
		"\3\2\2\2g\u0160\3\2\2\2i\u0163\3\2\2\2k\u0166\3\2\2\2m\u0169\3\2\2\2o"+
		"\u016c\3\2\2\2q\u016f\3\2\2\2s\u0176\3\2\2\2u\u0179\3\2\2\2w\u018b\3\2"+
		"\2\2y\u018f\3\2\2\2{\u0193\3\2\2\2}\u0195\3\2\2\2\177\u019e\3\2\2\2\u0081"+
		"\u01ab\3\2\2\2\u0083\u01ad\3\2\2\2\u0085\u01bb\3\2\2\2\u0087\u01be\3\2"+
		"\2\2\u0089\u01c8\3\2\2\2\u008b\u01cd\3\2\2\2\u008d\u01d7\3\2\2\2\u008f"+
		"\u01e1\3\2\2\2\u0091\u01eb\3\2\2\2\u0093\u01f5\3\2\2\2\u0095\u0204\3\2"+
		"\2\2\u0097\u0098\7c\2\2\u0098\4\3\2\2\2\u0099\u009a\t\2\2\2\u009a\u009b"+
		"\t\3\2\2\u009b\u009c\t\4\2\2\u009c\u009d\t\5\2\2\u009d\6\3\2\2\2\u009e"+
		"\u009f\t\6\2\2\u009f\u00a0\t\7\2\2\u00a0\u00a1\t\5\2\2\u00a1\u00a2\t\b"+
		"\2\2\u00a2\u00a3\t\t\2\2\u00a3\u00a4\t\n\2\2\u00a4\b\3\2\2\2\u00a5\u00a6"+
		"\t\b\2\2\u00a6\u00a7\t\3\2\2\u00a7\u00a8\t\13\2\2\u00a8\u00a9\t\4\2\2"+
		"\u00a9\u00aa\t\5\2\2\u00aa\n\3\2\2\2\u00ab\u00ac\t\f\2\2\u00ac\u00ad\t"+
		"\7\2\2\u00ad\u00ae\t\r\2\2\u00ae\u00af\t\5\2\2\u00af\f\3\2\2\2\u00b0\u00b1"+
		"\7`\2\2\u00b1\u00b2\7`\2\2\u00b2\16\3\2\2\2\u00b3\u00b4\7>\2\2\u00b4\u00b5"+
		"\7$\2\2\u00b5\20\3\2\2\2\u00b6\u00b7\7$\2\2\u00b7\u00b8\7@\2\2\u00b8\22"+
		"\3\2\2\2\u00b9\u00ba\7=\2\2\u00ba\24\3\2\2\2\u00bb\u00bc\7\60\2\2\u00bc"+
		"\26\3\2\2\2\u00bd\u00be\7.\2\2\u00be\30\3\2\2\2\u00bf\u00c0\7]\2\2\u00c0"+
		"\32\3\2\2\2\u00c1\u00c2\7_\2\2\u00c2\34\3\2\2\2\u00c3\u00c4\7}\2\2\u00c4"+
		"\36\3\2\2\2\u00c5\u00c6\7\177\2\2\u00c6 \3\2\2\2\u00c7\u00c8\7*\2\2\u00c8"+
		"\"\3\2\2\2\u00c9\u00ca\7+\2\2\u00ca$\3\2\2\2\u00cb\u00cc\7A\2\2\u00cc"+
		"&\3\2\2\2\u00cd\u00ce\7&\2\2\u00ce(\3\2\2\2\u00cf\u00d0\7$\2\2\u00d0*"+
		"\3\2\2\2\u00d1\u00d2\7)\2\2\u00d2,\3\2\2\2\u00d3\u00d4\7b\2\2\u00d4.\3"+
		"\2\2\2\u00d5\u00d6\7a\2\2\u00d6\60\3\2\2\2\u00d7\u00d8\7/\2\2\u00d8\62"+
		"\3\2\2\2\u00d9\u00da\7,\2\2\u00da\64\3\2\2\2\u00db\u00dc\7(\2\2\u00dc"+
		"\66\3\2\2\2\u00dd\u00de\7B\2\2\u00de8\3\2\2\2\u00df\u00e0\7#\2\2\u00e0"+
		":\3\2\2\2\u00e1\u00e2\7%\2\2\u00e2<\3\2\2\2\u00e3\u00e4\7\'\2\2\u00e4"+
		">\3\2\2\2\u00e5\u00e6\7-\2\2\u00e6@\3\2\2\2\u00e7\u00e8\7?\2\2\u00e8B"+
		"\3\2\2\2\u00e9\u00ea\7<\2\2\u00eaD\3\2\2\2\u00eb\u00ec\7>\2\2\u00ecF\3"+
		"\2\2\2\u00ed\u00ee\7@\2\2\u00eeH\3\2\2\2\u00ef\u00f0\7\61\2\2\u00f0J\3"+
		"\2\2\2\u00f1\u00f2\7\61\2\2\u00f2\u00f3\7\61\2\2\u00f3L\3\2\2\2\u00f4"+
		"\u00f5\7^\2\2\u00f5N\3\2\2\2\u00f6\u00f7\7]\2\2\u00f7\u00f8\7_\2\2\u00f8"+
		"P\3\2\2\2\u00f9\u00fa\7a\2\2\u00fa\u00fb\7<\2\2\u00fbR\3\2\2\2\u00fc\u00fd"+
		"\7\u0080\2\2\u00fdT\3\2\2\2\u00fe\u00ff\7`\2\2\u00ffV\3\2\2\2\u0100\u0101"+
		"\t\16\2\2\u0101X\3\2\2\2\u0102\u0103\4\62;\2\u0103Z\3\2\2\2\u0104\u0107"+
		"\5W,\2\u0105\u0107\5Y-\2\u0106\u0104\3\2\2\2\u0106\u0105\3\2\2\2\u0107"+
		"\\\3\2\2\2\u0108\u010d\5[.\2\u0109\u010d\5/\30\2\u010a\u010d\5\61\31\2"+
		"\u010b\u010d\5\25\13\2\u010c\u0108\3\2\2\2\u010c\u0109\3\2\2\2\u010c\u010a"+
		"\3\2\2\2\u010c\u010b\3\2\2\2\u010d^\3\2\2\2\u010e\u0110\5Y-\2\u010f\u010e"+
		"\3\2\2\2\u0110\u0111\3\2\2\2\u0111\u010f\3\2\2\2\u0111\u0112\3\2\2\2\u0112"+
		"`\3\2\2\2\u0113\u0115\5Y-\2\u0114\u0113\3\2\2\2\u0115\u0116\3\2\2\2\u0116"+
		"\u0114\3\2\2\2\u0116\u0117\3\2\2\2\u0117\u0118\3\2\2\2\u0118\u011c\5\25"+
		"\13\2\u0119\u011b\5Y-\2\u011a\u0119\3\2\2\2\u011b\u011e\3\2\2\2\u011c"+
		"\u011a\3\2\2\2\u011c\u011d\3\2\2\2\u011d\u011f\3\2\2\2\u011e\u011c\3\2"+
		"\2\2\u011f\u0121\t\5\2\2\u0120\u0122\t\17\2\2\u0121\u0120\3\2\2\2\u0121"+
		"\u0122\3\2\2\2\u0122\u0126\3\2\2\2\u0123\u0125\5Y-\2\u0124\u0123\3\2\2"+
		"\2\u0125\u0128\3\2\2\2\u0126\u0124\3\2\2\2\u0126\u0127\3\2\2\2\u0127\u0149"+
		"\3\2\2\2\u0128\u0126\3\2\2\2\u0129\u012b\5\25\13\2\u012a\u012c\5Y-\2\u012b"+
		"\u012a\3\2\2\2\u012c\u012d\3\2\2\2\u012d\u012b\3\2\2\2\u012d\u012e\3\2"+
		"\2\2\u012e\u012f\3\2\2\2\u012f\u0131\t\5\2\2\u0130\u0132\t\17\2\2\u0131"+
		"\u0130\3\2\2\2\u0131\u0132\3\2\2\2\u0132\u0136\3\2\2\2\u0133\u0135\5Y"+
		"-\2\u0134\u0133\3\2\2\2\u0135\u0138\3\2\2\2\u0136\u0134\3\2\2\2\u0136"+
		"\u0137\3\2\2\2\u0137\u0149\3\2\2\2\u0138\u0136\3\2\2\2\u0139\u013b\5Y"+
		"-\2\u013a\u0139\3\2\2\2\u013b\u013c\3\2\2\2\u013c\u013a\3\2\2\2\u013c"+
		"\u013d\3\2\2\2\u013d\u013e\3\2\2\2\u013e\u0140\t\5\2\2\u013f\u0141\t\17"+
		"\2\2\u0140\u013f\3\2\2\2\u0140\u0141\3\2\2\2\u0141\u0145\3\2\2\2\u0142"+
		"\u0144\5Y-\2\u0143\u0142\3\2\2\2\u0144\u0147\3\2\2\2\u0145\u0143\3\2\2"+
		"\2\u0145\u0146\3\2\2\2\u0146\u0149\3\2\2\2\u0147\u0145\3\2\2\2\u0148\u0114"+
		"\3\2\2\2\u0148\u0129\3\2\2\2\u0148\u013a\3\2\2\2\u0149b\3\2\2\2\u014a"+
		"\u014c\5Y-\2\u014b\u014a\3\2\2\2\u014c\u014d\3\2\2\2\u014d\u014b\3\2\2"+
		"\2\u014d\u014e\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u0151\5\25\13\2\u0150"+
		"\u0152\5Y-\2\u0151\u0150\3\2\2\2\u0152\u0153\3\2\2\2\u0153\u0151\3\2\2"+
		"\2\u0153\u0154\3\2\2\2\u0154\u015c\3\2\2\2\u0155\u0157\5\25\13\2\u0156"+
		"\u0158\5Y-\2\u0157\u0156\3\2\2\2\u0158\u0159\3\2\2\2\u0159\u0157\3\2\2"+
		"\2\u0159\u015a\3\2\2\2\u015a\u015c\3\2\2\2\u015b\u014b\3\2\2\2\u015b\u0155"+
		"\3\2\2\2\u015cd\3\2\2\2\u015d\u015e\5? \2\u015e\u015f\5_\60\2\u015ff\3"+
		"\2\2\2\u0160\u0161\5\61\31\2\u0161\u0162\5_\60\2\u0162h\3\2\2\2\u0163"+
		"\u0164\5? \2\u0164\u0165\5a\61\2\u0165j\3\2\2\2\u0166\u0167\5\61\31\2"+
		"\u0167\u0168\5a\61\2\u0168l\3\2\2\2\u0169\u016a\5? \2\u016a\u016b\5c\62"+
		"\2\u016bn\3\2\2\2\u016c\u016d\5\61\31\2\u016d\u016e\5c\62\2\u016ep\3\2"+
		"\2\2\u016f\u0173\5W,\2\u0170\u0172\5]/\2\u0171\u0170\3\2\2\2\u0172\u0175"+
		"\3\2\2\2\u0173\u0171\3\2\2\2\u0173\u0174\3\2\2\2\u0174r\3\2\2\2\u0175"+
		"\u0173\3\2\2\2\u0176\u0177\7^\2\2\u0177\u0178\t\20\2\2\u0178t\3\2\2\2"+
		"\u0179\u0180\5W,\2\u017a\u017f\5[.\2\u017b\u017f\5? \2\u017c\u017f\5\61"+
		"\31\2\u017d\u017f\5\25\13\2\u017e\u017a\3\2\2\2\u017e\u017b\3\2\2\2\u017e"+
		"\u017c\3\2\2\2\u017e\u017d\3\2\2\2\u017f\u0182\3\2\2\2\u0180\u017e\3\2"+
		"\2\2\u0180\u0181\3\2\2\2\u0181v\3\2\2\2\u0182\u0180\3\2\2\2\u0183\u018c"+
		"\5[.\2\u0184\u018c\5/\30\2\u0185\u018c\5\61\31\2\u0186\u018c\5C\"\2\u0187"+
		"\u018c\5\25\13\2\u0188\u018c\5;\36\2\u0189\u018c\5%\23\2\u018a\u018c\5"+
		"I%\2\u018b\u0183\3\2\2\2\u018b\u0184\3\2\2\2\u018b\u0185\3\2\2\2\u018b"+
		"\u0186\3\2\2\2\u018b\u0187\3\2\2\2\u018b\u0188\3\2\2\2\u018b\u0189\3\2"+
		"\2\2\u018b\u018a\3\2\2\2\u018cx\3\2\2\2\u018d\u0190\5W,\2\u018e\u0190"+
		"\5/\30\2\u018f\u018d\3\2\2\2\u018f\u018e\3\2\2\2\u0190z\3\2\2\2\u0191"+
		"\u0194\5y=\2\u0192\u0194\5Y-\2\u0193\u0191\3\2\2\2\u0193\u0192\3\2\2\2"+
		"\u0194|\3\2\2\2\u0195\u0199\5y=\2\u0196\u0198\5{>\2\u0197\u0196\3\2\2"+
		"\2\u0198\u019b\3\2\2\2\u0199\u0197\3\2\2\2\u0199\u019a\3\2\2\2\u019a~"+
		"\3\2\2\2\u019b\u0199\3\2\2\2\u019c\u019f\5W,\2\u019d\u019f\5/\30\2\u019e"+
		"\u019c\3\2\2\2\u019e\u019d\3\2\2\2\u019f\u0080\3\2\2\2\u01a0\u01ac\5\177"+
		"@\2\u01a1\u01ac\5Y-\2\u01a2\u01ac\5/\30\2\u01a3\u01ac\5\61\31\2\u01a4"+
		"\u01ac\5\25\13\2\u01a5\u01ac\5;\36\2\u01a6\u01ac\5%\23\2\u01a7\u01ac\5"+
		"I%\2\u01a8\u01ac\5=\37\2\u01a9\u01ac\5A!\2\u01aa\u01ac\5\23\n\2\u01ab"+
		"\u01a0\3\2\2\2\u01ab\u01a1\3\2\2\2\u01ab\u01a2\3\2\2\2\u01ab\u01a3\3\2"+
		"\2\2\u01ab\u01a4\3\2\2\2\u01ab\u01a5\3\2\2\2\u01ab\u01a6\3\2\2\2\u01ab"+
		"\u01a7\3\2\2\2\u01ab\u01a8\3\2\2\2\u01ab\u01a9\3\2\2\2\u01ab\u01aa\3\2"+
		"\2\2\u01ac\u0082\3\2\2\2\u01ad\u01b1\5\177@\2\u01ae\u01b0\5\u0081A\2\u01af"+
		"\u01ae\3\2\2\2\u01b0\u01b3\3\2\2\2\u01b1\u01af\3\2\2\2\u01b1\u01b2\3\2"+
		"\2\2\u01b2\u0084\3\2\2\2\u01b3\u01b1\3\2\2\2\u01b4\u01ba\5\u0081A\2\u01b5"+
		"\u01ba\5\35\17\2\u01b6\u01ba\5\37\20\2\u01b7\u01ba\5;\36\2\u01b8\u01ba"+
		"\5I%\2\u01b9\u01b4\3\2\2\2\u01b9\u01b5\3\2\2\2\u01b9\u01b6\3\2\2\2\u01b9"+
		"\u01b7\3\2\2\2\u01b9\u01b8\3\2\2\2\u01ba\u01bd\3\2\2\2\u01bb\u01b9\3\2"+
		"\2\2\u01bb\u01bc\3\2\2\2\u01bc\u0086\3\2\2\2\u01bd\u01bb\3\2\2\2\u01be"+
		"\u01c2\5\177@\2\u01bf\u01c1\5\u0081A\2\u01c0\u01bf\3\2\2\2\u01c1\u01c4"+
		"\3\2\2\2\u01c2\u01c0\3\2\2\2\u01c2\u01c3\3\2\2\2\u01c3\u01c5\3\2\2\2\u01c4"+
		"\u01c2\3\2\2\2\u01c5\u01c6\5C\"\2\u01c6\u0088\3\2\2\2\u01c7\u01c9\5\u0083"+
		"B\2\u01c8\u01c7\3\2\2\2\u01c8\u01c9\3\2\2\2\u01c9\u01ca\3\2\2\2\u01ca"+
		"\u01cb\5C\"\2\u01cb\u01cc\5\u0085C\2\u01cc\u008a\3\2\2\2\u01cd\u01d2\7"+
		")\2\2\u01ce\u01d1\n\21\2\2\u01cf\u01d1\5s:\2\u01d0\u01ce\3\2\2\2\u01d0"+
		"\u01cf\3\2\2\2\u01d1\u01d4\3\2\2\2\u01d2\u01d3\3\2\2\2\u01d2\u01d0\3\2"+
		"\2\2\u01d3\u01d5\3\2\2\2\u01d4\u01d2\3\2\2\2\u01d5\u01d6\7)\2\2\u01d6"+
		"\u008c\3\2\2\2\u01d7\u01dc\7$\2\2\u01d8\u01db\n\22\2\2\u01d9\u01db\5s"+
		":\2\u01da\u01d8\3\2\2\2\u01da\u01d9\3\2\2\2\u01db\u01de\3\2\2\2\u01dc"+
		"\u01dd\3\2\2\2\u01dc\u01da\3\2\2\2\u01dd\u01df\3\2\2\2\u01de\u01dc\3\2"+
		"\2\2\u01df\u01e0\7$\2\2\u01e0\u008e\3\2\2\2\u01e1\u01e6\7>\2\2\u01e2\u01e5"+
		"\n\22\2\2\u01e3\u01e5\5s:\2\u01e4\u01e2\3\2\2\2\u01e4\u01e3\3\2\2\2\u01e5"+
		"\u01e8\3\2\2\2\u01e6\u01e7\3\2\2\2\u01e6\u01e4\3\2\2\2\u01e7\u01e9\3\2"+
		"\2\2\u01e8\u01e6\3\2\2\2\u01e9\u01ea\7@\2\2\u01ea\u0090\3\2\2\2\u01eb"+
		"\u01f0\7}\2\2\u01ec\u01ef\n\22\2\2\u01ed\u01ef\5s:\2\u01ee\u01ec\3\2\2"+
		"\2\u01ee\u01ed\3\2\2\2\u01ef\u01f2\3\2\2\2\u01f0\u01f1\3\2\2\2\u01f0\u01ee"+
		"\3\2\2\2\u01f1\u01f3\3\2\2\2\u01f2\u01f0\3\2\2\2\u01f3\u01f4\7\177\2\2"+
		"\u01f4\u0092\3\2\2\2\u01f5\u01f6\5u;\2\u01f6\u01f7\5C\"\2\u01f7\u01fb"+
		"\5K&\2\u01f8\u01fa\5w<\2\u01f9\u01f8\3\2\2\2\u01fa\u01fd\3\2\2\2\u01fb"+
		"\u01f9\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc\u0094\3\2\2\2\u01fd\u01fb\3\2"+
		"\2\2\u01fe\u0205\t\23\2\2\u01ff\u0203\7\f\2\2\u0200\u0201\7\17\2\2\u0201"+
		"\u0203\7\f\2\2\u0202\u01ff\3\2\2\2\u0202\u0200\3\2\2\2\u0203\u0205\3\2"+
		"\2\2\u0204\u01fe\3\2\2\2\u0204\u0202\3\2\2\2\u0205\u0206\3\2\2\2\u0206"+
		"\u0204\3\2\2\2\u0206\u0207\3\2\2\2\u0207\u0208\3\2\2\2\u0208\u0209\bK"+
		"\2\2\u0209\u0096\3\2\2\2/\2\u0106\u010c\u0111\u0116\u011c\u0121\u0126"+
		"\u012d\u0131\u0136\u013c\u0140\u0145\u0148\u014d\u0153\u0159\u015b\u0173"+
		"\u017e\u0180\u018b\u018f\u0193\u0199\u019e\u01ab\u01b1\u01b9\u01bb\u01c2"+
		"\u01c8\u01d0\u01d2\u01da\u01dc\u01e4\u01e6\u01ee\u01f0\u01fb\u0202\u0204"+
		"\u0206\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}