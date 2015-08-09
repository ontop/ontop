package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;

public class DataPropertyExpressionImpl implements DataPropertyExpression {

	private static final long serialVersionUID = 500873858691854474L;

	private final Predicate predicate;
	private final String string;
	
	private final DataSomeValuesFromImpl domain;
	private final DataPropertyRangeExpressionImpl range;

	public static final String owlTopDataPropertyIRI = "http://www.w3.org/2002/07/owl#topDataProperty";
	public static final String owlBottomDataPropertyIRI  = "http://www.w3.org/2002/07/owl#bottomDataProperty";
	
	private static final  Datatype rdfsLiteral; 
    static final DataPropertyExpression owlTopDataProperty; 
    public static final DataPropertyExpression owlBottomDataProperty; 
	
	static {
		OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	    rdfsLiteral = new DatatypeImpl(ofac.getDatatypeFactory().getTypePredicate(COL_TYPE.LITERAL));   	

		Predicate prop = ofac.getDataPropertyPredicate(owlTopDataPropertyIRI);
		owlTopDataProperty = new DataPropertyExpressionImpl(prop);  	
	    
		Predicate propbot = ofac.getDataPropertyPredicate(owlBottomDataPropertyIRI);
		owlBottomDataProperty = new DataPropertyExpressionImpl(propbot);  	
	}
	

	DataPropertyExpressionImpl(Predicate p) {
		this.predicate = p;
		this.string = predicate.toString();		

		this.domain = new DataSomeValuesFromImpl(this, rdfsLiteral);
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
		
		// object and data properties share the same name space
		
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
