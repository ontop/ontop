package inf.unibz.it.dl.domain;

public class PropertyDomain implements ClassExpression {
	
	private PropertyExpression	property	= null;

	public PropertyDomain(PropertyExpression property) {
		this.property = property;
	}

	public PropertyExpression getProperty() {
		return property;
	}

	public String toString() {
		if (!(property instanceof InverseProperty)) {
			return "some " + property.toString();
		} else {
			return "some(" + property.toString() + ")";
		}
	}
}
