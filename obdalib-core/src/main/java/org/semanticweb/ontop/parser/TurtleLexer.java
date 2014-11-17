// $ANTLR 3.5.1 /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g 2014-10-07 15:46:29

package org.semanticweb.ontop.parser;

import java.util.List;
import java.util.Vector;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class TurtleLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__75=75;
	public static final int ALPHA=4;
	public static final int ALPHANUM=5;
	public static final int AMPERSAND=6;
	public static final int APOSTROPHE=7;
	public static final int ASTERISK=8;
	public static final int AT=9;
	public static final int BACKSLASH=10;
	public static final int BASE=11;
	public static final int BLANK=12;
	public static final int BLANK_PREFIX=13;
	public static final int CARET=14;
	public static final int CHAR=15;
	public static final int COLON=16;
	public static final int COMMA=17;
	public static final int DECIMAL=18;
	public static final int DECIMAL_NEGATIVE=19;
	public static final int DECIMAL_POSITIVE=20;
	public static final int DIGIT=21;
	public static final int DOLLAR=22;
	public static final int DOUBLE=23;
	public static final int DOUBLE_NEGATIVE=24;
	public static final int DOUBLE_POSITIVE=25;
	public static final int DOUBLE_SLASH=26;
	public static final int ECHAR=27;
	public static final int EQUALS=28;
	public static final int EXCLAMATION=29;
	public static final int FALSE=30;
	public static final int GREATER=31;
	public static final int HASH=32;
	public static final int ID=33;
	public static final int ID_CORE=34;
	public static final int ID_START=35;
	public static final int INTEGER=36;
	public static final int INTEGER_NEGATIVE=37;
	public static final int INTEGER_POSITIVE=38;
	public static final int LCR_BRACKET=39;
	public static final int LESS=40;
	public static final int LPAREN=41;
	public static final int LSQ_BRACKET=42;
	public static final int LTSIGN=43;
	public static final int MINUS=44;
	public static final int NAMESPACE=45;
	public static final int NAME_CHAR=46;
	public static final int NAME_START_CHAR=47;
	public static final int NCNAME=48;
	public static final int PERCENT=49;
	public static final int PERIOD=50;
	public static final int PLUS=51;
	public static final int PREFIX=52;
	public static final int PREFIXED_NAME=53;
	public static final int QUESTION=54;
	public static final int QUOTE_DOUBLE=55;
	public static final int QUOTE_SINGLE=56;
	public static final int RCR_BRACKET=57;
	public static final int REFERENCE=58;
	public static final int RPAREN=59;
	public static final int RSQ_BRACKET=60;
	public static final int RTSIGN=61;
	public static final int SCHEMA=62;
	public static final int SEMI=63;
	public static final int SLASH=64;
	public static final int STRING_URI=65;
	public static final int STRING_WITH_QUOTE=66;
	public static final int STRING_WITH_QUOTE_DOUBLE=67;
	public static final int STRING_WITH_TEMPLATE_SIGN=68;
	public static final int TILDE=69;
	public static final int TRUE=70;
	public static final int UNDERSCORE=71;
	public static final int URI_PATH=72;
	public static final int VARNAME=73;
	public static final int WS=74;

	private String error = "";
	    
	public String getError() {
		return error;
	}

	@Override
	public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
	    throws RecognitionException
	{
	    throw e;
	}

	@Override
	public void recover(IntStream input, RecognitionException re) {
		throw new RuntimeException(error);
	}
	    
	@Override
	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	    String hdr = getErrorHeader(e);
	    String msg = getErrorMessage(e, tokenNames);
	        
	    emitErrorMessage("Syntax error: " + msg + " Location: " + hdr);
	}

	@Override
	public void emitErrorMessage(String msg) 	{
		error = msg;
		throw new RuntimeException(error);
	}
	    
	@Override
	public Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow)
	    throws RecognitionException {
	    throw new RecognitionException(input);
	}


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public TurtleLexer() {} 
	public TurtleLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public TurtleLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g"; }

	// $ANTLR start "T__75"
	public final void mT__75() throws RecognitionException {
		try {
			int _type = T__75;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:48:7: ( 'a' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:48:9: 'a'
			{
			match('a'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__75"

	// $ANTLR start "BASE"
	public final void mBASE() throws RecognitionException {
		try {
			int _type = BASE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:558:5: ( ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:558:7: ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
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

	// $ANTLR start "PREFIX"
	public final void mPREFIX() throws RecognitionException {
		try {
			int _type = PREFIX;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:560:7: ( ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:560:9: ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' )
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

	// $ANTLR start "FALSE"
	public final void mFALSE() throws RecognitionException {
		try {
			int _type = FALSE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:562:6: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:562:8: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
			{
			if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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
			if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
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
	// $ANTLR end "FALSE"

	// $ANTLR start "TRUE"
	public final void mTRUE() throws RecognitionException {
		try {
			int _type = TRUE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:564:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:564:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
			{
			if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
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
			if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
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
	// $ANTLR end "TRUE"

	// $ANTLR start "REFERENCE"
	public final void mREFERENCE() throws RecognitionException {
		try {
			int _type = REFERENCE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:566:10: ( '^^' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:566:16: '^^'
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

	// $ANTLR start "LTSIGN"
	public final void mLTSIGN() throws RecognitionException {
		try {
			int _type = LTSIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:567:7: ( '<\"' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:567:16: '<\"'
			{
			match("<\""); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LTSIGN"

	// $ANTLR start "RTSIGN"
	public final void mRTSIGN() throws RecognitionException {
		try {
			int _type = RTSIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:568:7: ( '\">' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:568:16: '\">'
			{
			match("\">"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RTSIGN"

	// $ANTLR start "SEMI"
	public final void mSEMI() throws RecognitionException {
		try {
			int _type = SEMI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:569:5: ( ';' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:569:16: ';'
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

	// $ANTLR start "PERIOD"
	public final void mPERIOD() throws RecognitionException {
		try {
			int _type = PERIOD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:570:7: ( '.' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:570:16: '.'
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
	// $ANTLR end "PERIOD"

	// $ANTLR start "COMMA"
	public final void mCOMMA() throws RecognitionException {
		try {
			int _type = COMMA;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:571:6: ( ',' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:571:16: ','
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:572:12: ( '[' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:572:16: '['
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:573:12: ( ']' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:573:16: ']'
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

	// $ANTLR start "LCR_BRACKET"
	public final void mLCR_BRACKET() throws RecognitionException {
		try {
			int _type = LCR_BRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:574:12: ( '{' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:574:16: '{'
			{
			match('{'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LCR_BRACKET"

	// $ANTLR start "RCR_BRACKET"
	public final void mRCR_BRACKET() throws RecognitionException {
		try {
			int _type = RCR_BRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:575:12: ( '}' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:575:16: '}'
			{
			match('}'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RCR_BRACKET"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:576:7: ( '(' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:576:16: '('
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:577:7: ( ')' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:577:16: ')'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:578:9: ( '?' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:578:16: '?'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:579:7: ( '$' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:579:16: '$'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:580:13: ( '\"' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:580:16: '\"'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:581:13: ( '\\'' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:581:16: '\\''
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:582:11: ( '`' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:582:16: '`'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:583:11: ( '_' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:583:16: '_'
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

	// $ANTLR start "MINUS"
	public final void mMINUS() throws RecognitionException {
		try {
			int _type = MINUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:584:6: ( '-' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:584:16: '-'
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
	// $ANTLR end "MINUS"

	// $ANTLR start "ASTERISK"
	public final void mASTERISK() throws RecognitionException {
		try {
			int _type = ASTERISK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:585:9: ( '*' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:585:16: '*'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:586:10: ( '&' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:586:16: '&'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:587:3: ( '@' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:587:16: '@'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:588:12: ( '!' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:588:16: '!'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:589:5: ( '#' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:589:16: '#'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:590:8: ( '%' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:590:16: '%'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:591:5: ( '+' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:591:16: '+'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:592:7: ( '=' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:592:16: '='
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:593:6: ( ':' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:593:16: ':'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:594:5: ( '<' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:594:16: '<'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:595:8: ( '>' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:595:16: '>'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:596:6: ( '/' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:596:16: '/'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:597:13: ( '//' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:597:16: '//'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:598:10: ( '\\\\' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:598:16: '\\\\'
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

	// $ANTLR start "BLANK"
	public final void mBLANK() throws RecognitionException {
		try {
			int _type = BLANK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:599:6: ( '[]' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:599:15: '[]'
			{
			match("[]"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BLANK"

	// $ANTLR start "BLANK_PREFIX"
	public final void mBLANK_PREFIX() throws RecognitionException {
		try {
			int _type = BLANK_PREFIX;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:600:13: ( '_:' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:600:16: '_:'
			{
			match("_:"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BLANK_PREFIX"

	// $ANTLR start "TILDE"
	public final void mTILDE() throws RecognitionException {
		try {
			int _type = TILDE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:601:6: ( '~' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:601:16: '~'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:602:6: ( '^' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:602:16: '^'
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:605:3: ( 'a' .. 'z' | 'A' .. 'Z' | '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u02FF' | '\\u0370' .. '\\u037D' | '\\u037F' .. '\\u1FFF' | '\\u200C' .. '\\u200D' | '\\u2070' .. '\\u218F' | '\\u2C00' .. '\\u2FEF' | '\\u3001' .. '\\uD7FF' | '\\uF900' .. '\\uFDCF' | '\\uFDF0' .. '\\uFFFD' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:621:3: ( '0' .. '9' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:625:3: ( ALPHA | DIGIT )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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

	// $ANTLR start "CHAR"
	public final void mCHAR() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:630:3: ( ALPHANUM | UNDERSCORE | MINUS | PERIOD )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( (input.LA(1) >= '-' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
	// $ANTLR end "CHAR"

	// $ANTLR start "INTEGER"
	public final void mINTEGER() throws RecognitionException {
		try {
			int _type = INTEGER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:637:3: ( ( DIGIT )+ )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:637:5: ( DIGIT )+
			{
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:637:5: ( DIGIT )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '0' && LA1_0 <= '9')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGER"

	// $ANTLR start "DOUBLE"
	public final void mDOUBLE() throws RecognitionException {
		try {
			int _type = DOUBLE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:641:3: ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? )
			int alt9=3;
			alt9 = dfa9.predict(input);
			switch (alt9) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:641:5: ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )?
					{
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:641:5: ( DIGIT )+
					int cnt2=0;
					loop2:
					while (true) {
						int alt2=2;
						int LA2_0 = input.LA(1);
						if ( ((LA2_0 >= '0' && LA2_0 <= '9')) ) {
							alt2=1;
						}

						switch (alt2) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
							break;

						default :
							if ( cnt2 >= 1 ) break loop2;
							EarlyExitException eee = new EarlyExitException(2, input);
							throw eee;
						}
						cnt2++;
					}

					mPERIOD(); 

					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:641:19: ( DIGIT )*
					loop3:
					while (true) {
						int alt3=2;
						int LA3_0 = input.LA(1);
						if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
							alt3=1;
						}

						switch (alt3) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
							break;

						default :
							break loop3;
						}
					}

					if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:641:36: ( '-' | '+' )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0=='+'||LA4_0=='-') ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
							{
							if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
								input.consume();
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:642:5: PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
					{
					mPERIOD(); 

					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:642:12: ( DIGIT )+
					int cnt5=0;
					loop5:
					while (true) {
						int alt5=2;
						int LA5_0 = input.LA(1);
						if ( ((LA5_0 >= '0' && LA5_0 <= '9')) ) {
							alt5=1;
						}

						switch (alt5) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
							break;

						default :
							if ( cnt5 >= 1 ) break loop5;
							EarlyExitException eee = new EarlyExitException(5, input);
							throw eee;
						}
						cnt5++;
					}

					if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:642:29: ( '-' | '+' )?
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0=='+'||LA6_0=='-') ) {
						alt6=1;
					}
					switch (alt6) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
							{
							if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
								input.consume();
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;
				case 3 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:643:5: ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
					{
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:643:5: ( DIGIT )+
					int cnt7=0;
					loop7:
					while (true) {
						int alt7=2;
						int LA7_0 = input.LA(1);
						if ( ((LA7_0 >= '0' && LA7_0 <= '9')) ) {
							alt7=1;
						}

						switch (alt7) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
							break;

						default :
							if ( cnt7 >= 1 ) break loop7;
							EarlyExitException eee = new EarlyExitException(7, input);
							throw eee;
						}
						cnt7++;
					}

					if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:643:22: ( '-' | '+' )?
					int alt8=2;
					int LA8_0 = input.LA(1);
					if ( (LA8_0=='+'||LA8_0=='-') ) {
						alt8=1;
					}
					switch (alt8) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
							{
							if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
								input.consume();
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOUBLE"

	// $ANTLR start "DECIMAL"
	public final void mDECIMAL() throws RecognitionException {
		try {
			int _type = DECIMAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:647:3: ( ( DIGIT )+ PERIOD ( DIGIT )+ | PERIOD ( DIGIT )+ )
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( ((LA13_0 >= '0' && LA13_0 <= '9')) ) {
				alt13=1;
			}
			else if ( (LA13_0=='.') ) {
				alt13=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}

			switch (alt13) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:647:5: ( DIGIT )+ PERIOD ( DIGIT )+
					{
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:647:5: ( DIGIT )+
					int cnt10=0;
					loop10:
					while (true) {
						int alt10=2;
						int LA10_0 = input.LA(1);
						if ( ((LA10_0 >= '0' && LA10_0 <= '9')) ) {
							alt10=1;
						}

						switch (alt10) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
							break;

						default :
							if ( cnt10 >= 1 ) break loop10;
							EarlyExitException eee = new EarlyExitException(10, input);
							throw eee;
						}
						cnt10++;
					}

					mPERIOD(); 

					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:647:19: ( DIGIT )+
					int cnt11=0;
					loop11:
					while (true) {
						int alt11=2;
						int LA11_0 = input.LA(1);
						if ( ((LA11_0 >= '0' && LA11_0 <= '9')) ) {
							alt11=1;
						}

						switch (alt11) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
							break;

						default :
							if ( cnt11 >= 1 ) break loop11;
							EarlyExitException eee = new EarlyExitException(11, input);
							throw eee;
						}
						cnt11++;
					}

					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:648:5: PERIOD ( DIGIT )+
					{
					mPERIOD(); 

					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:648:12: ( DIGIT )+
					int cnt12=0;
					loop12:
					while (true) {
						int alt12=2;
						int LA12_0 = input.LA(1);
						if ( ((LA12_0 >= '0' && LA12_0 <= '9')) ) {
							alt12=1;
						}

						switch (alt12) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
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
							break;

						default :
							if ( cnt12 >= 1 ) break loop12;
							EarlyExitException eee = new EarlyExitException(12, input);
							throw eee;
						}
						cnt12++;
					}

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DECIMAL"

	// $ANTLR start "INTEGER_POSITIVE"
	public final void mINTEGER_POSITIVE() throws RecognitionException {
		try {
			int _type = INTEGER_POSITIVE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:652:3: ( PLUS INTEGER )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:652:5: PLUS INTEGER
			{
			mPLUS(); 

			mINTEGER(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGER_POSITIVE"

	// $ANTLR start "INTEGER_NEGATIVE"
	public final void mINTEGER_NEGATIVE() throws RecognitionException {
		try {
			int _type = INTEGER_NEGATIVE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:656:3: ( MINUS INTEGER )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:656:5: MINUS INTEGER
			{
			mMINUS(); 

			mINTEGER(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGER_NEGATIVE"

	// $ANTLR start "DOUBLE_POSITIVE"
	public final void mDOUBLE_POSITIVE() throws RecognitionException {
		try {
			int _type = DOUBLE_POSITIVE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:660:3: ( PLUS DOUBLE )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:660:5: PLUS DOUBLE
			{
			mPLUS(); 

			mDOUBLE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOUBLE_POSITIVE"

	// $ANTLR start "DOUBLE_NEGATIVE"
	public final void mDOUBLE_NEGATIVE() throws RecognitionException {
		try {
			int _type = DOUBLE_NEGATIVE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:664:3: ( MINUS DOUBLE )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:664:5: MINUS DOUBLE
			{
			mMINUS(); 

			mDOUBLE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOUBLE_NEGATIVE"

	// $ANTLR start "DECIMAL_POSITIVE"
	public final void mDECIMAL_POSITIVE() throws RecognitionException {
		try {
			int _type = DECIMAL_POSITIVE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:668:3: ( PLUS DECIMAL )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:668:5: PLUS DECIMAL
			{
			mPLUS(); 

			mDECIMAL(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DECIMAL_POSITIVE"

	// $ANTLR start "DECIMAL_NEGATIVE"
	public final void mDECIMAL_NEGATIVE() throws RecognitionException {
		try {
			int _type = DECIMAL_NEGATIVE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:672:3: ( MINUS DECIMAL )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:672:5: MINUS DECIMAL
			{
			mMINUS(); 

			mDECIMAL(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DECIMAL_NEGATIVE"

	// $ANTLR start "VARNAME"
	public final void mVARNAME() throws RecognitionException {
		try {
			int _type = VARNAME;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:676:3: ( ALPHA ( CHAR )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:676:5: ALPHA ( CHAR )*
			{
			mALPHA(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:676:11: ( CHAR )*
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( ((LA14_0 >= '-' && LA14_0 <= '.')||(LA14_0 >= '0' && LA14_0 <= '9')||(LA14_0 >= 'A' && LA14_0 <= 'Z')||LA14_0=='_'||(LA14_0 >= 'a' && LA14_0 <= 'z')||(LA14_0 >= '\u00C0' && LA14_0 <= '\u00D6')||(LA14_0 >= '\u00D8' && LA14_0 <= '\u00F6')||(LA14_0 >= '\u00F8' && LA14_0 <= '\u02FF')||(LA14_0 >= '\u0370' && LA14_0 <= '\u037D')||(LA14_0 >= '\u037F' && LA14_0 <= '\u1FFF')||(LA14_0 >= '\u200C' && LA14_0 <= '\u200D')||(LA14_0 >= '\u2070' && LA14_0 <= '\u218F')||(LA14_0 >= '\u2C00' && LA14_0 <= '\u2FEF')||(LA14_0 >= '\u3001' && LA14_0 <= '\uD7FF')||(LA14_0 >= '\uF900' && LA14_0 <= '\uFDCF')||(LA14_0 >= '\uFDF0' && LA14_0 <= '\uFFFD')) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
					{
					if ( (input.LA(1) >= '-' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
					break loop14;
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
	// $ANTLR end "VARNAME"

	// $ANTLR start "ECHAR"
	public final void mECHAR() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:680:3: ( '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:680:5: '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' )
			{
			match('\\'); 
			if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
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
	// $ANTLR end "ECHAR"

	// $ANTLR start "SCHEMA"
	public final void mSCHEMA() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:683:16: ( ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:683:18: ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )*
			{
			mALPHA(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:683:24: ( ALPHANUM | PLUS | MINUS | PERIOD )*
			loop15:
			while (true) {
				int alt15=2;
				int LA15_0 = input.LA(1);
				if ( (LA15_0=='+'||(LA15_0 >= '-' && LA15_0 <= '.')||(LA15_0 >= '0' && LA15_0 <= '9')||(LA15_0 >= 'A' && LA15_0 <= 'Z')||(LA15_0 >= 'a' && LA15_0 <= 'z')||(LA15_0 >= '\u00C0' && LA15_0 <= '\u00D6')||(LA15_0 >= '\u00D8' && LA15_0 <= '\u00F6')||(LA15_0 >= '\u00F8' && LA15_0 <= '\u02FF')||(LA15_0 >= '\u0370' && LA15_0 <= '\u037D')||(LA15_0 >= '\u037F' && LA15_0 <= '\u1FFF')||(LA15_0 >= '\u200C' && LA15_0 <= '\u200D')||(LA15_0 >= '\u2070' && LA15_0 <= '\u218F')||(LA15_0 >= '\u2C00' && LA15_0 <= '\u2FEF')||(LA15_0 >= '\u3001' && LA15_0 <= '\uD7FF')||(LA15_0 >= '\uF900' && LA15_0 <= '\uFDCF')||(LA15_0 >= '\uFDF0' && LA15_0 <= '\uFFFD')) ) {
					alt15=1;
				}

				switch (alt15) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
					{
					if ( input.LA(1)=='+'||(input.LA(1) >= '-' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
					break loop15;
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:685:18: ( ( ALPHANUM | UNDERSCORE | MINUS | COLON | PERIOD | HASH | QUESTION | SLASH ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= ':')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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

	// $ANTLR start "ID_START"
	public final void mID_START() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:687:18: ( ( ALPHA | UNDERSCORE ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:689:17: ( ( ID_START | DIGIT ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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

	// $ANTLR start "ID"
	public final void mID() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:691:12: ( ID_START ( ID_CORE )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:691:14: ID_START ( ID_CORE )*
			{
			mID_START(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:691:23: ( ID_CORE )*
			loop16:
			while (true) {
				int alt16=2;
				int LA16_0 = input.LA(1);
				if ( ((LA16_0 >= '0' && LA16_0 <= '9')||(LA16_0 >= 'A' && LA16_0 <= 'Z')||LA16_0=='_'||(LA16_0 >= 'a' && LA16_0 <= 'z')||(LA16_0 >= '\u00C0' && LA16_0 <= '\u00D6')||(LA16_0 >= '\u00D8' && LA16_0 <= '\u00F6')||(LA16_0 >= '\u00F8' && LA16_0 <= '\u02FF')||(LA16_0 >= '\u0370' && LA16_0 <= '\u037D')||(LA16_0 >= '\u037F' && LA16_0 <= '\u1FFF')||(LA16_0 >= '\u200C' && LA16_0 <= '\u200D')||(LA16_0 >= '\u2070' && LA16_0 <= '\u218F')||(LA16_0 >= '\u2C00' && LA16_0 <= '\u2FEF')||(LA16_0 >= '\u3001' && LA16_0 <= '\uD7FF')||(LA16_0 >= '\uF900' && LA16_0 <= '\uFDCF')||(LA16_0 >= '\uFDF0' && LA16_0 <= '\uFFFD')) ) {
					alt16=1;
				}

				switch (alt16) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
					break loop16;
				}
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID"

	// $ANTLR start "NAME_START_CHAR"
	public final void mNAME_START_CHAR() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:693:25: ( ( ALPHA | UNDERSCORE ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
	// $ANTLR end "NAME_START_CHAR"

	// $ANTLR start "NAME_CHAR"
	public final void mNAME_CHAR() throws RecognitionException {
		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:695:19: ( ( NAME_START_CHAR | DIGIT | UNDERSCORE | MINUS | PERIOD | HASH | QUESTION | SLASH ) )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
			{
			if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= '9')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
	// $ANTLR end "NAME_CHAR"

	// $ANTLR start "NCNAME"
	public final void mNCNAME() throws RecognitionException {
		try {
			int _type = NCNAME;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:698:3: ( NAME_START_CHAR ( NAME_CHAR )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:698:5: NAME_START_CHAR ( NAME_CHAR )*
			{
			mNAME_START_CHAR(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:698:21: ( NAME_CHAR )*
			loop17:
			while (true) {
				int alt17=2;
				int LA17_0 = input.LA(1);
				if ( (LA17_0=='#'||(LA17_0 >= '-' && LA17_0 <= '9')||LA17_0=='?'||(LA17_0 >= 'A' && LA17_0 <= 'Z')||LA17_0=='_'||(LA17_0 >= 'a' && LA17_0 <= 'z')||(LA17_0 >= '\u00C0' && LA17_0 <= '\u00D6')||(LA17_0 >= '\u00D8' && LA17_0 <= '\u00F6')||(LA17_0 >= '\u00F8' && LA17_0 <= '\u02FF')||(LA17_0 >= '\u0370' && LA17_0 <= '\u037D')||(LA17_0 >= '\u037F' && LA17_0 <= '\u1FFF')||(LA17_0 >= '\u200C' && LA17_0 <= '\u200D')||(LA17_0 >= '\u2070' && LA17_0 <= '\u218F')||(LA17_0 >= '\u2C00' && LA17_0 <= '\u2FEF')||(LA17_0 >= '\u3001' && LA17_0 <= '\uD7FF')||(LA17_0 >= '\uF900' && LA17_0 <= '\uFDCF')||(LA17_0 >= '\uFDF0' && LA17_0 <= '\uFFFD')) ) {
					alt17=1;
				}

				switch (alt17) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
					{
					if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= '9')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
					break loop17;
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
	// $ANTLR end "NCNAME"

	// $ANTLR start "NAMESPACE"
	public final void mNAMESPACE() throws RecognitionException {
		try {
			int _type = NAMESPACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:701:3: ( NAME_START_CHAR ( NAME_CHAR )* COLON )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:701:5: NAME_START_CHAR ( NAME_CHAR )* COLON
			{
			mNAME_START_CHAR(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:701:21: ( NAME_CHAR )*
			loop18:
			while (true) {
				int alt18=2;
				int LA18_0 = input.LA(1);
				if ( (LA18_0=='#'||(LA18_0 >= '-' && LA18_0 <= '9')||LA18_0=='?'||(LA18_0 >= 'A' && LA18_0 <= 'Z')||LA18_0=='_'||(LA18_0 >= 'a' && LA18_0 <= 'z')||(LA18_0 >= '\u00C0' && LA18_0 <= '\u00D6')||(LA18_0 >= '\u00D8' && LA18_0 <= '\u00F6')||(LA18_0 >= '\u00F8' && LA18_0 <= '\u02FF')||(LA18_0 >= '\u0370' && LA18_0 <= '\u037D')||(LA18_0 >= '\u037F' && LA18_0 <= '\u1FFF')||(LA18_0 >= '\u200C' && LA18_0 <= '\u200D')||(LA18_0 >= '\u2070' && LA18_0 <= '\u218F')||(LA18_0 >= '\u2C00' && LA18_0 <= '\u2FEF')||(LA18_0 >= '\u3001' && LA18_0 <= '\uD7FF')||(LA18_0 >= '\uF900' && LA18_0 <= '\uFDCF')||(LA18_0 >= '\uFDF0' && LA18_0 <= '\uFFFD')) ) {
					alt18=1;
				}

				switch (alt18) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
					{
					if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= '9')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
					break loop18;
				}
			}

			mCOLON(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NAMESPACE"

	// $ANTLR start "PREFIXED_NAME"
	public final void mPREFIXED_NAME() throws RecognitionException {
		try {
			int _type = PREFIXED_NAME;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:705:3: ( ( NCNAME )? COLON NCNAME )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:705:5: ( NCNAME )? COLON NCNAME
			{
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:705:5: ( NCNAME )?
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( ((LA19_0 >= 'A' && LA19_0 <= 'Z')||LA19_0=='_'||(LA19_0 >= 'a' && LA19_0 <= 'z')||(LA19_0 >= '\u00C0' && LA19_0 <= '\u00D6')||(LA19_0 >= '\u00D8' && LA19_0 <= '\u00F6')||(LA19_0 >= '\u00F8' && LA19_0 <= '\u02FF')||(LA19_0 >= '\u0370' && LA19_0 <= '\u037D')||(LA19_0 >= '\u037F' && LA19_0 <= '\u1FFF')||(LA19_0 >= '\u200C' && LA19_0 <= '\u200D')||(LA19_0 >= '\u2070' && LA19_0 <= '\u218F')||(LA19_0 >= '\u2C00' && LA19_0 <= '\u2FEF')||(LA19_0 >= '\u3001' && LA19_0 <= '\uD7FF')||(LA19_0 >= '\uF900' && LA19_0 <= '\uFDCF')||(LA19_0 >= '\uFDF0' && LA19_0 <= '\uFFFD')) ) {
				alt19=1;
			}
			switch (alt19) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:705:5: NCNAME
					{
					mNCNAME(); 

					}
					break;

			}

			mCOLON(); 

			mNCNAME(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PREFIXED_NAME"

	// $ANTLR start "STRING_WITH_QUOTE"
	public final void mSTRING_WITH_QUOTE() throws RecognitionException {
		try {
			int _type = STRING_WITH_QUOTE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:709:3: ( '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\'' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:709:5: '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\''
			{
			match('\''); 
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:709:10: ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop20:
			while (true) {
				int alt20=3;
				int LA20_0 = input.LA(1);
				if ( ((LA20_0 >= '\u0000' && LA20_0 <= '\t')||(LA20_0 >= '\u000B' && LA20_0 <= '\f')||(LA20_0 >= '\u000E' && LA20_0 <= '&')||(LA20_0 >= '(' && LA20_0 <= '[')||(LA20_0 >= ']' && LA20_0 <= '\uFFFF')) ) {
					alt20=1;
				}
				else if ( (LA20_0=='\\') ) {
					alt20=2;
				}
				else if ( (LA20_0=='\'') ) {
					alt20=3;
				}

				switch (alt20) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:709:40: ~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:709:87: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop20;
				}
			}

			match('\''); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_WITH_QUOTE"

	// $ANTLR start "STRING_WITH_QUOTE_DOUBLE"
	public final void mSTRING_WITH_QUOTE_DOUBLE() throws RecognitionException {
		try {
			int _type = STRING_WITH_QUOTE_DOUBLE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:713:3: ( '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:713:5: '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"'
			{
			match('\"'); 
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:713:10: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop21:
			while (true) {
				int alt21=3;
				int LA21_0 = input.LA(1);
				if ( ((LA21_0 >= '\u0000' && LA21_0 <= '\t')||(LA21_0 >= '\u000B' && LA21_0 <= '\f')||(LA21_0 >= '\u000E' && LA21_0 <= '!')||(LA21_0 >= '#' && LA21_0 <= '[')||(LA21_0 >= ']' && LA21_0 <= '\uFFFF')) ) {
					alt21=1;
				}
				else if ( (LA21_0=='\\') ) {
					alt21=2;
				}
				else if ( (LA21_0=='\"') ) {
					alt21=3;
				}

				switch (alt21) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:713:40: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:713:87: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop21;
				}
			}

			match('\"'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_WITH_QUOTE_DOUBLE"

	// $ANTLR start "STRING_WITH_TEMPLATE_SIGN"
	public final void mSTRING_WITH_TEMPLATE_SIGN() throws RecognitionException {
		try {
			int _type = STRING_WITH_TEMPLATE_SIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:717:3: ( '<\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\">' )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:717:5: '<\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\">'
			{
			match("<\""); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:717:11: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop22:
			while (true) {
				int alt22=3;
				int LA22_0 = input.LA(1);
				if ( ((LA22_0 >= '\u0000' && LA22_0 <= '\t')||(LA22_0 >= '\u000B' && LA22_0 <= '\f')||(LA22_0 >= '\u000E' && LA22_0 <= '!')||(LA22_0 >= '#' && LA22_0 <= '[')||(LA22_0 >= ']' && LA22_0 <= '\uFFFF')) ) {
					alt22=1;
				}
				else if ( (LA22_0=='\\') ) {
					alt22=2;
				}
				else if ( (LA22_0=='\"') ) {
					alt22=3;
				}

				switch (alt22) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:717:41: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:717:88: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop22;
				}
			}

			match("\">"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_WITH_TEMPLATE_SIGN"

	// $ANTLR start "STRING_URI"
	public final void mSTRING_URI() throws RecognitionException {
		try {
			int _type = STRING_URI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:721:3: ( SCHEMA COLON DOUBLE_SLASH ( URI_PATH )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:721:5: SCHEMA COLON DOUBLE_SLASH ( URI_PATH )*
			{
			mSCHEMA(); 

			mCOLON(); 

			mDOUBLE_SLASH(); 

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:721:31: ( URI_PATH )*
			loop23:
			while (true) {
				int alt23=2;
				int LA23_0 = input.LA(1);
				if ( (LA23_0=='#'||(LA23_0 >= '-' && LA23_0 <= ':')||LA23_0=='?'||(LA23_0 >= 'A' && LA23_0 <= 'Z')||LA23_0=='_'||(LA23_0 >= 'a' && LA23_0 <= 'z')||(LA23_0 >= '\u00C0' && LA23_0 <= '\u00D6')||(LA23_0 >= '\u00D8' && LA23_0 <= '\u00F6')||(LA23_0 >= '\u00F8' && LA23_0 <= '\u02FF')||(LA23_0 >= '\u0370' && LA23_0 <= '\u037D')||(LA23_0 >= '\u037F' && LA23_0 <= '\u1FFF')||(LA23_0 >= '\u200C' && LA23_0 <= '\u200D')||(LA23_0 >= '\u2070' && LA23_0 <= '\u218F')||(LA23_0 >= '\u2C00' && LA23_0 <= '\u2FEF')||(LA23_0 >= '\u3001' && LA23_0 <= '\uD7FF')||(LA23_0 >= '\uF900' && LA23_0 <= '\uFDCF')||(LA23_0 >= '\uFDF0' && LA23_0 <= '\uFFFD')) ) {
					alt23=1;
				}

				switch (alt23) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:
					{
					if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= ':')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
					break loop23;
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

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			{
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			int cnt25=0;
			loop25:
			while (true) {
				int alt25=4;
				switch ( input.LA(1) ) {
				case ' ':
					{
					alt25=1;
					}
					break;
				case '\t':
					{
					alt25=2;
					}
					break;
				case '\n':
				case '\r':
					{
					alt25=3;
					}
					break;
				}
				switch (alt25) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:6: ' '
					{
					match(' '); 
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:10: '\\t'
					{
					match('\t'); 
					}
					break;
				case 3 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:15: ( '\\n' | '\\r' ( '\\n' ) )
					{
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:15: ( '\\n' | '\\r' ( '\\n' ) )
					int alt24=2;
					int LA24_0 = input.LA(1);
					if ( (LA24_0=='\n') ) {
						alt24=1;
					}
					else if ( (LA24_0=='\r') ) {
						alt24=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 24, 0, input);
						throw nvae;
					}

					switch (alt24) {
						case 1 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:16: '\\n'
							{
							match('\n'); 
							}
							break;
						case 2 :
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:21: '\\r' ( '\\n' )
							{
							match('\r'); 
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:25: ( '\\n' )
							// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:724:26: '\\n'
							{
							match('\n'); 
							}

							}
							break;

					}

					}
					break;

				default :
					if ( cnt25 >= 1 ) break loop25;
					EarlyExitException eee = new EarlyExitException(25, input);
					throw eee;
				}
				cnt25++;
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
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:8: ( T__75 | BASE | PREFIX | FALSE | TRUE | REFERENCE | LTSIGN | RTSIGN | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LCR_BRACKET | RCR_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | NCNAME | NAMESPACE | PREFIXED_NAME | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_WITH_TEMPLATE_SIGN | STRING_URI | WS )
		int alt26=60;
		alt26 = dfa26.predict(input);
		switch (alt26) {
			case 1 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:10: T__75
				{
				mT__75(); 

				}
				break;
			case 2 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:16: BASE
				{
				mBASE(); 

				}
				break;
			case 3 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:21: PREFIX
				{
				mPREFIX(); 

				}
				break;
			case 4 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:28: FALSE
				{
				mFALSE(); 

				}
				break;
			case 5 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:34: TRUE
				{
				mTRUE(); 

				}
				break;
			case 6 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:39: REFERENCE
				{
				mREFERENCE(); 

				}
				break;
			case 7 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:49: LTSIGN
				{
				mLTSIGN(); 

				}
				break;
			case 8 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:56: RTSIGN
				{
				mRTSIGN(); 

				}
				break;
			case 9 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:63: SEMI
				{
				mSEMI(); 

				}
				break;
			case 10 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:68: PERIOD
				{
				mPERIOD(); 

				}
				break;
			case 11 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:75: COMMA
				{
				mCOMMA(); 

				}
				break;
			case 12 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:81: LSQ_BRACKET
				{
				mLSQ_BRACKET(); 

				}
				break;
			case 13 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:93: RSQ_BRACKET
				{
				mRSQ_BRACKET(); 

				}
				break;
			case 14 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:105: LCR_BRACKET
				{
				mLCR_BRACKET(); 

				}
				break;
			case 15 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:117: RCR_BRACKET
				{
				mRCR_BRACKET(); 

				}
				break;
			case 16 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:129: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 17 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:136: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 18 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:143: QUESTION
				{
				mQUESTION(); 

				}
				break;
			case 19 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:152: DOLLAR
				{
				mDOLLAR(); 

				}
				break;
			case 20 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:159: QUOTE_DOUBLE
				{
				mQUOTE_DOUBLE(); 

				}
				break;
			case 21 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:172: QUOTE_SINGLE
				{
				mQUOTE_SINGLE(); 

				}
				break;
			case 22 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:185: APOSTROPHE
				{
				mAPOSTROPHE(); 

				}
				break;
			case 23 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:196: UNDERSCORE
				{
				mUNDERSCORE(); 

				}
				break;
			case 24 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:207: MINUS
				{
				mMINUS(); 

				}
				break;
			case 25 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:213: ASTERISK
				{
				mASTERISK(); 

				}
				break;
			case 26 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:222: AMPERSAND
				{
				mAMPERSAND(); 

				}
				break;
			case 27 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:232: AT
				{
				mAT(); 

				}
				break;
			case 28 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:235: EXCLAMATION
				{
				mEXCLAMATION(); 

				}
				break;
			case 29 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:247: HASH
				{
				mHASH(); 

				}
				break;
			case 30 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:252: PERCENT
				{
				mPERCENT(); 

				}
				break;
			case 31 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:260: PLUS
				{
				mPLUS(); 

				}
				break;
			case 32 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:265: EQUALS
				{
				mEQUALS(); 

				}
				break;
			case 33 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:272: COLON
				{
				mCOLON(); 

				}
				break;
			case 34 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:278: LESS
				{
				mLESS(); 

				}
				break;
			case 35 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:283: GREATER
				{
				mGREATER(); 

				}
				break;
			case 36 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:291: SLASH
				{
				mSLASH(); 

				}
				break;
			case 37 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:297: DOUBLE_SLASH
				{
				mDOUBLE_SLASH(); 

				}
				break;
			case 38 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:310: BACKSLASH
				{
				mBACKSLASH(); 

				}
				break;
			case 39 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:320: BLANK
				{
				mBLANK(); 

				}
				break;
			case 40 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:326: BLANK_PREFIX
				{
				mBLANK_PREFIX(); 

				}
				break;
			case 41 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:339: TILDE
				{
				mTILDE(); 

				}
				break;
			case 42 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:345: CARET
				{
				mCARET(); 

				}
				break;
			case 43 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:351: INTEGER
				{
				mINTEGER(); 

				}
				break;
			case 44 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:359: DOUBLE
				{
				mDOUBLE(); 

				}
				break;
			case 45 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:366: DECIMAL
				{
				mDECIMAL(); 

				}
				break;
			case 46 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:374: INTEGER_POSITIVE
				{
				mINTEGER_POSITIVE(); 

				}
				break;
			case 47 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:391: INTEGER_NEGATIVE
				{
				mINTEGER_NEGATIVE(); 

				}
				break;
			case 48 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:408: DOUBLE_POSITIVE
				{
				mDOUBLE_POSITIVE(); 

				}
				break;
			case 49 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:424: DOUBLE_NEGATIVE
				{
				mDOUBLE_NEGATIVE(); 

				}
				break;
			case 50 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:440: DECIMAL_POSITIVE
				{
				mDECIMAL_POSITIVE(); 

				}
				break;
			case 51 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:457: DECIMAL_NEGATIVE
				{
				mDECIMAL_NEGATIVE(); 

				}
				break;
			case 52 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:474: VARNAME
				{
				mVARNAME(); 

				}
				break;
			case 53 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:482: NCNAME
				{
				mNCNAME(); 

				}
				break;
			case 54 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:489: NAMESPACE
				{
				mNAMESPACE(); 

				}
				break;
			case 55 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:499: PREFIXED_NAME
				{
				mPREFIXED_NAME(); 

				}
				break;
			case 56 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:513: STRING_WITH_QUOTE
				{
				mSTRING_WITH_QUOTE(); 

				}
				break;
			case 57 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:531: STRING_WITH_QUOTE_DOUBLE
				{
				mSTRING_WITH_QUOTE_DOUBLE(); 

				}
				break;
			case 58 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:556: STRING_WITH_TEMPLATE_SIGN
				{
				mSTRING_WITH_TEMPLATE_SIGN(); 

				}
				break;
			case 59 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:582: STRING_URI
				{
				mSTRING_URI(); 

				}
				break;
			case 60 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Turtle.g:1:593: WS
				{
				mWS(); 

				}
				break;

		}
	}


	protected DFA9 dfa9 = new DFA9(this);
	protected DFA26 dfa26 = new DFA26(this);
	static final String DFA9_eotS =
		"\5\uffff";
	static final String DFA9_eofS =
		"\5\uffff";
	static final String DFA9_minS =
		"\2\56\3\uffff";
	static final String DFA9_maxS =
		"\1\71\1\145\3\uffff";
	static final String DFA9_acceptS =
		"\2\uffff\1\2\1\1\1\3";
	static final String DFA9_specialS =
		"\5\uffff}>";
	static final String[] DFA9_transitionS = {
			"\1\2\1\uffff\12\1",
			"\1\3\1\uffff\12\1\13\uffff\1\4\37\uffff\1\4",
			"",
			"",
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
			return "640:1: DOUBLE : ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? );";
		}
	}

	static final String DFA26_eotS =
		"\1\uffff\1\50\4\57\1\64\1\66\1\70\1\uffff\1\72\1\uffff\1\75\7\uffff\1"+
		"\76\1\uffff\1\101\1\102\6\uffff\1\105\1\uffff\1\110\1\uffff\1\113\2\uffff"+
		"\1\114\1\57\2\uffff\1\57\1\117\1\121\1\57\1\uffff\1\57\1\uffff\3\57\2"+
		"\uffff\1\126\1\uffff\1\130\3\uffff\1\131\4\uffff\1\132\2\uffff\1\133\2"+
		"\uffff\1\137\11\uffff\1\121\1\uffff\4\57\10\uffff\1\151\3\uffff\1\153"+
		"\1\131\1\154\2\57\1\157\1\151\1\uffff\1\153\2\uffff\1\57\1\161\1\uffff"+
		"\1\162\2\uffff";
	static final String DFA26_eofS =
		"\163\uffff";
	static final String DFA26_minS =
		"\1\11\5\43\1\136\1\42\1\0\1\uffff\1\60\1\uffff\1\135\7\uffff\1\0\1\uffff"+
		"\1\43\1\56\6\uffff\1\56\1\uffff\1\101\1\uffff\1\57\2\uffff\1\56\1\43\2"+
		"\uffff\2\43\1\57\1\43\1\uffff\1\43\1\uffff\3\43\2\uffff\1\0\1\uffff\1"+
		"\0\3\uffff\1\60\4\uffff\1\101\2\uffff\1\56\1\60\1\uffff\1\56\1\60\5\uffff"+
		"\1\60\2\uffff\1\101\1\uffff\4\43\6\uffff\1\60\1\uffff\1\60\1\uffff\1\60"+
		"\1\uffff\2\60\4\43\1\60\1\uffff\1\60\2\uffff\2\43\1\uffff\1\43\2\uffff";
	static final String DFA26_maxS =
		"\6\ufffd\1\136\1\42\1\uffff\1\uffff\1\71\1\uffff\1\135\7\uffff\1\uffff"+
		"\1\uffff\1\ufffd\1\71\6\uffff\1\71\1\uffff\1\ufffd\1\uffff\1\57\2\uffff"+
		"\1\145\1\ufffd\2\uffff\4\ufffd\1\uffff\1\ufffd\1\uffff\3\ufffd\2\uffff"+
		"\1\uffff\1\uffff\1\uffff\3\uffff\1\145\4\uffff\1\ufffd\2\uffff\1\145\1"+
		"\71\1\uffff\1\145\1\71\5\uffff\1\145\2\uffff\1\ufffd\1\uffff\4\ufffd\6"+
		"\uffff\1\145\1\uffff\1\145\1\uffff\1\145\1\uffff\2\145\4\ufffd\1\145\1"+
		"\uffff\1\145\2\uffff\2\ufffd\1\uffff\1\ufffd\2\uffff";
	static final String DFA26_acceptS =
		"\11\uffff\1\11\1\uffff\1\13\1\uffff\1\15\1\16\1\17\1\20\1\21\1\22\1\23"+
		"\1\uffff\1\26\2\uffff\1\31\1\32\1\33\1\34\1\35\1\36\1\uffff\1\40\1\uffff"+
		"\1\43\1\uffff\1\46\1\51\2\uffff\1\74\1\1\4\uffff\1\73\1\uffff\1\64\3\uffff"+
		"\1\6\1\52\1\uffff\1\42\1\uffff\1\24\1\71\1\12\1\uffff\1\47\1\14\1\25\1"+
		"\70\1\uffff\1\27\1\30\2\uffff\1\37\2\uffff\1\41\1\67\1\45\1\44\1\53\1"+
		"\uffff\1\54\1\65\1\uffff\1\66\4\uffff\1\7\1\72\1\10\1\55\1\50\1\57\1\uffff"+
		"\1\61\1\uffff\1\56\1\uffff\1\60\7\uffff\1\63\1\uffff\1\62\1\2\2\uffff"+
		"\1\5\1\uffff\1\4\1\3";
	static final String DFA26_specialS =
		"\10\uffff\1\0\13\uffff\1\1\40\uffff\1\3\1\uffff\1\2\73\uffff}>";
	static final String[] DFA26_transitionS = {
			"\2\47\2\uffff\1\47\22\uffff\1\47\1\33\1\10\1\34\1\23\1\35\1\31\1\24\1"+
			"\20\1\21\1\30\1\36\1\13\1\27\1\12\1\42\12\45\1\40\1\11\1\7\1\37\1\41"+
			"\1\22\1\32\1\46\1\2\3\46\1\4\11\46\1\3\3\46\1\5\6\46\1\14\1\43\1\15\1"+
			"\6\1\26\1\25\1\1\1\2\3\46\1\4\11\46\1\3\3\46\1\5\6\46\1\16\1\uffff\1"+
			"\17\1\44\101\uffff\27\46\1\uffff\37\46\1\uffff\u0208\46\160\uffff\16"+
			"\46\1\uffff\u1c81\46\14\uffff\2\46\142\uffff\u0120\46\u0a70\uffff\u03f0"+
			"\46\21\uffff\ua7ff\46\u2100\uffff\u04d0\46\40\uffff\u020e\46",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\32\51\4\uffff\1\54\1\uffff\32\51\105\uffff\27\51\1\uffff\37\51\1\uffff"+
			"\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142\uffff\u0120"+
			"\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0\51\40\uffff"+
			"\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\1\56\31\51\4\uffff\1\54\1\uffff\1\56\31\51\105\uffff\27\51\1\uffff\37"+
			"\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142"+
			"\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0"+
			"\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\21\51\1\60\10\51\4\uffff\1\54\1\uffff\21\51\1\60\10\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\1\61\31\51\4\uffff\1\54\1\uffff\1\61\31\51\105\uffff\27\51\1\uffff\37"+
			"\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142"+
			"\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0"+
			"\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\21\51\1\62\10\51\4\uffff\1\54\1\uffff\21\51\1\62\10\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\63",
			"\1\65",
			"\12\71\1\uffff\2\71\1\uffff\60\71\1\67\uffc1\71",
			"",
			"\12\73",
			"",
			"\1\74",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\12\77\1\uffff\2\77\1\uffff\ufff2\77",
			"",
			"\1\52\11\uffff\15\52\1\100\4\uffff\1\52\1\uffff\32\52\4\uffff\1\52\1"+
			"\uffff\32\52\105\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\104\1\uffff\12\103",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\107\1\uffff\12\106",
			"",
			"\32\111\4\uffff\1\111\1\uffff\32\111\105\uffff\27\111\1\uffff\37\111"+
			"\1\uffff\u0208\111\160\uffff\16\111\1\uffff\u1c81\111\14\uffff\2\111"+
			"\142\uffff\u0120\111\u0a70\uffff\u03f0\111\21\uffff\ua7ff\111\u2100\uffff"+
			"\u04d0\111\40\uffff\u020e\111",
			"",
			"\1\112",
			"",
			"",
			"\1\115\1\uffff\12\45\13\uffff\1\116\37\uffff\1\116",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\32\51\4\uffff\1\54\1\uffff\32\51\105\uffff\27\51\1\uffff\37\51\1\uffff"+
			"\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142\uffff\u0120"+
			"\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0\51\40\uffff"+
			"\u020e\51",
			"",
			"",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\32\51\4\uffff\1\54\1\uffff\32\51\105\uffff\27\51\1\uffff\37\51\1\uffff"+
			"\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142\uffff\u0120"+
			"\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0\51\40\uffff"+
			"\u020e\51",
			"\1\52\11\uffff\15\52\1\120\4\uffff\1\52\1\uffff\32\52\4\uffff\1\52\1"+
			"\uffff\32\52\105\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\55\21\uffff\32\111\4\uffff\1\111\1\uffff\32\111\105\uffff\27\111"+
			"\1\uffff\37\111\1\uffff\u0208\111\160\uffff\16\111\1\uffff\u1c81\111"+
			"\14\uffff\2\111\142\uffff\u0120\111\u0a70\uffff\u03f0\111\21\uffff\ua7ff"+
			"\111\u2100\uffff\u04d0\111\40\uffff\u020e\111",
			"\1\52\11\uffff\2\54\1\52\12\54\1\120\4\uffff\1\52\1\uffff\32\54\4\uffff"+
			"\1\54\1\uffff\32\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u0208\54\160"+
			"\uffff\16\54\1\uffff\u1c81\54\14\uffff\2\54\142\uffff\u0120\54\u0a70"+
			"\uffff\u03f0\54\21\uffff\ua7ff\54\u2100\uffff\u04d0\54\40\uffff\u020e"+
			"\54",
			"",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\22\51\1\122\7\51\4\uffff\1\54\1\uffff\22\51\1\122\7\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\4\51\1\123\25\51\4\uffff\1\54\1\uffff\4\51\1\123\25\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\13\51\1\124\16\51\4\uffff\1\54\1\uffff\13\51\1\124\16\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\24\51\1\125\5\51\4\uffff\1\54\1\uffff\24\51\1\125\5\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"",
			"",
			"\12\127\1\uffff\2\127\1\uffff\ufff2\127",
			"",
			"\12\71\1\uffff\2\71\1\uffff\ufff2\71",
			"",
			"",
			"",
			"\12\73\13\uffff\1\116\37\uffff\1\116",
			"",
			"",
			"",
			"",
			"\32\111\4\uffff\1\111\1\uffff\32\111\105\uffff\27\111\1\uffff\37\111"+
			"\1\uffff\u0208\111\160\uffff\16\111\1\uffff\u1c81\111\14\uffff\2\111"+
			"\142\uffff\u0120\111\u0a70\uffff\u03f0\111\21\uffff\ua7ff\111\u2100\uffff"+
			"\u04d0\111\40\uffff\u020e\111",
			"",
			"",
			"\1\134\1\uffff\12\103\13\uffff\1\135\37\uffff\1\135",
			"\12\136",
			"",
			"\1\140\1\uffff\12\106\13\uffff\1\141\37\uffff\1\141",
			"\12\142",
			"",
			"",
			"",
			"",
			"",
			"\12\143\13\uffff\1\116\37\uffff\1\116",
			"",
			"",
			"\32\111\4\uffff\1\111\1\uffff\32\111\105\uffff\27\111\1\uffff\37\111"+
			"\1\uffff\u0208\111\160\uffff\16\111\1\uffff\u1c81\111\14\uffff\2\111"+
			"\142\uffff\u0120\111\u0a70\uffff\u03f0\111\21\uffff\ua7ff\111\u2100\uffff"+
			"\u04d0\111\40\uffff\u020e\111",
			"",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\4\51\1\144\25\51\4\uffff\1\54\1\uffff\4\51\1\144\25\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\5\51\1\145\24\51\4\uffff\1\54\1\uffff\5\51\1\145\24\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\22\51\1\146\7\51\4\uffff\1\54\1\uffff\22\51\1\146\7\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\4\51\1\147\25\51\4\uffff\1\54\1\uffff\4\51\1\147\25\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"",
			"",
			"",
			"",
			"",
			"",
			"\12\150\13\uffff\1\135\37\uffff\1\135",
			"",
			"\12\136\13\uffff\1\135\37\uffff\1\135",
			"",
			"\12\152\13\uffff\1\141\37\uffff\1\141",
			"",
			"\12\142\13\uffff\1\141\37\uffff\1\141",
			"\12\143\13\uffff\1\116\37\uffff\1\116",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\32\51\4\uffff\1\54\1\uffff\32\51\105\uffff\27\51\1\uffff\37\51\1\uffff"+
			"\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142\uffff\u0120"+
			"\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0\51\40\uffff"+
			"\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\10\51\1\155\21\51\4\uffff\1\54\1\uffff\10\51\1\155\21\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\4\51\1\156\25\51\4\uffff\1\54\1\uffff\4\51\1\156\25\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\32\51\4\uffff\1\54\1\uffff\32\51\105\uffff\27\51\1\uffff\37\51\1\uffff"+
			"\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142\uffff\u0120"+
			"\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0\51\40\uffff"+
			"\u020e\51",
			"\12\150\13\uffff\1\135\37\uffff\1\135",
			"",
			"\12\152\13\uffff\1\141\37\uffff\1\141",
			"",
			"",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\27\51\1\160\2\51\4\uffff\1\54\1\uffff\27\51\1\160\2\51\105\uffff\27"+
			"\51\1\uffff\37\51\1\uffff\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14"+
			"\uffff\2\51\142\uffff\u0120\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51"+
			"\u2100\uffff\u04d0\51\40\uffff\u020e\51",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\32\51\4\uffff\1\54\1\uffff\32\51\105\uffff\27\51\1\uffff\37\51\1\uffff"+
			"\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142\uffff\u0120"+
			"\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0\51\40\uffff"+
			"\u020e\51",
			"",
			"\1\52\7\uffff\1\55\1\uffff\2\51\1\52\12\51\1\53\4\uffff\1\52\1\uffff"+
			"\32\51\4\uffff\1\54\1\uffff\32\51\105\uffff\27\51\1\uffff\37\51\1\uffff"+
			"\u0208\51\160\uffff\16\51\1\uffff\u1c81\51\14\uffff\2\51\142\uffff\u0120"+
			"\51\u0a70\uffff\u03f0\51\21\uffff\ua7ff\51\u2100\uffff\u04d0\51\40\uffff"+
			"\u020e\51",
			"",
			""
	};

	static final short[] DFA26_eot = DFA.unpackEncodedString(DFA26_eotS);
	static final short[] DFA26_eof = DFA.unpackEncodedString(DFA26_eofS);
	static final char[] DFA26_min = DFA.unpackEncodedStringToUnsignedChars(DFA26_minS);
	static final char[] DFA26_max = DFA.unpackEncodedStringToUnsignedChars(DFA26_maxS);
	static final short[] DFA26_accept = DFA.unpackEncodedString(DFA26_acceptS);
	static final short[] DFA26_special = DFA.unpackEncodedString(DFA26_specialS);
	static final short[][] DFA26_transition;

	static {
		int numStates = DFA26_transitionS.length;
		DFA26_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA26_transition[i] = DFA.unpackEncodedString(DFA26_transitionS[i]);
		}
	}

	protected class DFA26 extends DFA {

		public DFA26(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 26;
			this.eot = DFA26_eot;
			this.eof = DFA26_eof;
			this.min = DFA26_min;
			this.max = DFA26_max;
			this.accept = DFA26_accept;
			this.special = DFA26_special;
			this.transition = DFA26_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__75 | BASE | PREFIX | FALSE | TRUE | REFERENCE | LTSIGN | RTSIGN | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LCR_BRACKET | RCR_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | NCNAME | NAMESPACE | PREFIXED_NAME | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_WITH_TEMPLATE_SIGN | STRING_URI | WS );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA26_8 = input.LA(1);
						s = -1;
						if ( (LA26_8=='>') ) {s = 55;}
						else if ( ((LA26_8 >= '\u0000' && LA26_8 <= '\t')||(LA26_8 >= '\u000B' && LA26_8 <= '\f')||(LA26_8 >= '\u000E' && LA26_8 <= '=')||(LA26_8 >= '?' && LA26_8 <= '\uFFFF')) ) {s = 57;}
						else s = 56;
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA26_20 = input.LA(1);
						s = -1;
						if ( ((LA26_20 >= '\u0000' && LA26_20 <= '\t')||(LA26_20 >= '\u000B' && LA26_20 <= '\f')||(LA26_20 >= '\u000E' && LA26_20 <= '\uFFFF')) ) {s = 63;}
						else s = 62;
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA26_55 = input.LA(1);
						s = -1;
						if ( ((LA26_55 >= '\u0000' && LA26_55 <= '\t')||(LA26_55 >= '\u000B' && LA26_55 <= '\f')||(LA26_55 >= '\u000E' && LA26_55 <= '\uFFFF')) ) {s = 57;}
						else s = 88;
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA26_53 = input.LA(1);
						s = -1;
						if ( ((LA26_53 >= '\u0000' && LA26_53 <= '\t')||(LA26_53 >= '\u000B' && LA26_53 <= '\f')||(LA26_53 >= '\u000E' && LA26_53 <= '\uFFFF')) ) {s = 87;}
						else s = 86;
						if ( s>=0 ) return s;
						break;
			}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 26, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
