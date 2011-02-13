// $ANTLR 3.3 Nov 30, 2010 12:50:56 Constraints.g 2011-01-19 14:13:15

package inf.unibz.it.obda.constraints.parser;

import java.util.List;
import java.util.Vector;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class ConstraintsLexer extends Lexer {
    public static final int EOF=-1;
    public static final int CHECK=4;
    public static final int LPAREN=5;
    public static final int RPAREN=6;
    public static final int COMMA=7;
    public static final int UNIQUE=8;
    public static final int REFERENCES=9;
    public static final int PRIMARY_KEY=10;
    public static final int STRING_LITERAL=11;
    public static final int STRING_LITERAL2=12;
    public static final int NUMBER=13;
    public static final int TRUE=14;
    public static final int FALSE=15;
    public static final int DOLLAR=16;
    public static final int STRING=17;
    public static final int LESS=18;
    public static final int GREATER=19;
    public static final int EQUALS=20;
    public static final int LESS_OR_EQUAL=21;
    public static final int GREATER_OR_EQUAL=22;
    public static final int DOT=23;
    public static final int QUOTE_DOUBLE=24;
    public static final int QUOTE_SINGLE=25;
    public static final int UNDERSCORE=26;
    public static final int DASH=27;
    public static final int ALPHA=28;
    public static final int DIGIT=29;
    public static final int ALPHANUM=30;
    public static final int WS=31;

    private Vector<String> errors = new Vector<String>();

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

    public ConstraintsLexer() {;} 
    public ConstraintsLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public ConstraintsLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "Constraints.g"; }

    // $ANTLR start "CHECK"
    public final void mCHECK() throws RecognitionException {
        try {
            int _type = CHECK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:266:6: ( ( 'C' | 'c' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'K' | 'k' ) )
            // Constraints.g:266:8: ( 'C' | 'c' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'K' | 'k' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
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
    // $ANTLR end "CHECK"

    // $ANTLR start "UNIQUE"
    public final void mUNIQUE() throws RecognitionException {
        try {
            int _type = UNIQUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:268:7: ( ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'Q' | 'q' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
            // Constraints.g:268:9: ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'Q' | 'q' ) ( 'U' | 'u' ) ( 'E' | 'e' )
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

            if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
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
    // $ANTLR end "UNIQUE"

    // $ANTLR start "REFERENCES"
    public final void mREFERENCES() throws RecognitionException {
        try {
            int _type = REFERENCES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:270:11: ( ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'E' | 'e' ) ( 'S' | 's' ) )
            // Constraints.g:270:13: ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'E' | 'e' ) ( 'S' | 's' )
            {
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

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
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
    // $ANTLR end "REFERENCES"

    // $ANTLR start "PRIMARY_KEY"
    public final void mPRIMARY_KEY() throws RecognitionException {
        try {
            int _type = PRIMARY_KEY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:272:12: ( ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'Y' | 'y' ) ( ' ' ) ( 'K' | 'k' ) ( 'E' | 'e' ) ( 'Y' | 'y' ) )
            // Constraints.g:272:14: ( 'P' | 'p' ) ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'Y' | 'y' ) ( ' ' ) ( 'K' | 'k' ) ( 'E' | 'e' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
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

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
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

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
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

            // Constraints.g:272:77: ( ' ' )
            // Constraints.g:272:78: ' '
            {
            match(' '); 

            }

            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
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
    // $ANTLR end "PRIMARY_KEY"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:274:5: ( ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' ) )
            // Constraints.g:274:7: ( 'T' | 't' ) ( 'R' | 'r' ) ( 'U' | 'u' ) ( 'E' | 'e' )
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

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:276:6: ( ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // Constraints.g:276:8: ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'S' | 's' ) ( 'E' | 'e' )
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

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:278:4: ( '.' )
            // Constraints.g:278:18: '.'
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
            // Constraints.g:279:6: ( ',' )
            // Constraints.g:279:18: ','
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

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:280:7: ( '(' )
            // Constraints.g:280:18: '('
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
            // Constraints.g:281:7: ( ')' )
            // Constraints.g:281:18: ')'
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

    // $ANTLR start "DOLLAR"
    public final void mDOLLAR() throws RecognitionException {
        try {
            int _type = DOLLAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:282:7: ( '$' )
            // Constraints.g:282:18: '$'
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
            // Constraints.g:283:13: ( '\"' )
            // Constraints.g:283:18: '\"'
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
            // Constraints.g:284:13: ( '\\'' )
            // Constraints.g:284:18: '\\''
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

    // $ANTLR start "UNDERSCORE"
    public final void mUNDERSCORE() throws RecognitionException {
        try {
            int _type = UNDERSCORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:285:11: ( '_' )
            // Constraints.g:285:18: '_'
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
            // Constraints.g:286:5: ( '-' )
            // Constraints.g:286:18: '-'
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

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:287:5: ( '<' )
            // Constraints.g:287:18: '<'
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
            // Constraints.g:288:8: ( '>' )
            // Constraints.g:288:18: '>'
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

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:289:7: ( '=' )
            // Constraints.g:289:18: '='
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

    // $ANTLR start "LESS_OR_EQUAL"
    public final void mLESS_OR_EQUAL() throws RecognitionException {
        try {
            int _type = LESS_OR_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:290:14: ( '<=' )
            // Constraints.g:290:18: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS_OR_EQUAL"

    // $ANTLR start "GREATER_OR_EQUAL"
    public final void mGREATER_OR_EQUAL() throws RecognitionException {
        try {
            int _type = GREATER_OR_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:291:17: ( '>=' )
            // Constraints.g:291:18: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER_OR_EQUAL"

    // $ANTLR start "ALPHA"
    public final void mALPHA() throws RecognitionException {
        try {
            // Constraints.g:293:15: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // Constraints.g:293:17: ( 'a' .. 'z' | 'A' .. 'Z' )
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
            // Constraints.g:295:15: ( '0' .. '9' )
            // Constraints.g:295:17: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:297:7: ( ( DIGIT )+ ( DOT ( DIGIT )+ )? )
            // Constraints.g:297:9: ( DIGIT )+ ( DOT ( DIGIT )+ )?
            {
            // Constraints.g:297:9: ( DIGIT )+
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
            	    // Constraints.g:297:9: DIGIT
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

            // Constraints.g:297:16: ( DOT ( DIGIT )+ )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='.') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // Constraints.g:297:17: DOT ( DIGIT )+
                    {
                    mDOT(); 
                    // Constraints.g:297:21: ( DIGIT )+
                    int cnt2=0;
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // Constraints.g:297:21: DIGIT
                    	    {
                    	    mDIGIT(); 

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
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "ALPHANUM"
    public final void mALPHANUM() throws RecognitionException {
        try {
            // Constraints.g:299:18: ( ( ALPHA | DIGIT ) )
            // Constraints.g:299:20: ( ALPHA | DIGIT )
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

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:301:7: ( ( ALPHANUM | UNDERSCORE | DASH )+ )
            // Constraints.g:301:9: ( ALPHANUM | UNDERSCORE | DASH )+
            {
            // Constraints.g:301:9: ( ALPHANUM | UNDERSCORE | DASH )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='-'||(LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='Z')||LA4_0=='_'||(LA4_0>='a' && LA4_0<='z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // Constraints.g:
            	    {
            	    if ( input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:304:3: ( QUOTE_DOUBLE ( options {greedy=false; } : . )* QUOTE_DOUBLE )
            // Constraints.g:304:5: QUOTE_DOUBLE ( options {greedy=false; } : . )* QUOTE_DOUBLE
            {
            mQUOTE_DOUBLE(); 
            // Constraints.g:304:18: ( options {greedy=false; } : . )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\"') ) {
                    alt5=2;
                }
                else if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // Constraints.g:304:45: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop5;
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
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "STRING_LITERAL2"
    public final void mSTRING_LITERAL2() throws RecognitionException {
        try {
            int _type = STRING_LITERAL2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:308:3: ( QUOTE_SINGLE ( options {greedy=false; } : . )* QUOTE_SINGLE )
            // Constraints.g:308:5: QUOTE_SINGLE ( options {greedy=false; } : . )* QUOTE_SINGLE
            {
            mQUOTE_SINGLE(); 
            // Constraints.g:308:18: ( options {greedy=false; } : . )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\'') ) {
                    alt6=2;
                }
                else if ( ((LA6_0>='\u0000' && LA6_0<='&')||(LA6_0>='(' && LA6_0<='\uFFFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // Constraints.g:308:45: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            mQUOTE_SINGLE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL2"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Constraints.g:311:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
            // Constraints.g:311:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            {
            // Constraints.g:311:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            int cnt8=0;
            loop8:
            do {
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
            	    // Constraints.g:311:6: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // Constraints.g:311:10: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // Constraints.g:311:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    {
            	    // Constraints.g:311:15: ( '\\n' | '\\r' ( '\\n' ) )
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
            	            // Constraints.g:311:16: '\\n'
            	            {
            	            match('\n'); 

            	            }
            	            break;
            	        case 2 :
            	            // Constraints.g:311:21: '\\r' ( '\\n' )
            	            {
            	            match('\r'); 
            	            // Constraints.g:311:25: ( '\\n' )
            	            // Constraints.g:311:26: '\\n'
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
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
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
        // Constraints.g:1:8: ( CHECK | UNIQUE | REFERENCES | PRIMARY_KEY | TRUE | FALSE | DOT | COMMA | LPAREN | RPAREN | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | UNDERSCORE | DASH | LESS | GREATER | EQUALS | LESS_OR_EQUAL | GREATER_OR_EQUAL | NUMBER | STRING | STRING_LITERAL | STRING_LITERAL2 | WS )
        int alt9=25;
        alt9 = dfa9.predict(input);
        switch (alt9) {
            case 1 :
                // Constraints.g:1:10: CHECK
                {
                mCHECK(); 

                }
                break;
            case 2 :
                // Constraints.g:1:16: UNIQUE
                {
                mUNIQUE(); 

                }
                break;
            case 3 :
                // Constraints.g:1:23: REFERENCES
                {
                mREFERENCES(); 

                }
                break;
            case 4 :
                // Constraints.g:1:34: PRIMARY_KEY
                {
                mPRIMARY_KEY(); 

                }
                break;
            case 5 :
                // Constraints.g:1:46: TRUE
                {
                mTRUE(); 

                }
                break;
            case 6 :
                // Constraints.g:1:51: FALSE
                {
                mFALSE(); 

                }
                break;
            case 7 :
                // Constraints.g:1:57: DOT
                {
                mDOT(); 

                }
                break;
            case 8 :
                // Constraints.g:1:61: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 9 :
                // Constraints.g:1:67: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 10 :
                // Constraints.g:1:74: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 11 :
                // Constraints.g:1:81: DOLLAR
                {
                mDOLLAR(); 

                }
                break;
            case 12 :
                // Constraints.g:1:88: QUOTE_DOUBLE
                {
                mQUOTE_DOUBLE(); 

                }
                break;
            case 13 :
                // Constraints.g:1:101: QUOTE_SINGLE
                {
                mQUOTE_SINGLE(); 

                }
                break;
            case 14 :
                // Constraints.g:1:114: UNDERSCORE
                {
                mUNDERSCORE(); 

                }
                break;
            case 15 :
                // Constraints.g:1:125: DASH
                {
                mDASH(); 

                }
                break;
            case 16 :
                // Constraints.g:1:130: LESS
                {
                mLESS(); 

                }
                break;
            case 17 :
                // Constraints.g:1:135: GREATER
                {
                mGREATER(); 

                }
                break;
            case 18 :
                // Constraints.g:1:143: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 19 :
                // Constraints.g:1:150: LESS_OR_EQUAL
                {
                mLESS_OR_EQUAL(); 

                }
                break;
            case 20 :
                // Constraints.g:1:164: GREATER_OR_EQUAL
                {
                mGREATER_OR_EQUAL(); 

                }
                break;
            case 21 :
                // Constraints.g:1:181: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 22 :
                // Constraints.g:1:188: STRING
                {
                mSTRING(); 

                }
                break;
            case 23 :
                // Constraints.g:1:195: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 24 :
                // Constraints.g:1:210: STRING_LITERAL2
                {
                mSTRING_LITERAL2(); 

                }
                break;
            case 25 :
                // Constraints.g:1:226: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA9_eotS =
        "\1\uffff\6\24\5\uffff\1\34\1\36\1\40\1\41\1\43\1\45\1\uffff\1\46"+
        "\2\uffff\6\24\13\uffff\12\24\1\67\1\24\1\71\3\24\1\uffff\1\75\1"+
        "\uffff\1\76\2\24\2\uffff\3\24\1\uffff\1\24\1\105\1\uffff";
    static final String DFA9_eofS =
        "\106\uffff";
    static final String DFA9_minS =
        "\1\11\1\110\1\116\1\105\2\122\1\101\5\uffff\2\0\2\55\2\75\1\uffff"+
        "\1\55\2\uffff\1\105\1\111\1\106\1\111\1\125\1\114\13\uffff\1\103"+
        "\1\121\1\105\1\115\1\105\1\123\1\113\1\125\1\122\1\101\1\55\1\105"+
        "\1\55\2\105\1\122\1\uffff\1\55\1\uffff\1\55\1\116\1\131\2\uffff"+
        "\1\103\1\40\1\105\1\uffff\1\123\1\55\1\uffff";
    static final String DFA9_maxS =
        "\1\172\1\150\1\156\1\145\2\162\1\141\5\uffff\2\uffff\2\172\2\75"+
        "\1\uffff\1\172\2\uffff\1\145\1\151\1\146\1\151\1\165\1\154\13\uffff"+
        "\1\143\1\161\1\145\1\155\1\145\1\163\1\153\1\165\1\162\1\141\1\172"+
        "\1\145\1\172\2\145\1\162\1\uffff\1\172\1\uffff\1\172\1\156\1\171"+
        "\2\uffff\1\143\1\40\1\145\1\uffff\1\163\1\172\1\uffff";
    static final String DFA9_acceptS =
        "\7\uffff\1\7\1\10\1\11\1\12\1\13\6\uffff\1\22\1\uffff\1\26\1\31"+
        "\6\uffff\1\14\1\27\1\15\1\30\1\16\1\17\1\23\1\20\1\24\1\21\1\25"+
        "\20\uffff\1\5\1\uffff\1\1\3\uffff\1\6\1\2\3\uffff\1\4\2\uffff\1"+
        "\3";
    static final String DFA9_specialS =
        "\14\uffff\1\0\1\1\70\uffff}>";
    static final String[] DFA9_transitionS = {
            "\2\25\2\uffff\1\25\22\uffff\1\25\1\uffff\1\14\1\uffff\1\13\2"+
            "\uffff\1\15\1\11\1\12\2\uffff\1\10\1\17\1\7\1\uffff\12\23\2"+
            "\uffff\1\20\1\22\1\21\2\uffff\2\24\1\1\2\24\1\6\11\24\1\4\1"+
            "\24\1\3\1\24\1\5\1\2\5\24\4\uffff\1\16\1\uffff\2\24\1\1\2\24"+
            "\1\6\11\24\1\4\1\24\1\3\1\24\1\5\1\2\5\24",
            "\1\26\37\uffff\1\26",
            "\1\27\37\uffff\1\27",
            "\1\30\37\uffff\1\30",
            "\1\31\37\uffff\1\31",
            "\1\32\37\uffff\1\32",
            "\1\33\37\uffff\1\33",
            "",
            "",
            "",
            "",
            "",
            "\0\35",
            "\0\37",
            "\1\24\2\uffff\12\24\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
            "\1\24\2\uffff\12\24\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
            "\1\42",
            "\1\44",
            "",
            "\1\24\2\uffff\12\23\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
            "",
            "",
            "\1\47\37\uffff\1\47",
            "\1\50\37\uffff\1\50",
            "\1\51\37\uffff\1\51",
            "\1\52\37\uffff\1\52",
            "\1\53\37\uffff\1\53",
            "\1\54\37\uffff\1\54",
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
            "\1\55\37\uffff\1\55",
            "\1\56\37\uffff\1\56",
            "\1\57\37\uffff\1\57",
            "\1\60\37\uffff\1\60",
            "\1\61\37\uffff\1\61",
            "\1\62\37\uffff\1\62",
            "\1\63\37\uffff\1\63",
            "\1\64\37\uffff\1\64",
            "\1\65\37\uffff\1\65",
            "\1\66\37\uffff\1\66",
            "\1\24\2\uffff\12\24\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
            "\1\70\37\uffff\1\70",
            "\1\24\2\uffff\12\24\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
            "\1\72\37\uffff\1\72",
            "\1\73\37\uffff\1\73",
            "\1\74\37\uffff\1\74",
            "",
            "\1\24\2\uffff\12\24\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
            "",
            "\1\24\2\uffff\12\24\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
            "\1\77\37\uffff\1\77",
            "\1\100\37\uffff\1\100",
            "",
            "",
            "\1\101\37\uffff\1\101",
            "\1\102",
            "\1\103\37\uffff\1\103",
            "",
            "\1\104\37\uffff\1\104",
            "\1\24\2\uffff\12\24\7\uffff\32\24\4\uffff\1\24\1\uffff\32\24",
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
            return "1:1: Tokens : ( CHECK | UNIQUE | REFERENCES | PRIMARY_KEY | TRUE | FALSE | DOT | COMMA | LPAREN | RPAREN | DOLLAR | QUOTE_DOUBLE | QUOTE_SINGLE | UNDERSCORE | DASH | LESS | GREATER | EQUALS | LESS_OR_EQUAL | GREATER_OR_EQUAL | NUMBER | STRING | STRING_LITERAL | STRING_LITERAL2 | WS );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA9_12 = input.LA(1);

                        s = -1;
                        if ( ((LA9_12>='\u0000' && LA9_12<='\uFFFF')) ) {s = 29;}

                        else s = 28;

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA9_13 = input.LA(1);

                        s = -1;
                        if ( ((LA9_13>='\u0000' && LA9_13<='\uFFFF')) ) {s = 31;}

                        else s = 30;

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