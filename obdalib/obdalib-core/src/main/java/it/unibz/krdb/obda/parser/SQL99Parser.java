// $ANTLR 3.4 C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g 2011-08-31 10:45:35

package it.unibz.krdb.obda.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALL", "ALPHA", "ALPHANUM", "AMPERSAND", "AND", "ANY", "APOSTROPHE", "AS", "ASTERISK", "AT", "AVG", "BACKSLASH", "BY", "CARET", "CHAR", "COLON", "COMMA", "CONCATENATION", "COUNT", "DASH", "DIGIT", "DISTINCT", "DOLLAR", "DOT", "DOUBLE_SLASH", "ECHAR", "EQUALS", "EVERY", "EXCLAMATION", "FALSE", "FROM", "FULL", "GREATER", "GROUP", "HASH", "IN", "INNER", "INTEGER", "IS", "JOIN", "LEFT", "LESS", "LPAREN", "LSQ_BRACKET", "MAX", "MIN", "NOT", "NULL", "ON", "OR", "ORDER", "OUTER", "PERCENT", "PLUS", "QUESTION", "QUOTE_DOUBLE", "QUOTE_SINGLE", "RIGHT", "RPAREN", "RSQ_BRACKET", "SELECT", "SEMI", "SLASH", "SOME", "STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "SUM", "TILDE", "TRUE", "UNDERSCORE", "UNION", "USING", "VARNAME", "WHERE", "WS"
    };

    public static final int EOF=-1;
    public static final int ALL=4;
    public static final int ALPHA=5;
    public static final int ALPHANUM=6;
    public static final int AMPERSAND=7;
    public static final int AND=8;
    public static final int ANY=9;
    public static final int APOSTROPHE=10;
    public static final int AS=11;
    public static final int ASTERISK=12;
    public static final int AT=13;
    public static final int AVG=14;
    public static final int BACKSLASH=15;
    public static final int BY=16;
    public static final int CARET=17;
    public static final int CHAR=18;
    public static final int COLON=19;
    public static final int COMMA=20;
    public static final int CONCATENATION=21;
    public static final int COUNT=22;
    public static final int DASH=23;
    public static final int DIGIT=24;
    public static final int DISTINCT=25;
    public static final int DOLLAR=26;
    public static final int DOT=27;
    public static final int DOUBLE_SLASH=28;
    public static final int ECHAR=29;
    public static final int EQUALS=30;
    public static final int EVERY=31;
    public static final int EXCLAMATION=32;
    public static final int FALSE=33;
    public static final int FROM=34;
    public static final int FULL=35;
    public static final int GREATER=36;
    public static final int GROUP=37;
    public static final int HASH=38;
    public static final int IN=39;
    public static final int INNER=40;
    public static final int INTEGER=41;
    public static final int IS=42;
    public static final int JOIN=43;
    public static final int LEFT=44;
    public static final int LESS=45;
    public static final int LPAREN=46;
    public static final int LSQ_BRACKET=47;
    public static final int MAX=48;
    public static final int MIN=49;
    public static final int NOT=50;
    public static final int NULL=51;
    public static final int ON=52;
    public static final int OR=53;
    public static final int ORDER=54;
    public static final int OUTER=55;
    public static final int PERCENT=56;
    public static final int PLUS=57;
    public static final int QUESTION=58;
    public static final int QUOTE_DOUBLE=59;
    public static final int QUOTE_SINGLE=60;
    public static final int RIGHT=61;
    public static final int RPAREN=62;
    public static final int RSQ_BRACKET=63;
    public static final int SELECT=64;
    public static final int SEMI=65;
    public static final int SLASH=66;
    public static final int SOME=67;
    public static final int STRING_WITH_QUOTE=68;
    public static final int STRING_WITH_QUOTE_DOUBLE=69;
    public static final int SUM=70;
    public static final int TILDE=71;
    public static final int TRUE=72;
    public static final int UNDERSCORE=73;
    public static final int UNION=74;
    public static final int USING=75;
    public static final int VARNAME=76;
    public static final int WHERE=77;
    public static final int WS=78;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public SQL99Parser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public SQL99Parser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return SQL99Parser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g"; }



    // $ANTLR start "parse"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:15:1: parse : query EOF ;
    public final void parse() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:16:3: ( query EOF )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:16:5: query EOF
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "parse"



    // $ANTLR start "query"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:19:1: query : query_specification ( UNION ( set_quantifier )? query_specification )* ;
    public final void query() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:20:3: ( query_specification ( UNION ( set_quantifier )? query_specification )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:20:5: query_specification ( UNION ( set_quantifier )? query_specification )*
            {
            pushFollow(FOLLOW_query_specification_in_query47);
            query_specification();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:20:25: ( UNION ( set_quantifier )? query_specification )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==UNION) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:20:26: UNION ( set_quantifier )? query_specification
            	    {
            	    match(input,UNION,FOLLOW_UNION_in_query50); 

            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:20:32: ( set_quantifier )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0==ALL||LA1_0==DISTINCT) ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:20:33: set_quantifier
            	            {
            	            pushFollow(FOLLOW_set_quantifier_in_query53);
            	            set_quantifier();

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_query_specification_in_query57);
            	    query_specification();

            	    state._fsp--;


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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "query"



    // $ANTLR start "query_specification"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:23:1: query_specification : select_clause table_expression ;
    public final void query_specification() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:24:3: ( select_clause table_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:24:5: select_clause table_expression
            {
            pushFollow(FOLLOW_select_clause_in_query_specification74);
            select_clause();

            state._fsp--;


            pushFollow(FOLLOW_table_expression_in_query_specification76);
            table_expression();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "query_specification"



    // $ANTLR start "select_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:27:1: select_clause : SELECT ( set_quantifier )? select_list ;
    public final void select_clause() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:28:3: ( SELECT ( set_quantifier )? select_list )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:28:5: SELECT ( set_quantifier )? select_list
            {
            match(input,SELECT,FOLLOW_SELECT_in_select_clause89); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:28:12: ( set_quantifier )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ALL||LA3_0==DISTINCT) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:28:12: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_select_clause91);
                    set_quantifier();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_select_list_in_select_clause94);
            select_list();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "select_clause"



    // $ANTLR start "set_quantifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:31:1: set_quantifier : ( DISTINCT | ALL );
    public final void set_quantifier() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:32:3: ( DISTINCT | ALL )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:
            {
            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "set_quantifier"



    // $ANTLR start "select_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:36:1: select_list : ( ASTERISK | select_sublist ( COMMA select_sublist )* );
    public final void select_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:37:3: ( ASTERISK | select_sublist ( COMMA select_sublist )* )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==ASTERISK) ) {
                alt5=1;
            }
            else if ( (LA5_0==ANY||LA5_0==AVG||LA5_0==COUNT||LA5_0==EVERY||LA5_0==LPAREN||(LA5_0 >= MAX && LA5_0 <= MIN)||LA5_0==SOME||(LA5_0 >= STRING_WITH_QUOTE_DOUBLE && LA5_0 <= SUM)||LA5_0==VARNAME) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:37:5: ASTERISK
                    {
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_select_list130); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:38:5: select_sublist ( COMMA select_sublist )*
                    {
                    pushFollow(FOLLOW_select_sublist_in_select_list136);
                    select_sublist();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:38:20: ( COMMA select_sublist )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==COMMA) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:38:21: COMMA select_sublist
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_select_list139); 

                    	    pushFollow(FOLLOW_select_sublist_in_select_list141);
                    	    select_sublist();

                    	    state._fsp--;


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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "select_list"



    // $ANTLR start "select_sublist"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:41:1: select_sublist : ( qualified_asterisk | derived_column );
    public final void select_sublist() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:42:3: ( qualified_asterisk | derived_column )
            int alt6=2;
            switch ( input.LA(1) ) {
            case VARNAME:
                {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==DOT) ) {
                    int LA6_4 = input.LA(3);

                    if ( (LA6_4==ASTERISK) ) {
                        alt6=1;
                    }
                    else if ( (LA6_4==STRING_WITH_QUOTE_DOUBLE||LA6_4==VARNAME) ) {
                        alt6=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 4, input);

                        throw nvae;

                    }
                }
                else if ( (LA6_1==AS||LA6_1==COMMA||LA6_1==FROM||LA6_1==STRING_WITH_QUOTE_DOUBLE||LA6_1==VARNAME) ) {
                    alt6=2;
                }
                else {
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
                    else if ( (LA6_4==STRING_WITH_QUOTE_DOUBLE||LA6_4==VARNAME) ) {
                        alt6=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 4, input);

                        throw nvae;

                    }
                }
                else if ( (LA6_2==AS||LA6_2==COMMA||LA6_2==FROM||LA6_2==STRING_WITH_QUOTE_DOUBLE||LA6_2==VARNAME) ) {
                    alt6=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 2, input);

                    throw nvae;

                }
                }
                break;
            case ANY:
            case AVG:
            case COUNT:
            case EVERY:
            case LPAREN:
            case MAX:
            case MIN:
            case SOME:
            case SUM:
                {
                alt6=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }

            switch (alt6) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:42:5: qualified_asterisk
                    {
                    pushFollow(FOLLOW_qualified_asterisk_in_select_sublist158);
                    qualified_asterisk();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:43:5: derived_column
                    {
                    pushFollow(FOLLOW_derived_column_in_select_sublist164);
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "select_sublist"



    // $ANTLR start "qualified_asterisk"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:46:1: qualified_asterisk : table_identifier DOT ASTERISK ;
    public final void qualified_asterisk() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:47:3: ( table_identifier DOT ASTERISK )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:47:5: table_identifier DOT ASTERISK
            {
            pushFollow(FOLLOW_table_identifier_in_qualified_asterisk179);
            table_identifier();

            state._fsp--;


            match(input,DOT,FOLLOW_DOT_in_qualified_asterisk181); 

            match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk183); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "qualified_asterisk"



    // $ANTLR start "derived_column"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:50:1: derived_column : value_expression ( ( AS )? alias_name )? ;
    public final void derived_column() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:51:3: ( value_expression ( ( AS )? alias_name )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:51:5: value_expression ( ( AS )? alias_name )?
            {
            pushFollow(FOLLOW_value_expression_in_derived_column198);
            value_expression();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:51:22: ( ( AS )? alias_name )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==AS||LA8_0==STRING_WITH_QUOTE_DOUBLE||LA8_0==VARNAME) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:51:23: ( AS )? alias_name
                    {
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:51:23: ( AS )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==AS) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:51:23: AS
                            {
                            match(input,AS,FOLLOW_AS_in_derived_column201); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_derived_column204);
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "derived_column"



    // $ANTLR start "value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:54:1: value_expression : ( string_value_expression | reference_value_expression | collection_value_expression );
    public final void value_expression() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:55:3: ( string_value_expression | reference_value_expression | collection_value_expression )
            int alt9=3;
            switch ( input.LA(1) ) {
            case LPAREN:
                {
                alt9=1;
                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
            case VARNAME:
                {
                alt9=2;
                }
                break;
            case ANY:
            case AVG:
            case COUNT:
            case EVERY:
            case MAX:
            case MIN:
            case SOME:
            case SUM:
                {
                alt9=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:55:5: string_value_expression
                    {
                    pushFollow(FOLLOW_string_value_expression_in_value_expression222);
                    string_value_expression();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:56:5: reference_value_expression
                    {
                    pushFollow(FOLLOW_reference_value_expression_in_value_expression228);
                    reference_value_expression();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:57:5: collection_value_expression
                    {
                    pushFollow(FOLLOW_collection_value_expression_in_value_expression234);
                    collection_value_expression();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "value_expression"



    // $ANTLR start "string_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:60:1: string_value_expression : LPAREN concatenation RPAREN ;
    public final void string_value_expression() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:61:3: ( LPAREN concatenation RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:61:5: LPAREN concatenation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_string_value_expression247); 

            pushFollow(FOLLOW_concatenation_in_string_value_expression249);
            concatenation();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_string_value_expression251); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "string_value_expression"



    // $ANTLR start "concatenation"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:64:1: concatenation : concatenation_value ( concatenation_operator concatenation_value )+ ;
    public final void concatenation() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:65:3: ( concatenation_value ( concatenation_operator concatenation_value )+ )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:65:5: concatenation_value ( concatenation_operator concatenation_value )+
            {
            pushFollow(FOLLOW_concatenation_value_in_concatenation266);
            concatenation_value();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:65:25: ( concatenation_operator concatenation_value )+
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
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:65:26: concatenation_operator concatenation_value
            	    {
            	    pushFollow(FOLLOW_concatenation_operator_in_concatenation269);
            	    concatenation_operator();

            	    state._fsp--;


            	    pushFollow(FOLLOW_concatenation_value_in_concatenation271);
            	    concatenation_value();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "concatenation"



    // $ANTLR start "concatenation_value"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:68:1: concatenation_value : ( column_reference | value );
    public final void concatenation_value() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:69:3: ( column_reference | value )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==STRING_WITH_QUOTE_DOUBLE||LA11_0==VARNAME) ) {
                alt11=1;
            }
            else if ( (LA11_0==FALSE||LA11_0==INTEGER||LA11_0==STRING_WITH_QUOTE||LA11_0==TRUE) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }
            switch (alt11) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:69:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_concatenation_value286);
                    column_reference();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:70:5: value
                    {
                    pushFollow(FOLLOW_value_in_concatenation_value292);
                    value();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "concatenation_value"



    // $ANTLR start "reference_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:73:1: reference_value_expression : column_reference ;
    public final void reference_value_expression() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:74:3: ( column_reference )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:74:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_reference_value_expression305);
            column_reference();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "reference_value_expression"



    // $ANTLR start "column_reference"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:77:1: column_reference : ( table_identifier DOT )? column_name ;
    public final void column_reference() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:78:3: ( ( table_identifier DOT )? column_name )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:78:5: ( table_identifier DOT )? column_name
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:78:5: ( table_identifier DOT )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==VARNAME) ) {
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
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:78:6: table_identifier DOT
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference319);
                    table_identifier();

                    state._fsp--;


                    match(input,DOT,FOLLOW_DOT_in_column_reference321); 

                    }
                    break;

            }


            pushFollow(FOLLOW_column_name_in_column_reference325);
            column_name();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "column_reference"



    // $ANTLR start "collection_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:81:1: collection_value_expression : set_function_specification ;
    public final void collection_value_expression() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:82:3: ( set_function_specification )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:82:5: set_function_specification
            {
            pushFollow(FOLLOW_set_function_specification_in_collection_value_expression342);
            set_function_specification();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "collection_value_expression"



    // $ANTLR start "set_function_specification"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:85:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:86:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==COUNT) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==LPAREN) ) {
                    int LA13_3 = input.LA(3);

                    if ( (LA13_3==ASTERISK) ) {
                        alt13=1;
                    }
                    else if ( (LA13_3==ALL||LA13_3==ANY||LA13_3==AVG||LA13_3==COUNT||LA13_3==DISTINCT||LA13_3==EVERY||LA13_3==LPAREN||(LA13_3 >= MAX && LA13_3 <= MIN)||LA13_3==SOME||(LA13_3 >= STRING_WITH_QUOTE_DOUBLE && LA13_3 <= SUM)||LA13_3==VARNAME) ) {
                        alt13=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 13, 3, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA13_0==ANY||LA13_0==AVG||LA13_0==EVERY||(LA13_0 >= MAX && LA13_0 <= MIN)||LA13_0==SOME||LA13_0==SUM) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }
            switch (alt13) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:86:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    match(input,COUNT,FOLLOW_COUNT_in_set_function_specification355); 

                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification357); 

                    match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification359); 

                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification361); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:87:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification367);
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "set_function_specification"



    // $ANTLR start "general_set_function"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:90:1: general_set_function : set_function_op LPAREN ( set_quantifier )? value_expression RPAREN ;
    public final void general_set_function() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:91:3: ( set_function_op LPAREN ( set_quantifier )? value_expression RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:91:5: set_function_op LPAREN ( set_quantifier )? value_expression RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function382);
            set_function_op();

            state._fsp--;


            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function384); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:91:28: ( set_quantifier )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==ALL||LA14_0==DISTINCT) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:91:29: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_general_set_function387);
                    set_quantifier();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_value_expression_in_general_set_function391);
            value_expression();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function393); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "general_set_function"



    // $ANTLR start "set_function_op"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:94:1: set_function_op : ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT );
    public final void set_function_op() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:95:3: ( AVG | MAX | MIN | SUM | EVERY | ANY | SOME | COUNT )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:
            {
            if ( input.LA(1)==ANY||input.LA(1)==AVG||input.LA(1)==COUNT||input.LA(1)==EVERY||(input.LA(1) >= MAX && input.LA(1) <= MIN)||input.LA(1)==SOME||input.LA(1)==SUM ) {
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "set_function_op"



    // $ANTLR start "table_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:105:1: table_expression : from_clause ( where_clause )? ( group_by_clause )? ;
    public final void table_expression() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:106:3: ( from_clause ( where_clause )? ( group_by_clause )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:106:5: from_clause ( where_clause )? ( group_by_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression469);
            from_clause();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:106:17: ( where_clause )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==WHERE) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:106:18: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression472);
                    where_clause();

                    state._fsp--;


                    }
                    break;

            }


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:106:33: ( group_by_clause )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==GROUP) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:106:34: group_by_clause
                    {
                    pushFollow(FOLLOW_group_by_clause_in_table_expression477);
                    group_by_clause();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_expression"



    // $ANTLR start "from_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:109:1: from_clause : FROM table_reference_list ;
    public final void from_clause() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:110:3: ( FROM table_reference_list )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:110:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause494); 

            pushFollow(FOLLOW_table_reference_list_in_from_clause496);
            table_reference_list();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "from_clause"



    // $ANTLR start "table_reference_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:113:1: table_reference_list : table_reference ( COMMA table_reference )* ;
    public final void table_reference_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:114:3: ( table_reference ( COMMA table_reference )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:114:5: table_reference ( COMMA table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list513);
            table_reference();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:114:21: ( COMMA table_reference )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==COMMA) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:114:22: COMMA table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list516); 

            	    pushFollow(FOLLOW_table_reference_in_table_reference_list518);
            	    table_reference();

            	    state._fsp--;


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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_reference_list"



    // $ANTLR start "table_reference"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:117:1: table_reference : table_primary ( joined_table )? ;
    public final void table_reference() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:118:3: ( table_primary ( joined_table )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:118:5: table_primary ( joined_table )?
            {
            pushFollow(FOLLOW_table_primary_in_table_reference535);
            table_primary();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:118:19: ( joined_table )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==FULL||LA18_0==INNER||(LA18_0 >= JOIN && LA18_0 <= LEFT)||LA18_0==RIGHT) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:118:20: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference538);
                    joined_table();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_reference"



    // $ANTLR start "where_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:121:1: where_clause : WHERE search_condition ;
    public final void where_clause() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:122:3: ( WHERE search_condition )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:122:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause553); 

            pushFollow(FOLLOW_search_condition_in_where_clause555);
            search_condition();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "where_clause"



    // $ANTLR start "search_condition"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:125:1: search_condition : boolean_value_expression ;
    public final void search_condition() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:126:3: ( boolean_value_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:126:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition568);
            boolean_value_expression();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "search_condition"



    // $ANTLR start "boolean_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:129:1: boolean_value_expression : boolean_term ( OR boolean_term )* ;
    public final void boolean_value_expression() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:130:3: ( boolean_term ( OR boolean_term )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:130:5: boolean_term ( OR boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression583);
            boolean_term();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:130:18: ( OR boolean_term )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==OR) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:130:19: OR boolean_term
            	    {
            	    match(input,OR,FOLLOW_OR_in_boolean_value_expression586); 

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression588);
            	    boolean_term();

            	    state._fsp--;


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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_value_expression"



    // $ANTLR start "boolean_term"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:133:1: boolean_term : boolean_factor ( AND boolean_factor )* ;
    public final void boolean_term() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:134:3: ( boolean_factor ( AND boolean_factor )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:134:5: boolean_factor ( AND boolean_factor )*
            {
            pushFollow(FOLLOW_boolean_factor_in_boolean_term605);
            boolean_factor();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:134:20: ( AND boolean_factor )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==AND) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:134:21: AND boolean_factor
            	    {
            	    match(input,AND,FOLLOW_AND_in_boolean_term608); 

            	    pushFollow(FOLLOW_boolean_factor_in_boolean_term610);
            	    boolean_factor();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_term"



    // $ANTLR start "boolean_factor"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:137:1: boolean_factor : ( NOT )? boolean_test ;
    public final void boolean_factor() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:138:3: ( ( NOT )? boolean_test )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:138:5: ( NOT )? boolean_test
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:138:5: ( NOT )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==NOT) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:138:6: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_boolean_factor628); 

                    }
                    break;

            }


            pushFollow(FOLLOW_boolean_test_in_boolean_factor632);
            boolean_test();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_factor"



    // $ANTLR start "boolean_test"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:141:1: boolean_test : boolean_primary ( IS ( NOT )? truth_value )? ;
    public final void boolean_test() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:142:3: ( boolean_primary ( IS ( NOT )? truth_value )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:142:5: boolean_primary ( IS ( NOT )? truth_value )?
            {
            pushFollow(FOLLOW_boolean_primary_in_boolean_test645);
            boolean_primary();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:142:21: ( IS ( NOT )? truth_value )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==IS) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:142:22: IS ( NOT )? truth_value
                    {
                    match(input,IS,FOLLOW_IS_in_boolean_test648); 

                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:142:25: ( NOT )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==NOT) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:142:26: NOT
                            {
                            match(input,NOT,FOLLOW_NOT_in_boolean_test651); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_truth_value_in_boolean_test655);
                    truth_value();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_test"



    // $ANTLR start "boolean_primary"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:145:1: boolean_primary : ( predicate | parenthesized_boolean_value_expression );
    public final void boolean_primary() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:146:3: ( predicate | parenthesized_boolean_value_expression )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==LPAREN) ) {
                switch ( input.LA(2) ) {
                case VARNAME:
                    {
                    switch ( input.LA(3) ) {
                    case DOT:
                        {
                        int LA24_6 = input.LA(4);

                        if ( (LA24_6==VARNAME) ) {
                            int LA24_7 = input.LA(5);

                            if ( (LA24_7==CONCATENATION) ) {
                                alt24=1;
                            }
                            else if ( (LA24_7==EQUALS||LA24_7==GREATER||LA24_7==IN||LA24_7==IS||LA24_7==LESS||LA24_7==NOT) ) {
                                alt24=2;
                            }
                            else {
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
                            else if ( (LA24_8==EQUALS||LA24_8==GREATER||LA24_8==IN||LA24_8==IS||LA24_8==LESS||LA24_8==NOT) ) {
                                alt24=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 24, 8, input);

                                throw nvae;

                            }
                        }
                        else {
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
                    case EQUALS:
                    case GREATER:
                    case IN:
                    case IS:
                    case LESS:
                    case NOT:
                        {
                        alt24=2;
                        }
                        break;
                    default:
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

                        if ( (LA24_6==VARNAME) ) {
                            int LA24_7 = input.LA(5);

                            if ( (LA24_7==CONCATENATION) ) {
                                alt24=1;
                            }
                            else if ( (LA24_7==EQUALS||LA24_7==GREATER||LA24_7==IN||LA24_7==IS||LA24_7==LESS||LA24_7==NOT) ) {
                                alt24=2;
                            }
                            else {
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
                            else if ( (LA24_8==EQUALS||LA24_8==GREATER||LA24_8==IN||LA24_8==IS||LA24_8==LESS||LA24_8==NOT) ) {
                                alt24=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 24, 8, input);

                                throw nvae;

                            }
                        }
                        else {
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
                    case EQUALS:
                    case GREATER:
                    case IN:
                    case IS:
                    case LESS:
                    case NOT:
                        {
                        alt24=2;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 24, 4, input);

                        throw nvae;

                    }

                    }
                    break;
                case FALSE:
                case INTEGER:
                case STRING_WITH_QUOTE:
                case TRUE:
                    {
                    alt24=1;
                    }
                    break;
                case ANY:
                case AVG:
                case COUNT:
                case EVERY:
                case LPAREN:
                case MAX:
                case MIN:
                case NOT:
                case SOME:
                case SUM:
                    {
                    alt24=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 24, 1, input);

                    throw nvae;

                }

            }
            else if ( (LA24_0==ANY||LA24_0==AVG||LA24_0==COUNT||LA24_0==EVERY||(LA24_0 >= MAX && LA24_0 <= MIN)||LA24_0==SOME||(LA24_0 >= STRING_WITH_QUOTE_DOUBLE && LA24_0 <= SUM)||LA24_0==VARNAME) ) {
                alt24=1;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;

            }
            switch (alt24) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:146:5: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_boolean_primary670);
                    predicate();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:147:5: parenthesized_boolean_value_expression
                    {
                    pushFollow(FOLLOW_parenthesized_boolean_value_expression_in_boolean_primary676);
                    parenthesized_boolean_value_expression();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_primary"



    // $ANTLR start "parenthesized_boolean_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:150:1: parenthesized_boolean_value_expression : LPAREN boolean_value_expression RPAREN ;
    public final void parenthesized_boolean_value_expression() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:151:3: ( LPAREN boolean_value_expression RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:151:5: LPAREN boolean_value_expression RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_parenthesized_boolean_value_expression689); 

            pushFollow(FOLLOW_boolean_value_expression_in_parenthesized_boolean_value_expression691);
            boolean_value_expression();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_parenthesized_boolean_value_expression693); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "parenthesized_boolean_value_expression"



    // $ANTLR start "predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:154:1: predicate : ( comparison_predicate | null_predicate | in_predicate );
    public final void predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:155:3: ( comparison_predicate | null_predicate | in_predicate )
            int alt25=3;
            switch ( input.LA(1) ) {
            case ANY:
            case AVG:
            case COUNT:
            case EVERY:
            case LPAREN:
            case MAX:
            case MIN:
            case SOME:
            case SUM:
                {
                alt25=1;
                }
                break;
            case VARNAME:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    int LA25_4 = input.LA(3);

                    if ( (LA25_4==VARNAME) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 7, input);

                            throw nvae;

                        }

                    }
                    else if ( (LA25_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 8, input);

                            throw nvae;

                        }

                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 25, 4, input);

                        throw nvae;

                    }
                    }
                    break;
                case EQUALS:
                case GREATER:
                case LESS:
                    {
                    alt25=1;
                    }
                    break;
                case IS:
                    {
                    alt25=2;
                    }
                    break;
                case IN:
                case NOT:
                    {
                    alt25=3;
                    }
                    break;
                default:
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

                    if ( (LA25_4==VARNAME) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 7, input);

                            throw nvae;

                        }

                    }
                    else if ( (LA25_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt25=1;
                            }
                            break;
                        case IS:
                            {
                            alt25=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt25=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 25, 8, input);

                            throw nvae;

                        }

                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 25, 4, input);

                        throw nvae;

                    }
                    }
                    break;
                case EQUALS:
                case GREATER:
                case LESS:
                    {
                    alt25=1;
                    }
                    break;
                case IS:
                    {
                    alt25=2;
                    }
                    break;
                case IN:
                case NOT:
                    {
                    alt25=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 3, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;

            }

            switch (alt25) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:155:5: comparison_predicate
                    {
                    pushFollow(FOLLOW_comparison_predicate_in_predicate707);
                    comparison_predicate();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:156:5: null_predicate
                    {
                    pushFollow(FOLLOW_null_predicate_in_predicate713);
                    null_predicate();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:157:5: in_predicate
                    {
                    pushFollow(FOLLOW_in_predicate_in_predicate719);
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "predicate"



    // $ANTLR start "comparison_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:160:1: comparison_predicate : value_expression comp_op ( value | value_expression ) ;
    public final void comparison_predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:161:3: ( value_expression comp_op ( value | value_expression ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:161:5: value_expression comp_op ( value | value_expression )
            {
            pushFollow(FOLLOW_value_expression_in_comparison_predicate734);
            value_expression();

            state._fsp--;


            pushFollow(FOLLOW_comp_op_in_comparison_predicate736);
            comp_op();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:161:30: ( value | value_expression )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==FALSE||LA26_0==INTEGER||LA26_0==STRING_WITH_QUOTE||LA26_0==TRUE) ) {
                alt26=1;
            }
            else if ( (LA26_0==ANY||LA26_0==AVG||LA26_0==COUNT||LA26_0==EVERY||LA26_0==LPAREN||(LA26_0 >= MAX && LA26_0 <= MIN)||LA26_0==SOME||(LA26_0 >= STRING_WITH_QUOTE_DOUBLE && LA26_0 <= SUM)||LA26_0==VARNAME) ) {
                alt26=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;

            }
            switch (alt26) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:161:31: value
                    {
                    pushFollow(FOLLOW_value_in_comparison_predicate739);
                    value();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:161:37: value_expression
                    {
                    pushFollow(FOLLOW_value_expression_in_comparison_predicate741);
                    value_expression();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "comparison_predicate"



    // $ANTLR start "comp_op"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:164:1: comp_op : ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator );
    public final void comp_op() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:165:3: ( equals_operator | not_equals_operator | less_than_operator | greater_than_operator | less_than_or_equals_operator | greater_than_or_equals_operator )
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
                case ANY:
                case AVG:
                case COUNT:
                case EVERY:
                case FALSE:
                case INTEGER:
                case LPAREN:
                case MAX:
                case MIN:
                case SOME:
                case STRING_WITH_QUOTE:
                case STRING_WITH_QUOTE_DOUBLE:
                case SUM:
                case TRUE:
                case VARNAME:
                    {
                    alt27=3;
                    }
                    break;
                default:
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
                else if ( (LA27_3==ANY||LA27_3==AVG||LA27_3==COUNT||LA27_3==EVERY||LA27_3==FALSE||LA27_3==INTEGER||LA27_3==LPAREN||(LA27_3 >= MAX && LA27_3 <= MIN)||(LA27_3 >= SOME && LA27_3 <= SUM)||LA27_3==TRUE||LA27_3==VARNAME) ) {
                    alt27=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 27, 3, input);

                    throw nvae;

                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;

            }

            switch (alt27) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:165:5: equals_operator
                    {
                    pushFollow(FOLLOW_equals_operator_in_comp_op755);
                    equals_operator();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:166:5: not_equals_operator
                    {
                    pushFollow(FOLLOW_not_equals_operator_in_comp_op761);
                    not_equals_operator();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:167:5: less_than_operator
                    {
                    pushFollow(FOLLOW_less_than_operator_in_comp_op767);
                    less_than_operator();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:168:5: greater_than_operator
                    {
                    pushFollow(FOLLOW_greater_than_operator_in_comp_op773);
                    greater_than_operator();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:169:5: less_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_less_than_or_equals_operator_in_comp_op779);
                    less_than_or_equals_operator();

                    state._fsp--;


                    }
                    break;
                case 6 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:170:5: greater_than_or_equals_operator
                    {
                    pushFollow(FOLLOW_greater_than_or_equals_operator_in_comp_op785);
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "comp_op"



    // $ANTLR start "null_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:173:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:174:3: ( column_reference IS ( NOT )? NULL )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:174:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate798);
            column_reference();

            state._fsp--;


            match(input,IS,FOLLOW_IS_in_null_predicate800); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:174:25: ( NOT )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==NOT) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:174:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate803); 

                    }
                    break;

            }


            match(input,NULL,FOLLOW_NULL_in_null_predicate807); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "null_predicate"



    // $ANTLR start "in_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:177:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:178:3: ( column_reference ( NOT )? IN in_predicate_value )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:178:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate820);
            column_reference();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:178:22: ( NOT )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==NOT) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:178:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate823); 

                    }
                    break;

            }


            match(input,IN,FOLLOW_IN_in_in_predicate827); 

            pushFollow(FOLLOW_in_predicate_value_in_in_predicate829);
            in_predicate_value();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "in_predicate"



    // $ANTLR start "in_predicate_value"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:181:1: in_predicate_value : ( table_subquery | LPAREN value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:182:3: ( table_subquery | LPAREN value_list RPAREN )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==LPAREN) ) {
                int LA30_1 = input.LA(2);

                if ( (LA30_1==SELECT) ) {
                    alt30=1;
                }
                else if ( (LA30_1==FALSE||LA30_1==INTEGER||LA30_1==STRING_WITH_QUOTE||LA30_1==TRUE) ) {
                    alt30=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;

            }
            switch (alt30) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:182:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value844);
                    table_subquery();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:183:5: LPAREN value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value850); 

                    pushFollow(FOLLOW_value_list_in_in_predicate_value852);
                    value_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value854); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "in_predicate_value"



    // $ANTLR start "table_subquery"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:186:1: table_subquery : subquery ;
    public final void table_subquery() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:187:3: ( subquery )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:187:5: subquery
            {
            pushFollow(FOLLOW_subquery_in_table_subquery867);
            subquery();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_subquery"



    // $ANTLR start "subquery"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:190:1: subquery : LPAREN query RPAREN ;
    public final void subquery() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:191:3: ( LPAREN query RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:191:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_subquery880); 

            pushFollow(FOLLOW_query_in_subquery882);
            query();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_subquery884); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "subquery"



    // $ANTLR start "value_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:194:1: value_list : value ( COMMA value )* ;
    public final void value_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:195:3: ( value ( COMMA value )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:195:5: value ( COMMA value )*
            {
            pushFollow(FOLLOW_value_in_value_list899);
            value();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:195:11: ( COMMA value )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==COMMA) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:195:12: COMMA value
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_value_list902); 

            	    pushFollow(FOLLOW_value_in_value_list904);
            	    value();

            	    state._fsp--;


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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "value_list"



    // $ANTLR start "group_by_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:198:1: group_by_clause : GROUP BY grouping_element_list ;
    public final void group_by_clause() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:199:3: ( GROUP BY grouping_element_list )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:199:5: GROUP BY grouping_element_list
            {
            match(input,GROUP,FOLLOW_GROUP_in_group_by_clause919); 

            match(input,BY,FOLLOW_BY_in_group_by_clause921); 

            pushFollow(FOLLOW_grouping_element_list_in_group_by_clause923);
            grouping_element_list();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "group_by_clause"



    // $ANTLR start "grouping_element_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:202:1: grouping_element_list : grouping_element ( COMMA grouping_element )* ;
    public final void grouping_element_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:203:3: ( grouping_element ( COMMA grouping_element )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:203:5: grouping_element ( COMMA grouping_element )*
            {
            pushFollow(FOLLOW_grouping_element_in_grouping_element_list936);
            grouping_element();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:203:22: ( COMMA grouping_element )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==COMMA) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:203:23: COMMA grouping_element
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list939); 

            	    pushFollow(FOLLOW_grouping_element_in_grouping_element_list941);
            	    grouping_element();

            	    state._fsp--;


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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "grouping_element_list"



    // $ANTLR start "grouping_element"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:206:1: grouping_element : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
    public final void grouping_element() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:207:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==STRING_WITH_QUOTE_DOUBLE||LA33_0==VARNAME) ) {
                alt33=1;
            }
            else if ( (LA33_0==LPAREN) ) {
                alt33=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;

            }
            switch (alt33) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:207:5: grouping_column_reference
                    {
                    pushFollow(FOLLOW_grouping_column_reference_in_grouping_element958);
                    grouping_column_reference();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:208:5: LPAREN grouping_column_reference_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element964); 

                    pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element966);
                    grouping_column_reference_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element968); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "grouping_element"



    // $ANTLR start "grouping_column_reference"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:211:1: grouping_column_reference : column_reference ;
    public final void grouping_column_reference() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:212:3: ( column_reference )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:212:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference984);
            column_reference();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "grouping_column_reference"



    // $ANTLR start "grouping_column_reference_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:215:1: grouping_column_reference_list : column_reference ( COMMA column_reference )* ;
    public final void grouping_column_reference_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:216:3: ( column_reference ( COMMA column_reference )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:216:5: column_reference ( COMMA column_reference )*
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list999);
            column_reference();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:216:22: ( COMMA column_reference )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==COMMA) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:216:23: COMMA column_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list1002); 

            	    pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1004);
            	    column_reference();

            	    state._fsp--;


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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "grouping_column_reference_list"



    // $ANTLR start "joined_table"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:219:1: joined_table : ( ( join_type )? JOIN table_reference join_specification )+ ;
    public final void joined_table() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:220:3: ( ( ( join_type )? JOIN table_reference join_specification )+ )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:220:5: ( ( join_type )? JOIN table_reference join_specification )+
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:220:5: ( ( join_type )? JOIN table_reference join_specification )+
            int cnt36=0;
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==FULL||LA36_0==INNER||(LA36_0 >= JOIN && LA36_0 <= LEFT)||LA36_0==RIGHT) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:220:6: ( join_type )? JOIN table_reference join_specification
            	    {
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:220:6: ( join_type )?
            	    int alt35=2;
            	    int LA35_0 = input.LA(1);

            	    if ( (LA35_0==FULL||LA35_0==INNER||LA35_0==LEFT||LA35_0==RIGHT) ) {
            	        alt35=1;
            	    }
            	    switch (alt35) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:220:7: join_type
            	            {
            	            pushFollow(FOLLOW_join_type_in_joined_table1023);
            	            join_type();

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    match(input,JOIN,FOLLOW_JOIN_in_joined_table1027); 

            	    pushFollow(FOLLOW_table_reference_in_joined_table1029);
            	    table_reference();

            	    state._fsp--;


            	    pushFollow(FOLLOW_join_specification_in_joined_table1031);
            	    join_specification();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt36 >= 1 ) break loop36;
                        EarlyExitException eee =
                            new EarlyExitException(36, input);
                        throw eee;
                }
                cnt36++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "joined_table"



    // $ANTLR start "join_type"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:223:1: join_type : ( INNER | outer_join_type ( OUTER )? );
    public final void join_type() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:224:3: ( INNER | outer_join_type ( OUTER )? )
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==INNER) ) {
                alt38=1;
            }
            else if ( (LA38_0==FULL||LA38_0==LEFT||LA38_0==RIGHT) ) {
                alt38=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;

            }
            switch (alt38) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:224:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type1046); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:225:5: outer_join_type ( OUTER )?
                    {
                    pushFollow(FOLLOW_outer_join_type_in_join_type1052);
                    outer_join_type();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:225:21: ( OUTER )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==OUTER) ) {
                        alt37=1;
                    }
                    switch (alt37) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:225:22: OUTER
                            {
                            match(input,OUTER,FOLLOW_OUTER_in_join_type1055); 

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "join_type"



    // $ANTLR start "outer_join_type"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:228:1: outer_join_type : ( LEFT | RIGHT | FULL );
    public final void outer_join_type() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:229:3: ( LEFT | RIGHT | FULL )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:
            {
            if ( input.LA(1)==FULL||input.LA(1)==LEFT||input.LA(1)==RIGHT ) {
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "outer_join_type"



    // $ANTLR start "join_specification"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:234:1: join_specification : ( join_condition | named_columns_join );
    public final void join_specification() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:235:3: ( join_condition | named_columns_join )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==ON) ) {
                alt39=1;
            }
            else if ( (LA39_0==USING) ) {
                alt39=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;

            }
            switch (alt39) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:235:5: join_condition
                    {
                    pushFollow(FOLLOW_join_condition_in_join_specification1099);
                    join_condition();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:236:5: named_columns_join
                    {
                    pushFollow(FOLLOW_named_columns_join_in_join_specification1105);
                    named_columns_join();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "join_specification"



    // $ANTLR start "join_condition"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:239:1: join_condition : ON search_condition ;
    public final void join_condition() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:240:3: ( ON search_condition )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:240:5: ON search_condition
            {
            match(input,ON,FOLLOW_ON_in_join_condition1118); 

            pushFollow(FOLLOW_search_condition_in_join_condition1120);
            search_condition();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "join_condition"



    // $ANTLR start "named_columns_join"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:243:1: named_columns_join : USING LPAREN join_column_list RPAREN ;
    public final void named_columns_join() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:244:3: ( USING LPAREN join_column_list RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:244:5: USING LPAREN join_column_list RPAREN
            {
            match(input,USING,FOLLOW_USING_in_named_columns_join1133); 

            match(input,LPAREN,FOLLOW_LPAREN_in_named_columns_join1135); 

            pushFollow(FOLLOW_join_column_list_in_named_columns_join1137);
            join_column_list();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_named_columns_join1139); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "named_columns_join"



    // $ANTLR start "join_column_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:247:1: join_column_list : column_name ( COMMA column_name )* ;
    public final void join_column_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:248:3: ( column_name ( COMMA column_name )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:248:5: column_name ( COMMA column_name )*
            {
            pushFollow(FOLLOW_column_name_in_join_column_list1152);
            column_name();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:248:17: ( COMMA column_name )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==COMMA) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:248:18: COMMA column_name
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_join_column_list1155); 

            	    pushFollow(FOLLOW_column_name_in_join_column_list1157);
            	    column_name();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "join_column_list"



    // $ANTLR start "table_primary"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:251:1: table_primary : ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name );
    public final void table_primary() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:252:3: ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==STRING_WITH_QUOTE_DOUBLE||LA44_0==VARNAME) ) {
                alt44=1;
            }
            else if ( (LA44_0==LPAREN) ) {
                alt44=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;

            }
            switch (alt44) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:252:5: table_name ( ( AS )? alias_name )?
                    {
                    pushFollow(FOLLOW_table_name_in_table_primary1172);
                    table_name();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:252:16: ( ( AS )? alias_name )?
                    int alt42=2;
                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==AS||LA42_0==STRING_WITH_QUOTE_DOUBLE||LA42_0==VARNAME) ) {
                        alt42=1;
                    }
                    switch (alt42) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:252:17: ( AS )? alias_name
                            {
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:252:17: ( AS )?
                            int alt41=2;
                            int LA41_0 = input.LA(1);

                            if ( (LA41_0==AS) ) {
                                alt41=1;
                            }
                            switch (alt41) {
                                case 1 :
                                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:252:17: AS
                                    {
                                    match(input,AS,FOLLOW_AS_in_table_primary1175); 

                                    }
                                    break;

                            }


                            pushFollow(FOLLOW_alias_name_in_table_primary1178);
                            alias_name();

                            state._fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:253:5: derived_table ( AS )? alias_name
                    {
                    pushFollow(FOLLOW_derived_table_in_table_primary1186);
                    derived_table();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:253:19: ( AS )?
                    int alt43=2;
                    int LA43_0 = input.LA(1);

                    if ( (LA43_0==AS) ) {
                        alt43=1;
                    }
                    switch (alt43) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:253:19: AS
                            {
                            match(input,AS,FOLLOW_AS_in_table_primary1188); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_table_primary1191);
                    alias_name();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_primary"



    // $ANTLR start "table_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:256:1: table_name : ( schema_name DOT )? table_identifier ;
    public final void table_name() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:257:3: ( ( schema_name DOT )? table_identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:257:5: ( schema_name DOT )? table_identifier
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:257:5: ( schema_name DOT )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==VARNAME) ) {
                int LA45_1 = input.LA(2);

                if ( (LA45_1==DOT) ) {
                    alt45=1;
                }
            }
            else if ( (LA45_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA45_2 = input.LA(2);

                if ( (LA45_2==DOT) ) {
                    alt45=1;
                }
            }
            switch (alt45) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:257:6: schema_name DOT
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name1207);
                    schema_name();

                    state._fsp--;


                    match(input,DOT,FOLLOW_DOT_in_table_name1209); 

                    }
                    break;

            }


            pushFollow(FOLLOW_table_identifier_in_table_name1213);
            table_identifier();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_name"



    // $ANTLR start "alias_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:260:1: alias_name : identifier ;
    public final void alias_name() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:261:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:261:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name1228);
            identifier();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "alias_name"



    // $ANTLR start "derived_table"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:264:1: derived_table : table_subquery ;
    public final void derived_table() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:265:3: ( table_subquery )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:265:5: table_subquery
            {
            pushFollow(FOLLOW_table_subquery_in_derived_table1241);
            table_subquery();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "derived_table"



    // $ANTLR start "table_identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:268:1: table_identifier : identifier ;
    public final void table_identifier() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:269:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:269:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier1258);
            identifier();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_identifier"



    // $ANTLR start "schema_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:272:1: schema_name : identifier ;
    public final void schema_name() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:273:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:273:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name1273);
            identifier();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "schema_name"



    // $ANTLR start "column_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:276:1: column_name : identifier ;
    public final void column_name() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:277:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:277:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name1290);
            identifier();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "column_name"



    // $ANTLR start "identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:280:1: identifier : ( regular_identifier | delimited_identifier );
    public final void identifier() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:281:3: ( regular_identifier | delimited_identifier )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==VARNAME) ) {
                alt46=1;
            }
            else if ( (LA46_0==STRING_WITH_QUOTE_DOUBLE) ) {
                alt46=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;

            }
            switch (alt46) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:281:5: regular_identifier
                    {
                    pushFollow(FOLLOW_regular_identifier_in_identifier1305);
                    regular_identifier();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:282:5: delimited_identifier
                    {
                    pushFollow(FOLLOW_delimited_identifier_in_identifier1312);
                    delimited_identifier();

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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "identifier"



    // $ANTLR start "regular_identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:285:1: regular_identifier : VARNAME ;
    public final void regular_identifier() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:286:3: ( VARNAME )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:286:5: VARNAME
            {
            match(input,VARNAME,FOLLOW_VARNAME_in_regular_identifier1325); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "regular_identifier"



    // $ANTLR start "delimited_identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:289:1: delimited_identifier : STRING_WITH_QUOTE_DOUBLE ;
    public final void delimited_identifier() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:290:3: ( STRING_WITH_QUOTE_DOUBLE )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:290:5: STRING_WITH_QUOTE_DOUBLE
            {
            match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1338); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "delimited_identifier"



    // $ANTLR start "value"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:293:1: value : ( TRUE | FALSE | INTEGER | STRING_WITH_QUOTE );
    public final void value() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:294:3: ( TRUE | FALSE | INTEGER | STRING_WITH_QUOTE )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:
            {
            if ( input.LA(1)==FALSE||input.LA(1)==INTEGER||input.LA(1)==STRING_WITH_QUOTE||input.LA(1)==TRUE ) {
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "value"



    // $ANTLR start "truth_value"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:300:1: truth_value : ( TRUE | FALSE );
    public final void truth_value() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:301:3: ( TRUE | FALSE )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:
            {
            if ( input.LA(1)==FALSE||input.LA(1)==TRUE ) {
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "truth_value"



    // $ANTLR start "concatenation_operator"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:305:1: concatenation_operator : CONCATENATION ;
    public final void concatenation_operator() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:306:3: ( CONCATENATION )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:306:5: CONCATENATION
            {
            match(input,CONCATENATION,FOLLOW_CONCATENATION_in_concatenation_operator1402); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "concatenation_operator"



    // $ANTLR start "equals_operator"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:309:1: equals_operator : EQUALS ;
    public final void equals_operator() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:310:3: ( EQUALS )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:310:5: EQUALS
            {
            match(input,EQUALS,FOLLOW_EQUALS_in_equals_operator1415); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "equals_operator"



    // $ANTLR start "not_equals_operator"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:313:1: not_equals_operator : LESS GREATER ;
    public final void not_equals_operator() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:314:3: ( LESS GREATER )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:314:5: LESS GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_not_equals_operator1428); 

            match(input,GREATER,FOLLOW_GREATER_in_not_equals_operator1430); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "not_equals_operator"



    // $ANTLR start "less_than_operator"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:317:1: less_than_operator : LESS ;
    public final void less_than_operator() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:318:3: ( LESS )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:318:5: LESS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_operator1445); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "less_than_operator"



    // $ANTLR start "greater_than_operator"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:321:1: greater_than_operator : GREATER ;
    public final void greater_than_operator() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:322:3: ( GREATER )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:322:5: GREATER
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_operator1460); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "greater_than_operator"



    // $ANTLR start "less_than_or_equals_operator"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:325:1: less_than_or_equals_operator : LESS EQUALS ;
    public final void less_than_or_equals_operator() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:326:3: ( LESS EQUALS )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:326:5: LESS EQUALS
            {
            match(input,LESS,FOLLOW_LESS_in_less_than_or_equals_operator1474); 

            match(input,EQUALS,FOLLOW_EQUALS_in_less_than_or_equals_operator1476); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "less_than_or_equals_operator"



    // $ANTLR start "greater_than_or_equals_operator"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:329:1: greater_than_or_equals_operator : GREATER EQUALS ;
    public final void greater_than_or_equals_operator() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:330:3: ( GREATER EQUALS )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:330:5: GREATER EQUALS
            {
            match(input,GREATER,FOLLOW_GREATER_in_greater_than_or_equals_operator1490); 

            match(input,EQUALS,FOLLOW_EQUALS_in_greater_than_or_equals_operator1492); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "greater_than_or_equals_operator"

    // Delegated rules


 

    public static final BitSet FOLLOW_query_in_parse30 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse32 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_query_specification_in_query47 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_UNION_in_query50 = new BitSet(new long[]{0x0000000002000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_set_quantifier_in_query53 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_query_specification_in_query57 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_select_clause_in_query_specification74 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_table_expression_in_query_specification76 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_select_clause89 = new BitSet(new long[]{0x0003400082405210L,0x0000000000001068L});
    public static final BitSet FOLLOW_set_quantifier_in_select_clause91 = new BitSet(new long[]{0x0003400080405200L,0x0000000000001068L});
    public static final BitSet FOLLOW_select_list_in_select_clause94 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_select_list130 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list136 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_select_list139 = new BitSet(new long[]{0x0003400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_select_sublist_in_select_list141 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist158 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk179 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_DOT_in_qualified_asterisk181 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column198 = new BitSet(new long[]{0x0000000000000802L,0x0000000000001020L});
    public static final BitSet FOLLOW_AS_in_derived_column201 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_alias_name_in_derived_column204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_value_expression_in_value_expression222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reference_value_expression_in_value_expression228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_value_expression_in_value_expression234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_string_value_expression247 = new BitSet(new long[]{0x0000020200000000L,0x0000000000001130L});
    public static final BitSet FOLLOW_concatenation_in_string_value_expression249 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_string_value_expression251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_concatenation_value_in_concatenation266 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_concatenation_operator_in_concatenation269 = new BitSet(new long[]{0x0000020200000000L,0x0000000000001130L});
    public static final BitSet FOLLOW_concatenation_value_in_concatenation271 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_column_reference_in_concatenation_value286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_concatenation_value292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_reference_value_expression305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference319 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_DOT_in_column_reference321 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_column_name_in_column_reference325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification355 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification357 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification359 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function382 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function384 = new BitSet(new long[]{0x0003400082404210L,0x0000000000001068L});
    public static final BitSet FOLLOW_set_quantifier_in_general_set_function387 = new BitSet(new long[]{0x0003400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_value_expression_in_general_set_function391 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function393 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression469 = new BitSet(new long[]{0x0000002000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_where_clause_in_table_expression472 = new BitSet(new long[]{0x0000002000000002L});
    public static final BitSet FOLLOW_group_by_clause_in_table_expression477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause494 = new BitSet(new long[]{0x0000400000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list513 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list516 = new BitSet(new long[]{0x0000400000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list518 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_primary_in_table_reference535 = new BitSet(new long[]{0x2000190800000002L});
    public static final BitSet FOLLOW_joined_table_in_table_reference538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause553 = new BitSet(new long[]{0x0007400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_search_condition_in_where_clause555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression583 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_OR_in_boolean_value_expression586 = new BitSet(new long[]{0x0007400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression588 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term605 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_AND_in_boolean_term608 = new BitSet(new long[]{0x0007400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term610 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_NOT_in_boolean_factor628 = new BitSet(new long[]{0x0003400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_boolean_test_in_boolean_factor632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_primary_in_boolean_test645 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_IS_in_boolean_test648 = new BitSet(new long[]{0x0004000200000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_NOT_in_boolean_test651 = new BitSet(new long[]{0x0000000200000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_truth_value_in_boolean_test655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_boolean_primary670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parenthesized_boolean_value_expression_in_boolean_primary676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_parenthesized_boolean_value_expression689 = new BitSet(new long[]{0x0007400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_boolean_value_expression_in_parenthesized_boolean_value_expression691 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_parenthesized_boolean_value_expression693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_predicate_in_predicate713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_in_predicate_in_predicate719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate734 = new BitSet(new long[]{0x0000201040000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate736 = new BitSet(new long[]{0x0003420280404200L,0x0000000000001178L});
    public static final BitSet FOLLOW_value_in_comparison_predicate739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_comparison_predicate741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equals_operator_in_comp_op755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equals_operator_in_comp_op761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_operator_in_comp_op767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_operator_in_comp_op773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_than_or_equals_operator_in_comp_op779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_than_or_equals_operator_in_comp_op785 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate798 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate800 = new BitSet(new long[]{0x000C000000000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate803 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate807 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate820 = new BitSet(new long[]{0x0004008000000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate823 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate827 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate829 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value850 = new BitSet(new long[]{0x0000020200000000L,0x0000000000000110L});
    public static final BitSet FOLLOW_value_list_in_in_predicate_value852 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value854 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_table_subquery867 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_subquery880 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_query_in_subquery882 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_subquery884 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_value_list899 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_value_list902 = new BitSet(new long[]{0x0000020200000000L,0x0000000000000110L});
    public static final BitSet FOLLOW_value_in_value_list904 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_GROUP_in_group_by_clause919 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_BY_in_group_by_clause921 = new BitSet(new long[]{0x0000400000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list936 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_element_list939 = new BitSet(new long[]{0x0000400000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list941 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_grouping_element964 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element966 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_grouping_element968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list999 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list1002 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1004 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_join_type_in_joined_table1023 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1027 = new BitSet(new long[]{0x0000400000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_table_reference_in_joined_table1029 = new BitSet(new long[]{0x0010000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_join_specification_in_joined_table1031 = new BitSet(new long[]{0x2000190800000002L});
    public static final BitSet FOLLOW_INNER_in_join_type1046 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outer_join_type_in_join_type1052 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_OUTER_in_join_type1055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_condition_in_join_specification1099 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_named_columns_join_in_join_specification1105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_condition1118 = new BitSet(new long[]{0x0007400080404200L,0x0000000000001068L});
    public static final BitSet FOLLOW_search_condition_in_join_condition1120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USING_in_named_columns_join1133 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LPAREN_in_named_columns_join1135 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_join_column_list_in_named_columns_join1137 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_named_columns_join1139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1152 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_join_column_list1155 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1157 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_name_in_table_primary1172 = new BitSet(new long[]{0x0000000000000802L,0x0000000000001020L});
    public static final BitSet FOLLOW_AS_in_table_primary1175 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_table_in_table_primary1186 = new BitSet(new long[]{0x0000000000000800L,0x0000000000001020L});
    public static final BitSet FOLLOW_AS_in_table_primary1188 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name1207 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_DOT_in_table_name1209 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001020L});
    public static final BitSet FOLLOW_table_identifier_in_table_name1213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name1228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_derived_table1241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier1258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name1273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name1290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regular_identifier_in_identifier1305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delimited_identifier_in_identifier1312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_regular_identifier1325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONCATENATION_in_concatenation_operator1402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_equals_operator1415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_not_equals_operator1428 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_not_equals_operator1430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_operator1445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_operator1460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_less_than_or_equals_operator1474 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_EQUALS_in_less_than_or_equals_operator1476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_greater_than_or_equals_operator1490 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_EQUALS_in_greater_than_or_equals_operator1492 = new BitSet(new long[]{0x0000000000000002L});

}