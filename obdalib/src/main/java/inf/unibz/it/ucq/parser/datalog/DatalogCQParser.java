// $ANTLR 3.1.1 /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g 2009-02-13 11:21:53


package inf.unibz.it.ucq.parser.datalog;

import inf.unibz.it.dl.domain.DataProperty;
import inf.unibz.it.dl.domain.NamedConcept;
import inf.unibz.it.dl.domain.NamedProperty;
import inf.unibz.it.dl.domain.ObjectProperty;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConstantTerm;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

public class DatalogCQParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALPHAVAR", "ALPHA", "INT", "CHAR", "WS", "','", "'('", "')'", "'$'", "'?'", "'\\''"
    };
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

    
    APIController apic = null;
    
    // delegates
    // delegators


        public DatalogCQParser(TokenStream input) {
            this(input, new RecognizerSharedState());
            
        }
        public DatalogCQParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
        
    public void  setOBDAAPIController(APIController apicontroller) {
    	this.apic = apicontroller;
    }
        

    public String[] getTokenNames() { return DatalogCQParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g"; }




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

    ArrayList<QueryTerm> function_parameter_collector = new ArrayList<QueryTerm>();
    //Stack<QueryAtom> atom_stack = new Stack<QueryAtom>();
    ArrayList<QueryAtom> query_atoms = new ArrayList<QueryAtom>();

    	public void resetErrorFlag() {
    		error1 = false;
    	}
    	
    	public boolean getErrorFlag() {
    		return error1;
    	}
    	
    	public ArrayList<QueryAtom> getQueryAtoms() {
    		return query_atoms;
    	}
    /*
    public void recoverFromMismatchedToken(IntStream input,
    										   RecognitionException e,
    										   int ttype,
    										   BitSet follow)
    		throws RecognitionException
    	{
    		System.err.println("BR.recoverFromMismatchedToken");		
    		// if next token is what we are looking for then "delete" this token
    		if ( input.LA(2)==ttype ) {
    		
    			reportError(e);
    			
    			beginResync();
    			input.consume(); // simply delete extra token
    			endResync();
    			input.consume(); // move past ttype token as if all were ok
    			throw e;
    		}
    		if ( !recoverFromMismatchedElement(input,e,follow) ) {
    			throw e;
    		}
    		throw e;
    	} */



    // $ANTLR start "parse"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:103:1: parse returns [boolean value] : prog EOF ;
    public final boolean parse() throws RecognitionException {
        boolean value = false;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:104:1: ( prog EOF )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:104:3: prog EOF
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
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:116:1: prog : atom ( ',' atom )* ;
    public final void prog() throws RecognitionException {
        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:117:7: ( atom ( ',' atom )* )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:117:10: atom ( ',' atom )*
            {
            pushFollow(FOLLOW_atom_in_prog79);
            atom();

            state._fsp--;

            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:117:15: ( ',' atom )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==9) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:117:16: ',' atom
            	    {
            	    match(input,9,FOLLOW_9_in_prog82); 
            	    pushFollow(FOLLOW_atom_in_prog84);
            	    atom();

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


    // $ANTLR start "atom"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:126:1: atom : ( concept_query_atom | binary_query_atom );
    public final void atom() throws RecognitionException {
        ConceptQueryAtom concept_query_atom1 = null;

        BinaryQueryAtom binary_query_atom2 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:127:2: ( concept_query_atom | binary_query_atom )
            int alt2=2;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:127:4: concept_query_atom
                    {
                    pushFollow(FOLLOW_concept_query_atom_in_atom110);
                    concept_query_atom1=concept_query_atom();

                    state._fsp--;

                     query_atoms.add(concept_query_atom1); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:128:5: binary_query_atom
                    {
                    pushFollow(FOLLOW_binary_query_atom_in_atom118);
                    binary_query_atom2=binary_query_atom();

                    state._fsp--;
                    if (binary_query_atom2 == null) {
                    	throw new IllegalArgumentException("Unrecognized query atom: " + errors.get(errors.size()-1));
                    }
                     query_atoms.add(binary_query_atom2); 

                    }
                    break;

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
    // $ANTLR end "atom"


    // $ANTLR start "concept_query_atom"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:137:1: concept_query_atom returns [ConceptQueryAtom value] : function_id '(' term ')' ;
    public final ConceptQueryAtom concept_query_atom() throws RecognitionException {
        ConceptQueryAtom value = null;

        String function_id3 = null;

        QueryTerm term4 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:138:2: ( function_id '(' term ')' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:138:4: function_id '(' term ')'
            {
            pushFollow(FOLLOW_function_id_in_concept_query_atom147);
            function_id3=function_id();

            state._fsp--;

            match(input,10,FOLLOW_10_in_concept_query_atom149); 
            pushFollow(FOLLOW_term_in_concept_query_atom151);
            term4=term();

            state._fsp--;

            match(input,11,FOLLOW_11_in_concept_query_atom153); 

            			NamedConcept concept = null;
            			try {
            				concept = new NamedConcept(new URI(function_id3));
            			} catch (URISyntaxException uriex) {
            				uriex.printStackTrace(System.err);
            				throw new RecognitionException();
            			}
            			ConceptQueryAtom atom = new ConceptQueryAtom(concept, term4);
            			value = atom;
            	

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex;  
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "concept_query_atom"


    // $ANTLR start "binary_query_atom"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:155:1: binary_query_atom returns [BinaryQueryAtom value] : function_id '(' term1 ',' term2 ')' ;
    public final BinaryQueryAtom binary_query_atom() throws RecognitionException {
        BinaryQueryAtom value = null;

        String function_id5 = null;

        QueryTerm term16 = null;

        QueryTerm term27 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:156:2: ( function_id '(' term1 ',' term2 ')' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:156:4: function_id '(' term1 ',' term2 ')'
            {
            pushFollow(FOLLOW_function_id_in_binary_query_atom178);
            function_id5=function_id();

            state._fsp--;

            match(input,10,FOLLOW_10_in_binary_query_atom180); 
            pushFollow(FOLLOW_term1_in_binary_query_atom182);
            term16=term1();

            state._fsp--;

            match(input,9,FOLLOW_9_in_binary_query_atom184); 
            pushFollow(FOLLOW_term2_in_binary_query_atom186);
            term27=term2();

            state._fsp--;

            match(input,11,FOLLOW_11_in_binary_query_atom188); 

            			NamedProperty relation = null;
            			String rolename = function_id5;
            			APICoupler coupler = apic.getCoupler();
            			
            			if (coupler != null) {
            				if ((coupler.isDatatypeProperty(URI.create(rolename)))||(coupler.isDatatypeProperty(URI.create(apic.getCurrentOntologyURI() +"#" + rolename)))) {
            					relation = new DataProperty(URI.create(rolename));
            				} else if ((coupler.isObjectProperty(URI.create(rolename)))||(coupler.isObjectProperty(URI.create(apic.getCurrentOntologyURI() +"#" + rolename)))) {
            					relation = new ObjectProperty(URI.create(rolename));
            				} else {
            					throw new Exception(rolename + ": Impossible to detect if predicate is an ObjectProperty/DatatypeProperty. Verify that the OBDA API has a coupler and that it is able to answer for this property.");
            				}
            			} else {
            				throw new Exception("No APICoupler has been defined. Define a APICoupler for the current APIController");
            			}
            			BinaryQueryAtom query_atom = new BinaryQueryAtom(relation, term16, term27);
            			value = query_atom;
            	

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        catch (Exception ex) {
        			errors.add(ex.getMessage());
            		error1 = true; 
            		
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "binary_query_atom"


    // $ANTLR start "term1"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:184:1: term1 returns [QueryTerm value] : term ;
    public final QueryTerm term1() throws RecognitionException {
        QueryTerm value = null;

        QueryTerm term8 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:185:2: ( term )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:185:4: term
            {
            pushFollow(FOLLOW_term_in_term1221);
            term8=term();

            state._fsp--;

            value = term8;

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "term1"


    // $ANTLR start "term2"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:192:1: term2 returns [QueryTerm value] : term ;
    public final QueryTerm term2() throws RecognitionException {
        QueryTerm value = null;

        QueryTerm term9 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:193:2: ( term )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:193:4: term
            {
            pushFollow(FOLLOW_term_in_term2246);
            term9=term();

            state._fsp--;

            value = term9;

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "term2"


    // $ANTLR start "term"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:200:1: term returns [QueryTerm value] : ( variable_term | constant_term | function_term );
    public final QueryTerm term() throws RecognitionException {
        QueryTerm value = null;

        VariableTerm variable_term10 = null;

        ConstantTerm constant_term11 = null;

        FunctionTerm function_term12 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:201:2: ( variable_term | constant_term | function_term )
            int alt3=3;
            switch ( input.LA(1) ) {
            case 12:
            case 13:
                {
                alt3=1;
                }
                break;
            case 14:
                {
                alt3=2;
                }
                break;
            case ALPHAVAR:
                {
                alt3=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:201:4: variable_term
                    {
                    pushFollow(FOLLOW_variable_term_in_term271);
                    variable_term10=variable_term();

                    state._fsp--;

                    value = variable_term10;

                    }
                    break;
                case 2 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:202:4: constant_term
                    {
                    pushFollow(FOLLOW_constant_term_in_term279);
                    constant_term11=constant_term();

                    state._fsp--;

                    value = constant_term11;

                    }
                    break;
                case 3 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:203:4: function_term
                    {
                    pushFollow(FOLLOW_function_term_in_term287);
                    function_term12=function_term();

                    state._fsp--;

                    value = function_term12;

                    }
                    break;

            }
        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "term"


    // $ANTLR start "function_term"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:213:1: function_term returns [FunctionTerm value] : function_id '(' function_parameter ( ',' function_parameter )* ')' ;
    public final FunctionTerm function_term() throws RecognitionException {
        FunctionTerm value = null;

        String function_id13 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:214:2: ( function_id '(' function_parameter ( ',' function_parameter )* ')' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:214:4: function_id '(' function_parameter ( ',' function_parameter )* ')'
            {
            pushFollow(FOLLOW_function_id_in_function_term319);
            function_id13=function_id();

            state._fsp--;

            match(input,10,FOLLOW_10_in_function_term321); 
            pushFollow(FOLLOW_function_parameter_in_function_term323);
            function_parameter();

            state._fsp--;

            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:214:39: ( ',' function_parameter )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==9) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:214:40: ',' function_parameter
            	    {
            	    match(input,9,FOLLOW_9_in_function_term326); 
            	    pushFollow(FOLLOW_function_parameter_in_function_term328);
            	    function_parameter();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match(input,11,FOLLOW_11_in_function_term332); 
             
            		//ArrayList<QueryTerm> terms = new ArrayList<QueryTerm>();
            		//while (!function_parameter_collector.isEmpty()) {
            		//	terms.add(function_parameter_collector.pop());
            		//}
            		
            		FunctionTerm new_function = new FunctionTerm(function_id13, function_parameter_collector);
            		value = new_function;
            		function_parameter_collector = new ArrayList<QueryTerm>();
            		

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "function_term"


    // $ANTLR start "function_parameter"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:230:1: function_parameter : ( function_variable | function_constant );
    public final void function_parameter() throws RecognitionException {
        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:231:2: ( function_variable | function_constant )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>=12 && LA5_0<=13)) ) {
                alt5=1;
            }
            else if ( (LA5_0==14) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:231:4: function_variable
                    {
                    pushFollow(FOLLOW_function_variable_in_function_parameter354);
                    function_variable();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:232:4: function_constant
                    {
                    pushFollow(FOLLOW_function_constant_in_function_parameter359);
                    function_constant();

                    state._fsp--;


                    }
                    break;

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
    // $ANTLR end "function_parameter"


    // $ANTLR start "function_id"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:239:1: function_id returns [String value] : ALPHAVAR ;
    public final String function_id() throws RecognitionException {
        String value = null;

        Token ALPHAVAR14=null;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:240:2: ( ALPHAVAR )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:240:4: ALPHAVAR
            {
            ALPHAVAR14=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_function_id381); 
            value = (ALPHAVAR14!=null?ALPHAVAR14.getText():null);

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "function_id"


    // $ANTLR start "function_variable"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:248:1: function_variable returns [VariableTerm value] : ( '$' | '?' ) varname ;
    public final VariableTerm function_variable() throws RecognitionException {
        VariableTerm value = null;

        String varname15 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:249:2: ( ( '$' | '?' ) varname )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:250:2: ( '$' | '?' ) varname
            {
            if ( (input.LA(1)>=12 && input.LA(1)<=13) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            pushFollow(FOLLOW_varname_in_function_variable413);
            varname15=varname();

            state._fsp--;

            value = new VariableTerm(varname15); function_parameter_collector.add(value); 

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "function_variable"


    // $ANTLR start "function_constant"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:260:1: function_constant returns [ConstantTerm value] : '\\'' ALPHAVAR '\\'' ;
    public final ConstantTerm function_constant() throws RecognitionException {
        ConstantTerm value = null;

        Token ALPHAVAR16=null;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:261:2: ( '\\'' ALPHAVAR '\\'' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:262:2: '\\'' ALPHAVAR '\\''
            {
            match(input,14,FOLLOW_14_in_function_constant441); 
            ALPHAVAR16=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_function_constant443); 
            match(input,14,FOLLOW_14_in_function_constant445); 
            value = new ConstantTerm((ALPHAVAR16!=null?ALPHAVAR16.getText():null)); function_parameter_collector.add(value); 

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "function_constant"


    // $ANTLR start "variable_term"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:270:1: variable_term returns [VariableTerm value] : ( '$' | '?' ) varname ;
    public final VariableTerm variable_term() throws RecognitionException {
        VariableTerm value = null;

        String varname17 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:271:2: ( ( '$' | '?' ) varname )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:272:2: ( '$' | '?' ) varname
            {
            if ( (input.LA(1)>=12 && input.LA(1)<=13) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            pushFollow(FOLLOW_varname_in_variable_term477);
            varname17=varname();

            state._fsp--;

            value = new VariableTerm(varname17);

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "variable_term"


    // $ANTLR start "constant_term"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:279:1: constant_term returns [ConstantTerm value] : '\\'' ALPHAVAR '\\'' ;
    public final ConstantTerm constant_term() throws RecognitionException {
        ConstantTerm value = null;

        Token ALPHAVAR18=null;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:280:2: ( '\\'' ALPHAVAR '\\'' )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:281:2: '\\'' ALPHAVAR '\\''
            {
            match(input,14,FOLLOW_14_in_constant_term503); 
            ALPHAVAR18=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_constant_term505); 
            match(input,14,FOLLOW_14_in_constant_term507); 
            value = new ConstantTerm((ALPHAVAR18!=null?ALPHAVAR18.getText():null)); 

            }

        }
        catch (RecognitionException ex) {
             
            		//reportError(ex); 
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "constant_term"


    // $ANTLR start "relationname"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:288:1: relationname returns [String value] : ALPHAVAR ;
    public final String relationname() throws RecognitionException {
        String value = null;

        Token ALPHAVAR19=null;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:289:2: ( ALPHAVAR )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:289:4: ALPHAVAR
            {
            ALPHAVAR19=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_relationname531); 
            value = (ALPHAVAR19!=null?ALPHAVAR19.getText():null);

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
    // $ANTLR end "relationname"


    // $ANTLR start "varname"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:292:1: varname returns [String value] : ALPHAVAR ;
    public final String varname() throws RecognitionException {
        String value = null;

        Token ALPHAVAR20=null;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:293:2: ( ALPHAVAR )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/domain/ucq/DatalogCQ.g:293:4: ALPHAVAR
            {
            ALPHAVAR20=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_varname549); 
            value = (ALPHAVAR20!=null?ALPHAVAR20.getText():null);

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
    // $ANTLR end "varname"

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    static final String DFA2_eotS =
        "\30\uffff";
    static final String DFA2_eofS =
        "\30\uffff";
    static final String DFA2_minS =
        "\1\4\1\12\3\4\1\12\1\11\1\16\1\14\2\uffff\1\11\2\4\1\11\1\16\1\14"+
        "\2\11\2\4\1\11\1\16\1\11";
    static final String DFA2_maxS =
        "\1\4\1\12\1\16\2\4\1\12\1\13\2\16\2\uffff\1\13\2\4\1\13\2\16\2\13"+
        "\2\4\1\13\1\16\1\13";
    static final String DFA2_acceptS =
        "\11\uffff\1\1\1\2\15\uffff";
    static final String DFA2_specialS =
        "\30\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1",
            "\1\2",
            "\1\5\7\uffff\2\3\1\4",
            "\1\6",
            "\1\7",
            "\1\10",
            "\1\12\1\uffff\1\11",
            "\1\13",
            "\2\14\1\15",
            "",
            "",
            "\1\12\1\uffff\1\11",
            "\1\16",
            "\1\17",
            "\1\20\1\uffff\1\21",
            "\1\22",
            "\2\23\1\24",
            "\1\12\1\uffff\1\11",
            "\1\20\1\uffff\1\21",
            "\1\25",
            "\1\26",
            "\1\20\1\uffff\1\21",
            "\1\27",
            "\1\20\1\uffff\1\21"
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "126:1: atom : ( concept_query_atom | binary_query_atom );";
        }
    }
 

    public static final BitSet FOLLOW_prog_in_parse47 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse49 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_prog79 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_9_in_prog82 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_atom_in_prog84 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_concept_query_atom_in_atom110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_binary_query_atom_in_atom118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_id_in_concept_query_atom147 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_concept_query_atom149 = new BitSet(new long[]{0x0000000000007010L});
    public static final BitSet FOLLOW_term_in_concept_query_atom151 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_concept_query_atom153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_id_in_binary_query_atom178 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_binary_query_atom180 = new BitSet(new long[]{0x0000000000007010L});
    public static final BitSet FOLLOW_term1_in_binary_query_atom182 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_9_in_binary_query_atom184 = new BitSet(new long[]{0x0000000000007010L});
    public static final BitSet FOLLOW_term2_in_binary_query_atom186 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_binary_query_atom188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_term1221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_term2246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_term_in_term271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_term_in_term279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_term_in_term287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_id_in_function_term319 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_function_term321 = new BitSet(new long[]{0x0000000000007000L});
    public static final BitSet FOLLOW_function_parameter_in_function_term323 = new BitSet(new long[]{0x0000000000000A00L});
    public static final BitSet FOLLOW_9_in_function_term326 = new BitSet(new long[]{0x0000000000007000L});
    public static final BitSet FOLLOW_function_parameter_in_function_term328 = new BitSet(new long[]{0x0000000000000A00L});
    public static final BitSet FOLLOW_11_in_function_term332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_variable_in_function_parameter354 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_constant_in_function_parameter359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_function_id381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_function_variable407 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_varname_in_function_variable413 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_function_constant441 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ALPHAVAR_in_function_constant443 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_function_constant445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_variable_term471 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_varname_in_variable_term477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_constant_term503 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ALPHAVAR_in_constant_term505 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_constant_term507 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_relationname531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_varname549 = new BitSet(new long[]{0x0000000000000002L});

}