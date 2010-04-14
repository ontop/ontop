// $ANTLR 3.1.1 /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g 2009-02-13 11:21:54

package inf.unibz.it.ucq.parser.datalog;

import java.util.LinkedList;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DatalogCQLexer extends Lexer {
    public static final int WS=8;
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
    public String getGrammarFileName() { return "/Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g"; }

    // $ANTLR start "T__9"
    public final void mT__9() throws RecognitionException {
        try {
            int _type = T__9;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:26:6: ( ',' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:26:8: ','
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:27:7: ( '(' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:27:9: '('
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:28:7: ( ')' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:28:9: ')'
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:29:7: ( '$' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:29:9: '$'
            {
            match('$'); 

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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:30:7: ( '?' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:30:9: '?'
            {
            match('?'); 

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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:31:7: ( '\\'' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:31:9: '\\''
            {
            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "ALPHAVAR"
    public final void mALPHAVAR() throws RecognitionException {
        try {
            int _type = ALPHAVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:298:11: ( ( ALPHA | INT | CHAR )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:298:15: ( ALPHA | INT | CHAR )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:298:15: ( ALPHA | INT | CHAR )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:298:16: ALPHA
            	    {
            	    mALPHA(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:298:24: INT
            	    {
            	    mINT(); 

            	    }
            	    break;
            	case 3 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:298:30: CHAR
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:301:8: ( ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | '.' ) )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:301:10: ( '_' | '-' | '*' | '&' | '@' | '!' | '#' | '%' | '+' | '=' | ':' | '.' )
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:304:9: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:304:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:304:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:307:7: ( ( '0' .. '9' )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:307:11: ( '0' .. '9' )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:307:11: ( '0' .. '9' )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:307:11: '0' .. '9'
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
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:7: ( ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+ )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
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
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:12: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:16: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:21: ( '\\r' | '\\r\\n' )
            	    {
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:21: ( '\\r' | '\\r\\n' )
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
            	            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:22: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;
            	        case 2 :
            	            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:309:27: '\\r\\n'
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
        // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:8: ( T__9 | T__10 | T__11 | T__12 | T__13 | T__14 | ALPHAVAR | WS )
        int alt6=8;
        switch ( input.LA(1) ) {
        case ',':
            {
            alt6=1;
            }
            break;
        case '(':
            {
            alt6=2;
            }
            break;
        case ')':
            {
            alt6=3;
            }
            break;
        case '$':
            {
            alt6=4;
            }
            break;
        case '?':
            {
            alt6=5;
            }
            break;
        case '\'':
            {
            alt6=6;
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
        case ':':
        case '=':
        case '@':
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
        case '_':
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
            alt6=7;
            }
            break;
        case '\t':
        case '\r':
        case ' ':
            {
            alt6=8;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 6, 0, input);

            throw nvae;
        }

        switch (alt6) {
            case 1 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:10: T__9
                {
                mT__9(); 

                }
                break;
            case 2 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:15: T__10
                {
                mT__10(); 

                }
                break;
            case 3 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:21: T__11
                {
                mT__11(); 

                }
                break;
            case 4 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:27: T__12
                {
                mT__12(); 

                }
                break;
            case 5 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:33: T__13
                {
                mT__13(); 

                }
                break;
            case 6 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:39: T__14
                {
                mT__14(); 

                }
                break;
            case 7 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:45: ALPHAVAR
                {
                mALPHAVAR(); 

                }
                break;
            case 8 :
                // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:1:54: WS
                {
                mWS(); 

                }
                break;

        }

    }


 

}