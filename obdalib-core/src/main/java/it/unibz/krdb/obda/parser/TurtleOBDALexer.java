// $ANTLR 3.5.2 obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g 2015-11-10 14:55:23

package it.unibz.krdb.obda.parser;

import org.antlr.runtime.*;

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
	@Override public String getGrammarFileName() { return "obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g"; }

	// $ANTLR start "T__77"
	public final void mT__77() throws RecognitionException {
		try {
			int _type = T__77;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:44:7: ( 'a' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:44:9: 'a'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:730:5: ( ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:730:7: ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:732:7: ( ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:732:9: ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' )
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:734:6: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:734:8: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:736:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:736:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:738:10: ( '^^' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:738:16: '^^'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:739:7: ( '<\"' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:739:16: '<\"'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:740:7: ( '\">' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:740:16: '\">'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:741:5: ( ';' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:741:16: ';'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:742:7: ( '.' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:742:16: '.'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:743:6: ( ',' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:743:16: ','
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:744:12: ( '[' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:744:16: '['
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:745:12: ( ']' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:745:16: ']'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:746:12: ( '{' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:746:16: '{'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:747:12: ( '}' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:747:16: '}'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:748:7: ( '(' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:748:16: '('
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:749:7: ( ')' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:749:16: ')'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:750:9: ( '?' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:750:16: '?'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:751:7: ( '$' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:751:16: '$'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:752:13: ( '\"' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:752:16: '\"'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:753:13: ( '\\'' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:753:16: '\\''
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:754:11: ( '`' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:754:16: '`'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:755:11: ( '_' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:755:16: '_'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:756:6: ( '-' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:756:16: '-'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:757:9: ( '*' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:757:16: '*'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:758:10: ( '&' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:758:16: '&'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:759:3: ( '@' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:759:16: '@'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:760:12: ( '!' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:760:16: '!'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:761:5: ( '#' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:761:16: '#'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:762:8: ( '%' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:762:16: '%'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:763:5: ( '+' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:763:16: '+'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:764:7: ( '=' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:764:16: '='
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:765:6: ( ':' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:765:16: ':'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:766:5: ( '<' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:766:16: '<'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:767:8: ( '>' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:767:16: '>'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:768:6: ( '/' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:768:16: '/'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:769:13: ( '//' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:769:16: '//'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:770:10: ( '\\\\' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:770:16: '\\\\'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:771:6: ( '[]' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:771:15: '[]'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:772:13: ( '_:' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:772:16: '_:'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:773:6: ( '~' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:773:16: '~'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:774:6: ( '^' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:774:16: '^'
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:777:3: ( 'a' .. 'z' | 'A' .. 'Z' | '\\u00C0' .. '\\u00D6' | '\\u00D8' .. '\\u00F6' | '\\u00F8' .. '\\u02FF' | '\\u0370' .. '\\u037D' | '\\u037F' .. '\\u1FFF' | '\\u200C' .. '\\u200D' | '\\u2070' .. '\\u218F' | '\\u2C00' .. '\\u2FEF' | '\\u3001' .. '\\uD7FF' | '\\uF900' .. '\\uFDCF' | '\\uFDF0' .. '\\uFFFD' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:793:3: ( '0' .. '9' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:797:3: ( ALPHA | DIGIT )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:802:3: ( ALPHANUM | UNDERSCORE | MINUS | PERIOD )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:809:3: ( ( DIGIT )+ )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:809:5: ( DIGIT )+
			{
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:809:5: ( DIGIT )+
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
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:813:3: ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )* | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )* | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )* )
			int alt12=3;
			alt12 = dfa12.predict(input);
			switch (alt12) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:813:5: ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )*
					{
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:813:5: ( DIGIT )+
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
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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

					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:813:19: ( DIGIT )*
					loop3:
					while (true) {
						int alt3=2;
						int LA3_0 = input.LA(1);
						if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
							alt3=1;
						}

						switch (alt3) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:813:36: ( '-' | '+' )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0=='+'||LA4_0=='-') ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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

					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:813:47: ( DIGIT )*
					loop5:
					while (true) {
						int alt5=2;
						int LA5_0 = input.LA(1);
						if ( ((LA5_0 >= '0' && LA5_0 <= '9')) ) {
							alt5=1;
						}

						switch (alt5) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							break loop5;
						}
					}

					}
					break;
				case 2 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:814:5: PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )*
					{
					mPERIOD(); 

					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:814:12: ( DIGIT )+
					int cnt6=0;
					loop6:
					while (true) {
						int alt6=2;
						int LA6_0 = input.LA(1);
						if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
							alt6=1;
						}

						switch (alt6) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							if ( cnt6 >= 1 ) break loop6;
							EarlyExitException eee = new EarlyExitException(6, input);
							throw eee;
						}
						cnt6++;
					}

					if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:814:29: ( '-' | '+' )?
					int alt7=2;
					int LA7_0 = input.LA(1);
					if ( (LA7_0=='+'||LA7_0=='-') ) {
						alt7=1;
					}
					switch (alt7) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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

					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:814:40: ( DIGIT )*
					loop8:
					while (true) {
						int alt8=2;
						int LA8_0 = input.LA(1);
						if ( ((LA8_0 >= '0' && LA8_0 <= '9')) ) {
							alt8=1;
						}

						switch (alt8) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							break loop8;
						}
					}

					}
					break;
				case 3 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:815:5: ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )*
					{
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:815:5: ( DIGIT )+
					int cnt9=0;
					loop9:
					while (true) {
						int alt9=2;
						int LA9_0 = input.LA(1);
						if ( ((LA9_0 >= '0' && LA9_0 <= '9')) ) {
							alt9=1;
						}

						switch (alt9) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							if ( cnt9 >= 1 ) break loop9;
							EarlyExitException eee = new EarlyExitException(9, input);
							throw eee;
						}
						cnt9++;
					}

					if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:815:22: ( '-' | '+' )?
					int alt10=2;
					int LA10_0 = input.LA(1);
					if ( (LA10_0=='+'||LA10_0=='-') ) {
						alt10=1;
					}
					switch (alt10) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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

					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:815:33: ( DIGIT )*
					loop11:
					while (true) {
						int alt11=2;
						int LA11_0 = input.LA(1);
						if ( ((LA11_0 >= '0' && LA11_0 <= '9')) ) {
							alt11=1;
						}

						switch (alt11) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							break loop11;
						}
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:819:3: ( ( DIGIT )+ PERIOD ( DIGIT )+ | PERIOD ( DIGIT )+ )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( ((LA16_0 >= '0' && LA16_0 <= '9')) ) {
				alt16=1;
			}
			else if ( (LA16_0=='.') ) {
				alt16=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:819:5: ( DIGIT )+ PERIOD ( DIGIT )+
					{
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:819:5: ( DIGIT )+
					int cnt13=0;
					loop13:
					while (true) {
						int alt13=2;
						int LA13_0 = input.LA(1);
						if ( ((LA13_0 >= '0' && LA13_0 <= '9')) ) {
							alt13=1;
						}

						switch (alt13) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							if ( cnt13 >= 1 ) break loop13;
							EarlyExitException eee = new EarlyExitException(13, input);
							throw eee;
						}
						cnt13++;
					}

					mPERIOD(); 

					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:819:19: ( DIGIT )+
					int cnt14=0;
					loop14:
					while (true) {
						int alt14=2;
						int LA14_0 = input.LA(1);
						if ( ((LA14_0 >= '0' && LA14_0 <= '9')) ) {
							alt14=1;
						}

						switch (alt14) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							if ( cnt14 >= 1 ) break loop14;
							EarlyExitException eee = new EarlyExitException(14, input);
							throw eee;
						}
						cnt14++;
					}

					}
					break;
				case 2 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:820:5: PERIOD ( DIGIT )+
					{
					mPERIOD(); 

					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:820:12: ( DIGIT )+
					int cnt15=0;
					loop15:
					while (true) {
						int alt15=2;
						int LA15_0 = input.LA(1);
						if ( ((LA15_0 >= '0' && LA15_0 <= '9')) ) {
							alt15=1;
						}

						switch (alt15) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
							if ( cnt15 >= 1 ) break loop15;
							EarlyExitException eee = new EarlyExitException(15, input);
							throw eee;
						}
						cnt15++;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:824:3: ( PLUS INTEGER )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:824:5: PLUS INTEGER
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:828:3: ( MINUS INTEGER )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:828:5: MINUS INTEGER
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:832:3: ( PLUS DOUBLE )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:832:5: PLUS DOUBLE
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:836:3: ( MINUS DOUBLE )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:836:5: MINUS DOUBLE
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:840:3: ( PLUS DECIMAL )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:840:5: PLUS DECIMAL
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:844:3: ( MINUS DECIMAL )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:844:5: MINUS DECIMAL
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:848:3: ( ALPHA ( CHAR )* )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:848:5: ALPHA ( CHAR )*
			{
			mALPHA(); 

			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:848:11: ( CHAR )*
			loop17:
			while (true) {
				int alt17=2;
				int LA17_0 = input.LA(1);
				if ( ((LA17_0 >= '-' && LA17_0 <= '.')||(LA17_0 >= '0' && LA17_0 <= '9')||(LA17_0 >= 'A' && LA17_0 <= 'Z')||LA17_0=='_'||(LA17_0 >= 'a' && LA17_0 <= 'z')||(LA17_0 >= '\u00C0' && LA17_0 <= '\u00D6')||(LA17_0 >= '\u00D8' && LA17_0 <= '\u00F6')||(LA17_0 >= '\u00F8' && LA17_0 <= '\u02FF')||(LA17_0 >= '\u0370' && LA17_0 <= '\u037D')||(LA17_0 >= '\u037F' && LA17_0 <= '\u1FFF')||(LA17_0 >= '\u200C' && LA17_0 <= '\u200D')||(LA17_0 >= '\u2070' && LA17_0 <= '\u218F')||(LA17_0 >= '\u2C00' && LA17_0 <= '\u2FEF')||(LA17_0 >= '\u3001' && LA17_0 <= '\uD7FF')||(LA17_0 >= '\uF900' && LA17_0 <= '\uFDCF')||(LA17_0 >= '\uFDF0' && LA17_0 <= '\uFFFD')) ) {
					alt17=1;
				}

				switch (alt17) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
	// $ANTLR end "VARNAME"

	// $ANTLR start "ECHAR"
	public final void mECHAR() throws RecognitionException {
		try {
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:852:3: ( '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:852:5: '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' )
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:16: ( ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )* )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:18: ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )*
			{
			mALPHA(); 

			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:855:24: ( ALPHANUM | PLUS | MINUS | PERIOD )*
			loop18:
			while (true) {
				int alt18=2;
				int LA18_0 = input.LA(1);
				if ( (LA18_0=='+'||(LA18_0 >= '-' && LA18_0 <= '.')||(LA18_0 >= '0' && LA18_0 <= '9')||(LA18_0 >= 'A' && LA18_0 <= 'Z')||(LA18_0 >= 'a' && LA18_0 <= 'z')||(LA18_0 >= '\u00C0' && LA18_0 <= '\u00D6')||(LA18_0 >= '\u00D8' && LA18_0 <= '\u00F6')||(LA18_0 >= '\u00F8' && LA18_0 <= '\u02FF')||(LA18_0 >= '\u0370' && LA18_0 <= '\u037D')||(LA18_0 >= '\u037F' && LA18_0 <= '\u1FFF')||(LA18_0 >= '\u200C' && LA18_0 <= '\u200D')||(LA18_0 >= '\u2070' && LA18_0 <= '\u218F')||(LA18_0 >= '\u2C00' && LA18_0 <= '\u2FEF')||(LA18_0 >= '\u3001' && LA18_0 <= '\uD7FF')||(LA18_0 >= '\uF900' && LA18_0 <= '\uFDCF')||(LA18_0 >= '\uFDF0' && LA18_0 <= '\uFFFD')) ) {
					alt18=1;
				}

				switch (alt18) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					break loop18;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:857:18: ( ( ALPHANUM | UNDERSCORE | MINUS | COLON | PERIOD | HASH | QUESTION | SLASH ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:859:18: ( ( ALPHA | UNDERSCORE ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:861:17: ( ( ID_START | DIGIT ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:863:12: ( ID_START ( ID_CORE )* )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:863:14: ID_START ( ID_CORE )*
			{
			mID_START(); 

			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:863:23: ( ID_CORE )*
			loop19:
			while (true) {
				int alt19=2;
				int LA19_0 = input.LA(1);
				if ( ((LA19_0 >= '0' && LA19_0 <= '9')||(LA19_0 >= 'A' && LA19_0 <= 'Z')||LA19_0=='_'||(LA19_0 >= 'a' && LA19_0 <= 'z')||(LA19_0 >= '\u00C0' && LA19_0 <= '\u00D6')||(LA19_0 >= '\u00D8' && LA19_0 <= '\u00F6')||(LA19_0 >= '\u00F8' && LA19_0 <= '\u02FF')||(LA19_0 >= '\u0370' && LA19_0 <= '\u037D')||(LA19_0 >= '\u037F' && LA19_0 <= '\u1FFF')||(LA19_0 >= '\u200C' && LA19_0 <= '\u200D')||(LA19_0 >= '\u2070' && LA19_0 <= '\u218F')||(LA19_0 >= '\u2C00' && LA19_0 <= '\u2FEF')||(LA19_0 >= '\u3001' && LA19_0 <= '\uD7FF')||(LA19_0 >= '\uF900' && LA19_0 <= '\uFDCF')||(LA19_0 >= '\uFDF0' && LA19_0 <= '\uFFFD')) ) {
					alt19=1;
				}

				switch (alt19) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					break loop19;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:865:25: ( ( ALPHA | UNDERSCORE ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:867:19: ( ( NAME_START_CHAR | DIGIT | UNDERSCORE | MINUS | PERIOD | HASH | QUESTION | SLASH | PERCENT | EQUALS | SEMI ) )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:870:3: ( NAME_START_CHAR ( NAME_CHAR )* )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:870:5: NAME_START_CHAR ( NAME_CHAR )*
			{
			mNAME_START_CHAR(); 

			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:870:21: ( NAME_CHAR )*
			loop20:
			while (true) {
				int alt20=2;
				int LA20_0 = input.LA(1);
				if ( (LA20_0=='#'||LA20_0=='%'||(LA20_0 >= '-' && LA20_0 <= '9')||LA20_0==';'||LA20_0=='='||LA20_0=='?'||(LA20_0 >= 'A' && LA20_0 <= 'Z')||LA20_0=='_'||(LA20_0 >= 'a' && LA20_0 <= 'z')||(LA20_0 >= '\u00C0' && LA20_0 <= '\u00D6')||(LA20_0 >= '\u00D8' && LA20_0 <= '\u00F6')||(LA20_0 >= '\u00F8' && LA20_0 <= '\u02FF')||(LA20_0 >= '\u0370' && LA20_0 <= '\u037D')||(LA20_0 >= '\u037F' && LA20_0 <= '\u1FFF')||(LA20_0 >= '\u200C' && LA20_0 <= '\u200D')||(LA20_0 >= '\u2070' && LA20_0 <= '\u218F')||(LA20_0 >= '\u2C00' && LA20_0 <= '\u2FEF')||(LA20_0 >= '\u3001' && LA20_0 <= '\uD7FF')||(LA20_0 >= '\uF900' && LA20_0 <= '\uFDCF')||(LA20_0 >= '\uFDF0' && LA20_0 <= '\uFFFD')) ) {
					alt20=1;
				}

				switch (alt20) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					break loop20;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:874:3: ( ( NAME_CHAR | LCR_BRACKET | RCR_BRACKET | HASH | SLASH )* )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:874:5: ( NAME_CHAR | LCR_BRACKET | RCR_BRACKET | HASH | SLASH )*
			{
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:874:5: ( NAME_CHAR | LCR_BRACKET | RCR_BRACKET | HASH | SLASH )*
			loop21:
			while (true) {
				int alt21=2;
				int LA21_0 = input.LA(1);
				if ( (LA21_0=='#'||LA21_0=='%'||(LA21_0 >= '-' && LA21_0 <= '9')||LA21_0==';'||LA21_0=='='||LA21_0=='?'||(LA21_0 >= 'A' && LA21_0 <= 'Z')||LA21_0=='_'||(LA21_0 >= 'a' && LA21_0 <= '{')||LA21_0=='}'||(LA21_0 >= '\u00C0' && LA21_0 <= '\u00D6')||(LA21_0 >= '\u00D8' && LA21_0 <= '\u00F6')||(LA21_0 >= '\u00F8' && LA21_0 <= '\u02FF')||(LA21_0 >= '\u0370' && LA21_0 <= '\u037D')||(LA21_0 >= '\u037F' && LA21_0 <= '\u1FFF')||(LA21_0 >= '\u200C' && LA21_0 <= '\u200D')||(LA21_0 >= '\u2070' && LA21_0 <= '\u218F')||(LA21_0 >= '\u2C00' && LA21_0 <= '\u2FEF')||(LA21_0 >= '\u3001' && LA21_0 <= '\uD7FF')||(LA21_0 >= '\uF900' && LA21_0 <= '\uFDCF')||(LA21_0 >= '\uFDF0' && LA21_0 <= '\uFFFD')) ) {
					alt21=1;
				}

				switch (alt21) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					break loop21;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:878:3: ( NAME_START_CHAR ( NAME_CHAR )* COLON )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:878:5: NAME_START_CHAR ( NAME_CHAR )* COLON
			{
			mNAME_START_CHAR(); 

			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:878:21: ( NAME_CHAR )*
			loop22:
			while (true) {
				int alt22=2;
				int LA22_0 = input.LA(1);
				if ( (LA22_0=='#'||LA22_0=='%'||(LA22_0 >= '-' && LA22_0 <= '9')||LA22_0==';'||LA22_0=='='||LA22_0=='?'||(LA22_0 >= 'A' && LA22_0 <= 'Z')||LA22_0=='_'||(LA22_0 >= 'a' && LA22_0 <= 'z')||(LA22_0 >= '\u00C0' && LA22_0 <= '\u00D6')||(LA22_0 >= '\u00D8' && LA22_0 <= '\u00F6')||(LA22_0 >= '\u00F8' && LA22_0 <= '\u02FF')||(LA22_0 >= '\u0370' && LA22_0 <= '\u037D')||(LA22_0 >= '\u037F' && LA22_0 <= '\u1FFF')||(LA22_0 >= '\u200C' && LA22_0 <= '\u200D')||(LA22_0 >= '\u2070' && LA22_0 <= '\u218F')||(LA22_0 >= '\u2C00' && LA22_0 <= '\u2FEF')||(LA22_0 >= '\u3001' && LA22_0 <= '\uD7FF')||(LA22_0 >= '\uF900' && LA22_0 <= '\uFDCF')||(LA22_0 >= '\uFDF0' && LA22_0 <= '\uFFFD')) ) {
					alt22=1;
				}

				switch (alt22) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					break loop22;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:882:3: ( ( NCNAME )? COLON NCNAME_EXT )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:882:5: ( NCNAME )? COLON NCNAME_EXT
			{
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:882:5: ( NCNAME )?
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( ((LA23_0 >= 'A' && LA23_0 <= 'Z')||LA23_0=='_'||(LA23_0 >= 'a' && LA23_0 <= 'z')||(LA23_0 >= '\u00C0' && LA23_0 <= '\u00D6')||(LA23_0 >= '\u00D8' && LA23_0 <= '\u00F6')||(LA23_0 >= '\u00F8' && LA23_0 <= '\u02FF')||(LA23_0 >= '\u0370' && LA23_0 <= '\u037D')||(LA23_0 >= '\u037F' && LA23_0 <= '\u1FFF')||(LA23_0 >= '\u200C' && LA23_0 <= '\u200D')||(LA23_0 >= '\u2070' && LA23_0 <= '\u218F')||(LA23_0 >= '\u2C00' && LA23_0 <= '\u2FEF')||(LA23_0 >= '\u3001' && LA23_0 <= '\uD7FF')||(LA23_0 >= '\uF900' && LA23_0 <= '\uFDCF')||(LA23_0 >= '\uFDF0' && LA23_0 <= '\uFFFD')) ) {
				alt23=1;
			}
			switch (alt23) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:882:5: NCNAME
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:886:3: ( '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\'' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:886:5: '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\''
			{
			match('\''); 
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:886:10: ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop24:
			while (true) {
				int alt24=3;
				int LA24_0 = input.LA(1);
				if ( ((LA24_0 >= '\u0000' && LA24_0 <= '\t')||(LA24_0 >= '\u000B' && LA24_0 <= '\f')||(LA24_0 >= '\u000E' && LA24_0 <= '&')||(LA24_0 >= '(' && LA24_0 <= '[')||(LA24_0 >= ']' && LA24_0 <= '\uFFFF')) ) {
					alt24=1;
				}
				else if ( (LA24_0=='\\') ) {
					alt24=2;
				}
				else if ( (LA24_0=='\'') ) {
					alt24=3;
				}

				switch (alt24) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:886:40: ~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:886:87: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop24;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:898:3: ( '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:898:5: '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"'
			{
			match('\"'); 
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:898:10: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop25:
			while (true) {
				int alt25=3;
				int LA25_0 = input.LA(1);
				if ( ((LA25_0 >= '\u0000' && LA25_0 <= '\t')||(LA25_0 >= '\u000B' && LA25_0 <= '\f')||(LA25_0 >= '\u000E' && LA25_0 <= '!')||(LA25_0 >= '#' && LA25_0 <= '[')||(LA25_0 >= ']' && LA25_0 <= '\uFFFF')) ) {
					alt25=1;
				}
				else if ( (LA25_0=='\\') ) {
					alt25=2;
				}
				else if ( (LA25_0=='\"') ) {
					alt25=3;
				}

				switch (alt25) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:898:40: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:898:87: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop25;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:902:3: ( '<' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '>' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:902:5: '<' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '>'
			{
			match('<'); 
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:902:9: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop26:
			while (true) {
				int alt26=3;
				int LA26_0 = input.LA(1);
				if ( (LA26_0=='>') ) {
					alt26=3;
				}
				else if ( ((LA26_0 >= '\u0000' && LA26_0 <= '\t')||(LA26_0 >= '\u000B' && LA26_0 <= '\f')||(LA26_0 >= '\u000E' && LA26_0 <= '!')||(LA26_0 >= '#' && LA26_0 <= '=')||(LA26_0 >= '?' && LA26_0 <= '[')||(LA26_0 >= ']' && LA26_0 <= '\uFFFF')) ) {
					alt26=1;
				}
				else if ( (LA26_0=='\\') ) {
					alt26=2;
				}

				switch (alt26) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:902:39: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:902:86: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop26;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:906:3: ( '{' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '}' )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:906:5: '{' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '}'
			{
			match('{'); 
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:906:9: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
			loop27:
			while (true) {
				int alt27=3;
				int LA27_0 = input.LA(1);
				if ( (LA27_0=='}') ) {
					alt27=3;
				}
				else if ( ((LA27_0 >= '\u0000' && LA27_0 <= '\t')||(LA27_0 >= '\u000B' && LA27_0 <= '\f')||(LA27_0 >= '\u000E' && LA27_0 <= '!')||(LA27_0 >= '#' && LA27_0 <= '[')||(LA27_0 >= ']' && LA27_0 <= '|')||(LA27_0 >= '~' && LA27_0 <= '\uFFFF')) ) {
					alt27=1;
				}
				else if ( (LA27_0=='\\') ) {
					alt27=2;
				}

				switch (alt27) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:906:39: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:906:86: ECHAR
					{
					mECHAR(); 

					}
					break;

				default :
					break loop27;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:910:3: ( SCHEMA COLON DOUBLE_SLASH ( URI_PATH )* )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:910:5: SCHEMA COLON DOUBLE_SLASH ( URI_PATH )*
			{
			mSCHEMA(); 

			mCOLON(); 

			mDOUBLE_SLASH(); 

			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:910:31: ( URI_PATH )*
			loop28:
			while (true) {
				int alt28=2;
				int LA28_0 = input.LA(1);
				if ( (LA28_0=='#'||(LA28_0 >= '-' && LA28_0 <= ':')||LA28_0=='?'||(LA28_0 >= 'A' && LA28_0 <= 'Z')||LA28_0=='_'||(LA28_0 >= 'a' && LA28_0 <= 'z')||(LA28_0 >= '\u00C0' && LA28_0 <= '\u00D6')||(LA28_0 >= '\u00D8' && LA28_0 <= '\u00F6')||(LA28_0 >= '\u00F8' && LA28_0 <= '\u02FF')||(LA28_0 >= '\u0370' && LA28_0 <= '\u037D')||(LA28_0 >= '\u037F' && LA28_0 <= '\u1FFF')||(LA28_0 >= '\u200C' && LA28_0 <= '\u200D')||(LA28_0 >= '\u2070' && LA28_0 <= '\u218F')||(LA28_0 >= '\u2C00' && LA28_0 <= '\u2FEF')||(LA28_0 >= '\u3001' && LA28_0 <= '\uD7FF')||(LA28_0 >= '\uF900' && LA28_0 <= '\uFDCF')||(LA28_0 >= '\uFDF0' && LA28_0 <= '\uFFFD')) ) {
					alt28=1;
				}

				switch (alt28) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:
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
					break loop28;
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
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			{
			// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
			int cnt30=0;
			loop30:
			while (true) {
				int alt30=4;
				switch ( input.LA(1) ) {
				case ' ':
					{
					alt30=1;
					}
					break;
				case '\t':
					{
					alt30=2;
					}
					break;
				case '\n':
				case '\r':
					{
					alt30=3;
					}
					break;
				}
				switch (alt30) {
				case 1 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:6: ' '
					{
					match(' '); 
					}
					break;
				case 2 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:10: '\\t'
					{
					match('\t'); 
					}
					break;
				case 3 :
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:15: ( '\\n' | '\\r' ( '\\n' ) )
					{
					// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:15: ( '\\n' | '\\r' ( '\\n' ) )
					int alt29=2;
					int LA29_0 = input.LA(1);
					if ( (LA29_0=='\n') ) {
						alt29=1;
					}
					else if ( (LA29_0=='\r') ) {
						alt29=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 29, 0, input);
						throw nvae;
					}

					switch (alt29) {
						case 1 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:16: '\\n'
							{
							match('\n'); 
							}
							break;
						case 2 :
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:21: '\\r' ( '\\n' )
							{
							match('\r'); 
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:25: ( '\\n' )
							// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:913:26: '\\n'
							{
							match('\n'); 
							}

							}
							break;

					}

					}
					break;

				default :
					if ( cnt30 >= 1 ) break loop30;
					EarlyExitException eee = new EarlyExitException(30, input);
					throw eee;
				}
				cnt30++;
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
		// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:8: ( T__77 | BASE | PREFIX | FALSE | TRUE | REFERENCE | LTSIGN | RTSIGN | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LCR_BRACKET | RCR_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | NCNAME | NCNAME_EXT | NAMESPACE | PREFIXED_NAME | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_WITH_BRACKET | STRING_WITH_CURLY_BRACKET | STRING_URI | WS )
		int alt31=62;
		alt31 = dfa31.predict(input);
		switch (alt31) {
			case 1 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:10: T__77
				{
				mT__77(); 

				}
				break;
			case 2 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:16: BASE
				{
				mBASE(); 

				}
				break;
			case 3 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:21: PREFIX
				{
				mPREFIX(); 

				}
				break;
			case 4 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:28: FALSE
				{
				mFALSE(); 

				}
				break;
			case 5 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:34: TRUE
				{
				mTRUE(); 

				}
				break;
			case 6 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:39: REFERENCE
				{
				mREFERENCE(); 

				}
				break;
			case 7 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:49: LTSIGN
				{
				mLTSIGN(); 

				}
				break;
			case 8 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:56: RTSIGN
				{
				mRTSIGN(); 

				}
				break;
			case 9 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:63: SEMI
				{
				mSEMI(); 

				}
				break;
			case 10 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:68: PERIOD
				{
				mPERIOD(); 

				}
				break;
			case 11 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:75: COMMA
				{
				mCOMMA(); 

				}
				break;
			case 12 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:81: LSQ_BRACKET
				{
				mLSQ_BRACKET(); 

				}
				break;
			case 13 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:93: RSQ_BRACKET
				{
				mRSQ_BRACKET(); 

				}
				break;
			case 14 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:105: LCR_BRACKET
				{
				mLCR_BRACKET(); 

				}
				break;
			case 15 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:117: RCR_BRACKET
				{
				mRCR_BRACKET(); 

				}
				break;
			case 16 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:129: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 17 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:136: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 18 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:143: QUESTION
				{
				mQUESTION(); 

				}
				break;
			case 19 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:152: DOLLAR
				{
				mDOLLAR(); 

				}
				break;
			case 20 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:159: QUOTE_DOUBLE
				{
				mQUOTE_DOUBLE(); 

				}
				break;
			case 21 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:172: QUOTE_SINGLE
				{
				mQUOTE_SINGLE(); 

				}
				break;
			case 22 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:185: APOSTROPHE
				{
				mAPOSTROPHE(); 

				}
				break;
			case 23 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:196: UNDERSCORE
				{
				mUNDERSCORE(); 

				}
				break;
			case 24 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:207: MINUS
				{
				mMINUS(); 

				}
				break;
			case 25 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:213: ASTERISK
				{
				mASTERISK(); 

				}
				break;
			case 26 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:222: AMPERSAND
				{
				mAMPERSAND(); 

				}
				break;
			case 27 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:232: AT
				{
				mAT(); 

				}
				break;
			case 28 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:235: EXCLAMATION
				{
				mEXCLAMATION(); 

				}
				break;
			case 29 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:247: HASH
				{
				mHASH(); 

				}
				break;
			case 30 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:252: PERCENT
				{
				mPERCENT(); 

				}
				break;
			case 31 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:260: PLUS
				{
				mPLUS(); 

				}
				break;
			case 32 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:265: EQUALS
				{
				mEQUALS(); 

				}
				break;
			case 33 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:272: COLON
				{
				mCOLON(); 

				}
				break;
			case 34 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:278: LESS
				{
				mLESS(); 

				}
				break;
			case 35 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:283: GREATER
				{
				mGREATER(); 

				}
				break;
			case 36 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:291: SLASH
				{
				mSLASH(); 

				}
				break;
			case 37 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:297: DOUBLE_SLASH
				{
				mDOUBLE_SLASH(); 

				}
				break;
			case 38 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:310: BACKSLASH
				{
				mBACKSLASH(); 

				}
				break;
			case 39 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:320: BLANK
				{
				mBLANK(); 

				}
				break;
			case 40 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:326: BLANK_PREFIX
				{
				mBLANK_PREFIX(); 

				}
				break;
			case 41 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:339: TILDE
				{
				mTILDE(); 

				}
				break;
			case 42 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:345: CARET
				{
				mCARET(); 

				}
				break;
			case 43 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:351: INTEGER
				{
				mINTEGER(); 

				}
				break;
			case 44 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:359: DOUBLE
				{
				mDOUBLE(); 

				}
				break;
			case 45 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:366: DECIMAL
				{
				mDECIMAL(); 

				}
				break;
			case 46 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:374: INTEGER_POSITIVE
				{
				mINTEGER_POSITIVE(); 

				}
				break;
			case 47 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:391: INTEGER_NEGATIVE
				{
				mINTEGER_NEGATIVE(); 

				}
				break;
			case 48 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:408: DOUBLE_POSITIVE
				{
				mDOUBLE_POSITIVE(); 

				}
				break;
			case 49 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:424: DOUBLE_NEGATIVE
				{
				mDOUBLE_NEGATIVE(); 

				}
				break;
			case 50 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:440: DECIMAL_POSITIVE
				{
				mDECIMAL_POSITIVE(); 

				}
				break;
			case 51 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:457: DECIMAL_NEGATIVE
				{
				mDECIMAL_NEGATIVE(); 

				}
				break;
			case 52 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:474: VARNAME
				{
				mVARNAME(); 

				}
				break;
			case 53 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:482: NCNAME
				{
				mNCNAME(); 

				}
				break;
			case 54 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:489: NCNAME_EXT
				{
				mNCNAME_EXT(); 

				}
				break;
			case 55 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:500: NAMESPACE
				{
				mNAMESPACE(); 

				}
				break;
			case 56 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:510: PREFIXED_NAME
				{
				mPREFIXED_NAME(); 

				}
				break;
			case 57 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:524: STRING_WITH_QUOTE
				{
				mSTRING_WITH_QUOTE(); 

				}
				break;
			case 58 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:542: STRING_WITH_QUOTE_DOUBLE
				{
				mSTRING_WITH_QUOTE_DOUBLE(); 

				}
				break;
			case 59 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:567: STRING_WITH_BRACKET
				{
				mSTRING_WITH_BRACKET(); 

				}
				break;
			case 60 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:587: STRING_WITH_CURLY_BRACKET
				{
				mSTRING_WITH_CURLY_BRACKET(); 

				}
				break;
			case 61 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:613: STRING_URI
				{
				mSTRING_URI(); 

				}
				break;
			case 62 :
				// obdalib-core/src/main/java/it/unibz/krdb/obda/parser/TurtleOBDA.g:1:624: WS
				{
				mWS(); 

				}
				break;

		}
	}


	protected DFA12 dfa12 = new DFA12(this);
	protected DFA31 dfa31 = new DFA31(this);
	static final String DFA12_eotS =
		"\5\uffff";
	static final String DFA12_eofS =
		"\5\uffff";
	static final String DFA12_minS =
		"\2\56\3\uffff";
	static final String DFA12_maxS =
		"\1\71\1\145\3\uffff";
	static final String DFA12_acceptS =
		"\2\uffff\1\2\1\1\1\3";
	static final String DFA12_specialS =
		"\5\uffff}>";
	static final String[] DFA12_transitionS = {
			"\1\2\1\uffff\12\1",
			"\1\3\1\uffff\12\1\13\uffff\1\4\37\uffff\1\4",
			"",
			"",
			""
	};

	static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
	static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
	static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
	static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
	static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
	static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
	static final short[][] DFA12_transition;

	static {
		int numStates = DFA12_transitionS.length;
		DFA12_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
		}
	}

	protected class DFA12 extends DFA {

		public DFA12(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 12;
			this.eot = DFA12_eot;
			this.eof = DFA12_eof;
			this.min = DFA12_min;
			this.max = DFA12_max;
			this.accept = DFA12_accept;
			this.special = DFA12_special;
			this.transition = DFA12_transition;
		}
		@Override
		public String getDescription() {
			return "812:1: DOUBLE : ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )* | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )* | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? ( DIGIT )* );";
		}
	}

	static final String DFA31_eotS =
		"\1\47\1\51\4\60\1\65\1\67\1\72\1\74\1\75\1\uffff\1\100\1\uffff\1\101\1"+
		"\105\2\uffff\1\106\1\uffff\1\107\1\uffff\1\112\1\113\4\uffff\1\116\1\117"+
		"\1\120\1\123\1\124\1\uffff\1\127\2\uffff\1\130\1\60\3\uffff\1\60\1\133"+
		"\1\135\1\60\1\uffff\1\60\1\uffff\3\60\5\uffff\1\143\4\uffff\1\145\3\uffff"+
		"\1\47\1\uffff\1\47\4\uffff\1\146\2\uffff\1\147\1\47\3\uffff\1\153\4\uffff"+
		"\1\157\2\uffff\1\47\1\164\1\uffff\1\135\1\uffff\1\125\4\60\1\uffff\1\164"+
		"\3\uffff\1\47\1\u0080\1\u0082\3\uffff\1\u0084\1\uffff\1\145\3\164\1\uffff"+
		"\1\125\1\u0088\2\60\1\u008b\2\164\1\u0082\3\u0080\1\uffff\1\u0080\1\uffff"+
		"\1\u0084\1\uffff\2\164\1\125\1\uffff\1\60\1\u0091\1\uffff\4\u0080\1\u0092"+
		"\2\uffff";
	static final String DFA31_eofS =
		"\u0093\uffff";
	static final String DFA31_minS =
		"\1\11\5\43\1\136\2\0\2\43\1\uffff\1\135\1\uffff\1\0\1\43\2\uffff\1\43"+
		"\1\uffff\1\0\1\uffff\2\43\4\uffff\2\43\1\56\2\43\1\uffff\1\43\2\uffff"+
		"\2\43\3\uffff\4\43\1\uffff\1\43\1\uffff\3\43\5\uffff\1\0\4\uffff\1\43"+
		"\3\uffff\1\0\1\uffff\1\0\4\uffff\1\43\2\uffff\1\43\1\60\3\uffff\1\56\1"+
		"\60\3\uffff\1\43\2\uffff\1\60\1\43\1\uffff\1\43\1\uffff\1\57\4\43\1\uffff"+
		"\1\43\3\uffff\1\60\2\43\1\uffff\1\60\1\uffff\1\60\1\uffff\4\43\1\uffff"+
		"\13\43\1\uffff\1\43\1\uffff\1\60\1\uffff\3\43\1\uffff\2\43\1\uffff\5\43"+
		"\2\uffff";
	static final String DFA31_maxS =
		"\6\ufffd\1\136\2\uffff\2\ufffd\1\uffff\1\135\1\uffff\1\uffff\1\ufffd\2"+
		"\uffff\1\ufffd\1\uffff\1\uffff\1\uffff\2\ufffd\4\uffff\2\ufffd\1\71\2"+
		"\ufffd\1\uffff\1\ufffd\2\uffff\2\ufffd\3\uffff\4\ufffd\1\uffff\1\ufffd"+
		"\1\uffff\3\ufffd\5\uffff\1\uffff\4\uffff\1\ufffd\3\uffff\1\uffff\1\uffff"+
		"\1\uffff\4\uffff\1\ufffd\2\uffff\1\ufffd\1\71\3\uffff\1\145\1\71\3\uffff"+
		"\1\ufffd\2\uffff\1\145\1\ufffd\1\uffff\1\ufffd\1\uffff\1\57\4\ufffd\1"+
		"\uffff\1\ufffd\3\uffff\1\145\2\ufffd\1\uffff\1\145\1\uffff\1\145\1\uffff"+
		"\4\ufffd\1\uffff\13\ufffd\1\uffff\1\ufffd\1\uffff\1\145\1\uffff\3\ufffd"+
		"\1\uffff\2\ufffd\1\uffff\5\ufffd\2\uffff";
	static final String DFA31_acceptS =
		"\13\uffff\1\13\1\uffff\1\15\2\uffff\1\20\1\21\1\uffff\1\23\1\uffff\1\26"+
		"\2\uffff\1\31\1\32\1\33\1\34\5\uffff\1\43\1\uffff\1\46\1\51\2\uffff\1"+
		"\66\1\76\1\1\4\uffff\1\75\1\uffff\1\64\3\uffff\1\6\1\52\1\7\1\42\1\73"+
		"\1\uffff\1\24\1\72\1\11\1\12\1\uffff\1\47\1\14\1\16\1\uffff\1\74\1\uffff"+
		"\1\17\1\22\1\25\1\71\1\uffff\1\27\1\30\2\uffff\1\35\1\36\1\37\2\uffff"+
		"\1\40\1\41\1\70\1\uffff\1\44\1\53\2\uffff\1\65\1\uffff\1\67\5\uffff\1"+
		"\10\1\uffff\1\55\1\50\1\57\3\uffff\1\56\1\uffff\1\60\1\uffff\1\45\4\uffff"+
		"\1\54\13\uffff\1\61\1\uffff\1\63\1\uffff\1\62\3\uffff\1\2\2\uffff\1\5"+
		"\5\uffff\1\4\1\3";
	static final String DFA31_specialS =
		"\7\uffff\1\4\1\5\5\uffff\1\2\5\uffff\1\0\44\uffff\1\1\10\uffff\1\6\1\uffff"+
		"\1\3\116\uffff}>";
	static final String[] DFA31_transitionS = {
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
			"\1\47\1\uffff\1\47\7\uffff\1\162\2\47\12\163\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47"+
			"\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff"+
			"\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff"+
			"\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\1\125\1\uffff\1\125\7\uffff\15\125\1\uffff\1\125\1\uffff\1\125\1\uffff"+
			"\1\125\1\uffff\32\125\4\uffff\1\125\1\uffff\33\125\1\uffff\1\125\102"+
			"\uffff\27\125\1\uffff\37\125\1\uffff\u0208\125\160\uffff\16\125\1\uffff"+
			"\u1c81\125\14\uffff\2\125\142\uffff\u0120\125\u0a70\uffff\u03f0\125\21"+
			"\uffff\ua7ff\125\u2100\uffff\u04d0\125\40\uffff\u020e\125",
			"",
			"\1\165",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\4\52\1\166\25\52\4\uffff\1\55\1\uffff"+
			"\4\52\1\166\25\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\5\52\1\167\24\52\4\uffff\1\55\1\uffff"+
			"\5\52\1\167\24\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\22\52\1\170\7\52\4\uffff\1\55\1\uffff"+
			"\22\52\1\170\7\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\4\52\1\171\25\52\4\uffff\1\55\1\uffff"+
			"\4\52\1\171\25\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff"+
			"\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120"+
			"\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff"+
			"\u020e\52",
			"",
			"\1\47\1\uffff\1\47\7\uffff\1\172\2\47\12\173\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47"+
			"\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff"+
			"\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff"+
			"\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"",
			"",
			"\12\174\13\uffff\1\175\37\uffff\1\175",
			"\1\47\1\uffff\1\47\7\uffff\1\176\2\47\12\177\1\uffff\1\47\1\uffff\1"+
			"\47\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47"+
			"\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff"+
			"\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff"+
			"\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\152\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\4\47\1\u0081\25\47\4\uffff\1\47\1\uffff\4\47\1\u0081\26"+
			"\47\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff"+
			"\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff"+
			"\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\12\u0083\13\uffff\1\155\37\uffff\1\155",
			"",
			"\12\156\13\uffff\1\155\37\uffff\1\155",
			"",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\160\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\4\47\1\161\25\47\4\uffff\1\47\1\uffff\4\47\1\161\26\47"+
			"\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff"+
			"\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff"+
			"\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\1\u0085\2\47\12\u0086\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1"+
			"\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1"+
			"\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47"+
			"\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\163\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\163\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\1\u0087\11\uffff\15\u0087\1\56\4\uffff\1\u0087\1\uffff\32\u0087\4\uffff"+
			"\1\u0087\1\uffff\32\u0087\105\uffff\27\u0087\1\uffff\37\u0087\1\uffff"+
			"\u0208\u0087\160\uffff\16\u0087\1\uffff\u1c81\u0087\14\uffff\2\u0087"+
			"\142\uffff\u0120\u0087\u0a70\uffff\u03f0\u0087\21\uffff\ua7ff\u0087\u2100"+
			"\uffff\u04d0\u0087\40\uffff\u020e\u0087",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\10\52\1\u0089\21\52\4\uffff\1\55\1\uffff"+
			"\10\52\1\u0089\21\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52"+
			"\1\uffff\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142"+
			"\uffff\u0120\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0"+
			"\52\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\4\52\1\u008a\25\52\4\uffff\1\55\1\uffff"+
			"\4\52\1\u008a\25\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1"+
			"\uffff\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff"+
			"\u0120\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52"+
			"\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\173\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\173\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\174\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\4\47\1\175\25\47\4\uffff\1\47\1\uffff\4\47\1\175\26\47"+
			"\1\uffff\1\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff"+
			"\16\47\1\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff"+
			"\u03f0\47\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\1\u008c\2\47\12\u008d\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1"+
			"\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1"+
			"\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47"+
			"\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\177\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\177\1\uffff\1\47\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102\uffff"+
			"\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81\47"+
			"\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\1\47\1\uffff\1\47\7\uffff\1\u008e\2\47\12\u008f\1\uffff\1\47\1\uffff"+
			"\1\47\1\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1"+
			"\47\102\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1"+
			"\uffff\u1c81\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47"+
			"\21\uffff\ua7ff\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"",
			"\12\u0083\13\uffff\1\155\37\uffff\1\155",
			"",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\u0086\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\u0086\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\u0087\11\uffff\15\u0087\1\56\4\uffff\1\u0087\1\uffff\32\u0087\4\uffff"+
			"\1\u0087\1\uffff\32\u0087\105\uffff\27\u0087\1\uffff\37\u0087\1\uffff"+
			"\u0208\u0087\160\uffff\16\u0087\1\uffff\u1c81\u0087\14\uffff\2\u0087"+
			"\142\uffff\u0120\u0087\u0a70\uffff\u03f0\u0087\21\uffff\ua7ff\u0087\u2100"+
			"\uffff\u04d0\u0087\40\uffff\u020e\u0087",
			"",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\27\52\1\u0090\2\52\4\uffff\1\55\1\uffff"+
			"\27\52\1\u0090\2\52\1\47\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1"+
			"\uffff\u0208\52\160\uffff\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff"+
			"\u0120\52\u0a70\uffff\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52"+
			"\40\uffff\u020e\52",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\u008d\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\u008d\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\u008f\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\47\1\uffff\1\47\7\uffff\3\47\12\u008f\1\uffff\1\47\1\uffff\1\47\1"+
			"\uffff\1\47\1\uffff\32\47\4\uffff\1\47\1\uffff\33\47\1\uffff\1\47\102"+
			"\uffff\27\47\1\uffff\37\47\1\uffff\u0208\47\160\uffff\16\47\1\uffff\u1c81"+
			"\47\14\uffff\2\47\142\uffff\u0120\47\u0a70\uffff\u03f0\47\21\uffff\ua7ff"+
			"\47\u2100\uffff\u04d0\47\40\uffff\u020e\47",
			"\1\53\1\uffff\1\53\5\uffff\1\56\1\uffff\2\52\1\53\12\52\1\54\1\53\1"+
			"\uffff\1\53\1\uffff\1\53\1\uffff\32\52\4\uffff\1\55\1\uffff\32\52\1\47"+
			"\1\uffff\1\47\102\uffff\27\52\1\uffff\37\52\1\uffff\u0208\52\160\uffff"+
			"\16\52\1\uffff\u1c81\52\14\uffff\2\52\142\uffff\u0120\52\u0a70\uffff"+
			"\u03f0\52\21\uffff\ua7ff\52\u2100\uffff\u04d0\52\40\uffff\u020e\52",
			"",
			""
	};

	static final short[] DFA31_eot = DFA.unpackEncodedString(DFA31_eotS);
	static final short[] DFA31_eof = DFA.unpackEncodedString(DFA31_eofS);
	static final char[] DFA31_min = DFA.unpackEncodedStringToUnsignedChars(DFA31_minS);
	static final char[] DFA31_max = DFA.unpackEncodedStringToUnsignedChars(DFA31_maxS);
	static final short[] DFA31_accept = DFA.unpackEncodedString(DFA31_acceptS);
	static final short[] DFA31_special = DFA.unpackEncodedString(DFA31_specialS);
	static final short[][] DFA31_transition;

	static {
		int numStates = DFA31_transitionS.length;
		DFA31_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA31_transition[i] = DFA.unpackEncodedString(DFA31_transitionS[i]);
		}
	}

	protected class DFA31 extends DFA {

		public DFA31(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 31;
			this.eot = DFA31_eot;
			this.eof = DFA31_eof;
			this.min = DFA31_min;
			this.max = DFA31_max;
			this.accept = DFA31_accept;
			this.special = DFA31_special;
			this.transition = DFA31_transition;
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
						int LA31_20 = input.LA(1);
						s = -1;
						if ( ((LA31_20 >= '\u0000' && LA31_20 <= '\t')||(LA31_20 >= '\u000B' && LA31_20 <= '\f')||(LA31_20 >= '\u000E' && LA31_20 <= '\uFFFF')) ) {s = 72;}
						else s = 71;
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA31_57 = input.LA(1);
						s = -1;
						if ( ((LA31_57 >= '\u0000' && LA31_57 <= '\t')||(LA31_57 >= '\u000B' && LA31_57 <= '\f')||(LA31_57 >= '\u000E' && LA31_57 <= '\uFFFF')) ) {s = 59;}
						else s = 99;
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA31_14 = input.LA(1);
						s = -1;
						if ( (LA31_14=='}') ) {s = 66;}
						else if ( ((LA31_14 >= '\u0000' && LA31_14 <= '\t')||(LA31_14 >= '\u000B' && LA31_14 <= '\f')||(LA31_14 >= '\u000E' && LA31_14 <= '!')||LA31_14=='$'||(LA31_14 >= '&' && LA31_14 <= ',')||LA31_14==':'||LA31_14=='<'||LA31_14=='>'||LA31_14=='@'||(LA31_14 >= '[' && LA31_14 <= '^')||LA31_14=='`'||LA31_14=='|'||(LA31_14 >= '~' && LA31_14 <= '\u00BF')||LA31_14=='\u00D7'||LA31_14=='\u00F7'||(LA31_14 >= '\u0300' && LA31_14 <= '\u036F')||LA31_14=='\u037E'||(LA31_14 >= '\u2000' && LA31_14 <= '\u200B')||(LA31_14 >= '\u200E' && LA31_14 <= '\u206F')||(LA31_14 >= '\u2190' && LA31_14 <= '\u2BFF')||(LA31_14 >= '\u2FF0' && LA31_14 <= '\u3000')||(LA31_14 >= '\uD800' && LA31_14 <= '\uF8FF')||(LA31_14 >= '\uFDD0' && LA31_14 <= '\uFDEF')||(LA31_14 >= '\uFFFE' && LA31_14 <= '\uFFFF')) ) {s = 67;}
						else if ( (LA31_14=='#'||LA31_14=='%'||(LA31_14 >= '-' && LA31_14 <= '9')||LA31_14==';'||LA31_14=='='||LA31_14=='?'||(LA31_14 >= 'A' && LA31_14 <= 'Z')||LA31_14=='_'||(LA31_14 >= 'a' && LA31_14 <= '{')||(LA31_14 >= '\u00C0' && LA31_14 <= '\u00D6')||(LA31_14 >= '\u00D8' && LA31_14 <= '\u00F6')||(LA31_14 >= '\u00F8' && LA31_14 <= '\u02FF')||(LA31_14 >= '\u0370' && LA31_14 <= '\u037D')||(LA31_14 >= '\u037F' && LA31_14 <= '\u1FFF')||(LA31_14 >= '\u200C' && LA31_14 <= '\u200D')||(LA31_14 >= '\u2070' && LA31_14 <= '\u218F')||(LA31_14 >= '\u2C00' && LA31_14 <= '\u2FEF')||(LA31_14 >= '\u3001' && LA31_14 <= '\uD7FF')||(LA31_14 >= '\uF900' && LA31_14 <= '\uFDCF')||(LA31_14 >= '\uFDF0' && LA31_14 <= '\uFFFD')) ) {s = 68;}
						else s = 65;
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA31_68 = input.LA(1);
						s = -1;
						if ( (LA31_68=='}') ) {s = 66;}
						else if ( (LA31_68=='#'||LA31_68=='%'||(LA31_68 >= '-' && LA31_68 <= '9')||LA31_68==';'||LA31_68=='='||LA31_68=='?'||(LA31_68 >= 'A' && LA31_68 <= 'Z')||LA31_68=='_'||(LA31_68 >= 'a' && LA31_68 <= '{')||(LA31_68 >= '\u00C0' && LA31_68 <= '\u00D6')||(LA31_68 >= '\u00D8' && LA31_68 <= '\u00F6')||(LA31_68 >= '\u00F8' && LA31_68 <= '\u02FF')||(LA31_68 >= '\u0370' && LA31_68 <= '\u037D')||(LA31_68 >= '\u037F' && LA31_68 <= '\u1FFF')||(LA31_68 >= '\u200C' && LA31_68 <= '\u200D')||(LA31_68 >= '\u2070' && LA31_68 <= '\u218F')||(LA31_68 >= '\u2C00' && LA31_68 <= '\u2FEF')||(LA31_68 >= '\u3001' && LA31_68 <= '\uD7FF')||(LA31_68 >= '\uF900' && LA31_68 <= '\uFDCF')||(LA31_68 >= '\uFDF0' && LA31_68 <= '\uFFFD')) ) {s = 68;}
						else if ( ((LA31_68 >= '\u0000' && LA31_68 <= '\t')||(LA31_68 >= '\u000B' && LA31_68 <= '\f')||(LA31_68 >= '\u000E' && LA31_68 <= '!')||LA31_68=='$'||(LA31_68 >= '&' && LA31_68 <= ',')||LA31_68==':'||LA31_68=='<'||LA31_68=='>'||LA31_68=='@'||(LA31_68 >= '[' && LA31_68 <= '^')||LA31_68=='`'||LA31_68=='|'||(LA31_68 >= '~' && LA31_68 <= '\u00BF')||LA31_68=='\u00D7'||LA31_68=='\u00F7'||(LA31_68 >= '\u0300' && LA31_68 <= '\u036F')||LA31_68=='\u037E'||(LA31_68 >= '\u2000' && LA31_68 <= '\u200B')||(LA31_68 >= '\u200E' && LA31_68 <= '\u206F')||(LA31_68 >= '\u2190' && LA31_68 <= '\u2BFF')||(LA31_68 >= '\u2FF0' && LA31_68 <= '\u3000')||(LA31_68 >= '\uD800' && LA31_68 <= '\uF8FF')||(LA31_68 >= '\uFDD0' && LA31_68 <= '\uFDEF')||(LA31_68 >= '\uFFFE' && LA31_68 <= '\uFFFF')) ) {s = 67;}
						else s = 39;
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA31_7 = input.LA(1);
						s = -1;
						if ( (LA31_7=='\"') ) {s = 54;}
						else if ( ((LA31_7 >= '\u0000' && LA31_7 <= '\t')||(LA31_7 >= '\u000B' && LA31_7 <= '\f')||(LA31_7 >= '\u000E' && LA31_7 <= '!')||(LA31_7 >= '#' && LA31_7 <= '\uFFFF')) ) {s = 56;}
						else s = 55;
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA31_8 = input.LA(1);
						s = -1;
						if ( (LA31_8=='>') ) {s = 57;}
						else if ( ((LA31_8 >= '\u0000' && LA31_8 <= '\t')||(LA31_8 >= '\u000B' && LA31_8 <= '\f')||(LA31_8 >= '\u000E' && LA31_8 <= '=')||(LA31_8 >= '?' && LA31_8 <= '\uFFFF')) ) {s = 59;}
						else s = 58;
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA31_66 = input.LA(1);
						s = -1;
						if ( (LA31_66=='}') ) {s = 66;}
						else if ( (LA31_66=='#'||LA31_66=='%'||(LA31_66 >= '-' && LA31_66 <= '9')||LA31_66==';'||LA31_66=='='||LA31_66=='?'||(LA31_66 >= 'A' && LA31_66 <= 'Z')||LA31_66=='_'||(LA31_66 >= 'a' && LA31_66 <= '{')||(LA31_66 >= '\u00C0' && LA31_66 <= '\u00D6')||(LA31_66 >= '\u00D8' && LA31_66 <= '\u00F6')||(LA31_66 >= '\u00F8' && LA31_66 <= '\u02FF')||(LA31_66 >= '\u0370' && LA31_66 <= '\u037D')||(LA31_66 >= '\u037F' && LA31_66 <= '\u1FFF')||(LA31_66 >= '\u200C' && LA31_66 <= '\u200D')||(LA31_66 >= '\u2070' && LA31_66 <= '\u218F')||(LA31_66 >= '\u2C00' && LA31_66 <= '\u2FEF')||(LA31_66 >= '\u3001' && LA31_66 <= '\uD7FF')||(LA31_66 >= '\uF900' && LA31_66 <= '\uFDCF')||(LA31_66 >= '\uFDF0' && LA31_66 <= '\uFFFD')) ) {s = 68;}
						else if ( ((LA31_66 >= '\u0000' && LA31_66 <= '\t')||(LA31_66 >= '\u000B' && LA31_66 <= '\f')||(LA31_66 >= '\u000E' && LA31_66 <= '!')||LA31_66=='$'||(LA31_66 >= '&' && LA31_66 <= ',')||LA31_66==':'||LA31_66=='<'||LA31_66=='>'||LA31_66=='@'||(LA31_66 >= '[' && LA31_66 <= '^')||LA31_66=='`'||LA31_66=='|'||(LA31_66 >= '~' && LA31_66 <= '\u00BF')||LA31_66=='\u00D7'||LA31_66=='\u00F7'||(LA31_66 >= '\u0300' && LA31_66 <= '\u036F')||LA31_66=='\u037E'||(LA31_66 >= '\u2000' && LA31_66 <= '\u200B')||(LA31_66 >= '\u200E' && LA31_66 <= '\u206F')||(LA31_66 >= '\u2190' && LA31_66 <= '\u2BFF')||(LA31_66 >= '\u2FF0' && LA31_66 <= '\u3000')||(LA31_66 >= '\uD800' && LA31_66 <= '\uF8FF')||(LA31_66 >= '\uFDD0' && LA31_66 <= '\uFDEF')||(LA31_66 >= '\uFFFE' && LA31_66 <= '\uFFFF')) ) {s = 67;}
						else s = 39;
						if ( s>=0 ) return s;
						break;
			}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 31, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
