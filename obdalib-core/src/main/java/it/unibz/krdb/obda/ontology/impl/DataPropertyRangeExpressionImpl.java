package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;

public class DataPropertyRangeExpressionImpl implements DataPropertyRangeExpression {

	private static final long serialVersionUID = 4159090478171795156L;

	private final DataPropertyExpression property;
	private final String string;

	DataPropertyRangeExpressionImpl(DataPropertyExpression property) {
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
		if (obj instanceof DataPropertyRangeExpressionImpl) {
			DataPropertyRangeExpressionImpl other = (DataPropertyRangeExpressionImpl) obj;
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

}
