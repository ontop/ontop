// $ANTLR 3.3 Nov 30, 2010 12:50:56 SQL99.g 2011-06-27 10:58:53

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "UNION", "SELECT", "DISTINCT", "ALL", "ASTERISK", "COMMA", "DOT", "AS", "LPAREN", "RPAREN", "COUNT", "AVG", "MAX", "MIN", "SUM", "EVERY", "ANY", "SOME", "FROM", "WHERE", "OR", "AND", "NOT", "IS", "NULL", "IN", "GROUP", "BY", "JOIN", "INNER", "OUTER", "LEFT", "RIGHT", "FULL", "ON", "STRING", "STRING_WITH_QUOTE_DOUBLE", "TRUE", "FALSE", "NUMERIC", "STRING_WITH_QUOTE", "CONCATENATION", "EQUALS", "LESS", "GREATER", "ORDER", "SEMI", "LSQ_BRACKET", "RSQ_BRACKET", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", "QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "DASH", "AMPERSAND", "AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "COLON", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "TILDE", "CARET", "ALPHA", "DIGIT", "ALPHANUM", "CHAR", "WS"
    };
    public static final int EOF=-1;
    public static final int UNION=4;
    public static final int SELECT=5;
    public static final int DISTINCT=6;
    public static final int ALL=7;
    public static final int ASTERISK=8;
    public static final int COMMA=9;
    public static final int DOT=10;
    public static final int AS=11;
    public static final int LPAREN=12;
    public static final int RPAREN=13;
    public static final int COUNT=14;
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
    public static final int NOT=26;
    public static final int IS=27;
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
    public static final int CONCATENATION=45;
    public static final int EQUALS=46;
    public static final int LESS=47;
    public static final int GREATER=48;
    public static final int ORDER=49;
    public static final int SEMI=50;
    public static final int LSQ_BRACKET=51;
    public static final int RSQ_BRACKET=52;
    public static final int QUESTION=53;
    public static final int DOLLAR=54;
    public static final int QUOTE_DOUBLE=55;
    public static final int QUOTE_SINGLE=56;
    public static final int APOSTROPHE=57;
    public static final int UNDERSCORE=58;
    public static final int DASH=59;
    public static final int AMPERSAND=60;
    public static final int AT=61;
    public static final int EXCLAMATION=62;
    public static final int HASH=63;
    public static final int PERCENT=64;
    public static final int PLUS=65;
    public static final int COLON=66;
    public static final int SLASH=67;
    public static final int DOUBLE_SLASH=68;
    public static final int BACKSLASH=69;
    public static final int TILDE=70;
    public static final int CARET=71;
    public static final int ALPHA=72;
    public static final int DIGIT=73;
    public static final int ALPHANUM=74;
    public static final int CHAR=75;
    public static final int WS=76;

    // delegates
    // delegators


        public SQL99Parser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public SQL99Parser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return SQL99Parser.tokenNames; }
    public String getGrammarFileName() { return "SQL99.g"; }



    // $ANTLR start "parse"
    // SQL99.g:15:1: parse : query EOF ;
    public final void parse() throws RecognitionException {
        try {
            // SQL99.g:16:3: ( query EOF )
            // SQL99.g:16:5: query EOF
            {
            pushFollow(FOLLOW_query_in_parse30);
            query();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FOLLOW_EOF_in_parse32); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parse"


    // $ANTLR start "query"
    // SQL99.g:19:1: query : query_expression ( UNION ( set_quantifier )? query_expression )* ;
    public final void query() throws RecognitionException {
        try {
            // SQL99.g:20:3: ( query_expression ( UNION ( set_quantifier )? query_expression )* )
            // SQL99.g:20:5: query_expression ( UNION ( set_quantifier )? query_expression )*
            {
            pushFollow(FOLLOW_query_expression_in_query47);
            query_expression();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:20:22: ( UNION ( set_quantifier )? query_expression )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==UNION) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // SQL99.g:20:23: UNION ( set_quantifier )? query_expression
            	    {
            	    match(input,UNION,FOLLOW_UNION_in_query50); if (state.failed) return ;
            	    // SQL99.g:20:29: ( set_quantifier )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( ((LA1_0>=DISTINCT && LA1_0<=ALL)) ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // SQL99.g:20:30: set_quantifier
            	            {
            	            pushFollow(FOLLOW_set_quantifier_in_query53);
            	            set_quantifier();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_query_expression_in_query57);
            	    query_expression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "query"


    // $ANTLR start "query_expression"
    // SQL99.g:23:1: query_expression : SELECT ( set_quantifier )? select_list table_expression ;
    public final void query_expression() throws RecognitionException {
        try {
            // SQL99.g:24:3: ( SELECT ( set_quantifier )? select_list table_expression )
            // SQL99.g:24:5: SELECT ( set_quantifier )? select_list table_expression
            {
            match(input,SELECT,FOLLOW_SELECT_in_query_expression74); if (state.failed) return ;
            // SQL99.g:24:12: ( set_quantifier )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( ((LA3_0>=DISTINCT && LA3_0<=ALL)) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // SQL99.g:24:12: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_query_expression76);
                    set_quantifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_select_list_in_query_expression79);
            select_list();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_table_expression_in_query_expression81);
            table_expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "query_expression"


    // $ANTLR start "set_quantifier"
    // SQL99.g:27:1: set_quantifier : ( DISTINCT | ALL );
    public final void set_quantifier() throws RecognitionException {
        try {
            // SQL99.g:28:3: ( DISTINCT | ALL )
            // SQL99.g:
            {
            if ( (input.LA(1)>=DISTINCT && input.LA(1)<=ALL) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "set_quantifier"


    // $ANTLR start "select_list"
    // SQL99.g:32:1: select_list : ( ASTERISK | select_sublist ( COMMA select_sublist )* );
    public final void select_list() throws RecognitionException {
        try {
            // SQL99.g:33:3: ( ASTERISK | select_sublist ( COMMA select_sublist )* )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==ASTERISK) ) {
                alt5=1;
            }
            else if ( (LA5_0==LPAREN||(LA5_0>=COUNT && LA5_0<=SOME)||(LA5_0>=STRING && LA5_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt5=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // SQL99.g:33:5: ASTERISK
                    {
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_select_list118); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:34:5: select_sublist ( COMMA select_sublist )*
                    {
                    pushFollow(FOLLOW_select_sublist_in_select_list124);
                    select_sublist();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:34:20: ( COMMA select_sublist )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==COMMA) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // SQL99.g:34:21: COMMA select_sublist
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_select_list127); if (state.failed) return ;
                    	    pushFollow(FOLLOW_select_sublist_in_select_list129);
                    	    select_sublist();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "select_list"


    // $ANTLR start "select_sublist"
    // SQL99.g:37:1: select_sublist : ( qualified_asterisk | derived_column );
    public final void select_sublist() throws RecognitionException {
        try {
            // SQL99.g:38:3: ( qualified_asterisk | derived_column )
            int alt6=2;
            switch ( input.LA(1) ) {
            case STRING:
                {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==DOT) ) {
                    int LA6_4 = input.LA(3);

                    if ( (LA6_4==ASTERISK) ) {
                        alt6=1;
                    }
                    else if ( ((LA6_4>=STRING && LA6_4<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt6=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 4, input);

                        throw nvae;
                    }
                }
                else if ( (LA6_1==COMMA||LA6_1==AS||LA6_1==FROM||(LA6_1>=STRING && LA6_1<=STRING_WITH_QUOTE_DOUBLE)) ) {
                    alt6=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;
                }
                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                int LA6_2 = input.LA(2);

                if ( (LA6_2==DOT) ) {
                    int LA6_4 = input.LA(3);

                    if ( (LA6_4==ASTERISK) ) {
                        alt6=1;
                    }
                    else if ( ((LA6_4>=STRING && LA6_4<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt6=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 4, input);

                        throw nvae;
                    }
                }
                else if ( (LA6_2==COMMA||LA6_2==AS||LA6_2==FROM||(LA6_2>=STRING && LA6_2<=STRING_WITH_QUOTE_DOUBLE)) ) {
                    alt6=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 2, input);

                    throw nvae;
                }
                }
                break;
            case LPAREN:
            case COUNT:
            case AVG:
            case MAX:
            case MIN:
            case SUM:
            case EVERY:
            case ANY:
            case SOME:
                {
                alt6=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // SQL99.g:38:5: qualified_asterisk
                    {
                    pushFollow(FOLLOW_qualified_asterisk_in_select_sublist146);
                    qualified_asterisk();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:39:5: derived_column
                    {
                    pushFollow(FOLLOW_derived_column_in_select_sublist152);
                    derived_column();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "select_sublist"


    // $ANTLR start "qualified_asterisk"
    // SQL99.g:42:1: qualified_asterisk : table_identifier DOT ASTERISK ;
    public final void qualified_asterisk() throws RecognitionException {
        try {
            // SQL99.g:43:3: ( table_identifier DOT ASTERISK )
            // SQL99.g:43:5: table_identifier DOT ASTERISK
            {
            pushFollow(FOLLOW_table_identifier_in_qualified_asterisk167);
            table_identifier();

            state._fsp--;
            if (state.failed) return ;
            match(input,DOT,FOLLOW_DOT_in_qualified_asterisk169); if (state.failed) return ;
            match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk171); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "qualified_asterisk"


    // $ANTLR start "derived_column"
    // SQL99.g:46:1: derived_column : value_expression ( ( AS )? alias_name )? ;
    public final void derived_column() throws RecognitionException {
        try {
            // SQL99.g:47:3: ( value_expression ( ( AS )? alias_name )? )
            // SQL99.g:47:5: value_expression ( ( AS )? alias_name )?
            {
            pushFollow(FOLLOW_value_expression_in_derived_column186);
            value_expression();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:47:22: ( ( AS )? alias_name )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==AS||(LA8_0>=STRING && LA8_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // SQL99.g:47:23: ( AS )? alias_name
                    {
                    // SQL99.g:47:23: ( AS )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==AS) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // SQL99.g:47:23: AS
                            {
                            match(input,AS,FOLLOW_AS_in_derived_column189); if (state.failed) return ;

                            }
                            break;

                    }

                    pushFollow(FOLLOW_alias_name_in_derived_column192);
                    alias_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "derived_column"


    // $ANTLR start "value_expression"
    // SQL99.g:50:1: value_expression : ( string_value_expression | reference_value_expression | collection_value_expression );
    public final void value_expression() throws RecognitionException {
        try {
            // SQL99.g:51:3: ( string_value_expression | reference_value_expression | collection_value_expression )
            int alt9=3;
            switch ( input.LA(1) ) {
            case LPAREN:
                {
                alt9=1;
                }
                break;
            case STRING:
            case STRING_WITH_QUOTE_DOUBLE:
                {
                alt9=2;
                }
                break;
            case COUNT:
            case AVG:
            case MAX:
            case MIN:
            case SUM:
            case EVERY:
            case ANY:
            case SOME:
                {
                alt9=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // SQL99.g:51:5: string_value_expression
                    {
                    pushFollow(FOLLOW_string_value_expression_in_value_expression210);
                    string_value_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:52:5: reference_value_expression
                    {
                    pushFollow(FOLLOW_reference_value_expression_in_value_expression216);
                    reference_value_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // SQL99.g:53:5: collection_value_expression
                    {
                    pushFollow(FOLLOW_collection_value_expression_in_value_expression222);
                    collection_value_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "value_expression"


    // $ANTLR start "string_value_expression"
    // SQL99.g:56:1: string_value_expression : LPAREN concatenation RPAREN ;
    public final void string_value_expression() throws RecognitionException {
        try {
            // SQL99.g:57:3: ( LPAREN concatenation RPAREN )
            // SQL99.g:57:5: LPAREN concatenation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_string_value_expression235); if (state.failed) return ;
            pushFollow(FOLLOW_concatenation_in_string_value_expression237);
            concatenation();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_string_value_expression239); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "string_value_expression"


    // $ANTLR start "concatenation"
    // SQL99.g:60:1: concatenation : concatenation_value ( concatenation_operator concatenation_value )+ ;
    public final void concatenation() throws RecognitionException {
        try {
            // SQL99.g:61:3: ( concatenation_value ( concatenation_operator concatenation_value )+ )
            // SQL99.g:61:5: concatenation_value ( concatenation_operator concatenation_value )+
            {
            pushFollow(FOLLOW_concatenation_value_in_concatenation254);
            concatenation_value();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:61:25: ( concatenation_operator concatenation_value )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==CONCATENATION) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // SQL99.g:61:26: concatenation_operator concatenation_value
            	    {
            	    pushFollow(FOLLOW_concatenation_operator_in_concatenation257);
            	    concatenation_operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    pushFollow(FOLLOW_concatenation_value_in_concatenation259);
            	    concatenation_value();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "concatenation"


    // $ANTLR start "concatenation_value"
    // SQL99.g:64:1: concatenation_value : ( column_reference | value );
    public final void concatenation_value() throws RecognitionException {
        try {
            // SQL99.g:65:3: ( column_reference | value )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( ((LA11_0>=STRING && LA11_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt11=1;
            }
            else if ( ((LA11_0>=TRUE && LA11_0<=STRING_WITH_QUOTE)) ) {
                alt11=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // SQL99.g:65:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_concatenation_value274);
                    column_reference();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:66:5: value
                    {
                    pushFollow(FOLLOW_value_in_concatenation_value280);
                    value();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "concatenation_value"


    // $ANTLR start "reference_value_expression"
    // SQL99.g:69:1: reference_value_expression : column_reference ;
    public final void reference_value_expression() throws RecognitionException {
        try {
            // SQL99.g:70:3: ( column_reference )
            // SQL99.g:70:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_reference_value_expression293);
            column_reference();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "reference_value_expression"


    // $ANTLR start "column_reference"
    // SQL99.g:73:1: column_reference : ( table_identifier DOT )? column_name ;
    public final void column_reference() throws RecognitionException {
        try {
            // SQL99.g:74:3: ( ( table_identifier DOT )? column_name )
            // SQL99.g:74:5: ( table_identifier DOT )? column_name
            {
            // SQL99.g:74:5: ( table_identifier DOT )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==STRING) ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==DOT) ) {
                    alt12=1;
                }
            }
            else if ( (LA12_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA12_2 = input.LA(2);

                if ( (LA12_2==DOT) ) {
                    alt12=1;
                }
            }
            switch (alt12) {
                case 1 :
                    // SQL99.g:74:6: table_identifier DOT
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference307);
                    table_identifier();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_column_reference309); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_column_name_in_column_reference313);
            column_name();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "column_reference"


    // $ANTLR start "collection_value_expression"
    // SQL99.g:77:1: collection_value_expression : set_function_specification ;
    public final void collection_value_expression() throws RecognitionException {
        try {
            // SQL99.g:78:3: ( set_function_specification )
            // SQL99.g:78:5: set_function_specification
            {
            pushFollow(FOLLOW_set_function_specification_in_collection_value_expression330);
            set_function_specification();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "collection_value_expression"


    // $ANTLR start "set_function_specification"
    // SQL99.g:81:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        try {
            // SQL99.g:82:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==COUNT) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==LPAREN) ) {
                    int LA13_3 = input.LA(3);

                    if ( (LA13_3==ASTERISK) ) {
                        alt13=1;
                    }
                    else if ( ((LA13_3>=DISTINCT && LA13_3<=ALL)||LA13_3==LPAREN||(LA13_3>=COUNT && LA13_3<=SOME)||(LA13_3>=STRING && LA13_3<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt13=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 13, 3, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA13_0>=AVG && LA13_0<=SOME)) ) {
                alt13=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // SQL99.g:82:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    match(input,COUNT,FOLLOW_COUNT_in_set_function_specification343); if (state.failed) return ;
                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification345); if (state.failed) return ;
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification347); if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification349); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:83:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification355);
                    general_set_function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "set_function_specification"


    // $ANTLR start "general_set_function"
    // SQL99.g:86:1: general_set_function : set_function_op LPAREN ( set_quantifier )? value_expression RPAREN ;
    public final void general_set_function() throws RecognitionException {
        try {
            // SQL99.g:87:3: ( set_function_op LPAREN ( set_quantifier )? value_expression RPAREN )
            // SQL99.g:87:5: set_function_op LPAREN ( set_quantifier )? value_expression RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function370);
            set_function_op();

            state._fsp--;
            if (state.failed) return ;
            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function372); if (state.failed) return ;
            // SQL99.g:87:28: ( set_quantifier )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( ((LA14_0>=DISTINCT && LA14_0<=ALL)) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // SQL99.g:87:29: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_general_set_function375);
                    set_quantifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_value_expression_in_general_set_function379);
            value_expression();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function381); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "general_set_function"


    // $ANTLR start "set_function_op"
    // SQL99.g:90:1: set_function_op : ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT );
    public final void set_function_op() throws RecognitionException {
        try {
            // SQL99.g:91:3: ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT )
            // SQL99.g:
            {
            if ( (input.LA(1)>=COUNT && input.LA(1)<=SOME) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "set_function_op"


    // $ANTLR start "table_expression"
    // SQL99.g:101:1: table_expression : from_clause ( where_clause )? ( group_by_clause )? ;
    public final void table_expression() throws RecognitionException {
        try {
            // SQL99.g:102:3: ( from_clause ( where_clause )? ( group_by_clause )? )
            // SQL99.g:102:5: from_clause ( where_clause )? ( group_by_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression457);
            from_clause();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:102:17: ( where_clause )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==WHERE) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // SQL99.g:102:18: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression460);
                    where_clause();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // SQL99.g:102:33: ( group_by_clause )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==GROUP) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // SQL99.g:102:34: group_by_clause
                    {
                    pushFollow(FOLLOW_group_by_clause_in_table_expression465);
                    group_by_clause();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_expression"


    // $ANTLR start "from_clause"
    // SQL99.g:105:1: from_clause : FROM table_reference_list ;
    public final void from_clause() throws RecognitionException {
        try {
            // SQL99.g:106:3: ( FROM table_reference_list )
            // SQL99.g:106:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause482); if (state.failed) return ;
            pushFollow(FOLLOW_table_reference_list_in_from_clause484);
            table_reference_list();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "from_clause"


    // $ANTLR start "table_reference_list"
    // SQL99.g:109:1: table_reference_list : table_reference ( COMMA table_reference )* ;
    public final void table_reference_list() throws RecognitionException {
        try {
            // SQL99.g:110:3: ( table_reference ( COMMA table_reference )* )
            // SQL99.g:110:5: table_reference ( COMMA table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list501);
            table_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:110:21: ( COMMA table_reference )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==COMMA) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // SQL99.g:110:22: COMMA table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list504); if (state.failed) return ;
            	    pushFollow(FOLLOW_table_reference_in_table_reference_list506);
            	    table_reference();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_reference_list"


    // $ANTLR start "table_reference"
    // SQL99.g:113:1: table_reference options {backtrack=true; } : table_primary ( joined_table )? ;
    public final void table_reference() throws RecognitionException {
        try {
            // SQL99.g:117:3: ( table_primary ( joined_table )? )
            // SQL99.g:117:5: table_primary ( joined_table )?
            {
            pushFollow(FOLLOW_table_primary_in_table_reference534);
            table_primary();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:117:19: ( joined_table )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=JOIN && LA18_0<=INNER)||(LA18_0>=LEFT && LA18_0<=FULL)) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // SQL99.g:117:20: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference537);
                    joined_table();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_reference"


    // $ANTLR start "where_clause"
    // SQL99.g:120:1: where_clause : WHERE search_condition ;
    public final void where_clause() throws RecognitionException {
        try {
            // SQL99.g:121:3: ( WHERE search_condition )
            // SQL99.g:121:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause552); if (state.failed) return ;
            pushFollow(FOLLOW_search_condition_in_where_clause554);
            search_condition();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "where_clause"


    // $ANTLR start "search_condition"
    // SQL99.g:124:1: search_condition : boolean_value_expression ;
    public final void search_condition() throws RecognitionException {
        try {
            // SQL99.g:125:3: ( boolean_value_expression )
            // SQL99.g:125:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition567);
            boolean_value_expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "search_condition"


    // $ANTLR start "boolean_value_expression"
    // SQL99.g:128:1: boolean_value_expression : boolean_term ( OR boolean_term )* ;
    public final void boolean_value_expression() throws RecognitionException {
        try {
            // SQL99.g:129:3: ( boolean_term ( OR boolean_term )* )
            // SQL99.g:129:5: boolean_term ( OR boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression582);
            boolean_term();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:129:18: ( OR boolean_term )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==OR) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // SQL99.g:129:19: OR boolean_term
            	    {
            	    match(input,OR,FOLLOW_OR_in_boolean_value_expression585); if (state.failed) return ;
            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression587);
            	    boolean_term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "boolean_value_expression"


    // $ANTLR start "boolean_term"
    // SQL99.g:132:1: boolean_term : boolean_factor ( AND boolean_factor )* ;
    public final void boolean_term() throws RecognitionException {
        try {
            // SQL99.g:133:3: ( boolean_factor ( AND boolean_factor )* )
            // SQL99.g:133:5: boolean_factor ( AND boolean_factor )*
            {
            pushFollow(FOLLOW_boolean_factor_in_boolean_term604);
            boolean_factor();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:133:20: ( AND boolean_factor )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==AND) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // SQL99.g:133:21: AND boolean_factor
            	    {
            	    match(input,AND,FOLLOW_AND_in_boolean_term607); if (state.failed) return ;
            	    pushFollow(FOLLOW_boolean_factor_in_boolean_term609);
            	    boolean_factor();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "boolean_term"


    // $ANTLR start "boolean_factor"
    // SQL99.g:136:1: boolean_factor : ( NOT )? boolean_test ;
    public final void boolean_factor() throws RecognitionException {
        try {
            // SQL99.g:137:3: ( ( NOT )? boolean_test )
            // SQL99.g:137:5: ( NOT )? boolean_test
            {
            // SQL99.g:137:5: ( NOT )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==NOT) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // SQL99.g:137:6: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_boolean_factor627); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_boolean_test_in_boolean_factor631);
            boolean_test();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "boolean_factor"


    // $ANTLR start "boolean_test"
    // SQL99.g:140:1: boolean_test : boolean_primary ( IS ( NOT )? truth_value )? ;
    public final void boolean_test() throws RecognitionException {
        try {
            // SQL99.g:141:3: ( boolean_primary ( IS ( NOT )? truth_value )? )
            // SQL99.g:141:5: boolean_primary ( IS ( NOT )? truth_value )?
            {
            pushFollow(FOLLOW_boolean_primary_in_boolean_test644);
            boolean_primary();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:141:21: ( IS ( NOT )? truth_value )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==IS) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // SQL99.g:141:22: IS ( NOT )? truth_value
                    {
                    match(input,IS,FOLLOW_IS_in_boolean_test647); if (state.failed) return ;
                    // SQL99.g:141:25: ( NOT )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==NOT) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // SQL99.g:141:26: NOT
                            {
                            match(input,NOT,FOLLOW_NOT_in_boolean_test650); if (state.failed) return ;

                            }
                            break;

                    }

                    pushFollow(FOLLOW_truth_value_in_boolean_test654);
                    truth_value();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "boolean_test"


    // $ANTLR start "boolean_primary"
    // SQL99.g:144:1: boolean_primary : ( predicate | parenthesized_boolean_value_expression );
    public final void boolean_primary() throws RecognitionException {
        try {
            // SQL99.g:145:3: ( predicate | parenthesized_boolean_value_expression )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==LPAREN) ) {
                switch ( input.LA(2) ) {
                case STRING:
                    {
                    switch ( input.LA(3) ) {
                    case DOT:
                        {
                        int LA24_6 = input.LA(4);

                        if ( (LA24_6==STRING) ) {
                            int LA24_7 = input.LA(5);

                            if ( (LA24_7==CONCATENATION) ) {
                                alt24=1;
                            }
                            else if ( ((LA24_7>=NOT && LA24_7<=IS)||LA24_7==IN||(LA24_7>=EQUALS && LA24_7<=GREATER)) ) {
                                alt24=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 24, 7, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA24_6==STRING_WITH_QUOTE_DOUBLE) ) {
                            int LA24_8 = input.LA(5);

                            if ( (LA24_8==CONCATENATION) ) {
                                alt24=1;
                            }
                            else if ( ((LA24_8>=NOT && LA24_8<=IS)||LA24_8==IN||(LA24_8>=EQUALS && LA24_8<=GREATER)) ) {
                                alt24=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 24, 8, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 24, 6, input);

                            throw nvae;
                        }
                        }
                        break;
                    case CONCATENATION:
                        {
                        alt24=1;
                        }
                        break;
                    case NOT:
                    case IS:
                    case IN:
                    case EQUALS:
                    case LESS:
                    case GREATER:
                        {
                        alt24=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 24, 3, input);

                        throw nvae;
                    }

                    }
                    break;
                case STRING_WITH_QUOTE_DOUBLE:
                    {
                    switch ( input.LA(3) ) {
                    case DOT:
                        {
                        int LA24_6 = input.LA(4);

                        if ( (LA24_6==STRING) ) {
                            int LA24_7 = input.LA(5);

                            if ( (LA24_7==CONCATENATION) ) {
                                alt24=1;
                            }
                            else if ( ((LA24_7>=NOT && LA24_7<=IS)||LA24_7==IN||(LA24_7>=EQUALS && LA24_7<=GREATER)) ) {
                                alt24=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 24, 7, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA24_6==STRING_WITH_QUOTE_DOUBLE) ) {
                            int LA24_8 = input.LA(5);

                            if ( (LA24_8==CONCATENATION) ) {
                                alt24=1;
                            }
                            else if ( ((LA24_8>=NOT && LA24_8<=IS)||LA24_8==IN||(LA24_8>=EQUALS && LA24_8<=GREATER)) ) {
                                alt24=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 24, 8, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 24, 6, input);

                            throw nvae;
                        }
                        }
                        break;
                    case CONCATENATION:
                        {
                        alt24=1;
                        }
                        break;
                    case NOT:
                    case IS:
                    case IN:
                    case EQUALS:
                    case LESS:
                    case GREATER:
                        {
                        alt24=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 24, 4, input);

                        throw nvae;
                    }

                    }
                    break;
                case TRUE:
                case FALSE:
                case NUMERIC:
                case STRING_WITH_QUOTE:
                    {
                    alt24=1;
                    }
                    break;
                case LPAREN:
                case COUNT:
                case AVG:
                case MAX:
                case MIN:
                case SUM:
                case EVERY:
                case ANY:
                case SOME:
                case NOT:
                    {
                    alt24=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 24, 1, input);

                    throw nvae;
                }

            }
            else if ( ((LA24_0>=COUNT && LA24_0<=SOME)||(LA24_0>=STRING && LA24_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt24=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // SQL99.g:145:5: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_boolean_primary669);
                    predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:146:5: parenthesized_boolean_value_expression
                    {
                    pushFollow(FOLLOW_parenthesized_boolean_value_expression_in_boolean_primary675);
                    parenthesized_boolean_value_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "boolean_primary"


    // $ANTLR start "parenthesized_boolean_value_expression"
    // SQL99.g:149:1: parenthesized_boolean_value_expression : LPAREN boolean_value_expression RPAREN ;
    public final void parenthesized_boolean_value_expression() throws RecognitionException {
        try {
            // SQL99.g:150:3: ( LPAREN boolean_value_expression RPAREN )
            // SQL99.g:150:5: LPAREN boolean_value_expression RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_parenthesized_boolean_value_expression688); if (state.failed) return ;
            pushFollow(FOLLOW_boolean_value_expression_in_parenthesized_boolean_value_expression690);
            boolean_value_expression();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_parenthesized_boolean_value_expression692); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parenthesized_boolean_value_expression"


    // $ANTLR start "predicate"
    // SQL99.g:153:1: predicate : ( comparison_predicate | null_predicate | in_predicate );
    public final void predicate() throws RecognitionException {
        try {
            // SQL99.g:154:3: ( comparison_predicate | null_predicate | in_predicate )
            int alt25=3;
            switch ( input.LA(1) ) {
            case LPAREN:
            case COUNT:
            case AVG:
            case MAX:
            case MIN:
            case SUM:
            case EVERY:
            case ANY:
            case SOME:
                {
                alt25=1;
                }
                break;
            case STRING:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA25_4 = input.LA(3);

                    if ( (LA25_4==STRING) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 7, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA25_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 8, input);

                            throw nvae;
                        }

                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 25, 4, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt25=1;
                    }
                    break;
                case IS:
                    {
                    alt25=2;
                    }
                    break;
                case NOT:
                case IN:
                    {
                    alt25=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 2, input);

                    throw nvae;
                }

                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA25_4 = input.LA(3);

                    if ( (LA25_4==STRING) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 7, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA25_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 8, input);

                            throw nvae;
                        }

                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 25, 4, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt25=1;
                    }
                    break;
                case IS:
                    {
                    alt25=2;
                    }
                    break;
                case NOT:
                case IN:
                    {
                    alt25=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 3, input);

                    throw nvae;
                }

                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }

            switch (alt25) {
                case 1 :
                    // SQL99.g:154:5: comparison_predicate
                    {
                    pushFollow(FOLLOW_comparison_predicate_in_predicate706);
                    comparison_predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:155:5: null_predicate
                    {
                    pushFollow(FOLLOW_null_predicate_in_predicate712);
                    null_predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // SQL99.g:156:5: in_predicate
                    {
                    pushFollow(FOLLOW_in_predicate_in_predicate718);
                    in_predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "predicate"


    // $ANTLR start "comparison_predicate"
    // SQL99.g:159:1: comparison_predicate : value_expression comp_op ( value | value_expression ) ;
    public final void comparison_predicate() throws RecognitionException {
        try {
            // SQL99.g:160:3: ( value_expression comp_op ( value | value_expression ) )
            // SQL99.g:160:5: value_expression comp_op ( value | value_expression )
            {
            pushFollow(FOLLOW_value_expression_in_comparison_predicate733);
            value_expression();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_comp_op_in_comparison_predicate735);
            comp_op();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:160:30: ( value | value_expression )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( ((LA26_0>=TRUE && LA26_0<=STRING_WITH_QUOTE)) ) {
                alt26=1;
            }
            else if ( (LA26_0==LPAREN||(LA26_0>=COUNT && LA26_0<=SOME)||(LA26_0>=STRING && LA26_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt26=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // SQL99.g:160:31: value
                    {
                    pushFollow(FOLLOW_value_in_comparison_predicate738);
                    value();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:160:37: value_expression
                    {
                    pushFollow(FOLLOW_value_expression_in_comparison_predicate740);
                    value_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "comparison_predicate"


    // $ANTLR start "comp_op"
    // SQL99.g:163:1: comp_op : ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator );
    public final void comp_op() throws RecognitionException {
        try {
            // SQL99.g:164:3: ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator )
            int alt27=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt27=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt27=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt27=5;
                    }
                    break;
                case LPAREN:
                case COUNT:
                case AVG:
                case MAX:
                case MIN:
                case SUM:
                case EVERY:
                case ANY:
                case SOME:
                case STRING:
                case STRING_WITH_QUOTE_DOUBLE:
                case TRUE:
                case FALSE:
                case NUMERIC:
                case STRING_WITH_QUOTE:
                    {
                    alt27=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 27, 2, input);

                    throw nvae;
                }

                }
                break;
            case GREATER:
                {
                int LA27_3 = input.LA(2);

                if ( (LA27_3==EQUALS) ) {
                    alt27=6;
                }
                else if ( (LA27_3==LPAREN||(LA27_3>=COUNT && LA27_3<=SOME)||(LA27_3>=STRING && LA27_3<=STRING_WITH_QUOTE)) ) {
                    alt27=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 27, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }

            switch (alt27) {
                case 1 :
                    // SQL99.g:164:5: equals_operator
                    {
                    pushFollow(FOLLOW_equals_operator_in_comp_op754);
                    equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:165:5: not_equals_operator
                    {
                    pushFollow(FOLLOW_not_equals_operator_in_comp_op760);
                    not_equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // SQL99.g:166:5: less_than_operator
                    {
                    pushFollow(FOLLOW_less_than_operator_in_comp_op766);
                    less_than_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // SQL99.g:167:5: greater_than_operator
                    {
                    pushFollow(FOLLOW_greater_than_operator_in_comp_op772);
                    greater_than_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // SQL99.g:168:5: less_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_less_than_or_equals_operator_in_comp_op778);
                    less_than_or_equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // SQL99.g:169:5: greater_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_greater_than_or_equals_operator_in_comp_op784);
                    greater_than_or_equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "comp_op"


    // $ANTLR start "null_predicate"
    // SQL99.g:172:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // SQL99.g:173:3: ( column_reference IS ( NOT )? NULL )
            // SQL99.g:173:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate797);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            match(input,IS,FOLLOW_IS_in_null_predicate799); if (state.failed) return ;
            // SQL99.g:173:25: ( NOT )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==NOT) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // SQL99.g:173:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate802); if (state.failed) return ;

                    }
                    break;

            }

            match(input,NULL,FOLLOW_NULL_in_null_predicate806); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "null_predicate"


    // $ANTLR start "in_predicate"
    // SQL99.g:176:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // SQL99.g:177:3: ( column_reference ( NOT )? IN in_predicate_value )
            // SQL99.g:177:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate819);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:177:22: ( NOT )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==NOT) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // SQL99.g:177:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate822); if (state.failed) return ;

                    }
                    break;

            }

            match(input,IN,FOLLOW_IN_in_in_predicate826); if (state.failed) return ;
            pushFollow(FOLLOW_in_predicate_value_in_in_predicate828);
            in_predicate_value();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "in_predicate"


    // $ANTLR start "in_predicate_value"
    // SQL99.g:180:1: in_predicate_value : ( table_subquery | LPAREN value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // SQL99.g:181:3: ( table_subquery | LPAREN value_list RPAREN )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==LPAREN) ) {
                int LA30_1 = input.LA(2);

                if ( (LA30_1==SELECT) ) {
                    alt30=1;
                }
                else if ( ((LA30_1>=TRUE && LA30_1<=STRING_WITH_QUOTE)) ) {
                    alt30=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }
            switch (alt30) {
                case 1 :
                    // SQL99.g:181:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value843);
                    table_subquery();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:182:5: LPAREN value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value849); if (state.failed) return ;
                    pushFollow(FOLLOW_value_list_in_in_predicate_value851);
                    value_list();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value853); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "in_predicate_value"


    // $ANTLR start "table_subquery"
    // SQL99.g:185:1: table_subquery : subquery ;
    public final void table_subquery() throws RecognitionException {
        try {
            // SQL99.g:186:3: ( subquery )
            // SQL99.g:186:5: subquery
            {
            pushFollow(FOLLOW_subquery_in_table_subquery866);
            subquery();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_subquery"


    // $ANTLR start "subquery"
    // SQL99.g:189:1: subquery : LPAREN query RPAREN ;
    public final void subquery() throws RecognitionException {
        try {
            // SQL99.g:190:3: ( LPAREN query RPAREN )
            // SQL99.g:190:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_subquery879); if (state.failed) return ;
            pushFollow(FOLLOW_query_in_subquery881);
            query();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_subquery883); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "subquery"


    // $ANTLR start "value_list"
    // SQL99.g:193:1: value_list : value ( COMMA value )* ;
    public final void value_list() throws RecognitionException {
        try {
            // SQL99.g:194:3: ( value ( COMMA value )* )
            // SQL99.g:194:5: value ( COMMA value )*
            {
            pushFollow(FOLLOW_value_in_value_list898);
            value();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:194:11: ( COMMA value )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==COMMA) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // SQL99.g:194:12: COMMA value
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_value_list901); if (state.failed) return ;
            	    pushFollow(FOLLOW_value_in_value_list903);
            	    value();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "value_list"


    // $ANTLR start "group_by_clause"
    // SQL99.g:197:1: group_by_clause : GROUP BY grouping_element_list ;
    public final void group_by_clause() throws RecognitionException {
        try {
            // SQL99.g:198:3: ( GROUP BY grouping_element_list )
            // SQL99.g:198:5: GROUP BY grouping_element_list
            {
            match(input,GROUP,FOLLOW_GROUP_in_group_by_clause918); if (state.failed) return ;
            match(input,BY,FOLLOW_BY_in_group_by_clause920); if (state.failed) return ;
            pushFollow(FOLLOW_grouping_element_list_in_group_by_clause922);
            grouping_element_list();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "group_by_clause"


    // $ANTLR start "grouping_element_list"
    // SQL99.g:201:1: grouping_element_list : grouping_element ( COMMA grouping_element )* ;
    public final void grouping_element_list() throws RecognitionException {
        try {
            // SQL99.g:202:3: ( grouping_element ( COMMA grouping_element )* )
            // SQL99.g:202:5: grouping_element ( COMMA grouping_element )*
            {
            pushFollow(FOLLOW_grouping_element_in_grouping_element_list935);
            grouping_element();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:202:22: ( COMMA grouping_element )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==COMMA) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // SQL99.g:202:23: COMMA grouping_element
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list938); if (state.failed) return ;
            	    pushFollow(FOLLOW_grouping_element_in_grouping_element_list940);
            	    grouping_element();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "grouping_element_list"


    // $ANTLR start "grouping_element"
    // SQL99.g:205:1: grouping_element : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
    public final void grouping_element() throws RecognitionException {
        try {
            // SQL99.g:206:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( ((LA33_0>=STRING && LA33_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt33=1;
            }
            else if ( (LA33_0==LPAREN) ) {
                alt33=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // SQL99.g:206:5: grouping_column_reference
                    {
                    pushFollow(FOLLOW_grouping_column_reference_in_grouping_element957);
                    grouping_column_reference();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:207:5: LPAREN grouping_column_reference_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element963); if (state.failed) return ;
                    pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element965);
                    grouping_column_reference_list();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element967); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "grouping_element"


    // $ANTLR start "grouping_column_reference"
    // SQL99.g:210:1: grouping_column_reference : column_reference ;
    public final void grouping_column_reference() throws RecognitionException {
        try {
            // SQL99.g:211:3: ( column_reference )
            // SQL99.g:211:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference983);
            column_reference();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "grouping_column_reference"


    // $ANTLR start "grouping_column_reference_list"
    // SQL99.g:214:1: grouping_column_reference_list : column_reference ( COMMA column_reference )* ;
    public final void grouping_column_reference_list() throws RecognitionException {
        try {
            // SQL99.g:215:3: ( column_reference ( COMMA column_reference )* )
            // SQL99.g:215:5: column_reference ( COMMA column_reference )*
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list998);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:215:22: ( COMMA column_reference )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==COMMA) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // SQL99.g:215:23: COMMA column_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list1001); if (state.failed) return ;
            	    pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1003);
            	    column_reference();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop34;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "grouping_column_reference_list"


    // $ANTLR start "joined_table"
    // SQL99.g:218:1: joined_table : qualified_join ;
    public final void joined_table() throws RecognitionException {
        try {
            // SQL99.g:219:3: ( qualified_join )
            // SQL99.g:219:5: qualified_join
            {
            pushFollow(FOLLOW_qualified_join_in_joined_table1020);
            qualified_join();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "joined_table"


    // $ANTLR start "qualified_join"
    // SQL99.g:222:1: qualified_join : ( join_type )? JOIN table_primary join_condition ;
    public final void qualified_join() throws RecognitionException {
        try {
            // SQL99.g:223:3: ( ( join_type )? JOIN table_primary join_condition )
            // SQL99.g:223:5: ( join_type )? JOIN table_primary join_condition
            {
            // SQL99.g:223:5: ( join_type )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==INNER||(LA35_0>=LEFT && LA35_0<=FULL)) ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // SQL99.g:223:6: join_type
                    {
                    pushFollow(FOLLOW_join_type_in_qualified_join1034);
                    join_type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,JOIN,FOLLOW_JOIN_in_qualified_join1038); if (state.failed) return ;
            pushFollow(FOLLOW_table_primary_in_qualified_join1040);
            table_primary();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_join_condition_in_qualified_join1042);
            join_condition();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "qualified_join"


    // $ANTLR start "join_type"
    // SQL99.g:226:1: join_type : ( INNER | outer_join_type ( OUTER )? );
    public final void join_type() throws RecognitionException {
        try {
            // SQL99.g:227:3: ( INNER | outer_join_type ( OUTER )? )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==INNER) ) {
                alt37=1;
            }
            else if ( ((LA37_0>=LEFT && LA37_0<=FULL)) ) {
                alt37=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // SQL99.g:227:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type1055); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:228:5: outer_join_type ( OUTER )?
                    {
                    pushFollow(FOLLOW_outer_join_type_in_join_type1061);
                    outer_join_type();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:228:21: ( OUTER )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==OUTER) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // SQL99.g:228:22: OUTER
                            {
                            match(input,OUTER,FOLLOW_OUTER_in_join_type1064); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "join_type"


    // $ANTLR start "outer_join_type"
    // SQL99.g:231:1: outer_join_type : ( LEFT | RIGHT | FULL );
    public final void outer_join_type() throws RecognitionException {
        try {
            // SQL99.g:232:3: ( LEFT | RIGHT | FULL )
            // SQL99.g:
            {
            if ( (input.LA(1)>=LEFT && input.LA(1)<=FULL) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "outer_join_type"


    // $ANTLR start "join_condition"
    // SQL99.g:237:1: join_condition : ON search_condition ;
    public final void join_condition() throws RecognitionException {
        try {
            // SQL99.g:238:3: ( ON search_condition )
            // SQL99.g:238:5: ON search_condition
            {
            match(input,ON,FOLLOW_ON_in_join_condition1108); if (state.failed) return ;
            pushFollow(FOLLOW_search_condition_in_join_condition1110);
            search_condition();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "join_condition"


    // $ANTLR start "table_primary"
    // SQL99.g:241:1: table_primary : ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name );
    public final void table_primary() throws RecognitionException {
        try {
            // SQL99.g:242:3: ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( ((LA41_0>=STRING && LA41_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt41=1;
            }
            else if ( (LA41_0==LPAREN) ) {
                alt41=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // SQL99.g:242:5: table_name ( ( AS )? alias_name )?
                    {
                    pushFollow(FOLLOW_table_name_in_table_primary1123);
                    table_name();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:242:16: ( ( AS )? alias_name )?
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==AS||(LA39_0>=STRING && LA39_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt39=1;
                    }
                    switch (alt39) {
                        case 1 :
                            // SQL99.g:242:17: ( AS )? alias_name
                            {
                            // SQL99.g:242:17: ( AS )?
                            int alt38=2;
                            int LA38_0 = input.LA(1);

                            if ( (LA38_0==AS) ) {
                                alt38=1;
                            }
                            switch (alt38) {
                                case 1 :
                                    // SQL99.g:242:17: AS
                                    {
                                    match(input,AS,FOLLOW_AS_in_table_primary1126); if (state.failed) return ;

                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_alias_name_in_table_primary1129);
                            alias_name();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // SQL99.g:243:5: derived_table ( AS )? alias_name
                    {
                    pushFollow(FOLLOW_derived_table_in_table_primary1137);
                    derived_table();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:243:19: ( AS )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==AS) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // SQL99.g:243:19: AS
                            {
                            match(input,AS,FOLLOW_AS_in_table_primary1139); if (state.failed) return ;

                            }
                            break;

                    }

                    pushFollow(FOLLOW_alias_name_in_table_primary1142);
                    alias_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_primary"


    // $ANTLR start "table_name"
    // SQL99.g:246:1: table_name : ( schema_name DOT )? table_identifier ;
    public final void table_name() throws RecognitionException {
        try {
            // SQL99.g:247:3: ( ( schema_name DOT )? table_identifier )
            // SQL99.g:247:5: ( schema_name DOT )? table_identifier
            {
            // SQL99.g:247:5: ( schema_name DOT )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==STRING) ) {
                int LA42_1 = input.LA(2);

                if ( (LA42_1==DOT) ) {
                    alt42=1;
                }
            }
            else if ( (LA42_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA42_2 = input.LA(2);

                if ( (LA42_2==DOT) ) {
                    alt42=1;
                }
            }
            switch (alt42) {
                case 1 :
                    // SQL99.g:247:6: schema_name DOT
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name1158);
                    schema_name();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_table_name1160); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_table_identifier_in_table_name1164);
            table_identifier();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_name"


    // $ANTLR start "alias_name"
    // SQL99.g:250:1: alias_name : identifier ;
    public final void alias_name() throws RecognitionException {
        try {
            // SQL99.g:251:3: ( identifier )
            // SQL99.g:251:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name1179);
            identifier();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "alias_name"


    // $ANTLR start "derived_table"
    // SQL99.g:254:1: derived_table : table_subquery ;
    public final void derived_table() throws RecognitionException {
        try {
            // SQL99.g:255:3: ( table_subquery )
            // SQL99.g:255:5: table_subquery
            {
            pushFollow(FOLLOW_table_subquery_in_derived_table1192);
            table_subquery();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "derived_table"


    // $ANTLR start "table_identifier"
    // SQL99.g:258:1: table_identifier : identifier ;
    public final void table_identifier() throws RecognitionException {
        try {
            // SQL99.g:259:3: ( identifier )
            // SQL99.g:259:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier1209);
            identifier();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_identifier"


    // $ANTLR start "schema_name"
    // SQL99.g:262:1: schema_name : identifier ;
    public final void schema_name() throws RecognitionException {
        try {
            // SQL99.g:263:3: ( identifier )
            // SQL99.g:263:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name1224);
            identifier();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "schema_name"


    // $ANTLR start "column_name"
    // SQL99.g:266:1: column_name : identifier ;
    public final void column_name() throws RecognitionException {
        try {
            // SQL99.g:267:3: ( identifier )
            // SQL99.g:267:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name1241);
            identifier();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "column_name"


    // $ANTLR start "identifier"
    // SQL99.g:270:1: identifier : ( regular_identifier | delimited_identifier );
    public final void identifier() throws RecognitionException {
        try {
            // SQL99.g:271:3: ( regular_identifier | delimited_identifier )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==STRING) ) {
                alt43=1;
            }
            else if ( (LA43_0==STRING_WITH_QUOTE_DOUBLE) ) {
                alt43=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // SQL99.g:271:5: regular_identifier
                    {
                    pushFollow(FOLLOW_regular_identifier_in_identifier1256);
                    regular_identifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:272:5: delimited_identifier
                    {
                    pushFollow(FOLLOW_delimited_identifier_in_identifier1263);
                    delimited_identifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "identifier"


    // $ANTLR start "regular_identifier"
    // SQL99.g:275:1: regular_identifier : STRING ;
    public final void regular_identifier() throws RecognitionException {
        try {
            // SQL99.g:276:3: ( STRING )
            // SQL99.g:276:5: STRING
            {
            match(input,STRING,FOLLOW_STRING_in_regular_identifier1276); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "regular_identifier"


    // $ANTLR start "delimited_identifier"
    // SQL99.g:279:1: delimited_identifier : STRING_WITH_QUOTE_DOUBLE ;
    public final void delimited_identifier() throws RecognitionException {
        try {
            // SQL99.g:280:3: ( STRING_WITH_QUOTE_DOUBLE )
            // SQL99.g:280:5: STRING_WITH_QUOTE_DOUBLE
            {
            match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1289); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "delimited_identifier"


    // $ANTLR start "value"
    // SQL99.g:283:1: value : ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE );
    public final void value() throws RecognitionException {
        try {
            // SQL99.g:284:3: ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE )
            // SQL99.g:
            {
            if ( (input.LA(1)>=TRUE && input.LA(1)<=STRING_WITH_QUOTE) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "value"


    // $ANTLR start "truth_value"
    // SQL99.g:290:1: truth_value : ( TRUE | FALSE );
    public final void truth_value() throws RecognitionException {
        try {
            // SQL99.g:291:3: ( TRUE | FALSE )
            // SQL99.g:
            {
            if ( (input.LA(1)>=TRUE && input.LA(1)<=FALSE) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "truth_value"


    // $ANTLR start "concatenation_operator"
    // SQL99.g:295:1: concatenation_operator : CONCATENATION ;
    public final void concatenation_operator() throws RecognitionException {
        try {
            // SQL99.g:296:3: ( CONCATENATION )
            // SQL99.g:296:5: CONCATENATION
            {
            match(input,CONCATENATION,FOLLOW_CONCATENATION_in_concatenation_operator1353); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "concatenation_operator"


    // $ANTLR start "equals_operator"
    // SQL99.g:299:1: equals_operator : EQUALS ;
    public final void equals_operator() throws RecognitionException {
        try {
            // SQL99.g:300:3: ( EQUALS )
            // SQL99.g:300:5: EQUALS
            {
            match(input,EQUALS,FOLLOW_EQUALS_in_equals_operator1366); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "equals_operator"


    // $ANTLR start "not_equals_operator"
    // SQL99.g:303:1: not_equals_operator : LESS GREATER ;
    public final void not_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:304:3: ( LESS GREATER )
            // SQL99.g:304:5: LESS GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_not_equals_operator1379); if (state.failed) return ;
            match(input,GREATER,FOLLOW_GREATER_in_not_equals_operator1381); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "not_equals_operator"


    // $ANTLR start "less_than_operator"
    // SQL99.g:307:1: less_than_operator : LESS ;
    public final void less_than_operator() throws RecognitionException {
        try {
            // SQL99.g:308:3: ( LESS )
            // SQL99.g:308:5: LESS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_operator1396); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "less_than_operator"


    // $ANTLR start "greater_than_operator"
    // SQL99.g:311:1: greater_than_operator : GREATER ;
    public final void greater_than_operator() throws RecognitionException {
        try {
            // SQL99.g:312:3: ( GREATER )
            // SQL99.g:312:5: GREATER
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_operator1411); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "greater_than_operator"


    // $ANTLR start "less_than_or_equals_operator"
    // SQL99.g:315:1: less_than_or_equals_operator : LESS EQUALS ;
    public final void less_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:316:3: ( LESS EQUALS )
            // SQL99.g:316:5: LESS EQUALS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_or_equals_operator1425); if (state.failed) return ;
            match(input,EQUALS,FOLLOW_EQUALS_in_less_than_or_equals_operator1427); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "less_than_or_equals_operator"


    // $ANTLR start "greater_than_or_equals_operator"
    // SQL99.g:319:1: greater_than_or_equals_operator : GREATER EQUALS ;
    public final void greater_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:320:3: ( GREATER EQUALS )
            // SQL99.g:320:5: GREATER EQUALS
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_or_equals_operator1441); if (state.failed) return ;
            match(input,EQUALS,FOLLOW_EQUALS_in_greater_than_or_equals_operator1443); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "greater_than_or_equals_operator"

    // Delegated rules


 

    public static final BitSet FOLLOW_query_in_parse30 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse32 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_query_expression_in_query47 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_UNION_in_query50 = new BitSet(new long[]{0x00000000000000E0L});
    public static final BitSet FOLLOW_set_quantifier_in_query53 = new BitSet(new long[]{0x00000000000000E0L});
    public static final BitSet FOLLOW_query_expression_in_query57 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_SELECT_in_query_expression74 = new BitSet(new long[]{0x00000180003FD1C0L});
    public static final BitSet FOLLOW_set_quantifier_in_query_expression76 = new BitSet(new long[]{0x00000180003FD1C0L});
    public static final BitSet FOLLOW_select_list_in_query_expression79 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_table_expression_in_query_expression81 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_quantifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_select_list118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list124 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_select_list127 = new BitSet(new long[]{0x00000180003FD1C0L});
    public static final BitSet FOLLOW_select_sublist_in_select_list129 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist146 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk167 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_qualified_asterisk169 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column186 = new BitSet(new long[]{0x0000018000000802L});
    public static final BitSet FOLLOW_AS_in_derived_column189 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_alias_name_in_derived_column192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_value_expression_in_value_expression210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reference_value_expression_in_value_expression216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_value_expression_in_value_expression222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_string_value_expression235 = new BitSet(new long[]{0x00001F8000000000L});
    public static final BitSet FOLLOW_concatenation_in_string_value_expression237 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_string_value_expression239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_concatenation_value_in_concatenation254 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_concatenation_operator_in_concatenation257 = new BitSet(new long[]{0x00001F8000000000L});
    public static final BitSet FOLLOW_concatenation_value_in_concatenation259 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_column_reference_in_concatenation_value274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_concatenation_value280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_reference_value_expression293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference307 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_column_reference309 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_column_name_in_column_reference313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification343 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification345 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification347 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function370 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function372 = new BitSet(new long[]{0x00000180003FD1C0L});
    public static final BitSet FOLLOW_set_quantifier_in_general_set_function375 = new BitSet(new long[]{0x00000180003FD1C0L});
    public static final BitSet FOLLOW_value_expression_in_general_set_function379 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_function_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression457 = new BitSet(new long[]{0x0000000040800002L});
    public static final BitSet FOLLOW_where_clause_in_table_expression460 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_group_by_clause_in_table_expression465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause482 = new BitSet(new long[]{0x0000018000001000L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list501 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list504 = new BitSet(new long[]{0x0000018000001000L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list506 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_table_primary_in_table_reference534 = new BitSet(new long[]{0x0000003B00000002L});
    public static final BitSet FOLLOW_joined_table_in_table_reference537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause552 = new BitSet(new long[]{0x00000180043FD1C0L});
    public static final BitSet FOLLOW_search_condition_in_where_clause554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression582 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_OR_in_boolean_value_expression585 = new BitSet(new long[]{0x00000180043FD1C0L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression587 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term604 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_AND_in_boolean_term607 = new BitSet(new long[]{0x00000180043FD1C0L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term609 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_NOT_in_boolean_factor627 = new BitSet(new long[]{0x00000180043FD1C0L});
    public static final BitSet FOLLOW_boolean_test_in_boolean_factor631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_primary_in_boolean_test644 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_IS_in_boolean_test647 = new BitSet(new long[]{0x0000060004000000L});
    public static final BitSet FOLLOW_NOT_in_boolean_test650 = new BitSet(new long[]{0x0000060004000000L});
    public static final BitSet FOLLOW_truth_value_in_boolean_test654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_boolean_primary669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parenthesized_boolean_value_expression_in_boolean_primary675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_parenthesized_boolean_value_expression688 = new BitSet(new long[]{0x00000180043FD1C0L});
    public static final BitSet FOLLOW_boolean_value_expression_in_parenthesized_boolean_value_expression690 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_parenthesized_boolean_value_expression692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_predicate_in_predicate712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_in_predicate_in_predicate718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate733 = new BitSet(new long[]{0x0001C00000000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate735 = new BitSet(new long[]{0x00001F80003FD1C0L});
    public static final BitSet FOLLOW_value_in_comparison_predicate738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equals_operator_in_comp_op754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equals_operator_in_comp_op760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_operator_in_comp_op766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_operator_in_comp_op772 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_or_equals_operator_in_comp_op778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_or_equals_operator_in_comp_op784 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate797 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate799 = new BitSet(new long[]{0x0000000014000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate802 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate819 = new BitSet(new long[]{0x0000000024000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate822 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate826 = new BitSet(new long[]{0x0000018000001000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value849 = new BitSet(new long[]{0x00001F8000000000L});
    public static final BitSet FOLLOW_value_list_in_in_predicate_value851 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_table_subquery866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_subquery879 = new BitSet(new long[]{0x00000000000000E0L});
    public static final BitSet FOLLOW_query_in_subquery881 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_subquery883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_value_list898 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_value_list901 = new BitSet(new long[]{0x00001F8000000000L});
    public static final BitSet FOLLOW_value_in_value_list903 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_GROUP_in_group_by_clause918 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_BY_in_group_by_clause920 = new BitSet(new long[]{0x0000018000001000L});
    public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list935 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_grouping_element_list938 = new BitSet(new long[]{0x0000018000001000L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list940 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_grouping_element963 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element965 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_grouping_element967 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference983 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list998 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list1001 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1003 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_qualified_join_in_joined_table1020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_type_in_qualified_join1034 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_JOIN_in_qualified_join1038 = new BitSet(new long[]{0x0000018000001000L});
    public static final BitSet FOLLOW_table_primary_in_qualified_join1040 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_join_condition_in_qualified_join1042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_join_type1055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outer_join_type_in_join_type1061 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_OUTER_in_join_type1064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_outer_join_type0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_condition1108 = new BitSet(new long[]{0x00000180043FD1C0L});
    public static final BitSet FOLLOW_search_condition_in_join_condition1110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_table_primary1123 = new BitSet(new long[]{0x0000018000000802L});
    public static final BitSet FOLLOW_AS_in_table_primary1126 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_table_in_table_primary1137 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_AS_in_table_primary1139 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name1158 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_table_name1160 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_table_identifier_in_table_name1164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name1179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_derived_table1192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier1209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name1224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name1241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regular_identifier_in_identifier1256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delimited_identifier_in_identifier1263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_regular_identifier1276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_value0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_truth_value0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONCATENATION_in_concatenation_operator1353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_equals_operator1366 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_not_equals_operator1379 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_GREATER_in_not_equals_operator1381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_operator1396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_operator1411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_or_equals_operator1425 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_less_than_or_equals_operator1427 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_or_equals_operator1441 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_greater_than_or_equals_operator1443 = new BitSet(new long[]{0x0000000000000002L});

}