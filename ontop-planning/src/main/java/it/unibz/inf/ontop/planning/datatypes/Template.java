package it.unibz.inf.ontop.planning.datatypes;

import it.unibz.inf.ontop.planning.sql.helpers.ExtendedTerm;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;

/**
 * A template is a pair f/n, where f is a function
 * symbol and n is the arity for f.
 * 
 * In this class, we use the URI representation of templates, e.g.
 * 
 * www.example.com/pagina/{}/paragrafo/{} corresponds to a binary
 * function example/2.
 * 
 * A template is NOT a Term! Indeed, a template 
 * does NOT contain variables, whereas a Term does.
 * 
 * @author Davide Lanti
 *
 */
public class Template{
    
    private String value;
    private int arity;
    
    public Template( Term term ){
	String templateString = takeTemplateString(term);
	this.value = templateString;
	this.arity = -1; // TODO Retrieve arities for all constructors
	
	if( term instanceof ExtendedTerm ){
	    this.arity = ((ExtendedTerm)term).getTermVariables().size();
	}
    }
    
    public Template(String value){
	this.value = value;
    }
    
    public String getValue(){
	return this.value;
    }
    
    public int getArity(){
	return this.arity;
    }

    @Override 
    public boolean equals(Object other) {
	if( this == other ) return true; // If they are the same object, then fine
	boolean result = false;
	if (other instanceof Template) {
	    Template that = (Template) other;
	    result = this.value.equals(that.value);
	}
	return result;
    }

    @Override
    public int hashCode(){
	return this.value.hashCode();
    }
        
    @Override
    public String toString(){
	return this.value;
    }
    
    private String takeTemplateString(Term t) {
	    assert (t instanceof Function) : "Assertion Failed: t is NOT an object or a data value\n";
	    
	    String result = null;
	    
	    String termString = t.toString();
	    if( termString.startsWith("URI") ){
		result = termString.substring( 0, termString.indexOf(",") ) + ")";
	    }
	    else{
		result = termString.substring( 0, termString.indexOf("(") );
	    }
	    
	    return result;
	}
};
