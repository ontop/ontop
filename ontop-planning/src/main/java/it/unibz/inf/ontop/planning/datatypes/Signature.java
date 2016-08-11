package it.unibz.inf.ontop.planning.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Signature is a tuple (t1, ..., tn) of Templates
 * 
 * @author Davide Lanti
 *
 */
public class Signature {
    
    private final List<Template> templates = new ArrayList<>();
            
    private Signature(Builder builder) {
	this.templates.addAll(builder.templates);
	builder.templates.clear();
    }
    
    public Template getTemplateOfIndex( int index ){
	
	Template result = null;
	
	try{
	    result = this.templates.get(index);
	}
	catch( IndexOutOfBoundsException e ){
	    System.out.println("Templates: " + templates);
	    System.out.println("Index: " + index);
	}
	
	return result;
    }
    
    public int numTemplates(){
	return templates.size();
    }
    
    public List<Template> getTemplates(){
	return Collections.unmodifiableList(templates);
    }
    
    public static class Builder{
	private List<Template> templates = new ArrayList<>();
	
	public Builder template(Template t){
	    this.templates.add(t);
	    return this;
	}
	
	
	public Signature build(){
	    Signature result = new Signature(this);
	    return result;
	}
	
	public boolean empty(){
	    return this.templates.size() == 0;
	}
    };
    
    @Override 
    public boolean equals(Object other) {
	if( this == other ) return true; // If they are the same object, then fine
	boolean result = false;
	if (other instanceof Signature) {
	    Signature that = (Signature) other;
	    result = this.templates.equals(that.templates);
	}
	return result;
    }

    @Override
    public int hashCode(){
	return this.templates.hashCode();
    }
    
    @Override
    public String toString(){
	return this.templates.toString();
    }
};