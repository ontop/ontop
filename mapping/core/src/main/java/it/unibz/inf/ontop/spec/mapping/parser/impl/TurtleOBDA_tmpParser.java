// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/java/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA_tmp.g4 by ANTLR 4.7
package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.spec.mapping.parser.TurtleOBDA_tmpListener;
import it.unibz.inf.ontop.spec.mapping.parser.TurtleOBDA_tmpVisitor;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TurtleOBDA_tmpParser extends AbstractTurtleOBDAParser {
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
	public static final int
		RULE_parse = 0, RULE_directiveStatement = 1, RULE_triplesStatement = 2, 
		RULE_directive = 3, RULE_base = 4, RULE_prefixID = 5, RULE_triples = 6, 
		RULE_predicateObjectList = 7, RULE_predicateObject = 8, RULE_verb = 9, 
		RULE_objectList = 10, RULE_subject = 11, RULE_object = 12, RULE_resource = 13, 
		RULE_uriref = 14, RULE_qname = 15, RULE_blank = 16, RULE_variable = 17, 
		RULE_function = 18, RULE_typedLiteral = 19, RULE_language = 20, RULE_terms = 21, 
		RULE_term = 22, RULE_literal = 23, RULE_stringLiteral = 24, RULE_dataTypeString = 25, 
		RULE_numericLiteral = 26, RULE_nodeID = 27, RULE_relativeURI = 28, RULE_namespace = 29, 
		RULE_defaultNamespace = 30, RULE_name = 31, RULE_languageTag = 32, RULE_booleanLiteral = 33, 
		RULE_numericUnsigned = 34, RULE_numericPositive = 35, RULE_numericNegative = 36;
	public static final String[] ruleNames = {
		"parse", "directiveStatement", "triplesStatement", "directive", "base", 
		"prefixID", "triples", "predicateObjectList", "predicateObject", "verb", 
		"objectList", "subject", "object", "resource", "uriref", "qname", "blank", 
		"variable", "function", "typedLiteral", "language", "terms", "term", "literal", 
		"stringLiteral", "dataTypeString", "numericLiteral", "nodeID", "relativeURI", 
		"namespace", "defaultNamespace", "name", "languageTag", "booleanLiteral", 
		"numericUnsigned", "numericPositive", "numericNegative"
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

	@Override
	public String getGrammarFileName() { return "TurtleOBDA_tmp.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TurtleOBDA_tmpParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParseContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(TurtleOBDA_tmpParser.EOF, 0); }
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
			if ( listener instanceof TurtleOBDA_tmpListener) ((TurtleOBDA_tmpListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitParse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitParse(this);
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
			setState(77);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT || _la==COLON) {
				{
				{
				setState(74);
				directiveStatement();
				}
				}
				setState(79);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(81); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(80);
				triplesStatement();
				}
				}
				setState(83); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BLANK) | (1L << BLANK_PREFIX) | (1L << PREFIXED_NAME) | (1L << STRING_WITH_BRACKET) | (1L << STRING_WITH_CURLY_BRACKET))) != 0) );
			setState(85);
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
		public TerminalNode PERIOD() { return getToken(TurtleOBDA_tmpParser.PERIOD, 0); }
		public DirectiveStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directiveStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterDirectiveStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitDirectiveStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitDirectiveStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveStatementContext directiveStatement() throws RecognitionException {
		DirectiveStatementContext _localctx = new DirectiveStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_directiveStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			directive();
			setState(88);
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
		public TerminalNode PERIOD() { return getToken(TurtleOBDA_tmpParser.PERIOD, 0); }
		public List<TerminalNode> WS() { return getTokens(TurtleOBDA_tmpParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(TurtleOBDA_tmpParser.WS, i);
		}
		public TriplesStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_triplesStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterTriplesStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitTriplesStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitTriplesStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TriplesStatementContext triplesStatement() throws RecognitionException {
		TriplesStatementContext _localctx = new TriplesStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_triplesStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			triples();
			setState(94);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WS) {
				{
				{
				setState(91);
				match(WS);
				}
				}
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(97);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterDirective(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitDirective(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitDirective(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveContext directive() throws RecognitionException {
		DirectiveContext _localctx = new DirectiveContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_directive);
		try {
			setState(101);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(99);
				base();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(100);
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
		public TerminalNode AT() { return getToken(TurtleOBDA_tmpParser.AT, 0); }
		public TerminalNode BASE() { return getToken(TurtleOBDA_tmpParser.BASE, 0); }
		public UrirefContext uriref() {
			return getRuleContext(UrirefContext.class,0);
		}
		public BaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_base; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterBase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitBase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitBase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BaseContext base() throws RecognitionException {
		BaseContext _localctx = new BaseContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_base);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(AT);
			setState(104);
			match(BASE);
			setState(105);
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
		public PrefixIDContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixID; }
	 
		public PrefixIDContext() { }
		public void copyFrom(PrefixIDContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class PrefixID_2Context extends PrefixIDContext {
		public DefaultNamespaceContext defaultNamespace() {
			return getRuleContext(DefaultNamespaceContext.class,0);
		}
		public UrirefContext uriref() {
			return getRuleContext(UrirefContext.class,0);
		}
		public PrefixID_2Context(PrefixIDContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterPrefixID_2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitPrefixID_2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitPrefixID_2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrefixID_1Context extends PrefixIDContext {
		public TerminalNode AT() { return getToken(TurtleOBDA_tmpParser.AT, 0); }
		public TerminalNode PREFIX() { return getToken(TurtleOBDA_tmpParser.PREFIX, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public UrirefContext uriref() {
			return getRuleContext(UrirefContext.class,0);
		}
		public PrefixID_1Context(PrefixIDContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterPrefixID_1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitPrefixID_1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitPrefixID_1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixIDContext prefixID() throws RecognitionException {
		PrefixIDContext _localctx = new PrefixIDContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_prefixID);
		try {
			setState(115);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				_localctx = new PrefixID_1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				match(AT);
				setState(108);
				match(PREFIX);
				setState(109);
				namespace();
				setState(110);
				uriref();
				}
				break;
			case COLON:
				_localctx = new PrefixID_2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(112);
				defaultNamespace();
				setState(113);
				uriref();
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterTriples(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitTriples(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitTriples(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TriplesContext triples() throws RecognitionException {
		TriplesContext _localctx = new TriplesContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_triples);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			subject();
			setState(118);
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
		public List<TerminalNode> SEMI() { return getTokens(TurtleOBDA_tmpParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(TurtleOBDA_tmpParser.SEMI, i);
		}
		public PredicateObjectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateObjectList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterPredicateObjectList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitPredicateObjectList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitPredicateObjectList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateObjectListContext predicateObjectList() throws RecognitionException {
		PredicateObjectListContext _localctx = new PredicateObjectListContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_predicateObjectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			predicateObject();
			setState(125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMI) {
				{
				{
				setState(121);
				match(SEMI);
				setState(122);
				predicateObject();
				}
				}
				setState(127);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterPredicateObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitPredicateObject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitPredicateObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateObjectContext predicateObject() throws RecognitionException {
		PredicateObjectContext _localctx = new PredicateObjectContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_predicateObject);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			verb();
			setState(129);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterVerb(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitVerb(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitVerb(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VerbContext verb() throws RecognitionException {
		VerbContext _localctx = new VerbContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_verb);
		try {
			setState(133);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PREFIXED_NAME:
			case STRING_WITH_BRACKET:
				enterOuterAlt(_localctx, 1);
				{
				setState(131);
				resource();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(132);
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
		public List<TerminalNode> COMMA() { return getTokens(TurtleOBDA_tmpParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TurtleOBDA_tmpParser.COMMA, i);
		}
		public ObjectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterObjectList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitObjectList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitObjectList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectListContext objectList() throws RecognitionException {
		ObjectListContext _localctx = new ObjectListContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_objectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			object();
			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(136);
				match(COMMA);
				setState(137);
				object();
				}
				}
				setState(142);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterSubject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitSubject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitSubject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubjectContext subject() throws RecognitionException {
		SubjectContext _localctx = new SubjectContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_subject);
		try {
			setState(146);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PREFIXED_NAME:
			case STRING_WITH_BRACKET:
				enterOuterAlt(_localctx, 1);
				{
				setState(143);
				resource();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(144);
				variable();
				}
				break;
			case BLANK:
			case BLANK_PREFIX:
				enterOuterAlt(_localctx, 3);
				{
				setState(145);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitObject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_object);
		try {
			setState(152);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(148);
				resource();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(149);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(150);
				typedLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(151);
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
		public QnameContext qname() {
			return getRuleContext(QnameContext.class,0);
		}
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_resource);
		try {
			setState(156);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_WITH_BRACKET:
				enterOuterAlt(_localctx, 1);
				{
				setState(154);
				uriref();
				}
				break;
			case PREFIXED_NAME:
				enterOuterAlt(_localctx, 2);
				{
				setState(155);
				qname();
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
		public TerminalNode STRING_WITH_BRACKET() { return getToken(TurtleOBDA_tmpParser.STRING_WITH_BRACKET, 0); }
		public UrirefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uriref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterUriref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitUriref(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitUriref(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UrirefContext uriref() throws RecognitionException {
		UrirefContext _localctx = new UrirefContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_uriref);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			match(STRING_WITH_BRACKET);
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

	public static class QnameContext extends ParserRuleContext {
		public TerminalNode PREFIXED_NAME() { return getToken(TurtleOBDA_tmpParser.PREFIXED_NAME, 0); }
		public QnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterQname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitQname(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitQname(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QnameContext qname() throws RecognitionException {
		QnameContext _localctx = new QnameContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_qname);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
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
		public NodeIDContext nodeID() {
			return getRuleContext(NodeIDContext.class,0);
		}
		public TerminalNode BLANK() { return getToken(TurtleOBDA_tmpParser.BLANK, 0); }
		public BlankContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blank; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterBlank(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitBlank(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitBlank(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlankContext blank() throws RecognitionException {
		BlankContext _localctx = new BlankContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_blank);
		try {
			setState(164);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BLANK_PREFIX:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				nodeID();
				}
				break;
			case BLANK:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
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
		public TerminalNode STRING_WITH_CURLY_BRACKET() { return getToken(TurtleOBDA_tmpParser.STRING_WITH_CURLY_BRACKET, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
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
		public TerminalNode LPAREN() { return getToken(TurtleOBDA_tmpParser.LPAREN, 0); }
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(TurtleOBDA_tmpParser.RPAREN, 0); }
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_function);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			resource();
			setState(169);
			match(LPAREN);
			setState(170);
			terms();
			setState(171);
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
		public TerminalNode REFERENCE() { return getToken(TurtleOBDA_tmpParser.REFERENCE, 0); }
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public TypedLiteral_2Context(TypedLiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterTypedLiteral_2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitTypedLiteral_2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitTypedLiteral_2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TypedLiteral_1Context extends TypedLiteralContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode AT() { return getToken(TurtleOBDA_tmpParser.AT, 0); }
		public LanguageContext language() {
			return getRuleContext(LanguageContext.class,0);
		}
		public TypedLiteral_1Context(TypedLiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterTypedLiteral_1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitTypedLiteral_1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitTypedLiteral_1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypedLiteralContext typedLiteral() throws RecognitionException {
		TypedLiteralContext _localctx = new TypedLiteralContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_typedLiteral);
		try {
			setState(181);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				_localctx = new TypedLiteral_1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(173);
				variable();
				setState(174);
				match(AT);
				setState(175);
				language();
				}
				break;
			case 2:
				_localctx = new TypedLiteral_2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(177);
				variable();
				setState(178);
				match(REFERENCE);
				setState(179);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterLanguage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitLanguage(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitLanguage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LanguageContext language() throws RecognitionException {
		LanguageContext _localctx = new LanguageContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_language);
		try {
			setState(185);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VARNAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(183);
				languageTag();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(184);
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
		public List<TerminalNode> COMMA() { return getTokens(TurtleOBDA_tmpParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TurtleOBDA_tmpParser.COMMA, i);
		}
		public TermsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_terms; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterTerms(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitTerms(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitTerms(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermsContext terms() throws RecognitionException {
		TermsContext _localctx = new TermsContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_terms);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			term();
			setState(192);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(188);
				match(COMMA);
				setState(189);
				term();
				}
				}
				setState(194);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_term);
		try {
			setState(198);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PREFIXED_NAME:
			case STRING_WITH_BRACKET:
				enterOuterAlt(_localctx, 1);
				{
				setState(195);
				function();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(196);
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
				setState(197);
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
		public TerminalNode AT() { return getToken(TurtleOBDA_tmpParser.AT, 0); }
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_literal);
		int _la;
		try {
			setState(208);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(200);
				stringLiteral();
				setState(203);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AT) {
					{
					setState(201);
					match(AT);
					setState(202);
					language();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(205);
				dataTypeString();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(206);
				numericLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(207);
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
		public TerminalNode STRING_WITH_QUOTE_DOUBLE() { return getToken(TurtleOBDA_tmpParser.STRING_WITH_QUOTE_DOUBLE, 0); }
		public StringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralContext stringLiteral() throws RecognitionException {
		StringLiteralContext _localctx = new StringLiteralContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
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
		public TerminalNode REFERENCE() { return getToken(TurtleOBDA_tmpParser.REFERENCE, 0); }
		public ResourceContext resource() {
			return getRuleContext(ResourceContext.class,0);
		}
		public DataTypeStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterDataTypeString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitDataTypeString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitDataTypeString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeStringContext dataTypeString() throws RecognitionException {
		DataTypeStringContext _localctx = new DataTypeStringContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_dataTypeString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			stringLiteral();
			setState(213);
			match(REFERENCE);
			setState(214);
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
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitNumericLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_numericLiteral);
		try {
			setState(219);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
			case DOUBLE:
			case DECIMAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(216);
				numericUnsigned();
				}
				break;
			case INTEGER_POSITIVE:
			case DOUBLE_POSITIVE:
			case DECIMAL_POSITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(217);
				numericPositive();
				}
				break;
			case INTEGER_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case DECIMAL_NEGATIVE:
				enterOuterAlt(_localctx, 3);
				{
				setState(218);
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

	public static class NodeIDContext extends ParserRuleContext {
		public TerminalNode BLANK_PREFIX() { return getToken(TurtleOBDA_tmpParser.BLANK_PREFIX, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public NodeIDContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeID; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterNodeID(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitNodeID(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitNodeID(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NodeIDContext nodeID() throws RecognitionException {
		NodeIDContext _localctx = new NodeIDContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_nodeID);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			match(BLANK_PREFIX);
			setState(222);
			name();
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

	public static class RelativeURIContext extends ParserRuleContext {
		public TerminalNode STRING_URI() { return getToken(TurtleOBDA_tmpParser.STRING_URI, 0); }
		public RelativeURIContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relativeURI; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterRelativeURI(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitRelativeURI(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitRelativeURI(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelativeURIContext relativeURI() throws RecognitionException {
		RelativeURIContext _localctx = new RelativeURIContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_relativeURI);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224);
			match(STRING_URI);
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

	public static class NamespaceContext extends ParserRuleContext {
		public TerminalNode NAMESPACE() { return getToken(TurtleOBDA_tmpParser.NAMESPACE, 0); }
		public NamespaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespace; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitNamespace(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespaceContext namespace() throws RecognitionException {
		NamespaceContext _localctx = new NamespaceContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_namespace);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			match(NAMESPACE);
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

	public static class DefaultNamespaceContext extends ParserRuleContext {
		public TerminalNode COLON() { return getToken(TurtleOBDA_tmpParser.COLON, 0); }
		public DefaultNamespaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultNamespace; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterDefaultNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitDefaultNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitDefaultNamespace(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultNamespaceContext defaultNamespace() throws RecognitionException {
		DefaultNamespaceContext _localctx = new DefaultNamespaceContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_defaultNamespace);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
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

	public static class NameContext extends ParserRuleContext {
		public TerminalNode VARNAME() { return getToken(TurtleOBDA_tmpParser.VARNAME, 0); }
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
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

	public static class LanguageTagContext extends ParserRuleContext {
		public TerminalNode VARNAME() { return getToken(TurtleOBDA_tmpParser.VARNAME, 0); }
		public LanguageTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_languageTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterLanguageTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitLanguageTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitLanguageTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LanguageTagContext languageTag() throws RecognitionException {
		LanguageTagContext _localctx = new LanguageTagContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_languageTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
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
		public TerminalNode TRUE() { return getToken(TurtleOBDA_tmpParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(TurtleOBDA_tmpParser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
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
		public TerminalNode INTEGER() { return getToken(TurtleOBDA_tmpParser.INTEGER, 0); }
		public TerminalNode DOUBLE() { return getToken(TurtleOBDA_tmpParser.DOUBLE, 0); }
		public TerminalNode DECIMAL() { return getToken(TurtleOBDA_tmpParser.DECIMAL, 0); }
		public NumericUnsignedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericUnsigned; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterNumericUnsigned(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitNumericUnsigned(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitNumericUnsigned(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericUnsignedContext numericUnsigned() throws RecognitionException {
		NumericUnsignedContext _localctx = new NumericUnsignedContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_numericUnsigned);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236);
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
		public TerminalNode INTEGER_POSITIVE() { return getToken(TurtleOBDA_tmpParser.INTEGER_POSITIVE, 0); }
		public TerminalNode DOUBLE_POSITIVE() { return getToken(TurtleOBDA_tmpParser.DOUBLE_POSITIVE, 0); }
		public TerminalNode DECIMAL_POSITIVE() { return getToken(TurtleOBDA_tmpParser.DECIMAL_POSITIVE, 0); }
		public NumericPositiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericPositive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterNumericPositive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitNumericPositive(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitNumericPositive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericPositiveContext numericPositive() throws RecognitionException {
		NumericPositiveContext _localctx = new NumericPositiveContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_numericPositive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
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
		public TerminalNode INTEGER_NEGATIVE() { return getToken(TurtleOBDA_tmpParser.INTEGER_NEGATIVE, 0); }
		public TerminalNode DOUBLE_NEGATIVE() { return getToken(TurtleOBDA_tmpParser.DOUBLE_NEGATIVE, 0); }
		public TerminalNode DECIMAL_NEGATIVE() { return getToken(TurtleOBDA_tmpParser.DECIMAL_NEGATIVE, 0); }
		public NumericNegativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericNegative; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).enterNumericNegative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDA_tmpListener ) ((TurtleOBDA_tmpListener)listener).exitNumericNegative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDA_tmpVisitor ) return ((TurtleOBDA_tmpVisitor<? extends T>)visitor).visitNumericNegative(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericNegativeContext numericNegative() throws RecognitionException {
		NumericNegativeContext _localctx = new NumericNegativeContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_numericNegative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(240);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3@\u00f5\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\3\2\7\2N\n\2\f\2\16\2Q\13\2\3\2\6"+
		"\2T\n\2\r\2\16\2U\3\2\3\2\3\3\3\3\3\3\3\4\3\4\7\4_\n\4\f\4\16\4b\13\4"+
		"\3\4\3\4\3\5\3\5\5\5h\n\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\5\7v\n\7\3\b\3\b\3\b\3\t\3\t\3\t\7\t~\n\t\f\t\16\t\u0081\13\t\3\n"+
		"\3\n\3\n\3\13\3\13\5\13\u0088\n\13\3\f\3\f\3\f\7\f\u008d\n\f\f\f\16\f"+
		"\u0090\13\f\3\r\3\r\3\r\5\r\u0095\n\r\3\16\3\16\3\16\3\16\5\16\u009b\n"+
		"\16\3\17\3\17\5\17\u009f\n\17\3\20\3\20\3\21\3\21\3\22\3\22\5\22\u00a7"+
		"\n\22\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\5\25\u00b8\n\25\3\26\3\26\5\26\u00bc\n\26\3\27\3\27\3\27\7"+
		"\27\u00c1\n\27\f\27\16\27\u00c4\13\27\3\30\3\30\3\30\5\30\u00c9\n\30\3"+
		"\31\3\31\3\31\5\31\u00ce\n\31\3\31\3\31\3\31\5\31\u00d3\n\31\3\32\3\32"+
		"\3\33\3\33\3\33\3\33\3\34\3\34\3\34\5\34\u00de\n\34\3\35\3\35\3\35\3\36"+
		"\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3&\2\2\'\2"+
		"\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJ\2"+
		"\6\3\2\6\7\3\2-/\5\2\60\60\62\62\64\64\5\2\61\61\63\63\65\65\2\u00e9\2"+
		"O\3\2\2\2\4Y\3\2\2\2\6\\\3\2\2\2\bg\3\2\2\2\ni\3\2\2\2\fu\3\2\2\2\16w"+
		"\3\2\2\2\20z\3\2\2\2\22\u0082\3\2\2\2\24\u0087\3\2\2\2\26\u0089\3\2\2"+
		"\2\30\u0094\3\2\2\2\32\u009a\3\2\2\2\34\u009e\3\2\2\2\36\u00a0\3\2\2\2"+
		" \u00a2\3\2\2\2\"\u00a6\3\2\2\2$\u00a8\3\2\2\2&\u00aa\3\2\2\2(\u00b7\3"+
		"\2\2\2*\u00bb\3\2\2\2,\u00bd\3\2\2\2.\u00c8\3\2\2\2\60\u00d2\3\2\2\2\62"+
		"\u00d4\3\2\2\2\64\u00d6\3\2\2\2\66\u00dd\3\2\2\28\u00df\3\2\2\2:\u00e2"+
		"\3\2\2\2<\u00e4\3\2\2\2>\u00e6\3\2\2\2@\u00e8\3\2\2\2B\u00ea\3\2\2\2D"+
		"\u00ec\3\2\2\2F\u00ee\3\2\2\2H\u00f0\3\2\2\2J\u00f2\3\2\2\2LN\5\4\3\2"+
		"ML\3\2\2\2NQ\3\2\2\2OM\3\2\2\2OP\3\2\2\2PS\3\2\2\2QO\3\2\2\2RT\5\6\4\2"+
		"SR\3\2\2\2TU\3\2\2\2US\3\2\2\2UV\3\2\2\2VW\3\2\2\2WX\7\2\2\3X\3\3\2\2"+
		"\2YZ\5\b\5\2Z[\7\f\2\2[\5\3\2\2\2\\`\5\16\b\2]_\7@\2\2^]\3\2\2\2_b\3\2"+
		"\2\2`^\3\2\2\2`a\3\2\2\2ac\3\2\2\2b`\3\2\2\2cd\7\f\2\2d\7\3\2\2\2eh\5"+
		"\n\6\2fh\5\f\7\2ge\3\2\2\2gf\3\2\2\2h\t\3\2\2\2ij\7\35\2\2jk\7\4\2\2k"+
		"l\5\36\20\2l\13\3\2\2\2mn\7\35\2\2no\7\5\2\2op\5<\37\2pq\5\36\20\2qv\3"+
		"\2\2\2rs\5> \2st\5\36\20\2tv\3\2\2\2um\3\2\2\2ur\3\2\2\2v\r\3\2\2\2wx"+
		"\5\30\r\2xy\5\20\t\2y\17\3\2\2\2z\177\5\22\n\2{|\7\13\2\2|~\5\22\n\2}"+
		"{\3\2\2\2~\u0081\3\2\2\2\177}\3\2\2\2\177\u0080\3\2\2\2\u0080\21\3\2\2"+
		"\2\u0081\177\3\2\2\2\u0082\u0083\5\24\13\2\u0083\u0084\5\26\f\2\u0084"+
		"\23\3\2\2\2\u0085\u0088\5\34\17\2\u0086\u0088\7\3\2\2\u0087\u0085\3\2"+
		"\2\2\u0087\u0086\3\2\2\2\u0088\25\3\2\2\2\u0089\u008e\5\32\16\2\u008a"+
		"\u008b\7\r\2\2\u008b\u008d\5\32\16\2\u008c\u008a\3\2\2\2\u008d\u0090\3"+
		"\2\2\2\u008e\u008c\3\2\2\2\u008e\u008f\3\2\2\2\u008f\27\3\2\2\2\u0090"+
		"\u008e\3\2\2\2\u0091\u0095\5\34\17\2\u0092\u0095\5$\23\2\u0093\u0095\5"+
		"\"\22\2\u0094\u0091\3\2\2\2\u0094\u0092\3\2\2\2\u0094\u0093\3\2\2\2\u0095"+
		"\31\3\2\2\2\u0096\u009b\5\34\17\2\u0097\u009b\5\60\31\2\u0098\u009b\5"+
		"(\25\2\u0099\u009b\5$\23\2\u009a\u0096\3\2\2\2\u009a\u0097\3\2\2\2\u009a"+
		"\u0098\3\2\2\2\u009a\u0099\3\2\2\2\u009b\33\3\2\2\2\u009c\u009f\5\36\20"+
		"\2\u009d\u009f\5 \21\2\u009e\u009c\3\2\2\2\u009e\u009d\3\2\2\2\u009f\35"+
		"\3\2\2\2\u00a0\u00a1\7=\2\2\u00a1\37\3\2\2\2\u00a2\u00a3\7:\2\2\u00a3"+
		"!\3\2\2\2\u00a4\u00a7\58\35\2\u00a5\u00a7\7)\2\2\u00a6\u00a4\3\2\2\2\u00a6"+
		"\u00a5\3\2\2\2\u00a7#\3\2\2\2\u00a8\u00a9\7>\2\2\u00a9%\3\2\2\2\u00aa"+
		"\u00ab\5\34\17\2\u00ab\u00ac\7\22\2\2\u00ac\u00ad\5,\27\2\u00ad\u00ae"+
		"\7\23\2\2\u00ae\'\3\2\2\2\u00af\u00b0\5$\23\2\u00b0\u00b1\7\35\2\2\u00b1"+
		"\u00b2\5*\26\2\u00b2\u00b8\3\2\2\2\u00b3\u00b4\5$\23\2\u00b4\u00b5\7\b"+
		"\2\2\u00b5\u00b6\5\34\17\2\u00b6\u00b8\3\2\2\2\u00b7\u00af\3\2\2\2\u00b7"+
		"\u00b3\3\2\2\2\u00b8)\3\2\2\2\u00b9\u00bc\5B\"\2\u00ba\u00bc\5$\23\2\u00bb"+
		"\u00b9\3\2\2\2\u00bb\u00ba\3\2\2\2\u00bc+\3\2\2\2\u00bd\u00c2\5.\30\2"+
		"\u00be\u00bf\7\r\2\2\u00bf\u00c1\5.\30\2\u00c0\u00be\3\2\2\2\u00c1\u00c4"+
		"\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3-\3\2\2\2\u00c4"+
		"\u00c2\3\2\2\2\u00c5\u00c9\5&\24\2\u00c6\u00c9\5$\23\2\u00c7\u00c9\5\60"+
		"\31\2\u00c8\u00c5\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c8\u00c7\3\2\2\2\u00c9"+
		"/\3\2\2\2\u00ca\u00cd\5\62\32\2\u00cb\u00cc\7\35\2\2\u00cc\u00ce\5*\26"+
		"\2\u00cd\u00cb\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce\u00d3\3\2\2\2\u00cf\u00d3"+
		"\5\64\33\2\u00d0\u00d3\5\66\34\2\u00d1\u00d3\5D#\2\u00d2\u00ca\3\2\2\2"+
		"\u00d2\u00cf\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d1\3\2\2\2\u00d3\61"+
		"\3\2\2\2\u00d4\u00d5\7<\2\2\u00d5\63\3\2\2\2\u00d6\u00d7\5\62\32\2\u00d7"+
		"\u00d8\7\b\2\2\u00d8\u00d9\5\34\17\2\u00d9\65\3\2\2\2\u00da\u00de\5F$"+
		"\2\u00db\u00de\5H%\2\u00dc\u00de\5J&\2\u00dd\u00da\3\2\2\2\u00dd\u00db"+
		"\3\2\2\2\u00dd\u00dc\3\2\2\2\u00de\67\3\2\2\2\u00df\u00e0\7*\2\2\u00e0"+
		"\u00e1\5@!\2\u00e19\3\2\2\2\u00e2\u00e3\7?\2\2\u00e3;\3\2\2\2\u00e4\u00e5"+
		"\79\2\2\u00e5=\3\2\2\2\u00e6\u00e7\7#\2\2\u00e7?\3\2\2\2\u00e8\u00e9\7"+
		"\66\2\2\u00e9A\3\2\2\2\u00ea\u00eb\7\66\2\2\u00ebC\3\2\2\2\u00ec\u00ed"+
		"\t\2\2\2\u00edE\3\2\2\2\u00ee\u00ef\t\3\2\2\u00efG\3\2\2\2\u00f0\u00f1"+
		"\t\4\2\2\u00f1I\3\2\2\2\u00f2\u00f3\t\5\2\2\u00f3K\3\2\2\2\25OU`gu\177"+
		"\u0087\u008e\u0094\u009a\u009e\u00a6\u00b7\u00bb\u00c2\u00c8\u00cd\u00d2"+
		"\u00dd";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}