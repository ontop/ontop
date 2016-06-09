package it.unibz.inf.ontop.planning.datatypes;

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
