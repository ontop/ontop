// $ANTLR 3.4 C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g 2012-03-22 15:31:49

package it.unibz.krdb.obda.parser;

import java.util.List;
import java.util.Vector;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class TurtleLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__67=67;
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
    public static final int ID_PLAIN=35;
    public static final int ID_START=36;
    public static final int INTEGER=37;
    public static final int INTEGER_NEGATIVE=38;
    public static final int INTEGER_POSITIVE=39;
    public static final int LESS=40;
    public static final int LPAREN=41;
    public static final int LSQ_BRACKET=42;
    public static final int MINUS=43;
    public static final int PERCENT=44;
    public static final int PERIOD=45;
    public static final int PLUS=46;
    public static final int PREFIX=47;
    public static final int QUESTION=48;
    public static final int QUOTE_DOUBLE=49;
    public static final int QUOTE_SINGLE=50;
    public static final int REFERENCE=51;
    public static final int RPAREN=52;
    public static final int RSQ_BRACKET=53;
    public static final int SCHEMA=54;
    public static final int SEMI=55;
    public static final int SLASH=56;
    public static final int STRING_PREFIX=57;
    public static final int STRING_URI=58;
    public static final int STRING_WITH_QUOTE=59;
    public static final int STRING_WITH_QUOTE_DOUBLE=60;
    public static final int TILDE=61;
    public static final int TRUE=62;
    public static final int UNDERSCORE=63;
    public static final int URI_PATH=64;
    public static final int VARNAME=65;
    public static final int WS=66;

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

    public TurtleLexer() {} 
    public TurtleLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public TurtleLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g"; }

    // $ANTLR start "T__67"
    public final void mT__67() throws RecognitionException {
        try {
            int _type = T__67;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:22:7: ( 'a' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:22:9: 'a'
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
    // $ANTLR end "T__67"

    // $ANTLR start "BASE"
    public final void mBASE() throws RecognitionException {
        try {
            int _type = BASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:357:5: ( ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:357:7: ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:359:7: ( ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:359:9: ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:361:6: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:361:8: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:363:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:363:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:365:10: ( '^^' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:365:16: '^^'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:366:5: ( ';' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:366:16: ';'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:367:7: ( '.' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:367:16: '.'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:368:6: ( ',' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:368:16: ','
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:369:12: ( '[' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:369:16: '['
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:370:12: ( ']' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:370:16: ']'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:371:7: ( '(' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:371:16: '('
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:372:7: ( ')' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:372:16: ')'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:373:9: ( '?' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:373:16: '?'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:374:7: ( '$' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:374:16: '$'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:375:13: ( '\"' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:375:16: '\"'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:376:13: ( '\\'' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:376:16: '\\''
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:377:11: ( '`' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:377:16: '`'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:378:11: ( '_' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:378:16: '_'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:379:6: ( '-' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:379:16: '-'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:380:9: ( '*' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:380:16: '*'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:381:10: ( '&' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:381:16: '&'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:382:3: ( '@' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:382:16: '@'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:383:12: ( '!' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:383:16: '!'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:384:5: ( '#' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:384:16: '#'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:385:8: ( '%' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:385:16: '%'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:386:5: ( '+' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:386:16: '+'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:387:7: ( '=' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:387:16: '='
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:388:6: ( ':' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:388:16: ':'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:389:5: ( '<' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:389:16: '<'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:390:8: ( '>' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:390:16: '>'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:391:6: ( '/' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:391:16: '/'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:392:13: ( '//' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:392:16: '//'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:393:10: ( '\\\\' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:393:16: '\\\\'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:394:6: ( '[]' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:394:15: '[]'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:395:13: ( '_:' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:395:16: '_:'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:396:6: ( '~' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:396:16: '~'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:397:6: ( '^' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:397:16: '^'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:400:3: ( 'a' .. 'z' | 'A' .. 'Z' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:405:3: ( '0' .. '9' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:409:3: ( ALPHA | DIGIT )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:414:3: ( ALPHANUM | UNDERSCORE | MINUS )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
            {
            if ( input.LA(1)=='-'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:420:3: ( ( DIGIT )+ )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:420:5: ( DIGIT )+
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:420:5: ( DIGIT )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= '0' && LA1_0 <= '9')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:424:3: ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? )
            int alt9=3;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:424:5: ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )?
                    {
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:424:5: ( DIGIT )+
                    int cnt2=0;
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0 >= '0' && LA2_0 <= '9')) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                                EarlyExitException eee =
                                    new EarlyExitException(2, input);
                                throw eee;
                        }
                        cnt2++;
                    } while (true);


                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:424:19: ( DIGIT )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                    } while (true);


                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:424:36: ( '-' | '+' )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0=='+'||LA4_0=='-') ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:425:5: PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
                    {
                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:425:12: ( DIGIT )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0 >= '0' && LA5_0 <= '9')) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                                EarlyExitException eee =
                                    new EarlyExitException(5, input);
                                throw eee;
                        }
                        cnt5++;
                    } while (true);


                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:425:29: ( '-' | '+' )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0=='+'||LA6_0=='-') ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:426:5: ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
                    {
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:426:5: ( DIGIT )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( ((LA7_0 >= '0' && LA7_0 <= '9')) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);


                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:426:22: ( '-' | '+' )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0=='+'||LA8_0=='-') ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:3: ( ( DIGIT )+ PERIOD ( DIGIT )+ | PERIOD ( DIGIT )+ )
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:5: ( DIGIT )+ PERIOD ( DIGIT )+
                    {
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:5: ( DIGIT )+
                    int cnt10=0;
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0 >= '0' && LA10_0 <= '9')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                                EarlyExitException eee =
                                    new EarlyExitException(10, input);
                                throw eee;
                        }
                        cnt10++;
                    } while (true);


                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:19: ( DIGIT )+
                    int cnt11=0;
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0 >= '0' && LA11_0 <= '9')) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                                EarlyExitException eee =
                                    new EarlyExitException(11, input);
                                throw eee;
                        }
                        cnt11++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:431:5: PERIOD ( DIGIT )+
                    {
                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:431:12: ( DIGIT )+
                    int cnt12=0;
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( ((LA12_0 >= '0' && LA12_0 <= '9')) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
                                EarlyExitException eee =
                                    new EarlyExitException(12, input);
                                throw eee;
                        }
                        cnt12++;
                    } while (true);


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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:435:3: ( PLUS INTEGER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:435:5: PLUS INTEGER
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:439:3: ( MINUS INTEGER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:439:5: MINUS INTEGER
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:443:3: ( PLUS DOUBLE )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:443:5: PLUS DOUBLE
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:447:3: ( MINUS DOUBLE )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:447:5: MINUS DOUBLE
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:451:3: ( PLUS DECIMAL )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:451:5: PLUS DECIMAL
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:455:3: ( MINUS DECIMAL )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:455:5: MINUS DECIMAL
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:459:3: ( ALPHA ( CHAR )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:459:5: ALPHA ( CHAR )*
            {
            mALPHA(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:459:11: ( CHAR )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='-'||(LA14_0 >= '0' && LA14_0 <= '9')||(LA14_0 >= 'A' && LA14_0 <= 'Z')||LA14_0=='_'||(LA14_0 >= 'a' && LA14_0 <= 'z')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
            	    {
            	    if ( input.LA(1)=='-'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
            } while (true);


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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:463:3: ( '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:463:5: '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:466:16: ( ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:466:18: ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )*
            {
            mALPHA(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:466:24: ( ALPHANUM | PLUS | MINUS | PERIOD )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0=='+'||(LA15_0 >= '-' && LA15_0 <= '.')||(LA15_0 >= '0' && LA15_0 <= '9')||(LA15_0 >= 'A' && LA15_0 <= 'Z')||(LA15_0 >= 'a' && LA15_0 <= 'z')) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            	    break loop15;
                }
            } while (true);


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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:468:18: ( ( ALPHANUM | UNDERSCORE | MINUS | COLON | PERIOD | HASH | QUESTION | SLASH ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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

    // $ANTLR start "ID_START"
    public final void mID_START() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:470:18: ( ( ALPHA | UNDERSCORE ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:472:17: ( ( ID_START | DIGIT ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:474:12: ( ID_START ( ID_CORE )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:474:14: ID_START ( ID_CORE )*
            {
            mID_START(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:474:23: ( ID_CORE )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( ((LA16_0 >= '0' && LA16_0 <= '9')||(LA16_0 >= 'A' && LA16_0 <= 'Z')||LA16_0=='_'||(LA16_0 >= 'a' && LA16_0 <= 'z')) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            	    break loop16;
                }
            } while (true);


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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:476:9: ( ID_START ( ID_CORE )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:476:11: ID_START ( ID_CORE )*
            {
            mID_START(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:476:20: ( ID_CORE )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( ((LA17_0 >= '0' && LA17_0 <= '9')||(LA17_0 >= 'A' && LA17_0 <= 'Z')||LA17_0=='_'||(LA17_0 >= 'a' && LA17_0 <= 'z')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            	    break loop17;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID_PLAIN"

    // $ANTLR start "STRING_WITH_QUOTE"
    public final void mSTRING_WITH_QUOTE() throws RecognitionException {
        try {
            int _type = STRING_WITH_QUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:479:3: ( '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\'' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:479:5: '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\''
            {
            match('\''); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:479:10: ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
            loop18:
            do {
                int alt18=3;
                int LA18_0 = input.LA(1);

                if ( ((LA18_0 >= '\u0000' && LA18_0 <= '\t')||(LA18_0 >= '\u000B' && LA18_0 <= '\f')||(LA18_0 >= '\u000E' && LA18_0 <= '&')||(LA18_0 >= '(' && LA18_0 <= '[')||(LA18_0 >= ']' && LA18_0 <= '\uFFFF')) ) {
                    alt18=1;
                }
                else if ( (LA18_0=='\\') ) {
                    alt18=2;
                }
                else if ( (LA18_0=='\'') ) {
                    alt18=3;
                }


                switch (alt18) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:479:40: ~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' )
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
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:479:87: ECHAR
            	    {
            	    mECHAR(); 


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);


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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:483:3: ( '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:483:5: '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"'
            {
            match('\"'); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:483:10: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
            loop19:
            do {
                int alt19=3;
                int LA19_0 = input.LA(1);

                if ( ((LA19_0 >= '\u0000' && LA19_0 <= '\t')||(LA19_0 >= '\u000B' && LA19_0 <= '\f')||(LA19_0 >= '\u000E' && LA19_0 <= '!')||(LA19_0 >= '#' && LA19_0 <= '[')||(LA19_0 >= ']' && LA19_0 <= '\uFFFF')) ) {
                    alt19=1;
                }
                else if ( (LA19_0=='\\') ) {
                    alt19=2;
                }
                else if ( (LA19_0=='\"') ) {
                    alt19=3;
                }


                switch (alt19) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:483:40: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:483:87: ECHAR
            	    {
            	    mECHAR(); 


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);


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

    // $ANTLR start "STRING_URI"
    public final void mSTRING_URI() throws RecognitionException {
        try {
            int _type = STRING_URI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:487:3: ( SCHEMA COLON DOUBLE_SLASH ( URI_PATH )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:487:5: SCHEMA COLON DOUBLE_SLASH ( URI_PATH )*
            {
            mSCHEMA(); 


            mCOLON(); 


            mDOUBLE_SLASH(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:487:31: ( URI_PATH )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0=='#'||(LA20_0 >= '-' && LA20_0 <= ':')||LA20_0=='?'||(LA20_0 >= 'A' && LA20_0 <= 'Z')||LA20_0=='_'||(LA20_0 >= 'a' && LA20_0 <= 'z')) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:
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
            	    break loop20;
                }
            } while (true);


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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:491:3: ( ID COLON )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:491:5: ID COLON
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            int cnt22=0;
            loop22:
            do {
                int alt22=4;
                switch ( input.LA(1) ) {
                case ' ':
                    {
                    alt22=1;
                    }
                    break;
                case '\t':
                    {
                    alt22=2;
                    }
                    break;
                case '\n':
                case '\r':
                    {
                    alt22=3;
                    }
                    break;

                }

                switch (alt22) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:6: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:10: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    {
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    int alt21=2;
            	    int LA21_0 = input.LA(1);

            	    if ( (LA21_0=='\n') ) {
            	        alt21=1;
            	    }
            	    else if ( (LA21_0=='\r') ) {
            	        alt21=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 21, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt21) {
            	        case 1 :
            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:16: '\\n'
            	            {
            	            match('\n'); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:21: '\\r' ( '\\n' )
            	            {
            	            match('\r'); 

            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:25: ( '\\n' )
            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:26: '\\n'
            	            {
            	            match('\n'); 

            	            }


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt22 >= 1 ) break loop22;
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);


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

    public void mTokens() throws RecognitionException {
        // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:8: ( T__67 | BASE | PREFIX | FALSE | TRUE | REFERENCE | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | ID_PLAIN | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_URI | STRING_PREFIX | WS )
        int alt23=54;
        alt23 = dfa23.predict(input);
        switch (alt23) {
            case 1 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:10: T__67
                {
                mT__67(); 


                }
                break;
            case 2 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:16: BASE
                {
                mBASE(); 


                }
                break;
            case 3 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:21: PREFIX
                {
                mPREFIX(); 


                }
                break;
            case 4 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:28: FALSE
                {
                mFALSE(); 


                }
                break;
            case 5 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:34: TRUE
                {
                mTRUE(); 


                }
                break;
            case 6 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:39: REFERENCE
                {
                mREFERENCE(); 


                }
                break;
            case 7 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:49: SEMI
                {
                mSEMI(); 


                }
                break;
            case 8 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:54: PERIOD
                {
                mPERIOD(); 


                }
                break;
            case 9 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:61: COMMA
                {
                mCOMMA(); 


                }
                break;
            case 10 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:67: LSQ_BRACKET
                {
                mLSQ_BRACKET(); 


                }
                break;
            case 11 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:79: RSQ_BRACKET
                {
                mRSQ_BRACKET(); 


                }
                break;
            case 12 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:91: LPAREN
                {
                mLPAREN(); 


                }
                break;
            case 13 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:98: RPAREN
                {
                mRPAREN(); 


                }
                break;
            case 14 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:105: QUESTION
                {
                mQUESTION(); 


                }
                break;
            case 15 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:114: DOLLAR
                {
                mDOLLAR(); 


                }
                break;
            case 16 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:121: QUOTE_DOUBLE
                {
                mQUOTE_DOUBLE(); 


                }
                break;
            case 17 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:134: QUOTE_SINGLE
                {
                mQUOTE_SINGLE(); 


                }
                break;
            case 18 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:147: APOSTROPHE
                {
                mAPOSTROPHE(); 


                }
                break;
            case 19 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:158: UNDERSCORE
                {
                mUNDERSCORE(); 


                }
                break;
            case 20 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:169: MINUS
                {
                mMINUS(); 


                }
                break;
            case 21 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:175: ASTERISK
                {
                mASTERISK(); 


                }
                break;
            case 22 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:184: AMPERSAND
                {
                mAMPERSAND(); 


                }
                break;
            case 23 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:194: AT
                {
                mAT(); 


                }
                break;
            case 24 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:197: EXCLAMATION
                {
                mEXCLAMATION(); 


                }
                break;
            case 25 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:209: HASH
                {
                mHASH(); 


                }
                break;
            case 26 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:214: PERCENT
                {
                mPERCENT(); 


                }
                break;
            case 27 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:222: PLUS
                {
                mPLUS(); 


                }
                break;
            case 28 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:227: EQUALS
                {
                mEQUALS(); 


                }
                break;
            case 29 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:234: COLON
                {
                mCOLON(); 


                }
                break;
            case 30 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:240: LESS
                {
                mLESS(); 


                }
                break;
            case 31 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:245: GREATER
                {
                mGREATER(); 


                }
                break;
            case 32 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:253: SLASH
                {
                mSLASH(); 


                }
                break;
            case 33 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:259: DOUBLE_SLASH
                {
                mDOUBLE_SLASH(); 


                }
                break;
            case 34 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:272: BACKSLASH
                {
                mBACKSLASH(); 


                }
                break;
            case 35 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:282: BLANK
                {
                mBLANK(); 


                }
                break;
            case 36 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:288: BLANK_PREFIX
                {
                mBLANK_PREFIX(); 


                }
                break;
            case 37 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:301: TILDE
                {
                mTILDE(); 


                }
                break;
            case 38 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:307: CARET
                {
                mCARET(); 


                }
                break;
            case 39 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:313: INTEGER
                {
                mINTEGER(); 


                }
                break;
            case 40 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:321: DOUBLE
                {
                mDOUBLE(); 


                }
                break;
            case 41 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:328: DECIMAL
                {
                mDECIMAL(); 


                }
                break;
            case 42 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:336: INTEGER_POSITIVE
                {
                mINTEGER_POSITIVE(); 


                }
                break;
            case 43 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:353: INTEGER_NEGATIVE
                {
                mINTEGER_NEGATIVE(); 


                }
                break;
            case 44 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:370: DOUBLE_POSITIVE
                {
                mDOUBLE_POSITIVE(); 


                }
                break;
            case 45 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:386: DOUBLE_NEGATIVE
                {
                mDOUBLE_NEGATIVE(); 


                }
                break;
            case 46 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:402: DECIMAL_POSITIVE
                {
                mDECIMAL_POSITIVE(); 


                }
                break;
            case 47 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:419: DECIMAL_NEGATIVE
                {
                mDECIMAL_NEGATIVE(); 


                }
                break;
            case 48 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:436: VARNAME
                {
                mVARNAME(); 


                }
                break;
            case 49 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:444: ID_PLAIN
                {
                mID_PLAIN(); 


                }
                break;
            case 50 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:453: STRING_WITH_QUOTE
                {
                mSTRING_WITH_QUOTE(); 


                }
                break;
            case 51 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:471: STRING_WITH_QUOTE_DOUBLE
                {
                mSTRING_WITH_QUOTE_DOUBLE(); 


                }
                break;
            case 52 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:496: STRING_URI
                {
                mSTRING_URI(); 


                }
                break;
            case 53 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:507: STRING_PREFIX
                {
                mSTRING_PREFIX(); 


                }
                break;
            case 54 :
                // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:1:521: WS
                {
                mWS(); 


                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    protected DFA23 dfa23 = new DFA23(this);
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

    class DFA9 extends DFA {

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
        public String getDescription() {
            return "423:1: DOUBLE : ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? );";
        }
    }
    static final String DFA23_eotS =
        "\1\uffff\1\46\4\55\1\62\1\uffff\1\63\1\uffff\1\66\5\uffff\1\67\1"+
        "\71\1\uffff\1\74\1\76\6\uffff\1\101\4\uffff\1\105\2\uffff\1\106"+
        "\1\55\2\uffff\3\55\1\uffff\1\111\1\55\1\uffff\3\55\3\uffff\1\116"+
        "\10\uffff\1\120\1\uffff\1\121\2\uffff\1\125\7\uffff\4\55\6\uffff"+
        "\1\137\3\uffff\1\141\1\116\1\142\2\55\1\145\1\137\1\uffff\1\141"+
        "\2\uffff\1\55\1\147\1\uffff\1\150\2\uffff";
    static final String DFA23_eofS =
        "\151\uffff";
    static final String DFA23_minS =
        "\1\11\5\53\1\136\1\uffff\1\60\1\uffff\1\135\5\uffff\2\0\1\uffff"+
        "\1\60\1\56\6\uffff\1\56\4\uffff\1\57\2\uffff\1\56\1\53\2\uffff\2"+
        "\53\1\60\1\uffff\1\57\1\53\1\uffff\3\53\3\uffff\1\60\10\uffff\1"+
        "\60\1\uffff\1\56\1\60\1\uffff\1\56\1\60\3\uffff\1\60\2\uffff\4\53"+
        "\4\uffff\1\60\1\uffff\1\60\1\uffff\1\60\1\uffff\2\60\4\53\1\60\1"+
        "\uffff\1\60\2\uffff\2\53\1\uffff\1\53\2\uffff";
    static final String DFA23_maxS =
        "\1\176\5\172\1\136\1\uffff\1\71\1\uffff\1\135\5\uffff\2\uffff\1"+
        "\uffff\1\172\1\71\6\uffff\1\71\4\uffff\1\57\2\uffff\1\145\1\172"+
        "\2\uffff\3\172\1\uffff\1\57\1\172\1\uffff\3\172\3\uffff\1\145\10"+
        "\uffff\1\172\1\uffff\1\145\1\71\1\uffff\1\145\1\71\3\uffff\1\145"+
        "\2\uffff\4\172\4\uffff\1\145\1\uffff\1\145\1\uffff\1\145\1\uffff"+
        "\2\145\4\172\1\145\1\uffff\1\145\2\uffff\2\172\1\uffff\1\172\2\uffff";
    static final String DFA23_acceptS =
        "\7\uffff\1\7\1\uffff\1\11\1\uffff\1\13\1\14\1\15\1\16\1\17\2\uffff"+
        "\1\22\2\uffff\1\25\1\26\1\27\1\30\1\31\1\32\1\uffff\1\34\1\35\1"+
        "\36\1\37\1\uffff\1\42\1\45\2\uffff\1\66\1\1\3\uffff\1\64\2\uffff"+
        "\1\60\3\uffff\1\6\1\46\1\10\1\uffff\1\43\1\12\1\20\1\63\1\21\1\62"+
        "\1\44\1\23\1\uffff\1\24\2\uffff\1\33\2\uffff\1\41\1\40\1\47\1\uffff"+
        "\1\50\1\65\4\uffff\1\51\1\44\1\61\1\53\1\uffff\1\55\1\uffff\1\52"+
        "\1\uffff\1\54\7\uffff\1\57\1\uffff\1\56\1\2\2\uffff\1\5\1\uffff"+
        "\1\4\1\3";
    static final String DFA23_specialS =
        "\20\uffff\1\0\1\1\127\uffff}>";
    static final String[] DFA23_transitionS = {
            "\2\45\2\uffff\1\45\22\uffff\1\45\1\30\1\20\1\31\1\17\1\32\1"+
            "\26\1\21\1\14\1\15\1\25\1\33\1\11\1\24\1\10\1\40\12\43\1\35"+
            "\1\7\1\36\1\34\1\37\1\16\1\27\1\44\1\2\3\44\1\4\11\44\1\3\3"+
            "\44\1\5\6\44\1\12\1\41\1\13\1\6\1\23\1\22\1\1\1\2\3\44\1\4\11"+
            "\44\1\3\3\44\1\5\6\44\3\uffff\1\42",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\32\47\4"+
            "\uffff\1\51\1\uffff\32\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\1\54\31"+
            "\47\4\uffff\1\51\1\uffff\1\54\31\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\21\47\1"+
            "\56\10\47\4\uffff\1\51\1\uffff\21\47\1\56\10\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\1\57\31"+
            "\47\4\uffff\1\51\1\uffff\1\57\31\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\21\47\1"+
            "\60\10\47\4\uffff\1\51\1\uffff\21\47\1\60\10\47",
            "\1\61",
            "",
            "\12\64",
            "",
            "\1\65",
            "",
            "",
            "",
            "",
            "",
            "\12\70\1\uffff\2\70\1\uffff\ufff2\70",
            "\12\72\1\uffff\2\72\1\uffff\ufff2\72",
            "",
            "\12\75\1\73\6\uffff\32\75\4\uffff\1\75\1\uffff\32\75",
            "\1\100\1\uffff\12\77",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\103\1\uffff\12\102",
            "",
            "",
            "",
            "",
            "\1\104",
            "",
            "",
            "\1\107\1\uffff\12\43\13\uffff\1\110\37\uffff\1\110",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\32\47\4"+
            "\uffff\1\51\1\uffff\32\47",
            "",
            "",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\32\47\4"+
            "\uffff\1\51\1\uffff\32\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\50\1\52\6\uffff\32\50\6"+
            "\uffff\32\50",
            "\12\51\1\111\6\uffff\32\51\4\uffff\1\51\1\uffff\32\51",
            "",
            "\1\52",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\22\47\1"+
            "\112\7\47\4\uffff\1\51\1\uffff\22\47\1\112\7\47",
            "",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\4\47\1\113"+
            "\25\47\4\uffff\1\51\1\uffff\4\47\1\113\25\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\13\47\1"+
            "\114\16\47\4\uffff\1\51\1\uffff\13\47\1\114\16\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\24\47\1"+
            "\115\5\47\4\uffff\1\51\1\uffff\24\47\1\115\5\47",
            "",
            "",
            "",
            "\12\64\13\uffff\1\110\37\uffff\1\110",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\75\1\111\6\uffff\32\75\4\uffff\1\75\1\uffff\32\75",
            "",
            "\1\122\1\uffff\12\77\13\uffff\1\123\37\uffff\1\123",
            "\12\124",
            "",
            "\1\126\1\uffff\12\102\13\uffff\1\127\37\uffff\1\127",
            "\12\130",
            "",
            "",
            "",
            "\12\131\13\uffff\1\110\37\uffff\1\110",
            "",
            "",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\4\47\1\132"+
            "\25\47\4\uffff\1\51\1\uffff\4\47\1\132\25\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\5\47\1\133"+
            "\24\47\4\uffff\1\51\1\uffff\5\47\1\133\24\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\22\47\1"+
            "\134\7\47\4\uffff\1\51\1\uffff\22\47\1\134\7\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\4\47\1\135"+
            "\25\47\4\uffff\1\51\1\uffff\4\47\1\135\25\47",
            "",
            "",
            "",
            "",
            "\12\136\13\uffff\1\123\37\uffff\1\123",
            "",
            "\12\124\13\uffff\1\123\37\uffff\1\123",
            "",
            "\12\140\13\uffff\1\127\37\uffff\1\127",
            "",
            "\12\130\13\uffff\1\127\37\uffff\1\127",
            "\12\131\13\uffff\1\110\37\uffff\1\110",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\32\47\4"+
            "\uffff\1\51\1\uffff\32\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\10\47\1"+
            "\143\21\47\4\uffff\1\51\1\uffff\10\47\1\143\21\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\4\47\1\144"+
            "\25\47\4\uffff\1\51\1\uffff\4\47\1\144\25\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\32\47\4"+
            "\uffff\1\51\1\uffff\32\47",
            "\12\136\13\uffff\1\123\37\uffff\1\123",
            "",
            "\12\140\13\uffff\1\127\37\uffff\1\127",
            "",
            "",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\27\47\1"+
            "\146\2\47\4\uffff\1\51\1\uffff\27\47\1\146\2\47",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\32\47\4"+
            "\uffff\1\51\1\uffff\32\47",
            "",
            "\1\52\1\uffff\1\50\1\52\1\uffff\12\47\1\53\6\uffff\32\47\4"+
            "\uffff\1\51\1\uffff\32\47",
            "",
            ""
    };

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__67 | BASE | PREFIX | FALSE | TRUE | REFERENCE | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | ID_PLAIN | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_URI | STRING_PREFIX | WS );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA23_16 = input.LA(1);

                        s = -1;
                        if ( ((LA23_16 >= '\u0000' && LA23_16 <= '\t')||(LA23_16 >= '\u000B' && LA23_16 <= '\f')||(LA23_16 >= '\u000E' && LA23_16 <= '\uFFFF')) ) {s = 56;}

                        else s = 55;

                        if ( s>=0 ) return s;
                        break;

                    case 1 : 
                        int LA23_17 = input.LA(1);

                        s = -1;
                        if ( ((LA23_17 >= '\u0000' && LA23_17 <= '\t')||(LA23_17 >= '\u000B' && LA23_17 <= '\f')||(LA23_17 >= '\u000E' && LA23_17 <= '\uFFFF')) ) {s = 58;}

                        else s = 57;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 23, _s, input);
            error(nvae);
            throw nvae;
        }

    }
 

}