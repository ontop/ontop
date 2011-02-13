// $ANTLR 3.3 Nov 30, 2010 12:50:56 DependencyAssertion.g 2011-01-19 16:47:23

package inf.unibz.it.obda.dependencies.parser;

import java.util.List;
import java.util.Vector;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class DependencyAssertionLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int SEMI=4;
    public static final int LPAREN=5;
    public static final int COMMA=6;
    public static final int RPAREN=7;
    public static final int LSQ_BRACKET=8;
    public static final int RSQ_BRACKET=9;
    public static final int BODY=10;
    public static final int HEAD=11;
    public static final int DOT=12;
    public static final int DOLLAR=13;
    public static final int STRING=14;
    public static final int UNDERSCORE=15;
    public static final int DASH=16;
    public static final int ALPHA=17;
    public static final int DIGIT=18;
    public static final int ALPHANUM=19;
    public static final int WS=20;

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

    public DependencyAssertionLexer() {;} 
    public DependencyAssertionLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public DependencyAssertionLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "DependencyAssertion.g"; }

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:22:7: ( 'includedIn' )
            // DependencyAssertion.g:22:9: 'includedIn'
            {
            match("includedIn"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:23:7: ( 'disjoint' )
            // DependencyAssertion.g:23:9: 'disjoint'
            {
            match("disjoint"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:24:7: ( 'functionOf' )
            // DependencyAssertion.g:24:9: 'functionOf'
            {
            match("functionOf"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "BODY"
    public final void mBODY() throws RecognitionException {
        try {
            int _type = BODY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:228:5: ( ( 'B' | 'b' ) ( 'O' | 'o' ) ( 'D' | 'd' ) ( 'Y' | 'y' ) )
            // DependencyAssertion.g:228:7: ( 'B' | 'b' ) ( 'O' | 'o' ) ( 'D' | 'd' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
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

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
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
    // $ANTLR end "BODY"

    // $ANTLR start "HEAD"
    public final void mHEAD() throws RecognitionException {
        try {
            int _type = HEAD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:230:5: ( ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'D' | 'd' ) )
            // DependencyAssertion.g:230:7: ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'D' | 'd' )
            {
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

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
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
    // $ANTLR end "HEAD"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:232:4: ( '.' )
            // DependencyAssertion.g:232:16: '.'
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

    // $ANTLR start "SEMI"
    public final void mSEMI() throws RecognitionException {
        try {
            int _type = SEMI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:233:5: ( ';' )
            // DependencyAssertion.g:233:16: ';'
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

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:234:6: ( ',' )
            // DependencyAssertion.g:234:16: ','
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
            // DependencyAssertion.g:235:7: ( '(' )
            // DependencyAssertion.g:235:16: '('
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
            // DependencyAssertion.g:236:7: ( ')' )
            // DependencyAssertion.g:236:16: ')'
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

    // $ANTLR start "LSQ_BRACKET"
    public final void mLSQ_BRACKET() throws RecognitionException {
        try {
            int _type = LSQ_BRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:237:12: ( '[' )
            // DependencyAssertion.g:237:16: '['
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
            // DependencyAssertion.g:238:12: ( ']' )
            // DependencyAssertion.g:238:16: ']'
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

    // $ANTLR start "DOLLAR"
    public final void mDOLLAR() throws RecognitionException {
        try {
            int _type = DOLLAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:239:7: ( '$' )
            // DependencyAssertion.g:239:16: '$'
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

    // $ANTLR start "UNDERSCORE"
    public final void mUNDERSCORE() throws RecognitionException {
        try {
            int _type = UNDERSCORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:240:11: ( '_' )
            // DependencyAssertion.g:240:16: '_'
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
            // DependencyAssertion.g:241:5: ( '-' )
            // DependencyAssertion.g:241:16: '-'
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

    // $ANTLR start "ALPHA"
    public final void mALPHA() throws RecognitionException {
        try {
            // DependencyAssertion.g:243:15: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // DependencyAssertion.g:243:17: ( 'a' .. 'z' | 'A' .. 'Z' )
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
            // DependencyAssertion.g:245:15: ( '0' .. '9' )
            // DependencyAssertion.g:245:17: '0' .. '9'
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
            // DependencyAssertion.g:247:18: ( ( ALPHA | DIGIT ) )
            // DependencyAssertion.g:247:20: ( ALPHA | DIGIT )
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
            // DependencyAssertion.g:249:7: ( ( ALPHANUM | UNDERSCORE | DASH )+ )
            // DependencyAssertion.g:249:9: ( ALPHANUM | UNDERSCORE | DASH )+
            {
            // DependencyAssertion.g:249:9: ( ALPHANUM | UNDERSCORE | DASH )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='-'||(LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // DependencyAssertion.g:
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
    // $ANTLR end "STRING"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // DependencyAssertion.g:251:3: ( ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+ )
            // DependencyAssertion.g:251:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            {
            // DependencyAssertion.g:251:5: ( ' ' | '\\t' | ( '\\n' | '\\r' ( '\\n' ) ) )+
            int cnt3=0;
            loop3:
            do {
                int alt3=4;
                switch ( input.LA(1) ) {
                case ' ':
                    {
                    alt3=1;
                    }
                    break;
                case '\t':
                    {
                    alt3=2;
                    }
                    break;
                case '\n':
                case '\r':
                    {
                    alt3=3;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // DependencyAssertion.g:251:6: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // DependencyAssertion.g:251:10: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // DependencyAssertion.g:251:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    {
            	    // DependencyAssertion.g:251:15: ( '\\n' | '\\r' ( '\\n' ) )
            	    int alt2=2;
            	    int LA2_0 = input.LA(1);

            	    if ( (LA2_0=='\n') ) {
            	        alt2=1;
            	    }
            	    else if ( (LA2_0=='\r') ) {
            	        alt2=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 2, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt2) {
            	        case 1 :
            	            // DependencyAssertion.g:251:16: '\\n'
            	            {
            	            match('\n'); 

            	            }
            	            break;
            	        case 2 :
            	            // DependencyAssertion.g:251:21: '\\r' ( '\\n' )
            	            {
            	            match('\r'); 
            	            // DependencyAssertion.g:251:25: ( '\\n' )
            	            // DependencyAssertion.g:251:26: '\\n'
            	            {
            	            match('\n'); 

            	            }


            	            }
            	            break;

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
        // DependencyAssertion.g:1:8: ( T__21 | T__22 | T__23 | BODY | HEAD | DOT | SEMI | COMMA | LPAREN | RPAREN | LSQ_BRACKET | RSQ_BRACKET | DOLLAR | UNDERSCORE | DASH | STRING | WS )
        int alt4=17;
        alt4 = dfa4.predict(input);
        switch (alt4) {
            case 1 :
                // DependencyAssertion.g:1:10: T__21
                {
                mT__21(); 

                }
                break;
            case 2 :
                // DependencyAssertion.g:1:16: T__22
                {
                mT__22(); 

                }
                break;
            case 3 :
                // DependencyAssertion.g:1:22: T__23
                {
                mT__23(); 

                }
                break;
            case 4 :
                // DependencyAssertion.g:1:28: BODY
                {
                mBODY(); 

                }
                break;
            case 5 :
                // DependencyAssertion.g:1:33: HEAD
                {
                mHEAD(); 

                }
                break;
            case 6 :
                // DependencyAssertion.g:1:38: DOT
                {
                mDOT(); 

                }
                break;
            case 7 :
                // DependencyAssertion.g:1:42: SEMI
                {
                mSEMI(); 

                }
                break;
            case 8 :
                // DependencyAssertion.g:1:47: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 9 :
                // DependencyAssertion.g:1:53: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 10 :
                // DependencyAssertion.g:1:60: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 11 :
                // DependencyAssertion.g:1:67: LSQ_BRACKET
                {
                mLSQ_BRACKET(); 

                }
                break;
            case 12 :
                // DependencyAssertion.g:1:79: RSQ_BRACKET
                {
                mRSQ_BRACKET(); 

                }
                break;
            case 13 :
                // DependencyAssertion.g:1:91: DOLLAR
                {
                mDOLLAR(); 

                }
                break;
            case 14 :
                // DependencyAssertion.g:1:98: UNDERSCORE
                {
                mUNDERSCORE(); 

                }
                break;
            case 15 :
                // DependencyAssertion.g:1:109: DASH
                {
                mDASH(); 

                }
                break;
            case 16 :
                // DependencyAssertion.g:1:114: STRING
                {
                mSTRING(); 

                }
                break;
            case 17 :
                // DependencyAssertion.g:1:121: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA4 dfa4 = new DFA4(this);
    static final String DFA4_eotS =
        "\1\uffff\5\20\10\uffff\1\27\1\30\2\uffff\5\20\2\uffff\10\20\1\46"+
        "\1\47\3\20\2\uffff\7\20\1\62\2\20\1\uffff\1\20\1\66\1\67\2\uffff";
    static final String DFA4_eofS =
        "\70\uffff";
    static final String DFA4_minS =
        "\1\11\1\156\1\151\1\165\1\117\1\105\10\uffff\2\55\2\uffff\1\143"+
        "\1\163\1\156\1\104\1\101\2\uffff\1\154\1\152\1\143\1\131\1\104\1"+
        "\165\1\157\1\164\2\55\1\144\2\151\2\uffff\1\145\1\156\1\157\1\144"+
        "\1\164\1\156\1\111\1\55\1\117\1\156\1\uffff\1\146\2\55\2\uffff";
    static final String DFA4_maxS =
        "\1\172\1\156\1\151\1\165\1\157\1\145\10\uffff\2\172\2\uffff\1\143"+
        "\1\163\1\156\1\144\1\141\2\uffff\1\154\1\152\1\143\1\171\1\144\1"+
        "\165\1\157\1\164\2\172\1\144\2\151\2\uffff\1\145\1\156\1\157\1\144"+
        "\1\164\1\156\1\111\1\172\1\117\1\156\1\uffff\1\146\2\172\2\uffff";
    static final String DFA4_acceptS =
        "\6\uffff\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\2\uffff\1\20\1\21"+
        "\5\uffff\1\16\1\17\15\uffff\1\4\1\5\12\uffff\1\2\3\uffff\1\1\1\3";
    static final String DFA4_specialS =
        "\70\uffff}>";
    static final String[] DFA4_transitionS = {
            "\2\21\2\uffff\1\21\22\uffff\1\21\3\uffff\1\15\3\uffff\1\11\1"+
            "\12\2\uffff\1\10\1\17\1\6\1\uffff\12\20\1\uffff\1\7\5\uffff"+
            "\1\20\1\4\5\20\1\5\22\20\1\13\1\uffff\1\14\1\uffff\1\16\1\uffff"+
            "\1\20\1\4\1\20\1\2\1\20\1\3\1\20\1\5\1\1\21\20",
            "\1\22",
            "\1\23",
            "\1\24",
            "\1\25\37\uffff\1\25",
            "\1\26\37\uffff\1\26",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "",
            "",
            "\1\31",
            "\1\32",
            "\1\33",
            "\1\34\37\uffff\1\34",
            "\1\35\37\uffff\1\35",
            "",
            "",
            "\1\36",
            "\1\37",
            "\1\40",
            "\1\41\37\uffff\1\41",
            "\1\42\37\uffff\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\50",
            "\1\51",
            "\1\52",
            "",
            "",
            "\1\53",
            "\1\54",
            "\1\55",
            "\1\56",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\63",
            "\1\64",
            "",
            "\1\65",
            "\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\20\2\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__21 | T__22 | T__23 | BODY | HEAD | DOT | SEMI | COMMA | LPAREN | RPAREN | LSQ_BRACKET | RSQ_BRACKET | DOLLAR | UNDERSCORE | DASH | STRING | WS );";
        }
    }
 

}