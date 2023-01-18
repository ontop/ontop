package it.unibz.inf.ontop.spec.ontology.impl;

import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.DataPropertyRangeExpression;

public class DataPropertyRangeExpressionImpl implements DataPropertyRangeExpression {

	private final DataPropertyExpression property;
	private final String string;

	DataPropertyRangeExpressionImpl(DataPropertyExpression property) {
		this.property = property;
		this.string = "E" + property;
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
