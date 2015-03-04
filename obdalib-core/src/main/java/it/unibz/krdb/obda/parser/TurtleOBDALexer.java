// $ANTLR 3.5.1 /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g 2015-03-04 16:46:21

package it.unibz.krdb.obda.parser;

import java.util.List;
import java.util.Vector;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class TurtleOBDALexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__77=77;
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
	public static final int NCNAME_EXT=49;
	public static final int PERCENT=50;
	public static final int PERIOD=51;
	public static final int PLUS=52;
	public static final int PREFIX=53;
	public static final int PREFIXED_NAME=54;
	public static final int QUESTION=55;
	public static final int QUOTE_DOUBLE=56;
	public static final int QUOTE_SINGLE=57;
	public static final int RCR_BRACKET=58;
	public static final int REFERENCE=59;
	public static final int RPAREN=60;
	public static final int RSQ_BRACKET=61;
	public static final int RTSIGN=62;
	public static final int SCHEMA=63;
	public static final int SEMI=64;
	public static final int SLASH=65;
	public static final int STRING_URI=66;
	public static final int STRING_WITH_BRACKET=67;
	public static final int STRING_WITH_CURLY_BRACKET=68;
	public static final int STRING_WITH_QUOTE=69;
	public static final int STRING_WITH_QUOTE_DOUBLE=70;
	public static final int TILDE=71;
	public static final int TRUE=72;
	public static final int UNDERSCORE=73;
	public static final int URI_PATH=74;
	public static final int VARNAME=75;
	public static final int WS=76;

	private String error = "";
	    
	public String getError() {
	   return error;
	}

	@Override
	public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
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
	public void emitErrorMessage(String msg) {
	   error = msg;
	   throw new RuntimeException(error);
	}
	    
	@Override
	public Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
	   throw new RecognitionException(input);
	}


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public TurtleOBDALexer() {} 
	public TurtleOBDALexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public TurtleOBDALexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g"; }

	// $ANTLR start "T__77"
	public final void mT__77() throws RecognitionException {
		try {
			int _type = T__77;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:44:7: ( 'a' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:44:9: 'a'
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
	// $ANTLR end "T__77"

	// $ANTLR start "BASE"
	public final void mBASE() throws RecognitionException {
		try {
			int _type = BASE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:683:5: ( ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:683:7: ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:685:7: ( ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:685:9: ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' )
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:687:6: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:687:8: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:689:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:689:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:691:10: ( '^^' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:691:16: '^^'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:692:7: ( '<\"' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:692:16: '<\"'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:693:7: ( '\">' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:693:16: '\">'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:694:5: ( ';' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:694:16: ';'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:695:7: ( '.' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:695:16: '.'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:696:6: ( ',' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:696:16: ','
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:697:12: ( '[' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:697:16: '['
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:698:12: ( ']' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:698:16: ']'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:699:12: ( '{' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:699:16: '{'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:700:12: ( '}' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:700:16: '}'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:701:7: ( '(' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:701:16: '('
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:702:7: ( ')' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:702:16: ')'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:703:9: ( '?' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:703:16: '?'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:704:7: ( '$' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:704:16: '$'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:705:13: ( '\"' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:705:16: '\"'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:706:13: ( '\\'' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:706:16: '\\''
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:707:11: ( '`' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:707:16: '`'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:708:11: ( '_' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:708:16: '_'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:709:6: ( '-' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:709:16: '-'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:710:9: ( '*' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:710:16: '*'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:711:10: ( '&' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:711:16: '&'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:712:3: ( '@' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:712:16: '@'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:713:12: ( '!' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:713:16: '!'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:714:5: ( '#' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:714:16: '#'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:715:8: ( '%' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:715:16: '%'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:716:5: ( '+' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:716:16: '+'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:717:7: ( '=' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:717:16: '='
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:718:6: ( ':' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:718:16: ':'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:719:5: ( '<' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:719:16: '<'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:720:8: ( '>' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:720:16: '>'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:721:6: ( '/' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:721:16: '/'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:722:13: ( '//' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:722:16: '//'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:723:10: ( '\\\\' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:723:16: '\\\\'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:724:6: ( '[]' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:724:15: '[]'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:725:13: ( '_:' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:725:16: '_:'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:726:6: ( '~' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:726:16: '~'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:727:6: ( '^' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:727:16: '^'
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:730:3: ( 'a' .. 'z' | 'A' .. 'Z' | '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u02FF' | '\\u0370' .. '\\u037D' | '\\u037F' .. '\\u1FFF' | '\\u200C' .. '\\u200D' | '\\u2070' .. '\\u218F' | '\\u2C00' .. '\\u2FEF' | '\\u3001' .. '\\uD7FF' | '\\uF900' .. '\\uFDCF' | '\\uFDF0' .. '\\uFFFD' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:746:3: ( '0' .. '9' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:750:3: ( ALPHA | DIGIT )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:755:3: ( ALPHANUM | UNDERSCORE | MINUS | PERIOD )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:762:3: ( ( DIGIT )+ )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:762:5: ( DIGIT )+
			{
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:762:5: ( DIGIT )+
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:766:3: ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? )
			int alt9=3;
			alt9 = dfa9.predict(input);
			switch (alt9) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:766:5: ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )?
					{
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:766:5: ( DIGIT )+
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
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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

					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:766:19: ( DIGIT )*
					loop3:
					while (true) {
						int alt3=2;
						int LA3_0 = input.LA(1);
						if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
							alt3=1;
						}

						switch (alt3) {
						case 1 :
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:766:36: ( '-' | '+' )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0=='+'||LA4_0=='-') ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:767:5: PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
					{
					mPERIOD(); 

					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:767:12: ( DIGIT )+
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
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:767:29: ( '-' | '+' )?
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0=='+'||LA6_0=='-') ) {
						alt6=1;
					}
					switch (alt6) {
						case 1 :
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:768:5: ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
					{
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:768:5: ( DIGIT )+
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
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:768:22: ( '-' | '+' )?
					int alt8=2;
					int LA8_0 = input.LA(1);
					if ( (LA8_0=='+'||LA8_0=='-') ) {
						alt8=1;
					}
					switch (alt8) {
						case 1 :
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:772:3: ( ( DIGIT )+ PERIOD ( DIGIT )+ | PERIOD ( DIGIT )+ )
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:772:5: ( DIGIT )+ PERIOD ( DIGIT )+
					{
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:772:5: ( DIGIT )+
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
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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

					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:772:19: ( DIGIT )+
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
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:773:5: PERIOD ( DIGIT )+
					{
					mPERIOD(); 

					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:773:12: ( DIGIT )+
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
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:777:3: ( PLUS INTEGER )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:777:5: PLUS INTEGER
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:781:3: ( MINUS INTEGER )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:781:5: MINUS INTEGER
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:785:3: ( PLUS DOUBLE )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:785:5: PLUS DOUBLE
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:789:3: ( MINUS DOUBLE )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:789:5: MINUS DOUBLE
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:793:3: ( PLUS DECIMAL )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:793:5: PLUS DECIMAL
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:797:3: ( MINUS DECIMAL )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:797:5: MINUS DECIMAL
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:801:3: ( ALPHA ( CHAR )* )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:801:5: ALPHA ( CHAR )*
			{
			mALPHA(); 

			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:801:11: ( CHAR )*
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( ((LA14_0 >= '-' && LA14_0 <= '.')||(LA14_0 >= '0' && LA14_0 <= '9')||(LA14_0 >= 'A' && LA14_0 <= 'Z')||LA14_0=='_'||(LA14_0 >= 'a' && LA14_0 <= 'z')||(LA14_0 >= '\u00C0' && LA14_0 <= '\u00D6')||(LA14_0 >= '\u00D8' && LA14_0 <= '\u00F6')||(LA14_0 >= '\u00F8' && LA14_0 <= '\u02FF')||(LA14_0 >= '\u0370' && LA14_0 <= '\u037D')||(LA14_0 >= '\u037F' && LA14_0 <= '\u1FFF')||(LA14_0 >= '\u200C' && LA14_0 <= '\u200D')||(LA14_0 >= '\u2070' && LA14_0 <= '\u218F')||(LA14_0 >= '\u2C00' && LA14_0 <= '\u2FEF')||(LA14_0 >= '\u3001' && LA14_0 <= '\uD7FF')||(LA14_0 >= '\uF900' && LA14_0 <= '\uFDCF')||(LA14_0 >= '\uFDF0' && LA14_0 <= '\uFFFD')) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:805:3: ( '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:805:5: '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' )
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:808:16: ( ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )* )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:808:18: ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )*
			{
			mALPHA(); 

			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:808:24: ( ALPHANUM | PLUS | MINUS | PERIOD )*
			loop15:
			while (true) {
				int alt15=2;
				int LA15_0 = input.LA(1);
				if ( (LA15_0=='+'||(LA15_0 >= '-' && LA15_0 <= '.')||(LA15_0 >= '0' && LA15_0 <= '9')||(LA15_0 >= 'A' && LA15_0 <= 'Z')||(LA15_0 >= 'a' && LA15_0 <= 'z')||(LA15_0 >= '\u00C0' && LA15_0 <= '\u00D6')||(LA15_0 >= '\u00D8' && LA15_0 <= '\u00F6')||(LA15_0 >= '\u00F8' && LA15_0 <= '\u02FF')||(LA15_0 >= '\u0370' && LA15_0 <= '\u037D')||(LA15_0 >= '\u037F' && LA15_0 <= '\u1FFF')||(LA15_0 >= '\u200C' && LA15_0 <= '\u200D')||(LA15_0 >= '\u2070' && LA15_0 <= '\u218F')||(LA15_0 >= '\u2C00' && LA15_0 <= '\u2FEF')||(LA15_0 >= '\u3001' && LA15_0 <= '\uD7FF')||(LA15_0 >= '\uF900' && LA15_0 <= '\uFDCF')||(LA15_0 >= '\uFDF0' && LA15_0 <= '\uFFFD')) ) {
					alt15=1;
				}

				switch (alt15) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:810:18: ( ( ALPHANUM | UNDERSCORE | MINUS | COLON | PERIOD | HASH | QUESTION | SLASH ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:812:18: ( ( ALPHA | UNDERSCORE ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:814:17: ( ( ID_START | DIGIT ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:816:12: ( ID_START ( ID_CORE )* )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:816:14: ID_START ( ID_CORE )*
			{
			mID_START(); 

			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:816:23: ( ID_CORE )*
			loop16:
			while (true) {
				int alt16=2;
				int LA16_0 = input.LA(1);
				if ( ((LA16_0 >= '0' && LA16_0 <= '9')||(LA16_0 >= 'A' && LA16_0 <= 'Z')||LA16_0=='_'||(LA16_0 >= 'a' && LA16_0 <= 'z')||(LA16_0 >= '\u00C0' && LA16_0 <= '\u00D6')||(LA16_0 >= '\u00D8' && LA16_0 <= '\u00F6')||(LA16_0 >= '\u00F8' && LA16_0 <= '\u02FF')||(LA16_0 >= '\u0370' && LA16_0 <= '\u037D')||(LA16_0 >= '\u037F' && LA16_0 <= '\u1FFF')||(LA16_0 >= '\u200C' && LA16_0 <= '\u200D')||(LA16_0 >= '\u2070' && LA16_0 <= '\u218F')||(LA16_0 >= '\u2C00' && LA16_0 <= '\u2FEF')||(LA16_0 >= '\u3001' && LA16_0 <= '\uD7FF')||(LA16_0 >= '\uF900' && LA16_0 <= '\uFDCF')||(LA16_0 >= '\uFDF0' && LA16_0 <= '\uFFFD')) ) {
					alt16=1;
				}

				switch (alt16) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:818:25: ( ( ALPHA | UNDERSCORE ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:820:19: ( ( NAME_START_CHAR | DIGIT | UNDERSCORE | MINUS | PERIOD | HASH | QUESTION | SLASH | PERCENT | EQUALS | SEMI ) )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
			{
			if ( input.LA(1)=='#'||input.LA(1)=='%'||(input.LA(1) >= '-' && input.LA(1) <= '9')||input.LA(1)==';'||input.LA(1)=='='||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:823:3: ( NAME_START_CHAR ( NAME_CHAR )* )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:823:5: NAME_START_CHAR ( NAME_CHAR )*
			{
			mNAME_START_CHAR(); 

			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:823:21: ( NAME_CHAR )*
			loop17:
			while (true) {
				int alt17=2;
				int LA17_0 = input.LA(1);
				if ( (LA17_0=='#'||LA17_0=='%'||(LA17_0 >= '-' && LA17_0 <= '9')||LA17_0==';'||LA17_0=='='||LA17_0=='?'||(LA17_0 >= 'A' && LA17_0 <= 'Z')||LA17_0=='_'||(LA17_0 >= 'a' && LA17_0 <= 'z')||(LA17_0 >= '\u00C0' && LA17_0 <= '\u00D6')||(LA17_0 >= '\u00D8' && LA17_0 <= '\u00F6')||(LA17_0 >= '\u00F8' && LA17_0 <= '\u02FF')||(LA17_0 >= '\u0370' && LA17_0 <= '\u037D')||(LA17_0 >= '\u037F' && LA17_0 <= '\u1FFF')||(LA17_0 >= '\u200C' && LA17_0 <= '\u200D')||(LA17_0 >= '\u2070' && LA17_0 <= '\u218F')||(LA17_0 >= '\u2C00' && LA17_0 <= '\u2FEF')||(LA17_0 >= '\u3001' && LA17_0 <= '\uD7FF')||(LA17_0 >= '\uF900' && LA17_0 <= '\uFDCF')||(LA17_0 >= '\uFDF0' && LA17_0 <= '\uFFFD')) ) {
					alt17=1;
				}

				switch (alt17) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
					{
					if ( input.LA(1)=='#'||input.LA(1)=='%'||(input.LA(1) >= '-' && input.LA(1) <= '9')||input.LA(1)==';'||input.LA(1)=='='||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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

	// $ANTLR start "NCNAME_EXT"
	public final void mNCNAME_EXT() throws RecognitionException {
		try {
			int _type = NCNAME_EXT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:827:3: ( ( NAME_CHAR | LCR_BRACKET | RCR_BRACKET | HASH | SLASH )* )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:827:5: ( NAME_CHAR | LCR_BRACKET | RCR_BRACKET | HASH | SLASH )*
			{
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:827:5: ( NAME_CHAR | LCR_BRACKET | RCR_BRACKET | HASH | SLASH )*
			loop18:
			while (true) {
				int alt18=2;
				int LA18_0 = input.LA(1);
				if ( (LA18_0=='#'||LA18_0=='%'||(LA18_0 >= '-' && LA18_0 <= '9')||LA18_0==';'||LA18_0=='='||LA18_0=='?'||(LA18_0 >= 'A' && LA18_0 <= 'Z')||LA18_0=='_'||(LA18_0 >= 'a' && LA18_0 <= '{')||LA18_0=='}'||(LA18_0 >= '\u00C0' && LA18_0 <= '\u00D6')||(LA18_0 >= '\u00D8' && LA18_0 <= '\u00F6')||(LA18_0 >= '\u00F8' && LA18_0 <= '\u02FF')||(LA18_0 >= '\u0370' && LA18_0 <= '\u037D')||(LA18_0 >= '\u037F' && LA18_0 <= '\u1FFF')||(LA18_0 >= '\u200C' && LA18_0 <= '\u200D')||(LA18_0 >= '\u2070' && LA18_0 <= '\u218F')||(LA18_0 >= '\u2C00' && LA18_0 <= '\u2FEF')||(LA18_0 >= '\u3001' && LA18_0 <= '\uD7FF')||(LA18_0 >= '\uF900' && LA18_0 <= '\uFDCF')||(LA18_0 >= '\uFDF0' && LA18_0 <= '\uFFFD')) ) {
					alt18=1;
				}

				switch (alt18) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
					{
					if ( input.LA(1)=='#'||input.LA(1)=='%'||(input.LA(1) >= '-' && input.LA(1) <= '9')||input.LA(1)==';'||input.LA(1)=='='||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= '{')||input.LA(1)=='}'||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NCNAME_EXT"

	// $ANTLR start "NAMESPACE"
	public final void mNAMESPACE() throws RecognitionException {
		try {
			int _type = NAMESPACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:831:3: ( NAME_START_CHAR ( NAME_CHAR )* COLON )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:831:5: NAME_START_CHAR ( NAME_CHAR )* COLON
			{
			mNAME_START_CHAR(); 

			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:831:21: ( NAME_CHAR )*
			loop19:
			while (true) {
				int alt19=2;
				int LA19_0 = input.LA(1);
				if ( (LA19_0=='#'||LA19_0=='%'||(LA19_0 >= '-' && LA19_0 <= '9')||LA19_0==';'||LA19_0=='='||LA19_0=='?'||(LA19_0 >= 'A' && LA19_0 <= 'Z')||LA19_0=='_'||(LA19_0 >= 'a' && LA19_0 <= 'z')||(LA19_0 >= '\u00C0' && LA19_0 <= '\u00D6')||(LA19_0 >= '\u00D8' && LA19_0 <= '\u00F6')||(LA19_0 >= '\u00F8' && LA19_0 <= '\u02FF')||(LA19_0 >= '\u0370' && LA19_0 <= '\u037D')||(LA19_0 >= '\u037F' && LA19_0 <= '\u1FFF')||(LA19_0 >= '\u200C' && LA19_0 <= '\u200D')||(LA19_0 >= '\u2070' && LA19_0 <= '\u218F')||(LA19_0 >= '\u2C00' && LA19_0 <= '\u2FEF')||(LA19_0 >= '\u3001' && LA19_0 <= '\uD7FF')||(LA19_0 >= '\uF900' && LA19_0 <= '\uFDCF')||(LA19_0 >= '\uFDF0' && LA19_0 <= '\uFFFD')) ) {
					alt19=1;
				}

				switch (alt19) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
					{
					if ( input.LA(1)=='#'||input.LA(1)=='%'||(input.LA(1) >= '-' && input.LA(1) <= '9')||input.LA(1)==';'||input.LA(1)=='='||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u02FF')||(input.LA(1) >= '\u0370' && input.LA(1) <= '\u037D')||(input.LA(1) >= '\u037F' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u200C' && input.LA(1) <= '\u200D')||(input.LA(1) >= '\u2070' && input.LA(1) <= '\u218F')||(input.LA(1) >= '\u2C00' && input.LA(1) <= '\u2FEF')||(input.LA(1) >= '\u3001' && input.LA(1) <= '\uD7FF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFDCF')||(input.LA(1) >= '\uFDF0' && input.LA(1) <= '\uFFFD') ) {
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
					break loop19;
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:835:3: ( ( NCNAME )? COLON NCNAME_EXT )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:835:5: ( NCNAME )? COLON NCNAME_EXT
			{
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:835:5: ( NCNAME )?
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( ((LA20_0 >= 'A' && LA20_0 <= 'Z')||LA20_0=='_'||(LA20_0 >= 'a' && LA20_0 <= 'z')||(LA20_0 >= '\u00C0' && LA20_0 <= '\u00D6')||(LA20_0 >= '\u00D8' && LA20_0 <= '\u00F6')||(LA20_0 >= '\u00F8' && LA20_0 <= '\u02FF')||(LA20_0 >= '\u0370' && LA20_0 <= '\u037D')||(LA20_0 >= '\u037F' && LA20_0 <= '\u1FFF')||(LA20_0 >= '\u200C' && LA20_0 <= '\u200D')||(LA20_0 >= '\u2070' && LA20_0 <= '\u218F')||(LA20_0 >= '\u2C00' && LA20_0 <= '\u2FEF')||(LA20_0 >= '\u3001' && LA20_0 <= '\uD7FF')||(LA20_0 >= '\uF900' && LA20_0 <= '\uFDCF')||(LA20_0 >= '\uFDF0' && LA20_0 <= '\uFFFD')) ) {
				alt20=1;
			}
			switch (alt20) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:835:5: NCNAME
					{
					mNCNAME(); 

					}
					break;

			}

			mCOLON(); 

			mNCNAME_EXT(); 

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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:839:3: ( '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\'' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:839:5: '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\''
			{
			match('\''); 
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:839:10: ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop21:
			while (true) {
				int alt21=3;
				int LA21_0 = input.LA(1);
				if ( ((LA21_0 >= '\u0000' && LA21_0 <= '\t')||(LA21_0 >= '\u000B' && LA21_0 <= '\f')||(LA21_0 >= '\u000E' && LA21_0 <= '&')||(LA21_0 >= '(' && LA21_0 <= '[')||(LA21_0 >= ']' && LA21_0 <= '\uFFFF')) ) {
					alt21=1;
				}
				else if ( (LA21_0=='\\') ) {
					alt21=2;
				}
				else if ( (LA21_0=='\'') ) {
					alt21=3;
				}

				switch (alt21) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:839:40: ~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:839:87: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop21;
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:851:3: ( '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:851:5: '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"'
			{
			match('\"'); 
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:851:10: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:851:40: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:851:87: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop22;
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

	// $ANTLR start "STRING_WITH_BRACKET"
	public final void mSTRING_WITH_BRACKET() throws RecognitionException {
		try {
			int _type = STRING_WITH_BRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:3: ( '<' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '>' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:5: '<' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '>'
			{
			match('<'); 
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:9: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop23:
			while (true) {
				int alt23=3;
				int LA23_0 = input.LA(1);
				if ( (LA23_0=='>') ) {
					alt23=3;
				}
				else if ( ((LA23_0 >= '\u0000' && LA23_0 <= '\t')||(LA23_0 >= '\u000B' && LA23_0 <= '\f')||(LA23_0 >= '\u000E' && LA23_0 <= '!')||(LA23_0 >= '#' && LA23_0 <= '=')||(LA23_0 >= '?' && LA23_0 <= '[')||(LA23_0 >= ']' && LA23_0 <= '\uFFFF')) ) {
					alt23=1;
				}
				else if ( (LA23_0=='\\') ) {
					alt23=2;
				}

				switch (alt23) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:39: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:86: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop23;
				}
			}

			match('>'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_WITH_BRACKET"

	// $ANTLR start "STRING_WITH_CURLY_BRACKET"
	public final void mSTRING_WITH_CURLY_BRACKET() throws RecognitionException {
		try {
			int _type = STRING_WITH_CURLY_BRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:859:3: ( '{' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '}' )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:859:5: '{' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '}'
			{
			match('{'); 
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:859:9: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop24:
			while (true) {
				int alt24=3;
				int LA24_0 = input.LA(1);
				if ( (LA24_0=='}') ) {
					alt24=3;
				}
				else if ( ((LA24_0 >= '\u0000' && LA24_0 <= '\t')||(LA24_0 >= '\u000B' && LA24_0 <= '\f')||(LA24_0 >= '\u000E' && LA24_0 <= '!')||(LA24_0 >= '#' && LA24_0 <= '[')||(LA24_0 >= ']' && LA24_0 <= '|')||(LA24_0 >= '~' && LA24_0 <= '\uFFFF')) ) {
					alt24=1;
				}
				else if ( (LA24_0=='\\') ) {
					alt24=2;
				}

				switch (alt24) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:859:39: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:859:86: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop24;
				}
			}

			match('}'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_WITH_CURLY_BRACKET"

	// $ANTLR start "STRING_URI"
	public final void mSTRING_URI() throws RecognitionException {
		try {
			int _type = STRING_URI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:863:3: ( SCHEMA COLON DOUBLE_SLASH ( URI_PATH )* )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:863:5: SCHEMA COLON DOUBLE_SLASH ( URI_PATH )*
			{
			mSCHEMA(); 

			mCOLON(); 

			mDOUBLE_SLASH(); 

			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:863:31: ( URI_PATH )*
			loop25:
			while (true) {
				int alt25=2;
				int LA25_0 = input.LA(1);
				if ( (LA25_0=='#'||(LA25_0 >= '-' && LA25_0 <= ':')||LA25_0=='?'||(LA25_0 >= 'A' && LA25_0 <= 'Z')||LA25_0=='_'||(LA25_0 >= 'a' && LA25_0 <= 'z')||(LA25_0 >= '\u00C0' && LA25_0 <= '\u00D6')||(LA25_0 >= '\u00D8' && LA25_0 <= '\u00F6')||(LA25_0 >= '\u00F8' && LA25_0 <= '\u02FF')||(LA25_0 >= '\u0370' && LA25_0 <= '\u037D')||(LA25_0 >= '\u037F' && LA25_0 <= '\u1FFF')||(LA25_0 >= '\u200C' && LA25_0 <= '\u200D')||(LA25_0 >= '\u2070' && LA25_0 <= '\u218F')||(LA25_0 >= '\u2C00' && LA25_0 <= '\u2FEF')||(LA25_0 >= '\u3001' && LA25_0 <= '\uD7FF')||(LA25_0 >= '\uF900' && LA25_0 <= '\uFDCF')||(LA25_0 >= '\uFDF0' && LA25_0 <= '\uFFFD')) ) {
					alt25=1;
				}

				switch (alt25) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					break loop25;
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
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			{
			// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			int cnt27=0;
			loop27:
			while (true) {
				int alt27=4;
				switch ( input.LA(1) ) {
				case ' ':
					{
					alt27=1;
					}
					break;
				case '\t':
					{
					alt27=2;
					}
					break;
				case '\n':
				case '\r':
					{
					alt27=3;
					}
					break;
				}
				switch (alt27) {
				case 1 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:6: ' '
					{
					match(' '); 
					}
					break;
				case 2 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:10: '\\t'
					{
					match('\t'); 
					}
					break;
				case 3 :
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:15: ( '\\n' | '\\r' ( '\\n' ) )
					{
					// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:15: ( '\\n' | '\\r' ( '\\n' ) )
					int alt26=2;
					int LA26_0 = input.LA(1);
					if ( (LA26_0=='\n') ) {
						alt26=1;
					}
					else if ( (LA26_0=='\r') ) {
						alt26=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 26, 0, input);
						throw nvae;
					}

					switch (alt26) {
						case 1 :
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:16: '\\n'
							{
							match('\n'); 
							}
							break;
						case 2 :
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:21: '\\r' ( '\\n' )
							{
							match('\r'); 
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:25: ( '\\n' )
							// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:866:26: '\\n'
							{
							match('\n'); 
							}

							}
							break;

					}

					}
					break;

				default :
					if ( cnt27 >= 1 ) break loop27;
					EarlyExitException eee = new EarlyExitException(27, input);
					throw eee;
				}
				cnt27++;
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
		// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:8: ( T__77 | BASE | PREFIX | FALSE | TRUE | REFERENCE | LTSIGN | RTSIGN | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LCR_BRACKET | RCR_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | NCNAME | NCNAME_EXT | NAMESPACE | PREFIXED_NAME | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_WITH_BRACKET | STRING_WITH_CURLY_BRACKET | STRING_URI | WS )
		int alt28=62;
		alt28 = dfa28.predict(input);
		switch (alt28) {
			case 1 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:10: T__77
				{
				mT__77(); 

				}
				break;
			case 2 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:16: BASE
				{
				mBASE(); 

				}
				break;
			case 3 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:21: PREFIX
				{
				mPREFIX(); 

				}
				break;
			case 4 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:28: FALSE
				{
				mFALSE(); 

				}
				break;
			case 5 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:34: TRUE
				{
				mTRUE(); 

				}
				break;
			case 6 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:39: REFERENCE
				{
				mREFERENCE(); 

				}
				break;
			case 7 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:49: LTSIGN
				{
				mLTSIGN(); 

				}
				break;
			case 8 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:56: RTSIGN
				{
				mRTSIGN(); 

				}
				break;
			case 9 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:63: SEMI
				{
				mSEMI(); 

				}
				break;
			case 10 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:68: PERIOD
				{
				mPERIOD(); 

				}
				break;
			case 11 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:75: COMMA
				{
				mCOMMA(); 

				}
				break;
			case 12 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:81: LSQ_BRACKET
				{
				mLSQ_BRACKET(); 

				}
				break;
			case 13 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:93: RSQ_BRACKET
				{
				mRSQ_BRACKET(); 

				}
				break;
			case 14 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:105: LCR_BRACKET
				{
				mLCR_BRACKET(); 

				}
				break;
			case 15 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:117: RCR_BRACKET
				{
				mRCR_BRACKET(); 

				}
				break;
			case 16 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:129: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 17 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:136: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 18 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:143: QUESTION
				{
				mQUESTION(); 

				}
				break;
			case 19 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:152: DOLLAR
				{
				mDOLLAR(); 

				}
				break;
			case 20 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:159: QUOTE_DOUBLE
				{
				mQUOTE_DOUBLE(); 

				}
				break;
			case 21 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:172: QUOTE_SINGLE
				{
				mQUOTE_SINGLE(); 

				}
				break;
			case 22 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:185: APOSTROPHE
				{
				mAPOSTROPHE(); 

				}
				break;
			case 23 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:196: UNDERSCORE
				{
				mUNDERSCORE(); 

				}
				break;
			case 24 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:207: MINUS
				{
				mMINUS(); 

				}
				break;
			case 25 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:213: ASTERISK
				{
				mASTERISK(); 

				}
				break;
			case 26 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:222: AMPERSAND
				{
				mAMPERSAND(); 

				}
				break;
			case 27 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:232: AT
				{
				mAT(); 

				}
				break;
			case 28 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:235: EXCLAMATION
				{
				mEXCLAMATION(); 

				}
				break;
			case 29 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:247: HASH
				{
				mHASH(); 

				}
				break;
			case 30 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:252: PERCENT
				{
				mPERCENT(); 

				}
				break;
			case 31 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:260: PLUS
				{
				mPLUS(); 

				}
				break;
			case 32 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:265: EQUALS
				{
				mEQUALS(); 

				}
				break;
			case 33 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:272: COLON
				{
				mCOLON(); 

				}
				break;
			case 34 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:278: LESS
				{
				mLESS(); 

				}
				break;
			case 35 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:283: GREATER
				{
				mGREATER(); 

				}
				break;
			case 36 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:291: SLASH
				{
				mSLASH(); 

				}
				break;
			case 37 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:297: DOUBLE_SLASH
				{
				mDOUBLE_SLASH(); 

				}
				break;
			case 38 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:310: BACKSLASH
				{
				mBACKSLASH(); 

				}
				break;
			case 39 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:320: BLANK
				{
				mBLANK(); 

				}
				break;
			case 40 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:326: BLANK_PREFIX
				{
				mBLANK_PREFIX(); 

				}
				break;
			case 41 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:339: TILDE
				{
				mTILDE(); 

				}
				break;
			case 42 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:345: CARET
				{
				mCARET(); 

				}
				break;
			case 43 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:351: INTEGER
				{
				mINTEGER(); 

				}
				break;
			case 44 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:359: DOUBLE
				{
				mDOUBLE(); 

				}
				break;
			case 45 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:366: DECIMAL
				{
				mDECIMAL(); 

				}
				break;
			case 46 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:374: INTEGER_POSITIVE
				{
				mINTEGER_POSITIVE(); 

				}
				break;
			case 47 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:391: INTEGER_NEGATIVE
				{
				mINTEGER_NEGATIVE(); 

				}
				break;
			case 48 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:408: DOUBLE_POSITIVE
				{
				mDOUBLE_POSITIVE(); 

				}
				break;
			case 49 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:424: DOUBLE_NEGATIVE
				{
				mDOUBLE_NEGATIVE(); 

				}
				break;
			case 50 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:440: DECIMAL_POSITIVE
				{
				mDECIMAL_POSITIVE(); 

				}
				break;
			case 51 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:457: DECIMAL_NEGATIVE
				{
				mDECIMAL_NEGATIVE(); 

				}
				break;
			case 52 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:474: VARNAME
				{
				mVARNAME(); 

				}
				break;
			case 53 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:482: NCNAME
				{
				mNCNAME(); 

				}
				break;
			case 54 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:489: NCNAME_EXT
				{
				mNCNAME_EXT(); 

				}
				break;
			case 55 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:500: NAMESPACE
				{
				mNAMESPACE(); 

				}
				break;
			case 56 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:510: PREFIXED_NAME
				{
				mPREFIXED_NAME(); 

				}
				break;
			case 57 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:524: STRING_WITH_QUOTE
				{
				mSTRING_WITH_QUOTE(); 

				}
				break;
			case 58 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:542: STRING_WITH_QUOTE_DOUBLE
				{
				mSTRING_WITH_QUOTE_DOUBLE(); 

				}
				break;
			case 59 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:567: STRING_WITH_BRACKET
				{
				mSTRING_WITH_BRACKET(); 

				}
				break;
			case 60 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:587: STRING_WITH_CURLY_BRACKET
				{
				mSTRING_WITH_CURLY_BRACKET(); 

				}
				break;
			case 61 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:613: STRING_URI
				{
				mSTRING_URI(); 

				}
				break;
			case 62 :
				// /Users/elem/git/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:624: WS
				{
				mWS(); 

				}
				break;

		}
	}


	protected DFA9 dfa9 = new DFA9(this);
	protected DFA28 dfa28 = new DFA28(this);
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
			return "765:1: DOUBLE : ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? );";
		}
	}

	static final String DFA28_eotS =
		"\1\47\1\51\4\60\1\65\1\67\1\72\1\74\1\75\1\uffff\1\100\1\uffff\1\101\1"+
		"\105\2\uffff\1\106\1\uffff\1\107\1\uffff\1\112\1\113\4\uffff\1\116\1\117"+
		"\1\120\1\123\1\124\1\uffff\1\127\2\uffff\1\130\1\60\3\uffff\1\60\1\133"+
		"\1\135\1\60\1\uffff\1\60\1\uffff\3\60\5\uffff\1\143\4\uffff\1\145\3\uffff"+
		"\1\47\1\uffff\1\47\4\uffff\1\146\2\uffff\1\147\1\47\3\uffff\1\153\4\uffff"+
		"\1\157\2\uffff\1\47\1\163\1\uffff\1\135\1\uffff\1\125\4\60\1\uffff\1\163"+
		"\3\uffff\1\47\1\175\1\177\3\uffff\1\u0081\1\uffff\1\145\2\163\1\uffff"+
		"\1\125\1\u0084\2\60\1\u0087\1\163\1\177\2\175\1\uffff\1\175\1\uffff\1"+
		"\u0081\1\uffff\1\163\1\125\1\uffff\1\60\1\u008b\1\uffff\2\175\1\u008c"+
		"\2\uffff";
	static final String DFA28_eofS =
		"\u008d\uffff";
	static final String DFA28_minS =
		"\1\11\5\43\1\136\2\0\2\43\1\uffff\1\135\1\uffff\1\0\1\43\2\uffff\1\43"+
		"\1\uffff\1\0\1\uffff\2\43\4\uffff\2\43\1\56\2\43\1\uffff\1\43\2\uffff"+
		"\2\43\3\uffff\4\43\1\uffff\1\43\1\uffff\3\43\5\uffff\1\0\4\uffff\1\43"+
		"\3\uffff\1\0\1\uffff\1\0\4\uffff\1\43\2\uffff\1\43\1\60\3\uffff\1\56\1"+
		"\60\3\uffff\1\43\2\uffff\1\60\1\43\1\uffff\1\43\1\uffff\1\57\4\43\1\uffff"+
		"\1\43\3\uffff\1\60\2\43\1\uffff\1\60\1\uffff\1\60\1\uffff\3\43\1\uffff"+
		"\11\43\1\uffff\1\43\1\uffff\1\60\1\uffff\2\43\1\uffff\2\43\1\uffff\3\43"+
		"\2\uffff";
	static final String DFA28_maxS =
		"\6\ufffd\1\136\2\uffff\2\ufffd\1\uffff\1\135\1\uffff\1\uffff\1\ufffd\2"+
		"\uffff\1\ufffd\1\uffff\1\uffff\1\uffff\2\ufffd\4\uffff\2\ufffd\1\71\2"+
		"\ufffd\1\uffff\1\ufffd\2\uffff\2\ufffd\3\uffff\4\ufffd\1\uffff\1\ufffd"+
		"\1\uffff\3\ufffd\5\uffff\1\uffff\4\uffff\1\ufffd\3\uffff\1\uffff\1\uffff"+
		"\1\uffff\4\uffff\1\ufffd\2\uffff\1\ufffd\1\71\3\uffff\1\145\1\71\3\uffff"+
		"\1\ufffd\2\uffff\1\145\1\ufffd\1\uffff\1\ufffd\1\uffff\1\57\4\ufffd\1"+
		"\uffff\1\ufffd\3\uffff\1\145\2\ufffd\1\uffff\1\145\1\uffff\1\145\1\uffff"+
		"\3\ufffd\1\uffff\11\ufffd\1\uffff\1\ufffd\1\uffff\1\145\1\uffff\2\ufffd"+
		"\1\uffff\2\ufffd\1\uffff\3\ufffd\2\uffff";
	static final String DFA28_acceptS =
		"\13\uffff\1\13\1\uffff\1\15\2\uffff\1\20\1\21\1\uffff\1\23\1\uffff\1\26"+
		"\2\uffff\1\31\1\32\1\33\1\34\5\uffff\1\43\1\uffff\1\46\1\51\2\uffff\1"+
		"\66\1\76\1\1\4\uffff\1\75\1\uffff\1\64\3\uffff\1\6\1\52\1\7\1\42\1\73"+
		"\1\uffff\1\24\1\72\1\11\1\12\1\uffff\1\47\1\14\1\16\1\uffff\1\74\1\uffff"+
		"\1\17\1\22\1\25\1\71\1\uffff\1\27\1\30\2\uffff\1\35\1\36\1\37\2\uffff"+
		"\1\40\1\41\1\70\1\uffff\1\44\1\53\2\uffff\1\65\1\uffff\1\67\5\uffff\1"+
		"\10\1\uffff\1\55\1\50\1\57\3\uffff\1\56\1\uffff\1\60\1\uffff\1\45\3\uffff"+
		"\1\54\11\uffff\1\61\1\uffff\1\63\1\uffff\1\62\2\uffff\1\2\2\uffff\1\5"+
		"\3\uffff\1\4\1\3";
	static final String DFA28_specialS =
		"\7\uffff\1\1\1\6\5\uffff\1\5\5\uffff\1\3\44\uffff\1\0\10\uffff\1\2\1\uffff"+
		"\1\4\110\uffff}>";
	static final String[] DFA28_transitionS = {
			"\2\50\2\uffff\1\50\22\uffff\1\50\1\33\1\10\1\34\1\23\1\35\1\31\1\24\1"+
			"\20\1\21\1\30\1\36\1\13\1\27\1\12\1\42\12\45\1\40\1\11\1\7\1\37\1\41"+
			"\1\22\1\32\1\46\1\2\3\46\1\4\11\46\1\3\3\46\1\5\6\46\1\14\1\43\1\15\1"+
			"\6\1\26\1\25\1\1\1\2\3\46\1\4\11\46\1\3\3\46\1\5\6\46\1\16\1\uffff\1"+
			"\17\1\44\101\uffff\27\46\1\uffff\37\46\1\uffff\u0208\46\160\uffff\16"+
			"\46\1\uffff\u1c81\46\14\uffff\2\46\142\uffff\u0120\46\u0a70\uffff\u03f0"+
			"\46\21\uffff\ua7ff\46\u2100\uffff\u04d0\46\40\uffff\u020e\46",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\1\57\31\52\4\uffff\1\55\1\uffff\1\57"+
			"\31\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208"+
			"\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52"+
			"\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\21\52\1\61\10\52\4\uffff\1\55\1\uffff"+
			"\21\52\1\61\10\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\1\62\31\52\4\uffff\1\55\1\uffff\1\62"+
			"\31\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208"+
			"\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52"+
			"\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\21\52\1\63\10\52\4\uffff\1\55\1\uffff"+
			"\21\52\1\63\10\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\64",
			"\12\70\1\uffff\2\70\1\uffff\24\70\1\66\uffdd\70",
			"\12\73\1\uffff\2\73\1\uffff\60\73\1\71\uffc1\73",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\76\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\1\77",
			"",
			"\12\103\1\uffff\2\103\1\uffff\24\103\1\uffff\1\104\1\103\1\104\7\103"+
			"\15\104\1\103\1\104\1\103\1\104\1\103\1\104\1\103\32\104\4\103\1\104"+
			"\1\103\33\104\1\103\1\102\102\103\27\104\1\103\37\104\1\103\u0208\104"+
			"\160\103\16\104\1\103\u1c81\104\14\103\2\104\142\103\u0120\104\u0a70"+
			"\103\u03f0\104\21\103\ua7ff\104\u2100\103\u04d0\104\40\103\u020e\104"+
			"\2\103",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\12\110\1\uffff\2\110\1\uffff\ufff2\110",
			"",
			"\1\53\1\uffff\1\53\7\uffff\15\53\1\111\1\53\1\uffff\1\53\1\uffff\1\53"+
			"\1\uffff\32\53\4\uffff\1\53\1\uffff\32\53\1\47\1\uffff\1\47\102\uffff"+
			"\27\53\1\uffff\37\53\1\uffff\u0208\53\160\uffff\16\53\1\uffff\u1c81\53"+
			"\14\uffff\2\53\142\uffff\u0120\53\u0a70\uffff\u03f0\53\21\uffff\ua7ff"+
			"\53\u2100\uffff\u04d0\53\40\uffff\u020e\53",
			"\1\47\1\uffff\1\47\7\uffff\1\47\1\115\1\47\12\114\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1"+
			"\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1"+
			"\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47"+
			"\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"",
			"",
			"",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\122\1\uffff\12\121",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\125\1\uffff\1\125\7\uffff\15\125\1\uffff\1\125\1\uffff\1\125\1\uffff"+
			"\1\125\1\uffff\32\125\4\uffff\1\125\1\uffff\33\125\1\uffff\1\125\102"+
			"\uffff\27\125\1\uffff\37\125\1\uffff\u0208\125\160\uffff\16\125\1\uffff"+
			"\u1c81\125\14\uffff\2\125\142\uffff\u0120\125\u0a70\uffff\u03f0\125\21"+
			"\uffff\ua7ff\125\u2100\uffff\u04d0\125\40\uffff\u020e\125",
			"",
			"\1\47\1\uffff\1\47\7\uffff\2\47\1\126\12\47\1\uffff\1\47\1\uffff\1\47"+
			"\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"",
			"\1\47\1\uffff\1\47\7\uffff\1\47\1\131\1\47\12\45\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\1\47\1\uffff\4\47\1\132\25\47\4\uffff\1\47\1\uffff\4\47"+
			"\1\132\26\47\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208"+
			"\47\160\uffff\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47"+
			"\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff"+
			"\u020e\47",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"",
			"",
			"",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\7\uffff\15\53\1\134\1\53\1\uffff\1\53\1\uffff\1\53"+
			"\1\uffff\32\53\4\uffff\1\53\1\uffff\32\53\1\47\1\uffff\1\47\102\uffff"+
			"\27\53\1\uffff\37\53\1\uffff\u0208\53\160\uffff\16\53\1\uffff\u1c81\53"+
			"\14\uffff\2\53\142\uffff\u0120\53\u0a70\uffff\u03f0\53\21\uffff\ua7ff"+
			"\53\u2100\uffff\u04d0\53\40\uffff\u020e\53",
			"\1\125\1\uffff\1\125\7\uffff\2\125\1\136\12\125\1\uffff\1\125\1\uffff"+
			"\1\125\1\uffff\1\125\1\uffff\32\125\4\uffff\1\125\1\uffff\33\125\1\uffff"+
			"\1\125\102\uffff\27\125\1\uffff\37\125\1\uffff\u0208\125\160\uffff\16"+
			"\125\1\uffff\u1c81\125\14\uffff\2\125\142\uffff\u0120\125\u0a70\uffff"+
			"\u03f0\125\21\uffff\ua7ff\125\u2100\uffff\u04d0\125\40\uffff\u020e\125",
			"\1\53\1\uffff\1\53\7\uffff\2\55\1\53\12\55\1\134\1\53\1\uffff\1\53\1"+
			"\uffff\1\53\1\uffff\32\55\4\uffff\1\55\1\uffff\32\55\1\47\1\uffff\1\47"+
			"\102\uffff\27\55\1\uffff\37\55\1\uffff\u0208\55\160\uffff\16\55\1\uffff"+
			"\u1c81\55\14\uffff\2\55\142\uffff\u0120\55\u0a70\uffff\u03f0\55\21\uffff"+
			"\ua7ff\55\u2100\uffff\u04d0\55\40\uffff\u020e\55",
			"",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\22\52\1\137\7\52\4\uffff\1\55\1\uffff"+
			"\22\52\1\137\7\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\4\52\1\140\25\52\4\uffff\1\55\1\uffff"+
			"\4\52\1\140\25\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\13\52\1\141\16\52\4\uffff\1\55\1\uffff"+
			"\13\52\1\141\16\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1"+
			"\uffff\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff"+
			"\u0120\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52"+
			"\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\24\52\1\142\5\52\4\uffff\1\55\1\uffff"+
			"\24\52\1\142\5\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"",
			"",
			"",
			"",
			"",
			"\12\73\1\uffff\2\73\1\uffff\ufff2\73",
			"",
			"",
			"",
			"",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\76\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\4\47\1\144\25\47\4\uffff\1\47\1\uffff\4\47\1\144\26\47"+
			"\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff"+
			"\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff"+
			"\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"",
			"",
			"\12\103\1\uffff\2\103\1\uffff\24\103\1\uffff\1\104\1\103\1\104\7\103"+
			"\15\104\1\103\1\104\1\103\1\104\1\103\1\104\1\103\32\104\4\103\1\104"+
			"\1\103\33\104\1\103\1\102\102\103\27\104\1\103\37\104\1\103\u0208\104"+
			"\160\103\16\104\1\103\u1c81\104\14\103\2\104\142\103\u0120\104\u0a70"+
			"\103\u03f0\104\21\103\ua7ff\104\u2100\103\u04d0\104\40\103\u020e\104"+
			"\2\103",
			"",
			"\12\103\1\uffff\2\103\1\uffff\24\103\1\uffff\1\104\1\103\1\104\7\103"+
			"\15\104\1\103\1\104\1\103\1\104\1\103\1\104\1\103\32\104\4\103\1\104"+
			"\1\103\33\104\1\103\1\102\102\103\27\104\1\103\37\104\1\103\u0208\104"+
			"\160\103\16\104\1\103\u1c81\104\14\103\2\104\142\103\u0120\104\u0a70"+
			"\103\u03f0\104\21\103\ua7ff\104\u2100\103\u04d0\104\40\103\u020e\104"+
			"\2\103",
			"",
			"",
			"",
			"",
			"\1\125\1\uffff\1\125\7\uffff\15\125\1\uffff\1\125\1\uffff\1\125\1\uffff"+
			"\1\125\1\uffff\32\125\4\uffff\1\125\1\uffff\33\125\1\uffff\1\125\102"+
			"\uffff\27\125\1\uffff\37\125\1\uffff\u0208\125\160\uffff\16\125\1\uffff"+
			"\u1c81\125\14\uffff\2\125\142\uffff\u0120\125\u0a70\uffff\u03f0\125\21"+
			"\uffff\ua7ff\125\u2100\uffff\u04d0\125\40\uffff\u020e\125",
			"",
			"",
			"\1\47\1\uffff\1\47\7\uffff\1\47\1\150\1\47\12\114\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\1\47\1\uffff\4\47\1\151\25\47\4\uffff\1\47\1\uffff\4\47"+
			"\1\151\26\47\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208"+
			"\47\160\uffff\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47"+
			"\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff"+
			"\u020e\47",
			"\12\152",
			"",
			"",
			"",
			"\1\154\1\uffff\12\121\13\uffff\1\155\37\uffff\1\155",
			"\12\156",
			"",
			"",
			"",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"",
			"\12\160\13\uffff\1\161\37\uffff\1\161",
			"\1\47\1\uffff\1\47\7\uffff\1\162\14\47\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\1\125\1\uffff\1\125\7\uffff\15\125\1\uffff\1\125\1\uffff\1\125\1\uffff"+
			"\1\125\1\uffff\32\125\4\uffff\1\125\1\uffff\33\125\1\uffff\1\125\102"+
			"\uffff\27\125\1\uffff\37\125\1\uffff\u0208\125\160\uffff\16\125\1\uffff"+
			"\u1c81\125\14\uffff\2\125\142\uffff\u0120\125\u0a70\uffff\u03f0\125\21"+
			"\uffff\ua7ff\125\u2100\uffff\u04d0\125\40\uffff\u020e\125",
			"",
			"\1\164",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\4\52\1\165\25\52\4\uffff\1\55\1\uffff"+
			"\4\52\1\165\25\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\5\52\1\166\24\52\4\uffff\1\55\1\uffff"+
			"\5\52\1\166\24\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\22\52\1\167\7\52\4\uffff\1\55\1\uffff"+
			"\22\52\1\167\7\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\4\52\1\170\25\52\4\uffff\1\55\1\uffff"+
			"\4\52\1\170\25\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"",
			"\1\47\1\uffff\1\47\7\uffff\1\171\14\47\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"",
			"",
			"\12\172\13\uffff\1\173\37\uffff\1\173",
			"\1\47\1\uffff\1\47\7\uffff\1\174\14\47\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\152\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\4\47\1\176\25\47\4\uffff\1\47\1\uffff\4\47\1\176\26\47"+
			"\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff"+
			"\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff"+
			"\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\12\u0080\13\uffff\1\155\37\uffff\1\155",
			"",
			"\12\156\13\uffff\1\155\37\uffff\1\155",
			"",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\160\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\4\47\1\161\25\47\4\uffff\1\47\1\uffff\4\47\1\161\26\47"+
			"\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff"+
			"\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff"+
			"\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\1\u0082\14\47\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\1\u0083\11\uffff\15\u0083\1\56\4\uffff\1\u0083\1\uffff\32\u0083\4\uffff"+
			"\1\u0083\1\uffff\32\u0083\105\uffff\27\u0083\1\uffff\37\u0083\1\uffff"+
			"\u0208\u0083\160\uffff\16\u0083\1\uffff\u1c81\u0083\14\uffff\2\u0083"+
			"\142\uffff\u0120\u0083\u0a70\uffff\u03f0\u0083\21\uffff\ua7ff\u0083\u2100"+
			"\uffff\u04d0\u0083\40\uffff\u020e\u0083",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\10\52\1\u0085\21\52\4\uffff\1\55\1\uffff"+
			"\10\52\1\u0085\21\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52"+
			"\1\uffff\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142"+
			"\uffff\u0120\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0"+
			"\52\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\4\52\1\u0086\25\52\4\uffff\1\55\1\uffff"+
			"\4\52\1\u0086\25\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1"+
			"\uffff\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff"+
			"\u0120\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52"+
			"\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\172\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\4\47\1\173\25\47\4\uffff\1\47\1\uffff\4\47\1\173\26\47"+
			"\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff"+
			"\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff"+
			"\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\1\u0088\14\47\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\1\47\1\uffff\1\47\7\uffff\1\u0089\14\47\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\12\u0080\13\uffff\1\155\37\uffff\1\155",
			"",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\u0083\11\uffff\15\u0083\1\56\4\uffff\1\u0083\1\uffff\32\u0083\4\uffff"+
			"\1\u0083\1\uffff\32\u0083\105\uffff\27\u0083\1\uffff\37\u0083\1\uffff"+
			"\u0208\u0083\160\uffff\16\u0083\1\uffff\u1c81\u0083\14\uffff\2\u0083"+
			"\142\uffff\u0120\u0083\u0a70\uffff\u03f0\u0083\21\uffff\ua7ff\u0083\u2100"+
			"\uffff\u04d0\u0083\40\uffff\u020e\u0083",
			"",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\27\52\1\u008a\2\52\4\uffff\1\55\1\uffff"+
			"\27\52\1\u008a\2\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1"+
			"\uffff\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff"+
			"\u0120\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52"+
			"\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\15\47\1\uffff\1\47\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff\27"+
			"\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47\14"+
			"\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff\47"+
			"\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"",
			""
	};

	static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
	static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
	static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
	static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
	static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
	static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
	static final short[][] DFA28_transition;

	static {
		int numStates = DFA28_transitionS.length;
		DFA28_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
		}
	}

	protected class DFA28 extends DFA {

		public DFA28(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 28;
			this.eot = DFA28_eot;
			this.eof = DFA28_eof;
			this.min = DFA28_min;
			this.max = DFA28_max;
			this.accept = DFA28_accept;
			this.special = DFA28_special;
			this.transition = DFA28_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__77 | BASE | PREFIX | FALSE | TRUE | REFERENCE | LTSIGN | RTSIGN | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LCR_BRACKET | RCR_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | NCNAME | NCNAME_EXT | NAMESPACE | PREFIXED_NAME | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_WITH_BRACKET | STRING_WITH_CURLY_BRACKET | STRING_URI | WS );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA28_57 = input.LA(1);
						s = -1;
						if ( ((LA28_57 >= '\u0000' && LA28_57 <= '\t')||(LA28_57 >= '\u000B' && LA28_57 <= '\f')||(LA28_57 >= '\u000E' && LA28_57 <= '\uFFFF')) ) {s = 59;}
						else s = 99;
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA28_7 = input.LA(1);
						s = -1;
						if ( (LA28_7=='\"') ) {s = 54;}
						else if ( ((LA28_7 >= '\u0000' && LA28_7 <= '\t')||(LA28_7 >= '\u000B' && LA28_7 <= '\f')||(LA28_7 >= '\u000E' && LA28_7 <= '!')||(LA28_7 >= '#' && LA28_7 <= '\uFFFF')) ) {s = 56;}
						else s = 55;
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA28_66 = input.LA(1);
						s = -1;
						if ( (LA28_66=='}') ) {s = 66;}
						else if ( (LA28_66=='#'||LA28_66=='%'||(LA28_66 >= '-' && LA28_66 <= '9')||LA28_66==';'||LA28_66=='='||LA28_66=='?'||(LA28_66 >= 'A' && LA28_66 <= 'Z')||LA28_66=='_'||(LA28_66 >= 'a' && LA28_66 <= '{')||(LA28_66 >= '\u00C0' && LA28_66 <= '\u00D6')||(LA28_66 >= '\u00D8' && LA28_66 <= '\u00F6')||(LA28_66 >= '\u00F8' && LA28_66 <= '\u02FF')||(LA28_66 >= '\u0370' && LA28_66 <= '\u037D')||(LA28_66 >= '\u037F' && LA28_66 <= '\u1FFF')||(LA28_66 >= '\u200C' && LA28_66 <= '\u200D')||(LA28_66 >= '\u2070' && LA28_66 <= '\u218F')||(LA28_66 >= '\u2C00' && LA28_66 <= '\u2FEF')||(LA28_66 >= '\u3001' && LA28_66 <= '\uD7FF')||(LA28_66 >= '\uF900' && LA28_66 <= '\uFDCF')||(LA28_66 >= '\uFDF0' && LA28_66 <= '\uFFFD')) ) {s = 68;}
						else if ( ((LA28_66 >= '\u0000' && LA28_66 <= '\t')||(LA28_66 >= '\u000B' && LA28_66 <= '\f')||(LA28_66 >= '\u000E' && LA28_66 <= '!')||LA28_66=='$'||(LA28_66 >= '&' && LA28_66 <= ',')||LA28_66==':'||LA28_66=='<'||LA28_66=='>'||LA28_66=='@'||(LA28_66 >= '[' && LA28_66 <= '^')||LA28_66=='`'||LA28_66=='|'||(LA28_66 >= '~' && LA28_66 <= '\u00BF')||LA28_66=='\u00D7'||LA28_66=='\u00F7'||(LA28_66 >= '\u0300' && LA28_66 <= '\u036F')||LA28_66=='\u037E'||(LA28_66 >= '\u2000' && LA28_66 <= '\u200B')||(LA28_66 >= '\u200E' && LA28_66 <= '\u206F')||(LA28_66 >= '\u2190' && LA28_66 <= '\u2BFF')||(LA28_66 >= '\u2FF0' && LA28_66 <= '\u3000')||(LA28_66 >= '\uD800' && LA28_66 <= '\uF8FF')||(LA28_66 >= '\uFDD0' && LA28_66 <= '\uFDEF')||(LA28_66 >= '\uFFFE' && LA28_66 <= '\uFFFF')) ) {s = 67;}
						else s = 39;
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA28_20 = input.LA(1);
						s = -1;
						if ( ((LA28_20 >= '\u0000' && LA28_20 <= '\t')||(LA28_20 >= '\u000B' && LA28_20 <= '\f')||(LA28_20 >= '\u000E' && LA28_20 <= '\uFFFF')) ) {s = 72;}
						else s = 71;
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA28_68 = input.LA(1);
						s = -1;
						if ( (LA28_68=='}') ) {s = 66;}
						else if ( (LA28_68=='#'||LA28_68=='%'||(LA28_68 >= '-' && LA28_68 <= '9')||LA28_68==';'||LA28_68=='='||LA28_68=='?'||(LA28_68 >= 'A' && LA28_68 <= 'Z')||LA28_68=='_'||(LA28_68 >= 'a' && LA28_68 <= '{')||(LA28_68 >= '\u00C0' && LA28_68 <= '\u00D6')||(LA28_68 >= '\u00D8' && LA28_68 <= '\u00F6')||(LA28_68 >= '\u00F8' && LA28_68 <= '\u02FF')||(LA28_68 >= '\u0370' && LA28_68 <= '\u037D')||(LA28_68 >= '\u037F' && LA28_68 <= '\u1FFF')||(LA28_68 >= '\u200C' && LA28_68 <= '\u200D')||(LA28_68 >= '\u2070' && LA28_68 <= '\u218F')||(LA28_68 >= '\u2C00' && LA28_68 <= '\u2FEF')||(LA28_68 >= '\u3001' && LA28_68 <= '\uD7FF')||(LA28_68 >= '\uF900' && LA28_68 <= '\uFDCF')||(LA28_68 >= '\uFDF0' && LA28_68 <= '\uFFFD')) ) {s = 68;}
						else if ( ((LA28_68 >= '\u0000' && LA28_68 <= '\t')||(LA28_68 >= '\u000B' && LA28_68 <= '\f')||(LA28_68 >= '\u000E' && LA28_68 <= '!')||LA28_68=='$'||(LA28_68 >= '&' && LA28_68 <= ',')||LA28_68==':'||LA28_68=='<'||LA28_68=='>'||LA28_68=='@'||(LA28_68 >= '[' && LA28_68 <= '^')||LA28_68=='`'||LA28_68=='|'||(LA28_68 >= '~' && LA28_68 <= '\u00BF')||LA28_68=='\u00D7'||LA28_68=='\u00F7'||(LA28_68 >= '\u0300' && LA28_68 <= '\u036F')||LA28_68=='\u037E'||(LA28_68 >= '\u2000' && LA28_68 <= '\u200B')||(LA28_68 >= '\u200E' && LA28_68 <= '\u206F')||(LA28_68 >= '\u2190' && LA28_68 <= '\u2BFF')||(LA28_68 >= '\u2FF0' && LA28_68 <= '\u3000')||(LA28_68 >= '\uD800' && LA28_68 <= '\uF8FF')||(LA28_68 >= '\uFDD0' && LA28_68 <= '\uFDEF')||(LA28_68 >= '\uFFFE' && LA28_68 <= '\uFFFF')) ) {s = 67;}
						else s = 39;
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA28_14 = input.LA(1);
						s = -1;
						if ( (LA28_14=='}') ) {s = 66;}
						else if ( ((LA28_14 >= '\u0000' && LA28_14 <= '\t')||(LA28_14 >= '\u000B' && LA28_14 <= '\f')||(LA28_14 >= '\u000E' && LA28_14 <= '!')||LA28_14=='$'||(LA28_14 >= '&' && LA28_14 <= ',')||LA28_14==':'||LA28_14=='<'||LA28_14=='>'||LA28_14=='@'||(LA28_14 >= '[' && LA28_14 <= '^')||LA28_14=='`'||LA28_14=='|'||(LA28_14 >= '~' && LA28_14 <= '\u00BF')||LA28_14=='\u00D7'||LA28_14=='\u00F7'||(LA28_14 >= '\u0300' && LA28_14 <= '\u036F')||LA28_14=='\u037E'||(LA28_14 >= '\u2000' && LA28_14 <= '\u200B')||(LA28_14 >= '\u200E' && LA28_14 <= '\u206F')||(LA28_14 >= '\u2190' && LA28_14 <= '\u2BFF')||(LA28_14 >= '\u2FF0' && LA28_14 <= '\u3000')||(LA28_14 >= '\uD800' && LA28_14 <= '\uF8FF')||(LA28_14 >= '\uFDD0' && LA28_14 <= '\uFDEF')||(LA28_14 >= '\uFFFE' && LA28_14 <= '\uFFFF')) ) {s = 67;}
						else if ( (LA28_14=='#'||LA28_14=='%'||(LA28_14 >= '-' && LA28_14 <= '9')||LA28_14==';'||LA28_14=='='||LA28_14=='?'||(LA28_14 >= 'A' && LA28_14 <= 'Z')||LA28_14=='_'||(LA28_14 >= 'a' && LA28_14 <= '{')||(LA28_14 >= '\u00C0' && LA28_14 <= '\u00D6')||(LA28_14 >= '\u00D8' && LA28_14 <= '\u00F6')||(LA28_14 >= '\u00F8' && LA28_14 <= '\u02FF')||(LA28_14 >= '\u0370' && LA28_14 <= '\u037D')||(LA28_14 >= '\u037F' && LA28_14 <= '\u1FFF')||(LA28_14 >= '\u200C' && LA28_14 <= '\u200D')||(LA28_14 >= '\u2070' && LA28_14 <= '\u218F')||(LA28_14 >= '\u2C00' && LA28_14 <= '\u2FEF')||(LA28_14 >= '\u3001' && LA28_14 <= '\uD7FF')||(LA28_14 >= '\uF900' && LA28_14 <= '\uFDCF')||(LA28_14 >= '\uFDF0' && LA28_14 <= '\uFFFD')) ) {s = 68;}
						else s = 65;
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA28_8 = input.LA(1);
						s = -1;
						if ( (LA28_8=='>') ) {s = 57;}
						else if ( ((LA28_8 >= '\u0000' && LA28_8 <= '\t')||(LA28_8 >= '\u000B' && LA28_8 <= '\f')||(LA28_8 >= '\u000E' && LA28_8 <= '=')||(LA28_8 >= '?' && LA28_8 <= '\uFFFF')) ) {s = 59;}
						else s = 58;
						if ( s>=0 ) return s;
						break;
			}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 28, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
