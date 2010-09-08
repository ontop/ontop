package inf.unibz.it.dl.domain;

public class ExistentialQuantification implements ClassExpression {
	
	private PropertyExpression property = null;
	
	public ExistentialQuantification(PropertyExpression property) {
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
