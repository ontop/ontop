// Generated from /home/julien/workspace/ontop/ontop/mapping/core/src/main/antlr4/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA.g4 by ANTLR 4.7
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TurtleOBDAParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		WS=10, STRING_WITH_CURLY_BRACKET=11, BOOLEAN_LITERAL=12, IRIREF_EXT=13, 
		IRIREF=14, PNAME_NS=15, PN_PREFIX=16, PREFIXED_NAME=17, PREFIXED_NAME_EXT=18, 
		BLANK_NODE_FUNCTION=19, BLANK_NODE_LABEL=20, LANGTAG=21, INTEGER=22, DECIMAL=23, 
		DOUBLE=24, EXPONENT=25, INTEGER_POSITIVE=26, INTEGER_NEGATIVE=27, DOUBLE_POSITIVE=28, 
		DOUBLE_NEGATIVE=29, DECIMAL_POSITIVE=30, DECIMAL_NEGATIVE=31, STRING_LITERAL_LONG_SINGLE_QUOTE=32, 
		STRING_LITERAL_LONG_QUOTE=33, STRING_LITERAL_QUOTE=34, STRING_LITERAL_SINGLE_QUOTE=35, 
		UCHAR=36, ECHAR=37, ANON_WS=38, ANON=39, PN_CHARS_BASE=40, PN_CHARS_U=41, 
		PN_CHARS=42, PN_LOCAL_EXT=43, PN_LOCAL=44, PLX=45, PERCENT=46, HEX=47, 
		PN_LOCAL_ESC=48;
	public static final int
		RULE_parse = 0, RULE_directiveStatement = 1, RULE_triplesStatement = 2, 
		RULE_directive = 3, RULE_prefixID = 4, RULE_base = 5, RULE_triples = 6, 
		RULE_predicateObjectList = 7, RULE_predicateObject = 8, RULE_objectList = 9, 
		RULE_verb = 10, RULE_subject = 11, RULE_object = 12, RULE_resource = 13, 
		RULE_iriExt = 14, RULE_blank = 15, RULE_variable = 16, RULE_variableLiteral = 17, 
		RULE_languageTag = 18, RULE_iri = 19, RULE_literal = 20, RULE_untypedStringLiteral = 21, 
		RULE_typedLiteral = 22, RULE_litString = 23, RULE_untypedNumericLiteral = 24, 
		RULE_untypedBooleanLiteral = 25, RULE_numericUnsigned = 26, RULE_numericPositive = 27, 
		RULE_numericNegative = 28;
	public static final String[] ruleNames = {
		"parse", "directiveStatement", "triplesStatement", "directive", "prefixID", 
		"base", "triples", "predicateObjectList", "predicateObject", "objectList", 
		"verb", "subject", "object", "resource", "iriExt", "blank", "variable", 
		"variableLiteral", "languageTag", "iri", "literal", "untypedStringLiteral", 
		"typedLiteral", "litString", "untypedNumericLiteral", "untypedBooleanLiteral", 
		"numericUnsigned", "numericPositive", "numericNegative"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'@prefix'", "'@PREFIX'", "'@base'", "'@BASE'", "';'", "','", 
		"'a'", "'^^'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, "WS", "STRING_WITH_CURLY_BRACKET", 
		"BOOLEAN_LITERAL", "IRIREF_EXT", "IRIREF", "PNAME_NS", "PN_PREFIX", "PREFIXED_NAME", 
		"PREFIXED_NAME_EXT", "BLANK_NODE_FUNCTION", "BLANK_NODE_LABEL", "LANGTAG", 
		"INTEGER", "DECIMAL", "DOUBLE", "EXPONENT", "INTEGER_POSITIVE", "INTEGER_NEGATIVE", 
		"DOUBLE_POSITIVE", "DOUBLE_NEGATIVE", "DECIMAL_POSITIVE", "DECIMAL_NEGATIVE", 
		"STRING_LITERAL_LONG_SINGLE_QUOTE", "STRING_LITERAL_LONG_QUOTE", "STRING_LITERAL_QUOTE", 
		"STRING_LITERAL_SINGLE_QUOTE", "UCHAR", "ECHAR", "ANON_WS", "ANON", "PN_CHARS_BASE", 
		"PN_CHARS_U", "PN_CHARS", "PN_LOCAL_EXT", "PN_LOCAL", "PLX", "PERCENT", 
		"HEX", "PN_LOCAL_ESC"
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
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4))) != 0)) {
				{
				{
				setState(58);
				directiveStatement();
				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(65); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(64);
				triplesStatement();
				}
				}
				setState(67); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING_WITH_CURLY_BRACKET) | (1L << IRIREF_EXT) | (1L << IRIREF) | (1L << PREFIXED_NAME) | (1L << PREFIXED_NAME_EXT) | (1L << BLANK_NODE_FUNCTION) | (1L << BLANK_NODE_LABEL) | (1L << ANON))) != 0) );
			setState(69);
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
			setState(71);
			directive();
			setState(72);
			match(T__0);
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
			setState(74);
			triples();
			setState(75);
			match(T__0);
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
			setState(79);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__3:
			case T__4:
				enterOuterAlt(_localctx, 1);
				{
				setState(77);
				base();
				}
				break;
			case T__1:
			case T__2:
				enterOuterAlt(_localctx, 2);
				{
				setState(78);
				prefixID();
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

	public static class PrefixIDContext extends ParserRuleContext {
		public TerminalNode PNAME_NS() { return getToken(TurtleOBDAParser.PNAME_NS, 0); }
		public TerminalNode IRIREF() { return getToken(TurtleOBDAParser.IRIREF, 0); }
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
		enterRule(_localctx, 8, RULE_prefixID);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			_la = _input.LA(1);
			if ( !(_la==T__1 || _la==T__2) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(82);
			match(PNAME_NS);
			setState(83);
			match(IRIREF);
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
		public TerminalNode IRIREF() { return getToken(TurtleOBDAParser.IRIREF, 0); }
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
		enterRule(_localctx, 10, RULE_base);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			_la = _input.LA(1);
			if ( !(_la==T__3 || _la==T__4) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(86);
			match(IRIREF);
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
		enterRule(_localctx, 12, RULE_triples);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			subject();
			setState(89);
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
		enterRule(_localctx, 14, RULE_predicateObjectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(91);
			predicateObject();
			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(92);
				match(T__5);
				setState(93);
				predicateObject();
				}
				}
				setState(98);
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
		enterRule(_localctx, 16, RULE_predicateObject);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			verb();
			setState(100);
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

	public static class ObjectListContext extends ParserRuleContext {
		public List<ObjectContext> object() {
			return getRuleContexts(ObjectContext.class);
		}
		public ObjectContext object(int i) {
			return getRuleContext(ObjectContext.class,i);
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
		enterRule(_localctx, 18, RULE_objectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			object();
			setState(107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__6) {
				{
				{
				setState(103);
				match(T__6);
				setState(104);
				object();
				}
				}
				setState(109);
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
			setState(112);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IRIREF_EXT:
			case IRIREF:
			case PREFIXED_NAME:
			case PREFIXED_NAME_EXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(110);
				resource();
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 2);
				{
				setState(111);
				match(T__7);
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
		enterRule(_localctx, 22, RULE_subject);
		try {
			setState(117);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IRIREF_EXT:
			case IRIREF:
			case PREFIXED_NAME:
			case PREFIXED_NAME_EXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(114);
				resource();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(115);
				variable();
				}
				break;
			case BLANK_NODE_FUNCTION:
			case BLANK_NODE_LABEL:
			case ANON:
				enterOuterAlt(_localctx, 3);
				{
				setState(116);
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
		public VariableLiteralContext variableLiteral() {
			return getRuleContext(VariableLiteralContext.class,0);
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
		enterRule(_localctx, 24, RULE_object);
		try {
			setState(123);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(119);
				resource();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(120);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(121);
				variableLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(122);
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
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public IriExtContext iriExt() {
			return getRuleContext(IriExtContext.class,0);
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
		enterRule(_localctx, 26, RULE_resource);
		try {
			setState(127);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IRIREF:
			case PREFIXED_NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(125);
				iri();
				}
				break;
			case IRIREF_EXT:
			case PREFIXED_NAME_EXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(126);
				iriExt();
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

	public static class IriExtContext extends ParserRuleContext {
		public TerminalNode IRIREF_EXT() { return getToken(TurtleOBDAParser.IRIREF_EXT, 0); }
		public TerminalNode PREFIXED_NAME_EXT() { return getToken(TurtleOBDAParser.PREFIXED_NAME_EXT, 0); }
		public IriExtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iriExt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterIriExt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitIriExt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitIriExt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IriExtContext iriExt() throws RecognitionException {
		IriExtContext _localctx = new IriExtContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_iriExt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			_la = _input.LA(1);
			if ( !(_la==IRIREF_EXT || _la==PREFIXED_NAME_EXT) ) {
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

	public static class BlankContext extends ParserRuleContext {
		public TerminalNode BLANK_NODE_FUNCTION() { return getToken(TurtleOBDAParser.BLANK_NODE_FUNCTION, 0); }
		public TerminalNode BLANK_NODE_LABEL() { return getToken(TurtleOBDAParser.BLANK_NODE_LABEL, 0); }
		public TerminalNode ANON() { return getToken(TurtleOBDAParser.ANON, 0); }
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
		enterRule(_localctx, 30, RULE_blank);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BLANK_NODE_FUNCTION) | (1L << BLANK_NODE_LABEL) | (1L << ANON))) != 0)) ) {
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
		enterRule(_localctx, 32, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
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

	public static class VariableLiteralContext extends ParserRuleContext {
		public VariableLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableLiteral; }
	 
		public VariableLiteralContext() { }
		public void copyFrom(VariableLiteralContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class VariableLiteral_2Context extends VariableLiteralContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public VariableLiteral_2Context(VariableLiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterVariableLiteral_2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitVariableLiteral_2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitVariableLiteral_2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class VariableLiteral_1Context extends VariableLiteralContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public LanguageTagContext languageTag() {
			return getRuleContext(LanguageTagContext.class,0);
		}
		public VariableLiteral_1Context(VariableLiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterVariableLiteral_1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitVariableLiteral_1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitVariableLiteral_1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableLiteralContext variableLiteral() throws RecognitionException {
		VariableLiteralContext _localctx = new VariableLiteralContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_variableLiteral);
		try {
			setState(142);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				_localctx = new VariableLiteral_1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(135);
				variable();
				setState(136);
				languageTag();
				}
				break;
			case 2:
				_localctx = new VariableLiteral_2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(138);
				variable();
				setState(139);
				match(T__8);
				setState(140);
				iri();
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

	public static class LanguageTagContext extends ParserRuleContext {
		public TerminalNode LANGTAG() { return getToken(TurtleOBDAParser.LANGTAG, 0); }
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
		enterRule(_localctx, 36, RULE_languageTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			match(LANGTAG);
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

	public static class IriContext extends ParserRuleContext {
		public TerminalNode IRIREF() { return getToken(TurtleOBDAParser.IRIREF, 0); }
		public TerminalNode PREFIXED_NAME() { return getToken(TurtleOBDAParser.PREFIXED_NAME, 0); }
		public IriContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iri; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterIri(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitIri(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitIri(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IriContext iri() throws RecognitionException {
		IriContext _localctx = new IriContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_iri);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			_la = _input.LA(1);
			if ( !(_la==IRIREF || _la==PREFIXED_NAME) ) {
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

	public static class LiteralContext extends ParserRuleContext {
		public TypedLiteralContext typedLiteral() {
			return getRuleContext(TypedLiteralContext.class,0);
		}
		public UntypedStringLiteralContext untypedStringLiteral() {
			return getRuleContext(UntypedStringLiteralContext.class,0);
		}
		public UntypedNumericLiteralContext untypedNumericLiteral() {
			return getRuleContext(UntypedNumericLiteralContext.class,0);
		}
		public UntypedBooleanLiteralContext untypedBooleanLiteral() {
			return getRuleContext(UntypedBooleanLiteralContext.class,0);
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
		enterRule(_localctx, 40, RULE_literal);
		try {
			setState(152);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(148);
				typedLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(149);
				untypedStringLiteral();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(150);
				untypedNumericLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(151);
				untypedBooleanLiteral();
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

	public static class UntypedStringLiteralContext extends ParserRuleContext {
		public LitStringContext litString() {
			return getRuleContext(LitStringContext.class,0);
		}
		public LanguageTagContext languageTag() {
			return getRuleContext(LanguageTagContext.class,0);
		}
		public UntypedStringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_untypedStringLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterUntypedStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitUntypedStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitUntypedStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UntypedStringLiteralContext untypedStringLiteral() throws RecognitionException {
		UntypedStringLiteralContext _localctx = new UntypedStringLiteralContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_untypedStringLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			litString();
			setState(156);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LANGTAG) {
				{
				setState(155);
				languageTag();
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

	public static class TypedLiteralContext extends ParserRuleContext {
		public LitStringContext litString() {
			return getRuleContext(LitStringContext.class,0);
		}
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public TypedLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typedLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterTypedLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitTypedLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTypedLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypedLiteralContext typedLiteral() throws RecognitionException {
		TypedLiteralContext _localctx = new TypedLiteralContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_typedLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			litString();
			setState(159);
			match(T__8);
			setState(160);
			iri();
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

	public static class LitStringContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL_QUOTE() { return getToken(TurtleOBDAParser.STRING_LITERAL_QUOTE, 0); }
		public LitStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_litString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterLitString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitLitString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitLitString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LitStringContext litString() throws RecognitionException {
		LitStringContext _localctx = new LitStringContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_litString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			match(STRING_LITERAL_QUOTE);
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

	public static class UntypedNumericLiteralContext extends ParserRuleContext {
		public NumericUnsignedContext numericUnsigned() {
			return getRuleContext(NumericUnsignedContext.class,0);
		}
		public NumericPositiveContext numericPositive() {
			return getRuleContext(NumericPositiveContext.class,0);
		}
		public NumericNegativeContext numericNegative() {
			return getRuleContext(NumericNegativeContext.class,0);
		}
		public UntypedNumericLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_untypedNumericLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterUntypedNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitUntypedNumericLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitUntypedNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UntypedNumericLiteralContext untypedNumericLiteral() throws RecognitionException {
		UntypedNumericLiteralContext _localctx = new UntypedNumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_untypedNumericLiteral);
		try {
			setState(167);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(164);
				numericUnsigned();
				}
				break;
			case INTEGER_POSITIVE:
			case DOUBLE_POSITIVE:
			case DECIMAL_POSITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(165);
				numericPositive();
				}
				break;
			case INTEGER_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case DECIMAL_NEGATIVE:
				enterOuterAlt(_localctx, 3);
				{
				setState(166);
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

	public static class UntypedBooleanLiteralContext extends ParserRuleContext {
		public TerminalNode BOOLEAN_LITERAL() { return getToken(TurtleOBDAParser.BOOLEAN_LITERAL, 0); }
		public UntypedBooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_untypedBooleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).enterUntypedBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TurtleOBDAListener ) ((TurtleOBDAListener)listener).exitUntypedBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitUntypedBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UntypedBooleanLiteralContext untypedBooleanLiteral() throws RecognitionException {
		UntypedBooleanLiteralContext _localctx = new UntypedBooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_untypedBooleanLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			match(BOOLEAN_LITERAL);
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
		enterRule(_localctx, 52, RULE_numericUnsigned);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INTEGER) | (1L << DECIMAL) | (1L << DOUBLE))) != 0)) ) {
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
		enterRule(_localctx, 54, RULE_numericPositive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
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
		enterRule(_localctx, 56, RULE_numericNegative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\62\u00b4\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\3\2\7\2>\n\2\f\2\16"+
		"\2A\13\2\3\2\6\2D\n\2\r\2\16\2E\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3"+
		"\5\5\5R\n\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\7\ta\n"+
		"\t\f\t\16\td\13\t\3\n\3\n\3\n\3\13\3\13\3\13\7\13l\n\13\f\13\16\13o\13"+
		"\13\3\f\3\f\5\fs\n\f\3\r\3\r\3\r\5\rx\n\r\3\16\3\16\3\16\3\16\5\16~\n"+
		"\16\3\17\3\17\5\17\u0082\n\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\5\23\u0091\n\23\3\24\3\24\3\25\3\25\3\26\3\26"+
		"\3\26\3\26\5\26\u009b\n\26\3\27\3\27\5\27\u009f\n\27\3\30\3\30\3\30\3"+
		"\30\3\31\3\31\3\32\3\32\3\32\5\32\u00aa\n\32\3\33\3\33\3\34\3\34\3\35"+
		"\3\35\3\36\3\36\3\36\2\2\37\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \""+
		"$&(*,.\60\62\64\668:\2\n\3\2\4\5\3\2\6\7\4\2\17\17\24\24\4\2\25\26))\4"+
		"\2\20\20\23\23\3\2\30\32\5\2\34\34\36\36  \5\2\35\35\37\37!!\2\u00a9\2"+
		"?\3\2\2\2\4I\3\2\2\2\6L\3\2\2\2\bQ\3\2\2\2\nS\3\2\2\2\fW\3\2\2\2\16Z\3"+
		"\2\2\2\20]\3\2\2\2\22e\3\2\2\2\24h\3\2\2\2\26r\3\2\2\2\30w\3\2\2\2\32"+
		"}\3\2\2\2\34\u0081\3\2\2\2\36\u0083\3\2\2\2 \u0085\3\2\2\2\"\u0087\3\2"+
		"\2\2$\u0090\3\2\2\2&\u0092\3\2\2\2(\u0094\3\2\2\2*\u009a\3\2\2\2,\u009c"+
		"\3\2\2\2.\u00a0\3\2\2\2\60\u00a4\3\2\2\2\62\u00a9\3\2\2\2\64\u00ab\3\2"+
		"\2\2\66\u00ad\3\2\2\28\u00af\3\2\2\2:\u00b1\3\2\2\2<>\5\4\3\2=<\3\2\2"+
		"\2>A\3\2\2\2?=\3\2\2\2?@\3\2\2\2@C\3\2\2\2A?\3\2\2\2BD\5\6\4\2CB\3\2\2"+
		"\2DE\3\2\2\2EC\3\2\2\2EF\3\2\2\2FG\3\2\2\2GH\7\2\2\3H\3\3\2\2\2IJ\5\b"+
		"\5\2JK\7\3\2\2K\5\3\2\2\2LM\5\16\b\2MN\7\3\2\2N\7\3\2\2\2OR\5\f\7\2PR"+
		"\5\n\6\2QO\3\2\2\2QP\3\2\2\2R\t\3\2\2\2ST\t\2\2\2TU\7\21\2\2UV\7\20\2"+
		"\2V\13\3\2\2\2WX\t\3\2\2XY\7\20\2\2Y\r\3\2\2\2Z[\5\30\r\2[\\\5\20\t\2"+
		"\\\17\3\2\2\2]b\5\22\n\2^_\7\b\2\2_a\5\22\n\2`^\3\2\2\2ad\3\2\2\2b`\3"+
		"\2\2\2bc\3\2\2\2c\21\3\2\2\2db\3\2\2\2ef\5\26\f\2fg\5\24\13\2g\23\3\2"+
		"\2\2hm\5\32\16\2ij\7\t\2\2jl\5\32\16\2ki\3\2\2\2lo\3\2\2\2mk\3\2\2\2m"+
		"n\3\2\2\2n\25\3\2\2\2om\3\2\2\2ps\5\34\17\2qs\7\n\2\2rp\3\2\2\2rq\3\2"+
		"\2\2s\27\3\2\2\2tx\5\34\17\2ux\5\"\22\2vx\5 \21\2wt\3\2\2\2wu\3\2\2\2"+
		"wv\3\2\2\2x\31\3\2\2\2y~\5\34\17\2z~\5*\26\2{~\5$\23\2|~\5\"\22\2}y\3"+
		"\2\2\2}z\3\2\2\2}{\3\2\2\2}|\3\2\2\2~\33\3\2\2\2\177\u0082\5(\25\2\u0080"+
		"\u0082\5\36\20\2\u0081\177\3\2\2\2\u0081\u0080\3\2\2\2\u0082\35\3\2\2"+
		"\2\u0083\u0084\t\4\2\2\u0084\37\3\2\2\2\u0085\u0086\t\5\2\2\u0086!\3\2"+
		"\2\2\u0087\u0088\7\r\2\2\u0088#\3\2\2\2\u0089\u008a\5\"\22\2\u008a\u008b"+
		"\5&\24\2\u008b\u0091\3\2\2\2\u008c\u008d\5\"\22\2\u008d\u008e\7\13\2\2"+
		"\u008e\u008f\5(\25\2\u008f\u0091\3\2\2\2\u0090\u0089\3\2\2\2\u0090\u008c"+
		"\3\2\2\2\u0091%\3\2\2\2\u0092\u0093\7\27\2\2\u0093\'\3\2\2\2\u0094\u0095"+
		"\t\6\2\2\u0095)\3\2\2\2\u0096\u009b\5.\30\2\u0097\u009b\5,\27\2\u0098"+
		"\u009b\5\62\32\2\u0099\u009b\5\64\33\2\u009a\u0096\3\2\2\2\u009a\u0097"+
		"\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u0099\3\2\2\2\u009b+\3\2\2\2\u009c"+
		"\u009e\5\60\31\2\u009d\u009f\5&\24\2\u009e\u009d\3\2\2\2\u009e\u009f\3"+
		"\2\2\2\u009f-\3\2\2\2\u00a0\u00a1\5\60\31\2\u00a1\u00a2\7\13\2\2\u00a2"+
		"\u00a3\5(\25\2\u00a3/\3\2\2\2\u00a4\u00a5\7$\2\2\u00a5\61\3\2\2\2\u00a6"+
		"\u00aa\5\66\34\2\u00a7\u00aa\58\35\2\u00a8\u00aa\5:\36\2\u00a9\u00a6\3"+
		"\2\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00a8\3\2\2\2\u00aa\63\3\2\2\2\u00ab"+
		"\u00ac\7\16\2\2\u00ac\65\3\2\2\2\u00ad\u00ae\t\7\2\2\u00ae\67\3\2\2\2"+
		"\u00af\u00b0\t\b\2\2\u00b09\3\2\2\2\u00b1\u00b2\t\t\2\2\u00b2;\3\2\2\2"+
		"\17?EQbmrw}\u0081\u0090\u009a\u009e\u00a9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}