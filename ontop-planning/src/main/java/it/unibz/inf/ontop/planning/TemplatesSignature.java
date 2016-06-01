package it.unibz.inf.ontop.planning;

public class TemplatesSignature {
    public final String value;
    
    private TemplatesSignature(String value){
	this.value = value;
    }
    
    public static TemplatesSignature makeTemplatesSignature(String value){
	return new TemplatesSignature(value);
    }
        
    @Override 
    public boolean equals(Object other) {
	if( this == other ) return true; // If they are the same object, then fine
	boolean result = false;
	if (other instanceof TemplatesSignature) {
	    TemplatesSignature that = (TemplatesSignature) other;
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
}
