// $ANTLR 3.3 Nov 30, 2010 12:50:56 SQL99.g 2011-06-20 17:03:48

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SQL99Lexer extends Lexer {
    public static final int EOF=-1;
    public static final int SELECT=4;
    public static final int DISTINCT=5;
    public static final int ALL=6;
    public static final int ASTERISK=7;
    public static final int COMMA=8;
    public static final int DOT=9;
    public static final int AS=10;
    public static final int FROM=11;
    public static final int WHERE=12;
    public static final int OR=13;
    public static final int AND=14;
    public static final int IS=15;
    public static final int NOT=16;
    public static final int NULL=17;
    public static final int JOIN=18;
    public static final int STRING=19;
    public static final int STRING_WITH_QUOTE=20;
    public static final int TRUE=21;
    public static final int FALSE=22;
    public static final int NUMERIC=23;
    public static final int EQUALS=24;
    public static final int LESS=25;
    public static final int GREATER=26;
    public static final int ORDER=27;
    public static final int BY=28;
    public static final int ON=29;
    public static final int LEFT=30;
    public static final int RIGHT=31;
    public static final int SEMI=32;
    public static final int LSQ_BRACKET=33;
    public static final int RSQ_BRACKET=34;
    public static final int LPAREN=35;
    public static final int RPAREN=36;
    public static final int QUESTION=37;
    public static final int DOLLAR=38;
    public static final int QUOTE_DOUBLE=39;
    public static final int QUOTE_SINGLE=40;
    public static final int APOSTROPHE=41;
    public static final int UNDERSCORE=42;
    public static final int DASH=43;
    public static final int AMPERSAND=44;
    public static final int AT=45;
    public static final int EXCLAMATION=46;
    public static final int HASH=47;
    public static final int PERCENT=48;
    public static final int PLUS=49;
    public static final int COLON=50;
    public static final int SLASH=51;
    public static final int DOUBLE_SLASH=52;
    public static final int BACKSLASH=53;
    public static final int TILDE=54;
    public static final int CARET=55;
    public static final int ALPHA=56;
    public static final int DIGIT=57;
    public static final int ALPHANUM=58;
    public static final int CHAR=59;
    public static final int WS=60;

    // delegates
    // delegators

    public SQL99Lexer() {;} 
    public SQL99Lexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public SQL99Lexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "SQL99.g"; }

    // $ANTLR start "SELECT"
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:170:7: ( ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // SQL99.g:170:9: ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SELECT"

    // $ANTLR start "DISTINCT"
    public final void mDISTINCT() throws RecognitionException {
        try {
            int _type = DISTINCT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:172:9: ( ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // SQL99.g:172:11: ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DISTINCT"

    // $ANTLR start "ALL"
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:174:4: ( ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // SQL99.g:174:6: ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ALL"

    // $ANTLR start "FROM"
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:176:5: ( ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' ) )
            // SQL99.g:176:7: ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FROM"

    // $ANTLR start "WHERE"
    public final void mWHERE() throws RecognitionException {
        try {
            int _type = WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:178:6: ( ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // SQL99.g:178:8: ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHERE"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:180:4: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // SQL99.g:180:6: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:182:3: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // SQL99.g:182:5: ( 'O' | 'o' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:184:4: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // SQL99.g:184:6: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "ORDER"
    public final void mORDER() throws RecognitionException {
        try {
            int _type = ORDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:186:6: ( ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // SQL99.g:186:8: ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ORDER"

    // $ANTLR start "BY"
    public final void mBY() throws RecognitionException {
        try {
            int _type = BY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:188:3: ( ( 'B' | 'b' ) ( 'Y' | 'y' ) )
            // SQL99.g:188:5: ( 'B' | 'b' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BY"

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:190:3: ( ( 'A' | 'a' ) ( 'S' | 's' ) )
            // SQL99.g:190:5: ( 'A' | 'a' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AS"

    // $ANTLR start "JOIN"
    public final void mJOIN() throws RecognitionException {
        try {
            int _type = JOIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:192:5: ( ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // SQL99.g:192:7: ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "JOIN"

    // $ANTLR start "ON"
    public final void mON() throws RecognitionException {
        try {
            int _type = ON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:194:3: ( ( 'O' | 'o' ) ( 'N' | 'n' ) )
            // SQL99.g:194:5: ( 'O' | 'o' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ON"

    // $ANTLR start "LEFT"
    public final void mLEFT() throws RecognitionException {
        try {
            int _type = LEFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:196:5: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' ) )
            // SQL99.g:196:7: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LEFT"

    // $ANTLR start "RIGHT"
    public final void mRIGHT() throws RecognitionException {
        try {
            int _type = RIGHT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:198:6: ( ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' ) )
            // SQL99.g:198:8: ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RIGHT"

    // $ANTLR start "IS"
    public final void mIS() throws RecognitionException {
        try {
            int _type = IS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:200:3: ( ( 'I' | 'i' ) ( 'S' | 's' ) )
            // SQL99.g:200:5: ( 'I' | 'i' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IS"

    // $ANTLR start "NULL"
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:202:5: ( ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // SQL99.g:202:7: ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NULL"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:204:6: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // SQL99.g:204:8: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:206:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
            // SQL99.g:206:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "SEMI"
    public final void mSEMI() throws RecognitionException {
        try {
            int _type = SEMI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:208:5: ( ';' )
            // SQL99.g:208:16: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMI"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:209:4: ( '.' )
            // SQL99.g:209:16: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:210:6: ( ',' )
            // SQL99.g:210:16: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "LSQ_BRACKET"
    public final void mLSQ_BRACKET() throws RecognitionException {
        try {
            int _type = LSQ_BRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:211:12: ( '[' )
            // SQL99.g:211:16: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LSQ_BRACKET"

    // $ANTLR start "RSQ_BRACKET"
    public final void mRSQ_BRACKET() throws RecognitionException {
        try {
            int _type = RSQ_BRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:212:12: ( ']' )
            // SQL99.g:212:16: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RSQ_BRACKET"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:213:7: ( '(' )
            // SQL99.g:213:16: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:214:7: ( ')' )
            // SQL99.g:214:16: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "QUESTION"
    public final void mQUESTION() throws RecognitionException {
        try {
            int _type = QUESTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:215:9: ( '?' )
            // SQL99.g:215:16: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUESTION"

    // $ANTLR start "DOLLAR"
    public final void mDOLLAR() throws RecognitionException {
        try {
            int _type = DOLLAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:216:7: ( '$' )
            // SQL99.g:216:16: '$'
            {
            match('$'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOLLAR"

    // $ANTLR start "QUOTE_DOUBLE"
    public final void mQUOTE_DOUBLE() throws RecognitionException {
        try {
            int _type = QUOTE_DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:217:13: ( '\"' )
            // SQL99.g:217:16: '\"'
            {
            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTE_DOUBLE"

    // $ANTLR start "QUOTE_SINGLE"
    public final void mQUOTE_SINGLE() throws RecognitionException {
        try {
            int _type = QUOTE_SINGLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:218:13: ( '\\'' )
            // SQL99.g:218:16: '\\''
            {
            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTE_SINGLE"

    // $ANTLR start "APOSTROPHE"
    public final void mAPOSTROPHE() throws RecognitionException {
        try {
            int _type = APOSTROPHE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:219:11: ( '`' )
            // SQL99.g:219:16: '`'
            {
            match('`'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "APOSTROPHE"

    // $ANTLR start "UNDERSCORE"
    public final void mUNDERSCORE() throws RecognitionException {
        try {
            int _type = UNDERSCORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:220:11: ( '_' )
            // SQL99.g:220:16: '_'
            {
            match('_'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNDERSCORE"

    // $ANTLR start "DASH"
    public final void mDASH() throws RecognitionException {
        try {
            int _type = DASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:221:5: ( '-' )
            // SQL99.g:221:16: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DASH"

    // $ANTLR start "ASTERISK"
    public final void mASTERISK() throws RecognitionException {
        try {
            int _type = ASTERISK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:222:9: ( '*' )
            // SQL99.g:222:16: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ASTERISK"

    // $ANTLR start "AMPERSAND"
    public final void mAMPERSAND() throws RecognitionException {
        try {
            int _type = AMPERSAND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:223:10: ( '&' )
            // SQL99.g:223:16: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AMPERSAND"

    // $ANTLR start "AT"
    public final void mAT() throws RecognitionException {
        try {
            int _type = AT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:224:3: ( '@' )
            // SQL99.g:224:16: '@'
            {
            match('@'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT"

    // $ANTLR start "EXCLAMATION"
    public final void mEXCLAMATION() throws RecognitionException {
        try {
            int _type = EXCLAMATION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:225:12: ( '!' )
            // SQL99.g:225:16: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXCLAMATION"

    // $ANTLR start "HASH"
    public final void mHASH() throws RecognitionException {
        try {
            int _type = HASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:226:5: ( '#' )
            // SQL99.g:226:16: '#'
            {
            match('#'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HASH"

    // $ANTLR start "PERCENT"
    public final void mPERCENT() throws RecognitionException {
        try {
            int _type = PERCENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:227:8: ( '%' )
            // SQL99.g:227:16: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERCENT"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:228:5: ( '+' )
            // SQL99.g:228:16: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:229:7: ( '=' )
            // SQL99.g:229:16: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:230:6: ( ':' )
            // SQL99.g:230:16: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:231:5: ( '<' )
            // SQL99.g:231:16: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:232:8: ( '>' )
            // SQL99.g:232:16: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "SLASH"
    public final void mSLASH() throws RecognitionException {
        try {
            int _type = SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:233:6: ( '/' )
            // SQL99.g:233:16: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLASH"

    // $ANTLR start "DOUBLE_SLASH"
    public final void mDOUBLE_SLASH() throws RecognitionException {
        try {
            int _type = DOUBLE_SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:234:13: ( '//' )
            // SQL99.g:234:16: '//'
            {
            match("//"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOUBLE_SLASH"

    // $ANTLR start "BACKSLASH"
    public final void mBACKSLASH() throws RecognitionException {
        try {
            int _type = BACKSLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:235:10: ( '\\\\' )
            // SQL99.g:235:16: '\\\\'
            {
            match('\\'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BACKSLASH"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:236:6: ( '~' )
            // SQL99.g:236:16: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "CARET"
    public final void mCARET() throws RecognitionException {
        try {
            int _type = CARET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:237:6: ( '^' )
            // SQL99.g:237:16: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CARET"

    // $ANTLR start "ALPHA"
    public final void mALPHA() throws RecognitionException {
        try {
            // SQL99.g:239:15: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // SQL99.g:239:17: ( 'a' .. 'z' | 'A' .. 'Z' )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "ALPHA"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // SQL99.g:241:15: ( '0' .. '9' )
            // SQL99.g:241:17: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "ALPHANUM"
    public final void mALPHANUM() throws RecognitionException {
        try {
            // SQL99.g:243:18: ( ( ALPHA | DIGIT ) )
            // SQL99.g:243:20: ( ALPHA | DIGIT )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "ALPHANUM"

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            // SQL99.g:245:14: ( ( ALPHANUM | UNDERSCORE | DASH ) )
            // SQL99.g:245:16: ( ALPHANUM | UNDERSCORE | DASH )
            {
            if ( input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "CHAR"

    // $ANTLR start "NUMERIC"
    public final void mNUMERIC() throws RecognitionException {
        try {
            int _type = NUMERIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:247:8: ( ( DIGIT )+ )
            // SQL99.g:247:10: ( DIGIT )+
            {
            // SQL99.g:247:10: ( DIGIT )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // SQL99.g:247:10: DIGIT
            	    {
            	    mDIGIT(); 

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
        }
    }
    // $ANTLR end "NUMERIC"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:249:7: ( ( CHAR )* )
            // SQL99.g:249:9: ( CHAR )*
            {
            // SQL99.g:249:9: ( CHAR )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='-'||(LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // SQL99.g:249:9: CHAR
            	    {
            	    mCHAR(); 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "STRING_WITH_QUOTE"
    public final void mSTRING_WITH_QUOTE() throws RecognitionException {
        try {
            int _type = STRING_WITH_QUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:251:18: ( ( QUOTE_SINGLE | QUOTE_DOUBLE ) ( CHAR )* ( QUOTE_SINGLE | QUOTE_DOUBLE ) )
            // SQL99.g:251:20: ( QUOTE_SINGLE | QUOTE_DOUBLE ) ( CHAR )* ( QUOTE_SINGLE | QUOTE_DOUBLE )
            {
            if ( input.LA(1)=='\"'||input.LA(1)=='\'' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // SQL99.g:251:48: ( CHAR )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='-'||(LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='Z')||LA3_0=='_'||(LA3_0>='a' && LA3_0<='z')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // SQL99.g:251:48: CHAR
            	    {
            	    mCHAR(); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            if ( input.LA(1)=='\"'||input.LA(1)=='\'' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_WITH_QUOTE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:253:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
            // SQL99.g:253:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            {
            // SQL99.g:253:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            int cnt5=0;
            loop5:
            do {
                int alt5=4;
                switch ( input.LA(1) ) {
                case ' ':
                    {
                    alt5=1;
                    }
                    break;
                case '\t':
                    {
                    alt5=2;
                    }
                    break;
                case '\n':
                case '\r':
                    {
                    alt5=3;
                    }
                    break;

                }

                switch (alt5) {
            	case 1 :
            	    // SQL99.g:253:6: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // SQL99.g:253:10: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // SQL99.g:253:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    {
            	    // SQL99.g:253:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    int alt4=2;
            	    int LA4_0 = input.LA(1);

            	    if ( (LA4_0=='\n') ) {
            	        alt4=1;
            	    }
            	    else if ( (LA4_0=='\r') ) {
            	        alt4=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 4, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt4) {
            	        case 1 :
            	            // SQL99.g:253:16: '\\n'
            	            {
            	            match('\n'); 

            	            }
            	            break;
            	        case 2 :
            	            // SQL99.g:253:21: '\\r' ( '\\n' )
            	            {
            	            match('\r'); 
            	            // SQL99.g:253:25: ( '\\n' )
            	            // SQL99.g:253:26: '\\n'
            	            {
            	            match('\n'); 

            	            }


            	            }
            	            break;

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

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // SQL99.g:1:8: ( SELECT | DISTINCT | ALL | FROM | WHERE | AND | OR | NOT | ORDER | BY | AS | JOIN | ON | LEFT | RIGHT | IS | NULL | FALSE | TRUE | SEMI | DOT | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | DASH | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | TILDE | CARET | NUMERIC | STRING | STRING_WITH_QUOTE | WS )
        int alt6=53;
        alt6 = dfa6.predict(input);
        switch (alt6) {
            case 1 :
                // SQL99.g:1:10: SELECT
                {
                mSELECT(); 

                }
                break;
            case 2 :
                // SQL99.g:1:17: DISTINCT
                {
                mDISTINCT(); 

                }
                break;
            case 3 :
                // SQL99.g:1:26: ALL
                {
                mALL(); 

                }
                break;
            case 4 :
                // SQL99.g:1:30: FROM
                {
                mFROM(); 

                }
                break;
            case 5 :
                // SQL99.g:1:35: WHERE
                {
                mWHERE(); 

                }
                break;
            case 6 :
                // SQL99.g:1:41: AND
                {
                mAND(); 

                }
                break;
            case 7 :
                // SQL99.g:1:45: OR
                {
                mOR(); 

                }
                break;
            case 8 :
                // SQL99.g:1:48: NOT
                {
                mNOT(); 

                }
                break;
            case 9 :
                // SQL99.g:1:52: ORDER
                {
                mORDER(); 

                }
                break;
            case 10 :
                // SQL99.g:1:58: BY
                {
                mBY(); 

                }
                break;
            case 11 :
                // SQL99.g:1:61: AS
                {
                mAS(); 

                }
                break;
            case 12 :
                // SQL99.g:1:64: JOIN
                {
                mJOIN(); 

                }
                break;
            case 13 :
                // SQL99.g:1:69: ON
                {
                mON(); 

                }
                break;
            case 14 :
                // SQL99.g:1:72: LEFT
                {
                mLEFT(); 

                }
                break;
            case 15 :
                // SQL99.g:1:77: RIGHT
                {
                mRIGHT(); 

                }
                break;
            case 16 :
                // SQL99.g:1:83: IS
                {
                mIS(); 

                }
                break;
            case 17 :
                // SQL99.g:1:86: NULL
                {
                mNULL(); 

                }
                break;
            case 18 :
                // SQL99.g:1:91: FALSE
                {
                mFALSE(); 

                }
                break;
            case 19 :
                // SQL99.g:1:97: TRUE
                {
                mTRUE(); 

                }
                break;
            case 20 :
                // SQL99.g:1:102: SEMI
                {
                mSEMI(); 

                }
                break;
            case 21 :
                // SQL99.g:1:107: DOT
                {
                mDOT(); 

                }
                break;
            case 22 :
                // SQL99.g:1:111: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 23 :
                // SQL99.g:1:117: LSQ_BRACKET
                {
                mLSQ_BRACKET(); 

                }
                break;
            case 24 :
                // SQL99.g:1:129: RSQ_BRACKET
                {
                mRSQ_BRACKET(); 

                }
                break;
            case 25 :
                // SQL99.g:1:141: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 26 :
                // SQL99.g:1:148: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 27 :
                // SQL99.g:1:155: QUESTION
                {
                mQUESTION(); 

                }
                break;
            case 28 :
                // SQL99.g:1:164: DOLLAR
                {
                mDOLLAR(); 

                }
                break;
            case 29 :
                // SQL99.g:1:171: QUOTE_DOUBLE
                {
                mQUOTE_DOUBLE(); 

                }
                break;
            case 30 :
                // SQL99.g:1:184: QUOTE_SINGLE
                {
                mQUOTE_SINGLE(); 

                }
                break;
            case 31 :
                // SQL99.g:1:197: APOSTROPHE
                {
                mAPOSTROPHE(); 

                }
                break;
            case 32 :
                // SQL99.g:1:208: UNDERSCORE
                {
                mUNDERSCORE(); 

                }
                break;
            case 33 :
                // SQL99.g:1:219: DASH
                {
                mDASH(); 

                }
                break;
            case 34 :
                // SQL99.g:1:224: ASTERISK
                {
                mASTERISK(); 

                }
                break;
            case 35 :
                // SQL99.g:1:233: AMPERSAND
                {
                mAMPERSAND(); 

                }
                break;
            case 36 :
                // SQL99.g:1:243: AT
                {
                mAT(); 

                }
                break;
            case 37 :
                // SQL99.g:1:246: EXCLAMATION
                {
                mEXCLAMATION(); 

                }
                break;
            case 38 :
                // SQL99.g:1:258: HASH
                {
                mHASH(); 

                }
                break;
            case 39 :
                // SQL99.g:1:263: PERCENT
                {
                mPERCENT(); 

                }
                break;
            case 40 :
                // SQL99.g:1:271: PLUS
                {
                mPLUS(); 

                }
                break;
            case 41 :
                // SQL99.g:1:276: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 42 :
                // SQL99.g:1:283: COLON
                {
                mCOLON(); 

                }
                break;
            case 43 :
                // SQL99.g:1:289: LESS
                {
                mLESS(); 

                }
                break;
            case 44 :
                // SQL99.g:1:294: GREATER
                {
                mGREATER(); 

                }
                break;
            case 45 :
                // SQL99.g:1:302: SLASH
                {
                mSLASH(); 

                }
                break;
            case 46 :
                // SQL99.g:1:308: DOUBLE_SLASH
                {
                mDOUBLE_SLASH(); 

                }
                break;
            case 47 :
                // SQL99.g:1:321: BACKSLASH
                {
                mBACKSLASH(); 

                }
                break;
            case 48 :
                // SQL99.g:1:331: TILDE
                {
                mTILDE(); 

                }
                break;
            case 49 :
                // SQL99.g:1:337: CARET
                {
                mCARET(); 

                }
                break;
            case 50 :
                // SQL99.g:1:343: NUMERIC
                {
                mNUMERIC(); 

                }
                break;
            case 51 :
                // SQL99.g:1:351: STRING
                {
                mSTRING(); 

                }
                break;
            case 52 :
                // SQL99.g:1:358: STRING_WITH_QUOTE
                {
                mSTRING_WITH_QUOTE(); 

                }
                break;
            case 53 :
                // SQL99.g:1:376: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA6_eotS =
        "\16\54\11\uffff\1\100\1\102\1\uffff\1\103\1\104\13\uffff\1\106\3"+
        "\uffff\1\107\2\uffff\4\54\1\114\3\54\1\121\1\122\2\54\1\125\3\54"+
        "\1\131\1\54\10\uffff\2\54\1\135\1\136\1\uffff\4\54\2\uffff\1\143"+
        "\1\54\1\uffff\3\54\1\uffff\3\54\2\uffff\1\153\3\54\1\uffff\1\157"+
        "\1\160\1\161\1\54\1\163\2\54\1\uffff\1\166\1\167\1\170\3\uffff\1"+
        "\171\1\uffff\1\172\1\54\5\uffff\1\54\1\175\1\uffff";
    static final String DFA6_eofS =
        "\176\uffff";
    static final String DFA6_minS =
        "\1\11\1\105\1\111\1\114\1\101\1\110\1\116\1\117\1\131\1\117\1\105"+
        "\1\111\1\123\1\122\11\uffff\2\42\1\uffff\2\55\13\uffff\1\57\3\uffff"+
        "\1\55\2\uffff\1\114\1\123\1\114\1\104\1\55\1\117\1\114\1\105\2\55"+
        "\1\124\1\114\1\55\1\111\1\106\1\107\1\55\1\125\10\uffff\1\105\1"+
        "\124\2\55\1\uffff\1\115\1\123\1\122\1\105\2\uffff\1\55\1\114\1\uffff"+
        "\1\116\1\124\1\110\1\uffff\1\105\1\103\1\111\2\uffff\1\55\2\105"+
        "\1\122\1\uffff\3\55\1\124\1\55\1\124\1\116\1\uffff\3\55\3\uffff"+
        "\1\55\1\uffff\1\55\1\103\5\uffff\1\124\1\55\1\uffff";
    static final String DFA6_maxS =
        "\1\176\1\145\1\151\1\163\1\162\1\150\1\162\1\165\1\171\1\157\1\145"+
        "\1\151\1\163\1\162\11\uffff\2\172\1\uffff\2\172\13\uffff\1\57\3"+
        "\uffff\1\172\2\uffff\1\154\1\163\1\154\1\144\1\172\1\157\1\154\1"+
        "\145\2\172\1\164\1\154\1\172\1\151\1\146\1\147\1\172\1\165\10\uffff"+
        "\1\145\1\164\2\172\1\uffff\1\155\1\163\1\162\1\145\2\uffff\1\172"+
        "\1\154\1\uffff\1\156\1\164\1\150\1\uffff\1\145\1\143\1\151\2\uffff"+
        "\1\172\2\145\1\162\1\uffff\3\172\1\164\1\172\1\164\1\156\1\uffff"+
        "\3\172\3\uffff\1\172\1\uffff\1\172\1\143\5\uffff\1\164\1\172\1\uffff";
    static final String DFA6_acceptS =
        "\16\uffff\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\2\uffff\1"+
        "\37\2\uffff\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1"+
        "\54\1\uffff\1\57\1\60\1\61\1\uffff\1\63\1\65\22\uffff\1\35\1\64"+
        "\1\36\1\40\1\41\1\56\1\55\1\62\4\uffff\1\13\4\uffff\1\7\1\15\2\uffff"+
        "\1\12\3\uffff\1\20\3\uffff\1\3\1\6\4\uffff\1\10\7\uffff\1\4\3\uffff"+
        "\1\21\1\14\1\16\1\uffff\1\23\2\uffff\1\22\1\5\1\11\1\17\1\1\2\uffff"+
        "\1\2";
    static final String DFA6_specialS =
        "\176\uffff}>";
    static final String[] DFA6_transitionS = {
            "\2\55\2\uffff\1\55\22\uffff\1\55\1\37\1\27\1\40\1\26\1\41\1"+
            "\35\1\30\1\23\1\24\1\34\1\42\1\20\1\33\1\17\1\47\12\53\1\44"+
            "\1\16\1\45\1\43\1\46\1\25\1\36\1\3\1\10\1\uffff\1\2\1\uffff"+
            "\1\4\2\uffff\1\14\1\11\1\uffff\1\12\1\uffff\1\7\1\6\2\uffff"+
            "\1\13\1\1\1\15\2\uffff\1\5\3\uffff\1\21\1\50\1\22\1\52\1\32"+
            "\1\31\1\3\1\10\1\uffff\1\2\1\uffff\1\4\2\uffff\1\14\1\11\1\uffff"+
            "\1\12\1\uffff\1\7\1\6\2\uffff\1\13\1\1\1\15\2\uffff\1\5\6\uffff"+
            "\1\51",
            "\1\56\37\uffff\1\56",
            "\1\57\37\uffff\1\57",
            "\1\60\1\uffff\1\61\4\uffff\1\62\30\uffff\1\60\1\uffff\1\61"+
            "\4\uffff\1\62",
            "\1\64\20\uffff\1\63\16\uffff\1\64\20\uffff\1\63",
            "\1\65\37\uffff\1\65",
            "\1\67\3\uffff\1\66\33\uffff\1\67\3\uffff\1\66",
            "\1\70\5\uffff\1\71\31\uffff\1\70\5\uffff\1\71",
            "\1\72\37\uffff\1\72",
            "\1\73\37\uffff\1\73",
            "\1\74\37\uffff\1\74",
            "\1\75\37\uffff\1\75",
            "\1\76\37\uffff\1\76",
            "\1\77\37\uffff\1\77",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\101\4\uffff\1\101\5\uffff\1\101\2\uffff\12\101\7\uffff\32"+
            "\101\4\uffff\1\101\1\uffff\32\101",
            "\1\101\4\uffff\1\101\5\uffff\1\101\2\uffff\12\101\7\uffff\32"+
            "\101\4\uffff\1\101\1\uffff\32\101",
            "",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
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
            "\1\105",
            "",
            "",
            "",
            "\1\54\2\uffff\12\53\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "\1\110\37\uffff\1\110",
            "\1\111\37\uffff\1\111",
            "\1\112\37\uffff\1\112",
            "\1\113\37\uffff\1\113",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\115\37\uffff\1\115",
            "\1\116\37\uffff\1\116",
            "\1\117\37\uffff\1\117",
            "\1\54\2\uffff\12\54\7\uffff\3\54\1\120\26\54\4\uffff\1\54\1"+
            "\uffff\3\54\1\120\26\54",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\123\37\uffff\1\123",
            "\1\124\37\uffff\1\124",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\126\37\uffff\1\126",
            "\1\127\37\uffff\1\127",
            "\1\130\37\uffff\1\130",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\132\37\uffff\1\132",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\133\37\uffff\1\133",
            "\1\134\37\uffff\1\134",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\1\137\37\uffff\1\137",
            "\1\140\37\uffff\1\140",
            "\1\141\37\uffff\1\141",
            "\1\142\37\uffff\1\142",
            "",
            "",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\144\37\uffff\1\144",
            "",
            "\1\145\37\uffff\1\145",
            "\1\146\37\uffff\1\146",
            "\1\147\37\uffff\1\147",
            "",
            "\1\150\37\uffff\1\150",
            "\1\151\37\uffff\1\151",
            "\1\152\37\uffff\1\152",
            "",
            "",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\154\37\uffff\1\154",
            "\1\155\37\uffff\1\155",
            "\1\156\37\uffff\1\156",
            "",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\162\37\uffff\1\162",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\164\37\uffff\1\164",
            "\1\165\37\uffff\1\165",
            "",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\173\37\uffff\1\173",
            "",
            "",
            "",
            "",
            "",
            "\1\174\37\uffff\1\174",
            "\1\54\2\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            ""
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( SELECT | DISTINCT | ALL | FROM | WHERE | AND | OR | NOT | ORDER | BY | AS | JOIN | ON | LEFT | RIGHT | IS | NULL | FALSE | TRUE | SEMI | DOT | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | DASH | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | TILDE | CARET | NUMERIC | STRING | STRING_WITH_QUOTE | WS );";
        }
    }
 

}