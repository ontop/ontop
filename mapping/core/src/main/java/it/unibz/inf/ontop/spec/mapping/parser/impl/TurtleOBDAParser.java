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
		T__0=1, BASE_KW=2, PREFIX_KW=3, FALSE=4, TRUE=5, PERIOD=6, LSQ_BRACKET=7, 
		RSQ_BRACKET=8, LCR_BRACKET=9, RCR_BRACKET=10, QUESTION=11, QUOTE_DOUBLE=12, 
		QUOTE_SINGLE=13, UNDERSCORE=14, MINUS=15, HASH=16, PERCENT=17, PLUS=18, 
		EQUALS=19, COLON=20, LESS=21, GREATER=22, SLASH=23, BLANK=24, BLANK_PREFIX=25, 
		INTEGER=26, DOUBLE=27, DECIMAL=28, INTEGER_POSITIVE=29, INTEGER_NEGATIVE=30, 
		DOUBLE_POSITIVE=31, DOUBLE_NEGATIVE=32, DECIMAL_POSITIVE=33, DECIMAL_NEGATIVE=34, 
		LANGUAGE_STRING=35, NAME_SBS=36, STRING_WITH_QUOTE_DOUBLE=37, STRING_WITH_CURLY_BRACKET=38, 
		URI_REF=39, WS=40, AT=41, SEMI=42, COMMA=43, NAME_CHAR=44, LPAREN=45, 
		RPAREN=46, REFERENCE=47;
	public static final int
		RULE_parse = 0, RULE_directiveStatement = 1, RULE_triplesStatement = 2, 
		RULE_directive = 3, RULE_base = 4, RULE_prefixID = 5, RULE_prefix = 6, 
		RULE_triples = 7, RULE_predicateObjectList = 8, RULE_predicateObject = 9, 
		RULE_verb = 10, RULE_objectList = 11, RULE_subject = 12, RULE_object = 13, 
		RULE_resource = 14, RULE_uriref = 15, RULE_prefixedName = 16, RULE_ncNameExt = 17, 
		RULE_blank = 18, RULE_variable = 19, RULE_function = 20, RULE_typedLiteral = 21, 
		RULE_language = 22, RULE_terms = 23, RULE_term = 24, RULE_literal = 25, 
		RULE_stringLiteral = 26, RULE_dataTypeString = 27, RULE_numericLiteral = 28, 
		RULE_booleanLiteral = 29, RULE_numericUnsigned = 30, RULE_numericPositive = 31, 
		RULE_numericNegative = 32;
	public static final String[] ruleNames = {
		"parse", "directiveStatement", "triplesStatement", "directive", "base", 
		"prefixID", "prefix", "triples", "predicateObjectList", "predicateObject", 
		"verb", "objectList", "subject", "object", "resource", "uriref", "prefixedName", 
		"ncNameExt", "blank", "variable", "function", "typedLiteral", "language", 
		"terms", "term", "literal", "stringLiteral", "dataTypeString", "numericLiteral", 
		"booleanLiteral", "numericUnsigned", "numericPositive", "numericNegative"
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
		"WS", "AT", "SEMI", "COMMA", "NAME_CHAR", "LPAREN", "RPAREN", "REFERENCE"
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
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << UNDERSCORE) | (1L << COLON) | (1L << BLANK) | (1L << BLANK_PREFIX) | (1L << NAME_SBS) | (1L << STRING_WITH_CURLY_BRACKET) | (1L << URI_REF))) != 0) );
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
		public TerminalNode COLON() { return getToken(TurtleOBDAParser.COLON, 0); }
		public TerminalNode UNDERSCORE() { return getToken(TurtleOBDAParser.UNDERSCORE, 0); }
		public List<TerminalNode> NAME_SBS() { return getTokens(TurtleOBDAParser.NAME_SBS); }
		public TerminalNode NAME_SBS(int i) {
			return getToken(TurtleOBDAParser.NAME_SBS, i);
		}
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNDERSCORE || _la==NAME_SBS) {
				{
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==UNDERSCORE) {
					{
					setState(98);
					match(UNDERSCORE);
					}
				}

				setState(102); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(101);
					match(NAME_SBS);
					}
					}
					setState(104); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==NAME_SBS );
				}
			}

			setState(108);
			match(COLON);
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
			setState(110);
			subject();
			setState(111);
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
			setState(113);
			predicateObject();
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMI) {
				{
				{
				setState(114);
				match(SEMI);
				setState(115);
				predicateObject();
				}
				}
				setState(120);
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
			setState(121);
			verb();
			setState(122);
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
			setState(126);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNDERSCORE:
			case COLON:
			case NAME_SBS:
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(124);
				resource();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(125);
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
			setState(128);
			object();
			setState(133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(129);
				match(COMMA);
				setState(130);
				object();
				}
				}
				setState(135);
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
			setState(139);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNDERSCORE:
			case COLON:
			case NAME_SBS:
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				resource();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(137);
				variable();
				}
				break;
			case BLANK:
			case BLANK_PREFIX:
				enterOuterAlt(_localctx, 3);
				{
				setState(138);
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
			setState(145);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(141);
				resource();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(143);
				typedLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(144);
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
			setState(149);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(147);
				uriref();
				}
				break;
			case UNDERSCORE:
			case COLON:
			case NAME_SBS:
				enterOuterAlt(_localctx, 2);
				{
				setState(148);
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
			setState(151);
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
		public PrefixContext prefix() {
			return getRuleContext(PrefixContext.class,0);
		}
		public NcNameExtContext ncNameExt() {
			return getRuleContext(NcNameExtContext.class,0);
		}
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
			setState(153);
			prefix();
			setState(154);
			ncNameExt();
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

	public static class NcNameExtContext extends ParserRuleContext {
		public List<TerminalNode> NAME_SBS() { return getTokens(TurtleOBDAParser.NAME_SBS); }
		public TerminalNode NAME_SBS(int i) {
			return getToken(TurtleOBDAParser.NAME_SBS, i);
		}
		public List<TerminalNode> STRING_WITH_CURLY_BRACKET() { return getTokens(TurtleOBDAParser.STRING_WITH_CURLY_BRACKET); }
		public TerminalNode STRING_WITH_CURLY_BRACKET(int i) {
			return getToken(TurtleOBDAParser.STRING_WITH_CURLY_BRACKET, i);
		}
		public NcNameExtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ncNameExt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterNcNameExt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitNcNameExt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNcNameExt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NcNameExtContext ncNameExt() throws RecognitionException {
		NcNameExtContext _localctx = new NcNameExtContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_ncNameExt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(157); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(156);
					_la = _input.LA(1);
					if ( !(_la==NAME_SBS || _la==STRING_WITH_CURLY_BRACKET) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(159); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
		enterRule(_localctx, 36, RULE_blank);
		int _la;
		try {
			setState(168);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BLANK_PREFIX:
				enterOuterAlt(_localctx, 1);
				{
				setState(161);
				match(BLANK_PREFIX);
				setState(163); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(162);
					match(NAME_CHAR);
					}
					}
					setState(165); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==NAME_CHAR );
				}
				break;
			case BLANK:
				enterOuterAlt(_localctx, 2);
				{
				setState(167);
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
		enterRule(_localctx, 38, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
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
		enterRule(_localctx, 40, RULE_function);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			resource();
			setState(173);
			match(LPAREN);
			setState(174);
			terms();
			setState(175);
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
		enterRule(_localctx, 42, RULE_typedLiteral);
		try {
			setState(185);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				_localctx = new TypedLiteral_1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(177);
				variable();
				setState(178);
				match(AT);
				setState(179);
				language();
				}
				break;
			case 2:
				_localctx = new TypedLiteral_2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(181);
				variable();
				setState(182);
				match(REFERENCE);
				setState(183);
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
		public TerminalNode LANGUAGE_STRING() { return getToken(TurtleOBDAParser.LANGUAGE_STRING, 0); }
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
		enterRule(_localctx, 44, RULE_language);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			match(LANGUAGE_STRING);
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
		enterRule(_localctx, 46, RULE_terms);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			term();
			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(190);
				match(COMMA);
				setState(191);
				term();
				}
				}
				setState(196);
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
		enterRule(_localctx, 48, RULE_term);
		try {
			setState(200);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNDERSCORE:
			case COLON:
			case NAME_SBS:
			case URI_REF:
				enterOuterAlt(_localctx, 1);
				{
				setState(197);
				function();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(198);
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
				setState(199);
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
		enterRule(_localctx, 50, RULE_literal);
		int _la;
		try {
			setState(210);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(202);
				stringLiteral();
				setState(205);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AT) {
					{
					setState(203);
					match(AT);
					setState(204);
					language();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(207);
				dataTypeString();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(208);
				numericLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(209);
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
		enterRule(_localctx, 52, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
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
		enterRule(_localctx, 54, RULE_dataTypeString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			stringLiteral();
			setState(215);
			match(REFERENCE);
			setState(216);
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
		enterRule(_localctx, 56, RULE_numericLiteral);
		try {
			setState(221);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
			case DOUBLE:
			case DECIMAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(218);
				numericUnsigned();
				}
				break;
			case INTEGER_POSITIVE:
			case DOUBLE_POSITIVE:
			case DECIMAL_POSITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(219);
				numericPositive();
				}
				break;
			case INTEGER_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case DECIMAL_NEGATIVE:
				enterOuterAlt(_localctx, 3);
				{
				setState(220);
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
			setState(223);
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
			setState(225);
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
			setState(227);
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
			setState(229);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\61\u00ea\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\3\2\7\2F\n\2\f\2\16\2I\13\2\3\2\6\2L\n\2\r\2\16\2M\3\2\3\2"+
		"\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\5\5Z\n\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7"+
		"\3\7\3\7\3\b\5\bf\n\b\3\b\6\bi\n\b\r\b\16\bj\5\bm\n\b\3\b\3\b\3\t\3\t"+
		"\3\t\3\n\3\n\3\n\7\nw\n\n\f\n\16\nz\13\n\3\13\3\13\3\13\3\f\3\f\5\f\u0081"+
		"\n\f\3\r\3\r\3\r\7\r\u0086\n\r\f\r\16\r\u0089\13\r\3\16\3\16\3\16\5\16"+
		"\u008e\n\16\3\17\3\17\3\17\3\17\5\17\u0094\n\17\3\20\3\20\5\20\u0098\n"+
		"\20\3\21\3\21\3\22\3\22\3\22\3\23\6\23\u00a0\n\23\r\23\16\23\u00a1\3\24"+
		"\3\24\6\24\u00a6\n\24\r\24\16\24\u00a7\3\24\5\24\u00ab\n\24\3\25\3\25"+
		"\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\5\27"+
		"\u00bc\n\27\3\30\3\30\3\31\3\31\3\31\7\31\u00c3\n\31\f\31\16\31\u00c6"+
		"\13\31\3\32\3\32\3\32\5\32\u00cb\n\32\3\33\3\33\3\33\5\33\u00d0\n\33\3"+
		"\33\3\33\3\33\5\33\u00d5\n\33\3\34\3\34\3\35\3\35\3\35\3\35\3\36\3\36"+
		"\3\36\5\36\u00e0\n\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3\"\2\2#\2\4\6\b\n"+
		"\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@B\2\7\4\2&&((\3"+
		"\2\6\7\3\2\34\36\5\2\37\37!!##\5\2  \"\"$$\2\u00e4\2G\3\2\2\2\4Q\3\2\2"+
		"\2\6T\3\2\2\2\bY\3\2\2\2\n[\3\2\2\2\f_\3\2\2\2\16l\3\2\2\2\20p\3\2\2\2"+
		"\22s\3\2\2\2\24{\3\2\2\2\26\u0080\3\2\2\2\30\u0082\3\2\2\2\32\u008d\3"+
		"\2\2\2\34\u0093\3\2\2\2\36\u0097\3\2\2\2 \u0099\3\2\2\2\"\u009b\3\2\2"+
		"\2$\u009f\3\2\2\2&\u00aa\3\2\2\2(\u00ac\3\2\2\2*\u00ae\3\2\2\2,\u00bb"+
		"\3\2\2\2.\u00bd\3\2\2\2\60\u00bf\3\2\2\2\62\u00ca\3\2\2\2\64\u00d4\3\2"+
		"\2\2\66\u00d6\3\2\2\28\u00d8\3\2\2\2:\u00df\3\2\2\2<\u00e1\3\2\2\2>\u00e3"+
		"\3\2\2\2@\u00e5\3\2\2\2B\u00e7\3\2\2\2DF\5\4\3\2ED\3\2\2\2FI\3\2\2\2G"+
		"E\3\2\2\2GH\3\2\2\2HK\3\2\2\2IG\3\2\2\2JL\5\6\4\2KJ\3\2\2\2LM\3\2\2\2"+
		"MK\3\2\2\2MN\3\2\2\2NO\3\2\2\2OP\7\2\2\3P\3\3\2\2\2QR\5\b\5\2RS\7\b\2"+
		"\2S\5\3\2\2\2TU\5\20\t\2UV\7\b\2\2V\7\3\2\2\2WZ\5\n\6\2XZ\5\f\7\2YW\3"+
		"\2\2\2YX\3\2\2\2Z\t\3\2\2\2[\\\7+\2\2\\]\7\4\2\2]^\5 \21\2^\13\3\2\2\2"+
		"_`\7+\2\2`a\7\5\2\2ab\5\16\b\2bc\5 \21\2c\r\3\2\2\2df\7\20\2\2ed\3\2\2"+
		"\2ef\3\2\2\2fh\3\2\2\2gi\7&\2\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2"+
		"\2km\3\2\2\2le\3\2\2\2lm\3\2\2\2mn\3\2\2\2no\7\26\2\2o\17\3\2\2\2pq\5"+
		"\32\16\2qr\5\22\n\2r\21\3\2\2\2sx\5\24\13\2tu\7,\2\2uw\5\24\13\2vt\3\2"+
		"\2\2wz\3\2\2\2xv\3\2\2\2xy\3\2\2\2y\23\3\2\2\2zx\3\2\2\2{|\5\26\f\2|}"+
		"\5\30\r\2}\25\3\2\2\2~\u0081\5\36\20\2\177\u0081\7\3\2\2\u0080~\3\2\2"+
		"\2\u0080\177\3\2\2\2\u0081\27\3\2\2\2\u0082\u0087\5\34\17\2\u0083\u0084"+
		"\7-\2\2\u0084\u0086\5\34\17\2\u0085\u0083\3\2\2\2\u0086\u0089\3\2\2\2"+
		"\u0087\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088\31\3\2\2\2\u0089\u0087"+
		"\3\2\2\2\u008a\u008e\5\36\20\2\u008b\u008e\5(\25\2\u008c\u008e\5&\24\2"+
		"\u008d\u008a\3\2\2\2\u008d\u008b\3\2\2\2\u008d\u008c\3\2\2\2\u008e\33"+
		"\3\2\2\2\u008f\u0094\5\36\20\2\u0090\u0094\5\64\33\2\u0091\u0094\5,\27"+
		"\2\u0092\u0094\5(\25\2\u0093\u008f\3\2\2\2\u0093\u0090\3\2\2\2\u0093\u0091"+
		"\3\2\2\2\u0093\u0092\3\2\2\2\u0094\35\3\2\2\2\u0095\u0098\5 \21\2\u0096"+
		"\u0098\5\"\22\2\u0097\u0095\3\2\2\2\u0097\u0096\3\2\2\2\u0098\37\3\2\2"+
		"\2\u0099\u009a\7)\2\2\u009a!\3\2\2\2\u009b\u009c\5\16\b\2\u009c\u009d"+
		"\5$\23\2\u009d#\3\2\2\2\u009e\u00a0\t\2\2\2\u009f\u009e\3\2\2\2\u00a0"+
		"\u00a1\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2%\3\2\2\2"+
		"\u00a3\u00a5\7\33\2\2\u00a4\u00a6\7.\2\2\u00a5\u00a4\3\2\2\2\u00a6\u00a7"+
		"\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00ab\3\2\2\2\u00a9"+
		"\u00ab\7\32\2\2\u00aa\u00a3\3\2\2\2\u00aa\u00a9\3\2\2\2\u00ab\'\3\2\2"+
		"\2\u00ac\u00ad\7(\2\2\u00ad)\3\2\2\2\u00ae\u00af\5\36\20\2\u00af\u00b0"+
		"\7/\2\2\u00b0\u00b1\5\60\31\2\u00b1\u00b2\7\60\2\2\u00b2+\3\2\2\2\u00b3"+
		"\u00b4\5(\25\2\u00b4\u00b5\7+\2\2\u00b5\u00b6\5.\30\2\u00b6\u00bc\3\2"+
		"\2\2\u00b7\u00b8\5(\25\2\u00b8\u00b9\7\61\2\2\u00b9\u00ba\5\36\20\2\u00ba"+
		"\u00bc\3\2\2\2\u00bb\u00b3\3\2\2\2\u00bb\u00b7\3\2\2\2\u00bc-\3\2\2\2"+
		"\u00bd\u00be\7%\2\2\u00be/\3\2\2\2\u00bf\u00c4\5\62\32\2\u00c0\u00c1\7"+
		"-\2\2\u00c1\u00c3\5\62\32\2\u00c2\u00c0\3\2\2\2\u00c3\u00c6\3\2\2\2\u00c4"+
		"\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\61\3\2\2\2\u00c6\u00c4\3\2\2"+
		"\2\u00c7\u00cb\5*\26\2\u00c8\u00cb\5(\25\2\u00c9\u00cb\5\64\33\2\u00ca"+
		"\u00c7\3\2\2\2\u00ca\u00c8\3\2\2\2\u00ca\u00c9\3\2\2\2\u00cb\63\3\2\2"+
		"\2\u00cc\u00cf\5\66\34\2\u00cd\u00ce\7+\2\2\u00ce\u00d0\5.\30\2\u00cf"+
		"\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00d5\3\2\2\2\u00d1\u00d5\58"+
		"\35\2\u00d2\u00d5\5:\36\2\u00d3\u00d5\5<\37\2\u00d4\u00cc\3\2\2\2\u00d4"+
		"\u00d1\3\2\2\2\u00d4\u00d2\3\2\2\2\u00d4\u00d3\3\2\2\2\u00d5\65\3\2\2"+
		"\2\u00d6\u00d7\7\'\2\2\u00d7\67\3\2\2\2\u00d8\u00d9\5\66\34\2\u00d9\u00da"+
		"\7\61\2\2\u00da\u00db\5\36\20\2\u00db9\3\2\2\2\u00dc\u00e0\5> \2\u00dd"+
		"\u00e0\5@!\2\u00de\u00e0\5B\"\2\u00df\u00dc\3\2\2\2\u00df\u00dd\3\2\2"+
		"\2\u00df\u00de\3\2\2\2\u00e0;\3\2\2\2\u00e1\u00e2\t\3\2\2\u00e2=\3\2\2"+
		"\2\u00e3\u00e4\t\4\2\2\u00e4?\3\2\2\2\u00e5\u00e6\t\5\2\2\u00e6A\3\2\2"+
		"\2\u00e7\u00e8\t\6\2\2\u00e8C\3\2\2\2\27GMYejlx\u0080\u0087\u008d\u0093"+
		"\u0097\u00a1\u00a7\u00aa\u00bb\u00c4\u00ca\u00cf\u00d4\u00df";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}