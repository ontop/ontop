// $ANTLR 3.1.2 /home/obda/SQL.g 2009-11-19 11:28:05

package inf.unibz.it.sql.parser;

import java.util.LinkedList;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SQLLexer extends Lexer {
    public static final int INTEGER=9;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int BOOL=10;
    public static final int NUMBER=5;
    public static final int CHAR=8;
    public static final int EOF=-1;
    public static final int ALPHA=6;
    public static final int T__19=19;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__16=16;
    public static final int T__33=33;
    public static final int WS=11;
    public static final int T__15=15;
    public static final int T__34=34;
    public static final int T__18=18;
    public static final int T__35=35;
    public static final int T__17=17;
    public static final int T__36=36;
    public static final int T__12=12;
    public static final int T__37=37;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALPHAVAR=4;
    public static final int DIGIT=7;


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

    public SQLLexer() {;} 
    public SQLLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public SQLLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/obda/SQL.g"; }

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:23:7: ( 'select' )
            // /home/obda/SQL.g:23:9: 'select'
            {
            match("select"); 


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
            // /home/obda/SQL.g:24:7: ( 'SELECT' )
            // /home/obda/SQL.g:24:9: 'SELECT'
            {
            match("SELECT"); 


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
            // /home/obda/SQL.g:25:7: ( 'Select' )
            // /home/obda/SQL.g:25:9: 'Select'
            {
            match("Select"); 


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
            // /home/obda/SQL.g:26:7: ( 'FROM' )
            // /home/obda/SQL.g:26:9: 'FROM'
            {
            match("FROM"); 


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
            // /home/obda/SQL.g:27:7: ( 'from' )
            // /home/obda/SQL.g:27:9: 'from'
            {
            match("from"); 


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
            // /home/obda/SQL.g:28:7: ( 'WHERE' )
            // /home/obda/SQL.g:28:9: 'WHERE'
            {
            match("WHERE"); 


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
            // /home/obda/SQL.g:29:7: ( 'where' )
            // /home/obda/SQL.g:29:9: 'where'
            {
            match("where"); 


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
            // /home/obda/SQL.g:30:7: ( 'DISTINCT' )
            // /home/obda/SQL.g:30:9: 'DISTINCT'
            {
            match("DISTINCT"); 


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
            // /home/obda/SQL.g:31:7: ( 'distinct' )
            // /home/obda/SQL.g:31:9: 'distinct'
            {
            match("distinct"); 


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
            // /home/obda/SQL.g:32:7: ( ',' )
            // /home/obda/SQL.g:32:9: ','
            {
            match(','); 

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
            // /home/obda/SQL.g:33:7: ( '.' )
            // /home/obda/SQL.g:33:9: '.'
            {
            match('.'); 

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
            // /home/obda/SQL.g:34:7: ( 'as' )
            // /home/obda/SQL.g:34:9: 'as'
            {
            match("as"); 


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
            // /home/obda/SQL.g:35:7: ( 'AS' )
            // /home/obda/SQL.g:35:9: 'AS'
            {
            match("AS"); 


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
            // /home/obda/SQL.g:36:7: ( 'AND' )
            // /home/obda/SQL.g:36:9: 'AND'
            {
            match("AND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:37:7: ( 'and' )
            // /home/obda/SQL.g:37:9: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:38:7: ( 'YEAR(' )
            // /home/obda/SQL.g:38:9: 'YEAR('
            {
            match("YEAR("); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:39:7: ( ')' )
            // /home/obda/SQL.g:39:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:40:7: ( '\\'' )
            // /home/obda/SQL.g:40:9: '\\''
            {
            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:41:7: ( '>' )
            // /home/obda/SQL.g:41:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:42:7: ( '=' )
            // /home/obda/SQL.g:42:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:43:7: ( '<' )
            // /home/obda/SQL.g:43:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:44:7: ( '<>' )
            // /home/obda/SQL.g:44:9: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:45:7: ( '<=' )
            // /home/obda/SQL.g:45:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:46:7: ( '>=' )
            // /home/obda/SQL.g:46:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:47:7: ( '=>' )
            // /home/obda/SQL.g:47:9: '=>'
            {
            match("=>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:48:7: ( '=<' )
            // /home/obda/SQL.g:48:9: '=<'
            {
            match("=<"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "ALPHAVAR"
    public final void mALPHAVAR() throws RecognitionException {
        try {
            int _type = ALPHAVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SQL.g:263:11: ( ALPHA ( ALPHA | DIGIT | CHAR )* )
            // /home/obda/SQL.g:263:13: ALPHA ( ALPHA | DIGIT | CHAR )*
            {
            mALPHA(); 
            // /home/obda/SQL.g:263:20: ( ALPHA | DIGIT | CHAR )*
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
                case '-':
                case '@':
                case '_':
                    {
                    alt1=3;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // /home/obda/SQL.g:263:21: ALPHA
            	    {
            	    mALPHA(); 

            	    }
            	    break;
            	case 2 :
            	    // /home/obda/SQL.g:263:29: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;
            	case 3 :
            	    // /home/obda/SQL.g:263:37: CHAR
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
            // /home/obda/SQL.g:267:8: ( ( '_' | '-' | '@' ) )
            // /home/obda/SQL.g:267:10: ( '_' | '-' | '@' )
            {
            if ( input.LA(1)=='-'||input.LA(1)=='@'||input.LA(1)=='_' ) {
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
            // /home/obda/SQL.g:270:9: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
            // /home/obda/SQL.g:270:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
            {
            // /home/obda/SQL.g:270:13: ( 'a' .. 'z' | 'A' .. 'Z' )+
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
            	    // /home/obda/SQL.g:
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
            // /home/obda/SQL.g:273:9: ( DIGIT ( DIGIT )* )
            // /home/obda/SQL.g:273:11: DIGIT ( DIGIT )*
            {
            mDIGIT(); 
            // /home/obda/SQL.g:273:16: ( DIGIT )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/obda/SQL.g:273:17: DIGIT
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
            // /home/obda/SQL.g:274:8: ( INTEGER ( '.' INTEGER )? )
            // /home/obda/SQL.g:274:10: INTEGER ( '.' INTEGER )?
            {
            mINTEGER(); 
            // /home/obda/SQL.g:274:17: ( '.' INTEGER )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='.') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // /home/obda/SQL.g:274:18: '.' INTEGER
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
            // /home/obda/SQL.g:276:9: ( ( '0' .. '9' )+ )
            // /home/obda/SQL.g:276:13: ( '0' .. '9' )+
            {
            // /home/obda/SQL.g:276:13: ( '0' .. '9' )+
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
            	    // /home/obda/SQL.g:276:13: '0' .. '9'
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
            // /home/obda/SQL.g:278:6: ( 'true' | 'false' )
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
                    // /home/obda/SQL.g:278:8: 'true'
                    {
                    match("true"); 


                    }
                    break;
                case 2 :
                    // /home/obda/SQL.g:278:15: 'false'
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
            // /home/obda/SQL.g:280:7: ( ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+ )
            // /home/obda/SQL.g:280:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
            {
            // /home/obda/SQL.g:280:11: ( ' ' | '\\t' | ( '\\r' | '\\r\\n' ) )+
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
            	    // /home/obda/SQL.g:280:12: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 2 :
            	    // /home/obda/SQL.g:280:16: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;
            	case 3 :
            	    // /home/obda/SQL.g:280:21: ( '\\r' | '\\r\\n' )
            	    {
            	    // /home/obda/SQL.g:280:21: ( '\\r' | '\\r\\n' )
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
            	            // /home/obda/SQL.g:280:22: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;
            	        case 2 :
            	            // /home/obda/SQL.g:280:27: '\\r\\n'
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
        // /home/obda/SQL.g:1:8: ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | ALPHAVAR | NUMBER | WS )
        int alt9=29;
        alt9 = dfa9.predict(input);
        switch (alt9) {
            case 1 :
                // /home/obda/SQL.g:1:10: T__12
                {
                mT__12(); 

                }
                break;
            case 2 :
                // /home/obda/SQL.g:1:16: T__13
                {
                mT__13(); 

                }
                break;
            case 3 :
                // /home/obda/SQL.g:1:22: T__14
                {
                mT__14(); 

                }
                break;
            case 4 :
                // /home/obda/SQL.g:1:28: T__15
                {
                mT__15(); 

                }
                break;
            case 5 :
                // /home/obda/SQL.g:1:34: T__16
                {
                mT__16(); 

                }
                break;
            case 6 :
                // /home/obda/SQL.g:1:40: T__17
                {
                mT__17(); 

                }
                break;
            case 7 :
                // /home/obda/SQL.g:1:46: T__18
                {
                mT__18(); 

                }
                break;
            case 8 :
                // /home/obda/SQL.g:1:52: T__19
                {
                mT__19(); 

                }
                break;
            case 9 :
                // /home/obda/SQL.g:1:58: T__20
                {
                mT__20(); 

                }
                break;
            case 10 :
                // /home/obda/SQL.g:1:64: T__21
                {
                mT__21(); 

                }
                break;
            case 11 :
                // /home/obda/SQL.g:1:70: T__22
                {
                mT__22(); 

                }
                break;
            case 12 :
                // /home/obda/SQL.g:1:76: T__23
                {
                mT__23(); 

                }
                break;
            case 13 :
                // /home/obda/SQL.g:1:82: T__24
                {
                mT__24(); 

                }
                break;
            case 14 :
                // /home/obda/SQL.g:1:88: T__25
                {
                mT__25(); 

                }
                break;
            case 15 :
                // /home/obda/SQL.g:1:94: T__26
                {
                mT__26(); 

                }
                break;
            case 16 :
                // /home/obda/SQL.g:1:100: T__27
                {
                mT__27(); 

                }
                break;
            case 17 :
                // /home/obda/SQL.g:1:106: T__28
                {
                mT__28(); 

                }
                break;
            case 18 :
                // /home/obda/SQL.g:1:112: T__29
                {
                mT__29(); 

                }
                break;
            case 19 :
                // /home/obda/SQL.g:1:118: T__30
                {
                mT__30(); 

                }
                break;
            case 20 :
                // /home/obda/SQL.g:1:124: T__31
                {
                mT__31(); 

                }
                break;
            case 21 :
                // /home/obda/SQL.g:1:130: T__32
                {
                mT__32(); 

                }
                break;
            case 22 :
                // /home/obda/SQL.g:1:136: T__33
                {
                mT__33(); 

                }
                break;
            case 23 :
                // /home/obda/SQL.g:1:142: T__34
                {
                mT__34(); 

                }
                break;
            case 24 :
                // /home/obda/SQL.g:1:148: T__35
                {
                mT__35(); 

                }
                break;
            case 25 :
                // /home/obda/SQL.g:1:154: T__36
                {
                mT__36(); 

                }
                break;
            case 26 :
                // /home/obda/SQL.g:1:160: T__37
                {
                mT__37(); 

                }
                break;
            case 27 :
                // /home/obda/SQL.g:1:166: ALPHAVAR
                {
                mALPHAVAR(); 

                }
                break;
            case 28 :
                // /home/obda/SQL.g:1:175: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 29 :
                // /home/obda/SQL.g:1:182: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA9_eotS =
        "\1\uffff\10\23\2\uffff\3\23\2\uffff\1\45\1\50\1\53\3\uffff\11\23"+
        "\1\65\1\23\1\67\2\23\10\uffff\11\23\1\uffff\1\103\1\uffff\1\104"+
        "\4\23\1\111\1\112\4\23\2\uffff\4\23\2\uffff\1\123\1\124\2\23\1\uffff"+
        "\1\127\1\130\1\131\2\uffff\2\23\3\uffff\2\23\1\136\1\137\2\uffff";
    static final String DFA9_eofS =
        "\140\uffff";
    static final String DFA9_minS =
        "\1\11\1\145\1\105\1\122\1\162\1\110\1\150\1\111\1\151\2\uffff\1"+
        "\156\1\116\1\105\2\uffff\1\75\1\74\1\75\3\uffff\1\154\1\114\1\154"+
        "\1\117\1\157\1\105\1\145\1\123\1\163\1\55\1\144\1\55\1\104\1\101"+
        "\10\uffff\1\145\1\105\1\145\1\115\1\155\1\122\1\162\1\124\1\164"+
        "\1\uffff\1\55\1\uffff\1\55\1\122\1\143\1\103\1\143\2\55\1\105\1"+
        "\145\1\111\1\151\2\uffff\1\50\1\164\1\124\1\164\2\uffff\2\55\1\116"+
        "\1\156\1\uffff\3\55\2\uffff\1\103\1\143\3\uffff\1\124\1\164\2\55"+
        "\2\uffff";
    static final String DFA9_maxS =
        "\1\172\2\145\1\122\1\162\1\110\1\150\1\111\1\151\2\uffff\1\163\1"+
        "\123\1\105\2\uffff\1\75\2\76\3\uffff\1\154\1\114\1\154\1\117\1\157"+
        "\1\105\1\145\1\123\1\163\1\172\1\144\1\172\1\104\1\101\10\uffff"+
        "\1\145\1\105\1\145\1\115\1\155\1\122\1\162\1\124\1\164\1\uffff\1"+
        "\172\1\uffff\1\172\1\122\1\143\1\103\1\143\2\172\1\105\1\145\1\111"+
        "\1\151\2\uffff\1\50\1\164\1\124\1\164\2\uffff\2\172\1\116\1\156"+
        "\1\uffff\3\172\2\uffff\1\103\1\143\3\uffff\1\124\1\164\2\172\2\uffff";
    static final String DFA9_acceptS =
        "\11\uffff\1\12\1\13\3\uffff\1\21\1\22\3\uffff\1\33\1\34\1\35\16"+
        "\uffff\1\30\1\23\1\31\1\32\1\24\1\26\1\27\1\25\11\uffff\1\14\1\uffff"+
        "\1\15\13\uffff\1\17\1\16\4\uffff\1\4\1\5\4\uffff\1\20\3\uffff\1"+
        "\6\1\7\2\uffff\1\1\1\2\1\3\4\uffff\1\10\1\11";
    static final String DFA9_specialS =
        "\140\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\25\3\uffff\1\25\22\uffff\1\25\6\uffff\1\17\1\uffff\1\16\2"+
            "\uffff\1\11\1\uffff\1\12\1\uffff\12\24\2\uffff\1\22\1\21\1\20"+
            "\2\uffff\1\14\2\23\1\7\1\23\1\3\14\23\1\2\3\23\1\5\1\23\1\15"+
            "\1\23\6\uffff\1\13\2\23\1\10\1\23\1\4\14\23\1\1\3\23\1\6\3\23",
            "\1\26",
            "\1\27\37\uffff\1\30",
            "\1\31",
            "\1\32",
            "\1\33",
            "\1\34",
            "\1\35",
            "\1\36",
            "",
            "",
            "\1\40\4\uffff\1\37",
            "\1\42\4\uffff\1\41",
            "\1\43",
            "",
            "",
            "\1\44",
            "\1\47\1\uffff\1\46",
            "\1\52\1\51",
            "",
            "",
            "",
            "\1\54",
            "\1\55",
            "\1\56",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\62",
            "\1\63",
            "\1\64",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\66",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\70",
            "\1\71",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\102",
            "",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\113",
            "\1\114",
            "\1\115",
            "\1\116",
            "",
            "",
            "\1\117",
            "\1\120",
            "\1\121",
            "\1\122",
            "",
            "",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\125",
            "\1\126",
            "",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "",
            "",
            "\1\132",
            "\1\133",
            "",
            "",
            "",
            "\1\134",
            "\1\135",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "\1\23\2\uffff\12\23\6\uffff\33\23\4\uffff\1\23\1\uffff\32\23",
            "",
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
            return "1:1: Tokens : ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | ALPHAVAR | NUMBER | WS );";
        }
    }
 

}