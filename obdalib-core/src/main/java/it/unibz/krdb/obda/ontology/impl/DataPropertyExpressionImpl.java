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
	private final String name;
	
	private final boolean isTop, isBottom;
	
	private final DataSomeValuesFromImpl domain;
	private final DataPropertyRangeExpressionImpl range;

	public static final String owlTopDataPropertyIRI = "http://www.w3.org/2002/07/owl#topDataProperty";
	public static final String owlBottomDataPropertyIRI  = "http://www.w3.org/2002/07/owl#bottomDataProperty";
	
	private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
    public static final DataPropertyExpression owlTopDataProperty = new DataPropertyExpressionImpl(owlTopDataPropertyIRI);
    public static final DataPropertyExpression owlBottomDataProperty = new DataPropertyExpressionImpl(owlBottomDataPropertyIRI);
	

	DataPropertyExpressionImpl(String name) {
		this.predicate = ofac.getDataPropertyPredicate(name);
		this.name = name;		
		this.isTop = name.equals(owlTopDataPropertyIRI);
		this.isBottom = name.equals(owlBottomDataPropertyIRI);

		this.domain = new DataSomeValuesFromImpl(this, DatatypeImpl.rdfsLiteral);
		this.range = new DataPropertyRangeExpressionImpl(this);
	}


	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public String getName() {
		return name;
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
		return isBottom;
	}
	
	@Override 
	public boolean isTop() {
		return isTop;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof DataPropertyExpressionImpl) {
			DataPropertyExpressionImpl other = (DataPropertyExpressionImpl) obj;
			return name.equals(other.name);
		}
		
		// object and data properties share the same name space	
		if (obj instanceof ObjectPropertyExpressionImpl) {
			ObjectPropertyExpressionImpl other = (ObjectPropertyExpressionImpl) obj;
			return (false == other.isInverse()) && name.equals(other.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}

}
