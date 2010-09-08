grammar DatalogCQ;

@header {

package inf.unibz.it.obda.api.domain.ucq;

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

}

@lexer::header {
package inf.unibz.it.obda.api.domain.ucq;

import java.util.LinkedList;

}

@lexer::members {

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

       
    
}


@members {


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
	
	APIController apic = null;
	String _prefix = null;
	
	public void  setOBDAAPIController(APIController apicontroller) {
    	this.apic = apicontroller;
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
}

parse 	returns [boolean value] 
:	prog EOF { 
		//System.out.println(query_atoms.toString()); 
		$value = !error1; 
		//System.out.println("test" + $value);}
		}
	; catch  [RecognitionException ex] { 
//		reportError(ex); 
		$value = false; 
		throw ex; 
	}


prog   
      :	 atom (',' atom)*  
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

///////////////////////////////////////////////////////////////	

atom 	
	:	concept_query_atom { query_atoms.add($concept_query_atom.value); }
	| 	binary_query_atom  { query_atoms.add($binary_query_atom.value); }
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

///////////////////////////////////////////////////////////////

concept_query_atom 	returns [ConceptQueryAtom value] 
	:	function_id '(' term ')' {
			String aux = _prefix+$function_id.value;
			NamedConcept concept = null;
			URI uri = null;
			String prefix = "";
			if(aux.contains(":")){
				String auxarray[] = aux.split(":");
				prefix = auxarray[0];
				aux = auxarray[1];
				String url =apic.getCoupler().getUriForPrefix(prefix);
				if(url.endsWith("/")){
					uri = URI.create(url +aux);
				}else{
					uri = URI.create(url +"#"+aux);
				}
			}else{
				uri = URI.create(apic.getCurrentOntologyURI() +"#" + aux);
			}
			concept = new NamedConcept(prefix, uri);
			ConceptQueryAtom atom = new ConceptQueryAtom(concept, $term.value);
			value = atom;
			_prefix = "";
	}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex;  }


binary_query_atom returns [BinaryQueryAtom value]
	:	function_id '(' term1 ',' term2 ')' {
	
					String aux = _prefix+$function_id.value;
					NamedProperty relation = null;
					String rolename = aux;
					APICoupler coupler = apic.getCoupler();
					URI uri = null;
					String prefix = "";
					if(rolename.contains(":")){
						String auxarray[] = rolename.split(":");
						prefix = auxarray[0];
						rolename = auxarray[1];
						String url =apic.getCoupler().getUriForPrefix(prefix);
						if(url.endsWith("/")){
							uri = URI.create(url +rolename);
						}else{
							uri = URI.create(url +"#"+rolename);
						}
					}else{
						uri = URI.create(apic.getCurrentOntologyURI() +"#" + rolename);
					}
					if (coupler != null) {
						if ((coupler.isDatatypeProperty(URI.create(rolename)))||(coupler.isDatatypeProperty(uri))) {
							relation = new DataProperty(prefix, uri);
						} else if ((coupler.isObjectProperty(URI.create(rolename)))||(coupler.isObjectProperty(uri))) {
							relation = new ObjectProperty(prefix,uri);
						} else {
							throw new Exception(rolename + ": Impossible to detect if predicate is an ObjectProperty/DatatypeProperty. Verify that the OBDA API has a coupler and that it is able to answer for this property.");
						}
					} else {
						throw new Exception("No APICoupler has been defined. Define a APICoupler for the current APIController");
					}
					BinaryQueryAtom query_atom = new BinaryQueryAtom(relation, $term1.value, $term2.value);
					value = query_atom;
					_prefix ="";
	}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}
		catch [Exception ex] {
		error1 = true; 
		
		}

term1 returns [QueryTerm value] 
	: term {$value = $term.value;}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

term2 returns [QueryTerm value] 
	: term {$value = $term.value;}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

term 	returns [QueryTerm value]
	:	variable_term  {$value = $variable_term.value;}
	|	constant_term  {$value = $constant_term.value;}
	|	function_term  {$value = $function_term.value;}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}	
	

///////////////////////////////////////////////////////////////

function_term  returns [FunctionTerm value] 
	:	function_id '(' function_parameter (',' function_parameter)* ')' { 
		//ArrayList<QueryTerm> terms = new ArrayList<QueryTerm>();
		//while (!function_parameter_collector.isEmpty()) {
		//	terms.add(function_parameter_collector.pop());
		//}
		
		URI uri = null;
		String aux = _prefix+$function_id.value;
		String prefix = "";
		if(aux.contains(":")){
			String auxarray[] = aux.split(":");
			prefix = auxarray[0];
			aux = auxarray[1];
			String url =apic.getCoupler().getUriForPrefix(prefix);
			if(url.endsWith("/")){
				uri = URI.create(url +aux);
			}else{
				uri = URI.create(url +"#"+aux);
			}
		}else{
			uri = URI.create(apic.getCurrentOntologyURI() +"#" + aux);
		}
		FunctionTerm new_function = new FunctionTerm(uri, function_parameter_collector);
		value = new_function;
		function_parameter_collector = new ArrayList<QueryTerm>();
		_prefix = "";
		}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}
	
function_parameter 
	:	function_variable
	|	function_constant
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

function_id 	returns[String value]
	: (prefix':')?ALPHAVAR {$value = $ALPHAVAR.text;}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}
prefix	
	:ALPHAVAR {_prefix = $ALPHAVAR.text +":";}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

// This stands for variables inside functions		
function_variable 	returns[VariableTerm value]
	:
	('$'|'?') varname {$value = new VariableTerm($varname.value); function_parameter_collector.add($value); }
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}



// This stands for variables inside functions		
function_constant 	returns[ConstantTerm value]
	:
	'\'' ALPHAVAR '\''{$value = new ConstantTerm($ALPHAVAR.text); function_parameter_collector.add($value); }
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}


variable_term 	returns[VariableTerm value] 
	:
	('$'|'?') varname {$value = new VariableTerm($varname.value);}
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

constant_term 	returns[ConstantTerm value] 
	:
	'\'' ALPHAVAR '\''{$value = new ConstantTerm($ALPHAVAR.text); }
	; catch  [RecognitionException ex] { 
		//reportError(ex); 
		error1 = true; 
		throw ex; 
		}

relationname returns [String value] 
	:	ALPHAVAR {$value = $ALPHAVAR.text;}
	;

varname returns [String value] 
	:	ALPHAVAR {$value = $ALPHAVAR.text;}
	;
	

	
ALPHAVAR 	:	  (ALPHA | INT | CHAR)+ ; 	

fragment
CHAR 		:	('_'|'-'|'*'|'&'|'@'|'!'|'#'|'%'|'+'|'='|':'|'.');

fragment
ALPHA 		:   ('a'..'z'|'A'..'Z')+ ;

fragment
INT 		:   '0'..'9'+ ; 

WS  		:   (' '|'\t'|('\r'|'\r\n'))+ {skip();};


