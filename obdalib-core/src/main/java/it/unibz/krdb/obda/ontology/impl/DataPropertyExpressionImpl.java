package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;

public class DataPropertyExpressionImpl implements DataPropertyExpression {

	private static final long serialVersionUID = 500873858691854474L;

	private final Predicate predicate;
	private final String string;
	
	private final DataSomeValuesFromImpl domain;
	private final DataPropertyRangeExpressionImpl range;

	public static final String owlTopDataPropertyIRI = "http://www.w3.org/2002/07/owl#topDataProperty";
	public static final String owlBottomDataPropertyIRI  = "http://www.w3.org/2002/07/owl#bottomDataProperty";
	
    static final DataPropertyExpression owlTopDataProperty = initialize(owlTopDataPropertyIRI); 
    static final DataPropertyExpression owlBottomDataProperty = initialize(owlBottomDataPropertyIRI); 
    
    private static DataPropertyExpression initialize(String uri) {
    	final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		Predicate prop = ofac.getDataPropertyPredicate(uri);
		return new DataPropertyExpressionImpl(prop);  	
    }

	DataPropertyExpressionImpl(Predicate p) {
		this.predicate = p;
		this.string = predicate.toString();
		
		this.domain = new DataSomeValuesFromImpl(this);
		this.range = new DataPropertyRangeExpressionImpl(this);
	}


	@Override
	public Predicate getPredicate() {
		return predicate;
	}
	
	@Override
	public DataSomeValuesFrom getDomain() {
		return domain;
	}

	@Override
	public DataPropertyRangeExpression getRange() {
		return range;
	}
	
	@Override 
	public boolean isBottom() {
		return predicate.getName().equals(owlBottomDataPropertyIRI);
	}
	
	@Override 
	public boolean isTop() {
		return predicate.getName().equals(owlTopDataPropertyIRI);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataPropertyExpressionImpl) {
			DataPropertyExpressionImpl other = (DataPropertyExpressionImpl) obj;
			return predicate.equals(other.predicate);
		}
		
		// the two types of properties share the same name space
		
		if (obj instanceof ObjectPropertyExpressionImpl) {
			ObjectPropertyExpressionImpl other = (ObjectPropertyExpressionImpl) obj;
			return (false == other.isInverse()) && predicate.equals(other.getPredicate());
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
