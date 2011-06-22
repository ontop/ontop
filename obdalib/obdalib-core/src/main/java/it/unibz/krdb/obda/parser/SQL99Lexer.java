// $ANTLR 3.3 Nov 30, 2010 12:50:56 SQL99.g 2011-06-22 16:15:48

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SQL99Lexer extends Lexer {
    public static final int EOF=-1;
    public static final int UNION=4;
    public static final int SELECT=5;
    public static final int DISTINCT=6;
    public static final int ALL=7;
    public static final int ASTERISK=8;
    public static final int COMMA=9;
    public static final int DOT=10;
    public static final int AS=11;
    public static final int COUNT=12;
    public static final int LPAREN=13;
    public static final int RPAREN=14;
    public static final int AVG=15;
    public static final int MAX=16;
    public static final int MIN=17;
    public static final int SUM=18;
    public static final int EVERY=19;
    public static final int ANY=20;
    public static final int SOME=21;
    public static final int FROM=22;
    public static final int WHERE=23;
    public static final int OR=24;
    public static final int AND=25;
    public static final int IS=26;
    public static final int NOT=27;
    public static final int NULL=28;
    public static final int IN=29;
    public static final int GROUP=30;
    public static final int BY=31;
    public static final int JOIN=32;
    public static final int INNER=33;
    public static final int OUTER=34;
    public static final int LEFT=35;
    public static final int RIGHT=36;
    public static final int FULL=37;
    public static final int ON=38;
    public static final int STRING=39;
    public static final int STRING_WITH_QUOTE_DOUBLE=40;
    public static final int TRUE=41;
    public static final int FALSE=42;
    public static final int NUMERIC=43;
    public static final int STRING_WITH_QUOTE=44;
    public static final int EQUALS=45;
    public static final int LESS=46;
    public static final int GREATER=47;
    public static final int ORDER=48;
    public static final int SEMI=49;
    public static final int LSQ_BRACKET=50;
    public static final int RSQ_BRACKET=51;
    public static final int QUESTION=52;
    public static final int DOLLAR=53;
    public static final int QUOTE_DOUBLE=54;
    public static final int QUOTE_SINGLE=55;
    public static final int APOSTROPHE=56;
    public static final int UNDERSCORE=57;
    public static final int DASH=58;
    public static final int AMPERSAND=59;
    public static final int AT=60;
    public static final int EXCLAMATION=61;
    public static final int HASH=62;
    public static final int PERCENT=63;
    public static final int PLUS=64;
    public static final int COLON=65;
    public static final int SLASH=66;
    public static final int DOUBLE_SLASH=67;
    public static final int BACKSLASH=68;
    public static final int TILDE=69;
    public static final int CARET=70;
    public static final int ALPHA=71;
    public static final int DIGIT=72;
    public static final int ALPHANUM=73;
    public static final int CHAR=74;
    public static final int WS=75;

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
            // SQL99.g:287:7: ( ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // SQL99.g:287:9: ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )
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
            // SQL99.g:289:9: ( ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // SQL99.g:289:11: ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' )
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
            // SQL99.g:291:4: ( ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // SQL99.g:291:6: ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )
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

    // $ANTLR start "AVG"
    public final void mAVG() throws RecognitionException {
        try {
            int _type = AVG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:293:4: ( ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'G' | 'g' ) )
            // SQL99.g:293:6: ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'G' | 'g' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
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


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AVG"

    // $ANTLR start "MAX"
    public final void mMAX() throws RecognitionException {
        try {
            int _type = MAX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:295:4: ( ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'X' | 'x' ) )
            // SQL99.g:295:6: ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'X' | 'x' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
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

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
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
    // $ANTLR end "MAX"

    // $ANTLR start "MIN"
    public final void mMIN() throws RecognitionException {
        try {
            int _type = MIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:297:4: ( ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // SQL99.g:297:6: ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
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
    // $ANTLR end "MIN"

    // $ANTLR start "SUM"
    public final void mSUM() throws RecognitionException {
        try {
            int _type = SUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:299:4: ( ( 'S' | 's' ) ( 'U' | 'u' ) ( 'M' | 'm' ) )
            // SQL99.g:299:6: ( 'S' | 's' ) ( 'U' | 'u' ) ( 'M' | 'm' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
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
    // $ANTLR end "SUM"

    // $ANTLR start "EVERY"
    public final void mEVERY() throws RecognitionException {
        try {
            int _type = EVERY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:301:6: ( ( 'E' | 'e' ) ( 'V' | 'v' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'Y' | 'y' ) )
            // SQL99.g:301:8: ( 'E' | 'e' ) ( 'V' | 'v' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
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
    // $ANTLR end "EVERY"

    // $ANTLR start "ANY"
    public final void mANY() throws RecognitionException {
        try {
            int _type = ANY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:303:4: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' ) )
            // SQL99.g:303:6: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' )
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
    // $ANTLR end "ANY"

    // $ANTLR start "SOME"
    public final void mSOME() throws RecognitionException {
        try {
            int _type = SOME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:305:5: ( ( 'S' | 's' ) ( 'O' | 'o' ) ( 'M' | 'm' ) ( 'E' | 'e' ) )
            // SQL99.g:305:7: ( 'S' | 's' ) ( 'O' | 'o' ) ( 'M' | 'm' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
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
    // $ANTLR end "SOME"

    // $ANTLR start "COUNT"
    public final void mCOUNT() throws RecognitionException {
        try {
            int _type = COUNT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:307:6: ( ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'T' | 't' ) )
            // SQL99.g:307:8: ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
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
    // $ANTLR end "COUNT"

    // $ANTLR start "FROM"
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:309:5: ( ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' ) )
            // SQL99.g:309:7: ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )
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
            // SQL99.g:311:6: ( ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // SQL99.g:311:8: ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )
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
            // SQL99.g:313:4: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // SQL99.g:313:6: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
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
            // SQL99.g:315:3: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // SQL99.g:315:5: ( 'O' | 'o' ) ( 'R' | 'r' )
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
            // SQL99.g:317:4: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // SQL99.g:317:6: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
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
            // SQL99.g:319:6: ( ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // SQL99.g:319:8: ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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

    // $ANTLR start "GROUP"
    public final void mGROUP() throws RecognitionException {
        try {
            int _type = GROUP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:321:6: ( ( 'G' | 'g' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'P' | 'p' ) )
            // SQL99.g:321:8: ( 'G' | 'g' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'P' | 'p' )
            {
            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
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

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
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
    // $ANTLR end "GROUP"

    // $ANTLR start "BY"
    public final void mBY() throws RecognitionException {
        try {
            int _type = BY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:323:3: ( ( 'B' | 'b' ) ( 'Y' | 'y' ) )
            // SQL99.g:323:5: ( 'B' | 'b' ) ( 'Y' | 'y' )
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
            // SQL99.g:325:3: ( ( 'A' | 'a' ) ( 'S' | 's' ) )
            // SQL99.g:325:5: ( 'A' | 'a' ) ( 'S' | 's' )
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
            // SQL99.g:327:5: ( ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // SQL99.g:327:7: ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' )
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

    // $ANTLR start "INNER"
    public final void mINNER() throws RecognitionException {
        try {
            int _type = INNER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:329:6: ( ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // SQL99.g:329:8: ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
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

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
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
    // $ANTLR end "INNER"

    // $ANTLR start "OUTER"
    public final void mOUTER() throws RecognitionException {
        try {
            int _type = OUTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:331:6: ( ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // SQL99.g:331:8: ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
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

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
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
    // $ANTLR end "OUTER"

    // $ANTLR start "LEFT"
    public final void mLEFT() throws RecognitionException {
        try {
            int _type = LEFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:333:5: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' ) )
            // SQL99.g:333:7: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' )
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
            // SQL99.g:335:6: ( ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' ) )
            // SQL99.g:335:8: ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' )
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

    // $ANTLR start "FULL"
    public final void mFULL() throws RecognitionException {
        try {
            int _type = FULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:337:5: ( ( 'F' | 'f' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // SQL99.g:337:7: ( 'F' | 'f' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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
    // $ANTLR end "FULL"

    // $ANTLR start "UNION"
    public final void mUNION() throws RecognitionException {
        try {
            int _type = UNION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:339:6: ( ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' ) )
            // SQL99.g:339:8: ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
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

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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
    // $ANTLR end "UNION"

    // $ANTLR start "ON"
    public final void mON() throws RecognitionException {
        try {
            int _type = ON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:341:3: ( ( 'O' | 'o' ) ( 'N' | 'n' ) )
            // SQL99.g:341:5: ( 'O' | 'o' ) ( 'N' | 'n' )
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

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:343:3: ( ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // SQL99.g:343:5: ( 'I' | 'i' ) ( 'N' | 'n' )
            {
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
    // $ANTLR end "IN"

    // $ANTLR start "IS"
    public final void mIS() throws RecognitionException {
        try {
            int _type = IS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:345:3: ( ( 'I' | 'i' ) ( 'S' | 's' ) )
            // SQL99.g:345:5: ( 'I' | 'i' ) ( 'S' | 's' )
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
            // SQL99.g:347:5: ( ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // SQL99.g:347:7: ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
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
            // SQL99.g:349:6: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // SQL99.g:349:8: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
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
            // SQL99.g:351:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
            // SQL99.g:351:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
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
            // SQL99.g:353:5: ( ';' )
            // SQL99.g:353:16: ';'
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
            // SQL99.g:354:4: ( '.' )
            // SQL99.g:354:16: '.'
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
            // SQL99.g:355:6: ( ',' )
            // SQL99.g:355:16: ','
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
            // SQL99.g:356:12: ( '[' )
            // SQL99.g:356:16: '['
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
            // SQL99.g:357:12: ( ']' )
            // SQL99.g:357:16: ']'
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
            // SQL99.g:358:7: ( '(' )
            // SQL99.g:358:16: '('
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
            // SQL99.g:359:7: ( ')' )
            // SQL99.g:359:16: ')'
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
            // SQL99.g:360:9: ( '?' )
            // SQL99.g:360:16: '?'
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
            // SQL99.g:361:7: ( '$' )
            // SQL99.g:361:16: '$'
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
            // SQL99.g:362:13: ( '\"' )
            // SQL99.g:362:16: '\"'
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
            // SQL99.g:363:13: ( '\\'' )
            // SQL99.g:363:16: '\\''
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
            // SQL99.g:364:11: ( '`' )
            // SQL99.g:364:16: '`'
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
            // SQL99.g:365:11: ( '_' )
            // SQL99.g:365:16: '_'
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
            // SQL99.g:366:5: ( '-' )
            // SQL99.g:366:16: '-'
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
            // SQL99.g:367:9: ( '*' )
            // SQL99.g:367:16: '*'
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
            // SQL99.g:368:10: ( '&' )
            // SQL99.g:368:16: '&'
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
            // SQL99.g:369:3: ( '@' )
            // SQL99.g:369:16: '@'
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
            // SQL99.g:370:12: ( '!' )
            // SQL99.g:370:16: '!'
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
            // SQL99.g:371:5: ( '#' )
            // SQL99.g:371:16: '#'
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
            // SQL99.g:372:8: ( '%' )
            // SQL99.g:372:16: '%'
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
            // SQL99.g:373:5: ( '+' )
            // SQL99.g:373:16: '+'
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
            // SQL99.g:374:7: ( '=' )
            // SQL99.g:374:16: '='
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
            // SQL99.g:375:6: ( ':' )
            // SQL99.g:375:16: ':'
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
            // SQL99.g:376:5: ( '<' )
            // SQL99.g:376:16: '<'
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
            // SQL99.g:377:8: ( '>' )
            // SQL99.g:377:16: '>'
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
            // SQL99.g:378:6: ( '/' )
            // SQL99.g:378:16: '/'
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
            // SQL99.g:379:13: ( '//' )
            // SQL99.g:379:16: '//'
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
            // SQL99.g:380:10: ( '\\\\' )
            // SQL99.g:380:16: '\\\\'
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
            // SQL99.g:381:6: ( '~' )
            // SQL99.g:381:16: '~'
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
            // SQL99.g:382:6: ( '^' )
            // SQL99.g:382:16: '^'
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
            // SQL99.g:384:15: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // SQL99.g:384:17: ( 'a' .. 'z' | 'A' .. 'Z' )
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
            // SQL99.g:386:15: ( '0' .. '9' )
            // SQL99.g:386:17: '0' .. '9'
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
            // SQL99.g:388:18: ( ( ALPHA | DIGIT ) )
            // SQL99.g:388:20: ( ALPHA | DIGIT )
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
            // SQL99.g:390:14: ( ( ALPHANUM | UNDERSCORE | DASH ) )
            // SQL99.g:390:16: ( ALPHANUM | UNDERSCORE | DASH )
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
            // SQL99.g:392:8: ( ( DIGIT )+ )
            // SQL99.g:392:10: ( DIGIT )+
            {
            // SQL99.g:392:10: ( DIGIT )+
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
            	    // SQL99.g:392:10: DIGIT
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
            // SQL99.g:394:7: ( ( CHAR )* )
            // SQL99.g:394:9: ( CHAR )*
            {
            // SQL99.g:394:9: ( CHAR )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='-'||(LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // SQL99.g:394:9: CHAR
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

    // $ANTLR start "STRING_WITH_QUOTE_DOUBLE"
    public final void mSTRING_WITH_QUOTE_DOUBLE() throws RecognitionException {
        try {
            int _type = STRING_WITH_QUOTE_DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:396:25: ( QUOTE_DOUBLE ( CHAR )* QUOTE_DOUBLE )
            // SQL99.g:396:27: QUOTE_DOUBLE ( CHAR )* QUOTE_DOUBLE
            {
            mQUOTE_DOUBLE(); 
            // SQL99.g:396:40: ( CHAR )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='-'||(LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='Z')||LA3_0=='_'||(LA3_0>='a' && LA3_0<='z')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // SQL99.g:396:40: CHAR
            	    {
            	    mCHAR(); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            mQUOTE_DOUBLE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_WITH_QUOTE_DOUBLE"

    // $ANTLR start "STRING_WITH_QUOTE"
    public final void mSTRING_WITH_QUOTE() throws RecognitionException {
        try {
            int _type = STRING_WITH_QUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // SQL99.g:398:18: ( ( QUOTE_SINGLE | QUOTE_DOUBLE ) ( CHAR )* ( QUOTE_SINGLE | QUOTE_DOUBLE ) )
            // SQL99.g:398:20: ( QUOTE_SINGLE | QUOTE_DOUBLE ) ( CHAR )* ( QUOTE_SINGLE | QUOTE_DOUBLE )
            {
            if ( input.LA(1)=='\"'||input.LA(1)=='\'' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // SQL99.g:398:48: ( CHAR )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='-'||(LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='Z')||LA4_0=='_'||(LA4_0>='a' && LA4_0<='z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // SQL99.g:398:48: CHAR
            	    {
            	    mCHAR(); 

            	    }
            	    break;

            	default :
            	    break loop4;
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
            // SQL99.g:400:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
            // SQL99.g:400:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            {
            // SQL99.g:400:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            int cnt6=0;
            loop6:
            do {
                int alt6=4;
                switch ( input.LA(1) ) {
                case ' ':
                    {
                    alt6=1;
                    }
                    break;
                case '\t':
                    {
                    alt6=2;
                    }
                    break;
                case '\n':
                case '\r':
                    {
                    alt6=3;
                    }
                    break;

                }

                switch (alt6) {
            	case 1 :
            	    // SQL99.g:400:6: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // SQL99.g:400:10: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // SQL99.g:400:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    {
            	    // SQL99.g:400:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    int alt5=2;
            	    int LA5_0 = input.LA(1);

            	    if ( (LA5_0=='\n') ) {
            	        alt5=1;
            	    }
            	    else if ( (LA5_0=='\r') ) {
            	        alt5=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 5, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt5) {
            	        case 1 :
            	            // SQL99.g:400:16: '\\n'
            	            {
            	            match('\n'); 

            	            }
            	            break;
            	        case 2 :
            	            // SQL99.g:400:21: '\\r' ( '\\n' )
            	            {
            	            match('\r'); 
            	            // SQL99.g:400:25: ( '\\n' )
            	            // SQL99.g:400:26: '\\n'
            	            {
            	            match('\n'); 

            	            }


            	            }
            	            break;

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
        // SQL99.g:1:8: ( SELECT | DISTINCT | ALL | AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT | FROM | WHERE | AND | OR | NOT | ORDER | GROUP | BY | AS | JOIN | INNER | OUTER | LEFT | RIGHT | FULL | UNION | ON | IN | IS | NULL | FALSE | TRUE | SEMI | DOT | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | DASH | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | TILDE | CARET | NUMERIC | STRING | STRING_WITH_QUOTE_DOUBLE | STRING_WITH_QUOTE | WS )
        int alt7=68;
        alt7 = dfa7.predict(input);
        switch (alt7) {
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
                // SQL99.g:1:30: AVG
                {
                mAVG(); 

                }
                break;
            case 5 :
                // SQL99.g:1:34: MAX
                {
                mMAX(); 

                }
                break;
            case 6 :
                // SQL99.g:1:38: MIN
                {
                mMIN(); 

                }
                break;
            case 7 :
                // SQL99.g:1:42: SUM
                {
                mSUM(); 

                }
                break;
            case 8 :
                // SQL99.g:1:46: EVERY
                {
                mEVERY(); 

                }
                break;
            case 9 :
                // SQL99.g:1:52: ANY
                {
                mANY(); 

                }
                break;
            case 10 :
                // SQL99.g:1:56: SOME
                {
                mSOME(); 

                }
                break;
            case 11 :
                // SQL99.g:1:61: COUNT
                {
                mCOUNT(); 

                }
                break;
            case 12 :
                // SQL99.g:1:67: FROM
                {
                mFROM(); 

                }
                break;
            case 13 :
                // SQL99.g:1:72: WHERE
                {
                mWHERE(); 

                }
                break;
            case 14 :
                // SQL99.g:1:78: AND
                {
                mAND(); 

                }
                break;
            case 15 :
                // SQL99.g:1:82: OR
                {
                mOR(); 

                }
                break;
            case 16 :
                // SQL99.g:1:85: NOT
                {
                mNOT(); 

                }
                break;
            case 17 :
                // SQL99.g:1:89: ORDER
                {
                mORDER(); 

                }
                break;
            case 18 :
                // SQL99.g:1:95: GROUP
                {
                mGROUP(); 

                }
                break;
            case 19 :
                // SQL99.g:1:101: BY
                {
                mBY(); 

                }
                break;
            case 20 :
                // SQL99.g:1:104: AS
                {
                mAS(); 

                }
                break;
            case 21 :
                // SQL99.g:1:107: JOIN
                {
                mJOIN(); 

                }
                break;
            case 22 :
                // SQL99.g:1:112: INNER
                {
                mINNER(); 

                }
                break;
            case 23 :
                // SQL99.g:1:118: OUTER
                {
                mOUTER(); 

                }
                break;
            case 24 :
                // SQL99.g:1:124: LEFT
                {
                mLEFT(); 

                }
                break;
            case 25 :
                // SQL99.g:1:129: RIGHT
                {
                mRIGHT(); 

                }
                break;
            case 26 :
                // SQL99.g:1:135: FULL
                {
                mFULL(); 

                }
                break;
            case 27 :
                // SQL99.g:1:140: UNION
                {
                mUNION(); 

                }
                break;
            case 28 :
                // SQL99.g:1:146: ON
                {
                mON(); 

                }
                break;
            case 29 :
                // SQL99.g:1:149: IN
                {
                mIN(); 

                }
                break;
            case 30 :
                // SQL99.g:1:152: IS
                {
                mIS(); 

                }
                break;
            case 31 :
                // SQL99.g:1:155: NULL
                {
                mNULL(); 

                }
                break;
            case 32 :
                // SQL99.g:1:160: FALSE
                {
                mFALSE(); 

                }
                break;
            case 33 :
                // SQL99.g:1:166: TRUE
                {
                mTRUE(); 

                }
                break;
            case 34 :
                // SQL99.g:1:171: SEMI
                {
                mSEMI(); 

                }
                break;
            case 35 :
                // SQL99.g:1:176: DOT
                {
                mDOT(); 

                }
                break;
            case 36 :
                // SQL99.g:1:180: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 37 :
                // SQL99.g:1:186: LSQ_BRACKET
                {
                mLSQ_BRACKET(); 

                }
                break;
            case 38 :
                // SQL99.g:1:198: RSQ_BRACKET
                {
                mRSQ_BRACKET(); 

                }
                break;
            case 39 :
                // SQL99.g:1:210: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 40 :
                // SQL99.g:1:217: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 41 :
                // SQL99.g:1:224: QUESTION
                {
                mQUESTION(); 

                }
                break;
            case 42 :
                // SQL99.g:1:233: DOLLAR
                {
                mDOLLAR(); 

                }
                break;
            case 43 :
                // SQL99.g:1:240: QUOTE_DOUBLE
                {
                mQUOTE_DOUBLE(); 

                }
                break;
            case 44 :
                // SQL99.g:1:253: QUOTE_SINGLE
                {
                mQUOTE_SINGLE(); 

                }
                break;
            case 45 :
                // SQL99.g:1:266: APOSTROPHE
                {
                mAPOSTROPHE(); 

                }
                break;
            case 46 :
                // SQL99.g:1:277: UNDERSCORE
                {
                mUNDERSCORE(); 

                }
                break;
            case 47 :
                // SQL99.g:1:288: DASH
                {
                mDASH(); 

                }
                break;
            case 48 :
                // SQL99.g:1:293: ASTERISK
                {
                mASTERISK(); 

                }
                break;
            case 49 :
                // SQL99.g:1:302: AMPERSAND
                {
                mAMPERSAND(); 

                }
                break;
            case 50 :
                // SQL99.g:1:312: AT
                {
                mAT(); 

                }
                break;
            case 51 :
                // SQL99.g:1:315: EXCLAMATION
                {
                mEXCLAMATION(); 

                }
                break;
            case 52 :
                // SQL99.g:1:327: HASH
                {
                mHASH(); 

                }
                break;
            case 53 :
                // SQL99.g:1:332: PERCENT
                {
                mPERCENT(); 

                }
                break;
            case 54 :
                // SQL99.g:1:340: PLUS
                {
                mPLUS(); 

                }
                break;
            case 55 :
                // SQL99.g:1:345: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 56 :
                // SQL99.g:1:352: COLON
                {
                mCOLON(); 

                }
                break;
            case 57 :
                // SQL99.g:1:358: LESS
                {
                mLESS(); 

                }
                break;
            case 58 :
                // SQL99.g:1:363: GREATER
                {
                mGREATER(); 

                }
                break;
            case 59 :
                // SQL99.g:1:371: SLASH
                {
                mSLASH(); 

                }
                break;
            case 60 :
                // SQL99.g:1:377: DOUBLE_SLASH
                {
                mDOUBLE_SLASH(); 

                }
                break;
            case 61 :
                // SQL99.g:1:390: BACKSLASH
                {
                mBACKSLASH(); 

                }
                break;
            case 62 :
                // SQL99.g:1:400: TILDE
                {
                mTILDE(); 

                }
                break;
            case 63 :
                // SQL99.g:1:406: CARET
                {
                mCARET(); 

                }
                break;
            case 64 :
                // SQL99.g:1:412: NUMERIC
                {
                mNUMERIC(); 

                }
                break;
            case 65 :
                // SQL99.g:1:420: STRING
                {
                mSTRING(); 

                }
                break;
            case 66 :
                // SQL99.g:1:427: STRING_WITH_QUOTE_DOUBLE
                {
                mSTRING_WITH_QUOTE_DOUBLE(); 

                }
                break;
            case 67 :
                // SQL99.g:1:452: STRING_WITH_QUOTE
                {
                mSTRING_WITH_QUOTE(); 

                }
                break;
            case 68 :
                // SQL99.g:1:470: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA7 dfa7 = new DFA7(this);
    static final String DFA7_eotS =
        "\23\61\11\uffff\1\121\1\125\1\uffff\1\126\1\127\13\uffff\1\131\3"+
        "\uffff\1\132\2\uffff\7\61\1\143\10\61\1\155\1\61\1\157\3\61\1\163"+
        "\1\61\1\166\1\167\4\61\12\uffff\1\61\1\176\2\61\1\u0081\1\u0082"+
        "\1\u0083\1\u0084\1\uffff\1\u0085\1\u0086\7\61\1\uffff\1\61\1\uffff"+
        "\1\u008f\2\61\1\uffff\2\61\2\uffff\4\61\1\uffff\1\61\1\uffff\1\u0099"+
        "\1\61\6\uffff\2\61\1\u009d\1\u009e\4\61\1\uffff\1\u00a3\1\61\1\u00a5"+
        "\1\61\1\u00a7\2\61\1\u00aa\1\61\1\uffff\1\61\1\u00ad\1\u00ae\2\uffff"+
        "\1\u00af\1\u00b0\1\u00b1\1\u00b2\1\uffff\1\u00b3\1\uffff\1\u00b4"+
        "\1\uffff\1\u00b5\1\u00b6\1\uffff\1\u00b7\1\61\13\uffff\1\61\1\u00ba"+
        "\1\uffff";
    static final String DFA7_eofS =
        "\u00bb\uffff";
    static final String DFA7_minS =
        "\1\11\1\105\1\111\1\114\1\101\1\126\1\117\1\101\1\110\1\116\1\117"+
        "\1\122\1\131\1\117\1\116\1\105\1\111\1\116\1\122\11\uffff\2\42\1"+
        "\uffff\2\55\13\uffff\1\57\3\uffff\1\55\2\uffff\1\114\2\115\1\123"+
        "\1\114\1\107\1\104\1\55\1\130\1\116\1\105\1\125\1\117\2\114\1\105"+
        "\1\55\1\124\1\55\1\124\1\114\1\117\1\55\1\111\2\55\1\106\1\107\1"+
        "\111\1\125\1\uffff\1\42\10\uffff\1\105\1\55\1\105\1\124\4\55\1\uffff"+
        "\2\55\1\122\1\116\1\115\1\114\1\123\1\122\1\105\1\uffff\1\105\1"+
        "\uffff\1\55\1\114\1\125\1\uffff\1\116\1\105\2\uffff\1\124\1\110"+
        "\1\117\1\105\1\uffff\1\103\1\uffff\1\55\1\111\6\uffff\1\131\1\124"+
        "\2\55\2\105\2\122\1\uffff\1\55\1\120\1\55\1\122\1\55\1\124\1\116"+
        "\1\55\1\124\1\uffff\1\116\2\55\2\uffff\4\55\1\uffff\1\55\1\uffff"+
        "\1\55\1\uffff\2\55\1\uffff\1\55\1\103\13\uffff\1\124\1\55\1\uffff";
    static final String DFA7_maxS =
        "\1\176\1\165\1\151\1\166\1\151\1\166\1\157\1\165\1\150\2\165\1\162"+
        "\1\171\1\157\1\163\1\145\1\151\1\156\1\162\11\uffff\2\172\1\uffff"+
        "\2\172\13\uffff\1\57\3\uffff\1\172\2\uffff\1\154\2\155\1\163\1\154"+
        "\1\147\1\171\1\172\1\170\1\156\1\145\1\165\1\157\2\154\1\145\1\172"+
        "\1\164\1\172\1\164\1\154\1\157\1\172\1\151\2\172\1\146\1\147\1\151"+
        "\1\165\1\uffff\1\172\10\uffff\1\145\1\172\1\145\1\164\4\172\1\uffff"+
        "\2\172\1\162\1\156\1\155\1\154\1\163\1\162\1\145\1\uffff\1\145\1"+
        "\uffff\1\172\1\154\1\165\1\uffff\1\156\1\145\2\uffff\1\164\1\150"+
        "\1\157\1\145\1\uffff\1\143\1\uffff\1\172\1\151\6\uffff\1\171\1\164"+
        "\2\172\2\145\2\162\1\uffff\1\172\1\160\1\172\1\162\1\172\1\164\1"+
        "\156\1\172\1\164\1\uffff\1\156\2\172\2\uffff\4\172\1\uffff\1\172"+
        "\1\uffff\1\172\1\uffff\2\172\1\uffff\1\172\1\143\13\uffff\1\164"+
        "\1\172\1\uffff";
    static final String DFA7_acceptS =
        "\23\uffff\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\2\uffff\1"+
        "\55\2\uffff\1\60\1\61\1\62\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1"+
        "\72\1\uffff\1\75\1\76\1\77\1\uffff\1\101\1\104\36\uffff\1\53\1\uffff"+
        "\1\102\1\103\1\54\1\56\1\57\1\74\1\73\1\100\10\uffff\1\24\11\uffff"+
        "\1\17\1\uffff\1\34\3\uffff\1\23\2\uffff\1\35\1\36\4\uffff\1\102"+
        "\1\uffff\1\7\2\uffff\1\3\1\4\1\11\1\16\1\5\1\6\10\uffff\1\20\11"+
        "\uffff\1\12\3\uffff\1\14\1\32\4\uffff\1\37\1\uffff\1\25\1\uffff"+
        "\1\30\2\uffff\1\41\2\uffff\1\10\1\13\1\40\1\15\1\21\1\27\1\22\1"+
        "\26\1\31\1\33\1\1\2\uffff\1\2";
    static final String DFA7_specialS =
        "\u00bb\uffff}>";
    static final String[] DFA7_transitionS = {
            "\2\62\2\uffff\1\62\22\uffff\1\62\1\44\1\34\1\45\1\33\1\46\1"+
            "\42\1\35\1\30\1\31\1\41\1\47\1\25\1\40\1\24\1\54\12\60\1\51"+
            "\1\23\1\52\1\50\1\53\1\32\1\43\1\3\1\14\1\6\1\2\1\5\1\7\1\13"+
            "\1\uffff\1\16\1\15\1\uffff\1\17\1\4\1\12\1\11\2\uffff\1\20\1"+
            "\1\1\22\1\21\1\uffff\1\10\3\uffff\1\26\1\55\1\27\1\57\1\37\1"+
            "\36\1\3\1\14\1\6\1\2\1\5\1\7\1\13\1\uffff\1\16\1\15\1\uffff"+
            "\1\17\1\4\1\12\1\11\2\uffff\1\20\1\1\1\22\1\21\1\uffff\1\10"+
            "\6\uffff\1\56",
            "\1\63\11\uffff\1\65\5\uffff\1\64\17\uffff\1\63\11\uffff\1\65"+
            "\5\uffff\1\64",
            "\1\66\37\uffff\1\66",
            "\1\67\1\uffff\1\71\4\uffff\1\72\2\uffff\1\70\25\uffff\1\67"+
            "\1\uffff\1\71\4\uffff\1\72\2\uffff\1\70",
            "\1\73\7\uffff\1\74\27\uffff\1\73\7\uffff\1\74",
            "\1\75\37\uffff\1\75",
            "\1\76\37\uffff\1\76",
            "\1\101\20\uffff\1\77\2\uffff\1\100\13\uffff\1\101\20\uffff"+
            "\1\77\2\uffff\1\100",
            "\1\102\37\uffff\1\102",
            "\1\105\3\uffff\1\103\2\uffff\1\104\30\uffff\1\105\3\uffff\1"+
            "\103\2\uffff\1\104",
            "\1\106\5\uffff\1\107\31\uffff\1\106\5\uffff\1\107",
            "\1\110\37\uffff\1\110",
            "\1\111\37\uffff\1\111",
            "\1\112\37\uffff\1\112",
            "\1\113\4\uffff\1\114\32\uffff\1\113\4\uffff\1\114",
            "\1\115\37\uffff\1\115",
            "\1\116\37\uffff\1\116",
            "\1\117\37\uffff\1\117",
            "\1\120\37\uffff\1\120",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\123\4\uffff\1\124\5\uffff\1\122\2\uffff\12\122\7\uffff\32"+
            "\122\4\uffff\1\122\1\uffff\32\122",
            "\1\124\4\uffff\1\124\5\uffff\1\124\2\uffff\12\124\7\uffff\32"+
            "\124\4\uffff\1\124\1\uffff\32\124",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
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
            "\1\130",
            "",
            "",
            "",
            "\1\61\2\uffff\12\60\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "",
            "",
            "\1\133\37\uffff\1\133",
            "\1\134\37\uffff\1\134",
            "\1\135\37\uffff\1\135",
            "\1\136\37\uffff\1\136",
            "\1\137\37\uffff\1\137",
            "\1\140\37\uffff\1\140",
            "\1\142\24\uffff\1\141\12\uffff\1\142\24\uffff\1\141",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\144\37\uffff\1\144",
            "\1\145\37\uffff\1\145",
            "\1\146\37\uffff\1\146",
            "\1\147\37\uffff\1\147",
            "\1\150\37\uffff\1\150",
            "\1\151\37\uffff\1\151",
            "\1\152\37\uffff\1\152",
            "\1\153\37\uffff\1\153",
            "\1\61\2\uffff\12\61\7\uffff\3\61\1\154\26\61\4\uffff\1\61\1"+
            "\uffff\3\61\1\154\26\61",
            "\1\156\37\uffff\1\156",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\160\37\uffff\1\160",
            "\1\161\37\uffff\1\161",
            "\1\162\37\uffff\1\162",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\164\37\uffff\1\164",
            "\1\61\2\uffff\12\61\7\uffff\15\61\1\165\14\61\4\uffff\1\61"+
            "\1\uffff\15\61\1\165\14\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\170\37\uffff\1\170",
            "\1\171\37\uffff\1\171",
            "\1\172\37\uffff\1\172",
            "\1\173\37\uffff\1\173",
            "",
            "\1\123\4\uffff\1\124\5\uffff\1\122\2\uffff\12\122\7\uffff\32"+
            "\122\4\uffff\1\122\1\uffff\32\122",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\175\37\uffff\1\175",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\177\37\uffff\1\177",
            "\1\u0080\37\uffff\1\u0080",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u0087\37\uffff\1\u0087",
            "\1\u0088\37\uffff\1\u0088",
            "\1\u0089\37\uffff\1\u0089",
            "\1\u008a\37\uffff\1\u008a",
            "\1\u008b\37\uffff\1\u008b",
            "\1\u008c\37\uffff\1\u008c",
            "\1\u008d\37\uffff\1\u008d",
            "",
            "\1\u008e\37\uffff\1\u008e",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u0090\37\uffff\1\u0090",
            "\1\u0091\37\uffff\1\u0091",
            "",
            "\1\u0092\37\uffff\1\u0092",
            "\1\u0093\37\uffff\1\u0093",
            "",
            "",
            "\1\u0094\37\uffff\1\u0094",
            "\1\u0095\37\uffff\1\u0095",
            "\1\u0096\37\uffff\1\u0096",
            "\1\u0097\37\uffff\1\u0097",
            "",
            "\1\u0098\37\uffff\1\u0098",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u009a\37\uffff\1\u009a",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u009b\37\uffff\1\u009b",
            "\1\u009c\37\uffff\1\u009c",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u009f\37\uffff\1\u009f",
            "\1\u00a0\37\uffff\1\u00a0",
            "\1\u00a1\37\uffff\1\u00a1",
            "\1\u00a2\37\uffff\1\u00a2",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u00a4\37\uffff\1\u00a4",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u00a6\37\uffff\1\u00a6",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u00a8\37\uffff\1\u00a8",
            "\1\u00a9\37\uffff\1\u00a9",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u00ab\37\uffff\1\u00ab",
            "",
            "\1\u00ac\37\uffff\1\u00ac",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            "\1\u00b8\37\uffff\1\u00b8",
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
            "\1\u00b9\37\uffff\1\u00b9",
            "\1\61\2\uffff\12\61\7\uffff\32\61\4\uffff\1\61\1\uffff\32\61",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( SELECT | DISTINCT | ALL | AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT | FROM | WHERE | AND | OR | NOT | ORDER | GROUP | BY | AS | JOIN | INNER | OUTER | LEFT | RIGHT | FULL | UNION | ON | IN | IS | NULL | FALSE | TRUE | SEMI | DOT | COMMA | LSQ_BRACKET | RSQ_BRACKET | LPAREN | RPAREN | QUESTION | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | APOSTROPHE | UNDERSCORE | DASH | ASTERISK | AMPERSAND | AT | EXCLAMATION | HASH | PERCENT | PLUS | EQUALS | COLON | LESS | GREATER | SLASH | DOUBLE_SLASH | BACKSLASH | TILDE | CARET | NUMERIC | STRING | STRING_WITH_QUOTE_DOUBLE | STRING_WITH_QUOTE | WS );";
        }
    }
 

}