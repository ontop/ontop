// $ANTLR 3.1.1 /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g 2009-02-09 17:11:51


package inf.unibz.it.dl.parser.manchester;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ExpressionLexer extends Lexer {
    public static final int WS=8;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__10=10;
    public static final int CHAR=7;
    public static final int ALPHAVAR=4;
    public static final int INT=6;
    public static final int EOF=-1;
    public static final int T__9=9;
    public static final int ALPHA=5;



    // delegates
    // delegators

    public ExpressionLexer() {;} 
    public ExpressionLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public ExpressionLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g"; }

    // $ANTLR start "T__9"
    public final void mT__9() throws RecognitionException {
        try {
            int _type = T__9;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:11:6: ( '(' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:11:8: '('
            {
            match('('); 

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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:12:7: ( 'some' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:12:9: 'some'
            {
            match("some"); 


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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:13:7: ( ')' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:13:9: ')'
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:14:7: ( 'inverseof' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:14:9: 'inverseof'
            {
            match("inverseof"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "ALPHAVAR"
    public final void mALPHAVAR() throws RecognitionException {
        try {
            int _type = ALPHAVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:179:11: ( ( ALPHA | INT | CHAR )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:179:15: ( ALPHA | INT | CHAR )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:179:15: ( ALPHA | INT | CHAR )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:179:16: ALPHA
            	    {
            	    mALPHA(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:179:24: INT
            	    {
            	    mINT(); 

            	    }
            	    break;
            	case 3 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:179:30: CHAR
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:182:8: ( ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | '.' ) )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:182:10: ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | '.' )
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:185:9: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:185:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:185:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:188:7: ( ( '0' .. '9' )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:188:11: ( '0' .. '9' )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:188:11: ( '0' .. '9' )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:188:11: '0' .. '9'
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:7: ( ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:12: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:16: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:21: ( '\\r' | '\\r\\n' )
            	    {
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:21: ( '\\r' | '\\r\\n' )
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
            	            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:22: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;
            	        case 2 :
            	            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:190:27: '\\r\\n'
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
        // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:1:8: ( T__9 | T__10 | T__11 | T__12 | ALPHAVAR | WS )
        int alt6=6;
        alt6 = dfa6.predict(input);
        switch (alt6) {
            case 1 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:1:10: T__9
                {
                mT__9(); 

                }
                break;
            case 2 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:1:15: T__10
                {
                mT__10(); 

                }
                break;
            case 3 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:1:21: T__11
                {
                mT__11(); 

                }
                break;
            case 4 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:1:27: T__12
                {
                mT__12(); 

                }
                break;
            case 5 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:1:33: ALPHAVAR
                {
                mALPHAVAR(); 

                }
                break;
            case 6 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:1:42: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA6_eotS =
        "\2\uffff\1\5\1\uffff\1\5\2\uffff\4\5\1\15\1\5\1\uffff\4\5\1\23\1"+
        "\uffff";
    static final String DFA6_eofS =
        "\24\uffff";
    static final String DFA6_minS =
        "\1\11\1\uffff\1\157\1\uffff\1\156\2\uffff\1\155\1\166\2\145\1\41"+
        "\1\162\1\uffff\1\163\1\145\1\157\1\146\1\41\1\uffff";
    static final String DFA6_maxS =
        "\1\172\1\uffff\1\157\1\uffff\1\156\2\uffff\1\155\1\166\2\145\1\172"+
        "\1\162\1\uffff\1\163\1\145\1\157\1\146\1\172\1\uffff";
    static final String DFA6_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\1\uffff\1\5\1\6\6\uffff\1\2\5\uffff\1\4";
    static final String DFA6_specialS =
        "\24\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\6\3\uffff\1\6\22\uffff\1\6\1\5\1\uffff\1\5\1\uffff\2\5\1"+
            "\uffff\1\1\1\3\2\5\1\uffff\2\5\1\uffff\13\5\2\uffff\1\5\2\uffff"+
            "\33\5\4\uffff\1\5\1\uffff\10\5\1\4\11\5\1\2\7\5",
            "",
            "\1\7",
            "",
            "\1\10",
            "",
            "",
            "\1\11",
            "\1\12",
            "\1\13",
            "\1\14",
            "\1\5\1\uffff\1\5\1\uffff\2\5\3\uffff\2\5\1\uffff\2\5\1\uffff"+
            "\13\5\2\uffff\1\5\2\uffff\33\5\4\uffff\1\5\1\uffff\32\5",
            "\1\16",
            "",
            "\1\17",
            "\1\20",
            "\1\21",
            "\1\22",
            "\1\5\1\uffff\1\5\1\uffff\2\5\3\uffff\2\5\1\uffff\2\5\1\uffff"+
            "\13\5\2\uffff\1\5\2\uffff\33\5\4\uffff\1\5\1\uffff\32\5",
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
            return "1:1: Tokens : ( T__9 | T__10 | T__11 | T__12 | ALPHAVAR | WS );";
        }
    }
 

}