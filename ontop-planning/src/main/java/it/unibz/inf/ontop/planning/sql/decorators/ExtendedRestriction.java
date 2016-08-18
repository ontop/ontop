package it.unibz.inf.ontop.planning.sql.decorators;

import it.unibz.inf.ontop.planning.datatypes.Restriction;

public class ExtendedRestriction extends RestrictionDecorator {
    
    private ExtendedSignature extendedSignature;

    private ExtendedRestriction( Restriction component, ExtendedSignature decoratedSignature ) {

	super( component );
	this.extendedSignature = decoratedSignature;
    }

    public static class Builder{
	
	private ExtendedSignature signature;
	private Restriction component;
	
	public Builder(Restriction component){
	    this.component = component;
	}
	
	public Builder signature(ExtendedSignature signature){
	    this.signature = signature;
	    return this;
	}
	
	public ExtendedRestriction build(){
	    return new ExtendedRestriction(component, signature);
	}
    }

    // Decorations 
    public ExtendedSignature getExtendedSignature(){
	return this.extendedSignature;
    }
    
    @Override
    public String toString(){
	String decorated = "Restriction: " + super.toString();
	String extended = "ExtendedSignature: " + this.getExtendedSignature() + "\n";
	
	return decorated + extended;
    }
    
    
}
