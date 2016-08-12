package it.unibz.inf.ontop.planning.sql.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unibz.inf.ontop.planning.datatypes.Signature;
import it.unibz.krdb.obda.model.*;

public class ExtendedSignature extends SignatureDecorator{
    
    private final List<Variable> outVariables;
    private final List<ExtendedTerm> templates;  
    
    private ExtendedSignature(Builder builder) {
	
	super(builder.signature);
	
	this.outVariables = builder.outVariables;
	this.templates = builder.templates;
    }


    public static class Builder{
	private List<Variable> outVariables;
	private List<ExtendedTerm> templates;
	
	private Signature signature;
	
	public Builder(){
	    outVariables = new ArrayList<>();
	    templates = new ArrayList<>();
	}
	
	public Builder addOutVariableAndTerm( Variable outVariable, ExtendedTerm term ){
	    outVariables.add(outVariable);
	    templates.add(term);
	    return this;
	}
	
	public Builder signature( Signature component ){
	    this.signature = component;
	    return this;
	}
	
	
	public ExtendedSignature build(){
	    
	    ExtendedSignature instance = new ExtendedSignature(this);
	    
	    outVariables = new ArrayList<>();
	    templates = new ArrayList<>();
	
	    return instance;
	}
    };
    
    /**
     * 
     * @return The SPARQL variables
     */
    public List<Variable> getOutVariables(){
	return Collections.unmodifiableList(outVariables);
    }
    
    /**
     * 
     * @param outVariable
     * @return The template filling outVariable
     */
    public ExtendedTerm getTermOf( Variable outVariable ){
	return this.templates.get( indexOf(outVariable) );
    }
    
    private int indexOf( Variable v ){
	
	for( int i = 0; i < this.outVariables.size(); ++i ){
	    Variable w = this.outVariables.get(i);
	    
	    if( v.equals(w) ) return i;
	}
	
	return -1; // Not found
    }
    
    @Override
    public String toString(){
	StringBuilder builder = new StringBuilder();
	
	builder.append("Signature: " + super.toString() + "\n");
	builder.append("Out Variables: " + this.getOutVariables() + "\n");
	builder.append("Templates: " + this.templates + "\n");
	
	return builder.toString();
    }
};