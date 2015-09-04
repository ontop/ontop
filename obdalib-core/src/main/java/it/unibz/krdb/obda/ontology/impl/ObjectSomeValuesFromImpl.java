package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;

public class ObjectSomeValuesFromImpl implements ObjectSomeValuesFrom {

	private static final long serialVersionUID = 593821958539751283L;
	
	private final ObjectPropertyExpression property;
	private final String string;

	ObjectSomeValuesFromImpl(ObjectPropertyExpression property) {
		this.property = property;
		StringBuilder bf = new StringBuilder();
		bf.append("E");
		bf.append(property.toString());
		this.string =  bf.toString();
	}

	@Override
	public ObjectPropertyExpression getProperty() {
		return property;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectSomeValuesFromImpl) {
			ObjectSomeValuesFromImpl other = (ObjectSomeValuesFromImpl) obj;
			return property.equals(other.property);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public boolean isBottom() {
		return property.isBottom();
	}

	@Override
	public boolean isTop() {
		return property.isTop();
	}
}
