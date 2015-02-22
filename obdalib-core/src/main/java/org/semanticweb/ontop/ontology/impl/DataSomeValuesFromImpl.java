package org.semanticweb.ontop.ontology.impl;

import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.DataSomeValuesFrom;

public class DataSomeValuesFromImpl implements DataSomeValuesFrom {

	private static final long serialVersionUID = 593821958539751283L;
	
	private final DataPropertyExpression property;
	private final String string;

	DataSomeValuesFromImpl(DataPropertyExpression property) {
		this.property = property;
		StringBuilder bf = new StringBuilder();
		bf.append("E");
		bf.append(property.toString());
		this.string =  bf.toString();
	}

	@Override
	public DataPropertyExpression getProperty() {
		return property;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSomeValuesFromImpl) {
			DataSomeValuesFromImpl other = (DataSomeValuesFromImpl) obj;
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
	public boolean isNothing() {
		return property.isBottom();
	}

	@Override
	public boolean isThing() {
		return property.isTop();
	}
}
