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
		T__0=1, BASE_KW=2, PREFIX_KW=3, FALSE=4, TRUE=5, PERIOD=6, LSQ_BRACKET=7, 
		RSQ_BRACKET=8, LCR_BRACKET=9, RCR_BRACKET=10, QUESTION=11, QUOTE_DOUBLE=12, 
		QUOTE_SINGLE=13, UNDERSCORE=14, MINUS=15, HASH=16, PERCENT=17, PLUS=18, 
		EQUALS=19, COLON=20, LESS=21, GREATER=22, SLASH=23, BLANK=24, BLANK_PREFIX=25, 
		INTEGER=26, DOUBLE=27, DECIMAL=28, INTEGER_POSITIVE=29, INTEGER_NEGATIVE=30, 
		DOUBLE_POSITIVE=31, DOUBLE_NEGATIVE=32, DECIMAL_POSITIVE=33, DECIMAL_NEGATIVE=34, 
		LANGUAGE_STRING=35, NAME_SBS=36, STRING_WITH_QUOTE_DOUBLE=37, STRING_WITH_CURLY_BRACKET=38, 
		URI_REF=39, WS=40;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "BASE_KW", "PREFIX_KW", "FALSE", "TRUE", "PERIOD", "LSQ_BRACKET", 
		"RSQ_BRACKET", "LCR_BRACKET", "RCR_BRACKET", "QUESTION", "QUOTE_DOUBLE", 
		"QUOTE_SINGLE", "UNDERSCORE", "MINUS", "HASH", "PERCENT", "PLUS", "EQUALS", 
		"COLON", "LESS", "GREATER", "SLASH", "BLANK", "BLANK_PREFIX", "INTEGER", 
		"DOUBLE", "DECIMAL", "INTEGER_POSITIVE", "INTEGER_NEGATIVE", "DOUBLE_POSITIVE", 
		"DOUBLE_NEGATIVE", "DECIMAL_POSITIVE", "DECIMAL_NEGATIVE", "LANGUAGE_STRING", 
		"NAME_SBS", "STRING_WITH_QUOTE_DOUBLE", "STRING_WITH_CURLY_BRACKET", "URI_REF", 
		"ALPHA", "DIGIT", "ALPHANUM", "NAME_CHAR", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'a'", null, null, null, null, "'.'", "'['", "']'", "'{'", "'}'", 
		"'?'", "'\"'", "'''", "'_'", "'-'", "'#'", "'%'", "'+'", "'='", "':'", 
		"'<'", "'>'", "'/'", "'[]'", "'_:'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "BASE_KW", "PREFIX_KW", "FALSE", "TRUE", "PERIOD", "LSQ_BRACKET", 
		"RSQ_BRACKET", "LCR_BRACKET", "RCR_BRACKET", "QUESTION", "QUOTE_DOUBLE", 
		"QUOTE_SINGLE", "UNDERSCORE", "MINUS", "HASH", "PERCENT", "PLUS", "EQUALS", 
		"COLON", "LESS", "GREATER", "SLASH", "BLANK", "BLANK_PREFIX", "INTEGER", 
		"DOUBLE", "DECIMAL", "INTEGER_POSITIVE", "INTEGER_NEGATIVE", "DOUBLE_POSITIVE", 
		"DOUBLE_NEGATIVE", "DECIMAL_POSITIVE", "DECIMAL_NEGATIVE", "LANGUAGE_STRING", 
		"NAME_SBS", "STRING_WITH_QUOTE_DOUBLE", "STRING_WITH_CURLY_BRACKET", "URI_REF", 
		"WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2*\u014d\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3"+
		"\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3"+
		"\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3"+
		"\31\3\31\3\31\3\32\3\32\3\32\3\33\6\33\u00a0\n\33\r\33\16\33\u00a1\3\34"+
		"\6\34\u00a5\n\34\r\34\16\34\u00a6\3\34\3\34\7\34\u00ab\n\34\f\34\16\34"+
		"\u00ae\13\34\3\34\3\34\5\34\u00b2\n\34\3\34\7\34\u00b5\n\34\f\34\16\34"+
		"\u00b8\13\34\3\34\3\34\6\34\u00bc\n\34\r\34\16\34\u00bd\3\34\3\34\5\34"+
		"\u00c2\n\34\3\34\7\34\u00c5\n\34\f\34\16\34\u00c8\13\34\3\34\6\34\u00cb"+
		"\n\34\r\34\16\34\u00cc\3\34\3\34\5\34\u00d1\n\34\3\34\7\34\u00d4\n\34"+
		"\f\34\16\34\u00d7\13\34\5\34\u00d9\n\34\3\35\6\35\u00dc\n\35\r\35\16\35"+
		"\u00dd\3\35\3\35\6\35\u00e2\n\35\r\35\16\35\u00e3\3\35\3\35\6\35\u00e8"+
		"\n\35\r\35\16\35\u00e9\5\35\u00ec\n\35\3\36\3\36\3\36\3\37\3\37\3\37\3"+
		" \3 \3 \3!\3!\3!\3\"\3\"\3\"\3#\3#\3#\3$\6$\u0101\n$\r$\16$\u0102\3$\3"+
		"$\6$\u0107\n$\r$\16$\u0108\7$\u010b\n$\f$\16$\u010e\13$\3%\6%\u0111\n"+
		"%\r%\16%\u0112\3%\5%\u0116\n%\3&\3&\6&\u011a\n&\r&\16&\u011b\3&\3&\3\'"+
		"\3\'\6\'\u0122\n\'\r\'\16\'\u0123\3\'\3\'\3(\3(\6(\u012a\n(\r(\16(\u012b"+
		"\3(\3(\3)\3)\3*\3*\3+\3+\5+\u0136\n+\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\5,"+
		"\u0142\n,\3-\3-\3-\3-\6-\u0148\n-\r-\16-\u0149\3-\3-\4\u0123\u012b\2."+
		"\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20"+
		"\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37"+
		"= ?!A\"C#E$G%I&K\'M(O)Q\2S\2U\2W\2Y*\3\2\25\4\2DDdd\4\2CCcc\4\2UUuu\4"+
		"\2GGgg\4\2RRrr\4\2TTtt\4\2HHhh\4\2KKkk\4\2ZZzz\4\2NNnn\4\2VVvv\4\2WWw"+
		"w\4\2--//\4\2C\\c|\5\2\62;C\\c|\b\2\n\f\16\17\"\"$$))^^\17\2C\\c|\u00c2"+
		"\u00d8\u00da\u00f8\u00fa\u0301\u0372\u037f\u0381\u2001\u200e\u200f\u2072"+
		"\u2191\u2c02\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff\4\2\13\f\"\"\4"+
		"\2\n\n\16\16\2\u016e\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2"+
		"\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3"+
		"\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2"+
		"\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2"+
		"\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2"+
		"\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2"+
		"\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Y"+
		"\3\2\2\2\3[\3\2\2\2\5]\3\2\2\2\7b\3\2\2\2\ti\3\2\2\2\13o\3\2\2\2\rt\3"+
		"\2\2\2\17v\3\2\2\2\21x\3\2\2\2\23z\3\2\2\2\25|\3\2\2\2\27~\3\2\2\2\31"+
		"\u0080\3\2\2\2\33\u0082\3\2\2\2\35\u0084\3\2\2\2\37\u0086\3\2\2\2!\u0088"+
		"\3\2\2\2#\u008a\3\2\2\2%\u008c\3\2\2\2\'\u008e\3\2\2\2)\u0090\3\2\2\2"+
		"+\u0092\3\2\2\2-\u0094\3\2\2\2/\u0096\3\2\2\2\61\u0098\3\2\2\2\63\u009b"+
		"\3\2\2\2\65\u009f\3\2\2\2\67\u00d8\3\2\2\29\u00eb\3\2\2\2;\u00ed\3\2\2"+
		"\2=\u00f0\3\2\2\2?\u00f3\3\2\2\2A\u00f6\3\2\2\2C\u00f9\3\2\2\2E\u00fc"+
		"\3\2\2\2G\u0100\3\2\2\2I\u0115\3\2\2\2K\u0117\3\2\2\2M\u011f\3\2\2\2O"+
		"\u0127\3\2\2\2Q\u012f\3\2\2\2S\u0131\3\2\2\2U\u0135\3\2\2\2W\u0141\3\2"+
		"\2\2Y\u0147\3\2\2\2[\\\7c\2\2\\\4\3\2\2\2]^\t\2\2\2^_\t\3\2\2_`\t\4\2"+
		"\2`a\t\5\2\2a\6\3\2\2\2bc\t\6\2\2cd\t\7\2\2de\t\5\2\2ef\t\b\2\2fg\t\t"+
		"\2\2gh\t\n\2\2h\b\3\2\2\2ij\t\b\2\2jk\t\3\2\2kl\t\13\2\2lm\t\4\2\2mn\t"+
		"\5\2\2n\n\3\2\2\2op\t\f\2\2pq\t\7\2\2qr\t\r\2\2rs\t\5\2\2s\f\3\2\2\2t"+
		"u\7\60\2\2u\16\3\2\2\2vw\7]\2\2w\20\3\2\2\2xy\7_\2\2y\22\3\2\2\2z{\7}"+
		"\2\2{\24\3\2\2\2|}\7\177\2\2}\26\3\2\2\2~\177\7A\2\2\177\30\3\2\2\2\u0080"+
		"\u0081\7$\2\2\u0081\32\3\2\2\2\u0082\u0083\7)\2\2\u0083\34\3\2\2\2\u0084"+
		"\u0085\7a\2\2\u0085\36\3\2\2\2\u0086\u0087\7/\2\2\u0087 \3\2\2\2\u0088"+
		"\u0089\7%\2\2\u0089\"\3\2\2\2\u008a\u008b\7\'\2\2\u008b$\3\2\2\2\u008c"+
		"\u008d\7-\2\2\u008d&\3\2\2\2\u008e\u008f\7?\2\2\u008f(\3\2\2\2\u0090\u0091"+
		"\7<\2\2\u0091*\3\2\2\2\u0092\u0093\7>\2\2\u0093,\3\2\2\2\u0094\u0095\7"+
		"@\2\2\u0095.\3\2\2\2\u0096\u0097\7\61\2\2\u0097\60\3\2\2\2\u0098\u0099"+
		"\7]\2\2\u0099\u009a\7_\2\2\u009a\62\3\2\2\2\u009b\u009c\7a\2\2\u009c\u009d"+
		"\7<\2\2\u009d\64\3\2\2\2\u009e\u00a0\5S*\2\u009f\u009e\3\2\2\2\u00a0\u00a1"+
		"\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\66\3\2\2\2\u00a3"+
		"\u00a5\5S*\2\u00a4\u00a3\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a4\3\2\2"+
		"\2\u00a6\u00a7\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00ac\5\r\7\2\u00a9\u00ab"+
		"\5S*\2\u00aa\u00a9\3\2\2\2\u00ab\u00ae\3\2\2\2\u00ac\u00aa\3\2\2\2\u00ac"+
		"\u00ad\3\2\2\2\u00ad\u00af\3\2\2\2\u00ae\u00ac\3\2\2\2\u00af\u00b1\t\5"+
		"\2\2\u00b0\u00b2\t\16\2\2\u00b1\u00b0\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2"+
		"\u00b6\3\2\2\2\u00b3\u00b5\5S*\2\u00b4\u00b3\3\2\2\2\u00b5\u00b8\3\2\2"+
		"\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00d9\3\2\2\2\u00b8\u00b6"+
		"\3\2\2\2\u00b9\u00bb\5\r\7\2\u00ba\u00bc\5S*\2\u00bb\u00ba\3\2\2\2\u00bc"+
		"\u00bd\3\2\2\2\u00bd\u00bb\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00bf\3\2"+
		"\2\2\u00bf\u00c1\t\5\2\2\u00c0\u00c2\t\16\2\2\u00c1\u00c0\3\2\2\2\u00c1"+
		"\u00c2\3\2\2\2\u00c2\u00c6\3\2\2\2\u00c3\u00c5\5S*\2\u00c4\u00c3\3\2\2"+
		"\2\u00c5\u00c8\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00d9"+
		"\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c9\u00cb\5S*\2\u00ca\u00c9\3\2\2\2\u00cb"+
		"\u00cc\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cc\u00cd\3\2\2\2\u00cd\u00ce\3\2"+
		"\2\2\u00ce\u00d0\t\5\2\2\u00cf\u00d1\t\16\2\2\u00d0\u00cf\3\2\2\2\u00d0"+
		"\u00d1\3\2\2\2\u00d1\u00d5\3\2\2\2\u00d2\u00d4\5S*\2\u00d3\u00d2\3\2\2"+
		"\2\u00d4\u00d7\3\2\2\2\u00d5\u00d3\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d9"+
		"\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d8\u00a4\3\2\2\2\u00d8\u00b9\3\2\2\2\u00d8"+
		"\u00ca\3\2\2\2\u00d98\3\2\2\2\u00da\u00dc\5S*\2\u00db\u00da\3\2\2\2\u00dc"+
		"\u00dd\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00df\3\2"+
		"\2\2\u00df\u00e1\5\r\7\2\u00e0\u00e2\5S*\2\u00e1\u00e0\3\2\2\2\u00e2\u00e3"+
		"\3\2\2\2\u00e3\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00ec\3\2\2\2\u00e5"+
		"\u00e7\5\r\7\2\u00e6\u00e8\5S*\2\u00e7\u00e6\3\2\2\2\u00e8\u00e9\3\2\2"+
		"\2\u00e9\u00e7\3\2\2\2\u00e9\u00ea\3\2\2\2\u00ea\u00ec\3\2\2\2\u00eb\u00db"+
		"\3\2\2\2\u00eb\u00e5\3\2\2\2\u00ec:\3\2\2\2\u00ed\u00ee\5%\23\2\u00ee"+
		"\u00ef\5\65\33\2\u00ef<\3\2\2\2\u00f0\u00f1\5\37\20\2\u00f1\u00f2\5\65"+
		"\33\2\u00f2>\3\2\2\2\u00f3\u00f4\5%\23\2\u00f4\u00f5\5\67\34\2\u00f5@"+
		"\3\2\2\2\u00f6\u00f7\5\37\20\2\u00f7\u00f8\5\67\34\2\u00f8B\3\2\2\2\u00f9"+
		"\u00fa\5%\23\2\u00fa\u00fb\59\35\2\u00fbD\3\2\2\2\u00fc\u00fd\5\37\20"+
		"\2\u00fd\u00fe\59\35\2\u00feF\3\2\2\2\u00ff\u0101\t\17\2\2\u0100\u00ff"+
		"\3\2\2\2\u0101\u0102\3\2\2\2\u0102\u0100\3\2\2\2\u0102\u0103\3\2\2\2\u0103"+
		"\u010c\3\2\2\2\u0104\u0106\5\37\20\2\u0105\u0107\t\20\2\2\u0106\u0105"+
		"\3\2\2\2\u0107\u0108\3\2\2\2\u0108\u0106\3\2\2\2\u0108\u0109\3\2\2\2\u0109"+
		"\u010b\3\2\2\2\u010a\u0104\3\2\2\2\u010b\u010e\3\2\2\2\u010c\u010a\3\2"+
		"\2\2\u010c\u010d\3\2\2\2\u010dH\3\2\2\2\u010e\u010c\3\2\2\2\u010f\u0111"+
		"\5W,\2\u0110\u010f\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0110\3\2\2\2\u0112"+
		"\u0113\3\2\2\2\u0113\u0116\3\2\2\2\u0114\u0116\5G$\2\u0115\u0110\3\2\2"+
		"\2\u0115\u0114\3\2\2\2\u0116J\3\2\2\2\u0117\u0119\5\31\r\2\u0118\u011a"+
		"\n\21\2\2\u0119\u0118\3\2\2\2\u011a\u011b\3\2\2\2\u011b\u0119\3\2\2\2"+
		"\u011b\u011c\3\2\2\2\u011c\u011d\3\2\2\2\u011d\u011e\5\31\r\2\u011eL\3"+
		"\2\2\2\u011f\u0121\5\23\n\2\u0120\u0122\n\21\2\2\u0121\u0120\3\2\2\2\u0122"+
		"\u0123\3\2\2\2\u0123\u0124\3\2\2\2\u0123\u0121\3\2\2\2\u0124\u0125\3\2"+
		"\2\2\u0125\u0126\5\25\13\2\u0126N\3\2\2\2\u0127\u0129\5+\26\2\u0128\u012a"+
		"\n\21\2\2\u0129\u0128\3\2\2\2\u012a\u012b\3\2\2\2\u012b\u012c\3\2\2\2"+
		"\u012b\u0129\3\2\2\2\u012c\u012d\3\2\2\2\u012d\u012e\5-\27\2\u012eP\3"+
		"\2\2\2\u012f\u0130\t\22\2\2\u0130R\3\2\2\2\u0131\u0132\4\62;\2\u0132T"+
		"\3\2\2\2\u0133\u0136\5Q)\2\u0134\u0136\5S*\2\u0135\u0133\3\2\2\2\u0135"+
		"\u0134\3\2\2\2\u0136V\3\2\2\2\u0137\u0142\5U+\2\u0138\u0142\5S*\2\u0139"+
		"\u0142\5\35\17\2\u013a\u0142\5\37\20\2\u013b\u0142\5\r\7\2\u013c\u0142"+
		"\5!\21\2\u013d\u0142\5\27\f\2\u013e\u0142\5/\30\2\u013f\u0142\5#\22\2"+
		"\u0140\u0142\5\'\24\2\u0141\u0137\3\2\2\2\u0141\u0138\3\2\2\2\u0141\u0139"+
		"\3\2\2\2\u0141\u013a\3\2\2\2\u0141\u013b\3\2\2\2\u0141\u013c\3\2\2\2\u0141"+
		"\u013d\3\2\2\2\u0141\u013e\3\2\2\2\u0141\u013f\3\2\2\2\u0141\u0140\3\2"+
		"\2\2\u0142X\3\2\2\2\u0143\u0148\t\23\2\2\u0144\u0145\7\17\2\2\u0145\u0148"+
		"\7\f\2\2\u0146\u0148\t\24\2\2\u0147\u0143\3\2\2\2\u0147\u0144\3\2\2\2"+
		"\u0147\u0146\3\2\2\2\u0148\u0149\3\2\2\2\u0149\u0147\3\2\2\2\u0149\u014a"+
		"\3\2\2\2\u014a\u014b\3\2\2\2\u014b\u014c\b-\2\2\u014cZ\3\2\2\2\37\2\u00a1"+
		"\u00a6\u00ac\u00b1\u00b6\u00bd\u00c1\u00c6\u00cc\u00d0\u00d5\u00d8\u00dd"+
		"\u00e3\u00e9\u00eb\u0102\u0108\u010c\u0112\u0115\u011b\u0123\u012b\u0135"+
		"\u0141\u0147\u0149\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}