// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/antlr4/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA.g4 by ANTLR 4.7
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
		T__9=10, WS=11, STRING_WITH_CURLY_BRACKET=12, BOOLEAN_LITERAL=13, IRIREF_EXT=14, 
		IRIREF=15, PNAME_NS=16, PN_PREFIX=17, PREFIXED_NAME=18, PREFIXED_NAME_EXT=19, 
		BLANK_NODE_FUNCTION=20, BLANK_NODE_LABEL=21, LANGTAG=22, INTEGER=23, DECIMAL=24, 
		DOUBLE=25, EXPONENT=26, INTEGER_POSITIVE=27, INTEGER_NEGATIVE=28, DOUBLE_POSITIVE=29, 
		DOUBLE_NEGATIVE=30, DECIMAL_POSITIVE=31, DECIMAL_NEGATIVE=32, STRING_LITERAL_LONG_SINGLE_QUOTE=33, 
		STRING_LITERAL_LONG_QUOTE=34, STRING_LITERAL_QUOTE=35, STRING_LITERAL_SINGLE_QUOTE=36, 
		UCHAR=37, ECHAR=38, ANON_WS=39, ANON=40, PN_CHARS_BASE=41, PN_CHARS_U=42, 
		PN_CHARS=43, PN_LOCAL_EXT=44, PN_LOCAL=45, PLX=46, PERCENT=47, HEX=48, 
		PN_LOCAL_ESC=49;
	public static final int
		RULE_parse = 0, RULE_directiveStatement = 1, RULE_triplesStatement = 2, 
		RULE_directive = 3, RULE_prefixID = 4, RULE_base = 5, RULE_triples = 6, 
		RULE_predicateObjectList = 7, RULE_predicateObject = 8, RULE_objectList = 9, 
		RULE_verb = 10, RULE_subject = 11, RULE_object = 12, RULE_resource = 13, 
		RULE_iriExt = 14, RULE_blank = 15, RULE_variable = 16, RULE_variableLiteral = 17, 
		RULE_languageTag = 18, RULE_iri = 19, RULE_literal = 20, RULE_typedLiteral = 21, 
		RULE_stringLiteral = 22, RULE_numericLiteral = 23, RULE_booleanLiteral = 24, 
		RULE_numericUnsigned = 25, RULE_numericPositive = 26, RULE_numericNegative = 27;
	public static final String[] ruleNames = {
		"parse", "directiveStatement", "triplesStatement", "directive", "prefixID", 
		"base", "triples", "predicateObjectList", "predicateObject", "objectList", 
		"verb", "subject", "object", "resource", "iriExt", "blank", "variable", 
		"variableLiteral", "languageTag", "iri", "literal", "typedLiteral", "stringLiteral", 
		"numericLiteral", "booleanLiteral", "numericUnsigned", "numericPositive", 
		"numericNegative"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'@prefix'", "'@PREFIX'", "'@base'", "'@BASE'", "';'", "','", 
		"'a'", "'^^'", "'@'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, "WS", 
		"STRING_WITH_CURLY_BRACKET", "BOOLEAN_LITERAL", "IRIREF_EXT", "IRIREF", 
		"PNAME_NS", "PN_PREFIX", "PREFIXED_NAME", "PREFIXED_NAME_EXT", "BLANK_NODE_FUNCTION", 
		"BLANK_NODE_LABEL", "LANGTAG", "INTEGER", "DECIMAL", "DOUBLE", "EXPONENT", 
		"INTEGER_POSITIVE", "INTEGER_NEGATIVE", "DOUBLE_POSITIVE", "DOUBLE_NEGATIVE", 
		"DECIMAL_POSITIVE", "DECIMAL_NEGATIVE", "STRING_LITERAL_LONG_SINGLE_QUOTE", 
		"STRING_LITERAL_LONG_QUOTE", "STRING_LITERAL_QUOTE", "STRING_LITERAL_SINGLE_QUOTE", 
		"UCHAR", "ECHAR", "ANON_WS", "ANON", "PN_CHARS_BASE", "PN_CHARS_U", "PN_CHARS", 
		"PN_LOCAL_EXT", "PN_LOCAL", "PLX", "PERCENT", "HEX", "PN_LOCAL_ESC"
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
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4))) != 0)) {
				{
				{
				setState(56);
				directiveStatement();
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
				setState(62);
				triplesStatement();
				}
				}
				setState(65); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING_WITH_CURLY_BRACKET) | (1L << IRIREF_EXT) | (1L << IRIREF) | (1L << PREFIXED_NAME) | (1L << PREFIXED_NAME_EXT) | (1L << BLANK_NODE_FUNCTION) | (1L << BLANK_NODE_LABEL) | (1L << ANON))) != 0) );
			setState(67);
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
			setState(69);
			directive();
			setState(70);
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
			setState(72);
			triples();
			setState(73);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitDirective(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveContext directive() throws RecognitionException {
		DirectiveContext _localctx = new DirectiveContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_directive);
		try {
			setState(77);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__3:
			case T__4:
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				base();
				}
				break;
			case T__1:
			case T__2:
				enterOuterAlt(_localctx, 2);
				{
				setState(76);
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
			setState(79);
			_la = _input.LA(1);
			if ( !(_la==T__1 || _la==T__2) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(80);
			match(PNAME_NS);
			setState(81);
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
			setState(83);
			_la = _input.LA(1);
			if ( !(_la==T__3 || _la==T__4) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(84);
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
			setState(86);
			subject();
			setState(87);
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
			setState(89);
			predicateObject();
			setState(94);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(90);
				match(T__5);
				setState(91);
				predicateObject();
				}
				}
				setState(96);
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
			setState(97);
			verb();
			setState(98);
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
			setState(100);
			object();
			setState(105);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__6) {
				{
				{
				setState(101);
				match(T__6);
				setState(102);
				object();
				}
				}
				setState(107);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitVerb(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VerbContext verb() throws RecognitionException {
		VerbContext _localctx = new VerbContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_verb);
		try {
			setState(110);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IRIREF_EXT:
			case IRIREF:
			case PREFIXED_NAME:
			case PREFIXED_NAME_EXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(108);
				resource();
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 2);
				{
				setState(109);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitSubject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubjectContext subject() throws RecognitionException {
		SubjectContext _localctx = new SubjectContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_subject);
		try {
			setState(115);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IRIREF_EXT:
			case IRIREF:
			case PREFIXED_NAME:
			case PREFIXED_NAME_EXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(112);
				resource();
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(113);
				variable();
				}
				break;
			case BLANK_NODE_FUNCTION:
			case BLANK_NODE_LABEL:
			case ANON:
				enterOuterAlt(_localctx, 3);
				{
				setState(114);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_object);
		try {
			setState(121);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(117);
				resource();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(119);
				variableLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(120);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_resource);
		try {
			setState(125);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IRIREF:
			case PREFIXED_NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(123);
				iri();
				}
				break;
			case IRIREF_EXT:
			case PREFIXED_NAME_EXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(124);
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
			setState(127);
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
			setState(129);
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
			setState(131);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitVariableLiteral_1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableLiteralContext variableLiteral() throws RecognitionException {
		VariableLiteralContext _localctx = new VariableLiteralContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_variableLiteral);
		try {
			setState(140);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				_localctx = new VariableLiteral_1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(133);
				variable();
				setState(134);
				languageTag();
				}
				break;
			case 2:
				_localctx = new VariableLiteral_2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(136);
				variable();
				setState(137);
				match(T__8);
				setState(138);
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
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public LanguageTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_languageTag; }
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
			setState(145);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LANGTAG:
				enterOuterAlt(_localctx, 1);
				{
				setState(142);
				match(LANGTAG);
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				setState(143);
				match(T__9);
				setState(144);
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

	public static class IriContext extends ParserRuleContext {
		public TerminalNode IRIREF() { return getToken(TurtleOBDAParser.IRIREF, 0); }
		public TerminalNode PREFIXED_NAME() { return getToken(TurtleOBDAParser.PREFIXED_NAME, 0); }
		public IriContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iri; }
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
			setState(147);
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
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public LanguageTagContext languageTag() {
			return getRuleContext(LanguageTagContext.class,0);
		}
		public TypedLiteralContext typedLiteral() {
			return getRuleContext(TypedLiteralContext.class,0);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_literal);
		int _la;
		try {
			setState(156);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(149);
				stringLiteral();
				setState(151);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__9 || _la==LANGTAG) {
					{
					setState(150);
					languageTag();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(153);
				typedLiteral();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(154);
				numericLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(155);
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

	public static class TypedLiteralContext extends ParserRuleContext {
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public IriContext iri() {
			return getRuleContext(IriContext.class,0);
		}
		public TypedLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typedLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitTypedLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypedLiteralContext typedLiteral() throws RecognitionException {
		TypedLiteralContext _localctx = new TypedLiteralContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_typedLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			stringLiteral();
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

	public static class StringLiteralContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL_QUOTE() { return getToken(TurtleOBDAParser.STRING_LITERAL_QUOTE, 0); }
		public StringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralContext stringLiteral() throws RecognitionException {
		StringLiteralContext _localctx = new StringLiteralContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_stringLiteral);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_numericLiteral);
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

	public static class BooleanLiteralContext extends ParserRuleContext {
		public TerminalNode BOOLEAN_LITERAL() { return getToken(TurtleOBDAParser.BOOLEAN_LITERAL, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_booleanLiteral);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericUnsigned(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericUnsignedContext numericUnsigned() throws RecognitionException {
		NumericUnsignedContext _localctx = new NumericUnsignedContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_numericUnsigned);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericPositive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericPositiveContext numericPositive() throws RecognitionException {
		NumericPositiveContext _localctx = new NumericPositiveContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_numericPositive);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TurtleOBDAVisitor ) return ((TurtleOBDAVisitor<? extends T>)visitor).visitNumericNegative(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericNegativeContext numericNegative() throws RecognitionException {
		NumericNegativeContext _localctx = new NumericNegativeContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_numericNegative);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\63\u00b4\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\7\2<\n\2\f\2\16\2?\13\2\3"+
		"\2\6\2B\n\2\r\2\16\2C\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\5\5P\n\5"+
		"\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\7\t_\n\t\f\t\16\t"+
		"b\13\t\3\n\3\n\3\n\3\13\3\13\3\13\7\13j\n\13\f\13\16\13m\13\13\3\f\3\f"+
		"\5\fq\n\f\3\r\3\r\3\r\5\rv\n\r\3\16\3\16\3\16\3\16\5\16|\n\16\3\17\3\17"+
		"\5\17\u0080\n\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\5\23\u008f\n\23\3\24\3\24\3\24\5\24\u0094\n\24\3\25\3\25\3"+
		"\26\3\26\5\26\u009a\n\26\3\26\3\26\3\26\5\26\u009f\n\26\3\27\3\27\3\27"+
		"\3\27\3\30\3\30\3\31\3\31\3\31\5\31\u00aa\n\31\3\32\3\32\3\33\3\33\3\34"+
		"\3\34\3\35\3\35\3\35\2\2\36\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \""+
		"$&(*,.\60\62\64\668\2\n\3\2\4\5\3\2\6\7\4\2\20\20\25\25\4\2\26\27**\4"+
		"\2\21\21\24\24\3\2\31\33\5\2\35\35\37\37!!\5\2\36\36  \"\"\2\u00ab\2="+
		"\3\2\2\2\4G\3\2\2\2\6J\3\2\2\2\bO\3\2\2\2\nQ\3\2\2\2\fU\3\2\2\2\16X\3"+
		"\2\2\2\20[\3\2\2\2\22c\3\2\2\2\24f\3\2\2\2\26p\3\2\2\2\30u\3\2\2\2\32"+
		"{\3\2\2\2\34\177\3\2\2\2\36\u0081\3\2\2\2 \u0083\3\2\2\2\"\u0085\3\2\2"+
		"\2$\u008e\3\2\2\2&\u0093\3\2\2\2(\u0095\3\2\2\2*\u009e\3\2\2\2,\u00a0"+
		"\3\2\2\2.\u00a4\3\2\2\2\60\u00a9\3\2\2\2\62\u00ab\3\2\2\2\64\u00ad\3\2"+
		"\2\2\66\u00af\3\2\2\28\u00b1\3\2\2\2:<\5\4\3\2;:\3\2\2\2<?\3\2\2\2=;\3"+
		"\2\2\2=>\3\2\2\2>A\3\2\2\2?=\3\2\2\2@B\5\6\4\2A@\3\2\2\2BC\3\2\2\2CA\3"+
		"\2\2\2CD\3\2\2\2DE\3\2\2\2EF\7\2\2\3F\3\3\2\2\2GH\5\b\5\2HI\7\3\2\2I\5"+
		"\3\2\2\2JK\5\16\b\2KL\7\3\2\2L\7\3\2\2\2MP\5\f\7\2NP\5\n\6\2OM\3\2\2\2"+
		"ON\3\2\2\2P\t\3\2\2\2QR\t\2\2\2RS\7\22\2\2ST\7\21\2\2T\13\3\2\2\2UV\t"+
		"\3\2\2VW\7\21\2\2W\r\3\2\2\2XY\5\30\r\2YZ\5\20\t\2Z\17\3\2\2\2[`\5\22"+
		"\n\2\\]\7\b\2\2]_\5\22\n\2^\\\3\2\2\2_b\3\2\2\2`^\3\2\2\2`a\3\2\2\2a\21"+
		"\3\2\2\2b`\3\2\2\2cd\5\26\f\2de\5\24\13\2e\23\3\2\2\2fk\5\32\16\2gh\7"+
		"\t\2\2hj\5\32\16\2ig\3\2\2\2jm\3\2\2\2ki\3\2\2\2kl\3\2\2\2l\25\3\2\2\2"+
		"mk\3\2\2\2nq\5\34\17\2oq\7\n\2\2pn\3\2\2\2po\3\2\2\2q\27\3\2\2\2rv\5\34"+
		"\17\2sv\5\"\22\2tv\5 \21\2ur\3\2\2\2us\3\2\2\2ut\3\2\2\2v\31\3\2\2\2w"+
		"|\5\34\17\2x|\5*\26\2y|\5$\23\2z|\5\"\22\2{w\3\2\2\2{x\3\2\2\2{y\3\2\2"+
		"\2{z\3\2\2\2|\33\3\2\2\2}\u0080\5(\25\2~\u0080\5\36\20\2\177}\3\2\2\2"+
		"\177~\3\2\2\2\u0080\35\3\2\2\2\u0081\u0082\t\4\2\2\u0082\37\3\2\2\2\u0083"+
		"\u0084\t\5\2\2\u0084!\3\2\2\2\u0085\u0086\7\16\2\2\u0086#\3\2\2\2\u0087"+
		"\u0088\5\"\22\2\u0088\u0089\5&\24\2\u0089\u008f\3\2\2\2\u008a\u008b\5"+
		"\"\22\2\u008b\u008c\7\13\2\2\u008c\u008d\5(\25\2\u008d\u008f\3\2\2\2\u008e"+
		"\u0087\3\2\2\2\u008e\u008a\3\2\2\2\u008f%\3\2\2\2\u0090\u0094\7\30\2\2"+
		"\u0091\u0092\7\f\2\2\u0092\u0094\5\"\22\2\u0093\u0090\3\2\2\2\u0093\u0091"+
		"\3\2\2\2\u0094\'\3\2\2\2\u0095\u0096\t\6\2\2\u0096)\3\2\2\2\u0097\u0099"+
		"\5.\30\2\u0098\u009a\5&\24\2\u0099\u0098\3\2\2\2\u0099\u009a\3\2\2\2\u009a"+
		"\u009f\3\2\2\2\u009b\u009f\5,\27\2\u009c\u009f\5\60\31\2\u009d\u009f\5"+
		"\62\32\2\u009e\u0097\3\2\2\2\u009e\u009b\3\2\2\2\u009e\u009c\3\2\2\2\u009e"+
		"\u009d\3\2\2\2\u009f+\3\2\2\2\u00a0\u00a1\5.\30\2\u00a1\u00a2\7\13\2\2"+
		"\u00a2\u00a3\5(\25\2\u00a3-\3\2\2\2\u00a4\u00a5\7%\2\2\u00a5/\3\2\2\2"+
		"\u00a6\u00aa\5\64\33\2\u00a7\u00aa\5\66\34\2\u00a8\u00aa\58\35\2\u00a9"+
		"\u00a6\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00a8\3\2\2\2\u00aa\61\3\2\2"+
		"\2\u00ab\u00ac\7\17\2\2\u00ac\63\3\2\2\2\u00ad\u00ae\t\7\2\2\u00ae\65"+
		"\3\2\2\2\u00af\u00b0\t\b\2\2\u00b0\67\3\2\2\2\u00b1\u00b2\t\t\2\2\u00b2"+
		"9\3\2\2\2\20=CO`kpu{\177\u008e\u0093\u0099\u009e\u00a9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}