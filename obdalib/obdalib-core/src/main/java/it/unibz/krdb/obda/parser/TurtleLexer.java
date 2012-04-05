// $ANTLR 3.4 C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g 2012-04-05 13:42:49

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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:426:5: ( ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:426:7: ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:428:7: ( ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:428:9: ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'X' | 'x' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:6: ( ( 'F' | 'STRING_WITH_QUOTE_DOUBLEf' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:8: ( 'F' | 'STRING_WITH_QUOTE_DOUBLEf' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:8: ( 'F' | 'STRING_WITH_QUOTE_DOUBLEf' )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='F') ) {
                alt1=1;
            }
            else if ( (LA1_0=='S') ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }
            switch (alt1) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:9: 'F'
                    {
                    match('F'); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:430:13: 'STRING_WITH_QUOTE_DOUBLEf'
                    {
                    match("STRING_WITH_QUOTE_DOUBLEf"); 



                    }
                    break;

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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:432:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:432:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:434:10: ( '^^' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:434:16: '^^'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:435:5: ( ';' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:435:16: ';'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:436:7: ( '.' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:436:16: '.'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:437:6: ( ',' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:437:16: ','
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:438:12: ( '[' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:438:16: '['
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:439:12: ( ']' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:439:16: ']'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:440:7: ( '(' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:440:16: '('
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:441:7: ( ')' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:441:16: ')'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:442:9: ( '?' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:442:16: '?'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:443:7: ( '$' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:443:16: '$'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:444:13: ( '\"' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:444:16: '\"'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:445:13: ( '\\'' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:445:16: '\\''
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:446:11: ( '`' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:446:16: '`'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:447:11: ( '_' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:447:16: '_'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:448:6: ( '-' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:448:16: '-'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:449:9: ( '*' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:449:16: '*'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:450:10: ( '&' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:450:16: '&'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:451:3: ( '@' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:451:16: '@'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:452:12: ( '!' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:452:16: '!'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:453:5: ( '#' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:453:16: '#'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:454:8: ( '%' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:454:16: '%'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:455:5: ( '+' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:455:16: '+'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:456:7: ( '=' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:456:16: '='
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:457:6: ( ':' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:457:16: ':'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:458:5: ( '<' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:458:16: '<'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:459:8: ( '>' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:459:16: '>'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:460:6: ( '/' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:460:16: '/'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:461:13: ( '//' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:461:16: '//'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:462:10: ( '\\\\' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:462:16: '\\\\'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:463:6: ( '[]' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:463:15: '[]'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:464:13: ( '_:' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:464:16: '_:'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:465:6: ( '~' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:465:16: '~'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:466:6: ( '^' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:466:16: '^'
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:469:3: ( 'a' .. 'z' | 'A' .. 'Z' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:474:3: ( '0' .. '9' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:478:3: ( ALPHA | DIGIT )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:483:3: ( ALPHANUM | UNDERSCORE | MINUS )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:489:3: ( ( DIGIT )+ )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:489:5: ( DIGIT )+
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:489:5: ( DIGIT )+
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:493:3: ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? )
            int alt10=3;
            alt10 = dfa10.predict(input);
            switch (alt10) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:493:5: ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )?
                    {
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:493:5: ( DIGIT )+
                    int cnt3=0;
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
                    	    if ( cnt3 >= 1 ) break loop3;
                                EarlyExitException eee =
                                    new EarlyExitException(3, input);
                                throw eee;
                        }
                        cnt3++;
                    } while (true);


                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:493:19: ( DIGIT )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                            alt4=1;
                        }


                        switch (alt4) {
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
                    	    break loop4;
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


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:493:36: ( '-' | '+' )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0=='+'||LA5_0=='-') ) {
                        alt5=1;
                    }
                    switch (alt5) {
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:5: PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
                    {
                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:12: ( DIGIT )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
                            alt6=1;
                        }


                        switch (alt6) {
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
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);


                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:494:29: ( '-' | '+' )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='+'||LA7_0=='-') ) {
                        alt7=1;
                    }
                    switch (alt7) {
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:495:5: ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )?
                    {
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:495:5: ( DIGIT )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0 >= '0' && LA8_0 <= '9')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
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
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);


                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:495:22: ( '-' | '+' )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='+'||LA9_0=='-') ) {
                        alt9=1;
                    }
                    switch (alt9) {
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:499:3: ( ( DIGIT )+ PERIOD ( DIGIT )+ | PERIOD ( DIGIT )+ )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( ((LA14_0 >= '0' && LA14_0 <= '9')) ) {
                alt14=1;
            }
            else if ( (LA14_0=='.') ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }
            switch (alt14) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:499:5: ( DIGIT )+ PERIOD ( DIGIT )+
                    {
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:499:5: ( DIGIT )+
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


                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:499:19: ( DIGIT )+
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
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:500:5: PERIOD ( DIGIT )+
                    {
                    mPERIOD(); 


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:500:12: ( DIGIT )+
                    int cnt13=0;
                    loop13:
                    do {
                        int alt13=2;
                        int LA13_0 = input.LA(1);

                        if ( ((LA13_0 >= '0' && LA13_0 <= '9')) ) {
                            alt13=1;
                        }


                        switch (alt13) {
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
                    	    if ( cnt13 >= 1 ) break loop13;
                                EarlyExitException eee =
                                    new EarlyExitException(13, input);
                                throw eee;
                        }
                        cnt13++;
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:504:3: ( PLUS INTEGER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:504:5: PLUS INTEGER
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:508:3: ( MINUS INTEGER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:508:5: MINUS INTEGER
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:512:3: ( PLUS DOUBLE )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:512:5: PLUS DOUBLE
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:516:3: ( MINUS DOUBLE )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:516:5: MINUS DOUBLE
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:520:3: ( PLUS DECIMAL )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:520:5: PLUS DECIMAL
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:524:3: ( MINUS DECIMAL )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:524:5: MINUS DECIMAL
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:528:3: ( ALPHA ( CHAR )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:528:5: ALPHA ( CHAR )*
            {
            mALPHA(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:528:11: ( CHAR )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0=='-'||(LA15_0 >= '0' && LA15_0 <= '9')||(LA15_0 >= 'A' && LA15_0 <= 'Z')||LA15_0=='_'||(LA15_0 >= 'a' && LA15_0 <= 'z')) ) {
                    alt15=1;
                }


                switch (alt15) {
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
            	    break loop15;
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:532:3: ( '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:532:5: '\\\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\\\' | '\"' | '\\'' )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:535:16: ( ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:535:18: ALPHA ( ALPHANUM | PLUS | MINUS | PERIOD )*
            {
            mALPHA(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:535:24: ( ALPHANUM | PLUS | MINUS | PERIOD )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0=='+'||(LA16_0 >= '-' && LA16_0 <= '.')||(LA16_0 >= '0' && LA16_0 <= '9')||(LA16_0 >= 'A' && LA16_0 <= 'Z')||(LA16_0 >= 'a' && LA16_0 <= 'z')) ) {
                    alt16=1;
                }


                switch (alt16) {
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
            	    break loop16;
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:537:18: ( ( ALPHANUM | UNDERSCORE | MINUS | COLON | PERIOD | HASH | QUESTION | SLASH ) )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:539:18: ( ( ALPHA | UNDERSCORE ) )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:541:17: ( ( ID_START | DIGIT ) )
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:543:12: ( ID_START ( ID_CORE )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:543:14: ID_START ( ID_CORE )*
            {
            mID_START(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:543:23: ( ID_CORE )*
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:545:9: ( ID_START ( ID_CORE )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:545:11: ID_START ( ID_CORE )*
            {
            mID_START(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:545:20: ( ID_CORE )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( ((LA18_0 >= '0' && LA18_0 <= '9')||(LA18_0 >= 'A' && LA18_0 <= 'Z')||LA18_0=='_'||(LA18_0 >= 'a' && LA18_0 <= 'z')) ) {
                    alt18=1;
                }


                switch (alt18) {
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
            	    break loop18;
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:548:3: ( '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\'' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:548:5: '\\'' ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\\''
            {
            match('\''); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:548:10: ( options {greedy=false; } :~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
            loop19:
            do {
                int alt19=3;
                int LA19_0 = input.LA(1);

                if ( ((LA19_0 >= '\u0000' && LA19_0 <= '\t')||(LA19_0 >= '\u000B' && LA19_0 <= '\f')||(LA19_0 >= '\u000E' && LA19_0 <= '&')||(LA19_0 >= '(' && LA19_0 <= '[')||(LA19_0 >= ']' && LA19_0 <= '\uFFFF')) ) {
                    alt19=1;
                }
                else if ( (LA19_0=='\\') ) {
                    alt19=2;
                }
                else if ( (LA19_0=='\'') ) {
                    alt19=3;
                }


                switch (alt19) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:548:40: ~ ( '\\u0027' | '\\u005C' | '\\u000A' | '\\u000D' )
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
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:548:87: ECHAR
            	    {
            	    mECHAR(); 


            	    }
            	    break;

            	default :
            	    break loop19;
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:552:3: ( '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"' )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:552:5: '\"' ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )* '\"'
            {
            match('\"'); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:552:10: ( options {greedy=false; } :~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' ) | ECHAR )*
            loop20:
            do {
                int alt20=3;
                int LA20_0 = input.LA(1);

                if ( ((LA20_0 >= '\u0000' && LA20_0 <= '\t')||(LA20_0 >= '\u000B' && LA20_0 <= '\f')||(LA20_0 >= '\u000E' && LA20_0 <= '!')||(LA20_0 >= '#' && LA20_0 <= '[')||(LA20_0 >= ']' && LA20_0 <= '\uFFFF')) ) {
                    alt20=1;
                }
                else if ( (LA20_0=='\\') ) {
                    alt20=2;
                }
                else if ( (LA20_0=='\"') ) {
                    alt20=3;
                }


                switch (alt20) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:552:40: ~ ( '\\u0022' | '\\u005C' | '\\u000A' | '\\u000D' )
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
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:552:87: ECHAR
            	    {
            	    mECHAR(); 


            	    }
            	    break;

            	default :
            	    break loop20;
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:556:3: ( SCHEMA COLON DOUBLE_SLASH ( URI_PATH )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:556:5: SCHEMA COLON DOUBLE_SLASH ( URI_PATH )*
            {
            mSCHEMA(); 


            mCOLON(); 


            mDOUBLE_SLASH(); 


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:556:31: ( URI_PATH )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0=='#'||(LA21_0 >= '-' && LA21_0 <= ':')||LA21_0=='?'||(LA21_0 >= 'A' && LA21_0 <= 'Z')||LA21_0=='_'||(LA21_0 >= 'a' && LA21_0 <= 'z')) ) {
                    alt21=1;
                }


                switch (alt21) {
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
            	    break loop21;
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:560:3: ( ID COLON )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:560:5: ID COLON
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
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            int cnt23=0;
            loop23:
            do {
                int alt23=4;
                switch ( input.LA(1) ) {
                case ' ':
                    {
                    alt23=1;
                    }
                    break;
                case '\t':
                    {
                    alt23=2;
                    }
                    break;
                case '\n':
                case '\r':
                    {
                    alt23=3;
                    }
                    break;

                }

                switch (alt23) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:6: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:10: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    {
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    int alt22=2;
            	    int LA22_0 = input.LA(1);

            	    if ( (LA22_0=='\n') ) {
            	        alt22=1;
            	    }
            	    else if ( (LA22_0=='\r') ) {
            	        alt22=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 22, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt22) {
            	        case 1 :
            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:16: '\\n'
            	            {
            	            match('\n'); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:21: '\\r' ( '\\n' )
            	            {
            	            match('\r'); 

            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:25: ( '\\n' )
            	            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:563:26: '\\n'
            	            {
            	            match('\n'); 

            	            }


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt23 >= 1 ) break loop23;
                        EarlyExitException eee =
                            new EarlyExitException(23, input);
                        throw eee;
                }
                cnt23++;
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
        int alt24=54;
        alt24 = dfa24.predict(input);
        switch (alt24) {
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


    protected DFA10 dfa10 = new DFA10(this);
    protected DFA24 dfa24 = new DFA24(this);
    static final String DFA10_eotS =
        "\5\uffff";
    static final String DFA10_eofS =
        "\5\uffff";
    static final String DFA10_minS =
        "\2\56\3\uffff";
    static final String DFA10_maxS =
        "\1\71\1\145\3\uffff";
    static final String DFA10_acceptS =
        "\2\uffff\1\2\1\1\1\3";
    static final String DFA10_specialS =
        "\5\uffff}>";
    static final String[] DFA10_transitionS = {
            "\1\2\1\uffff\12\1",
            "\1\3\1\uffff\12\1\13\uffff\1\4\37\uffff\1\4",
            "",
            "",
            ""
    };

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "492:1: DOUBLE : ( ( DIGIT )+ PERIOD ( DIGIT )* ( 'e' | 'E' ) ( '-' | '+' )? | PERIOD ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? | ( DIGIT )+ ( 'e' | 'E' ) ( '-' | '+' )? );";
        }
    }
    static final String DFA24_eotS =
        "\1\uffff\1\47\5\56\1\64\1\uffff\1\65\1\uffff\1\70\5\uffff\1\71\1"+
        "\73\1\uffff\1\76\1\100\6\uffff\1\103\4\uffff\1\107\2\uffff\1\110"+
        "\1\56\2\uffff\3\56\1\uffff\1\113\1\56\1\uffff\4\56\3\uffff\1\121"+
        "\10\uffff\1\123\1\uffff\1\124\2\uffff\1\130\7\uffff\5\56\6\uffff"+
        "\1\143\3\uffff\1\145\1\121\1\146\3\56\1\152\1\143\1\uffff\1\145"+
        "\2\uffff\1\56\1\154\1\56\1\uffff\1\156\1\uffff\1\56\1\uffff\26\56"+
        "\1\154";
    static final String DFA24_eofS =
        "\u0086\uffff";
    static final String DFA24_minS =
        "\1\11\6\53\1\136\1\uffff\1\60\1\uffff\1\135\5\uffff\2\0\1\uffff"+
        "\1\60\1\56\6\uffff\1\56\4\uffff\1\57\2\uffff\1\56\1\53\2\uffff\2"+
        "\53\1\60\1\uffff\1\57\1\53\1\uffff\4\53\3\uffff\1\60\10\uffff\1"+
        "\60\1\uffff\1\56\1\60\1\uffff\1\56\1\60\3\uffff\1\60\2\uffff\5\53"+
        "\4\uffff\1\60\1\uffff\1\60\1\uffff\1\60\1\uffff\2\60\5\53\1\60\1"+
        "\uffff\1\60\2\uffff\3\53\1\uffff\1\53\1\uffff\1\53\1\uffff\26\60"+
        "\1\55";
    static final String DFA24_maxS =
        "\1\176\6\172\1\136\1\uffff\1\71\1\uffff\1\135\5\uffff\2\uffff\1"+
        "\uffff\1\172\1\71\6\uffff\1\71\4\uffff\1\57\2\uffff\1\145\1\172"+
        "\2\uffff\3\172\1\uffff\1\57\1\172\1\uffff\4\172\3\uffff\1\145\10"+
        "\uffff\1\172\1\uffff\1\145\1\71\1\uffff\1\145\1\71\3\uffff\1\145"+
        "\2\uffff\5\172\4\uffff\1\145\1\uffff\1\145\1\uffff\1\145\1\uffff"+
        "\2\145\5\172\1\145\1\uffff\1\145\2\uffff\3\172\1\uffff\1\172\1\uffff"+
        "\1\172\1\uffff\27\172";
    static final String DFA24_acceptS =
        "\10\uffff\1\7\1\uffff\1\11\1\uffff\1\13\1\14\1\15\1\16\1\17\2\uffff"+
        "\1\22\2\uffff\1\25\1\26\1\27\1\30\1\31\1\32\1\uffff\1\34\1\35\1"+
        "\36\1\37\1\uffff\1\42\1\45\2\uffff\1\66\1\1\3\uffff\1\64\2\uffff"+
        "\1\60\4\uffff\1\6\1\46\1\10\1\uffff\1\43\1\12\1\20\1\63\1\21\1\62"+
        "\1\44\1\23\1\uffff\1\24\2\uffff\1\33\2\uffff\1\41\1\40\1\47\1\uffff"+
        "\1\50\1\65\5\uffff\1\51\1\44\1\61\1\53\1\uffff\1\55\1\uffff\1\52"+
        "\1\uffff\1\54\10\uffff\1\57\1\uffff\1\56\1\2\3\uffff\1\5\1\uffff"+
        "\1\4\1\uffff\1\3\27\uffff";
    static final String DFA24_specialS =
        "\21\uffff\1\1\1\0\163\uffff}>";
    static final String[] DFA24_transitionS = {
            "\2\46\2\uffff\1\46\22\uffff\1\46\1\31\1\21\1\32\1\20\1\33\1"+
            "\27\1\22\1\15\1\16\1\26\1\34\1\12\1\25\1\11\1\41\12\44\1\36"+
            "\1\10\1\37\1\35\1\40\1\17\1\30\1\45\1\2\3\45\1\4\11\45\1\3\2"+
            "\45\1\5\1\6\6\45\1\13\1\42\1\14\1\7\1\24\1\23\1\1\1\2\15\45"+
            "\1\3\3\45\1\6\6\45\3\uffff\1\43",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\1\55\31"+
            "\50\4\uffff\1\52\1\uffff\1\55\31\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\21\50\1"+
            "\57\10\50\4\uffff\1\52\1\uffff\21\50\1\57\10\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\1\60\31"+
            "\50\4\uffff\1\52\1\uffff\1\60\31\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\23\50\1"+
            "\61\6\50\4\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\21\50\1"+
            "\62\10\50\4\uffff\1\52\1\uffff\21\50\1\62\10\50",
            "\1\63",
            "",
            "\12\66",
            "",
            "\1\67",
            "",
            "",
            "",
            "",
            "",
            "\12\72\1\uffff\2\72\1\uffff\ufff2\72",
            "\12\74\1\uffff\2\74\1\uffff\ufff2\74",
            "",
            "\12\77\1\75\6\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "\1\102\1\uffff\12\101",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\105\1\uffff\12\104",
            "",
            "",
            "",
            "",
            "\1\106",
            "",
            "",
            "\1\111\1\uffff\12\44\13\uffff\1\112\37\uffff\1\112",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\52\1\uffff\32\50",
            "",
            "",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\51\1\53\6\uffff\32\51\6"+
            "\uffff\32\51",
            "\12\52\1\113\6\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\53",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\22\50\1"+
            "\114\7\50\4\uffff\1\52\1\uffff\22\50\1\114\7\50",
            "",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\4\50\1\115"+
            "\25\50\4\uffff\1\52\1\uffff\4\50\1\115\25\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\13\50\1"+
            "\116\16\50\4\uffff\1\52\1\uffff\13\50\1\116\16\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\21\50\1"+
            "\117\10\50\4\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\24\50\1"+
            "\120\5\50\4\uffff\1\52\1\uffff\24\50\1\120\5\50",
            "",
            "",
            "",
            "\12\66\13\uffff\1\112\37\uffff\1\112",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\77\1\113\6\uffff\32\77\4\uffff\1\77\1\uffff\32\77",
            "",
            "\1\125\1\uffff\12\101\13\uffff\1\126\37\uffff\1\126",
            "\12\127",
            "",
            "\1\131\1\uffff\12\104\13\uffff\1\132\37\uffff\1\132",
            "\12\133",
            "",
            "",
            "",
            "\12\134\13\uffff\1\112\37\uffff\1\112",
            "",
            "",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\4\50\1\135"+
            "\25\50\4\uffff\1\52\1\uffff\4\50\1\135\25\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\5\50\1\136"+
            "\24\50\4\uffff\1\52\1\uffff\5\50\1\136\24\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\22\50\1"+
            "\137\7\50\4\uffff\1\52\1\uffff\22\50\1\137\7\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\10\50\1"+
            "\140\21\50\4\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\4\50\1\141"+
            "\25\50\4\uffff\1\52\1\uffff\4\50\1\141\25\50",
            "",
            "",
            "",
            "",
            "\12\142\13\uffff\1\126\37\uffff\1\126",
            "",
            "\12\127\13\uffff\1\126\37\uffff\1\126",
            "",
            "\12\144\13\uffff\1\132\37\uffff\1\132",
            "",
            "\12\133\13\uffff\1\132\37\uffff\1\132",
            "\12\134\13\uffff\1\112\37\uffff\1\112",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\10\50\1"+
            "\147\21\50\4\uffff\1\52\1\uffff\10\50\1\147\21\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\4\50\1\150"+
            "\25\50\4\uffff\1\52\1\uffff\4\50\1\150\25\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\15\50\1"+
            "\151\14\50\4\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\52\1\uffff\32\50",
            "\12\142\13\uffff\1\126\37\uffff\1\126",
            "",
            "\12\144\13\uffff\1\132\37\uffff\1\132",
            "",
            "",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\27\50\1"+
            "\153\2\50\4\uffff\1\52\1\uffff\27\50\1\153\2\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\52\1\uffff\32\50",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\6\50\1\155"+
            "\23\50\4\uffff\1\52\1\uffff\32\50",
            "",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\52\1\uffff\32\50",
            "",
            "\1\53\1\uffff\1\51\1\53\1\uffff\12\50\1\54\6\uffff\32\50\4"+
            "\uffff\1\157\1\uffff\32\50",
            "",
            "\12\52\1\113\6\uffff\26\52\1\160\3\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\10\52\1\161\21\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\23\52\1\162\6\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\7\52\1\163\22\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\32\52\4\uffff\1\164\1\uffff\32\52",
            "\12\52\1\113\6\uffff\20\52\1\165\11\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\24\52\1\166\5\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\16\52\1\167\13\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\23\52\1\170\6\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\4\52\1\171\25\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\32\52\4\uffff\1\172\1\uffff\32\52",
            "\12\52\1\113\6\uffff\3\52\1\173\26\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\16\52\1\174\13\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\24\52\1\175\5\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\1\52\1\176\30\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\13\52\1\177\16\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\4\52\1\u0080\25\52\4\uffff\1\52\1\uffff"+
            "\32\52",
            "\12\52\1\113\6\uffff\32\52\4\uffff\1\52\1\uffff\5\52\1\u0081"+
            "\24\52",
            "\12\52\1\113\6\uffff\1\u0082\31\52\4\uffff\1\52\1\uffff\1\u0082"+
            "\31\52",
            "\12\52\1\113\6\uffff\13\52\1\u0083\16\52\4\uffff\1\52\1\uffff"+
            "\13\52\1\u0083\16\52",
            "\12\52\1\113\6\uffff\22\52\1\u0084\7\52\4\uffff\1\52\1\uffff"+
            "\22\52\1\u0084\7\52",
            "\12\52\1\113\6\uffff\4\52\1\u0085\25\52\4\uffff\1\52\1\uffff"+
            "\4\52\1\u0085\25\52",
            "\1\56\2\uffff\12\52\1\113\6\uffff\32\52\4\uffff\1\52\1\uffff"+
            "\32\52"
    };

    static final short[] DFA24_eot = DFA.unpackEncodedString(DFA24_eotS);
    static final short[] DFA24_eof = DFA.unpackEncodedString(DFA24_eofS);
    static final char[] DFA24_min = DFA.unpackEncodedStringToUnsignedChars(DFA24_minS);
    static final char[] DFA24_max = DFA.unpackEncodedStringToUnsignedChars(DFA24_maxS);
    static final short[] DFA24_accept = DFA.unpackEncodedString(DFA24_acceptS);
    static final short[] DFA24_special = DFA.unpackEncodedString(DFA24_specialS);
    static final short[][] DFA24_transition;

    static {
        int numStates = DFA24_transitionS.length;
        DFA24_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA24_transition[i] = DFA.unpackEncodedString(DFA24_transitionS[i]);
        }
    }

    class DFA24 extends DFA {

        public DFA24(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 24;
            this.eot = DFA24_eot;
            this.eof = DFA24_eof;
            this.min = DFA24_min;
            this.max = DFA24_max;
            this.accept = DFA24_accept;
            this.special = DFA24_special;
            this.transition = DFA24_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__67 | BASE | PREFIX | FALSE | TRUE | REFERENCE | SEMI | PERIOD | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | MINUS | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | BLANK | BLANK_PREFIX | TILDE | CARET | INTEGER | DOUBLE | DECIMAL | INTEGER_POSITIVE | INTEGER_NEGATIVE | DOUBLE_POSITIVE | DOUBLE_NEGATIVE | DECIMAL_POSITIVE | DECIMAL_NEGATIVE | VARNAME | ID_PLAIN | STRING_WITH_QUOTE | STRING_WITH_QUOTE_DOUBLE | STRING_URI | STRING_PREFIX | WS );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA24_18 = input.LA(1);

                        s = -1;
                        if ( ((LA24_18 >= '\u0000' && LA24_18 <= '\t')||(LA24_18 >= '\u000B' && LA24_18 <= '\f')||(LA24_18 >= '\u000E' && LA24_18 <= '\uFFFF')) ) {s = 60;}

                        else s = 59;

                        if ( s>=0 ) return s;
                        break;

                    case 1 : 
                        int LA24_17 = input.LA(1);

                        s = -1;
                        if ( ((LA24_17 >= '\u0000' && LA24_17 <= '\t')||(LA24_17 >= '\u000B' && LA24_17 <= '\f')||(LA24_17 >= '\u000E' && LA24_17 <= '\uFFFF')) ) {s = 58;}

                        else s = 57;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 24, _s, input);
            error(nvae);
            throw nvae;
        }

    }
 

}