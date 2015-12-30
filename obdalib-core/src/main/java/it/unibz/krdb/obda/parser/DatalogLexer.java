// $ANTLR 3.5.1 /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g 2014-10-07 15:45:47

package it.unibz.krdb.obda.parser;

import java.util.List;
import java.util.Vector;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class DatalogLexer extends Lexer {
	public static final int EOF=-1;
	public static final int ALPHA=4;
	public static final int ALPHANUM=5;
	public static final int AMPERSAND=6;
	public static final int APOSTROPHE=7;
	public static final int ASTERISK=8;
	public static final int AT=9;
	public static final int BACKSLASH=10;
	public static final int BASE=11;
	public static final int CARET=12;
	public static final int COLON=13;
	public static final int COMMA=14;
	public static final int DASH=15;
	public static final int DIGIT=16;
	public static final int DOLLAR=17;
	public static final int DOT=18;
	public static final int DOUBLE_SLASH=19;
	public static final int EQUALS=20;
	public static final int EXCLAMATION=21;
	public static final int GREATER=22;
	public static final int HASH=23;
	public static final int ID=24;
	public static final int ID_CORE=25;
	public static final int ID_PLAIN=26;
	public static final int ID_START=27;
	public static final int IMPLIES=28;
	public static final int INV_IMPLIES=29;
	public static final int LESS=30;
	public static final int LPAREN=31;
	public static final int LSQ_BRACKET=32;
	public static final int PERCENT=33;
	public static final int PLUS=34;
	public static final int PREFIX=35;
	public static final int QUESTION=36;
	public static final int QUOTE_DOUBLE=37;
	public static final int QUOTE_SINGLE=38;
	public static final int REFERENCE=39;
	public static final int RPAREN=40;
	public static final int RSQ_BRACKET=41;
	public static final int SCHEMA=42;
	public static final int SEMI=43;
	public static final int SLASH=44;
	public static final int STRING_LITERAL=45;
	public static final int STRING_LITERAL2=46;
	public static final int STRING_PREFIX=47;
	public static final int STRING_URI=48;
	public static final int TILDE=49;
	public static final int UNDERSCORE=50;
	public static final int URI_PATH=51;
	public static final int WS=52;

	private List<String> errors = new Vector<String>();

	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	    String hdr = getErrorHeader(e);
	    String msg = getErrorMessage(e, tokenNames);
	    errors.add(hdr + " " + msg);
	}

	public List<String> getErrors() {
	    return errors;
	}   


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public DatalogLexer() {} 
	public DatalogLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public DatalogLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g"; }

	// $ANTLR start "PREFIX"
	public final void mPREFIX() throws RecognitionException {
		try {
			int _type = PREFIX;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:407:7: ( ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:407:9: ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' )
			{
			if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PREFIX"

	// $ANTLR start "BASE"
	public final void mBASE() throws RecognitionException {
		try {
			int _type = BASE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:409:5: ( ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:409:7: ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
			{
			if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BASE"

	// $ANTLR start "IMPLIES"
	public final void mIMPLIES() throws RecognitionException {
		try {
			int _type = IMPLIES;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:411:8: ( '->' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:411:16: '->'
			{
			match("->"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IMPLIES"

	// $ANTLR start "INV_IMPLIES"
	public final void mINV_IMPLIES() throws RecognitionException {
		try {
			int _type = INV_IMPLIES;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:412:12: ( ':-' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:412:16: ':-'
			{
			match(":-"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INV_IMPLIES"

	// $ANTLR start "REFERENCE"
	public final void mREFERENCE() throws RecognitionException {
		try {
			int _type = REFERENCE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:413:10: ( '^^' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:413:16: '^^'
			{
			match("^^"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "REFERENCE"

	// $ANTLR start "SEMI"
	public final void mSEMI() throws RecognitionException {
		try {
			int _type = SEMI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:414:5: ( ';' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:414:16: ';'
			{
			match(';'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SEMI"

	// $ANTLR start "DOT"
	public final void mDOT() throws RecognitionException {
		try {
			int _type = DOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:415:4: ( '.' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:415:16: '.'
			{
			match('.'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOT"

	// $ANTLR start "COMMA"
	public final void mCOMMA() throws RecognitionException {
		try {
			int _type = COMMA;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:416:6: ( ',' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:416:16: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMA"

	// $ANTLR start "LSQ_BRACKET"
	public final void mLSQ_BRACKET() throws RecognitionException {
		try {
			int _type = LSQ_BRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:417:12: ( '[' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:417:16: '['
			{
			match('['); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LSQ_BRACKET"

	// $ANTLR start "RSQ_BRACKET"
	public final void mRSQ_BRACKET() throws RecognitionException {
		try {
			int _type = RSQ_BRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:418:12: ( ']' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:418:16: ']'
			{
			match(']'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RSQ_BRACKET"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:419:7: ( '(' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:419:16: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LPAREN"

	// $ANTLR start "RPAREN"
	public final void mRPAREN() throws RecognitionException {
		try {
			int _type = RPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:420:7: ( ')' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:420:16: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RPAREN"

	// $ANTLR start "QUESTION"
	public final void mQUESTION() throws RecognitionException {
		try {
			int _type = QUESTION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:421:9: ( '?' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:421:16: '?'
			{
			match('?'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "QUESTION"

	// $ANTLR start "DOLLAR"
	public final void mDOLLAR() throws RecognitionException {
		try {
			int _type = DOLLAR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:422:7: ( '$' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:422:16: '$'
			{
			match('$'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOLLAR"

	// $ANTLR start "QUOTE_DOUBLE"
	public final void mQUOTE_DOUBLE() throws RecognitionException {
		try {
			int _type = QUOTE_DOUBLE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:423:13: ( '\"' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:423:16: '\"'
			{
			match('\"'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "QUOTE_DOUBLE"

	// $ANTLR start "QUOTE_SINGLE"
	public final void mQUOTE_SINGLE() throws RecognitionException {
		try {
			int _type = QUOTE_SINGLE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:424:13: ( '\\'' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:424:16: '\\''
			{
			match('\''); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "QUOTE_SINGLE"

	// $ANTLR start "APOSTROPHE"
	public final void mAPOSTROPHE() throws RecognitionException {
		try {
			int _type = APOSTROPHE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:425:11: ( '`' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:425:16: '`'
			{
			match('`'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "APOSTROPHE"

	// $ANTLR start "UNDERSCORE"
	public final void mUNDERSCORE() throws RecognitionException {
		try {
			int _type = UNDERSCORE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:426:11: ( '_' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:426:16: '_'
			{
			match('_'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNDERSCORE"

	// $ANTLR start "DASH"
	public final void mDASH() throws RecognitionException {
		try {
			int _type = DASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:427:5: ( '-' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:427:16: '-'
			{
			match('-'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DASH"

	// $ANTLR start "ASTERISK"
	public final void mASTERISK() throws RecognitionException {
		try {
			int _type = ASTERISK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:428:9: ( '*' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:428:16: '*'
			{
			match('*'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ASTERISK"

	// $ANTLR start "AMPERSAND"
	public final void mAMPERSAND() throws RecognitionException {
		try {
			int _type = AMPERSAND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:429:10: ( '&' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:429:16: '&'
			{
			match('&'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AMPERSAND"

	// $ANTLR start "AT"
	public final void mAT() throws RecognitionException {
		try {
			int _type = AT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:430:3: ( '@' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:430:16: '@'
			{
			match('@'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AT"

	// $ANTLR start "EXCLAMATION"
	public final void mEXCLAMATION() throws RecognitionException {
		try {
			int _type = EXCLAMATION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:431:12: ( '!' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:431:16: '!'
			{
			match('!'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EXCLAMATION"

	// $ANTLR start "HASH"
	public final void mHASH() throws RecognitionException {
		try {
			int _type = HASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:432:5: ( '#' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:432:16: '#'
			{
			match('#'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HASH"

	// $ANTLR start "PERCENT"
	public final void mPERCENT() throws RecognitionException {
		try {
			int _type = PERCENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:433:8: ( '%' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:433:16: '%'
			{
			match('%'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PERCENT"

	// $ANTLR start "PLUS"
	public final void mPLUS() throws RecognitionException {
		try {
			int _type = PLUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:434:5: ( '+' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:434:16: '+'
			{
			match('+'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PLUS"

	// $ANTLR start "EQUALS"
	public final void mEQUALS() throws RecognitionException {
		try {
			int _type = EQUALS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:435:7: ( '=' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:435:16: '='
			{
			match('='); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EQUALS"

	// $ANTLR start "COLON"
	public final void mCOLON() throws RecognitionException {
		try {
			int _type = COLON;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:436:6: ( ':' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:436:16: ':'
			{
			match(':'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COLON"

	// $ANTLR start "LESS"
	public final void mLESS() throws RecognitionException {
		try {
			int _type = LESS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:437:5: ( '<' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:437:16: '<'
			{
			match('<'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS"

	// $ANTLR start "GREATER"
	public final void mGREATER() throws RecognitionException {
		try {
			int _type = GREATER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:438:8: ( '>' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:438:16: '>'
			{
			match('>'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GREATER"

	// $ANTLR start "SLASH"
	public final void mSLASH() throws RecognitionException {
		try {
			int _type = SLASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:439:6: ( '/' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:439:16: '/'
			{
			match('/'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SLASH"

	// $ANTLR start "DOUBLE_SLASH"
	public final void mDOUBLE_SLASH() throws RecognitionException {
		try {
			int _type = DOUBLE_SLASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:440:13: ( '//' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:440:16: '//'
			{
			match("//"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOUBLE_SLASH"

	// $ANTLR start "BACKSLASH"
	public final void mBACKSLASH() throws RecognitionException {
		try {
			int _type = BACKSLASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:441:10: ( '\\\\' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:441:16: '\\\\'
			{
			match('\\'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BACKSLASH"

	// $ANTLR start "TILDE"
	public final void mTILDE() throws RecognitionException {
		try {
			int _type = TILDE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:442:6: ( '~' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:442:16: '~'
			{
			match('~'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TILDE"

	// $ANTLR start "CARET"
	public final void mCARET() throws RecognitionException {
		try {
			int _type = CARET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:443:6: ( '^' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:443:16: '^'
			{
			match('^'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CARET"

	// $ANTLR start "ALPHA"
	public final void mALPHA() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:445:15: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ALPHA"

	// $ANTLR start "DIGIT"
	public final void mDIGIT() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:447:15: ( '0' .. '9' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DIGIT"

	// $ANTLR start "ALPHANUM"
	public final void mALPHANUM() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:449:18: ( ( ALPHA | DIGIT ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ALPHANUM"

	// $ANTLR start "ID_START"
	public final void mID_START() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:451:18: ( ( ALPHA | UNDERSCORE ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID_START"

	// $ANTLR start "ID_CORE"
	public final void mID_CORE() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:453:17: ( ( ID_START | DIGIT ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID_CORE"

	// $ANTLR start "SCHEMA"
	public final void mSCHEMA() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:455:16: ( ALPHA ( ALPHA | DIGIT | PLUS | DASH | DOT )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:455:18: ALPHA ( ALPHA | DIGIT | PLUS | DASH | DOT )*
			{
			mALPHA(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:455:24: ( ALPHA | DIGIT | PLUS | DASH | DOT )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0=='+'||(LA1_0 >= '-' && LA1_0 <= '.')||(LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
					{
					if ( input.LA(1)=='+'||(input.LA(1) >= '-' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop1;
				}
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SCHEMA"

	// $ANTLR start "URI_PATH"
	public final void mURI_PATH() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:457:18: ( ( ALPHANUM | UNDERSCORE | DASH | COLON | DOT | HASH | QUESTION | SLASH ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= ':')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "URI_PATH"

	// $ANTLR start "ID"
	public final void mID() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:459:12: ( ID_START ( ID_CORE )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:459:14: ID_START ( ID_CORE )*
			{
			mID_START(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:459:23: ( ID_CORE )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||LA2_0=='_'||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop2;
				}
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID"

	// $ANTLR start "ID_PLAIN"
	public final void mID_PLAIN() throws RecognitionException {
		try {
			int _type = ID_PLAIN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:461:9: ( ID_START ( ID_CORE )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:461:11: ID_START ( ID_CORE )*
			{
			mID_START(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:461:20: ( ID_CORE )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '0' && LA3_0 <= '9')||(LA3_0 >= 'A' && LA3_0 <= 'Z')||LA3_0=='_'||(LA3_0 >= 'a' && LA3_0 <= 'z')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop3;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID_PLAIN"

	// $ANTLR start "STRING_LITERAL"
	public final void mSTRING_LITERAL() throws RecognitionException {
		try {
			int _type = STRING_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:464:3: ( QUOTE_DOUBLE ( options {greedy=false; } : . )* QUOTE_DOUBLE )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:464:5: QUOTE_DOUBLE ( options {greedy=false; } : . )* QUOTE_DOUBLE
			{
			mQUOTE_DOUBLE(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:464:18: ( options {greedy=false; } : . )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0=='\"') ) {
					alt4=2;
				}
				else if ( ((LA4_0 >= '\u0000' && LA4_0 <= '!')||(LA4_0 >= '#' && LA4_0 <= '\uFFFF')) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:464:45: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop4;
				}
			}

			mQUOTE_DOUBLE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_LITERAL"

	// $ANTLR start "STRING_LITERAL2"
	public final void mSTRING_LITERAL2() throws RecognitionException {
		try {
			int _type = STRING_LITERAL2;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:468:3: ( QUOTE_SINGLE ( options {greedy=false; } : . )* QUOTE_SINGLE )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:468:5: QUOTE_SINGLE ( options {greedy=false; } : . )* QUOTE_SINGLE
			{
			mQUOTE_SINGLE(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:468:18: ( options {greedy=false; } : . )*
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( (LA5_0=='\'') ) {
					alt5=2;
				}
				else if ( ((LA5_0 >= '\u0000' && LA5_0 <= '&')||(LA5_0 >= '(' && LA5_0 <= '\uFFFF')) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:468:45: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop5;
				}
			}

			mQUOTE_SINGLE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_LITERAL2"

	// $ANTLR start "STRING_URI"
	public final void mSTRING_URI() throws RecognitionException {
		try {
			int _type = STRING_URI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:472:3: ( SCHEMA COLON DOUBLE_SLASH ( URI_PATH )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:472:5: SCHEMA COLON DOUBLE_SLASH ( URI_PATH )*
			{
			mSCHEMA(); 

			mCOLON(); 

			mDOUBLE_SLASH(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:472:31: ( URI_PATH )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0=='#'||(LA6_0 >= '-' && LA6_0 <= ':')||LA6_0=='?'||(LA6_0 >= 'A' && LA6_0 <= 'Z')||LA6_0=='_'||(LA6_0 >= 'a' && LA6_0 <= 'z')) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
					{
					if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= ':')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop6;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_URI"

	// $ANTLR start "STRING_PREFIX"
	public final void mSTRING_PREFIX() throws RecognitionException {
		try {
			int _type = STRING_PREFIX;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:476:3: ( ID COLON )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:476:5: ID COLON
			{
			mID(); 

			mCOLON(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_PREFIX"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			{
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			int cnt8=0;
			loop8:
			while (true) {
				int alt8=4;
				switch ( input.LA(1) ) {
				case ' ':
					{
					alt8=1;
					}
					break;
				case '\t':
					{
					alt8=2;
					}
					break;
				case '\n':
				case '\r':
					{
					alt8=3;
					}
					break;
				}
				switch (alt8) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:6: ' '
					{
					match(' '); 
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:10: '\\t'
					{
					match('\t'); 
					}
					break;
				case 3 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:15: ( '\\n' | '\\r' ( '\\n' ) )
					{
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:15: ( '\\n' | '\\r' ( '\\n' ) )
					int alt7=2;
					int LA7_0 = input.LA(1);
					if ( (LA7_0=='\n') ) {
						alt7=1;
					}
					else if ( (LA7_0=='\r') ) {
						alt7=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 7, 0, input);
						throw nvae;
					}

					switch (alt7) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:16: '\\n'
							{
							match('\n'); 
							}
							break;
						case 2 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:21: '\\r' ( '\\n' )
							{
							match('\r'); 
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:25: ( '\\n' )
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:479:26: '\\n'
							{
							match('\n'); 
							}

							}
							break;

					}

					}
					break;

				default :
					if ( cnt8 >= 1 ) break loop8;
					EarlyExitException eee = new EarlyExitException(8, input);
					throw eee;
				}
				cnt8++;
			}

			_channel=HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	@Override
	public void mTokens() throws RecognitionException {
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:8: ( PREFIX | BASE | IMPLIES | INV_IMPLIES | REFERENCE | SEMI | DOT | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | DASH | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | TILDE | CARET | ID_PLAIN | STRING_LITERAL | STRING_LITERAL2 | STRING_URI | STRING_PREFIX | WS )
		int alt9=41;
		alt9 = dfa9.predict(input);
		switch (alt9) {
			case 1 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:10: PREFIX
				{
				mPREFIX(); 

				}
				break;
			case 2 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:17: BASE
				{
				mBASE(); 

				}
				break;
			case 3 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:22: IMPLIES
				{
				mIMPLIES(); 

				}
				break;
			case 4 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:30: INV_IMPLIES
				{
				mINV_IMPLIES(); 

				}
				break;
			case 5 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:42: REFERENCE
				{
				mREFERENCE(); 

				}
				break;
			case 6 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:52: SEMI
				{
				mSEMI(); 

				}
				break;
			case 7 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:57: DOT
				{
				mDOT(); 

				}
				break;
			case 8 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:61: COMMA
				{
				mCOMMA(); 

				}
				break;
			case 9 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:67: LSQ_BRACKET
				{
				mLSQ_BRACKET(); 

				}
				break;
			case 10 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:79: RSQ_BRACKET
				{
				mRSQ_BRACKET(); 

				}
				break;
			case 11 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:91: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 12 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:98: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 13 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:105: QUESTION
				{
				mQUESTION(); 

				}
				break;
			case 14 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:114: DOLLAR
				{
				mDOLLAR(); 

				}
				break;
			case 15 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:121: QUOTE_DOUBLE
				{
				mQUOTE_DOUBLE(); 

				}
				break;
			case 16 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:134: QUOTE_SINGLE
				{
				mQUOTE_SINGLE(); 

				}
				break;
			case 17 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:147: APOSTROPHE
				{
				mAPOSTROPHE(); 

				}
				break;
			case 18 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:158: UNDERSCORE
				{
				mUNDERSCORE(); 

				}
				break;
			case 19 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:169: DASH
				{
				mDASH(); 

				}
				break;
			case 20 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:174: ASTERISK
				{
				mASTERISK(); 

				}
				break;
			case 21 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:183: AMPERSAND
				{
				mAMPERSAND(); 

				}
				break;
			case 22 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:193: AT
				{
				mAT(); 

				}
				break;
			case 23 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:196: EXCLAMATION
				{
				mEXCLAMATION(); 

				}
				break;
			case 24 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:208: HASH
				{
				mHASH(); 

				}
				break;
			case 25 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:213: PERCENT
				{
				mPERCENT(); 

				}
				break;
			case 26 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:221: PLUS
				{
				mPLUS(); 

				}
				break;
			case 27 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:226: EQUALS
				{
				mEQUALS(); 

				}
				break;
			case 28 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:233: COLON
				{
				mCOLON(); 

				}
				break;
			case 29 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:239: LESS
				{
				mLESS(); 

				}
				break;
			case 30 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:244: GREATER
				{
				mGREATER(); 

				}
				break;
			case 31 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:252: SLASH
				{
				mSLASH(); 

				}
				break;
			case 32 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:258: DOUBLE_SLASH
				{
				mDOUBLE_SLASH(); 

				}
				break;
			case 33 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:271: BACKSLASH
				{
				mBACKSLASH(); 

				}
				break;
			case 34 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:281: TILDE
				{
				mTILDE(); 

				}
				break;
			case 35 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:287: CARET
				{
				mCARET(); 

				}
				break;
			case 36 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:293: ID_PLAIN
				{
				mID_PLAIN(); 

				}
				break;
			case 37 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:302: STRING_LITERAL
				{
				mSTRING_LITERAL(); 

				}
				break;
			case 38 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:317: STRING_LITERAL2
				{
				mSTRING_LITERAL2(); 

				}
				break;
			case 39 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:333: STRING_URI
				{
				mSTRING_URI(); 

				}
				break;
			case 40 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:344: STRING_PREFIX
				{
				mSTRING_PREFIX(); 

				}
				break;
			case 41 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:1:358: WS
				{
				mWS(); 

				}
				break;

		}
	}


	protected DFA9 dfa9 = new DFA9(this);
	static final String DFA9_eotS =
		"\1\uffff\2\43\1\52\1\54\1\56\11\uffff\1\57\1\61\1\uffff\1\63\12\uffff"+
		"\1\66\2\uffff\1\43\1\uffff\1\43\1\uffff\2\43\1\uffff\1\64\1\43\16\uffff"+
		"\3\43\1\74\1\43\1\uffff\1\76\1\uffff";
	static final String DFA9_eofS =
		"\77\uffff";
	static final String DFA9_minS =
		"\1\11\2\53\1\76\1\55\1\136\11\uffff\2\0\1\uffff\1\60\12\uffff\1\57\2\uffff"+
		"\1\53\1\uffff\1\53\1\uffff\1\53\1\60\1\uffff\1\57\1\53\16\uffff\5\53\1"+
		"\uffff\1\53\1\uffff";
	static final String DFA9_maxS =
		"\1\176\2\172\1\76\1\55\1\136\11\uffff\2\uffff\1\uffff\1\172\12\uffff\1"+
		"\57\2\uffff\1\172\1\uffff\1\172\1\uffff\2\172\1\uffff\1\57\1\172\16\uffff"+
		"\5\172\1\uffff\1\172\1\uffff";
	static final String DFA9_acceptS =
		"\6\uffff\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\2\uffff\1\21\1\uffff"+
		"\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\35\1\36\1\uffff\1\41\1\42\1"+
		"\uffff\1\51\1\uffff\1\44\2\uffff\1\47\2\uffff\1\3\1\23\1\4\1\34\1\5\1"+
		"\43\1\17\1\45\1\20\1\46\1\22\1\50\1\40\1\37\5\uffff\1\2\1\uffff\1\1";
	static final String DFA9_specialS =
		"\17\uffff\1\1\1\0\56\uffff}>";
	static final String[] DFA9_transitionS = {
			"\2\41\2\uffff\1\41\22\uffff\1\41\1\26\1\17\1\27\1\16\1\30\1\24\1\20\1"+
			"\13\1\14\1\23\1\31\1\10\1\3\1\7\1\35\12\uffff\1\4\1\6\1\33\1\32\1\34"+
			"\1\15\1\25\1\40\1\2\15\40\1\1\12\40\1\11\1\36\1\12\1\5\1\22\1\21\1\40"+
			"\1\2\15\40\1\1\12\40\3\uffff\1\37",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\21\44\1\42\10\44\4\uffff"+
			"\1\45\1\uffff\21\44\1\42\10\44",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\1\50\31\44\4\uffff\1\45"+
			"\1\uffff\1\50\31\44",
			"\1\51",
			"\1\53",
			"\1\55",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\0\60",
			"\0\62",
			"",
			"\12\45\1\64\6\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\65",
			"",
			"",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\32\44\4\uffff\1\45\1\uffff"+
			"\32\44",
			"",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\4\44\1\67\25\44\4\uffff"+
			"\1\45\1\uffff\4\44\1\67\25\44",
			"",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\32\44\4\uffff\1\45\1\uffff"+
			"\32\44",
			"\12\45\1\64\6\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"\1\46",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\22\44\1\70\7\44\4\uffff"+
			"\1\45\1\uffff\22\44\1\70\7\44",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\5\44\1\71\24\44\4\uffff"+
			"\1\45\1\uffff\5\44\1\71\24\44",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\4\44\1\72\25\44\4\uffff"+
			"\1\45\1\uffff\4\44\1\72\25\44",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\10\44\1\73\21\44\4\uffff"+
			"\1\45\1\uffff\10\44\1\73\21\44",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\32\44\4\uffff\1\45\1\uffff"+
			"\32\44",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\27\44\1\75\2\44\4\uffff"+
			"\1\45\1\uffff\27\44\1\75\2\44",
			"",
			"\1\46\1\uffff\2\46\1\uffff\12\44\1\47\6\uffff\32\44\4\uffff\1\45\1\uffff"+
			"\32\44",
			""
	};

	static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
	static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
	static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
	static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
	static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
	static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
	static final short[][] DFA9_transition;

	static {
		int numStates = DFA9_transitionS.length;
		DFA9_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
		}
	}

	protected class DFA9 extends DFA {

		public DFA9(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 9;
			this.eot = DFA9_eot;
			this.eof = DFA9_eof;
			this.min = DFA9_min;
			this.max = DFA9_max;
			this.accept = DFA9_accept;
			this.special = DFA9_special;
			this.transition = DFA9_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( PREFIX | BASE | IMPLIES | INV_IMPLIES | REFERENCE | SEMI | DOT | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | DASH | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | TILDE | CARET | ID_PLAIN | STRING_LITERAL | STRING_LITERAL2 | STRING_URI | STRING_PREFIX | WS );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA9_16 = input.LA(1);
						s = -1;
						if ( ((LA9_16 >= '\u0000' && LA9_16 <= '\uFFFF')) ) {s = 50;}
						else s = 49;
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA9_15 = input.LA(1);
						s = -1;
						if ( ((LA9_15 >= '\u0000' && LA9_15 <= '\uFFFF')) ) {s = 48;}
						else s = 47;
						if ( s>=0 ) return s;
						break;
			}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 9, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
