// $ANTLR 3.1.2 /home/obda/SQL.g 2009-11-19 11:28:05


package inf.unibz.it.sql.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SQLParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALPHAVAR", "NUMBER", "ALPHA", "DIGIT", "CHAR", "INTEGER", "BOOL", "WS", "'select'", "'SELECT'", "'Select'", "'FROM'", "'from'", "'WHERE'", "'where'", "'DISTINCT'", "'distinct'", "','", "'.'", "'as'", "'AS'", "'AND'", "'and'", "'YEAR('", "')'", "'\\''", "'>'", "'='", "'<'", "'<>'", "'<='", "'>='", "'=>'", "'=<'"
    };
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
    public static final int T__30=30;
    public static final int T__19=19;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=11;
    public static final int T__33=33;
    public static final int T__16=16;
    public static final int T__34=34;
    public static final int T__15=15;
    public static final int T__35=35;
    public static final int T__18=18;
    public static final int T__36=36;
    public static final int T__17=17;
    public static final int T__37=37;
    public static final int T__12=12;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALPHAVAR=4;
    public static final int DIGIT=7;

    // delegates
    // delegators


        public SQLParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public SQLParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return SQLParser.tokenNames; }
    public String getGrammarFileName() { return "/home/obda/SQL.g"; }



    	private SQLQuery finalquery = null;
    	private List<SQLTable> selectedTables = new Vector<SQLTable>();
    	private List<SQLCondition> conditions = new Vector<SQLCondition>();
    	private List<SQLSelection> selectedVariables = new Vector<SQLSelection>();
    	private String tablename = null;
    	private String alias = null;
    	private String schema = null;
    	private boolean error1 = false;
    	ISQLTerm t1 = null;
    	ISQLTerm t2 = null;
    	private boolean isDistinct = false;
    	private List<String> errors = new LinkedList<String>();
    	
        	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        	        String hdr = getErrorHeader(e);
        	        String msg = getErrorMessage(e, tokenNames);
        	        errors.add(hdr + " " + msg);
        	}
        	
        	public List<String> getErrors() {
        	        return errors;
        	}
        	
        	public SQLQuery getSQLQuery(){
        	        return finalquery ;
        	}


            	
    	
    	




    // $ANTLR start "parse"
    // /home/obda/SQL.g:81:1: parse returns [boolean value] : prog EOF ;
    public final boolean parse() throws RecognitionException {
        boolean value = false;

        try {
            // /home/obda/SQL.g:82:1: ( prog EOF )
            // /home/obda/SQL.g:82:3: prog EOF
            {
            pushFollow(FOLLOW_prog_in_parse46);
            prog();

            state._fsp--;

            match(input,EOF,FOLLOW_EOF_in_parse48); 
             
            		//System.out.println(query_atoms.toString()); 
            		value = !error1; 
            		//System.out.println("test" + value);}
            		

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		value = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "parse"


    // $ANTLR start "prog"
    // /home/obda/SQL.g:94:1: prog : query ;
    public final void prog() throws RecognitionException {
        try {
            // /home/obda/SQL.g:94:9: ( query )
            // /home/obda/SQL.g:94:11: query
            {
            pushFollow(FOLLOW_query_in_prog71);
            query();

            state._fsp--;


            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "prog"


    // $ANTLR start "query"
    // /home/obda/SQL.g:100:1: query : ( 'select' | 'SELECT' | 'Select' ) ( distinct )? select_exp ( 'FROM' | 'from' ) table_reference ( ( 'WHERE' | 'where' ) conditions )? ;
    public final void query() throws RecognitionException {
        try {
            // /home/obda/SQL.g:100:8: ( ( 'select' | 'SELECT' | 'Select' ) ( distinct )? select_exp ( 'FROM' | 'from' ) table_reference ( ( 'WHERE' | 'where' ) conditions )? )
            // /home/obda/SQL.g:100:10: ( 'select' | 'SELECT' | 'Select' ) ( distinct )? select_exp ( 'FROM' | 'from' ) table_reference ( ( 'WHERE' | 'where' ) conditions )?
            {
            if ( (input.LA(1)>=12 && input.LA(1)<=14) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            // /home/obda/SQL.g:100:39: ( distinct )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( ((LA1_0>=19 && LA1_0<=20)) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/obda/SQL.g:100:40: distinct
                    {
                    pushFollow(FOLLOW_distinct_in_query95);
                    distinct();

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_select_exp_in_query99);
            select_exp();

            state._fsp--;

            if ( (input.LA(1)>=15 && input.LA(1)<=16) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            pushFollow(FOLLOW_table_reference_in_query107);
            table_reference();

            state._fsp--;

            // /home/obda/SQL.g:100:94: ( ( 'WHERE' | 'where' ) conditions )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( ((LA2_0>=17 && LA2_0<=18)) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /home/obda/SQL.g:100:95: ( 'WHERE' | 'where' ) conditions
                    {
                    if ( (input.LA(1)>=17 && input.LA(1)<=18) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_conditions_in_query116);
                    conditions();

                    state._fsp--;


                    }
                    break;

            }

             finalquery = new SQLQuery(selectedVariables, selectedTables, conditions,isDistinct);
            		

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "query"


    // $ANTLR start "distinct"
    // /home/obda/SQL.g:107:1: distinct : ( 'DISTINCT' | 'distinct' ) ;
    public final void distinct() throws RecognitionException {
        try {
            // /home/obda/SQL.g:107:10: ( ( 'DISTINCT' | 'distinct' ) )
            // /home/obda/SQL.g:107:12: ( 'DISTINCT' | 'distinct' )
            {
            if ( (input.LA(1)>=19 && input.LA(1)<=20) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            isDistinct = true;

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "distinct"


    // $ANTLR start "select_exp"
    // /home/obda/SQL.g:113:1: select_exp : select_term ( ',' select_term )* ;
    public final void select_exp() throws RecognitionException {
        try {
            // /home/obda/SQL.g:113:13: ( select_term ( ',' select_term )* )
            // /home/obda/SQL.g:113:15: select_term ( ',' select_term )*
            {
            pushFollow(FOLLOW_select_term_in_select_exp154);
            select_term();

            state._fsp--;

            // /home/obda/SQL.g:113:27: ( ',' select_term )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==21) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/obda/SQL.g:113:28: ',' select_term
            	    {
            	    match(input,21,FOLLOW_21_in_select_exp157); 
            	    pushFollow(FOLLOW_select_term_in_select_exp158);
            	    select_term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "select_exp"


    // $ANTLR start "table_reference"
    // /home/obda/SQL.g:119:1: table_reference : ref ( ',' ref )* ;
    public final void table_reference() throws RecognitionException {
        try {
            // /home/obda/SQL.g:120:2: ( ref ( ',' ref )* )
            // /home/obda/SQL.g:120:4: ref ( ',' ref )*
            {
            pushFollow(FOLLOW_ref_in_table_reference177);
            ref();

            state._fsp--;

            // /home/obda/SQL.g:120:8: ( ',' ref )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==21) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/obda/SQL.g:120:9: ',' ref
            	    {
            	    match(input,21,FOLLOW_21_in_table_reference180); 
            	    pushFollow(FOLLOW_ref_in_table_reference182);
            	    ref();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "table_reference"


    // $ANTLR start "select_term"
    // /home/obda/SQL.g:126:1: select_term : sterm ( ',' sterm )* ;
    public final void select_term() throws RecognitionException {
        try {
            // /home/obda/SQL.g:126:13: ( sterm ( ',' sterm )* )
            // /home/obda/SQL.g:126:15: sterm ( ',' sterm )*
            {
            pushFollow(FOLLOW_sterm_in_select_term199);
            sterm();

            state._fsp--;

            // /home/obda/SQL.g:126:21: ( ',' sterm )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==21) ) {
                    int LA5_1 = input.LA(2);

                    if ( (LA5_1==ALPHAVAR) ) {
                        alt5=1;
                    }


                }


                switch (alt5) {
            	case 1 :
            	    // /home/obda/SQL.g:126:22: ',' sterm
            	    {
            	    match(input,21,FOLLOW_21_in_select_term202); 
            	    pushFollow(FOLLOW_sterm_in_select_term203);
            	    sterm();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "select_term"


    // $ANTLR start "ref"
    // /home/obda/SQL.g:132:1: ref : ( schema '.' )? tablename ( ( 'as' | 'AS' ) alias )? ;
    public final void ref() throws RecognitionException {
        try {
            // /home/obda/SQL.g:132:5: ( ( schema '.' )? tablename ( ( 'as' | 'AS' ) alias )? )
            // /home/obda/SQL.g:132:7: ( schema '.' )? tablename ( ( 'as' | 'AS' ) alias )?
            {
            // /home/obda/SQL.g:132:7: ( schema '.' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==ALPHAVAR) ) {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==22) ) {
                    alt6=1;
                }
            }
            switch (alt6) {
                case 1 :
                    // /home/obda/SQL.g:132:8: schema '.'
                    {
                    pushFollow(FOLLOW_schema_in_ref221);
                    schema();

                    state._fsp--;

                    match(input,22,FOLLOW_22_in_ref222); 

                    }
                    break;

            }

            pushFollow(FOLLOW_tablename_in_ref226);
            tablename();

            state._fsp--;

            // /home/obda/SQL.g:132:30: ( ( 'as' | 'AS' ) alias )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=23 && LA7_0<=24)) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/obda/SQL.g:132:31: ( 'as' | 'AS' ) alias
                    {
                    if ( (input.LA(1)>=23 && input.LA(1)<=24) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_alias_in_ref235);
                    alias();

                    state._fsp--;


                    }
                    break;

            }

             SQLTable t = new SQLTable(schema, tablename, alias);
            				           selectedTables.add(t);
            				           schema =null;
            				           tablename= null;
            				           alias = null;	
            					
            		

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ref"


    // $ANTLR start "conditions"
    // /home/obda/SQL.g:144:1: conditions : condition ( ( 'AND' | 'and' ) condition )* ;
    public final void conditions() throws RecognitionException {
        try {
            // /home/obda/SQL.g:144:13: ( condition ( ( 'AND' | 'and' ) condition )* )
            // /home/obda/SQL.g:144:15: condition ( ( 'AND' | 'and' ) condition )*
            {
            pushFollow(FOLLOW_condition_in_conditions255);
            condition();

            state._fsp--;

            // /home/obda/SQL.g:144:25: ( ( 'AND' | 'and' ) condition )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>=25 && LA8_0<=26)) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/obda/SQL.g:144:26: ( 'AND' | 'and' ) condition
            	    {
            	    if ( (input.LA(1)>=25 && input.LA(1)<=26) ) {
            	        input.consume();
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_condition_in_conditions264);
            	    condition();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1= false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "conditions"


    // $ANTLR start "condition"
    // /home/obda/SQL.g:150:1: condition : cterm op cterm ;
    public final void condition() throws RecognitionException {
        SQLParser.op_return op1 = null;


        try {
            // /home/obda/SQL.g:150:12: ( cterm op cterm )
            // /home/obda/SQL.g:150:15: cterm op cterm
            {
            pushFollow(FOLLOW_cterm_in_condition283);
            cterm();

            state._fsp--;

            pushFollow(FOLLOW_op_in_condition285);
            op1=op();

            state._fsp--;

            pushFollow(FOLLOW_cterm_in_condition287);
            cterm();

            state._fsp--;

             SQLCondition ct = new SQLCondition(t1, t2, (op1!=null?input.toString(op1.start,op1.stop):null));
            			     conditions.add(ct);	     		
            			     t1= null; t2=null;	
            		

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "condition"


    // $ANTLR start "sterm"
    // /home/obda/SQL.g:159:1: sterm : ( tablename '.' )? ALPHAVAR ( ( 'as' | 'AS' ) alias )? ;
    public final void sterm() throws RecognitionException {
        Token ALPHAVAR2=null;

        try {
            // /home/obda/SQL.g:159:7: ( ( tablename '.' )? ALPHAVAR ( ( 'as' | 'AS' ) alias )? )
            // /home/obda/SQL.g:159:9: ( tablename '.' )? ALPHAVAR ( ( 'as' | 'AS' ) alias )?
            {
            // /home/obda/SQL.g:159:9: ( tablename '.' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ALPHAVAR) ) {
                int LA9_1 = input.LA(2);

                if ( (LA9_1==22) ) {
                    alt9=1;
                }
            }
            switch (alt9) {
                case 1 :
                    // /home/obda/SQL.g:159:10: tablename '.'
                    {
                    pushFollow(FOLLOW_tablename_in_sterm303);
                    tablename();

                    state._fsp--;

                    match(input,22,FOLLOW_22_in_sterm304); 

                    }
                    break;

            }

            ALPHAVAR2=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_sterm308); 
            // /home/obda/SQL.g:159:34: ( ( 'as' | 'AS' ) alias )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( ((LA10_0>=23 && LA10_0<=24)) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/obda/SQL.g:159:35: ( 'as' | 'AS' ) alias
                    {
                    if ( (input.LA(1)>=23 && input.LA(1)<=24) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_alias_in_sterm317);
                    alias();

                    state._fsp--;


                    }
                    break;

            }

             SQLSelection s = new SQLSelection(tablename, (ALPHAVAR2!=null?ALPHAVAR2.getText():null), alias);
            					selectedVariables.add(s);
            					tablename = null;
            					alias = null;
            		

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1= false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "sterm"


    // $ANTLR start "cterm"
    // /home/obda/SQL.g:169:1: cterm : ( term | constant | number | function );
    public final void cterm() throws RecognitionException {
        try {
            // /home/obda/SQL.g:169:7: ( term | constant | number | function )
            int alt11=4;
            switch ( input.LA(1) ) {
            case ALPHAVAR:
                {
                alt11=1;
                }
                break;
            case 29:
                {
                alt11=2;
                }
                break;
            case NUMBER:
                {
                alt11=3;
                }
                break;
            case 27:
                {
                alt11=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // /home/obda/SQL.g:169:9: term
                    {
                    pushFollow(FOLLOW_term_in_cterm335);
                    term();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/obda/SQL.g:169:16: constant
                    {
                    pushFollow(FOLLOW_constant_in_cterm339);
                    constant();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/obda/SQL.g:169:26: number
                    {
                    pushFollow(FOLLOW_number_in_cterm342);
                    number();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /home/obda/SQL.g:169:34: function
                    {
                    pushFollow(FOLLOW_function_in_cterm345);
                    function();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cterm"


    // $ANTLR start "term"
    // /home/obda/SQL.g:175:1: term : ( tablename '.' )? ALPHAVAR ;
    public final void term() throws RecognitionException {
        Token ALPHAVAR3=null;

        try {
            // /home/obda/SQL.g:175:7: ( ( tablename '.' )? ALPHAVAR )
            // /home/obda/SQL.g:175:9: ( tablename '.' )? ALPHAVAR
            {
            // /home/obda/SQL.g:175:9: ( tablename '.' )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==ALPHAVAR) ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==22) ) {
                    alt12=1;
                }
            }
            switch (alt12) {
                case 1 :
                    // /home/obda/SQL.g:175:10: tablename '.'
                    {
                    pushFollow(FOLLOW_tablename_in_term361);
                    tablename();

                    state._fsp--;

                    match(input,22,FOLLOW_22_in_term363); 

                    }
                    break;

            }

            ALPHAVAR3=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_term367); 

            					SQLVariableTerm t =  new SQLVariableTerm(tablename, (ALPHAVAR3!=null?ALPHAVAR3.getText():null));
            					tablename = null;
            					if(t1 == null){
            					t1 = t;
            					}else{
            					t2 = t;
            					}
            		 

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /home/obda/SQL.g:189:1: function : db2_function ;
    public final void function() throws RecognitionException {
        try {
            // /home/obda/SQL.g:189:10: ( db2_function )
            // /home/obda/SQL.g:189:12: db2_function
            {
            pushFollow(FOLLOW_db2_function_in_function383);
            db2_function();

            state._fsp--;


            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "function"


    // $ANTLR start "db2_function"
    // /home/obda/SQL.g:195:1: db2_function : 'YEAR(' ALPHAVAR ')' ;
    public final void db2_function() throws RecognitionException {
        Token ALPHAVAR4=null;

        try {
            // /home/obda/SQL.g:195:14: ( 'YEAR(' ALPHAVAR ')' )
            // /home/obda/SQL.g:195:16: 'YEAR(' ALPHAVAR ')'
            {
            match(input,27,FOLLOW_27_in_db2_function398); 
            ALPHAVAR4=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_db2_function400); 
            match(input,28,FOLLOW_28_in_db2_function402); 
            SQLFunctionTerm nc = new SQLFunctionTerm("YEAR", new SQLVariableTerm(tablename,(ALPHAVAR4!=null?ALPHAVAR4.getText():null)));
            			if(t1 == null){
            				t1 = nc;
            			}else{
            				t2 = nc;
            			}
            			tablename=null;
            		

            }

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "db2_function"


    // $ANTLR start "constant"
    // /home/obda/SQL.g:208:1: constant : ( ( '\\'' string '\\'' ) | ( '\\'' number '\\'' ) );
    public final void constant() throws RecognitionException {
        try {
            // /home/obda/SQL.g:208:10: ( ( '\\'' string '\\'' ) | ( '\\'' number '\\'' ) )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==29) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==ALPHAVAR) ) {
                    alt13=1;
                }
                else if ( (LA13_1==NUMBER) ) {
                    alt13=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/obda/SQL.g:208:12: ( '\\'' string '\\'' )
                    {
                    // /home/obda/SQL.g:208:12: ( '\\'' string '\\'' )
                    // /home/obda/SQL.g:208:13: '\\'' string '\\''
                    {
                    match(input,29,FOLLOW_29_in_constant418); 
                    pushFollow(FOLLOW_string_in_constant419);
                    string();

                    state._fsp--;

                    match(input,29,FOLLOW_29_in_constant420); 

                    }


                    }
                    break;
                case 2 :
                    // /home/obda/SQL.g:208:29: ( '\\'' number '\\'' )
                    {
                    // /home/obda/SQL.g:208:29: ( '\\'' number '\\'' )
                    // /home/obda/SQL.g:208:30: '\\'' number '\\''
                    {
                    match(input,29,FOLLOW_29_in_constant424); 
                    pushFollow(FOLLOW_number_in_constant426);
                    number();

                    state._fsp--;

                    match(input,29,FOLLOW_29_in_constant428); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "constant"

    public static class string_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "string"
    // /home/obda/SQL.g:213:1: string : ALPHAVAR ;
    public final SQLParser.string_return string() throws RecognitionException {
        SQLParser.string_return retval = new SQLParser.string_return();
        retval.start = input.LT(1);

        try {
            // /home/obda/SQL.g:213:9: ( ALPHAVAR )
            // /home/obda/SQL.g:213:11: ALPHAVAR
            {
            match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_string443); 
            SQLLiteralConstant nc = new SQLLiteralConstant(input.toString(retval.start,input.LT(-1)));
            			if(t1 == null){
            				t1 = nc;
            			}else{
            				t2 = nc;
            			}
            		

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1= false; 
            		throw ex; 
            	
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "string"

    public static class number_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "number"
    // /home/obda/SQL.g:225:1: number : NUMBER ;
    public final SQLParser.number_return number() throws RecognitionException {
        SQLParser.number_return retval = new SQLParser.number_return();
        retval.start = input.LT(1);

        try {
            // /home/obda/SQL.g:225:9: ( NUMBER )
            // /home/obda/SQL.g:225:11: NUMBER
            {
            match(input,NUMBER,FOLLOW_NUMBER_in_number459); 
            SQLNummericConstant nc = new SQLNummericConstant(input.toString(retval.start,input.LT(-1)));
            			if(t1 == null){
            				t1 = nc;
            			}else{
            				t2 = nc;
            			}
            		

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "number"

    public static class schema_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "schema"
    // /home/obda/SQL.g:237:1: schema : ALPHAVAR ;
    public final SQLParser.schema_return schema() throws RecognitionException {
        SQLParser.schema_return retval = new SQLParser.schema_return();
        retval.start = input.LT(1);

        try {
            // /home/obda/SQL.g:237:9: ( ALPHAVAR )
            // /home/obda/SQL.g:237:11: ALPHAVAR
            {
            match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_schema475); 
            schema = input.toString(retval.start,input.LT(-1));

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "schema"

    public static class tablename_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "tablename"
    // /home/obda/SQL.g:243:1: tablename : ALPHAVAR ;
    public final SQLParser.tablename_return tablename() throws RecognitionException {
        SQLParser.tablename_return retval = new SQLParser.tablename_return();
        retval.start = input.LT(1);

        try {
            // /home/obda/SQL.g:243:12: ( ALPHAVAR )
            // /home/obda/SQL.g:243:14: ALPHAVAR
            {
            match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_tablename491); 
            tablename = input.toString(retval.start,input.LT(-1));

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tablename"

    public static class alias_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "alias"
    // /home/obda/SQL.g:249:1: alias : ALPHAVAR ;
    public final SQLParser.alias_return alias() throws RecognitionException {
        SQLParser.alias_return retval = new SQLParser.alias_return();
        retval.start = input.LT(1);

        try {
            // /home/obda/SQL.g:249:7: ( ALPHAVAR )
            // /home/obda/SQL.g:249:9: ALPHAVAR
            {
            match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_alias506); 
            alias = input.toString(retval.start,input.LT(-1));

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1 = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "alias"

    public static class op_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "op"
    // /home/obda/SQL.g:255:1: op : ( '>' | '=' | '<' | '<>' | '<=' | '>=' | '=>' | '=<' );
    public final SQLParser.op_return op() throws RecognitionException {
        SQLParser.op_return retval = new SQLParser.op_return();
        retval.start = input.LT(1);

        try {
            // /home/obda/SQL.g:255:4: ( '>' | '=' | '<' | '<>' | '<=' | '>=' | '=>' | '=<' )
            // /home/obda/SQL.g:
            {
            if ( (input.LA(1)>=30 && input.LA(1)<=37) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException ex) {
             
            //		reportError(ex); 
            		error1= false; 
            		throw ex; 
            	
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "op"

    // Delegated rules


 

    public static final BitSet FOLLOW_prog_in_parse46 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse48 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_query_in_prog71 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_query86 = new BitSet(new long[]{0x0000000000180010L});
    public static final BitSet FOLLOW_distinct_in_query95 = new BitSet(new long[]{0x0000000000180010L});
    public static final BitSet FOLLOW_select_exp_in_query99 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_set_in_query101 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_table_reference_in_query107 = new BitSet(new long[]{0x0000000000060002L});
    public static final BitSet FOLLOW_set_in_query110 = new BitSet(new long[]{0x0000000028000030L});
    public static final BitSet FOLLOW_conditions_in_query116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_distinct133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_term_in_select_exp154 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_21_in_select_exp157 = new BitSet(new long[]{0x0000000000180010L});
    public static final BitSet FOLLOW_select_term_in_select_exp158 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_ref_in_table_reference177 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_21_in_table_reference180 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ref_in_table_reference182 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_sterm_in_select_term199 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_21_in_select_term202 = new BitSet(new long[]{0x0000000000180010L});
    public static final BitSet FOLLOW_sterm_in_select_term203 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_schema_in_ref221 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_ref222 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_tablename_in_ref226 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_set_in_ref229 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_alias_in_ref235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condition_in_conditions255 = new BitSet(new long[]{0x0000000006000002L});
    public static final BitSet FOLLOW_set_in_conditions258 = new BitSet(new long[]{0x0000000028000030L});
    public static final BitSet FOLLOW_condition_in_conditions264 = new BitSet(new long[]{0x0000000006000002L});
    public static final BitSet FOLLOW_cterm_in_condition283 = new BitSet(new long[]{0x0000003FC0000000L});
    public static final BitSet FOLLOW_op_in_condition285 = new BitSet(new long[]{0x0000000028000030L});
    public static final BitSet FOLLOW_cterm_in_condition287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tablename_in_sterm303 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_sterm304 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ALPHAVAR_in_sterm308 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_set_in_sterm311 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_alias_in_sterm317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_cterm335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_cterm339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_number_in_cterm342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_cterm345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tablename_in_term361 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_term363 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ALPHAVAR_in_term367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_db2_function_in_function383 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_db2_function398 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ALPHAVAR_in_db2_function400 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_db2_function402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_constant418 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_string_in_constant419 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_constant420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_constant424 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_number_in_constant426 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_constant428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_string443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_number459 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_schema475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_tablename491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_alias506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_op0 = new BitSet(new long[]{0x0000000000000002L});

}