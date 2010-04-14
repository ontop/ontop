// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\Users\\obda\\Desktop\\DependencyAssertion.g 2010-02-04 09:54:23

package inf.unibz.it.obda.dependencies.parser;

import java.util.LinkedList;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DependencyAssertionLexer extends Lexer {
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int HEAD=5;
    public static final int CHAR=9;
    public static final int INT=8;
    public static final int EOF=-1;
    public static final int ALPHA=7;
    public static final int T__19=19;
    public static final int WS=10;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALPHAVAR=6;
    public static final int BODY=4;


        private List<String> errors = new LinkedList<String>();
        public void displayRecognitionError(String[] tokenNames,
                                            RecognitionException e) {
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
    public String getGrammarFileName() { return "C:\\Users\\obda\\Desktop\\DependencyAssertion.g"; }

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:23:7: ( ',' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:23:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__11"

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:24:7: ( '(' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:24:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:25:7: ( ';' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:25:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__13"

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:26:7: ( ')' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:26:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:27:7: ( 'includedIn' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:27:9: 'includedIn'
            {
            match("includedIn"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:28:7: ( 'disjoint' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:28:9: 'disjoint'
            {
            match("disjoint"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:29:7: ( 'functionOf' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:29:9: 'functionOf'
            {
            match("functionOf"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:30:7: ( '[' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:30:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:31:7: ( ']' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:31:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:32:7: ( '.' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:32:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:33:7: ( '$' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:33:9: '$'
            {
            match('$'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "BODY"
    public final void mBODY() throws RecognitionException {
        try {
            int _type = BODY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:205:7: ( 'Body' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:205:9: 'Body'
            {
            match("Body"); 


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
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:206:6: ( 'Head' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:206:8: 'Head'
            {
            match("Head"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HEAD"

    // $ANTLR start "ALPHAVAR"
    public final void mALPHAVAR() throws RecognitionException {
        try {
            int _type = ALPHAVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:208:11: ( ( ALPHA | INT | CHAR )+ )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:208:15: ( ALPHA | INT | CHAR )+
            {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:208:15: ( ALPHA | INT | CHAR )+
            int cnt1=0;
            loop1:
            do {
                int alt1=4;
                switch ( input.LA(1) ) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt1=1;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt1=2;
                    }
                    break;
                case ' ':
                case '!':
                case '#':
                case '%':
                case '&':
                case '*':
                case '+':
                case ',':
                case '-':
                case ':':
                case '=':
                case '@':
                case '_':
                    {
                    alt1=3;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:208:16: ALPHA
            	    {
            	    mALPHA(); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:208:24: INT
            	    {
            	    mINT(); 

            	    }
            	    break;
            	case 3 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:208:30: CHAR
            	    {
            	    mCHAR(); 

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
    // $ANTLR end "ALPHAVAR"

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:211:8: ( ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | ' ' | ',' ) )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:211:10: ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | ' ' | ',' )
            {
            if ( (input.LA(1)>=' ' && input.LA(1)<='!')||input.LA(1)=='#'||(input.LA(1)>='%' && input.LA(1)<='&')||(input.LA(1)>='*' && input.LA(1)<='-')||input.LA(1)==':'||input.LA(1)=='='||input.LA(1)=='@'||input.LA(1)=='_' ) {
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

    // $ANTLR start "ALPHA"
    public final void mALPHA() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:214:9: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:214:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
            {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:214:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='A' && LA2_0<='Z')||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:
            	    {
            	    if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


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

        }
        finally {
        }
    }
    // $ANTLR end "ALPHA"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:217:7: ( ( '0' .. '9' )+ )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:217:11: ( '0' .. '9' )+
            {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:217:11: ( '0' .. '9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:217:11: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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


            }

        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:7: ( ( '\\t' | ( '\\r' | '\\r\\n' ) )+ )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:11: ( '\\t' | ( '\\r' | '\\r\\n' ) )+
            {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:11: ( '\\t' | ( '\\r' | '\\r\\n' ) )+
            int cnt5=0;
            loop5:
            do {
                int alt5=3;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\t') ) {
                    alt5=1;
                }
                else if ( (LA5_0=='\r') ) {
                    alt5=2;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:12: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:17: ( '\\r' | '\\r\\n' )
            	    {
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:17: ( '\\r' | '\\r\\n' )
            	    int alt4=2;
            	    int LA4_0 = input.LA(1);

            	    if ( (LA4_0=='\r') ) {
            	        int LA4_1 = input.LA(2);

            	        if ( (LA4_1=='\n') ) {
            	            alt4=2;
            	        }
            	        else {
            	            alt4=1;}
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 4, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt4) {
            	        case 1 :
            	            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:18: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:219:23: '\\r\\n'
            	            {
            	            match("\r\n"); 


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

            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:8: ( T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | BODY | HEAD | ALPHAVAR | WS )
        int alt6=15;
        alt6 = dfa6.predict(input);
        switch (alt6) {
            case 1 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:10: T__11
                {
                mT__11(); 

                }
                break;
            case 2 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:16: T__12
                {
                mT__12(); 

                }
                break;
            case 3 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:22: T__13
                {
                mT__13(); 

                }
                break;
            case 4 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:28: T__14
                {
                mT__14(); 

                }
                break;
            case 5 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:34: T__15
                {
                mT__15(); 

                }
                break;
            case 6 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:40: T__16
                {
                mT__16(); 

                }
                break;
            case 7 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:46: T__17
                {
                mT__17(); 

                }
                break;
            case 8 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:52: T__18
                {
                mT__18(); 

                }
                break;
            case 9 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:58: T__19
                {
                mT__19(); 

                }
                break;
            case 10 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:64: T__20
                {
                mT__20(); 

                }
                break;
            case 11 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:70: T__21
                {
                mT__21(); 

                }
                break;
            case 12 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:76: BODY
                {
                mBODY(); 

                }
                break;
            case 13 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:81: HEAD
                {
                mHEAD(); 

                }
                break;
            case 14 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:86: ALPHAVAR
                {
                mALPHAVAR(); 

                }
                break;
            case 15 :
                // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:1:95: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA6_eotS =
        "\1\uffff\1\20\3\uffff\3\16\4\uffff\2\16\3\uffff\15\16\1\43\1\44"+
        "\3\16\2\uffff\7\16\1\57\2\16\1\uffff\1\16\1\63\1\64\2\uffff";
    static final String DFA6_eofS =
        "\65\uffff";
    static final String DFA6_minS =
        "\1\11\1\40\3\uffff\1\156\1\151\1\165\4\uffff\1\157\1\145\3\uffff"+
        "\1\143\1\163\1\156\1\144\1\141\1\154\1\152\1\143\1\171\1\144\1\165"+
        "\1\157\1\164\2\40\1\144\2\151\2\uffff\1\145\1\156\1\157\1\144\1"+
        "\164\1\156\1\111\1\40\1\117\1\156\1\uffff\1\146\2\40\2\uffff";
    static final String DFA6_maxS =
        "\2\172\3\uffff\1\156\1\151\1\165\4\uffff\1\157\1\145\3\uffff\1"+
        "\143\1\163\1\156\1\144\1\141\1\154\1\152\1\143\1\171\1\144\1\165"+
        "\1\157\1\164\2\172\1\144\2\151\2\uffff\1\145\1\156\1\157\1\144\1"+
        "\164\1\156\1\111\1\172\1\117\1\156\1\uffff\1\146\2\172\2\uffff";
    static final String DFA6_acceptS =
        "\2\uffff\1\2\1\3\1\4\3\uffff\1\10\1\11\1\12\1\13\2\uffff\1\16\1"+
        "\17\1\1\22\uffff\1\14\1\15\12\uffff\1\6\3\uffff\1\5\1\7";
    static final String DFA6_specialS =
        "\65\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\17\3\uffff\1\17\22\uffff\2\16\1\uffff\1\16\1\13\2\16\1\uffff"+
            "\1\2\1\4\2\16\1\1\1\16\1\12\1\uffff\13\16\1\3\1\uffff\1\16\2"+
            "\uffff\2\16\1\14\5\16\1\15\22\16\1\10\1\uffff\1\11\1\uffff\1"+
            "\16\1\uffff\3\16\1\6\1\16\1\7\2\16\1\5\21\16",
            "\2\16\1\uffff\1\16\1\uffff\2\16\3\uffff\4\16\2\uffff\13\16"+
            "\2\uffff\1\16\2\uffff\33\16\4\uffff\1\16\1\uffff\32\16",
            "",
            "",
            "",
            "\1\21",
            "\1\22",
            "\1\23",
            "",
            "",
            "",
            "",
            "\1\24",
            "\1\25",
            "",
            "",
            "",
            "\1\26",
            "\1\27",
            "\1\30",
            "\1\31",
            "\1\32",
            "\1\33",
            "\1\34",
            "\1\35",
            "\1\36",
            "\1\37",
            "\1\40",
            "\1\41",
            "\1\42",
            "\2\16\1\uffff\1\16\1\uffff\2\16\3\uffff\4\16\2\uffff\13\16"+
            "\2\uffff\1\16\2\uffff\33\16\4\uffff\1\16\1\uffff\32\16",
            "\2\16\1\uffff\1\16\1\uffff\2\16\3\uffff\4\16\2\uffff\13\16"+
            "\2\uffff\1\16\2\uffff\33\16\4\uffff\1\16\1\uffff\32\16",
            "\1\45",
            "\1\46",
            "\1\47",
            "",
            "",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\54",
            "\1\55",
            "\1\56",
            "\2\16\1\uffff\1\16\1\uffff\2\16\3\uffff\4\16\2\uffff\13\16"+
            "\2\uffff\1\16\2\uffff\33\16\4\uffff\1\16\1\uffff\32\16",
            "\1\60",
            "\1\61",
            "",
            "\1\62",
            "\2\16\1\uffff\1\16\1\uffff\2\16\3\uffff\4\16\2\uffff\13\16"+
            "\2\uffff\1\16\2\uffff\33\16\4\uffff\1\16\1\uffff\32\16",
            "\2\16\1\uffff\1\16\1\uffff\2\16\3\uffff\4\16\2\uffff\13\16"+
            "\2\uffff\1\16\2\uffff\33\16\4\uffff\1\16\1\uffff\32\16",
            "",
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
            return "1:1: Tokens : ( T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | BODY | HEAD | ALPHAVAR | WS );";
        }
    }
 

}