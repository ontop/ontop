package it.unibz.inf.ontop.planning.sql.decorators;

import it.unibz.inf.ontop.planning.datatypes.Signature;
import it.unibz.inf.ontop.planning.datatypes.Template;

//Decorator pattern
public class SignatureDecorator{

    private final Signature component;
    
    
    public SignatureDecorator( Signature component ){
	this.component = component;
    }
    
    // Decorator wrapping
    public Template getTemplateOfIndex( int index ){
	
	return component.getTemplateOfIndex(index);
    }
    
    @Override 
    public boolean equals(Object other) {
	if( other instanceof SignatureDecorator )
	    return component.equals( ((SignatureDecorator) other).component );
	else return false;
    }
    
    @Override
    public int hashCode(){
	return component.hashCode();
    }
    
    @Override
    public String toString(){
	return component.toString();
    }
};