// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\Users\\obda\\Desktop\\DependencyAssertion.g 2010-07-12 17:32:09

package inf.unibz.it.obda.dependencies.parser;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
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

public class DependencyAssertionParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "BODY", "HEAD", "ALPHAVAR", "ALPHA", "INT", "CHAR", "WS", "','", "'('", "';'", "')'", "'includedIn'", "'disjoint'", "'functionOf'", "'['", "']'", "'.'", "'$'"
    };
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int HEAD=5;
    public static final int CHAR=9;
    public static final int INT=8;
    public static final int EOF=-1;
    public static final int ALPHA=7;
    public static final int T__19=19;
    public static final int WS=10;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALPHAVAR=6;
    public static final int BODY=4;

    // delegates
    // delegators


        public DependencyAssertionParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DependencyAssertionParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return DependencyAssertionParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Users\\obda\\Desktop\\DependencyAssertion.g"; }



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


            	boolean error1 = false;

            	ArrayList<QueryTerm> term_collector = new ArrayList<QueryTerm>();
            	ArrayList<QueryTerm> terms_ofMappingOne= new ArrayList<QueryTerm>();
            	ArrayList<QueryTerm>terms_ofMappingTwo = new ArrayList<QueryTerm>();
            	String tmpId = null;
            	String idForMappingOne = null;
            	String idForMappingTwo = null;
            	
            	private APIController apic = null;
            	
            	Vector<AbstractDependencyAssertion> dependencyAssertions = new Vector<AbstractDependencyAssertion>();

            	public void resetErrorFlag() {
            		error1 = false;
            	}
            	
            	public boolean getErrorFlag() {
            		return error1;
            	}
            	
            	public Vector<AbstractDependencyAssertion> getDependencyAssertion() {
            		return dependencyAssertions;
            	}
            	
            	public void setController(APIController con){
            		apic = con;
            	}

            	private void resetValues(){
            		terms_ofMappingOne = null;
            		terms_ofMappingTwo =null;
            		idForMappingOne = null;
            		idForMappingTwo = null;
            	}



    // $ANTLR start "parse"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:107:1: parse returns [boolean value] : prog EOF ;
    public final boolean parse() throws RecognitionException {
        boolean value = false;

        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:108:1: ( prog EOF )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:108:3: prog EOF
            {
            pushFollow(FOLLOW_prog_in_parse47);
            prog();

            state._fsp--;

            match(input,EOF,FOLLOW_EOF_in_parse49); 
             
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
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:120:1: prog : exp ( ',' exp )* ;
    public final void prog() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:121:7: ( exp ( ',' exp )* )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:121:10: exp ( ',' exp )*
            {
            pushFollow(FOLLOW_exp_in_prog79);
            exp();

            state._fsp--;

            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:121:14: ( ',' exp )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==11) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:121:15: ',' exp
            	    {
            	    match(input,11,FOLLOW_11_in_prog82); 
            	    pushFollow(FOLLOW_exp_in_prog84);
            	    exp();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "prog"


    // $ANTLR start "exp"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:129:1: exp : function '(' parameter1 ';' parameter2 ')' ;
    public final void exp() throws RecognitionException {
        DependencyAssertionParser.function_return function1 = null;


        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:129:6: ( function '(' parameter1 ';' parameter2 ')' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:129:8: function '(' parameter1 ';' parameter2 ')'
            {
            pushFollow(FOLLOW_function_in_exp107);
            function1=function();

            state._fsp--;

            match(input,12,FOLLOW_12_in_exp109); 
            pushFollow(FOLLOW_parameter1_in_exp111);
            parameter1();

            state._fsp--;

            match(input,13,FOLLOW_13_in_exp113); 
            pushFollow(FOLLOW_parameter2_in_exp115);
            parameter2();

            state._fsp--;

            match(input,14,FOLLOW_14_in_exp117); 

            		//            		System.out.println(idForMappingOne);
            //                        System.out.println(idForMappingTwo);
            //                        Iterator<QueryTerm> it1 = terms_ofMappingOne.iterator();
            //                        Iterator<QueryTerm> it2 = terms_ofMappingTwo.iterator();
            //                        while(it1.hasNext() && it2.hasNext()){
            //                        	System.out.println(it1.next().toString() + ", " + it2.next().toString());
            //                        }
                        			if(terms_ofMappingOne.size() != terms_ofMappingTwo.size()){
                        				error1=true;
                        				try {
            								throw new Exception("Both mappings in the assertion must have the same number of terms");
            							} catch (Exception e) {
            								e.printStackTrace();
            							}
                        			}else{
            	                        MappingController con = apic.getMappingController();
            	                        DatasourcesController dscon = apic.getDatasourcesController();
            	                        URI source_uri = dscon.getCurrentDataSource().getSourceID();
            	                        OBDAMappingAxiom map1 = con.getMapping(source_uri, idForMappingOne);
            	                        OBDAMappingAxiom map2 = con.getMapping(source_uri, idForMappingTwo);
            	                        if(map1 != null && map2 != null){
            		                        String f = (function1!=null?input.toString(function1.start,function1.stop):null);
            		                        if(f.equals("includedIn")){
            		                        	RDBMSInclusionDependency de = new RDBMSInclusionDependency(source_uri,idForMappingOne,idForMappingTwo,(RDBMSSQLQuery)map1.getSourceQuery(),(RDBMSSQLQuery)map2.getSourceQuery(), terms_ofMappingOne, terms_ofMappingTwo);
            		                        	dependencyAssertions.add(de);
            		                        }else if(f.equals("functionOf")){
            		                        	RDBMSFunctionalDependency de = new RDBMSFunctionalDependency(source_uri,idForMappingOne,idForMappingTwo,(RDBMSSQLQuery)map1.getSourceQuery(),(RDBMSSQLQuery)map2.getSourceQuery(), terms_ofMappingOne, terms_ofMappingTwo);
            		                        	dependencyAssertions.add(de);
            		                        }else if(f.equals("disjoint")){
            		                        	RDBMSDisjointnessDependency de = new RDBMSDisjointnessDependency(source_uri,idForMappingOne,idForMappingTwo,(RDBMSSQLQuery)map1.getSourceQuery(),(RDBMSSQLQuery)map2.getSourceQuery(), terms_ofMappingOne, terms_ofMappingTwo);
            		                        	dependencyAssertions.add(de);
            		                        }else{
            		                        	
            		                        }
            	                        }else{
            	                        	error1=true;
            	            				try {
            									throw new Exception("Invalid Mapping ID");
            								} catch (Exception e) {
            									e.printStackTrace();
            								}
            	                        }
            	                          resetValues();
                        			}
            		

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "exp"

    public static class function_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "function"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:180:1: function : ( 'includedIn' | 'disjoint' | 'functionOf' ) ;
    public final DependencyAssertionParser.function_return function() throws RecognitionException {
        DependencyAssertionParser.function_return retval = new DependencyAssertionParser.function_return();
        retval.start = input.LT(1);

        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:181:2: ( ( 'includedIn' | 'disjoint' | 'functionOf' ) )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:181:4: ( 'includedIn' | 'disjoint' | 'functionOf' )
            {
            if ( (input.LA(1)>=15 && input.LA(1)<=17) ) {
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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function"


    // $ANTLR start "parameter1"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:182:1: parameter1 : parameter ;
    public final void parameter1() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:182:12: ( parameter )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:182:14: parameter
            {
            pushFollow(FOLLOW_parameter_in_parameter1156);
            parameter();

            state._fsp--;

            terms_ofMappingOne = term_collector; term_collector =  new ArrayList<QueryTerm>(); idForMappingOne = tmpId; tmpId = null;

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parameter1"


    // $ANTLR start "parameter2"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:187:1: parameter2 : parameter ;
    public final void parameter2() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:187:12: ( parameter )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:187:14: parameter
            {
            pushFollow(FOLLOW_parameter_in_parameter2173);
            parameter();

            state._fsp--;

            terms_ofMappingTwo = term_collector; term_collector =  new ArrayList<QueryTerm>(); idForMappingTwo = tmpId; tmpId = null; 

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parameter2"


    // $ANTLR start "parameter"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:192:1: parameter : map '[' term ( ',' term )* ']' ;
    public final void parameter() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:192:12: ( map '[' term ( ',' term )* ']' )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:192:14: map '[' term ( ',' term )* ']'
            {
            pushFollow(FOLLOW_map_in_parameter191);
            map();

            state._fsp--;

            match(input,18,FOLLOW_18_in_parameter193); 
            pushFollow(FOLLOW_term_in_parameter195);
            term();

            state._fsp--;

            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:192:27: ( ',' term )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==11) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:192:28: ',' term
            	    {
            	    match(input,11,FOLLOW_11_in_parameter198); 
            	    pushFollow(FOLLOW_term_in_parameter200);
            	    term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            match(input,19,FOLLOW_19_in_parameter204); 

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parameter"


    // $ANTLR start "map"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:197:1: map returns [String value] : aux '.' id ;
    public final String map() throws RecognitionException {
        String value = null;

        DependencyAssertionParser.id_return id2 = null;


        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:197:28: ( aux '.' id )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:197:30: aux '.' id
            {
            pushFollow(FOLLOW_aux_in_map224);
            aux();

            state._fsp--;

            match(input,20,FOLLOW_20_in_map226); 
            pushFollow(FOLLOW_id_in_map227);
            id2=id();

            state._fsp--;

            String s = (id2!=null?input.toString(id2.start,id2.stop):null); tmpId= s;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "map"


    // $ANTLR start "aux"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:198:1: aux : ( BODY | HEAD ) ;
    public final void aux() throws RecognitionException {
        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:198:6: ( ( BODY | HEAD ) )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:198:8: ( BODY | HEAD )
            {
            if ( (input.LA(1)>=BODY && input.LA(1)<=HEAD) ) {
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
    // $ANTLR end "aux"

    public static class id_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "id"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:199:1: id : var ;
    public final DependencyAssertionParser.id_return id() throws RecognitionException {
        DependencyAssertionParser.id_return retval = new DependencyAssertionParser.id_return();
        retval.start = input.LT(1);

        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:199:5: ( var )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:199:7: var
            {
            pushFollow(FOLLOW_var_in_id251);
            var();

            state._fsp--;


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "id"


    // $ANTLR start "term"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:200:1: term returns [QueryTerm value] : '$' var ;
    public final QueryTerm term() throws RecognitionException {
        QueryTerm value = null;

        String var3 = null;


        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:200:32: ( '$' var )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:200:34: '$' var
            {
            match(input,21,FOLLOW_21_in_term262); 
            pushFollow(FOLLOW_var_in_term263);
            var3=var();

            state._fsp--;

            VariableTerm t = new VariableTerm(var3); value =t; term_collector.add(t); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "term"


    // $ANTLR start "var"
    // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:201:1: var returns [String value] : ALPHAVAR ;
    public final String var() throws RecognitionException {
        String value = null;

        Token ALPHAVAR4=null;

        try {
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:202:2: ( ALPHAVAR )
            // C:\\Users\\obda\\Desktop\\DependencyAssertion.g:202:4: ALPHAVAR
            {
            ALPHAVAR4=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_var277); 
            value = (ALPHAVAR4!=null?ALPHAVAR4.getText():null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "var"

    // Delegated rules


 

    public static final BitSet FOLLOW_prog_in_parse47 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse49 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp_in_prog79 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_11_in_prog82 = new BitSet(new long[]{0x0000000000038000L});
    public static final BitSet FOLLOW_exp_in_prog84 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_function_in_exp107 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_exp109 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_parameter1_in_exp111 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_exp113 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_parameter2_in_exp115 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_exp117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_function143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_parameter1156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_parameter2173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_map_in_parameter191 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_18_in_parameter193 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_term_in_parameter195 = new BitSet(new long[]{0x0000000000080800L});
    public static final BitSet FOLLOW_11_in_parameter198 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_term_in_parameter200 = new BitSet(new long[]{0x0000000000080800L});
    public static final BitSet FOLLOW_19_in_parameter204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_aux_in_map224 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_map226 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_id_in_map227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_aux239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_id251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_term262 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_var_in_term263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_var277 = new BitSet(new long[]{0x0000000000000002L});

}