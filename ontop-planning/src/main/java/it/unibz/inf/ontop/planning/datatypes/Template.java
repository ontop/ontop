package it.unibz.inf.ontop.planning.datatypes;

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
    
    public Template(String value){
	this.value = value;
    }
    
    public String getValue(){
	return this.value;
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
};
