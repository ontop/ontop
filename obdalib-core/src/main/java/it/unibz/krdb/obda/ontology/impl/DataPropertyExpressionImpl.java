package it.unibz.krdb.obda.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;

/**
 * Represents DataPropertyExpression from the OWL 2 QL Specification
 * 
 * DataPropertyExpression := DataProperty
 * 
 * Support for owl:topDataProperty and owl:bottomDataProperty
 * 
 * @author Roman Kontchakov
 *
 */

public class DataPropertyExpressionImpl implements DataPropertyExpression {

	private static final long serialVersionUID = 500873858691854474L;

	private final Predicate predicate;
	private final String string;
	
	private final DataSomeValuesFromImpl domain;
	private final DataPropertyRangeExpressionImpl range;

	public static final String owlTopDataPropertyIRI = "http://www.w3.org/2002/07/owl#topDataProperty";
	public static final String owlBottomDataPropertyIRI  = "http://www.w3.org/2002/07/owl#bottomDataProperty";
	
    static final DataPropertyExpression owlTopDataProperty; 
    public static final DataPropertyExpression owlBottomDataProperty; 
	
	static {
		OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

		Predicate prop = ofac.getDataPropertyPredicate(owlTopDataPropertyIRI);
		owlTopDataProperty = new DataPropertyExpressionImpl(prop);  	
	    
		Predicate propbot = ofac.getDataPropertyPredicate(owlBottomDataPropertyIRI);
		owlBottomDataProperty = new DataPropertyExpressionImpl(propbot);  	
	}
	

	DataPropertyExpressionImpl(Predicate p) {
		this.predicate = p;
		this.string = predicate.toString();		

		this.domain = new DataSomeValuesFromImpl(this, DatatypeImpl.rdfsLiteral);
		this.range = new DataPropertyRangeExpressionImpl(this);
	}


	@Override
	public Predicate getPredicate() {
		return predicate;
	}
	
	@Override
	public DataSomeValuesFrom getDomainRestriction(Datatype datatype) {
		if (datatype.equals(DatatypeImpl.rdfsLiteral))
			return domain;
		return new DataSomeValuesFromImpl(this, datatype);
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
