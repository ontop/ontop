// $ANTLR 3.3 Nov 30, 2010 12:50:56 SQL99.g 2011-06-21 10:13:25

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SELECT", "DISTINCT", "ALL", "ASTERISK", "COMMA", "DOT", "AS", "COUNT", "LPAREN", "RPAREN", "AVG", "MAX", "MIN", "SUM", "EVERY", "ANY", "SOME", "FROM", "WHERE", "OR", "AND", "IS", "NOT", "NULL", "IN", "JOIN", "STRING", "STRING_WITH_QUOTE", "TRUE", "FALSE", "NUMERIC", "EQUALS", "LESS", "GREATER", "ORDER", "BY", "ON", "LEFT", "RIGHT", "SEMI", "LSQ_BRACKET", "RSQ_BRACKET", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", "QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "DASH", "AMPERSAND", "AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "COLON", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "TILDE", "CARET", "ALPHA", "DIGIT", "ALPHANUM", "CHAR", "WS"
    };
    public static final int EOF=-1;
    public static final int SELECT=4;
    public static final int DISTINCT=5;
    public static final int ALL=6;
    public static final int ASTERISK=7;
    public static final int COMMA=8;
    public static final int DOT=9;
    public static final int AS=10;
    public static final int COUNT=11;
    public static final int LPAREN=12;
    public static final int RPAREN=13;
    public static final int AVG=14;
    public static final int MAX=15;
    public static final int MIN=16;
    public static final int SUM=17;
    public static final int EVERY=18;
    public static final int ANY=19;
    public static final int SOME=20;
    public static final int FROM=21;
    public static final int WHERE=22;
    public static final int OR=23;
    public static final int AND=24;
    public static final int IS=25;
    public static final int NOT=26;
    public static final int NULL=27;
    public static final int IN=28;
    public static final int JOIN=29;
    public static final int STRING=30;
    public static final int STRING_WITH_QUOTE=31;
    public static final int TRUE=32;
    public static final int FALSE=33;
    public static final int NUMERIC=34;
    public static final int EQUALS=35;
    public static final int LESS=36;
    public static final int GREATER=37;
    public static final int ORDER=38;
    public static final int BY=39;
    public static final int ON=40;
    public static final int LEFT=41;
    public static final int RIGHT=42;
    public static final int SEMI=43;
    public static final int LSQ_BRACKET=44;
    public static final int RSQ_BRACKET=45;
    public static final int QUESTION=46;
    public static final int DOLLAR=47;
    public static final int QUOTE_DOUBLE=48;
    public static final int QUOTE_SINGLE=49;
    public static final int APOSTROPHE=50;
    public static final int UNDERSCORE=51;
    public static final int DASH=52;
    public static final int AMPERSAND=53;
    public static final int AT=54;
    public static final int EXCLAMATION=55;
    public static final int HASH=56;
    public static final int PERCENT=57;
    public static final int PLUS=58;
    public static final int COLON=59;
    public static final int SLASH=60;
    public static final int DOUBLE_SLASH=61;
    public static final int BACKSLASH=62;
    public static final int TILDE=63;
    public static final int CARET=64;
    public static final int ALPHA=65;
    public static final int DIGIT=66;
    public static final int ALPHANUM=67;
    public static final int CHAR=68;
    public static final int WS=69;

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
            else if ( (LA3_0==COUNT||(LA3_0>=AVG && LA3_0<=SOME)||(LA3_0>=STRING && LA3_0<=STRING_WITH_QUOTE)) ) {
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
    // SQL99.g:33:1: select_sublist : ( qualified_asterisk | derived_column );
    public final void select_sublist() throws RecognitionException {
        try {
            // SQL99.g:34:3: ( qualified_asterisk | derived_column )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>=STRING && LA4_0<=STRING_WITH_QUOTE)) ) {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==DOT) ) {
                    int LA4_3 = input.LA(3);

                    if ( (LA4_3==ASTERISK) ) {
                        alt4=1;
                    }
                    else if ( ((LA4_3>=STRING && LA4_3<=STRING_WITH_QUOTE)) ) {
                        alt4=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 3, input);

                        throw nvae;
                    }
                }
                else if ( (LA4_1==COMMA||LA4_1==AS||LA4_1==FROM) ) {
                    alt4=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA4_0==COUNT||(LA4_0>=AVG && LA4_0<=SOME)) ) {
                alt4=2;
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
                    // SQL99.g:35:5: derived_column
                    {
                    pushFollow(FOLLOW_derived_column_in_select_sublist125);
                    derived_column();

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


    // $ANTLR start "derived_column"
    // SQL99.g:42:1: derived_column : value_expression ( AS alias_name )? ;
    public final void derived_column() throws RecognitionException {
        try {
            // SQL99.g:43:3: ( value_expression ( AS alias_name )? )
            // SQL99.g:43:5: value_expression ( AS alias_name )?
            {
            pushFollow(FOLLOW_value_expression_in_derived_column159);
            value_expression();

            state._fsp--;

            // SQL99.g:43:22: ( AS alias_name )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==AS) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // SQL99.g:43:23: AS alias_name
                    {
                    match(input,AS,FOLLOW_AS_in_derived_column162); 
                    pushFollow(FOLLOW_alias_name_in_derived_column164);
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
    // $ANTLR end "derived_column"


    // $ANTLR start "value_expression"
    // SQL99.g:46:1: value_expression : ( column_reference | set_function_specification );
    public final void value_expression() throws RecognitionException {
        try {
            // SQL99.g:47:3: ( column_reference | set_function_specification )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( ((LA6_0>=STRING && LA6_0<=STRING_WITH_QUOTE)) ) {
                alt6=1;
            }
            else if ( (LA6_0==COUNT||(LA6_0>=AVG && LA6_0<=SOME)) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // SQL99.g:47:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_value_expression182);
                    column_reference();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:48:5: set_function_specification
                    {
                    pushFollow(FOLLOW_set_function_specification_in_value_expression188);
                    set_function_specification();

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
    // $ANTLR end "value_expression"


    // $ANTLR start "column_reference"
    // SQL99.g:51:1: column_reference : ( table_identifier DOT )? column_name ;
    public final void column_reference() throws RecognitionException {
        try {
            // SQL99.g:52:3: ( ( table_identifier DOT )? column_name )
            // SQL99.g:52:5: ( table_identifier DOT )? column_name
            {
            // SQL99.g:52:5: ( table_identifier DOT )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=STRING && LA7_0<=STRING_WITH_QUOTE)) ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==DOT) ) {
                    alt7=1;
                }
            }
            switch (alt7) {
                case 1 :
                    // SQL99.g:52:6: table_identifier DOT
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference204);
                    table_identifier();

                    state._fsp--;

                    match(input,DOT,FOLLOW_DOT_in_column_reference206); 

                    }
                    break;

            }

            pushFollow(FOLLOW_column_name_in_column_reference210);
            column_name();

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
    // $ANTLR end "column_reference"


    // $ANTLR start "set_function_specification"
    // SQL99.g:55:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        try {
            // SQL99.g:56:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==COUNT) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==LPAREN) ) {
                    int LA8_3 = input.LA(3);

                    if ( (LA8_3==ASTERISK) ) {
                        alt8=1;
                    }
                    else if ( ((LA8_3>=DISTINCT && LA8_3<=ALL)||LA8_3==COUNT||(LA8_3>=AVG && LA8_3<=SOME)||(LA8_3>=STRING && LA8_3<=STRING_WITH_QUOTE)) ) {
                        alt8=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 3, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA8_0>=AVG && LA8_0<=SOME)) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // SQL99.g:56:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    match(input,COUNT,FOLLOW_COUNT_in_set_function_specification223); 
                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification225); 
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification227); 
                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification229); 

                    }
                    break;
                case 2 :
                    // SQL99.g:57:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification235);
                    general_set_function();

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
    // $ANTLR end "set_function_specification"


    // $ANTLR start "general_set_function"
    // SQL99.g:60:1: general_set_function : set_function_op LPAREN ( set_quantifier )? value_expression RPAREN ;
    public final void general_set_function() throws RecognitionException {
        try {
            // SQL99.g:61:3: ( set_function_op LPAREN ( set_quantifier )? value_expression RPAREN )
            // SQL99.g:61:5: set_function_op LPAREN ( set_quantifier )? value_expression RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function250);
            set_function_op();

            state._fsp--;

            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function252); 
            // SQL99.g:61:28: ( set_quantifier )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=DISTINCT && LA9_0<=ALL)) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // SQL99.g:61:29: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_general_set_function255);
                    set_quantifier();

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_value_expression_in_general_set_function259);
            value_expression();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function261); 

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
    // SQL99.g:64:1: set_function_op : ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT );
    public final void set_function_op() throws RecognitionException {
        try {
            // SQL99.g:65:3: ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT )
            // SQL99.g:
            {
            if ( input.LA(1)==COUNT||(input.LA(1)>=AVG && input.LA(1)<=SOME) ) {
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
    // $ANTLR end "set_function_op"


    // $ANTLR start "table_expression"
    // SQL99.g:75:1: table_expression : from_clause ( where_clause )? ;
    public final void table_expression() throws RecognitionException {
        try {
            // SQL99.g:76:3: ( from_clause ( where_clause )? )
            // SQL99.g:76:5: from_clause ( where_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression337);
            from_clause();

            state._fsp--;

            // SQL99.g:76:17: ( where_clause )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==WHERE) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // SQL99.g:76:18: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression340);
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
    // SQL99.g:79:1: from_clause : FROM table_reference_list ;
    public final void from_clause() throws RecognitionException {
        try {
            // SQL99.g:80:3: ( FROM table_reference_list )
            // SQL99.g:80:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause357); 
            pushFollow(FOLLOW_table_reference_list_in_from_clause359);
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
    // SQL99.g:83:1: table_reference_list : table_reference ( COMMA table_reference )* ;
    public final void table_reference_list() throws RecognitionException {
        try {
            // SQL99.g:84:3: ( table_reference ( COMMA table_reference )* )
            // SQL99.g:84:5: table_reference ( COMMA table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list376);
            table_reference();

            state._fsp--;

            // SQL99.g:84:21: ( COMMA table_reference )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==COMMA) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // SQL99.g:84:22: COMMA table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list379); 
            	    pushFollow(FOLLOW_table_reference_in_table_reference_list381);
            	    table_reference();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop11;
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
    // SQL99.g:87:1: table_reference : ( table_primary | joined_table );
    public final void table_reference() throws RecognitionException {
        try {
            // SQL99.g:88:3: ( table_primary | joined_table )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( ((LA12_0>=STRING && LA12_0<=STRING_WITH_QUOTE)) ) {
                alt12=1;
            }
            else if ( (LA12_0==JOIN) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // SQL99.g:88:5: table_primary
                    {
                    pushFollow(FOLLOW_table_primary_in_table_reference398);
                    table_primary();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:89:5: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference405);
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
    // SQL99.g:92:1: where_clause : WHERE search_condition ;
    public final void where_clause() throws RecognitionException {
        try {
            // SQL99.g:93:3: ( WHERE search_condition )
            // SQL99.g:93:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause418); 
            pushFollow(FOLLOW_search_condition_in_where_clause420);
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
    // SQL99.g:96:1: search_condition : boolean_value_expression ;
    public final void search_condition() throws RecognitionException {
        try {
            // SQL99.g:97:3: ( boolean_value_expression )
            // SQL99.g:97:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition433);
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
    // SQL99.g:100:1: boolean_value_expression : boolean_term ( ( OR | AND ) boolean_term )* ;
    public final void boolean_value_expression() throws RecognitionException {
        try {
            // SQL99.g:101:3: ( boolean_term ( ( OR | AND ) boolean_term )* )
            // SQL99.g:101:5: boolean_term ( ( OR | AND ) boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression448);
            boolean_term();

            state._fsp--;

            // SQL99.g:101:18: ( ( OR | AND ) boolean_term )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>=OR && LA13_0<=AND)) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // SQL99.g:101:19: ( OR | AND ) boolean_term
            	    {
            	    if ( (input.LA(1)>=OR && input.LA(1)<=AND) ) {
            	        input.consume();
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression457);
            	    boolean_term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop13;
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
    // SQL99.g:104:1: boolean_term : predicate ;
    public final void boolean_term() throws RecognitionException {
        try {
            // SQL99.g:105:3: ( predicate )
            // SQL99.g:105:5: predicate
            {
            pushFollow(FOLLOW_predicate_in_boolean_term472);
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
    // SQL99.g:108:1: predicate : ( comparison_predicate | null_predicate | in_predicate );
    public final void predicate() throws RecognitionException {
        try {
            // SQL99.g:109:3: ( comparison_predicate | null_predicate | in_predicate )
            int alt14=3;
            int LA14_0 = input.LA(1);

            if ( ((LA14_0>=STRING && LA14_0<=STRING_WITH_QUOTE)) ) {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA14_2 = input.LA(3);

                    if ( ((LA14_2>=STRING && LA14_2<=STRING_WITH_QUOTE)) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt14=1;
                            }
                            break;
                        case IS:
                            {
                            alt14=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt14=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 14, 6, input);

                            throw nvae;
                        }

                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 14, 2, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt14=1;
                    }
                    break;
                case IS:
                    {
                    alt14=2;
                    }
                    break;
                case NOT:
                case IN:
                    {
                    alt14=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 14, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // SQL99.g:109:5: comparison_predicate
                    {
                    pushFollow(FOLLOW_comparison_predicate_in_predicate487);
                    comparison_predicate();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:110:5: null_predicate
                    {
                    pushFollow(FOLLOW_null_predicate_in_predicate493);
                    null_predicate();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // SQL99.g:111:5: in_predicate
                    {
                    pushFollow(FOLLOW_in_predicate_in_predicate499);
                    in_predicate();

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
    // SQL99.g:114:1: comparison_predicate : ( column_reference comp_op value | column_reference comp_op column_reference );
    public final void comparison_predicate() throws RecognitionException {
        try {
            // SQL99.g:115:3: ( column_reference comp_op value | column_reference comp_op column_reference )
            int alt15=2;
            alt15 = dfa15.predict(input);
            switch (alt15) {
                case 1 :
                    // SQL99.g:115:5: column_reference comp_op value
                    {
                    pushFollow(FOLLOW_column_reference_in_comparison_predicate514);
                    column_reference();

                    state._fsp--;

                    pushFollow(FOLLOW_comp_op_in_comparison_predicate516);
                    comp_op();

                    state._fsp--;

                    pushFollow(FOLLOW_value_in_comparison_predicate518);
                    value();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:116:5: column_reference comp_op column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_comparison_predicate524);
                    column_reference();

                    state._fsp--;

                    pushFollow(FOLLOW_comp_op_in_comparison_predicate526);
                    comp_op();

                    state._fsp--;

                    pushFollow(FOLLOW_column_reference_in_comparison_predicate528);
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
    // SQL99.g:119:1: comp_op : ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator );
    public final void comp_op() throws RecognitionException {
        try {
            // SQL99.g:120:3: ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator )
            int alt16=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt16=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt16=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt16=5;
                    }
                    break;
                case STRING:
                case STRING_WITH_QUOTE:
                case TRUE:
                case FALSE:
                case NUMERIC:
                    {
                    alt16=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 2, input);

                    throw nvae;
                }

                }
                break;
            case GREATER:
                {
                int LA16_3 = input.LA(2);

                if ( (LA16_3==EQUALS) ) {
                    alt16=6;
                }
                else if ( ((LA16_3>=STRING && LA16_3<=NUMERIC)) ) {
                    alt16=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }

            switch (alt16) {
                case 1 :
                    // SQL99.g:120:5: equals_operator
                    {
                    pushFollow(FOLLOW_equals_operator_in_comp_op541);
                    equals_operator();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:121:5: not_equals_operator
                    {
                    pushFollow(FOLLOW_not_equals_operator_in_comp_op547);
                    not_equals_operator();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // SQL99.g:122:5: less_than_operator
                    {
                    pushFollow(FOLLOW_less_than_operator_in_comp_op553);
                    less_than_operator();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // SQL99.g:123:5: greater_than_operator
                    {
                    pushFollow(FOLLOW_greater_than_operator_in_comp_op559);
                    greater_than_operator();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // SQL99.g:124:5: less_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_less_than_or_equals_operator_in_comp_op565);
                    less_than_or_equals_operator();

                    state._fsp--;


                    }
                    break;
                case 6 :
                    // SQL99.g:125:5: greater_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_greater_than_or_equals_operator_in_comp_op571);
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
    // SQL99.g:128:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // SQL99.g:129:3: ( column_reference IS ( NOT )? NULL )
            // SQL99.g:129:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate584);
            column_reference();

            state._fsp--;

            match(input,IS,FOLLOW_IS_in_null_predicate586); 
            // SQL99.g:129:25: ( NOT )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==NOT) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // SQL99.g:129:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate589); 

                    }
                    break;

            }

            match(input,NULL,FOLLOW_NULL_in_null_predicate593); 

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
    // SQL99.g:132:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // SQL99.g:133:3: ( column_reference ( NOT )? IN in_predicate_value )
            // SQL99.g:133:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate606);
            column_reference();

            state._fsp--;

            // SQL99.g:133:22: ( NOT )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==NOT) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // SQL99.g:133:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate609); 

                    }
                    break;

            }

            match(input,IN,FOLLOW_IN_in_in_predicate613); 
            pushFollow(FOLLOW_in_predicate_value_in_in_predicate615);
            in_predicate_value();

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
    // $ANTLR end "in_predicate"


    // $ANTLR start "in_predicate_value"
    // SQL99.g:136:1: in_predicate_value : ( table_subquery | LPAREN value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // SQL99.g:137:3: ( table_subquery | LPAREN value_list RPAREN )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==LPAREN) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==SELECT) ) {
                    alt19=1;
                }
                else if ( ((LA19_1>=STRING_WITH_QUOTE && LA19_1<=NUMERIC)) ) {
                    alt19=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // SQL99.g:137:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value630);
                    table_subquery();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // SQL99.g:138:5: LPAREN value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value636); 
                    pushFollow(FOLLOW_value_list_in_in_predicate_value638);
                    value_list();

                    state._fsp--;

                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value640); 

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
    // SQL99.g:141:1: table_subquery : LPAREN query RPAREN ;
    public final void table_subquery() throws RecognitionException {
        try {
            // SQL99.g:142:3: ( LPAREN query RPAREN )
            // SQL99.g:142:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_table_subquery653); 
            pushFollow(FOLLOW_query_in_table_subquery655);
            query();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_table_subquery657); 

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


    // $ANTLR start "value_list"
    // SQL99.g:145:1: value_list : value ( COMMA value )* ;
    public final void value_list() throws RecognitionException {
        try {
            // SQL99.g:146:3: ( value ( COMMA value )* )
            // SQL99.g:146:5: value ( COMMA value )*
            {
            pushFollow(FOLLOW_value_in_value_list672);
            value();

            state._fsp--;

            // SQL99.g:146:11: ( COMMA value )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==COMMA) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // SQL99.g:146:12: COMMA value
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_value_list675); 
            	    pushFollow(FOLLOW_value_in_value_list677);
            	    value();

            	    state._fsp--;


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
    // $ANTLR end "value_list"


    // $ANTLR start "joined_table"
    // SQL99.g:149:1: joined_table : JOIN ;
    public final void joined_table() throws RecognitionException {
        try {
            // SQL99.g:150:3: ( JOIN )
            // SQL99.g:150:5: JOIN
            {
            match(input,JOIN,FOLLOW_JOIN_in_joined_table692); 

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
    // SQL99.g:153:1: table_primary : table_name ( AS alias_name )? ;
    public final void table_primary() throws RecognitionException {
        try {
            // SQL99.g:154:3: ( table_name ( AS alias_name )? )
            // SQL99.g:154:5: table_name ( AS alias_name )?
            {
            pushFollow(FOLLOW_table_name_in_table_primary705);
            table_name();

            state._fsp--;

            // SQL99.g:154:16: ( AS alias_name )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==AS) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // SQL99.g:154:17: AS alias_name
                    {
                    match(input,AS,FOLLOW_AS_in_table_primary708); 
                    pushFollow(FOLLOW_alias_name_in_table_primary710);
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
    // SQL99.g:157:1: table_name : ( schema_name DOT )? table_identifier ;
    public final void table_name() throws RecognitionException {
        try {
            // SQL99.g:158:3: ( ( schema_name DOT )? table_identifier )
            // SQL99.g:158:5: ( schema_name DOT )? table_identifier
            {
            // SQL99.g:158:5: ( schema_name DOT )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( ((LA22_0>=STRING && LA22_0<=STRING_WITH_QUOTE)) ) {
                int LA22_1 = input.LA(2);

                if ( (LA22_1==DOT) ) {
                    alt22=1;
                }
            }
            switch (alt22) {
                case 1 :
                    // SQL99.g:158:6: schema_name DOT
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name728);
                    schema_name();

                    state._fsp--;

                    match(input,DOT,FOLLOW_DOT_in_table_name730); 

                    }
                    break;

            }

            pushFollow(FOLLOW_table_identifier_in_table_name734);
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
    // SQL99.g:161:1: alias_name : identifier ;
    public final void alias_name() throws RecognitionException {
        try {
            // SQL99.g:162:3: ( identifier )
            // SQL99.g:162:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name749);
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
    // SQL99.g:165:1: table_identifier : identifier ;
    public final void table_identifier() throws RecognitionException {
        try {
            // SQL99.g:166:3: ( identifier )
            // SQL99.g:166:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier766);
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
    // SQL99.g:169:1: schema_name : identifier ;
    public final void schema_name() throws RecognitionException {
        try {
            // SQL99.g:170:3: ( identifier )
            // SQL99.g:170:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name781);
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
    // SQL99.g:173:1: column_name : identifier ;
    public final void column_name() throws RecognitionException {
        try {
            // SQL99.g:174:3: ( identifier )
            // SQL99.g:174:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name798);
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
    // SQL99.g:177:1: identifier : ( STRING | STRING_WITH_QUOTE );
    public final void identifier() throws RecognitionException {
        try {
            // SQL99.g:178:3: ( STRING | STRING_WITH_QUOTE )
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
    // SQL99.g:182:1: value : ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE );
    public final void value() throws RecognitionException {
        try {
            // SQL99.g:183:3: ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE )
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
    // SQL99.g:189:1: equals_operator : EQUALS ;
    public final void equals_operator() throws RecognitionException {
        try {
            // SQL99.g:190:3: ( EQUALS )
            // SQL99.g:190:5: EQUALS
            {
            match(input,EQUALS,FOLLOW_EQUALS_in_equals_operator865); 

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
    // SQL99.g:193:1: not_equals_operator : LESS GREATER ;
    public final void not_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:194:3: ( LESS GREATER )
            // SQL99.g:194:5: LESS GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_not_equals_operator878); 
            match(input,GREATER,FOLLOW_GREATER_in_not_equals_operator880); 

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
    // SQL99.g:197:1: less_than_operator : LESS ;
    public final void less_than_operator() throws RecognitionException {
        try {
            // SQL99.g:198:3: ( LESS )
            // SQL99.g:198:5: LESS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_operator895); 

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
    // SQL99.g:201:1: greater_than_operator : GREATER ;
    public final void greater_than_operator() throws RecognitionException {
        try {
            // SQL99.g:202:3: ( GREATER )
            // SQL99.g:202:5: GREATER
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_operator910); 

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
    // SQL99.g:205:1: less_than_or_equals_operator : LESS EQUALS ;
    public final void less_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:206:3: ( LESS EQUALS )
            // SQL99.g:206:5: LESS EQUALS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_or_equals_operator924); 
            match(input,EQUALS,FOLLOW_EQUALS_in_less_than_or_equals_operator926); 

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
    // SQL99.g:209:1: greater_than_or_equals_operator : GREATER EQUALS ;
    public final void greater_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:210:3: ( GREATER EQUALS )
            // SQL99.g:210:5: GREATER EQUALS
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_or_equals_operator940); 
            match(input,EQUALS,FOLLOW_EQUALS_in_greater_than_or_equals_operator942); 

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


    protected DFA15 dfa15 = new DFA15(this);
    static final String DFA15_eotS =
        "\14\uffff";
    static final String DFA15_eofS =
        "\14\uffff";
    static final String DFA15_minS =
        "\1\36\1\11\4\36\1\43\2\uffff\3\36";
    static final String DFA15_maxS =
        "\1\37\1\45\1\37\1\42\1\45\1\43\1\45\2\uffff\3\42";
    static final String DFA15_acceptS =
        "\7\uffff\1\1\1\2\3\uffff";
    static final String DFA15_specialS =
        "\14\uffff}>";
    static final String[] DFA15_transitionS = {
            "\2\1",
            "\1\2\31\uffff\1\3\1\4\1\5",
            "\2\6",
            "\1\10\4\7",
            "\1\10\4\7\1\12\1\uffff\1\11",
            "\1\10\4\7\1\13",
            "\1\3\1\4\1\5",
            "",
            "",
            "\1\10\4\7",
            "\1\10\4\7",
            "\1\10\4\7"
    };

    static final short[] DFA15_eot = DFA.unpackEncodedString(DFA15_eotS);
    static final short[] DFA15_eof = DFA.unpackEncodedString(DFA15_eofS);
    static final char[] DFA15_min = DFA.unpackEncodedStringToUnsignedChars(DFA15_minS);
    static final char[] DFA15_max = DFA.unpackEncodedStringToUnsignedChars(DFA15_maxS);
    static final short[] DFA15_accept = DFA.unpackEncodedString(DFA15_acceptS);
    static final short[] DFA15_special = DFA.unpackEncodedString(DFA15_specialS);
    static final short[][] DFA15_transition;

    static {
        int numStates = DFA15_transitionS.length;
        DFA15_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA15_transition[i] = DFA.unpackEncodedString(DFA15_transitionS[i]);
        }
    }

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = DFA15_eot;
            this.eof = DFA15_eof;
            this.min = DFA15_min;
            this.max = DFA15_max;
            this.accept = DFA15_accept;
            this.special = DFA15_special;
            this.transition = DFA15_transition;
        }
        public String getDescription() {
            return "114:1: comparison_predicate : ( column_reference comp_op value | column_reference comp_op column_reference );";
        }
    }
 

    public static final BitSet FOLLOW_query_in_parse30 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse32 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_query47 = new BitSet(new long[]{0x00000000C01FC8E0L});
    public static final BitSet FOLLOW_set_quantifier_in_query49 = new BitSet(new long[]{0x00000000C01FC8E0L});
    public static final BitSet FOLLOW_select_list_in_query52 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_table_expression_in_query54 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_quantifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_select_list91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list97 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_select_list100 = new BitSet(new long[]{0x00000000C01FC8E0L});
    public static final BitSet FOLLOW_select_sublist_in_select_list102 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk140 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_qualified_asterisk142 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column159 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_AS_in_derived_column162 = new BitSet(new long[]{0x00000000C0000000L});
    public static final BitSet FOLLOW_alias_name_in_derived_column164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_value_expression182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_value_expression188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference204 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_column_reference206 = new BitSet(new long[]{0x00000000C0000000L});
    public static final BitSet FOLLOW_column_name_in_column_reference210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification223 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification225 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification227 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function250 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function252 = new BitSet(new long[]{0x00000000C01FC8E0L});
    public static final BitSet FOLLOW_set_quantifier_in_general_set_function255 = new BitSet(new long[]{0x00000000C01FC8E0L});
    public static final BitSet FOLLOW_value_expression_in_general_set_function259 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_function_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression337 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_where_clause_in_table_expression340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause357 = new BitSet(new long[]{0x00000000E0000000L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list376 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list379 = new BitSet(new long[]{0x00000000E0000000L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list381 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_table_primary_in_table_reference398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joined_table_in_table_reference405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause418 = new BitSet(new long[]{0x00000000C0000000L});
    public static final BitSet FOLLOW_search_condition_in_where_clause420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression448 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_set_in_boolean_value_expression451 = new BitSet(new long[]{0x00000000C0000000L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression457 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_predicate_in_boolean_term472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_predicate_in_predicate493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_in_predicate_in_predicate499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_comparison_predicate514 = new BitSet(new long[]{0x0000003800000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate516 = new BitSet(new long[]{0x0000000780000000L});
    public static final BitSet FOLLOW_value_in_comparison_predicate518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_comparison_predicate524 = new BitSet(new long[]{0x0000003800000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate526 = new BitSet(new long[]{0x00000000C0000000L});
    public static final BitSet FOLLOW_column_reference_in_comparison_predicate528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equals_operator_in_comp_op541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equals_operator_in_comp_op547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_operator_in_comp_op553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_operator_in_comp_op559 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_or_equals_operator_in_comp_op565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_or_equals_operator_in_comp_op571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate584 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate586 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate589 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate606 = new BitSet(new long[]{0x0000000014000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate609 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate613 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value636 = new BitSet(new long[]{0x0000000780000000L});
    public static final BitSet FOLLOW_value_list_in_in_predicate_value638 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_table_subquery653 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_query_in_table_subquery655 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_table_subquery657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_value_list672 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_value_list675 = new BitSet(new long[]{0x0000000780000000L});
    public static final BitSet FOLLOW_value_in_value_list677 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_JOIN_in_joined_table692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_table_primary705 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_AS_in_table_primary708 = new BitSet(new long[]{0x00000000C0000000L});
    public static final BitSet FOLLOW_alias_name_in_table_primary710 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name728 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_table_name730 = new BitSet(new long[]{0x00000000C0000000L});
    public static final BitSet FOLLOW_table_identifier_in_table_name734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_identifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_value0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_equals_operator865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_not_equals_operator878 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_GREATER_in_not_equals_operator880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_operator895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_operator910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_or_equals_operator924 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_less_than_or_equals_operator926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_or_equals_operator940 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_greater_than_or_equals_operator942 = new BitSet(new long[]{0x0000000000000002L});

}