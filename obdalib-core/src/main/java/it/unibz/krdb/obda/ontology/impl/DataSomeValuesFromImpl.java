package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;

public class DataSomeValuesFromImpl implements DataSomeValuesFrom {

	private static final long serialVersionUID = 593821958539751283L;
	
	private final DataPropertyExpression property;
	private final Datatype filler;
	private final String string;

	DataSomeValuesFromImpl(DataPropertyExpression property, Datatype filler) {
		this.property = property;
		this.filler = filler;
		StringBuilder bf = new StringBuilder();
		bf.append("E")
		  .append(property.toString())
	      .append(".")
	      .append(filler.toString());
		this.string =  bf.toString();
	}

	@Override
	public DataPropertyExpression getProperty() {
		return property;
	}

	@Override
	public Datatype getDatatype() {
		return filler;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSomeValuesFromImpl) {
			DataSomeValuesFromImpl other = (DataSomeValuesFromImpl) obj;
			return property.equals(other.property) && filler.equals(other.filler);
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
