// $ANTLR 3.3 Nov 30, 2010 12:50:56 SQL99.g 2011-06-22 16:15:47

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "UNION", "SELECT", "DISTINCT", "ALL", "ASTERISK", "COMMA", "DOT", "AS", "COUNT", "LPAREN", "RPAREN", "AVG", "MAX", "MIN", "SUM", "EVERY", "ANY", "SOME", "FROM", "WHERE", "OR", "AND", "IS", "NOT", "NULL", "IN", "GROUP", "BY", "JOIN", "INNER", "OUTER", "LEFT", "RIGHT", "FULL", "ON", "STRING", "STRING_WITH_QUOTE_DOUBLE", "TRUE", "FALSE", "NUMERIC", "STRING_WITH_QUOTE", "EQUALS", "LESS", "GREATER", "ORDER", "SEMI", "LSQ_BRACKET", "RSQ_BRACKET", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", "QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "DASH", "AMPERSAND", "AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "COLON", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "TILDE", "CARET", "ALPHA", "DIGIT", "ALPHANUM", "CHAR", "WS"
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
            else if ( (LA5_0==COUNT||(LA5_0>=AVG && LA5_0<=SOME)||(LA5_0>=STRING && LA5_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
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
    // SQL99.g:50:1: value_expression : ( reference_value_expression | collection_value_expression );
    public final void value_expression() throws RecognitionException {
        try {
            // SQL99.g:51:3: ( reference_value_expression | collection_value_expression )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=STRING && LA9_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt9=1;
            }
            else if ( (LA9_0==COUNT||(LA9_0>=AVG && LA9_0<=SOME)) ) {
                alt9=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // SQL99.g:51:5: reference_value_expression
                    {
                    pushFollow(FOLLOW_reference_value_expression_in_value_expression210);
                    reference_value_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:52:5: collection_value_expression
                    {
                    pushFollow(FOLLOW_collection_value_expression_in_value_expression216);
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


    // $ANTLR start "reference_value_expression"
    // SQL99.g:55:1: reference_value_expression : column_reference ;
    public final void reference_value_expression() throws RecognitionException {
        try {
            // SQL99.g:56:3: ( column_reference )
            // SQL99.g:56:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_reference_value_expression229);
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
    // SQL99.g:59:1: column_reference : ( table_identifier DOT )? column_name ;
    public final void column_reference() throws RecognitionException {
        try {
            // SQL99.g:60:3: ( ( table_identifier DOT )? column_name )
            // SQL99.g:60:5: ( table_identifier DOT )? column_name
            {
            // SQL99.g:60:5: ( table_identifier DOT )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==STRING) ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1==DOT) ) {
                    alt10=1;
                }
            }
            else if ( (LA10_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA10_2 = input.LA(2);

                if ( (LA10_2==DOT) ) {
                    alt10=1;
                }
            }
            switch (alt10) {
                case 1 :
                    // SQL99.g:60:6: table_identifier DOT
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference243);
                    table_identifier();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_column_reference245); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_column_name_in_column_reference249);
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
    // SQL99.g:63:1: collection_value_expression : set_function_specification ;
    public final void collection_value_expression() throws RecognitionException {
        try {
            // SQL99.g:64:3: ( set_function_specification )
            // SQL99.g:64:5: set_function_specification
            {
            pushFollow(FOLLOW_set_function_specification_in_collection_value_expression266);
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
    // SQL99.g:67:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        try {
            // SQL99.g:68:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==COUNT) ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1==LPAREN) ) {
                    int LA11_3 = input.LA(3);

                    if ( (LA11_3==ASTERISK) ) {
                        alt11=1;
                    }
                    else if ( ((LA11_3>=DISTINCT && LA11_3<=ALL)||LA11_3==COUNT||(LA11_3>=AVG && LA11_3<=SOME)||(LA11_3>=STRING && LA11_3<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt11=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 11, 3, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA11_0>=AVG && LA11_0<=SOME)) ) {
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
                    // SQL99.g:68:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    match(input,COUNT,FOLLOW_COUNT_in_set_function_specification279); if (state.failed) return ;
                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification281); if (state.failed) return ;
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification283); if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification285); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:69:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification291);
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
    // SQL99.g:72:1: general_set_function : set_function_op LPAREN ( set_quantifier )? value_expression RPAREN ;
    public final void general_set_function() throws RecognitionException {
        try {
            // SQL99.g:73:3: ( set_function_op LPAREN ( set_quantifier )? value_expression RPAREN )
            // SQL99.g:73:5: set_function_op LPAREN ( set_quantifier )? value_expression RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function306);
            set_function_op();

            state._fsp--;
            if (state.failed) return ;
            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function308); if (state.failed) return ;
            // SQL99.g:73:28: ( set_quantifier )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( ((LA12_0>=DISTINCT && LA12_0<=ALL)) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // SQL99.g:73:29: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_general_set_function311);
                    set_quantifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_value_expression_in_general_set_function315);
            value_expression();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function317); if (state.failed) return ;

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
    // SQL99.g:76:1: set_function_op : ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT );
    public final void set_function_op() throws RecognitionException {
        try {
            // SQL99.g:77:3: ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT )
            // SQL99.g:
            {
            if ( input.LA(1)==COUNT||(input.LA(1)>=AVG && input.LA(1)<=SOME) ) {
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
    // SQL99.g:87:1: table_expression : from_clause ( where_clause )? ( group_by_clause )? ;
    public final void table_expression() throws RecognitionException {
        try {
            // SQL99.g:88:3: ( from_clause ( where_clause )? ( group_by_clause )? )
            // SQL99.g:88:5: from_clause ( where_clause )? ( group_by_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression393);
            from_clause();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:88:17: ( where_clause )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==WHERE) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // SQL99.g:88:18: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression396);
                    where_clause();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // SQL99.g:88:33: ( group_by_clause )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==GROUP) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // SQL99.g:88:34: group_by_clause
                    {
                    pushFollow(FOLLOW_group_by_clause_in_table_expression401);
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
    // SQL99.g:91:1: from_clause : FROM table_reference_list ;
    public final void from_clause() throws RecognitionException {
        try {
            // SQL99.g:92:3: ( FROM table_reference_list )
            // SQL99.g:92:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause418); if (state.failed) return ;
            pushFollow(FOLLOW_table_reference_list_in_from_clause420);
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
    // SQL99.g:95:1: table_reference_list : table_reference ( COMMA table_reference )* ;
    public final void table_reference_list() throws RecognitionException {
        try {
            // SQL99.g:96:3: ( table_reference ( COMMA table_reference )* )
            // SQL99.g:96:5: table_reference ( COMMA table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list437);
            table_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:96:21: ( COMMA table_reference )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==COMMA) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // SQL99.g:96:22: COMMA table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list440); if (state.failed) return ;
            	    pushFollow(FOLLOW_table_reference_in_table_reference_list442);
            	    table_reference();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
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
    // SQL99.g:99:1: table_reference options {backtrack=true; } : table_primary ( joined_table )? ;
    public final void table_reference() throws RecognitionException {
        try {
            // SQL99.g:103:3: ( table_primary ( joined_table )? )
            // SQL99.g:103:5: table_primary ( joined_table )?
            {
            pushFollow(FOLLOW_table_primary_in_table_reference470);
            table_primary();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:103:19: ( joined_table )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( ((LA16_0>=JOIN && LA16_0<=INNER)||(LA16_0>=LEFT && LA16_0<=FULL)) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // SQL99.g:103:20: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference473);
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
    // SQL99.g:106:1: where_clause : WHERE search_condition ;
    public final void where_clause() throws RecognitionException {
        try {
            // SQL99.g:107:3: ( WHERE search_condition )
            // SQL99.g:107:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause488); if (state.failed) return ;
            pushFollow(FOLLOW_search_condition_in_where_clause490);
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
    // SQL99.g:110:1: search_condition : boolean_value_expression ;
    public final void search_condition() throws RecognitionException {
        try {
            // SQL99.g:111:3: ( boolean_value_expression )
            // SQL99.g:111:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition503);
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
    // SQL99.g:114:1: boolean_value_expression : boolean_term ( ( OR | AND ) boolean_term )* ;
    public final void boolean_value_expression() throws RecognitionException {
        try {
            // SQL99.g:115:3: ( boolean_term ( ( OR | AND ) boolean_term )* )
            // SQL99.g:115:5: boolean_term ( ( OR | AND ) boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression518);
            boolean_term();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:115:18: ( ( OR | AND ) boolean_term )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( ((LA17_0>=OR && LA17_0<=AND)) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // SQL99.g:115:19: ( OR | AND ) boolean_term
            	    {
            	    if ( (input.LA(1)>=OR && input.LA(1)<=AND) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression527);
            	    boolean_term();

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
    // $ANTLR end "boolean_value_expression"


    // $ANTLR start "boolean_term"
    // SQL99.g:118:1: boolean_term : predicate ;
    public final void boolean_term() throws RecognitionException {
        try {
            // SQL99.g:119:3: ( predicate )
            // SQL99.g:119:5: predicate
            {
            pushFollow(FOLLOW_predicate_in_boolean_term542);
            predicate();

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
    // $ANTLR end "boolean_term"


    // $ANTLR start "predicate"
    // SQL99.g:122:1: predicate : ( comparison_predicate | null_predicate | in_predicate );
    public final void predicate() throws RecognitionException {
        try {
            // SQL99.g:123:3: ( comparison_predicate | null_predicate | in_predicate )
            int alt18=3;
            switch ( input.LA(1) ) {
            case STRING:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA18_4 = input.LA(3);

                    if ( (LA18_4==STRING) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt18=1;
                            }
                            break;
                        case IS:
                            {
                            alt18=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt18=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 18, 7, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA18_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt18=1;
                            }
                            break;
                        case IS:
                            {
                            alt18=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt18=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 18, 8, input);

                            throw nvae;
                        }

                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 18, 4, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt18=1;
                    }
                    break;
                case IS:
                    {
                    alt18=2;
                    }
                    break;
                case NOT:
                case IN:
                    {
                    alt18=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;
                }

                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA18_4 = input.LA(3);

                    if ( (LA18_4==STRING) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt18=1;
                            }
                            break;
                        case IS:
                            {
                            alt18=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt18=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 18, 7, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA18_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt18=1;
                            }
                            break;
                        case IS:
                            {
                            alt18=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt18=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 18, 8, input);

                            throw nvae;
                        }

                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 18, 4, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt18=1;
                    }
                    break;
                case IS:
                    {
                    alt18=2;
                    }
                    break;
                case NOT:
                case IN:
                    {
                    alt18=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 2, input);

                    throw nvae;
                }

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
                alt18=1;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // SQL99.g:123:5: comparison_predicate
                    {
                    pushFollow(FOLLOW_comparison_predicate_in_predicate557);
                    comparison_predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:124:5: null_predicate
                    {
                    pushFollow(FOLLOW_null_predicate_in_predicate563);
                    null_predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // SQL99.g:125:5: in_predicate
                    {
                    pushFollow(FOLLOW_in_predicate_in_predicate569);
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
    // SQL99.g:128:1: comparison_predicate : value_expression comp_op ( value | value_expression ) ;
    public final void comparison_predicate() throws RecognitionException {
        try {
            // SQL99.g:129:3: ( value_expression comp_op ( value | value_expression ) )
            // SQL99.g:129:5: value_expression comp_op ( value | value_expression )
            {
            pushFollow(FOLLOW_value_expression_in_comparison_predicate584);
            value_expression();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_comp_op_in_comparison_predicate586);
            comp_op();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:129:30: ( value | value_expression )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=TRUE && LA19_0<=STRING_WITH_QUOTE)) ) {
                alt19=1;
            }
            else if ( (LA19_0==COUNT||(LA19_0>=AVG && LA19_0<=SOME)||(LA19_0>=STRING && LA19_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt19=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // SQL99.g:129:31: value
                    {
                    pushFollow(FOLLOW_value_in_comparison_predicate589);
                    value();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:129:37: value_expression
                    {
                    pushFollow(FOLLOW_value_expression_in_comparison_predicate591);
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
    // SQL99.g:132:1: comp_op : ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator );
    public final void comp_op() throws RecognitionException {
        try {
            // SQL99.g:133:3: ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator )
            int alt20=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt20=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt20=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt20=5;
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
                case STRING:
                case STRING_WITH_QUOTE_DOUBLE:
                case TRUE:
                case FALSE:
                case NUMERIC:
                case STRING_WITH_QUOTE:
                    {
                    alt20=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 2, input);

                    throw nvae;
                }

                }
                break;
            case GREATER:
                {
                int LA20_3 = input.LA(2);

                if ( (LA20_3==EQUALS) ) {
                    alt20=6;
                }
                else if ( (LA20_3==COUNT||(LA20_3>=AVG && LA20_3<=SOME)||(LA20_3>=STRING && LA20_3<=STRING_WITH_QUOTE)) ) {
                    alt20=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }

            switch (alt20) {
                case 1 :
                    // SQL99.g:133:5: equals_operator
                    {
                    pushFollow(FOLLOW_equals_operator_in_comp_op605);
                    equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:134:5: not_equals_operator
                    {
                    pushFollow(FOLLOW_not_equals_operator_in_comp_op611);
                    not_equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // SQL99.g:135:5: less_than_operator
                    {
                    pushFollow(FOLLOW_less_than_operator_in_comp_op617);
                    less_than_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // SQL99.g:136:5: greater_than_operator
                    {
                    pushFollow(FOLLOW_greater_than_operator_in_comp_op623);
                    greater_than_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // SQL99.g:137:5: less_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_less_than_or_equals_operator_in_comp_op629);
                    less_than_or_equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // SQL99.g:138:5: greater_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_greater_than_or_equals_operator_in_comp_op635);
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
    // SQL99.g:141:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // SQL99.g:142:3: ( column_reference IS ( NOT )? NULL )
            // SQL99.g:142:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate648);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            match(input,IS,FOLLOW_IS_in_null_predicate650); if (state.failed) return ;
            // SQL99.g:142:25: ( NOT )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==NOT) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // SQL99.g:142:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate653); if (state.failed) return ;

                    }
                    break;

            }

            match(input,NULL,FOLLOW_NULL_in_null_predicate657); if (state.failed) return ;

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
    // SQL99.g:145:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // SQL99.g:146:3: ( column_reference ( NOT )? IN in_predicate_value )
            // SQL99.g:146:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate670);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:146:22: ( NOT )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==NOT) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // SQL99.g:146:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate673); if (state.failed) return ;

                    }
                    break;

            }

            match(input,IN,FOLLOW_IN_in_in_predicate677); if (state.failed) return ;
            pushFollow(FOLLOW_in_predicate_value_in_in_predicate679);
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
    // SQL99.g:149:1: in_predicate_value : ( table_subquery | LPAREN value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // SQL99.g:150:3: ( table_subquery | LPAREN value_list RPAREN )
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==LPAREN) ) {
                int LA23_1 = input.LA(2);

                if ( (LA23_1==SELECT) ) {
                    alt23=1;
                }
                else if ( ((LA23_1>=TRUE && LA23_1<=STRING_WITH_QUOTE)) ) {
                    alt23=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // SQL99.g:150:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value694);
                    table_subquery();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:151:5: LPAREN value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value700); if (state.failed) return ;
                    pushFollow(FOLLOW_value_list_in_in_predicate_value702);
                    value_list();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value704); if (state.failed) return ;

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
    // SQL99.g:154:1: table_subquery : subquery ;
    public final void table_subquery() throws RecognitionException {
        try {
            // SQL99.g:155:3: ( subquery )
            // SQL99.g:155:5: subquery
            {
            pushFollow(FOLLOW_subquery_in_table_subquery717);
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
    // SQL99.g:158:1: subquery : LPAREN query RPAREN ;
    public final void subquery() throws RecognitionException {
        try {
            // SQL99.g:159:3: ( LPAREN query RPAREN )
            // SQL99.g:159:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_subquery730); if (state.failed) return ;
            pushFollow(FOLLOW_query_in_subquery732);
            query();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_subquery734); if (state.failed) return ;

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
    // SQL99.g:162:1: value_list : value ( COMMA value )* ;
    public final void value_list() throws RecognitionException {
        try {
            // SQL99.g:163:3: ( value ( COMMA value )* )
            // SQL99.g:163:5: value ( COMMA value )*
            {
            pushFollow(FOLLOW_value_in_value_list747);
            value();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:163:11: ( COMMA value )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==COMMA) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // SQL99.g:163:12: COMMA value
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_value_list750); if (state.failed) return ;
            	    pushFollow(FOLLOW_value_in_value_list752);
            	    value();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop24;
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
    // SQL99.g:166:1: group_by_clause : GROUP BY grouping_element_list ;
    public final void group_by_clause() throws RecognitionException {
        try {
            // SQL99.g:167:3: ( GROUP BY grouping_element_list )
            // SQL99.g:167:5: GROUP BY grouping_element_list
            {
            match(input,GROUP,FOLLOW_GROUP_in_group_by_clause767); if (state.failed) return ;
            match(input,BY,FOLLOW_BY_in_group_by_clause769); if (state.failed) return ;
            pushFollow(FOLLOW_grouping_element_list_in_group_by_clause771);
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
    // SQL99.g:170:1: grouping_element_list : grouping_element ( COMMA grouping_element )* ;
    public final void grouping_element_list() throws RecognitionException {
        try {
            // SQL99.g:171:3: ( grouping_element ( COMMA grouping_element )* )
            // SQL99.g:171:5: grouping_element ( COMMA grouping_element )*
            {
            pushFollow(FOLLOW_grouping_element_in_grouping_element_list784);
            grouping_element();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:171:22: ( COMMA grouping_element )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==COMMA) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // SQL99.g:171:23: COMMA grouping_element
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list787); if (state.failed) return ;
            	    pushFollow(FOLLOW_grouping_element_in_grouping_element_list789);
            	    grouping_element();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop25;
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
    // SQL99.g:174:1: grouping_element : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
    public final void grouping_element() throws RecognitionException {
        try {
            // SQL99.g:175:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( ((LA26_0>=STRING && LA26_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt26=1;
            }
            else if ( (LA26_0==LPAREN) ) {
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
                    // SQL99.g:175:5: grouping_column_reference
                    {
                    pushFollow(FOLLOW_grouping_column_reference_in_grouping_element806);
                    grouping_column_reference();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:176:5: LPAREN grouping_column_reference_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element812); if (state.failed) return ;
                    pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element814);
                    grouping_column_reference_list();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element816); if (state.failed) return ;

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
    // SQL99.g:179:1: grouping_column_reference : column_reference ;
    public final void grouping_column_reference() throws RecognitionException {
        try {
            // SQL99.g:180:3: ( column_reference )
            // SQL99.g:180:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference832);
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
    // SQL99.g:183:1: grouping_column_reference_list : column_reference ( COMMA column_reference )* ;
    public final void grouping_column_reference_list() throws RecognitionException {
        try {
            // SQL99.g:184:3: ( column_reference ( COMMA column_reference )* )
            // SQL99.g:184:5: column_reference ( COMMA column_reference )*
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list847);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:184:22: ( COMMA column_reference )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==COMMA) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // SQL99.g:184:23: COMMA column_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list850); if (state.failed) return ;
            	    pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list852);
            	    column_reference();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop27;
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
    // SQL99.g:187:1: joined_table : qualified_join ;
    public final void joined_table() throws RecognitionException {
        try {
            // SQL99.g:188:3: ( qualified_join )
            // SQL99.g:188:5: qualified_join
            {
            pushFollow(FOLLOW_qualified_join_in_joined_table869);
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
    // SQL99.g:191:1: qualified_join : ( join_type )? JOIN table_primary join_condition ;
    public final void qualified_join() throws RecognitionException {
        try {
            // SQL99.g:192:3: ( ( join_type )? JOIN table_primary join_condition )
            // SQL99.g:192:5: ( join_type )? JOIN table_primary join_condition
            {
            // SQL99.g:192:5: ( join_type )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==INNER||(LA28_0>=LEFT && LA28_0<=FULL)) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // SQL99.g:192:6: join_type
                    {
                    pushFollow(FOLLOW_join_type_in_qualified_join883);
                    join_type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,JOIN,FOLLOW_JOIN_in_qualified_join887); if (state.failed) return ;
            pushFollow(FOLLOW_table_primary_in_qualified_join889);
            table_primary();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_join_condition_in_qualified_join891);
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
    // SQL99.g:195:1: join_type : ( INNER | outer_join_type ( OUTER )? );
    public final void join_type() throws RecognitionException {
        try {
            // SQL99.g:196:3: ( INNER | outer_join_type ( OUTER )? )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==INNER) ) {
                alt30=1;
            }
            else if ( ((LA30_0>=LEFT && LA30_0<=FULL)) ) {
                alt30=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }
            switch (alt30) {
                case 1 :
                    // SQL99.g:196:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type904); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:197:5: outer_join_type ( OUTER )?
                    {
                    pushFollow(FOLLOW_outer_join_type_in_join_type910);
                    outer_join_type();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:197:21: ( OUTER )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==OUTER) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // SQL99.g:197:22: OUTER
                            {
                            match(input,OUTER,FOLLOW_OUTER_in_join_type913); if (state.failed) return ;

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
    // SQL99.g:200:1: outer_join_type : ( LEFT | RIGHT | FULL );
    public final void outer_join_type() throws RecognitionException {
        try {
            // SQL99.g:201:3: ( LEFT | RIGHT | FULL )
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
    // SQL99.g:206:1: join_condition : ON search_condition ;
    public final void join_condition() throws RecognitionException {
        try {
            // SQL99.g:207:3: ( ON search_condition )
            // SQL99.g:207:5: ON search_condition
            {
            match(input,ON,FOLLOW_ON_in_join_condition957); if (state.failed) return ;
            pushFollow(FOLLOW_search_condition_in_join_condition959);
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
    // SQL99.g:210:1: table_primary : ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name );
    public final void table_primary() throws RecognitionException {
        try {
            // SQL99.g:211:3: ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( ((LA34_0>=STRING && LA34_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt34=1;
            }
            else if ( (LA34_0==LPAREN) ) {
                alt34=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // SQL99.g:211:5: table_name ( ( AS )? alias_name )?
                    {
                    pushFollow(FOLLOW_table_name_in_table_primary972);
                    table_name();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:211:16: ( ( AS )? alias_name )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==AS||(LA32_0>=STRING && LA32_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // SQL99.g:211:17: ( AS )? alias_name
                            {
                            // SQL99.g:211:17: ( AS )?
                            int alt31=2;
                            int LA31_0 = input.LA(1);

                            if ( (LA31_0==AS) ) {
                                alt31=1;
                            }
                            switch (alt31) {
                                case 1 :
                                    // SQL99.g:211:17: AS
                                    {
                                    match(input,AS,FOLLOW_AS_in_table_primary975); if (state.failed) return ;

                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_alias_name_in_table_primary978);
                            alias_name();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // SQL99.g:212:5: derived_table ( AS )? alias_name
                    {
                    pushFollow(FOLLOW_derived_table_in_table_primary986);
                    derived_table();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:212:19: ( AS )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==AS) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // SQL99.g:212:19: AS
                            {
                            match(input,AS,FOLLOW_AS_in_table_primary988); if (state.failed) return ;

                            }
                            break;

                    }

                    pushFollow(FOLLOW_alias_name_in_table_primary991);
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
    // SQL99.g:215:1: table_name : ( schema_name DOT )? table_identifier ;
    public final void table_name() throws RecognitionException {
        try {
            // SQL99.g:216:3: ( ( schema_name DOT )? table_identifier )
            // SQL99.g:216:5: ( schema_name DOT )? table_identifier
            {
            // SQL99.g:216:5: ( schema_name DOT )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==STRING) ) {
                int LA35_1 = input.LA(2);

                if ( (LA35_1==DOT) ) {
                    alt35=1;
                }
            }
            else if ( (LA35_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA35_2 = input.LA(2);

                if ( (LA35_2==DOT) ) {
                    alt35=1;
                }
            }
            switch (alt35) {
                case 1 :
                    // SQL99.g:216:6: schema_name DOT
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name1007);
                    schema_name();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_table_name1009); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_table_identifier_in_table_name1013);
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
    // SQL99.g:219:1: alias_name : identifier ;
    public final void alias_name() throws RecognitionException {
        try {
            // SQL99.g:220:3: ( identifier )
            // SQL99.g:220:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name1028);
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
    // SQL99.g:223:1: derived_table : table_subquery ;
    public final void derived_table() throws RecognitionException {
        try {
            // SQL99.g:224:3: ( table_subquery )
            // SQL99.g:224:5: table_subquery
            {
            pushFollow(FOLLOW_table_subquery_in_derived_table1041);
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
    // SQL99.g:227:1: table_identifier : identifier ;
    public final void table_identifier() throws RecognitionException {
        try {
            // SQL99.g:228:3: ( identifier )
            // SQL99.g:228:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier1058);
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
    // SQL99.g:231:1: schema_name : identifier ;
    public final void schema_name() throws RecognitionException {
        try {
            // SQL99.g:232:3: ( identifier )
            // SQL99.g:232:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name1073);
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
    // SQL99.g:235:1: column_name : identifier ;
    public final void column_name() throws RecognitionException {
        try {
            // SQL99.g:236:3: ( identifier )
            // SQL99.g:236:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name1090);
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
    // SQL99.g:239:1: identifier : ( regular_identifier | delimited_identifier );
    public final void identifier() throws RecognitionException {
        try {
            // SQL99.g:240:3: ( regular_identifier | delimited_identifier )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==STRING) ) {
                alt36=1;
            }
            else if ( (LA36_0==STRING_WITH_QUOTE_DOUBLE) ) {
                alt36=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // SQL99.g:240:5: regular_identifier
                    {
                    pushFollow(FOLLOW_regular_identifier_in_identifier1105);
                    regular_identifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:241:5: delimited_identifier
                    {
                    pushFollow(FOLLOW_delimited_identifier_in_identifier1112);
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
    // SQL99.g:244:1: regular_identifier : STRING ;
    public final void regular_identifier() throws RecognitionException {
        try {
            // SQL99.g:245:3: ( STRING )
            // SQL99.g:245:5: STRING
            {
            match(input,STRING,FOLLOW_STRING_in_regular_identifier1125); if (state.failed) return ;

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
    // SQL99.g:248:1: delimited_identifier : STRING_WITH_QUOTE_DOUBLE ;
    public final void delimited_identifier() throws RecognitionException {
        try {
            // SQL99.g:249:3: ( STRING_WITH_QUOTE_DOUBLE )
            // SQL99.g:249:5: STRING_WITH_QUOTE_DOUBLE
            {
            match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1138); if (state.failed) return ;

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
    // SQL99.g:252:1: value : ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE );
    public final void value() throws RecognitionException {
        try {
            // SQL99.g:253:3: ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE )
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


    // $ANTLR start "equals_operator"
    // SQL99.g:259:1: equals_operator : EQUALS ;
    public final void equals_operator() throws RecognitionException {
        try {
            // SQL99.g:260:3: ( EQUALS )
            // SQL99.g:260:5: EQUALS
            {
            match(input,EQUALS,FOLLOW_EQUALS_in_equals_operator1183); if (state.failed) return ;

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
    // SQL99.g:263:1: not_equals_operator : LESS GREATER ;
    public final void not_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:264:3: ( LESS GREATER )
            // SQL99.g:264:5: LESS GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_not_equals_operator1196); if (state.failed) return ;
            match(input,GREATER,FOLLOW_GREATER_in_not_equals_operator1198); if (state.failed) return ;

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
    // SQL99.g:267:1: less_than_operator : LESS ;
    public final void less_than_operator() throws RecognitionException {
        try {
            // SQL99.g:268:3: ( LESS )
            // SQL99.g:268:5: LESS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_operator1213); if (state.failed) return ;

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
    // SQL99.g:271:1: greater_than_operator : GREATER ;
    public final void greater_than_operator() throws RecognitionException {
        try {
            // SQL99.g:272:3: ( GREATER )
            // SQL99.g:272:5: GREATER
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_operator1228); if (state.failed) return ;

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
    // SQL99.g:275:1: less_than_or_equals_operator : LESS EQUALS ;
    public final void less_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:276:3: ( LESS EQUALS )
            // SQL99.g:276:5: LESS EQUALS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_or_equals_operator1242); if (state.failed) return ;
            match(input,EQUALS,FOLLOW_EQUALS_in_less_than_or_equals_operator1244); if (state.failed) return ;

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
    // SQL99.g:279:1: greater_than_or_equals_operator : GREATER EQUALS ;
    public final void greater_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:280:3: ( GREATER EQUALS )
            // SQL99.g:280:5: GREATER EQUALS
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_or_equals_operator1258); if (state.failed) return ;
            match(input,EQUALS,FOLLOW_EQUALS_in_greater_than_or_equals_operator1260); if (state.failed) return ;

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
    public static final BitSet FOLLOW_SELECT_in_query_expression74 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_set_quantifier_in_query_expression76 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_select_list_in_query_expression79 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_table_expression_in_query_expression81 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_quantifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_select_list118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list124 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_select_list127 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_select_sublist_in_select_list129 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist146 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk167 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_qualified_asterisk169 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column186 = new BitSet(new long[]{0x0000018000000802L});
    public static final BitSet FOLLOW_AS_in_derived_column189 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_alias_name_in_derived_column192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reference_value_expression_in_value_expression210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_value_expression_in_value_expression216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_reference_value_expression229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference243 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_column_reference245 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_column_name_in_column_reference249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification279 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification281 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification283 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function306 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function308 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_set_quantifier_in_general_set_function311 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_value_expression_in_general_set_function315 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_function_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression393 = new BitSet(new long[]{0x0000000040800002L});
    public static final BitSet FOLLOW_where_clause_in_table_expression396 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_group_by_clause_in_table_expression401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause418 = new BitSet(new long[]{0x0000018000002000L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list437 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list440 = new BitSet(new long[]{0x0000018000002000L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list442 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_table_primary_in_table_reference470 = new BitSet(new long[]{0x0000003B00000002L});
    public static final BitSet FOLLOW_joined_table_in_table_reference473 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause488 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_search_condition_in_where_clause490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression518 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_set_in_boolean_value_expression521 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression527 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_predicate_in_boolean_term542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_predicate_in_predicate563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_in_predicate_in_predicate569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate584 = new BitSet(new long[]{0x0000E00000000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate586 = new BitSet(new long[]{0x00001F80003F91C0L});
    public static final BitSet FOLLOW_value_in_comparison_predicate589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate591 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equals_operator_in_comp_op605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equals_operator_in_comp_op611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_operator_in_comp_op617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_operator_in_comp_op623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_or_equals_operator_in_comp_op629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_or_equals_operator_in_comp_op635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate648 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate650 = new BitSet(new long[]{0x0000000018000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate653 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate670 = new BitSet(new long[]{0x0000000028000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate673 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate677 = new BitSet(new long[]{0x0000018000002000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value694 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value700 = new BitSet(new long[]{0x00001E0000000000L});
    public static final BitSet FOLLOW_value_list_in_in_predicate_value702 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_table_subquery717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_subquery730 = new BitSet(new long[]{0x00000000000000E0L});
    public static final BitSet FOLLOW_query_in_subquery732 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RPAREN_in_subquery734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_value_list747 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_value_list750 = new BitSet(new long[]{0x00001E0000000000L});
    public static final BitSet FOLLOW_value_in_value_list752 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_GROUP_in_group_by_clause767 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_BY_in_group_by_clause769 = new BitSet(new long[]{0x0000018000002000L});
    public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list784 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_grouping_element_list787 = new BitSet(new long[]{0x0000018000002000L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list789 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_grouping_element812 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element814 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RPAREN_in_grouping_element816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list847 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list850 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list852 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_qualified_join_in_joined_table869 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_type_in_qualified_join883 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_JOIN_in_qualified_join887 = new BitSet(new long[]{0x0000018000002000L});
    public static final BitSet FOLLOW_table_primary_in_qualified_join889 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_join_condition_in_qualified_join891 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_join_type904 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outer_join_type_in_join_type910 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_OUTER_in_join_type913 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_outer_join_type0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_condition957 = new BitSet(new long[]{0x00000180003F91C0L});
    public static final BitSet FOLLOW_search_condition_in_join_condition959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_table_primary972 = new BitSet(new long[]{0x0000018000000802L});
    public static final BitSet FOLLOW_AS_in_table_primary975 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_alias_name_in_table_primary978 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_table_in_table_primary986 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_AS_in_table_primary988 = new BitSet(new long[]{0x0000018000000800L});
    public static final BitSet FOLLOW_alias_name_in_table_primary991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name1007 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_table_name1009 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_table_identifier_in_table_name1013 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name1028 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_derived_table1041 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier1058 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name1073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name1090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regular_identifier_in_identifier1105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delimited_identifier_in_identifier1112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_regular_identifier1125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_value0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_equals_operator1183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_not_equals_operator1196 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_GREATER_in_not_equals_operator1198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_operator1213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_operator1228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_or_equals_operator1242 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_EQUALS_in_less_than_or_equals_operator1244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_or_equals_operator1258 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_EQUALS_in_greater_than_or_equals_operator1260 = new BitSet(new long[]{0x0000000000000002L});

}