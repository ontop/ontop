package inf.unibz.it.ucq.domain;

@Deprecated
public class Constant {
	//TODO ADD TYPES, URIS FOR OBJECT CONSTANTS AND XSDDATATYPES FOR OTHERS
	//TODO implement XSDTypes 
	String value = null;
	String type = null;
	
	public Constant(String value, String type) {
		this.type = type;
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
	
}
