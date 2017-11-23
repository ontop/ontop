// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/java/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA.g4 by ANTLR 4.7
package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.term.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TurtleOBDAParser extends AbstractTurtleOBDAParser {
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
		URI_REF=57, WS=58, NAME_CHAR=59, VARNAME=60;
	public static final int
		RULE_parse = 0, RULE_directiveStatement = 1, RULE_triplesStatement = 2, 
		RULE_directive = 3, RULE_base = 4, RULE_prefixID = 5, RULE_prefix = 6, 
		RULE_triples = 7, RULE_predicateObjectList = 8, RULE_predicateObject = 9, 
		RULE_verb = 10, RULE_objectList = 11, RULE_subject = 12, RULE_object = 13, 
		RULE_resource = 14, RULE_uriref = 15, RULE_prefixedName = 16, RULE_blank = 17, 
		RULE_variable = 18, RULE_function = 19, RULE_typedLiteral = 20, RULE_language = 21, 
		RULE_terms = 22, RULE_term = 23, RULE_literal = 24, RULE_stringLiteral = 25, 
		RULE_dataTypeString = 26, RULE_numericLiteral = 27, RULE_languageTag = 28, 
		RULE_booleanLiteral = 29, RULE_numericUnsigned = 30, RULE_numericPositive = 31, 
		RULE_numericNegative = 32;
	public static final String[] ruleNames = {
		"parse", "directiveStatement", "triplesStatement", "directive", "base", 
		"prefixID", "prefix", "triples", "predicateObjectList", "predicateObject", 
		"verb", "objectList", "subject", "object", "resource", "uriref", "prefixedName", 
		"blank", "variable", "function", "typedLiteral", "language", "terms", 
		"term", "literal", "stringLiteral", "dataTypeString", "numericLiteral", 
		"languageTag", "booleanLiteral", "numericUnsigned", "numericPositive", 
		"numericNegative"
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
		"STRING_WITH_CURLY_BRACKET", "URI_REF", "WS", "NAME_CHAR", "VARNAME"
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

	@Override
	public String getGrammarFileName() { return "TurtleOBDA.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TurtleOBDAParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParseContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(TurtleOBDAParser.EOF, 0); }
		public List<DirectiveStatementContext> directiveStatement() {
			return getRuleContexts(DirectiveStatementContext.class);
		}
		public DirectiveStatementContext directiveStatement(int i) {
			return getRuleContext(DirectiveStatementContext.class,i);
		}
		public List<TriplesStatementContext> triplesStatement() {
			return getRuleContexts(TriplesStatementContext.class);
		}
		public TriplesStatementContext triplesStatement(int i) {
			return getRuleContext(TriplesStatementContext.class,i);
		}
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitParse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitParse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(66);
				directiveStatement();
				}
				}
				setState(71);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(73); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(72);
				triplesStatement();
				}
				}
				setState(75); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BLANK) | (1L << BLANK_PREFIX) | (1L << PREFIXED_NAME) | (1L << STRING_WITH_CURLY_BRACKET) | (1L << URI_REF))) != 0) );
			setState(77);
			match(EOF);
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

	public static class DirectiveStatementContext extends ParserRuleContext {
		public DirectiveContext directive() {
			return getRuleContext(DirectiveContext.class,0);
		}
		public TerminalNode PERIOD() { return getToken(TurtleOBDAParser.PERIOD, 0); }
		public DirectiveStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directiveStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterDirectiveStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitDirectiveStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitDirectiveStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveStatementContext directiveStatement() throws RecognitionException {
		DirectiveStatementContext _localctx = new DirectiveStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_directiveStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			directive();
			setState(80);
			match(PERIOD);
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

	public static class TriplesStatementContext extends ParserRuleContext {
		public TriplesContext triples() {
			return getRuleContext(TriplesContext.class,0);
		}
		public TerminalNode PERIOD() { return getToken(TurtleOBDAParser.PERIOD, 0); }
		public TriplesStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_triplesStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterTriplesStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitTriplesStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTriplesStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TriplesStatementContext triplesStatement() throws RecognitionException {
		TriplesStatementContext _localctx = new TriplesStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_triplesStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			triples();
			setState(83);
			match(PERIOD);
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

	public static class DirectiveContext extends ParserRuleContext {
		public BaseContext base() {
			return getRuleContext(BaseContext.class,0);
		}
		public PrefixIDContext prefixID() {
			return getRuleContext(PrefixIDContext.class,0);
		}
		public DirectiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterDirective(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitDirective(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitDirective(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveContext directive() throws RecognitionException {
		DirectiveContext _localctx = new DirectiveContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_directive);
		try {
			setState(87);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(85);
				base();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(86);
				prefixID();
				}
				break;
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

	public static class BaseContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(TurtleOBDAParser.AT, 0); }
		public TerminalNode BASE_KW() { return getToken(TurtleOBDAParser.BASE_KW, 0); }
		public UrirefContext uriref() {
			return getRuleContext(UrirefContext.class,0);
		}
		public BaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_base; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterBase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitBase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitBase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BaseContext base() throws RecognitionException {
		BaseContext _localctx = new BaseContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_base);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(AT);
			setState(90);
			match(BASE_KW);
			setState(91);
			uriref();
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

	public static class PrefixIDContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(TurtleOBDAParser.AT, 0); }
		public TerminalNode PREFIX_KW() { return getToken(TurtleOBDAParser.PREFIX_KW, 0); }
		public PrefixContext prefix() {
			return getRuleContext(PrefixContext.class,0);
		}
		public UrirefContext uriref() {
			return getRuleContext(UrirefContext.class,0);
		}
		public PrefixIDContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixID; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterPrefixID(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitPrefixID(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitPrefixID(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixIDContext prefixID() throws RecognitionException {
		PrefixIDContext _localctx = new PrefixIDContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_prefixID);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			match(AT);
			setState(94);
			match(PREFIX_KW);
			setState(95);
			prefix();
			setState(96);
			uriref();
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

	public static class PrefixContext extends ParserRuleContext {
		public TerminalNode PREFIX() { return getToken(TurtleOBDAParser.PREFIX, 0); }
		public PrefixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterPrefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitPrefix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitPrefix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixContext prefix() throws RecognitionException {
		PrefixContext _localctx = new PrefixContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_prefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			match(PREFIX);
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

	public static class TriplesContext extends ParserRuleContext {
		public SubjectContext subject() {
			return getRuleContext(SubjectContext.class,0);
		}
		public PredicateObjectListContext predicateObjectList() {
			return getRuleContext(PredicateObjectListContext.class,0);
		}
		public TriplesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_triples; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterTriples(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitTriples(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTriples(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TriplesContext triples() throws RecognitionException {
		TriplesContext _localctx = new TriplesContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_triples);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			subject();
			setState(101);
			predicateObjectList();
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

	public static class PredicateObjectListContext extends ParserRuleContext {
		public List<PredicateObjectContext> predicateObject() {
			return getRuleContexts(PredicateObjectContext.class);
		}
		public PredicateObjectContext predicateObject(int i) {
			return getRuleContext(PredicateObjectContext.class,i);
		}
		public List<TerminalNode> SEMI() { return getTokens(TurtleOBDAParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(TurtleOBDAParser.SEMI, i);
		}
		public PredicateObjectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateObjectList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterPredicateObjectList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitPredicateObjectList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitPredicateObjectList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateObjectListContext predicateObjectList() throws RecognitionException {
		PredicateObjectListContext _localctx = new PredicateObjectListContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_predicateObjectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			predicateObject();
			setState(108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMI) {
				{
				{
				setState(104);
				match(SEMI);
				setState(105);
				predicateObject();
				}
				}
				setState(110);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class PredicateObjectContext extends ParserRuleContext {
		public VerbContext verb() {
			return getRuleContext(VerbContext.class,0);
		}
		public ObjectListContext objectList() {
			return getRuleContext(ObjectListContext.class,0);
		}
		public PredicateObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateObject; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterPredicateObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitPredicateObject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitPredicateObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateObjectContext predicateObject() throws RecognitionException {
		PredicateObjectContext _localctx = new PredicateObjectContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_predicateObject);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			verb();
			setState(112);
			objectList();
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

	public static class VerbContext extends ParserRuleContext {
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public VerbContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_verb; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterVerb(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitVerb(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitVerb(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VerbContext verb() throws RecognitionException {
		VerbContext _localctx = new VerbContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_verb);
		try {
			setState(116);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PREFIXED_NAME:
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(114);
				resource();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(115);
				match(T__0);
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

	public static class ObjectListContext extends ParserRuleContext {
		public List<ObjectContext> object() {
			return getRuleContexts(ObjectContext.class);
		}
		public ObjectContext object(int i) {
			return getRuleContext(ObjectContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TurtleOBDAParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TurtleOBDAParser.COMMA, i);
		}
		public ObjectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterObjectList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitObjectList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitObjectList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectListContext objectList() throws RecognitionException {
		ObjectListContext _localctx = new ObjectListContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_objectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			object();
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(119);
				match(COMMA);
				setState(120);
				object();
				}
				}
				setState(125);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class SubjectContext extends ParserRuleContext {
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public BlankContext blank() {
			return getRuleContext(BlankContext.class,0);
		}
		public SubjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subject; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterSubject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitSubject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitSubject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubjectContext subject() throws RecognitionException {
		SubjectContext _localctx = new SubjectContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_subject);
		try {
			setState(129);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PREFIXED_NAME:
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(126);
				resource();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(127);
				variable();
				}
				break;
			case BLANK:
			case BLANK_PREFIX:
				enterOuterAlt(_localctx, 3);
				{
				setState(128);
				blank();
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

	public static class ObjectContext extends ParserRuleContext {
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TypedLiteralContext typedLiteral() {
			return getRuleContext(TypedLiteralContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public ObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitObject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_object);
		try {
			setState(135);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(131);
				resource();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(132);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(133);
				typedLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(134);
				variable();
				}
				break;
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

	public static class ResourceContext extends ParserRuleContext {
		public UrirefContext uriref() {
			return getRuleContext(UrirefContext.class,0);
		}
		public PrefixedNameContext prefixedName() {
			return getRuleContext(PrefixedNameContext.class,0);
		}
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_resource);
		try {
			setState(139);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(137);
				uriref();
				}
				break;
			case PREFIXED_NAME:
				enterOuterAlt(_localctx, 2);
				{
				setState(138);
				prefixedName();
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

	public static class UrirefContext extends ParserRuleContext {
		public TerminalNode URI_REF() { return getToken(TurtleOBDAParser.URI_REF, 0); }
		public UrirefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uriref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterUriref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitUriref(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitUriref(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UrirefContext uriref() throws RecognitionException {
		UrirefContext _localctx = new UrirefContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_uriref);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			match(URI_REF);
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

	public static class PrefixedNameContext extends ParserRuleContext {
		public TerminalNode PREFIXED_NAME() { return getToken(TurtleOBDAParser.PREFIXED_NAME, 0); }
		public PrefixedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterPrefixedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitPrefixedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitPrefixedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixedNameContext prefixedName() throws RecognitionException {
		PrefixedNameContext _localctx = new PrefixedNameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_prefixedName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			match(PREFIXED_NAME);
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

	public static class BlankContext extends ParserRuleContext {
		public TerminalNode BLANK_PREFIX() { return getToken(TurtleOBDAParser.BLANK_PREFIX, 0); }
		public List<TerminalNode> NAME_CHAR() { return getTokens(TurtleOBDAParser.NAME_CHAR); }
		public TerminalNode NAME_CHAR(int i) {
			return getToken(TurtleOBDAParser.NAME_CHAR, i);
		}
		public TerminalNode BLANK() { return getToken(TurtleOBDAParser.BLANK, 0); }
		public BlankContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blank; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterBlank(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitBlank(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitBlank(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlankContext blank() throws RecognitionException {
		BlankContext _localctx = new BlankContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_blank);
		int _la;
		try {
			setState(152);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BLANK_PREFIX:
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				match(BLANK_PREFIX);
				setState(147); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(146);
					match(NAME_CHAR);
					}
					}
					setState(149); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==NAME_CHAR );
				}
				break;
			case BLANK:
				enterOuterAlt(_localctx, 2);
				{
				setState(151);
				match(BLANK);
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

	public static class VariableContext extends ParserRuleContext {
		public TerminalNode STRING_WITH_CURLY_BRACKET() { return getToken(TurtleOBDAParser.STRING_WITH_CURLY_BRACKET, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			match(STRING_WITH_CURLY_BRACKET);
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

	public static class FunctionContext extends ParserRuleContext {
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(TurtleOBDAParser.LPAREN, 0); }
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(TurtleOBDAParser.RPAREN, 0); }
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_function);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			resource();
			setState(157);
			match(LPAREN);
			setState(158);
			terms();
			setState(159);
			match(RPAREN);
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

	public static class TypedLiteralContext extends ParserRuleContext {
		public TypedLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typedLiteral; }
	 
		public TypedLiteralContext() { }
		public void copyFrom(TypedLiteralContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TypedLiteral_2Context extends TypedLiteralContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode REFERENCE() { return getToken(TurtleOBDAParser.REFERENCE, 0); }
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public TypedLiteral_2Context(TypedLiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterTypedLiteral_2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitTypedLiteral_2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTypedLiteral_2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TypedLiteral_1Context extends TypedLiteralContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode AT() { return getToken(TurtleOBDAParser.AT, 0); }
		public LanguageContext language() {
			return getRuleContext(LanguageContext.class,0);
		}
		public TypedLiteral_1Context(TypedLiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterTypedLiteral_1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitTypedLiteral_1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTypedLiteral_1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypedLiteralContext typedLiteral() throws RecognitionException {
		TypedLiteralContext _localctx = new TypedLiteralContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_typedLiteral);
		try {
			setState(169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				_localctx = new TypedLiteral_1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(161);
				variable();
				setState(162);
				match(AT);
				setState(163);
				language();
				}
				break;
			case 2:
				_localctx = new TypedLiteral_2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(165);
				variable();
				setState(166);
				match(REFERENCE);
				setState(167);
				resource();
				}
				break;
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

	public static class LanguageContext extends ParserRuleContext {
		public LanguageTagContext languageTag() {
			return getRuleContext(LanguageTagContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public LanguageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_language; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterLanguage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitLanguage(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitLanguage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LanguageContext language() throws RecognitionException {
		LanguageContext _localctx = new LanguageContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_language);
		try {
			setState(173);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VARNAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(171);
				languageTag();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(172);
				variable();
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

	public static class TermsContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TurtleOBDAParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TurtleOBDAParser.COMMA, i);
		}
		public TermsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_terms; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterTerms(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitTerms(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTerms(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermsContext terms() throws RecognitionException {
		TermsContext _localctx = new TermsContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_terms);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			term();
			setState(180);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(176);
				match(COMMA);
				setState(177);
				term();
				}
				}
				setState(182);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class TermContext extends ParserRuleContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_term);
		try {
			setState(186);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PREFIXED_NAME:
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(183);
				function();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(184);
				variable();
				}
				break;
			case FALSE:
			case TRUE:
			case INTEGER:
			case DOUBLE:
			case DECIMAL:
			case INTEGER_POSITIVE:
			case INTEGER_NEGATIVE:
			case DOUBLE_POSITIVE:
			case DOUBLE_NEGATIVE:
			case DECIMAL_POSITIVE:
			case DECIMAL_NEGATIVE:
			case STRING_WITH_QUOTE_DOUBLE:
				enterOuterAlt(_localctx, 3);
				{
				setState(185);
				literal();
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

	public static class LiteralContext extends ParserRuleContext {
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public TerminalNode AT() { return getToken(TurtleOBDAParser.AT, 0); }
		public LanguageContext language() {
			return getRuleContext(LanguageContext.class,0);
		}
		public DataTypeStringContext dataTypeString() {
			return getRuleContext(DataTypeStringContext.class,0);
		}
		public NumericLiteralContext numericLiteral() {
			return getRuleContext(NumericLiteralContext.class,0);
		}
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_literal);
		int _la;
		try {
			setState(196);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(188);
				stringLiteral();
				setState(191);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AT) {
					{
					setState(189);
					match(AT);
					setState(190);
					language();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(193);
				dataTypeString();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(194);
				numericLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(195);
				booleanLiteral();
				}
				break;
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

	public static class StringLiteralContext extends ParserRuleContext {
		public TerminalNode STRING_WITH_QUOTE_DOUBLE() { return getToken(TurtleOBDAParser.STRING_WITH_QUOTE_DOUBLE, 0); }
		public StringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralContext stringLiteral() throws RecognitionException {
		StringLiteralContext _localctx = new StringLiteralContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			match(STRING_WITH_QUOTE_DOUBLE);
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

	public static class DataTypeStringContext extends ParserRuleContext {
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public TerminalNode REFERENCE() { return getToken(TurtleOBDAParser.REFERENCE, 0); }
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public DataTypeStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterDataTypeString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitDataTypeString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitDataTypeString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeStringContext dataTypeString() throws RecognitionException {
		DataTypeStringContext _localctx = new DataTypeStringContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_dataTypeString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			stringLiteral();
			setState(201);
			match(REFERENCE);
			setState(202);
			resource();
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

	public static class NumericLiteralContext extends ParserRuleContext {
		public NumericUnsignedContext numericUnsigned() {
			return getRuleContext(NumericUnsignedContext.class,0);
		}
		public NumericPositiveContext numericPositive() {
			return getRuleContext(NumericPositiveContext.class,0);
		}
		public NumericNegativeContext numericNegative() {
			return getRuleContext(NumericNegativeContext.class,0);
		}
		public NumericLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitNumericLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_numericLiteral);
		try {
			setState(207);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
			case DOUBLE:
			case DECIMAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(204);
				numericUnsigned();
				}
				break;
			case INTEGER_POSITIVE:
			case DOUBLE_POSITIVE:
			case DECIMAL_POSITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(205);
				numericPositive();
				}
				break;
			case INTEGER_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case DECIMAL_NEGATIVE:
				enterOuterAlt(_localctx, 3);
				{
				setState(206);
				numericNegative();
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

	public static class LanguageTagContext extends ParserRuleContext {
		public TerminalNode VARNAME() { return getToken(TurtleOBDAParser.VARNAME, 0); }
		public LanguageTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_languageTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterLanguageTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitLanguageTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitLanguageTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LanguageTagContext languageTag() throws RecognitionException {
		LanguageTagContext _localctx = new LanguageTagContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_languageTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			match(VARNAME);
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

	public static class BooleanLiteralContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(TurtleOBDAParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(TurtleOBDAParser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			_la = _input.LA(1);
			if ( !(_la==FALSE || _la==TRUE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class NumericUnsignedContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(TurtleOBDAParser.INTEGER, 0); }
		public TerminalNode DOUBLE() { return getToken(TurtleOBDAParser.DOUBLE, 0); }
		public TerminalNode DECIMAL() { return getToken(TurtleOBDAParser.DECIMAL, 0); }
		public NumericUnsignedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericUnsigned; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterNumericUnsigned(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitNumericUnsigned(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericUnsigned(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericUnsignedContext numericUnsigned() throws RecognitionException {
		NumericUnsignedContext _localctx = new NumericUnsignedContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_numericUnsigned);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INTEGER) | (1L << DOUBLE) | (1L << DECIMAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class NumericPositiveContext extends ParserRuleContext {
		public TerminalNode INTEGER_POSITIVE() { return getToken(TurtleOBDAParser.INTEGER_POSITIVE, 0); }
		public TerminalNode DOUBLE_POSITIVE() { return getToken(TurtleOBDAParser.DOUBLE_POSITIVE, 0); }
		public TerminalNode DECIMAL_POSITIVE() { return getToken(TurtleOBDAParser.DECIMAL_POSITIVE, 0); }
		public NumericPositiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericPositive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterNumericPositive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitNumericPositive(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericPositive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericPositiveContext numericPositive() throws RecognitionException {
		NumericPositiveContext _localctx = new NumericPositiveContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_numericPositive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INTEGER_POSITIVE) | (1L << DOUBLE_POSITIVE) | (1L << DECIMAL_POSITIVE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class NumericNegativeContext extends ParserRuleContext {
		public TerminalNode INTEGER_NEGATIVE() { return getToken(TurtleOBDAParser.INTEGER_NEGATIVE, 0); }
		public TerminalNode DOUBLE_NEGATIVE() { return getToken(TurtleOBDAParser.DOUBLE_NEGATIVE, 0); }
		public TerminalNode DECIMAL_NEGATIVE() { return getToken(TurtleOBDAParser.DECIMAL_NEGATIVE, 0); }
		public NumericNegativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericNegative; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterNumericNegative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitNumericNegative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericNegative(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericNegativeContext numericNegative() throws RecognitionException {
		NumericNegativeContext _localctx = new NumericNegativeContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_numericNegative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INTEGER_NEGATIVE) | (1L << DOUBLE_NEGATIVE) | (1L << DECIMAL_NEGATIVE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3>\u00de\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\3\2\7\2F\n\2\f\2\16\2I\13\2\3\2\6\2L\n\2\r\2\16\2M\3\2\3\2"+
		"\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\5\5Z\n\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7"+
		"\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\7\nm\n\n\f\n\16\np\13\n\3\13"+
		"\3\13\3\13\3\f\3\f\5\fw\n\f\3\r\3\r\3\r\7\r|\n\r\f\r\16\r\177\13\r\3\16"+
		"\3\16\3\16\5\16\u0084\n\16\3\17\3\17\3\17\3\17\5\17\u008a\n\17\3\20\3"+
		"\20\5\20\u008e\n\20\3\21\3\21\3\22\3\22\3\23\3\23\6\23\u0096\n\23\r\23"+
		"\16\23\u0097\3\23\5\23\u009b\n\23\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u00ac\n\26\3\27\3\27\5\27"+
		"\u00b0\n\27\3\30\3\30\3\30\7\30\u00b5\n\30\f\30\16\30\u00b8\13\30\3\31"+
		"\3\31\3\31\5\31\u00bd\n\31\3\32\3\32\3\32\5\32\u00c2\n\32\3\32\3\32\3"+
		"\32\5\32\u00c7\n\32\3\33\3\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35\5\35"+
		"\u00d2\n\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3\"\2\2#\2\4\6\b\n"+
		"\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@B\2\6\3\2\6\7\3"+
		"\2-/\5\2\60\60\62\62\64\64\5\2\61\61\63\63\65\65\2\u00d5\2G\3\2\2\2\4"+
		"Q\3\2\2\2\6T\3\2\2\2\bY\3\2\2\2\n[\3\2\2\2\f_\3\2\2\2\16d\3\2\2\2\20f"+
		"\3\2\2\2\22i\3\2\2\2\24q\3\2\2\2\26v\3\2\2\2\30x\3\2\2\2\32\u0083\3\2"+
		"\2\2\34\u0089\3\2\2\2\36\u008d\3\2\2\2 \u008f\3\2\2\2\"\u0091\3\2\2\2"+
		"$\u009a\3\2\2\2&\u009c\3\2\2\2(\u009e\3\2\2\2*\u00ab\3\2\2\2,\u00af\3"+
		"\2\2\2.\u00b1\3\2\2\2\60\u00bc\3\2\2\2\62\u00c6\3\2\2\2\64\u00c8\3\2\2"+
		"\2\66\u00ca\3\2\2\28\u00d1\3\2\2\2:\u00d3\3\2\2\2<\u00d5\3\2\2\2>\u00d7"+
		"\3\2\2\2@\u00d9\3\2\2\2B\u00db\3\2\2\2DF\5\4\3\2ED\3\2\2\2FI\3\2\2\2G"+
		"E\3\2\2\2GH\3\2\2\2HK\3\2\2\2IG\3\2\2\2JL\5\6\4\2KJ\3\2\2\2LM\3\2\2\2"+
		"MK\3\2\2\2MN\3\2\2\2NO\3\2\2\2OP\7\2\2\3P\3\3\2\2\2QR\5\b\5\2RS\7\f\2"+
		"\2S\5\3\2\2\2TU\5\20\t\2UV\7\f\2\2V\7\3\2\2\2WZ\5\n\6\2XZ\5\f\7\2YW\3"+
		"\2\2\2YX\3\2\2\2Z\t\3\2\2\2[\\\7\35\2\2\\]\7\4\2\2]^\5 \21\2^\13\3\2\2"+
		"\2_`\7\35\2\2`a\7\5\2\2ab\5\16\b\2bc\5 \21\2c\r\3\2\2\2de\78\2\2e\17\3"+
		"\2\2\2fg\5\32\16\2gh\5\22\n\2h\21\3\2\2\2in\5\24\13\2jk\7\13\2\2km\5\24"+
		"\13\2lj\3\2\2\2mp\3\2\2\2nl\3\2\2\2no\3\2\2\2o\23\3\2\2\2pn\3\2\2\2qr"+
		"\5\26\f\2rs\5\30\r\2s\25\3\2\2\2tw\5\36\20\2uw\7\3\2\2vt\3\2\2\2vu\3\2"+
		"\2\2w\27\3\2\2\2x}\5\34\17\2yz\7\r\2\2z|\5\34\17\2{y\3\2\2\2|\177\3\2"+
		"\2\2}{\3\2\2\2}~\3\2\2\2~\31\3\2\2\2\177}\3\2\2\2\u0080\u0084\5\36\20"+
		"\2\u0081\u0084\5&\24\2\u0082\u0084\5$\23\2\u0083\u0080\3\2\2\2\u0083\u0081"+
		"\3\2\2\2\u0083\u0082\3\2\2\2\u0084\33\3\2\2\2\u0085\u008a\5\36\20\2\u0086"+
		"\u008a\5\62\32\2\u0087\u008a\5*\26\2\u0088\u008a\5&\24\2\u0089\u0085\3"+
		"\2\2\2\u0089\u0086\3\2\2\2\u0089\u0087\3\2\2\2\u0089\u0088\3\2\2\2\u008a"+
		"\35\3\2\2\2\u008b\u008e\5 \21\2\u008c\u008e\5\"\22\2\u008d\u008b\3\2\2"+
		"\2\u008d\u008c\3\2\2\2\u008e\37\3\2\2\2\u008f\u0090\7;\2\2\u0090!\3\2"+
		"\2\2\u0091\u0092\7\66\2\2\u0092#\3\2\2\2\u0093\u0095\7*\2\2\u0094\u0096"+
		"\7=\2\2\u0095\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0095\3\2\2\2\u0097"+
		"\u0098\3\2\2\2\u0098\u009b\3\2\2\2\u0099\u009b\7)\2\2\u009a\u0093\3\2"+
		"\2\2\u009a\u0099\3\2\2\2\u009b%\3\2\2\2\u009c\u009d\7:\2\2\u009d\'\3\2"+
		"\2\2\u009e\u009f\5\36\20\2\u009f\u00a0\7\22\2\2\u00a0\u00a1\5.\30\2\u00a1"+
		"\u00a2\7\23\2\2\u00a2)\3\2\2\2\u00a3\u00a4\5&\24\2\u00a4\u00a5\7\35\2"+
		"\2\u00a5\u00a6\5,\27\2\u00a6\u00ac\3\2\2\2\u00a7\u00a8\5&\24\2\u00a8\u00a9"+
		"\7\b\2\2\u00a9\u00aa\5\36\20\2\u00aa\u00ac\3\2\2\2\u00ab\u00a3\3\2\2\2"+
		"\u00ab\u00a7\3\2\2\2\u00ac+\3\2\2\2\u00ad\u00b0\5:\36\2\u00ae\u00b0\5"+
		"&\24\2\u00af\u00ad\3\2\2\2\u00af\u00ae\3\2\2\2\u00b0-\3\2\2\2\u00b1\u00b6"+
		"\5\60\31\2\u00b2\u00b3\7\r\2\2\u00b3\u00b5\5\60\31\2\u00b4\u00b2\3\2\2"+
		"\2\u00b5\u00b8\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7/"+
		"\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b9\u00bd\5(\25\2\u00ba\u00bd\5&\24\2\u00bb"+
		"\u00bd\5\62\32\2\u00bc\u00b9\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bb\3"+
		"\2\2\2\u00bd\61\3\2\2\2\u00be\u00c1\5\64\33\2\u00bf\u00c0\7\35\2\2\u00c0"+
		"\u00c2\5,\27\2\u00c1\u00bf\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c7\3\2"+
		"\2\2\u00c3\u00c7\5\66\34\2\u00c4\u00c7\58\35\2\u00c5\u00c7\5<\37\2\u00c6"+
		"\u00be\3\2\2\2\u00c6\u00c3\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c6\u00c5\3\2"+
		"\2\2\u00c7\63\3\2\2\2\u00c8\u00c9\79\2\2\u00c9\65\3\2\2\2\u00ca\u00cb"+
		"\5\64\33\2\u00cb\u00cc\7\b\2\2\u00cc\u00cd\5\36\20\2\u00cd\67\3\2\2\2"+
		"\u00ce\u00d2\5> \2\u00cf\u00d2\5@!\2\u00d0\u00d2\5B\"\2\u00d1\u00ce\3"+
		"\2\2\2\u00d1\u00cf\3\2\2\2\u00d1\u00d0\3\2\2\2\u00d29\3\2\2\2\u00d3\u00d4"+
		"\7>\2\2\u00d4;\3\2\2\2\u00d5\u00d6\t\2\2\2\u00d6=\3\2\2\2\u00d7\u00d8"+
		"\t\3\2\2\u00d8?\3\2\2\2\u00d9\u00da\t\4\2\2\u00daA\3\2\2\2\u00db\u00dc"+
		"\t\5\2\2\u00dcC\3\2\2\2\24GMYnv}\u0083\u0089\u008d\u0097\u009a\u00ab\u00af"+
		"\u00b6\u00bc\u00c1\u00c6\u00d1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}