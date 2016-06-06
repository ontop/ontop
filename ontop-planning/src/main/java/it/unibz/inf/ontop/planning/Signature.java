package it.unibz.inf.ontop.planning;

import java.util.ArrayList;
import java.util.List;

public class Signature {
    
    private final List<Template> templates = new ArrayList<>();
        
    private Signature(List<Template> templates) {
	this.templates.addAll(templates);
    }
    
    public static class Builder{
	private List<Template> templates = new ArrayList<>();
	
	public Builder template(Template t){
	    this.templates.add(t);
	    return this;
	}
	
	public Builder Builder(){
	    return this;
	}
	
	public Signature build(){
	    Signature result = new Signature(templates);
	    templates.clear();
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

class Template{
    
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
