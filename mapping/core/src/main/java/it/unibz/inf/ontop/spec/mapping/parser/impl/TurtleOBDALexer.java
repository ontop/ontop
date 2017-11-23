// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/java/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA.g4 by ANTLR 4.7
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
public class TurtleOBDALexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, BASE_KW=2, PREFIX_KW=3, FALSE=4, TRUE=5, REFERENCE=6, LTSIGN=7, 
		RTSIGN=8, SEMI=9, PERIOD=10, COMMA=11, LSQ_BRACKET=12, RSQ_BRACKET=13, 
		LCR_BRACKET=14, RCR_BRACKET=15, LPAREN=16, RPAREN=17, QUESTION=18, DOLLAR=19, 
		QUOTE_DOUBLE=20, QUOTE_SINGLE=21, APOSTROPHE=22, UNDERSCORE=23, MINUS=24, 
		ASTERISK=25, AMPERSAND=26, AT=27, EXCLAMATION=28, HASH=29, PERCENT=30, 
		PLUS=31, EQUALS=32, COLON=33, LESS=34, GREATER=35, SLASH=36, DOUBLE_SLASH=37, 
		BACKSLASH=38, BLANK=39, BLANK_PREFIX=40, TILDE=41, CARET=42, INTEGER=43, 
		DOUBLE=44, DECIMAL=45, INTEGER_POSITIVE=46, INTEGER_NEGATIVE=47, DOUBLE_POSITIVE=48, 
		DOUBLE_NEGATIVE=49, DECIMAL_POSITIVE=50, DECIMAL_NEGATIVE=51, PREFIXED_NAME=52, 
		NCNAME_EXT=53, PREFIX=54, STRING_WITH_QUOTE_DOUBLE=55, STRING_WITH_CURLY_BRACKET=56, 
		URI_REF=57, WS=58;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "BASE_KW", "PREFIX_KW", "FALSE", "TRUE", "REFERENCE", "LTSIGN", 
		"RTSIGN", "SEMI", "PERIOD", "COMMA", "LSQ_BRACKET", "RSQ_BRACKET", "LCR_BRACKET", 
		"RCR_BRACKET", "LPAREN", "RPAREN", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", 
		"QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "MINUS", "ASTERISK", "AMPERSAND", 
		"AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "EQUALS", "COLON", "LESS", 
		"GREATER", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "BLANK", "BLANK_PREFIX", 
		"TILDE", "CARET", "ALPHA", "DIGIT", "ALPHANUM", "INTEGER", "DOUBLE", "DECIMAL", 
		"INTEGER_POSITIVE", "INTEGER_NEGATIVE", "DOUBLE_POSITIVE", "DOUBLE_NEGATIVE", 
		"DECIMAL_POSITIVE", "DECIMAL_NEGATIVE", "PREFIXED_NAME", "NCNAME_EXT", 
		"NAME_CHAR", "PREFIX", "STRING_WITH_QUOTE_DOUBLE", "STRING_WITH_CURLY_BRACKET", 
		"URI_REF", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'a'", null, null, null, null, "'^^'", "'<\"'", "'\">'", "';'", 
		"'.'", "','", "'['", "']'", "'{'", "'}'", "'('", "')'", "'?'", "'$'", 
		"'\"'", "'''", "'`'", "'_'", "'-'", "'*'", "'&'", "'@'", "'!'", "'#'", 
		"'%'", "'+'", "'='", "':'", "'<'", "'>'", "'/'", "'//'", "'\\'", "'[]'", 
		"'_:'", "'~'", "'^'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "BASE_KW", "PREFIX_KW", "FALSE", "TRUE", "REFERENCE", "LTSIGN", 
		"RTSIGN", "SEMI", "PERIOD", "COMMA", "LSQ_BRACKET", "RSQ_BRACKET", "LCR_BRACKET", 
		"RCR_BRACKET", "LPAREN", "RPAREN", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", 
		"QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "MINUS", "ASTERISK", "AMPERSAND", 
		"AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "EQUALS", "COLON", "LESS", 
		"GREATER", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "BLANK", "BLANK_PREFIX", 
		"TILDE", "CARET", "INTEGER", "DOUBLE", "DECIMAL", "INTEGER_POSITIVE", 
		"INTEGER_NEGATIVE", "DOUBLE_POSITIVE", "DOUBLE_NEGATIVE", "DECIMAL_POSITIVE", 
		"DECIMAL_NEGATIVE", "PREFIXED_NAME", "NCNAME_EXT", "PREFIX", "STRING_WITH_QUOTE_DOUBLE", 
		"STRING_WITH_CURLY_BRACKET", "URI_REF", "WS"
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


	public TurtleOBDALexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "TurtleOBDA.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2<\u0198\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t"+
		"\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3"+
		"\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3"+
		"\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3"+
		"\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3&\3\'\3\'\3"+
		"(\3(\3(\3)\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\5.\u00ef\n.\3/\6/\u00f2"+
		"\n/\r/\16/\u00f3\3\60\6\60\u00f7\n\60\r\60\16\60\u00f8\3\60\3\60\7\60"+
		"\u00fd\n\60\f\60\16\60\u0100\13\60\3\60\3\60\5\60\u0104\n\60\3\60\7\60"+
		"\u0107\n\60\f\60\16\60\u010a\13\60\3\60\3\60\6\60\u010e\n\60\r\60\16\60"+
		"\u010f\3\60\3\60\5\60\u0114\n\60\3\60\7\60\u0117\n\60\f\60\16\60\u011a"+
		"\13\60\3\60\6\60\u011d\n\60\r\60\16\60\u011e\3\60\3\60\5\60\u0123\n\60"+
		"\3\60\7\60\u0126\n\60\f\60\16\60\u0129\13\60\5\60\u012b\n\60\3\61\6\61"+
		"\u012e\n\61\r\61\16\61\u012f\3\61\3\61\6\61\u0134\n\61\r\61\16\61\u0135"+
		"\3\61\3\61\6\61\u013a\n\61\r\61\16\61\u013b\5\61\u013e\n\61\3\62\3\62"+
		"\3\62\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\65\3\66\3\66\3\66\3\67"+
		"\3\67\3\67\38\38\38\39\69\u0156\n9\r9\169\u0157\39\69\u015b\n9\r9\169"+
		"\u015c\3:\3:\3:\3:\3:\3:\3:\3:\3:\3:\5:\u0169\n:\3;\5;\u016c\n;\3;\6;"+
		"\u016f\n;\r;\16;\u0170\5;\u0173\n;\3;\3;\3<\3<\6<\u0179\n<\r<\16<\u017a"+
		"\3<\3<\3=\3=\6=\u0181\n=\r=\16=\u0182\3=\3=\3>\3>\6>\u0189\n>\r>\16>\u018a"+
		"\3>\3>\3?\3?\3?\3?\6?\u0193\n?\r?\16?\u0194\3?\3?\4\u0182\u018a\2@\3\3"+
		"\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!"+
		"A\"C#E$G%I&K\'M(O)Q*S+U,W\2Y\2[\2]-_.a/c\60e\61g\62i\63k\64m\65o\66q\67"+
		"s\2u8w9y:{;}<\3\2\23\4\2DDdd\4\2CCcc\4\2UUuu\4\2GGgg\4\2RRrr\4\2TTtt\4"+
		"\2HHhh\4\2KKkk\4\2ZZzz\4\2NNnn\4\2VVvv\4\2WWww\17\2C\\c|\u00c2\u00d8\u00da"+
		"\u00f8\u00fa\u0301\u0372\u037f\u0381\u2001\u200e\u200f\u2072\u2191\u2c02"+
		"\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff\4\2--//\b\2\n\f\16\17\"\"$"+
		"$))^^\4\2\13\f\"\"\4\2\n\n\16\16\2\u01ba\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35"+
		"\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)"+
		"\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2"+
		"\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2"+
		"A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3"+
		"\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2]\3\2\2\2\2_\3\2\2"+
		"\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2"+
		"m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3"+
		"\2\2\2\2}\3\2\2\2\3\177\3\2\2\2\5\u0081\3\2\2\2\7\u0086\3\2\2\2\t\u008d"+
		"\3\2\2\2\13\u0093\3\2\2\2\r\u0098\3\2\2\2\17\u009b\3\2\2\2\21\u009e\3"+
		"\2\2\2\23\u00a1\3\2\2\2\25\u00a3\3\2\2\2\27\u00a5\3\2\2\2\31\u00a7\3\2"+
		"\2\2\33\u00a9\3\2\2\2\35\u00ab\3\2\2\2\37\u00ad\3\2\2\2!\u00af\3\2\2\2"+
		"#\u00b1\3\2\2\2%\u00b3\3\2\2\2\'\u00b5\3\2\2\2)\u00b7\3\2\2\2+\u00b9\3"+
		"\2\2\2-\u00bb\3\2\2\2/\u00bd\3\2\2\2\61\u00bf\3\2\2\2\63\u00c1\3\2\2\2"+
		"\65\u00c3\3\2\2\2\67\u00c5\3\2\2\29\u00c7\3\2\2\2;\u00c9\3\2\2\2=\u00cb"+
		"\3\2\2\2?\u00cd\3\2\2\2A\u00cf\3\2\2\2C\u00d1\3\2\2\2E\u00d3\3\2\2\2G"+
		"\u00d5\3\2\2\2I\u00d7\3\2\2\2K\u00d9\3\2\2\2M\u00dc\3\2\2\2O\u00de\3\2"+
		"\2\2Q\u00e1\3\2\2\2S\u00e4\3\2\2\2U\u00e6\3\2\2\2W\u00e8\3\2\2\2Y\u00ea"+
		"\3\2\2\2[\u00ee\3\2\2\2]\u00f1\3\2\2\2_\u012a\3\2\2\2a\u013d\3\2\2\2c"+
		"\u013f\3\2\2\2e\u0142\3\2\2\2g\u0145\3\2\2\2i\u0148\3\2\2\2k\u014b\3\2"+
		"\2\2m\u014e\3\2\2\2o\u0151\3\2\2\2q\u015a\3\2\2\2s\u0168\3\2\2\2u\u0172"+
		"\3\2\2\2w\u0176\3\2\2\2y\u017e\3\2\2\2{\u0186\3\2\2\2}\u0192\3\2\2\2\177"+
		"\u0080\7c\2\2\u0080\4\3\2\2\2\u0081\u0082\t\2\2\2\u0082\u0083\t\3\2\2"+
		"\u0083\u0084\t\4\2\2\u0084\u0085\t\5\2\2\u0085\6\3\2\2\2\u0086\u0087\t"+
		"\6\2\2\u0087\u0088\t\7\2\2\u0088\u0089\t\5\2\2\u0089\u008a\t\b\2\2\u008a"+
		"\u008b\t\t\2\2\u008b\u008c\t\n\2\2\u008c\b\3\2\2\2\u008d\u008e\t\b\2\2"+
		"\u008e\u008f\t\3\2\2\u008f\u0090\t\13\2\2\u0090\u0091\t\4\2\2\u0091\u0092"+
		"\t\5\2\2\u0092\n\3\2\2\2\u0093\u0094\t\f\2\2\u0094\u0095\t\7\2\2\u0095"+
		"\u0096\t\r\2\2\u0096\u0097\t\5\2\2\u0097\f\3\2\2\2\u0098\u0099\7`\2\2"+
		"\u0099\u009a\7`\2\2\u009a\16\3\2\2\2\u009b\u009c\7>\2\2\u009c\u009d\7"+
		"$\2\2\u009d\20\3\2\2\2\u009e\u009f\7$\2\2\u009f\u00a0\7@\2\2\u00a0\22"+
		"\3\2\2\2\u00a1\u00a2\7=\2\2\u00a2\24\3\2\2\2\u00a3\u00a4\7\60\2\2\u00a4"+
		"\26\3\2\2\2\u00a5\u00a6\7.\2\2\u00a6\30\3\2\2\2\u00a7\u00a8\7]\2\2\u00a8"+
		"\32\3\2\2\2\u00a9\u00aa\7_\2\2\u00aa\34\3\2\2\2\u00ab\u00ac\7}\2\2\u00ac"+
		"\36\3\2\2\2\u00ad\u00ae\7\177\2\2\u00ae \3\2\2\2\u00af\u00b0\7*\2\2\u00b0"+
		"\"\3\2\2\2\u00b1\u00b2\7+\2\2\u00b2$\3\2\2\2\u00b3\u00b4\7A\2\2\u00b4"+
		"&\3\2\2\2\u00b5\u00b6\7&\2\2\u00b6(\3\2\2\2\u00b7\u00b8\7$\2\2\u00b8*"+
		"\3\2\2\2\u00b9\u00ba\7)\2\2\u00ba,\3\2\2\2\u00bb\u00bc\7b\2\2\u00bc.\3"+
		"\2\2\2\u00bd\u00be\7a\2\2\u00be\60\3\2\2\2\u00bf\u00c0\7/\2\2\u00c0\62"+
		"\3\2\2\2\u00c1\u00c2\7,\2\2\u00c2\64\3\2\2\2\u00c3\u00c4\7(\2\2\u00c4"+
		"\66\3\2\2\2\u00c5\u00c6\7B\2\2\u00c68\3\2\2\2\u00c7\u00c8\7#\2\2\u00c8"+
		":\3\2\2\2\u00c9\u00ca\7%\2\2\u00ca<\3\2\2\2\u00cb\u00cc\7\'\2\2\u00cc"+
		">\3\2\2\2\u00cd\u00ce\7-\2\2\u00ce@\3\2\2\2\u00cf\u00d0\7?\2\2\u00d0B"+
		"\3\2\2\2\u00d1\u00d2\7<\2\2\u00d2D\3\2\2\2\u00d3\u00d4\7>\2\2\u00d4F\3"+
		"\2\2\2\u00d5\u00d6\7@\2\2\u00d6H\3\2\2\2\u00d7\u00d8\7\61\2\2\u00d8J\3"+
		"\2\2\2\u00d9\u00da\7\61\2\2\u00da\u00db\7\61\2\2\u00dbL\3\2\2\2\u00dc"+
		"\u00dd\7^\2\2\u00ddN\3\2\2\2\u00de\u00df\7]\2\2\u00df\u00e0\7_\2\2\u00e0"+
		"P\3\2\2\2\u00e1\u00e2\7a\2\2\u00e2\u00e3\7<\2\2\u00e3R\3\2\2\2\u00e4\u00e5"+
		"\7\u0080\2\2\u00e5T\3\2\2\2\u00e6\u00e7\7`\2\2\u00e7V\3\2\2\2\u00e8\u00e9"+
		"\t\16\2\2\u00e9X\3\2\2\2\u00ea\u00eb\4\62;\2\u00ebZ\3\2\2\2\u00ec\u00ef"+
		"\5W,\2\u00ed\u00ef\5Y-\2\u00ee\u00ec\3\2\2\2\u00ee\u00ed\3\2\2\2\u00ef"+
		"\\\3\2\2\2\u00f0\u00f2\5Y-\2\u00f1\u00f0\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3"+
		"\u00f1\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4^\3\2\2\2\u00f5\u00f7\5Y-\2\u00f6"+
		"\u00f5\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f8\u00f9\3\2"+
		"\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fe\5\25\13\2\u00fb\u00fd\5Y-\2\u00fc"+
		"\u00fb\3\2\2\2\u00fd\u0100\3\2\2\2\u00fe\u00fc\3\2\2\2\u00fe\u00ff\3\2"+
		"\2\2\u00ff\u0101\3\2\2\2\u0100\u00fe\3\2\2\2\u0101\u0103\t\5\2\2\u0102"+
		"\u0104\t\17\2\2\u0103\u0102\3\2\2\2\u0103\u0104\3\2\2\2\u0104\u0108\3"+
		"\2\2\2\u0105\u0107\5Y-\2\u0106\u0105\3\2\2\2\u0107\u010a\3\2\2\2\u0108"+
		"\u0106\3\2\2\2\u0108\u0109\3\2\2\2\u0109\u012b\3\2\2\2\u010a\u0108\3\2"+
		"\2\2\u010b\u010d\5\25\13\2\u010c\u010e\5Y-\2\u010d\u010c\3\2\2\2\u010e"+
		"\u010f\3\2\2\2\u010f\u010d\3\2\2\2\u010f\u0110\3\2\2\2\u0110\u0111\3\2"+
		"\2\2\u0111\u0113\t\5\2\2\u0112\u0114\t\17\2\2\u0113\u0112\3\2\2\2\u0113"+
		"\u0114\3\2\2\2\u0114\u0118\3\2\2\2\u0115\u0117\5Y-\2\u0116\u0115\3\2\2"+
		"\2\u0117\u011a\3\2\2\2\u0118\u0116\3\2\2\2\u0118\u0119\3\2\2\2\u0119\u012b"+
		"\3\2\2\2\u011a\u0118\3\2\2\2\u011b\u011d\5Y-\2\u011c\u011b\3\2\2\2\u011d"+
		"\u011e\3\2\2\2\u011e\u011c\3\2\2\2\u011e\u011f\3\2\2\2\u011f\u0120\3\2"+
		"\2\2\u0120\u0122\t\5\2\2\u0121\u0123\t\17\2\2\u0122\u0121\3\2\2\2\u0122"+
		"\u0123\3\2\2\2\u0123\u0127\3\2\2\2\u0124\u0126\5Y-\2\u0125\u0124\3\2\2"+
		"\2\u0126\u0129\3\2\2\2\u0127\u0125\3\2\2\2\u0127\u0128\3\2\2\2\u0128\u012b"+
		"\3\2\2\2\u0129\u0127\3\2\2\2\u012a\u00f6\3\2\2\2\u012a\u010b\3\2\2\2\u012a"+
		"\u011c\3\2\2\2\u012b`\3\2\2\2\u012c\u012e\5Y-\2\u012d\u012c\3\2\2\2\u012e"+
		"\u012f\3\2\2\2\u012f\u012d\3\2\2\2\u012f\u0130\3\2\2\2\u0130\u0131\3\2"+
		"\2\2\u0131\u0133\5\25\13\2\u0132\u0134\5Y-\2\u0133\u0132\3\2\2\2\u0134"+
		"\u0135\3\2\2\2\u0135\u0133\3\2\2\2\u0135\u0136\3\2\2\2\u0136\u013e\3\2"+
		"\2\2\u0137\u0139\5\25\13\2\u0138\u013a\5Y-\2\u0139\u0138\3\2\2\2\u013a"+
		"\u013b\3\2\2\2\u013b\u0139\3\2\2\2\u013b\u013c\3\2\2\2\u013c\u013e\3\2"+
		"\2\2\u013d\u012d\3\2\2\2\u013d\u0137\3\2\2\2\u013eb\3\2\2\2\u013f\u0140"+
		"\5? \2\u0140\u0141\5]/\2\u0141d\3\2\2\2\u0142\u0143\5\61\31\2\u0143\u0144"+
		"\5]/\2\u0144f\3\2\2\2\u0145\u0146\5? \2\u0146\u0147\5_\60\2\u0147h\3\2"+
		"\2\2\u0148\u0149\5\61\31\2\u0149\u014a\5_\60\2\u014aj\3\2\2\2\u014b\u014c"+
		"\5? \2\u014c\u014d\5a\61\2\u014dl\3\2\2\2\u014e\u014f\5\61\31\2\u014f"+
		"\u0150\5a\61\2\u0150n\3\2\2\2\u0151\u0152\5u;\2\u0152\u0153\5q9\2\u0153"+
		"p\3\2\2\2\u0154\u0156\5s:\2\u0155\u0154\3\2\2\2\u0156\u0157\3\2\2\2\u0157"+
		"\u0155\3\2\2\2\u0157\u0158\3\2\2\2\u0158\u015b\3\2\2\2\u0159\u015b\5y"+
		"=\2\u015a\u0155\3\2\2\2\u015a\u0159\3\2\2\2\u015b\u015c\3\2\2\2\u015c"+
		"\u015a\3\2\2\2\u015c\u015d\3\2\2\2\u015dr\3\2\2\2\u015e\u0169\5[.\2\u015f"+
		"\u0169\5Y-\2\u0160\u0169\5/\30\2\u0161\u0169\5\61\31\2\u0162\u0169\5\25"+
		"\13\2\u0163\u0169\5;\36\2\u0164\u0169\5%\23\2\u0165\u0169\5I%\2\u0166"+
		"\u0169\5=\37\2\u0167\u0169\5A!\2\u0168\u015e\3\2\2\2\u0168\u015f\3\2\2"+
		"\2\u0168\u0160\3\2\2\2\u0168\u0161\3\2\2\2\u0168\u0162\3\2\2\2\u0168\u0163"+
		"\3\2\2\2\u0168\u0164\3\2\2\2\u0168\u0165\3\2\2\2\u0168\u0166\3\2\2\2\u0168"+
		"\u0167\3\2\2\2\u0169t\3\2\2\2\u016a\u016c\5/\30\2\u016b\u016a\3\2\2\2"+
		"\u016b\u016c\3\2\2\2\u016c\u016e\3\2\2\2\u016d\u016f\5s:\2\u016e\u016d"+
		"\3\2\2\2\u016f\u0170\3\2\2\2\u0170\u016e\3\2\2\2\u0170\u0171\3\2\2\2\u0171"+
		"\u0173\3\2\2\2\u0172\u016b\3\2\2\2\u0172\u0173\3\2\2\2\u0173\u0174\3\2"+
		"\2\2\u0174\u0175\5C\"\2\u0175v\3\2\2\2\u0176\u0178\5)\25\2\u0177\u0179"+
		"\n\20\2\2\u0178\u0177\3\2\2\2\u0179\u017a\3\2\2\2\u017a\u0178\3\2\2\2"+
		"\u017a\u017b\3\2\2\2\u017b\u017c\3\2\2\2\u017c\u017d\5)\25\2\u017dx\3"+
		"\2\2\2\u017e\u0180\5\35\17\2\u017f\u0181\n\20\2\2\u0180\u017f\3\2\2\2"+
		"\u0181\u0182\3\2\2\2\u0182\u0183\3\2\2\2\u0182\u0180\3\2\2\2\u0183\u0184"+
		"\3\2\2\2\u0184\u0185\5\37\20\2\u0185z\3\2\2\2\u0186\u0188\5E#\2\u0187"+
		"\u0189\n\20\2\2\u0188\u0187\3\2\2\2\u0189\u018a\3\2\2\2\u018a\u018b\3"+
		"\2\2\2\u018a\u0188\3\2\2\2\u018b\u018c\3\2\2\2\u018c\u018d\5G$\2\u018d"+
		"|\3\2\2\2\u018e\u0193\t\21\2\2\u018f\u0190\7\17\2\2\u0190\u0193\7\f\2"+
		"\2\u0191\u0193\t\22\2\2\u0192\u018e\3\2\2\2\u0192\u018f\3\2\2\2\u0192"+
		"\u0191\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0192\3\2\2\2\u0194\u0195\3\2"+
		"\2\2\u0195\u0196\3\2\2\2\u0196\u0197\b?\2\2\u0197~\3\2\2\2 \2\u00ee\u00f3"+
		"\u00f8\u00fe\u0103\u0108\u010f\u0113\u0118\u011e\u0122\u0127\u012a\u012f"+
		"\u0135\u013b\u013d\u0157\u015a\u015c\u0168\u016b\u0170\u0172\u017a\u0182"+
		"\u018a\u0192\u0194\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}