// $ANTLR 3.3 Nov 30, 2010 12:50:56 SQL99.g 2011-06-20 17:03:48

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SELECT", "DISTINCT", "ALL", "ASTERISK", "COMMA", "DOT", "AS", "FROM", "WHERE", "OR", "AND", "IS", "NOT", "NULL", "JOIN", "STRING", "STRING_WITH_QUOTE", "TRUE", "FALSE", "NUMERIC", "EQUALS", "LESS", "GREATER", "ORDER", "BY", "ON", "LEFT", "RIGHT", "SEMI", "LSQ_BRACKET", "RSQ_BRACKET", "LPAREN", "RPAREN", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", "QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "DASH", "AMPERSAND", "AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "COLON", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "TILDE", "CARET", "ALPHA", "DIGIT", "ALPHANUM", "CHAR", "WS"
    };
    public static final int EOF=-1;
    public static final int SELECT=4;
    public static final int DISTINCT=5;
    public static final int ALL=6;
    public static final int ASTERISK=7;
    public static final int COMMA=8;
    public static final int DOT=9;
    public static final int AS=10;
    public static final int FROM=11;
    public static final int WHERE=12;
    public static final int OR=13;
    public static final int AND=14;
    public static final int IS=15;
    public static final int NOT=16;
    public static final int NULL=17;
    public static final int JOIN=18;
    public static final int STRING=19;
    public static final int STRING_WITH_QUOTE=20;
    public static final int TRUE=21;
    public static final int FALSE=22;
    public static final int NUMERIC=23;
    public static final int EQUALS=24;
    public static final int LESS=25;
    public static final int GREATER=26;
    public static final int ORDER=27;
    public static final int BY=28;
    public static final int ON=29;
    public static final int LEFT=30;
    public static final int RIGHT=31;
    public static final int SEMI=32;
    public static final int LSQ_BRACKET=33;
    public static final int RSQ_BRACKET=34;
    public static final int LPAREN=35;
    public static final int RPAREN=36;
    public static final int QUESTION=37;
    public static final int DOLLAR=38;
    public static final int QUOTE_DOUBLE=39;
    public static final int QUOTE_SINGLE=40;
    public static final int APOSTROPHE=41;
    public static final int UNDERSCORE=42;
    public static final int DASH=43;
    public static final int AMPERSAND=44;
    public static final int AT=45;
    public static final int EXCLAMATION=46;
    public static final int HASH=47;
    public static final int PERCENT=48;
    public static final int PLUS=49;
    public static final int COLON=50;
    public static final int SLASH=51;
    public static final int DOUBLE_SLASH=52;
    public static final int BACKSLASH=53;
    public static final int TILDE=54;
    public static final int CARET=55;
    public static final int ALPHA=56;
    public static final int DIGIT=57;
    public static final int ALPHANUM=58;
    public static final int CHAR=59;
    public static final int WS=60;

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

            match(input,EOF,FOLLOW_EOF_in_parse32); 

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
    // SQL99.g:19:1: query : SELECT ( set_quantifier )? select_list table_expression ;
    public final void query() throws RecognitionException {
        try {
            // SQL99.g:20:3: ( SELECT ( set_quantifier )? select_list table_expression )
            // SQL99.g:20:5: SELECT ( set_quantifier )? select_list table_expression
            {
            match(input,SELECT,FOLLOW_SELECT_in_query47); 
            // SQL99.g:20:12: ( set_quantifier )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( ((LA1_0>=DISTINCT && LA1_0<=ALL)) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // SQL99.g:20:12: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_query49);
                    set_quantifier();

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_select_list_in_query52);
            select_list();

            state._fsp--;

            pushFollow(FOLLOW_table_expression_in_query54);
            table_expression();

            state._fsp--;


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


    // $ANTLR start "set_quantifier"
    // SQL99.g:23:1: set_quantifier : ( DISTINCT | ALL );
    public final void set_quantifier() throws RecognitionException {
        try {
            // SQL99.g:24:3: ( DISTINCT | ALL )
            // SQL99.g:
            {
            if ( (input.LA(1)>=DISTINCT && input.LA(1)<=ALL) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
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
    // SQL99.g:28:1: select_list : ( ASTERISK | select_sublist ( COMMA select_sublist )* );
    public final void select_list() throws RecognitionException {
        try {
            // SQL99.g:29:3: ( ASTERISK | select_sublist ( COMMA select_sublist )* )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ASTERISK) ) {
                alt3=1;
            }
            else if ( ((LA3_0>=STRING && LA3_0<=STRING_WITH_QUOTE)) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // SQL99.g:29:5: ASTERISK
                    {
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_select_list91); 

                    }
                    break;
                case 2 :
                    // SQL99.g:30:5: select_sublist ( COMMA select_sublist )*
                    {
                    pushFollow(FOLLOW_select_sublist_in_select_list97);
                    select_sublist();

                    state._fsp--;

                    // SQL99.g:30:20: ( COMMA select_sublist )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( (LA2_0==COMMA) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // SQL99.g:30:21: COMMA select_sublist
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_select_list100); 
                    	    pushFollow(FOLLOW_select_sublist_in_select_list102);
                    	    select_sublist();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop2;
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
    // SQL99.g:33:1: select_sublist : ( qualified_asterisk | column_reference );
    public final void select_sublist() throws RecognitionException {
        try {
            // SQL99.g:34:3: ( qualified_asterisk | column_reference )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>=STRING && LA4_0<=STRING_WITH_QUOTE)) ) {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==DOT) ) {
                    int LA4_2 = input.LA(3);

                    if ( (LA4_2==ASTERISK) ) {
                        alt4=1;
                    }
                    else if ( ((LA4_2>=STRING && LA4_2<=STRING_WITH_QUOTE)) ) {
                        alt4=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA4_1==COMMA||(LA4_1>=AS && LA4_1<=FROM)) ) {
                    alt4=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // SQL99.g:34:5: qualified_asterisk
                    {
                    pushFollow(FOLLOW_qualified_asterisk_in_select_sublist119);
                    qualified_asterisk();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:35:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_select_sublist125);
                    column_reference();

                    state._fsp--;


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
    // SQL99.g:38:1: qualified_asterisk : table_identifier DOT ASTERISK ;
    public final void qualified_asterisk() throws RecognitionException {
        try {
            // SQL99.g:39:3: ( table_identifier DOT ASTERISK )
            // SQL99.g:39:5: table_identifier DOT ASTERISK
            {
            pushFollow(FOLLOW_table_identifier_in_qualified_asterisk140);
            table_identifier();

            state._fsp--;

            match(input,DOT,FOLLOW_DOT_in_qualified_asterisk142); 
            match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk144); 

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


    // $ANTLR start "column_reference"
    // SQL99.g:42:1: column_reference : ( table_identifier DOT )? column_name ( AS alias_name )? ;
    public final void column_reference() throws RecognitionException {
        try {
            // SQL99.g:43:3: ( ( table_identifier DOT )? column_name ( AS alias_name )? )
            // SQL99.g:43:5: ( table_identifier DOT )? column_name ( AS alias_name )?
            {
            // SQL99.g:43:5: ( table_identifier DOT )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>=STRING && LA5_0<=STRING_WITH_QUOTE)) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==DOT) ) {
                    alt5=1;
                }
            }
            switch (alt5) {
                case 1 :
                    // SQL99.g:43:6: table_identifier DOT
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference160);
                    table_identifier();

                    state._fsp--;

                    match(input,DOT,FOLLOW_DOT_in_column_reference162); 

                    }
                    break;

            }

            pushFollow(FOLLOW_column_name_in_column_reference166);
            column_name();

            state._fsp--;

            // SQL99.g:43:41: ( AS alias_name )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==AS) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // SQL99.g:43:42: AS alias_name
                    {
                    match(input,AS,FOLLOW_AS_in_column_reference169); 
                    pushFollow(FOLLOW_alias_name_in_column_reference171);
                    alias_name();

                    state._fsp--;


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
    // $ANTLR end "column_reference"


    // $ANTLR start "table_expression"
    // SQL99.g:46:1: table_expression : from_clause ( where_clause )? ;
    public final void table_expression() throws RecognitionException {
        try {
            // SQL99.g:47:3: ( from_clause ( where_clause )? )
            // SQL99.g:47:5: from_clause ( where_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression190);
            from_clause();

            state._fsp--;

            // SQL99.g:47:17: ( where_clause )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==WHERE) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // SQL99.g:47:18: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression193);
                    where_clause();

                    state._fsp--;


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
    // SQL99.g:50:1: from_clause : FROM table_reference_list ;
    public final void from_clause() throws RecognitionException {
        try {
            // SQL99.g:51:3: ( FROM table_reference_list )
            // SQL99.g:51:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause210); 
            pushFollow(FOLLOW_table_reference_list_in_from_clause212);
            table_reference_list();

            state._fsp--;


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
    // SQL99.g:54:1: table_reference_list : table_reference ( COMMA table_reference )* ;
    public final void table_reference_list() throws RecognitionException {
        try {
            // SQL99.g:55:3: ( table_reference ( COMMA table_reference )* )
            // SQL99.g:55:5: table_reference ( COMMA table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list229);
            table_reference();

            state._fsp--;

            // SQL99.g:55:21: ( COMMA table_reference )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==COMMA) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // SQL99.g:55:22: COMMA table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list232); 
            	    pushFollow(FOLLOW_table_reference_in_table_reference_list234);
            	    table_reference();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop8;
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
    // SQL99.g:58:1: table_reference : ( table_primary | joined_table );
    public final void table_reference() throws RecognitionException {
        try {
            // SQL99.g:59:3: ( table_primary | joined_table )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=STRING && LA9_0<=STRING_WITH_QUOTE)) ) {
                alt9=1;
            }
            else if ( (LA9_0==JOIN) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // SQL99.g:59:5: table_primary
                    {
                    pushFollow(FOLLOW_table_primary_in_table_reference251);
                    table_primary();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:60:5: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference258);
                    joined_table();

                    state._fsp--;


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
    // $ANTLR end "table_reference"


    // $ANTLR start "where_clause"
    // SQL99.g:63:1: where_clause : WHERE search_condition ;
    public final void where_clause() throws RecognitionException {
        try {
            // SQL99.g:64:3: ( WHERE search_condition )
            // SQL99.g:64:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause271); 
            pushFollow(FOLLOW_search_condition_in_where_clause273);
            search_condition();

            state._fsp--;


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
    // SQL99.g:67:1: search_condition : boolean_value_expression ;
    public final void search_condition() throws RecognitionException {
        try {
            // SQL99.g:68:3: ( boolean_value_expression )
            // SQL99.g:68:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition286);
            boolean_value_expression();

            state._fsp--;


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
    // SQL99.g:71:1: boolean_value_expression : boolean_term ( ( OR | AND ) boolean_term )* ;
    public final void boolean_value_expression() throws RecognitionException {
        try {
            // SQL99.g:72:3: ( boolean_term ( ( OR | AND ) boolean_term )* )
            // SQL99.g:72:5: boolean_term ( ( OR | AND ) boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression301);
            boolean_term();

            state._fsp--;

            // SQL99.g:72:18: ( ( OR | AND ) boolean_term )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>=OR && LA10_0<=AND)) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // SQL99.g:72:19: ( OR | AND ) boolean_term
            	    {
            	    if ( (input.LA(1)>=OR && input.LA(1)<=AND) ) {
            	        input.consume();
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression310);
            	    boolean_term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop10;
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
    // SQL99.g:75:1: boolean_term : predicate ;
    public final void boolean_term() throws RecognitionException {
        try {
            // SQL99.g:76:3: ( predicate )
            // SQL99.g:76:5: predicate
            {
            pushFollow(FOLLOW_predicate_in_boolean_term325);
            predicate();

            state._fsp--;


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
    // SQL99.g:79:1: predicate : ( comparison_predicate | null_predicate );
    public final void predicate() throws RecognitionException {
        try {
            // SQL99.g:80:3: ( comparison_predicate | null_predicate )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( ((LA11_0>=STRING && LA11_0<=STRING_WITH_QUOTE)) ) {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA11_2 = input.LA(3);

                    if ( ((LA11_2>=STRING && LA11_2<=STRING_WITH_QUOTE)) ) {
                        switch ( input.LA(4) ) {
                        case AS:
                            {
                            int LA11_3 = input.LA(5);

                            if ( ((LA11_3>=STRING && LA11_3<=STRING_WITH_QUOTE)) ) {
                                int LA11_7 = input.LA(6);

                                if ( ((LA11_7>=EQUALS && LA11_7<=GREATER)) ) {
                                    alt11=1;
                                }
                                else if ( (LA11_7==IS) ) {
                                    alt11=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 11, 7, input);

                                    throw nvae;
                                }
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 11, 3, input);

                                throw nvae;
                            }
                            }
                            break;
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt11=1;
                            }
                            break;
                        case IS:
                            {
                            alt11=2;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 11, 6, input);

                            throw nvae;
                        }

                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 11, 2, input);

                        throw nvae;
                    }
                    }
                    break;
                case AS:
                    {
                    int LA11_3 = input.LA(3);

                    if ( ((LA11_3>=STRING && LA11_3<=STRING_WITH_QUOTE)) ) {
                        int LA11_7 = input.LA(4);

                        if ( ((LA11_7>=EQUALS && LA11_7<=GREATER)) ) {
                            alt11=1;
                        }
                        else if ( (LA11_7==IS) ) {
                            alt11=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 11, 7, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 11, 3, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt11=1;
                    }
                    break;
                case IS:
                    {
                    alt11=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // SQL99.g:80:5: comparison_predicate
                    {
                    pushFollow(FOLLOW_comparison_predicate_in_predicate340);
                    comparison_predicate();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:81:5: null_predicate
                    {
                    pushFollow(FOLLOW_null_predicate_in_predicate346);
                    null_predicate();

                    state._fsp--;


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
    // SQL99.g:84:1: comparison_predicate : ( column_reference comp_op value | column_reference comp_op column_reference );
    public final void comparison_predicate() throws RecognitionException {
        try {
            // SQL99.g:85:3: ( column_reference comp_op value | column_reference comp_op column_reference )
            int alt12=2;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // SQL99.g:85:5: column_reference comp_op value
                    {
                    pushFollow(FOLLOW_column_reference_in_comparison_predicate361);
                    column_reference();

                    state._fsp--;

                    pushFollow(FOLLOW_comp_op_in_comparison_predicate363);
                    comp_op();

                    state._fsp--;

                    pushFollow(FOLLOW_value_in_comparison_predicate365);
                    value();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:86:5: column_reference comp_op column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_comparison_predicate371);
                    column_reference();

                    state._fsp--;

                    pushFollow(FOLLOW_comp_op_in_comparison_predicate373);
                    comp_op();

                    state._fsp--;

                    pushFollow(FOLLOW_column_reference_in_comparison_predicate375);
                    column_reference();

                    state._fsp--;


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
    // $ANTLR end "comparison_predicate"


    // $ANTLR start "comp_op"
    // SQL99.g:89:1: comp_op : ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator );
    public final void comp_op() throws RecognitionException {
        try {
            // SQL99.g:90:3: ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator )
            int alt13=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt13=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt13=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt13=5;
                    }
                    break;
                case STRING:
                case STRING_WITH_QUOTE:
                case TRUE:
                case FALSE:
                case NUMERIC:
                    {
                    alt13=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 2, input);

                    throw nvae;
                }

                }
                break;
            case GREATER:
                {
                int LA13_3 = input.LA(2);

                if ( (LA13_3==EQUALS) ) {
                    alt13=6;
                }
                else if ( ((LA13_3>=STRING && LA13_3<=NUMERIC)) ) {
                    alt13=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }

            switch (alt13) {
                case 1 :
                    // SQL99.g:90:5: equals_operator
                    {
                    pushFollow(FOLLOW_equals_operator_in_comp_op388);
                    equals_operator();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:91:5: not_equals_operator
                    {
                    pushFollow(FOLLOW_not_equals_operator_in_comp_op394);
                    not_equals_operator();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // SQL99.g:92:5: less_than_operator
                    {
                    pushFollow(FOLLOW_less_than_operator_in_comp_op400);
                    less_than_operator();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // SQL99.g:93:5: greater_than_operator
                    {
                    pushFollow(FOLLOW_greater_than_operator_in_comp_op406);
                    greater_than_operator();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // SQL99.g:94:5: less_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_less_than_or_equals_operator_in_comp_op412);
                    less_than_or_equals_operator();

                    state._fsp--;


                    }
                    break;
                case 6 :
                    // SQL99.g:95:5: greater_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_greater_than_or_equals_operator_in_comp_op418);
                    greater_than_or_equals_operator();

                    state._fsp--;


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
    // SQL99.g:98:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // SQL99.g:99:3: ( column_reference IS ( NOT )? NULL )
            // SQL99.g:99:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate431);
            column_reference();

            state._fsp--;

            match(input,IS,FOLLOW_IS_in_null_predicate433); 
            // SQL99.g:99:25: ( NOT )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==NOT) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // SQL99.g:99:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate436); 

                    }
                    break;

            }

            match(input,NULL,FOLLOW_NULL_in_null_predicate440); 

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


    // $ANTLR start "joined_table"
    // SQL99.g:102:1: joined_table : JOIN ;
    public final void joined_table() throws RecognitionException {
        try {
            // SQL99.g:103:3: ( JOIN )
            // SQL99.g:103:5: JOIN
            {
            match(input,JOIN,FOLLOW_JOIN_in_joined_table453); 

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


    // $ANTLR start "table_primary"
    // SQL99.g:106:1: table_primary : table_name ( AS alias_name )? ;
    public final void table_primary() throws RecognitionException {
        try {
            // SQL99.g:107:3: ( table_name ( AS alias_name )? )
            // SQL99.g:107:5: table_name ( AS alias_name )?
            {
            pushFollow(FOLLOW_table_name_in_table_primary466);
            table_name();

            state._fsp--;

            // SQL99.g:107:16: ( AS alias_name )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==AS) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // SQL99.g:107:17: AS alias_name
                    {
                    match(input,AS,FOLLOW_AS_in_table_primary469); 
                    pushFollow(FOLLOW_alias_name_in_table_primary471);
                    alias_name();

                    state._fsp--;


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
    // $ANTLR end "table_primary"


    // $ANTLR start "table_name"
    // SQL99.g:110:1: table_name : ( schema_name DOT )? table_identifier ;
    public final void table_name() throws RecognitionException {
        try {
            // SQL99.g:111:3: ( ( schema_name DOT )? table_identifier )
            // SQL99.g:111:5: ( schema_name DOT )? table_identifier
            {
            // SQL99.g:111:5: ( schema_name DOT )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( ((LA16_0>=STRING && LA16_0<=STRING_WITH_QUOTE)) ) {
                int LA16_1 = input.LA(2);

                if ( (LA16_1==DOT) ) {
                    alt16=1;
                }
            }
            switch (alt16) {
                case 1 :
                    // SQL99.g:111:6: schema_name DOT
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name489);
                    schema_name();

                    state._fsp--;

                    match(input,DOT,FOLLOW_DOT_in_table_name491); 

                    }
                    break;

            }

            pushFollow(FOLLOW_table_identifier_in_table_name495);
            table_identifier();

            state._fsp--;


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
    // SQL99.g:114:1: alias_name : identifier ;
    public final void alias_name() throws RecognitionException {
        try {
            // SQL99.g:115:3: ( identifier )
            // SQL99.g:115:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name510);
            identifier();

            state._fsp--;


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


    // $ANTLR start "table_identifier"
    // SQL99.g:118:1: table_identifier : identifier ;
    public final void table_identifier() throws RecognitionException {
        try {
            // SQL99.g:119:3: ( identifier )
            // SQL99.g:119:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier527);
            identifier();

            state._fsp--;


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
    // SQL99.g:122:1: schema_name : identifier ;
    public final void schema_name() throws RecognitionException {
        try {
            // SQL99.g:123:3: ( identifier )
            // SQL99.g:123:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name542);
            identifier();

            state._fsp--;


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
    // SQL99.g:126:1: column_name : identifier ;
    public final void column_name() throws RecognitionException {
        try {
            // SQL99.g:127:3: ( identifier )
            // SQL99.g:127:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name559);
            identifier();

            state._fsp--;


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
    // SQL99.g:130:1: identifier : ( STRING | STRING_WITH_QUOTE );
    public final void identifier() throws RecognitionException {
        try {
            // SQL99.g:131:3: ( STRING | STRING_WITH_QUOTE )
            // SQL99.g:
            {
            if ( (input.LA(1)>=STRING && input.LA(1)<=STRING_WITH_QUOTE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
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
    // $ANTLR end "identifier"


    // $ANTLR start "value"
    // SQL99.g:135:1: value : ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE );
    public final void value() throws RecognitionException {
        try {
            // SQL99.g:136:3: ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE )
            // SQL99.g:
            {
            if ( (input.LA(1)>=STRING_WITH_QUOTE && input.LA(1)<=NUMERIC) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
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
    // SQL99.g:142:1: equals_operator : EQUALS ;
    public final void equals_operator() throws RecognitionException {
        try {
            // SQL99.g:143:3: ( EQUALS )
            // SQL99.g:143:5: EQUALS
            {
            match(input,EQUALS,FOLLOW_EQUALS_in_equals_operator626); 

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
    // SQL99.g:146:1: not_equals_operator : LESS GREATER ;
    public final void not_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:147:3: ( LESS GREATER )
            // SQL99.g:147:5: LESS GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_not_equals_operator639); 
            match(input,GREATER,FOLLOW_GREATER_in_not_equals_operator641); 

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
    // SQL99.g:150:1: less_than_operator : LESS ;
    public final void less_than_operator() throws RecognitionException {
        try {
            // SQL99.g:151:3: ( LESS )
            // SQL99.g:151:5: LESS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_operator656); 

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
    // SQL99.g:154:1: greater_than_operator : GREATER ;
    public final void greater_than_operator() throws RecognitionException {
        try {
            // SQL99.g:155:3: ( GREATER )
            // SQL99.g:155:5: GREATER
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_operator671); 

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
    // SQL99.g:158:1: less_than_or_equals_operator : LESS EQUALS ;
    public final void less_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:159:3: ( LESS EQUALS )
            // SQL99.g:159:5: LESS EQUALS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_or_equals_operator685); 
            match(input,EQUALS,FOLLOW_EQUALS_in_less_than_or_equals_operator687); 

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
    // SQL99.g:162:1: greater_than_or_equals_operator : GREATER EQUALS ;
    public final void greater_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:163:3: ( GREATER EQUALS )
            // SQL99.g:163:5: GREATER EQUALS
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_or_equals_operator701); 
            match(input,EQUALS,FOLLOW_EQUALS_in_greater_than_or_equals_operator703); 

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


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\16\uffff";
    static final String DFA12_eofS =
        "\16\uffff";
    static final String DFA12_minS =
        "\1\23\1\11\5\23\1\12\1\30\2\uffff\3\23";
    static final String DFA12_maxS =
        "\1\24\1\32\2\24\1\27\1\32\1\30\2\32\2\uffff\3\27";
    static final String DFA12_acceptS =
        "\11\uffff\1\1\1\2\3\uffff";
    static final String DFA12_specialS =
        "\16\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\1",
            "\1\2\1\3\15\uffff\1\4\1\5\1\6",
            "\2\7",
            "\2\10",
            "\1\12\4\11",
            "\1\12\4\11\1\14\1\uffff\1\13",
            "\1\12\4\11\1\15",
            "\1\3\15\uffff\1\4\1\5\1\6",
            "\1\4\1\5\1\6",
            "",
            "",
            "\1\12\4\11",
            "\1\12\4\11",
            "\1\12\4\11"
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "84:1: comparison_predicate : ( column_reference comp_op value | column_reference comp_op column_reference );";
        }
    }
 

    public static final BitSet FOLLOW_query_in_parse30 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse32 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_query47 = new BitSet(new long[]{0x00000000001800E0L});
    public static final BitSet FOLLOW_set_quantifier_in_query49 = new BitSet(new long[]{0x00000000001800E0L});
    public static final BitSet FOLLOW_select_list_in_query52 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_table_expression_in_query54 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_quantifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_select_list91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list97 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_select_list100 = new BitSet(new long[]{0x00000000001800E0L});
    public static final BitSet FOLLOW_select_sublist_in_select_list102 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_select_sublist125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk140 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_qualified_asterisk142 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference160 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_column_reference162 = new BitSet(new long[]{0x00000000001800E0L});
    public static final BitSet FOLLOW_column_name_in_column_reference166 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_AS_in_column_reference169 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_alias_name_in_column_reference171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression190 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_where_clause_in_table_expression193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause210 = new BitSet(new long[]{0x00000000001C0000L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list229 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list232 = new BitSet(new long[]{0x00000000001C0000L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list234 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_table_primary_in_table_reference251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joined_table_in_table_reference258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause271 = new BitSet(new long[]{0x00000000001800E0L});
    public static final BitSet FOLLOW_search_condition_in_where_clause273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression301 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_set_in_boolean_value_expression304 = new BitSet(new long[]{0x00000000001800E0L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression310 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_predicate_in_boolean_term325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_predicate_in_predicate346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_comparison_predicate361 = new BitSet(new long[]{0x0000000007000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate363 = new BitSet(new long[]{0x0000000000F00000L});
    public static final BitSet FOLLOW_value_in_comparison_predicate365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_comparison_predicate371 = new BitSet(new long[]{0x0000000007000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate373 = new BitSet(new long[]{0x00000000001800E0L});
    public static final BitSet FOLLOW_column_reference_in_comparison_predicate375 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equals_operator_in_comp_op388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equals_operator_in_comp_op394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_operator_in_comp_op400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_operator_in_comp_op406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_or_equals_operator_in_comp_op412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_or_equals_operator_in_comp_op418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate431 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_IS_in_null_predicate433 = new BitSet(new long[]{0x0000000000030000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate436 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JOIN_in_joined_table453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_table_primary466 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_AS_in_table_primary469 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_alias_name_in_table_primary471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name489 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_table_name491 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_table_identifier_in_table_name495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name559 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_identifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_value0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_equals_operator626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_not_equals_operator639 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_GREATER_in_not_equals_operator641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_operator656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_operator671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_or_equals_operator685 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_EQUALS_in_less_than_or_equals_operator687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_or_equals_operator701 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_EQUALS_in_greater_than_or_equals_operator703 = new BitSet(new long[]{0x0000000000000002L});

}