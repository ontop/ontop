// $ANTLR 3.3 Nov 30, 2010 12:50:56 SQL99.g 2011-06-22 14:35:19

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SELECT", "DISTINCT", "ALL", "ASTERISK", "COMMA", "DOT", "AS", "COUNT", "LPAREN", "RPAREN", "AVG", "MAX", "MIN", "SUM", "EVERY", "ANY", "SOME", "FROM", "WHERE", "OR", "AND", "IS", "NOT", "NULL", "IN", "GROUP", "BY", "JOIN", "INNER", "OUTER", "LEFT", "RIGHT", "FULL", "ON", "STRING", "STRING_WITH_QUOTE_DOUBLE", "TRUE", "FALSE", "NUMERIC", "STRING_WITH_QUOTE", "EQUALS", "LESS", "GREATER", "ORDER", "SEMI", "LSQ_BRACKET", "RSQ_BRACKET", "QUESTION", "DOLLAR", "QUOTE_DOUBLE", "QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "DASH", "AMPERSAND", "AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "COLON", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "TILDE", "CARET", "ALPHA", "DIGIT", "ALPHANUM", "CHAR", "WS"
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
    public static final int GROUP=29;
    public static final int BY=30;
    public static final int JOIN=31;
    public static final int INNER=32;
    public static final int OUTER=33;
    public static final int LEFT=34;
    public static final int RIGHT=35;
    public static final int FULL=36;
    public static final int ON=37;
    public static final int STRING=38;
    public static final int STRING_WITH_QUOTE_DOUBLE=39;
    public static final int TRUE=40;
    public static final int FALSE=41;
    public static final int NUMERIC=42;
    public static final int STRING_WITH_QUOTE=43;
    public static final int EQUALS=44;
    public static final int LESS=45;
    public static final int GREATER=46;
    public static final int ORDER=47;
    public static final int SEMI=48;
    public static final int LSQ_BRACKET=49;
    public static final int RSQ_BRACKET=50;
    public static final int QUESTION=51;
    public static final int DOLLAR=52;
    public static final int QUOTE_DOUBLE=53;
    public static final int QUOTE_SINGLE=54;
    public static final int APOSTROPHE=55;
    public static final int UNDERSCORE=56;
    public static final int DASH=57;
    public static final int AMPERSAND=58;
    public static final int AT=59;
    public static final int EXCLAMATION=60;
    public static final int HASH=61;
    public static final int PERCENT=62;
    public static final int PLUS=63;
    public static final int COLON=64;
    public static final int SLASH=65;
    public static final int DOUBLE_SLASH=66;
    public static final int BACKSLASH=67;
    public static final int TILDE=68;
    public static final int CARET=69;
    public static final int ALPHA=70;
    public static final int DIGIT=71;
    public static final int ALPHANUM=72;
    public static final int CHAR=73;
    public static final int WS=74;

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
    // SQL99.g:19:1: query : SELECT ( set_quantifier )? select_list table_expression ;
    public final void query() throws RecognitionException {
        try {
            // SQL99.g:20:3: ( SELECT ( set_quantifier )? select_list table_expression )
            // SQL99.g:20:5: SELECT ( set_quantifier )? select_list table_expression
            {
            match(input,SELECT,FOLLOW_SELECT_in_query47); if (state.failed) return ;
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
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_select_list_in_query52);
            select_list();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_table_expression_in_query54);
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
    // SQL99.g:28:1: select_list : ( ASTERISK | select_sublist ( COMMA select_sublist )* );
    public final void select_list() throws RecognitionException {
        try {
            // SQL99.g:29:3: ( ASTERISK | select_sublist ( COMMA select_sublist )* )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ASTERISK) ) {
                alt3=1;
            }
            else if ( (LA3_0==COUNT||(LA3_0>=AVG && LA3_0<=SOME)||(LA3_0>=STRING && LA3_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt3=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // SQL99.g:29:5: ASTERISK
                    {
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_select_list91); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:30:5: select_sublist ( COMMA select_sublist )*
                    {
                    pushFollow(FOLLOW_select_sublist_in_select_list97);
                    select_sublist();

                    state._fsp--;
                    if (state.failed) return ;
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
                    	    match(input,COMMA,FOLLOW_COMMA_in_select_list100); if (state.failed) return ;
                    	    pushFollow(FOLLOW_select_sublist_in_select_list102);
                    	    select_sublist();

                    	    state._fsp--;
                    	    if (state.failed) return ;

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
            switch ( input.LA(1) ) {
            case STRING:
                {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==DOT) ) {
                    int LA4_4 = input.LA(3);

                    if ( (LA4_4==ASTERISK) ) {
                        alt4=1;
                    }
                    else if ( ((LA4_4>=STRING && LA4_4<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt4=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 4, input);

                        throw nvae;
                    }
                }
                else if ( (LA4_1==COMMA||LA4_1==AS||LA4_1==FROM||(LA4_1>=STRING && LA4_1<=STRING_WITH_QUOTE_DOUBLE)) ) {
                    alt4=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;
                }
                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                int LA4_2 = input.LA(2);

                if ( (LA4_2==DOT) ) {
                    int LA4_4 = input.LA(3);

                    if ( (LA4_4==ASTERISK) ) {
                        alt4=1;
                    }
                    else if ( ((LA4_4>=STRING && LA4_4<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt4=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 4, input);

                        throw nvae;
                    }
                }
                else if ( (LA4_2==COMMA||LA4_2==AS||LA4_2==FROM||(LA4_2>=STRING && LA4_2<=STRING_WITH_QUOTE_DOUBLE)) ) {
                    alt4=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 2, input);

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
                alt4=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
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
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:35:5: derived_column
                    {
                    pushFollow(FOLLOW_derived_column_in_select_sublist125);
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
    // SQL99.g:38:1: qualified_asterisk : table_identifier DOT ASTERISK ;
    public final void qualified_asterisk() throws RecognitionException {
        try {
            // SQL99.g:39:3: ( table_identifier DOT ASTERISK )
            // SQL99.g:39:5: table_identifier DOT ASTERISK
            {
            pushFollow(FOLLOW_table_identifier_in_qualified_asterisk140);
            table_identifier();

            state._fsp--;
            if (state.failed) return ;
            match(input,DOT,FOLLOW_DOT_in_qualified_asterisk142); if (state.failed) return ;
            match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk144); if (state.failed) return ;

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
    // SQL99.g:42:1: derived_column : value_expression ( ( AS )? alias_name )? ;
    public final void derived_column() throws RecognitionException {
        try {
            // SQL99.g:43:3: ( value_expression ( ( AS )? alias_name )? )
            // SQL99.g:43:5: value_expression ( ( AS )? alias_name )?
            {
            pushFollow(FOLLOW_value_expression_in_derived_column159);
            value_expression();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:43:22: ( ( AS )? alias_name )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==AS||(LA6_0>=STRING && LA6_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // SQL99.g:43:23: ( AS )? alias_name
                    {
                    // SQL99.g:43:23: ( AS )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==AS) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // SQL99.g:43:23: AS
                            {
                            match(input,AS,FOLLOW_AS_in_derived_column162); if (state.failed) return ;

                            }
                            break;

                    }

                    pushFollow(FOLLOW_alias_name_in_derived_column165);
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
    // SQL99.g:46:1: value_expression : ( reference_value_expression | collection_value_expression );
    public final void value_expression() throws RecognitionException {
        try {
            // SQL99.g:47:3: ( reference_value_expression | collection_value_expression )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=STRING && LA7_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt7=1;
            }
            else if ( (LA7_0==COUNT||(LA7_0>=AVG && LA7_0<=SOME)) ) {
                alt7=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // SQL99.g:47:5: reference_value_expression
                    {
                    pushFollow(FOLLOW_reference_value_expression_in_value_expression183);
                    reference_value_expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:48:5: collection_value_expression
                    {
                    pushFollow(FOLLOW_collection_value_expression_in_value_expression189);
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
    // SQL99.g:51:1: reference_value_expression : column_reference ;
    public final void reference_value_expression() throws RecognitionException {
        try {
            // SQL99.g:52:3: ( column_reference )
            // SQL99.g:52:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_reference_value_expression204);
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
    // SQL99.g:55:1: column_reference : ( table_identifier DOT )? column_name ;
    public final void column_reference() throws RecognitionException {
        try {
            // SQL99.g:56:3: ( ( table_identifier DOT )? column_name )
            // SQL99.g:56:5: ( table_identifier DOT )? column_name
            {
            // SQL99.g:56:5: ( table_identifier DOT )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==STRING) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==DOT) ) {
                    alt8=1;
                }
            }
            else if ( (LA8_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA8_2 = input.LA(2);

                if ( (LA8_2==DOT) ) {
                    alt8=1;
                }
            }
            switch (alt8) {
                case 1 :
                    // SQL99.g:56:6: table_identifier DOT
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference218);
                    table_identifier();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_column_reference220); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_column_name_in_column_reference224);
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
    // SQL99.g:59:1: collection_value_expression : set_function_specification ;
    public final void collection_value_expression() throws RecognitionException {
        try {
            // SQL99.g:60:3: ( set_function_specification )
            // SQL99.g:60:5: set_function_specification
            {
            pushFollow(FOLLOW_set_function_specification_in_collection_value_expression241);
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
    // SQL99.g:63:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        try {
            // SQL99.g:64:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==COUNT) ) {
                int LA9_1 = input.LA(2);

                if ( (LA9_1==LPAREN) ) {
                    int LA9_3 = input.LA(3);

                    if ( (LA9_3==ASTERISK) ) {
                        alt9=1;
                    }
                    else if ( ((LA9_3>=DISTINCT && LA9_3<=ALL)||LA9_3==COUNT||(LA9_3>=AVG && LA9_3<=SOME)||(LA9_3>=STRING && LA9_3<=STRING_WITH_QUOTE_DOUBLE)) ) {
                        alt9=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 9, 3, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA9_0>=AVG && LA9_0<=SOME)) ) {
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
                    // SQL99.g:64:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    match(input,COUNT,FOLLOW_COUNT_in_set_function_specification254); if (state.failed) return ;
                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification256); if (state.failed) return ;
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification258); if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification260); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:65:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification266);
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
    // SQL99.g:68:1: general_set_function : set_function_op LPAREN ( set_quantifier )? value_expression RPAREN ;
    public final void general_set_function() throws RecognitionException {
        try {
            // SQL99.g:69:3: ( set_function_op LPAREN ( set_quantifier )? value_expression RPAREN )
            // SQL99.g:69:5: set_function_op LPAREN ( set_quantifier )? value_expression RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function281);
            set_function_op();

            state._fsp--;
            if (state.failed) return ;
            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function283); if (state.failed) return ;
            // SQL99.g:69:28: ( set_quantifier )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( ((LA10_0>=DISTINCT && LA10_0<=ALL)) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // SQL99.g:69:29: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_general_set_function286);
                    set_quantifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_value_expression_in_general_set_function290);
            value_expression();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function292); if (state.failed) return ;

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
    // SQL99.g:72:1: set_function_op : ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT );
    public final void set_function_op() throws RecognitionException {
        try {
            // SQL99.g:73:3: ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT )
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
    // SQL99.g:83:1: table_expression : from_clause ( where_clause )? ( group_by_clause )? ;
    public final void table_expression() throws RecognitionException {
        try {
            // SQL99.g:84:3: ( from_clause ( where_clause )? ( group_by_clause )? )
            // SQL99.g:84:5: from_clause ( where_clause )? ( group_by_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression368);
            from_clause();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:84:17: ( where_clause )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==WHERE) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // SQL99.g:84:18: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression371);
                    where_clause();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // SQL99.g:84:33: ( group_by_clause )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==GROUP) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // SQL99.g:84:34: group_by_clause
                    {
                    pushFollow(FOLLOW_group_by_clause_in_table_expression376);
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
    // SQL99.g:87:1: from_clause : FROM table_reference_list ;
    public final void from_clause() throws RecognitionException {
        try {
            // SQL99.g:88:3: ( FROM table_reference_list )
            // SQL99.g:88:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause393); if (state.failed) return ;
            pushFollow(FOLLOW_table_reference_list_in_from_clause395);
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
    // SQL99.g:91:1: table_reference_list : table_reference ( COMMA table_reference )* ;
    public final void table_reference_list() throws RecognitionException {
        try {
            // SQL99.g:92:3: ( table_reference ( COMMA table_reference )* )
            // SQL99.g:92:5: table_reference ( COMMA table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list412);
            table_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:92:21: ( COMMA table_reference )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==COMMA) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // SQL99.g:92:22: COMMA table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list415); if (state.failed) return ;
            	    pushFollow(FOLLOW_table_reference_in_table_reference_list417);
            	    table_reference();

            	    state._fsp--;
            	    if (state.failed) return ;

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
    // $ANTLR end "table_reference_list"


    // $ANTLR start "table_reference"
    // SQL99.g:95:1: table_reference : ( table_primary | joined_table );
    public final void table_reference() throws RecognitionException {
        try {
            // SQL99.g:96:3: ( table_primary | joined_table )
            int alt14=2;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // SQL99.g:96:5: table_primary
                    {
                    pushFollow(FOLLOW_table_primary_in_table_reference434);
                    table_primary();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:97:5: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference441);
                    joined_table();

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
    // $ANTLR end "table_reference"


    // $ANTLR start "where_clause"
    // SQL99.g:100:1: where_clause : WHERE search_condition ;
    public final void where_clause() throws RecognitionException {
        try {
            // SQL99.g:101:3: ( WHERE search_condition )
            // SQL99.g:101:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause454); if (state.failed) return ;
            pushFollow(FOLLOW_search_condition_in_where_clause456);
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
    // SQL99.g:104:1: search_condition : boolean_value_expression ;
    public final void search_condition() throws RecognitionException {
        try {
            // SQL99.g:105:3: ( boolean_value_expression )
            // SQL99.g:105:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition469);
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
    // SQL99.g:108:1: boolean_value_expression : boolean_term ( ( OR | AND ) boolean_term )* ;
    public final void boolean_value_expression() throws RecognitionException {
        try {
            // SQL99.g:109:3: ( boolean_term ( ( OR | AND ) boolean_term )* )
            // SQL99.g:109:5: boolean_term ( ( OR | AND ) boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression484);
            boolean_term();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:109:18: ( ( OR | AND ) boolean_term )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( ((LA15_0>=OR && LA15_0<=AND)) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // SQL99.g:109:19: ( OR | AND ) boolean_term
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

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression493);
            	    boolean_term();

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
    // $ANTLR end "boolean_value_expression"


    // $ANTLR start "boolean_term"
    // SQL99.g:112:1: boolean_term : predicate ;
    public final void boolean_term() throws RecognitionException {
        try {
            // SQL99.g:113:3: ( predicate )
            // SQL99.g:113:5: predicate
            {
            pushFollow(FOLLOW_predicate_in_boolean_term508);
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
    // SQL99.g:116:1: predicate : ( comparison_predicate | null_predicate | in_predicate );
    public final void predicate() throws RecognitionException {
        try {
            // SQL99.g:117:3: ( comparison_predicate | null_predicate | in_predicate )
            int alt16=3;
            switch ( input.LA(1) ) {
            case STRING:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA16_4 = input.LA(3);

                    if ( (LA16_4==STRING) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt16=1;
                            }
                            break;
                        case IS:
                            {
                            alt16=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt16=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 16, 7, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA16_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt16=1;
                            }
                            break;
                        case IS:
                            {
                            alt16=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt16=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 16, 8, input);

                            throw nvae;
                        }

                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 16, 4, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt16=1;
                    }
                    break;
                case IS:
                    {
                    alt16=2;
                    }
                    break;
                case NOT:
                case IN:
                    {
                    alt16=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 1, input);

                    throw nvae;
                }

                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA16_4 = input.LA(3);

                    if ( (LA16_4==STRING) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt16=1;
                            }
                            break;
                        case IS:
                            {
                            alt16=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt16=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 16, 7, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA16_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case LESS:
                        case GREATER:
                            {
                            alt16=1;
                            }
                            break;
                        case IS:
                            {
                            alt16=2;
                            }
                            break;
                        case NOT:
                        case IN:
                            {
                            alt16=3;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 16, 8, input);

                            throw nvae;
                        }

                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 16, 4, input);

                        throw nvae;
                    }
                    }
                    break;
                case EQUALS:
                case LESS:
                case GREATER:
                    {
                    alt16=1;
                    }
                    break;
                case IS:
                    {
                    alt16=2;
                    }
                    break;
                case NOT:
                case IN:
                    {
                    alt16=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 2, input);

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
                alt16=1;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }

            switch (alt16) {
                case 1 :
                    // SQL99.g:117:5: comparison_predicate
                    {
                    pushFollow(FOLLOW_comparison_predicate_in_predicate523);
                    comparison_predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:118:5: null_predicate
                    {
                    pushFollow(FOLLOW_null_predicate_in_predicate529);
                    null_predicate();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // SQL99.g:119:5: in_predicate
                    {
                    pushFollow(FOLLOW_in_predicate_in_predicate535);
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
    // SQL99.g:122:1: comparison_predicate options {backtrack=true; } : ( value_expression comp_op value | value_expression comp_op value_expression );
    public final void comparison_predicate() throws RecognitionException {
        try {
            // SQL99.g:126:3: ( value_expression comp_op value | value_expression comp_op value_expression )
            int alt17=2;
            switch ( input.LA(1) ) {
            case STRING:
                {
                int LA17_1 = input.LA(2);

                if ( (synpred1_SQL99()) ) {
                    alt17=1;
                }
                else if ( (true) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

                    throw nvae;
                }
                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                int LA17_2 = input.LA(2);

                if ( (synpred1_SQL99()) ) {
                    alt17=1;
                }
                else if ( (true) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 2, input);

                    throw nvae;
                }
                }
                break;
            case COUNT:
                {
                int LA17_3 = input.LA(2);

                if ( (synpred1_SQL99()) ) {
                    alt17=1;
                }
                else if ( (true) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 3, input);

                    throw nvae;
                }
                }
                break;
            case AVG:
            case MAX:
            case MIN:
            case SUM:
            case EVERY:
            case ANY:
            case SOME:
                {
                int LA17_4 = input.LA(2);

                if ( (synpred1_SQL99()) ) {
                    alt17=1;
                }
                else if ( (true) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 4, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }

            switch (alt17) {
                case 1 :
                    // SQL99.g:126:5: value_expression comp_op value
                    {
                    pushFollow(FOLLOW_value_expression_in_comparison_predicate561);
                    value_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_comp_op_in_comparison_predicate563);
                    comp_op();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_value_in_comparison_predicate565);
                    value();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:127:5: value_expression comp_op value_expression
                    {
                    pushFollow(FOLLOW_value_expression_in_comparison_predicate571);
                    value_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_comp_op_in_comparison_predicate573);
                    comp_op();

                    state._fsp--;
                    if (state.failed) return ;
                    pushFollow(FOLLOW_value_expression_in_comparison_predicate575);
                    value_expression();

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
    // $ANTLR end "comparison_predicate"


    // $ANTLR start "comp_op"
    // SQL99.g:130:1: comp_op : ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator );
    public final void comp_op() throws RecognitionException {
        try {
            // SQL99.g:131:3: ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator )
            int alt18=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt18=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt18=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt18=5;
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
            case GREATER:
                {
                int LA18_3 = input.LA(2);

                if ( (LA18_3==EQUALS) ) {
                    alt18=6;
                }
                else if ( (LA18_3==COUNT||(LA18_3>=AVG && LA18_3<=SOME)||(LA18_3>=STRING && LA18_3<=STRING_WITH_QUOTE)) ) {
                    alt18=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 3, input);

                    throw nvae;
                }
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
                    // SQL99.g:131:5: equals_operator
                    {
                    pushFollow(FOLLOW_equals_operator_in_comp_op588);
                    equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:132:5: not_equals_operator
                    {
                    pushFollow(FOLLOW_not_equals_operator_in_comp_op594);
                    not_equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // SQL99.g:133:5: less_than_operator
                    {
                    pushFollow(FOLLOW_less_than_operator_in_comp_op600);
                    less_than_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // SQL99.g:134:5: greater_than_operator
                    {
                    pushFollow(FOLLOW_greater_than_operator_in_comp_op606);
                    greater_than_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // SQL99.g:135:5: less_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_less_than_or_equals_operator_in_comp_op612);
                    less_than_or_equals_operator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // SQL99.g:136:5: greater_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_greater_than_or_equals_operator_in_comp_op618);
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
    // SQL99.g:139:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // SQL99.g:140:3: ( column_reference IS ( NOT )? NULL )
            // SQL99.g:140:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate631);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            match(input,IS,FOLLOW_IS_in_null_predicate633); if (state.failed) return ;
            // SQL99.g:140:25: ( NOT )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==NOT) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // SQL99.g:140:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate636); if (state.failed) return ;

                    }
                    break;

            }

            match(input,NULL,FOLLOW_NULL_in_null_predicate640); if (state.failed) return ;

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
    // SQL99.g:143:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // SQL99.g:144:3: ( column_reference ( NOT )? IN in_predicate_value )
            // SQL99.g:144:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate653);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:144:22: ( NOT )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==NOT) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // SQL99.g:144:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate656); if (state.failed) return ;

                    }
                    break;

            }

            match(input,IN,FOLLOW_IN_in_in_predicate660); if (state.failed) return ;
            pushFollow(FOLLOW_in_predicate_value_in_in_predicate662);
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
    // SQL99.g:147:1: in_predicate_value : ( table_subquery | LPAREN value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // SQL99.g:148:3: ( table_subquery | LPAREN value_list RPAREN )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==LPAREN) ) {
                int LA21_1 = input.LA(2);

                if ( (LA21_1==SELECT) ) {
                    alt21=1;
                }
                else if ( ((LA21_1>=TRUE && LA21_1<=STRING_WITH_QUOTE)) ) {
                    alt21=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 21, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // SQL99.g:148:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value677);
                    table_subquery();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:149:5: LPAREN value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value683); if (state.failed) return ;
                    pushFollow(FOLLOW_value_list_in_in_predicate_value685);
                    value_list();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value687); if (state.failed) return ;

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
    // SQL99.g:152:1: table_subquery : LPAREN query RPAREN ;
    public final void table_subquery() throws RecognitionException {
        try {
            // SQL99.g:153:3: ( LPAREN query RPAREN )
            // SQL99.g:153:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_table_subquery700); if (state.failed) return ;
            pushFollow(FOLLOW_query_in_table_subquery702);
            query();

            state._fsp--;
            if (state.failed) return ;
            match(input,RPAREN,FOLLOW_RPAREN_in_table_subquery704); if (state.failed) return ;

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
    // SQL99.g:156:1: value_list : value ( COMMA value )* ;
    public final void value_list() throws RecognitionException {
        try {
            // SQL99.g:157:3: ( value ( COMMA value )* )
            // SQL99.g:157:5: value ( COMMA value )*
            {
            pushFollow(FOLLOW_value_in_value_list719);
            value();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:157:11: ( COMMA value )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==COMMA) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // SQL99.g:157:12: COMMA value
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_value_list722); if (state.failed) return ;
            	    pushFollow(FOLLOW_value_in_value_list724);
            	    value();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop22;
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
    // SQL99.g:160:1: group_by_clause : GROUP BY grouping_element_list ;
    public final void group_by_clause() throws RecognitionException {
        try {
            // SQL99.g:161:3: ( GROUP BY grouping_element_list )
            // SQL99.g:161:5: GROUP BY grouping_element_list
            {
            match(input,GROUP,FOLLOW_GROUP_in_group_by_clause739); if (state.failed) return ;
            match(input,BY,FOLLOW_BY_in_group_by_clause741); if (state.failed) return ;
            pushFollow(FOLLOW_grouping_element_list_in_group_by_clause743);
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
    // SQL99.g:164:1: grouping_element_list : grouping_element ( COMMA grouping_element )* ;
    public final void grouping_element_list() throws RecognitionException {
        try {
            // SQL99.g:165:3: ( grouping_element ( COMMA grouping_element )* )
            // SQL99.g:165:5: grouping_element ( COMMA grouping_element )*
            {
            pushFollow(FOLLOW_grouping_element_in_grouping_element_list756);
            grouping_element();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:165:22: ( COMMA grouping_element )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==COMMA) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // SQL99.g:165:23: COMMA grouping_element
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list759); if (state.failed) return ;
            	    pushFollow(FOLLOW_grouping_element_in_grouping_element_list761);
            	    grouping_element();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop23;
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
    // SQL99.g:168:1: grouping_element : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
    public final void grouping_element() throws RecognitionException {
        try {
            // SQL99.g:169:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0>=STRING && LA24_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt24=1;
            }
            else if ( (LA24_0==LPAREN) ) {
                alt24=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // SQL99.g:169:5: grouping_column_reference
                    {
                    pushFollow(FOLLOW_grouping_column_reference_in_grouping_element778);
                    grouping_column_reference();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:170:5: LPAREN grouping_column_reference_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element784); if (state.failed) return ;
                    pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element786);
                    grouping_column_reference_list();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element788); if (state.failed) return ;

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
    // SQL99.g:173:1: grouping_column_reference : column_reference ;
    public final void grouping_column_reference() throws RecognitionException {
        try {
            // SQL99.g:174:3: ( column_reference )
            // SQL99.g:174:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference804);
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
    // SQL99.g:177:1: grouping_column_reference_list : column_reference ( COMMA column_reference )* ;
    public final void grouping_column_reference_list() throws RecognitionException {
        try {
            // SQL99.g:178:3: ( column_reference ( COMMA column_reference )* )
            // SQL99.g:178:5: column_reference ( COMMA column_reference )*
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list819);
            column_reference();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:178:22: ( COMMA column_reference )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==COMMA) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // SQL99.g:178:23: COMMA column_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list822); if (state.failed) return ;
            	    pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list824);
            	    column_reference();

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
    // $ANTLR end "grouping_column_reference_list"


    // $ANTLR start "joined_table"
    // SQL99.g:181:1: joined_table : qualified_join ;
    public final void joined_table() throws RecognitionException {
        try {
            // SQL99.g:182:3: ( qualified_join )
            // SQL99.g:182:5: qualified_join
            {
            pushFollow(FOLLOW_qualified_join_in_joined_table841);
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
    // SQL99.g:185:1: qualified_join : table_primary ( join_type )? JOIN table_primary join_condition ;
    public final void qualified_join() throws RecognitionException {
        try {
            // SQL99.g:186:3: ( table_primary ( join_type )? JOIN table_primary join_condition )
            // SQL99.g:186:5: table_primary ( join_type )? JOIN table_primary join_condition
            {
            pushFollow(FOLLOW_table_primary_in_qualified_join854);
            table_primary();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:186:19: ( join_type )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==INNER||(LA26_0>=LEFT && LA26_0<=FULL)) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // SQL99.g:186:20: join_type
                    {
                    pushFollow(FOLLOW_join_type_in_qualified_join857);
                    join_type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,JOIN,FOLLOW_JOIN_in_qualified_join861); if (state.failed) return ;
            pushFollow(FOLLOW_table_primary_in_qualified_join863);
            table_primary();

            state._fsp--;
            if (state.failed) return ;
            pushFollow(FOLLOW_join_condition_in_qualified_join865);
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
    // SQL99.g:189:1: join_type : ( INNER | outer_join_type ( OUTER )? );
    public final void join_type() throws RecognitionException {
        try {
            // SQL99.g:190:3: ( INNER | outer_join_type ( OUTER )? )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==INNER) ) {
                alt28=1;
            }
            else if ( ((LA28_0>=LEFT && LA28_0<=FULL)) ) {
                alt28=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // SQL99.g:190:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type878); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:191:5: outer_join_type ( OUTER )?
                    {
                    pushFollow(FOLLOW_outer_join_type_in_join_type884);
                    outer_join_type();

                    state._fsp--;
                    if (state.failed) return ;
                    // SQL99.g:191:21: ( OUTER )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==OUTER) ) {
                        alt27=1;
                    }
                    switch (alt27) {
                        case 1 :
                            // SQL99.g:191:22: OUTER
                            {
                            match(input,OUTER,FOLLOW_OUTER_in_join_type887); if (state.failed) return ;

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
    // SQL99.g:194:1: outer_join_type : ( LEFT | RIGHT | FULL );
    public final void outer_join_type() throws RecognitionException {
        try {
            // SQL99.g:195:3: ( LEFT | RIGHT | FULL )
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
    // SQL99.g:200:1: join_condition : ON search_condition ;
    public final void join_condition() throws RecognitionException {
        try {
            // SQL99.g:201:3: ( ON search_condition )
            // SQL99.g:201:5: ON search_condition
            {
            match(input,ON,FOLLOW_ON_in_join_condition931); if (state.failed) return ;
            pushFollow(FOLLOW_search_condition_in_join_condition933);
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
    // SQL99.g:204:1: table_primary : table_name ( ( AS )? alias_name )? ;
    public final void table_primary() throws RecognitionException {
        try {
            // SQL99.g:205:3: ( table_name ( ( AS )? alias_name )? )
            // SQL99.g:205:5: table_name ( ( AS )? alias_name )?
            {
            pushFollow(FOLLOW_table_name_in_table_primary946);
            table_name();

            state._fsp--;
            if (state.failed) return ;
            // SQL99.g:205:16: ( ( AS )? alias_name )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==AS||(LA30_0>=STRING && LA30_0<=STRING_WITH_QUOTE_DOUBLE)) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // SQL99.g:205:17: ( AS )? alias_name
                    {
                    // SQL99.g:205:17: ( AS )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==AS) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // SQL99.g:205:17: AS
                            {
                            match(input,AS,FOLLOW_AS_in_table_primary949); if (state.failed) return ;

                            }
                            break;

                    }

                    pushFollow(FOLLOW_alias_name_in_table_primary952);
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
    // $ANTLR end "table_primary"


    // $ANTLR start "table_name"
    // SQL99.g:208:1: table_name : ( schema_name DOT )? table_identifier ;
    public final void table_name() throws RecognitionException {
        try {
            // SQL99.g:209:3: ( ( schema_name DOT )? table_identifier )
            // SQL99.g:209:5: ( schema_name DOT )? table_identifier
            {
            // SQL99.g:209:5: ( schema_name DOT )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==STRING) ) {
                int LA31_1 = input.LA(2);

                if ( (LA31_1==DOT) ) {
                    alt31=1;
                }
            }
            else if ( (LA31_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA31_2 = input.LA(2);

                if ( (LA31_2==DOT) ) {
                    alt31=1;
                }
            }
            switch (alt31) {
                case 1 :
                    // SQL99.g:209:6: schema_name DOT
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name970);
                    schema_name();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_table_name972); if (state.failed) return ;

                    }
                    break;

            }

            pushFollow(FOLLOW_table_identifier_in_table_name976);
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
    // SQL99.g:212:1: alias_name : identifier ;
    public final void alias_name() throws RecognitionException {
        try {
            // SQL99.g:213:3: ( identifier )
            // SQL99.g:213:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name991);
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


    // $ANTLR start "table_identifier"
    // SQL99.g:216:1: table_identifier : identifier ;
    public final void table_identifier() throws RecognitionException {
        try {
            // SQL99.g:217:3: ( identifier )
            // SQL99.g:217:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier1008);
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
    // SQL99.g:220:1: schema_name : identifier ;
    public final void schema_name() throws RecognitionException {
        try {
            // SQL99.g:221:3: ( identifier )
            // SQL99.g:221:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name1023);
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
    // SQL99.g:224:1: column_name : identifier ;
    public final void column_name() throws RecognitionException {
        try {
            // SQL99.g:225:3: ( identifier )
            // SQL99.g:225:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name1040);
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
    // SQL99.g:228:1: identifier : ( regular_identifier | delimited_identifier );
    public final void identifier() throws RecognitionException {
        try {
            // SQL99.g:229:3: ( regular_identifier | delimited_identifier )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==STRING) ) {
                alt32=1;
            }
            else if ( (LA32_0==STRING_WITH_QUOTE_DOUBLE) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // SQL99.g:229:5: regular_identifier
                    {
                    pushFollow(FOLLOW_regular_identifier_in_identifier1055);
                    regular_identifier();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // SQL99.g:230:5: delimited_identifier
                    {
                    pushFollow(FOLLOW_delimited_identifier_in_identifier1062);
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
    // SQL99.g:233:1: regular_identifier : STRING ;
    public final void regular_identifier() throws RecognitionException {
        try {
            // SQL99.g:234:3: ( STRING )
            // SQL99.g:234:5: STRING
            {
            match(input,STRING,FOLLOW_STRING_in_regular_identifier1075); if (state.failed) return ;

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
    // SQL99.g:237:1: delimited_identifier : STRING_WITH_QUOTE_DOUBLE ;
    public final void delimited_identifier() throws RecognitionException {
        try {
            // SQL99.g:238:3: ( STRING_WITH_QUOTE_DOUBLE )
            // SQL99.g:238:5: STRING_WITH_QUOTE_DOUBLE
            {
            match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1088); if (state.failed) return ;

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
    // SQL99.g:241:1: value : ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE );
    public final void value() throws RecognitionException {
        try {
            // SQL99.g:242:3: ( TRUE | FALSE | NUMERIC | STRING_WITH_QUOTE )
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
    // SQL99.g:248:1: equals_operator : EQUALS ;
    public final void equals_operator() throws RecognitionException {
        try {
            // SQL99.g:249:3: ( EQUALS )
            // SQL99.g:249:5: EQUALS
            {
            match(input,EQUALS,FOLLOW_EQUALS_in_equals_operator1133); if (state.failed) return ;

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
    // SQL99.g:252:1: not_equals_operator : LESS GREATER ;
    public final void not_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:253:3: ( LESS GREATER )
            // SQL99.g:253:5: LESS GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_not_equals_operator1146); if (state.failed) return ;
            match(input,GREATER,FOLLOW_GREATER_in_not_equals_operator1148); if (state.failed) return ;

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
    // SQL99.g:256:1: less_than_operator : LESS ;
    public final void less_than_operator() throws RecognitionException {
        try {
            // SQL99.g:257:3: ( LESS )
            // SQL99.g:257:5: LESS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_operator1163); if (state.failed) return ;

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
    // SQL99.g:260:1: greater_than_operator : GREATER ;
    public final void greater_than_operator() throws RecognitionException {
        try {
            // SQL99.g:261:3: ( GREATER )
            // SQL99.g:261:5: GREATER
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_operator1178); if (state.failed) return ;

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
    // SQL99.g:264:1: less_than_or_equals_operator : LESS EQUALS ;
    public final void less_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:265:3: ( LESS EQUALS )
            // SQL99.g:265:5: LESS EQUALS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_or_equals_operator1192); if (state.failed) return ;
            match(input,EQUALS,FOLLOW_EQUALS_in_less_than_or_equals_operator1194); if (state.failed) return ;

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
    // SQL99.g:268:1: greater_than_or_equals_operator : GREATER EQUALS ;
    public final void greater_than_or_equals_operator() throws RecognitionException {
        try {
            // SQL99.g:269:3: ( GREATER EQUALS )
            // SQL99.g:269:5: GREATER EQUALS
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_or_equals_operator1208); if (state.failed) return ;
            match(input,EQUALS,FOLLOW_EQUALS_in_greater_than_or_equals_operator1210); if (state.failed) return ;

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

    // $ANTLR start synpred1_SQL99
    public final void synpred1_SQL99_fragment() throws RecognitionException {   
        // SQL99.g:126:5: ( value_expression comp_op value )
        // SQL99.g:126:5: value_expression comp_op value
        {
        pushFollow(FOLLOW_value_expression_in_synpred1_SQL99561);
        value_expression();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_comp_op_in_synpred1_SQL99563);
        comp_op();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_value_in_synpred1_SQL99565);
        value();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_SQL99

    // Delegated rules

    public final boolean synpred1_SQL99() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_SQL99_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA14 dfa14 = new DFA14(this);
    static final String DFA14_eotS =
        "\13\uffff";
    static final String DFA14_eofS =
        "\1\uffff\2\7\2\uffff\2\7\2\uffff\2\7";
    static final String DFA14_minS =
        "\1\46\2\10\2\46\2\10\2\uffff\2\10";
    static final String DFA14_maxS =
        "\5\47\2\44\2\uffff\2\47";
    static final String DFA14_acceptS =
        "\7\uffff\1\1\1\2\2\uffff";
    static final String DFA14_specialS =
        "\13\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\1\1\2",
            "\1\7\1\3\1\4\2\uffff\1\7\10\uffff\1\7\6\uffff\1\7\1\uffff\2"+
            "\10\1\uffff\3\10\1\uffff\1\5\1\6",
            "\1\7\1\3\1\4\2\uffff\1\7\10\uffff\1\7\6\uffff\1\7\1\uffff\2"+
            "\10\1\uffff\3\10\1\uffff\1\5\1\6",
            "\1\11\1\12",
            "\1\5\1\6",
            "\1\7\4\uffff\1\7\10\uffff\1\7\6\uffff\1\7\1\uffff\2\10\1\uffff"+
            "\3\10",
            "\1\7\4\uffff\1\7\10\uffff\1\7\6\uffff\1\7\1\uffff\2\10\1\uffff"+
            "\3\10",
            "",
            "",
            "\1\7\1\uffff\1\4\2\uffff\1\7\10\uffff\1\7\6\uffff\1\7\1\uffff"+
            "\2\10\1\uffff\3\10\1\uffff\1\5\1\6",
            "\1\7\1\uffff\1\4\2\uffff\1\7\10\uffff\1\7\6\uffff\1\7\1\uffff"+
            "\2\10\1\uffff\3\10\1\uffff\1\5\1\6"
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "95:1: table_reference : ( table_primary | joined_table );";
        }
    }
 

    public static final BitSet FOLLOW_query_in_parse30 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse32 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_query47 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_set_quantifier_in_query49 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_select_list_in_query52 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_table_expression_in_query54 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_quantifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_select_list91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list97 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_select_list100 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_select_sublist_in_select_list102 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk140 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_qualified_asterisk142 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column159 = new BitSet(new long[]{0x000000C000000402L});
    public static final BitSet FOLLOW_AS_in_derived_column162 = new BitSet(new long[]{0x000000C000000400L});
    public static final BitSet FOLLOW_alias_name_in_derived_column165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reference_value_expression_in_value_expression183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_value_expression_in_value_expression189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_reference_value_expression204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference218 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_column_reference220 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_column_name_in_column_reference224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification254 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification256 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification258 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function281 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function283 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_set_quantifier_in_general_set_function286 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_value_expression_in_general_set_function290 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_set_function_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression368 = new BitSet(new long[]{0x0000000020400002L});
    public static final BitSet FOLLOW_where_clause_in_table_expression371 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_group_by_clause_in_table_expression376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause393 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list412 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list415 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list417 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_table_primary_in_table_reference434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joined_table_in_table_reference441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause454 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_search_condition_in_where_clause456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression484 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_set_in_boolean_value_expression487 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression493 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_predicate_in_boolean_term508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_predicate_in_predicate529 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_in_predicate_in_predicate535 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate561 = new BitSet(new long[]{0x0000700000000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate563 = new BitSet(new long[]{0x00000F0000000000L});
    public static final BitSet FOLLOW_value_in_comparison_predicate565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate571 = new BitSet(new long[]{0x0000700000000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate573 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equals_operator_in_comp_op588 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equals_operator_in_comp_op594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_operator_in_comp_op600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_operator_in_comp_op606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_or_equals_operator_in_comp_op612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_or_equals_operator_in_comp_op618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate631 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate633 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate636 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate653 = new BitSet(new long[]{0x0000000014000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate656 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate660 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value683 = new BitSet(new long[]{0x00000F0000000000L});
    public static final BitSet FOLLOW_value_list_in_in_predicate_value685 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_table_subquery700 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_query_in_table_subquery702 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_table_subquery704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_value_list719 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_value_list722 = new BitSet(new long[]{0x00000F0000000000L});
    public static final BitSet FOLLOW_value_in_value_list724 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_GROUP_in_group_by_clause739 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_BY_in_group_by_clause741 = new BitSet(new long[]{0x000000C000001000L});
    public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list756 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_grouping_element_list759 = new BitSet(new long[]{0x000000C000001000L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list761 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_grouping_element784 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element786 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_grouping_element788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list819 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list822 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list824 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_qualified_join_in_joined_table841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_primary_in_qualified_join854 = new BitSet(new long[]{0x0000001D80000000L});
    public static final BitSet FOLLOW_join_type_in_qualified_join857 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_JOIN_in_qualified_join861 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_table_primary_in_qualified_join863 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_join_condition_in_qualified_join865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_join_type878 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outer_join_type_in_join_type884 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_OUTER_in_join_type887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_outer_join_type0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_condition931 = new BitSet(new long[]{0x000000C0001FC8E0L});
    public static final BitSet FOLLOW_search_condition_in_join_condition933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_table_primary946 = new BitSet(new long[]{0x000000C000000402L});
    public static final BitSet FOLLOW_AS_in_table_primary949 = new BitSet(new long[]{0x000000C000000400L});
    public static final BitSet FOLLOW_alias_name_in_table_primary952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name970 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_DOT_in_table_name972 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_table_identifier_in_table_name976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier1008 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name1023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name1040 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regular_identifier_in_identifier1055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delimited_identifier_in_identifier1062 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_regular_identifier1075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_value0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_equals_operator1133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_not_equals_operator1146 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_GREATER_in_not_equals_operator1148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_operator1163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_operator1178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_or_equals_operator1192 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_EQUALS_in_less_than_or_equals_operator1194 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_or_equals_operator1208 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_EQUALS_in_greater_than_or_equals_operator1210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_synpred1_SQL99561 = new BitSet(new long[]{0x0000700000000000L});
    public static final BitSet FOLLOW_comp_op_in_synpred1_SQL99563 = new BitSet(new long[]{0x00000F0000000000L});
    public static final BitSet FOLLOW_value_in_synpred1_SQL99565 = new BitSet(new long[]{0x0000000000000002L});

}