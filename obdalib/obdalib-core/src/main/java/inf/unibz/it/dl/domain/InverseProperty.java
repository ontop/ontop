package inf.unibz.it.dl.domain;

public class InverseProperty implements PropertyExpression {
	ObjectProperty prop = null;
	
	public InverseProperty(ObjectProperty property) {
		this.prop = property;
	}
	
	public ObjectProperty getProperty() {
		return prop;
	}
	
	public String toString() {
		return "inverseof " + prop.toString();
	}
}
