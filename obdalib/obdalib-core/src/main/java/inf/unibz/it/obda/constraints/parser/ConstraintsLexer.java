// $ANTLR 3.1.2 /home/obda/Constraints.g 2009-11-14 10:44:45

package inf.unibz.it.obda.constraints.parser;

import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class ConstraintsLexer extends Lexer {
    public static final int INTEGER=10;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int NUMBER=4;
    public static final int BOOL=5;
    public static final int CHAR=9;
    public static final int EOF=-1;
    public static final int ALPHA=7;
    public static final int T__19=19;
    public static final int WS=11;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALPHAVAR=6;
    public static final int DIGIT=8;


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

    public ConstraintsLexer() {;} 
    public ConstraintsLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public ConstraintsLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/obda/Constraints.g"; }

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/Constraints.g:23:7: ( 'CHECK' )
            // /home/obda/Constraints.g:23:9: 'CHECK'
            {
            match("CHECK"); 


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
            // /home/obda/Constraints.g:24:7: ( ',' )
            // /home/obda/Constraints.g:24:9: ','
            {
            match(','); 

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
            // /home/obda/Constraints.g:25:7: ( 'UNIQUE (' )
            // /home/obda/Constraints.g:25:9: 'UNIQUE ('
            {
            match("UNIQUE ("); 


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
            // /home/obda/Constraints.g:26:7: ( ')' )
            // /home/obda/Constraints.g:26:9: ')'
            {
            match(')'); 

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
            // /home/obda/Constraints.g:27:7: ( '(' )
            // /home/obda/Constraints.g:27:9: '('
            {
            match('('); 

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
            // /home/obda/Constraints.g:28:7: ( ') REFERENCES' )
            // /home/obda/Constraints.g:28:9: ') REFERENCES'
            {
            match(") REFERENCES"); 


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
            // /home/obda/Constraints.g:29:7: ( 'PRIMARY KEY (' )
            // /home/obda/Constraints.g:29:9: 'PRIMARY KEY ('
            {
            match("PRIMARY KEY ("); 


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
            // /home/obda/Constraints.g:30:7: ( '\\'' )
            // /home/obda/Constraints.g:30:9: '\\''
            {
            match('\''); 

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
            // /home/obda/Constraints.g:31:7: ( '$' )
            // /home/obda/Constraints.g:31:9: '$'
            {
            match('$'); 

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
            // /home/obda/Constraints.g:32:7: ( '<' )
            // /home/obda/Constraints.g:32:9: '<'
            {
            match('<'); 

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
            // /home/obda/Constraints.g:33:7: ( '>' )
            // /home/obda/Constraints.g:33:9: '>'
            {
            match('>'); 

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
            // /home/obda/Constraints.g:34:7: ( '=' )
            // /home/obda/Constraints.g:34:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/Constraints.g:35:7: ( '<=' )
            // /home/obda/Constraints.g:35:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/Constraints.g:36:7: ( '>=' )
            // /home/obda/Constraints.g:36:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "ALPHAVAR"
    public final void mALPHAVAR() throws RecognitionException {
        try {
            int _type = ALPHAVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/Constraints.g:334:11: ( ALPHA ( ALPHA | DIGIT | CHAR )* )
            // /home/obda/Constraints.g:334:13: ALPHA ( ALPHA | DIGIT | CHAR )*
            {
            mALPHA(); 
            // /home/obda/Constraints.g:334:20: ( ALPHA | DIGIT | CHAR )*
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
                case ':':
                case '@':
                case '_':
                    {
                    alt1=3;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // /home/obda/Constraints.g:334:21: ALPHA
            	    {
            	    mALPHA(); 

            	    }
            	    break;
            	case 2 :
            	    // /home/obda/Constraints.g:334:29: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;
            	case 3 :
            	    // /home/obda/Constraints.g:334:37: CHAR
            	    {
            	    mCHAR(); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
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
            // /home/obda/Constraints.g:338:8: ( ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | ':' ) )
            // /home/obda/Constraints.g:338:10: ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | ':' )
            {
            if ( input.LA(1)=='!'||input.LA(1)=='#'||(input.LA(1)>='%' && input.LA(1)<='&')||(input.LA(1)>='*' && input.LA(1)<='+')||input.LA(1)=='-'||input.LA(1)==':'||input.LA(1)=='@'||input.LA(1)=='_' ) {
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
            // /home/obda/Constraints.g:341:9: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
            // /home/obda/Constraints.g:341:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
            {
            // /home/obda/Constraints.g:341:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
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
            	    // /home/obda/Constraints.g:
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

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:344:9: ( DIGIT ( DIGIT )* )
            // /home/obda/Constraints.g:344:11: DIGIT ( DIGIT )*
            {
            mDIGIT(); 
            // /home/obda/Constraints.g:344:16: ( DIGIT )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/obda/Constraints.g:344:17: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/Constraints.g:345:8: ( INTEGER ( '.' INTEGER )? )
            // /home/obda/Constraints.g:345:10: INTEGER ( '.' INTEGER )?
            {
            mINTEGER(); 
            // /home/obda/Constraints.g:345:17: ( '.' INTEGER )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='.') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // /home/obda/Constraints.g:345:18: '.' INTEGER
                    {
                    match('.'); 
                    mINTEGER(); 

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

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:347:9: ( ( '0' .. '9' )+ )
            // /home/obda/Constraints.g:347:13: ( '0' .. '9' )+
            {
            // /home/obda/Constraints.g:347:13: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/obda/Constraints.g:347:13: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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


            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "BOOL"
    public final void mBOOL() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:349:6: ( 'true' | 'false' )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='t') ) {
                alt6=1;
            }
            else if ( (LA6_0=='f') ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /home/obda/Constraints.g:349:8: 'true'
                    {
                    match("true"); 


                    }
                    break;
                case 2 :
                    // /home/obda/Constraints.g:349:15: 'false'
                    {
                    match("false"); 


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "BOOL"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/Constraints.g:351:7: ( ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+ )
            // /home/obda/Constraints.g:351:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
            {
            // /home/obda/Constraints.g:351:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
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
                case '\r':
                    {
                    alt8=3;
                    }
                    break;

                }

                switch (alt8) {
            	case 1 :
            	    // /home/obda/Constraints.g:351:12: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // /home/obda/Constraints.g:351:16: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // /home/obda/Constraints.g:351:21: ( '\\r' | '\\r\\n' )
            	    {
            	    // /home/obda/Constraints.g:351:21: ( '\\r' | '\\r\\n' )
            	    int alt7=2;
            	    int LA7_0 = input.LA(1);

            	    if ( (LA7_0=='\r') ) {
            	        int LA7_1 = input.LA(2);

            	        if ( (LA7_1=='\n') ) {
            	            alt7=2;
            	        }
            	        else {
            	            alt7=1;}
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 7, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt7) {
            	        case 1 :
            	            // /home/obda/Constraints.g:351:22: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;
            	        case 2 :
            	            // /home/obda/Constraints.g:351:27: '\\r\\n'
            	            {
            	            match("\r\n"); 


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
        // /home/obda/Constraints.g:1:8: ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | ALPHAVAR | NUMBER | WS )
        int alt9=17;
        alt9 = dfa9.predict(input);
        switch (alt9) {
            case 1 :
                // /home/obda/Constraints.g:1:10: T__12
                {
                mT__12(); 

                }
                break;
            case 2 :
                // /home/obda/Constraints.g:1:16: T__13
                {
                mT__13(); 

                }
                break;
            case 3 :
                // /home/obda/Constraints.g:1:22: T__14
                {
                mT__14(); 

                }
                break;
            case 4 :
                // /home/obda/Constraints.g:1:28: T__15
                {
                mT__15(); 

                }
                break;
            case 5 :
                // /home/obda/Constraints.g:1:34: T__16
                {
                mT__16(); 

                }
                break;
            case 6 :
                // /home/obda/Constraints.g:1:40: T__17
                {
                mT__17(); 

                }
                break;
            case 7 :
                // /home/obda/Constraints.g:1:46: T__18
                {
                mT__18(); 

                }
                break;
            case 8 :
                // /home/obda/Constraints.g:1:52: T__19
                {
                mT__19(); 

                }
                break;
            case 9 :
                // /home/obda/Constraints.g:1:58: T__20
                {
                mT__20(); 

                }
                break;
            case 10 :
                // /home/obda/Constraints.g:1:64: T__21
                {
                mT__21(); 

                }
                break;
            case 11 :
                // /home/obda/Constraints.g:1:70: T__22
                {
                mT__22(); 

                }
                break;
            case 12 :
                // /home/obda/Constraints.g:1:76: T__23
                {
                mT__23(); 

                }
                break;
            case 13 :
                // /home/obda/Constraints.g:1:82: T__24
                {
                mT__24(); 

                }
                break;
            case 14 :
                // /home/obda/Constraints.g:1:88: T__25
                {
                mT__25(); 

                }
                break;
            case 15 :
                // /home/obda/Constraints.g:1:94: ALPHAVAR
                {
                mALPHAVAR(); 

                }
                break;
            case 16 :
                // /home/obda/Constraints.g:1:103: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 17 :
                // /home/obda/Constraints.g:1:110: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA9_eotS =
        "\1\uffff\1\14\1\uffff\1\14\1\22\1\uffff\1\14\2\uffff\1\25\1\27\4"+
        "\uffff\2\14\2\uffff\1\14\4\uffff\6\14\1\41\2\14\1\uffff\2\14\1\uffff"+
        "\1\14\1\uffff";
    static final String DFA9_eofS =
        "\47\uffff";
    static final String DFA9_minS =
        "\1\11\1\110\1\uffff\1\116\1\40\1\uffff\1\122\2\uffff\2\75\4\uffff"+
        "\1\105\1\111\2\uffff\1\111\4\uffff\1\103\1\121\1\115\1\113\1\125"+
        "\1\101\1\41\1\105\1\122\1\uffff\1\40\1\131\1\uffff\1\40\1\uffff";
    static final String DFA9_maxS =
        "\1\172\1\110\1\uffff\1\116\1\40\1\uffff\1\122\2\uffff\2\75\4\uffff"+
        "\1\105\1\111\2\uffff\1\111\4\uffff\1\103\1\121\1\115\1\113\1\125"+
        "\1\101\1\172\1\105\1\122\1\uffff\1\40\1\131\1\uffff\1\40\1\uffff";
    static final String DFA9_acceptS =
        "\2\uffff\1\2\2\uffff\1\5\1\uffff\1\10\1\11\2\uffff\1\14\1\17\1\20"+
        "\1\21\2\uffff\1\6\1\4\1\uffff\1\15\1\12\1\16\1\13\11\uffff\1\1\2"+
        "\uffff\1\3\1\uffff\1\7";
    static final String DFA9_specialS =
        "\47\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\16\3\uffff\1\16\22\uffff\1\16\3\uffff\1\10\2\uffff\1\7\1"+
            "\5\1\4\2\uffff\1\2\3\uffff\12\15\2\uffff\1\11\1\13\1\12\2\uffff"+
            "\2\14\1\1\14\14\1\6\4\14\1\3\5\14\6\uffff\32\14",
            "\1\17",
            "",
            "\1\20",
            "\1\21",
            "",
            "\1\23",
            "",
            "",
            "\1\24",
            "\1\26",
            "",
            "",
            "",
            "",
            "\1\30",
            "\1\31",
            "",
            "",
            "\1\32",
            "",
            "",
            "",
            "",
            "\1\33",
            "\1\34",
            "\1\35",
            "\1\36",
            "\1\37",
            "\1\40",
            "\1\14\1\uffff\1\14\1\uffff\2\14\3\uffff\2\14\1\uffff\1\14\2"+
            "\uffff\13\14\5\uffff\33\14\4\uffff\1\14\1\uffff\32\14",
            "\1\42",
            "\1\43",
            "",
            "\1\44",
            "\1\45",
            "",
            "\1\46",
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
            return "1:1: Tokens : ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | ALPHAVAR | NUMBER | WS );";
        }
    }
 

}