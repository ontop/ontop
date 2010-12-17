// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g 2010-07-07 15:25:32

package inf.unibz.it.ucq.parser.datalog;

import java.util.LinkedList;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@Deprecated
public class DatalogCQLexer extends Lexer {
    public static final int WS=8;
    public static final int T__15=15;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int T__10=10;
    public static final int CHAR=7;
    public static final int ALPHAVAR=4;
    public static final int INT=6;
    public static final int EOF=-1;
    public static final int T__9=9;
    public static final int ALPHA=5;


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

    public DatalogCQLexer() {;} 
    public DatalogCQLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public DatalogCQLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g"; }

    // $ANTLR start "T__9"
    public final void mT__9() throws RecognitionException {
        try {
            int _type = T__9;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:26:6: ( ',' )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:26:8: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__9"

    // $ANTLR start "T__10"
    public final void mT__10() throws RecognitionException {
        try {
            int _type = T__10;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:27:7: ( '(' )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:27:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__10"

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:28:7: ( ')' )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:28:9: ')'
            {
            match(')'); 

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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:29:7: ( ':' )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:29:9: ':'
            {
            match(':'); 

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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:30:7: ( '$' )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:30:9: '$'
            {
            match('$'); 

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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:31:7: ( '?' )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:31:9: '?'
            {
            match('?'); 

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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:32:7: ( '\\'' )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:32:9: '\\''
            {
            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "ALPHAVAR"
    public final void mALPHAVAR() throws RecognitionException {
        try {
            int _type = ALPHAVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:379:11: ( ( ALPHA | INT | CHAR )+ )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:379:15: ( ALPHA | INT | CHAR )+
            {
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:379:15: ( ALPHA | INT | CHAR )+
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
                case '!':
                case '#':
                case '%':
                case '&':
                case '*':
                case '+':
                case '-':
                case '.':
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
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:379:16: ALPHA
            	    {
            	    mALPHA(); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:379:24: INT
            	    {
            	    mINT(); 

            	    }
            	    break;
            	case 3 :
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:379:30: CHAR
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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:382:8: ( ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | '.' ) )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:382:10: ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | '.' )
            {
            if ( input.LA(1)=='!'||input.LA(1)=='#'||(input.LA(1)>='%' && input.LA(1)<='&')||(input.LA(1)>='*' && input.LA(1)<='+')||(input.LA(1)>='-' && input.LA(1)<='.')||input.LA(1)==':'||input.LA(1)=='='||input.LA(1)=='@'||input.LA(1)=='_' ) {
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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:385:9: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:385:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
            {
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:385:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
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
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:
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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:388:7: ( ( '0' .. '9' )+ )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:388:11: ( '0' .. '9' )+
            {
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:388:11: ( '0' .. '9' )+
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
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:388:11: '0' .. '9'
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
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:7: ( ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+ )
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
            {
            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
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
                case '\r':
                    {
                    alt5=3;
                    }
                    break;

                }

                switch (alt5) {
            	case 1 :
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:12: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:16: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:21: ( '\\r' | '\\r\\n' )
            	    {
            	    // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:21: ( '\\r' | '\\r\\n' )
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
            	            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:22: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:390:27: '\\r\\n'
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
        // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:8: ( T__9 | T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | ALPHAVAR | WS )
        int alt6=9;
        alt6 = dfa6.predict(input);
        switch (alt6) {
            case 1 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:10: T__9
                {
                mT__9(); 

                }
                break;
            case 2 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:15: T__10
                {
                mT__10(); 

                }
                break;
            case 3 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:21: T__11
                {
                mT__11(); 

                }
                break;
            case 4 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:27: T__12
                {
                mT__12(); 

                }
                break;
            case 5 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:33: T__13
                {
                mT__13(); 

                }
                break;
            case 6 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:39: T__14
                {
                mT__14(); 

                }
                break;
            case 7 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:45: T__15
                {
                mT__15(); 

                }
                break;
            case 8 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:51: ALPHAVAR
                {
                mALPHAVAR(); 

                }
                break;
            case 9 :
                // C:\\Users\\obda\\workspacenew\\obdalib\\src\\main\\java\\inf\\unibz\\it\\ucq\\parser\\datalog\\DatalogCQ.g:1:60: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA6_eotS =
        "\4\uffff\1\12\6\uffff";
    static final String DFA6_eofS =
        "\13\uffff";
    static final String DFA6_minS =
        "\1\11\3\uffff\1\41\6\uffff";
    static final String DFA6_maxS =
        "\1\172\3\uffff\1\172\6\uffff";
    static final String DFA6_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\uffff\1\5\1\6\1\7\1\10\1\11\1\4";
    static final String DFA6_specialS =
        "\13\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\11\3\uffff\1\11\22\uffff\1\11\1\10\1\uffff\1\10\1\5\2\10"+
            "\1\7\1\2\1\3\2\10\1\1\2\10\1\uffff\12\10\1\4\2\uffff\1\10\1"+
            "\uffff\1\6\33\10\4\uffff\1\10\1\uffff\32\10",
            "",
            "",
            "",
            "\1\10\1\uffff\1\10\1\uffff\2\10\3\uffff\2\10\1\uffff\2\10"+
            "\1\uffff\13\10\2\uffff\1\10\2\uffff\33\10\4\uffff\1\10\1\uffff"+
            "\32\10",
            "",
            "",
            "",
            "",
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
            return "1:1: Tokens : ( T__9 | T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | ALPHAVAR | WS );";
        }
    }
 

}