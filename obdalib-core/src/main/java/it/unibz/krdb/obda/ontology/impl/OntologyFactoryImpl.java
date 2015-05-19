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
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;


public class OntologyFactoryImpl implements OntologyFactory {

	private static final OntologyFactoryImpl instance = new OntologyFactoryImpl();

	private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	
	
	private OntologyFactoryImpl() {
		// NO-OP to make the default constructor private
	}
	
	public static OntologyFactory getInstance() {
		return instance;
	}

	@Override
	public ClassAssertion createClassAssertion(OClass concept, ObjectConstant object) {
		return new ClassAssertionImpl(concept, object);
	}

	@Override
	public Ontology createOntology() {
		return new OntologyImpl();
	}
	
	public ObjectPropertyAssertion createObjectPropertyAssertion(ObjectPropertyExpression role, ObjectConstant o1, ObjectConstant o2) {
		if (role.isInverse())
			return new ObjectPropertyAssertionImpl(role.getInverse(), o2, o1);
		else
			return new ObjectPropertyAssertionImpl(role, o1, o2);			
	}


	@Override
	public OClass createClass(String c) {
		Predicate classp = ofac.getClassPredicate(c);
		return new ClassImpl(classp);
	}

	@Override
	public ObjectPropertyExpression createObjectProperty(String uri) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return new ObjectPropertyExpressionImpl(prop);
	}

	
	@Override
	public DataPropertyExpression createDataProperty(String p) {
		Predicate prop = ofac.getDataPropertyPredicate(p);
		return new DataPropertyExpressionImpl(prop);
	}


	@Override
	public Datatype createDataType(Predicate.COL_TYPE type) {
		return new DatatypeImpl(ofac.getDatatypeFactory().getTypePredicate(type));
	}

	@Override
	public DataPropertyAssertion createDataPropertyAssertion(DataPropertyExpression attribute, ObjectConstant o1, ValueConstant o2) {
		return new DataPropertyAssertionImpl(attribute, o1, o2);
	}

	@Override
	public OClass getThing() {
		return ClassImpl.owlThing;
	}

	@Override
	public OClass getNothing() {
		return ClassImpl.owlNothing;
	}

	@Override
	public ObjectPropertyExpression getTopObjectProperty() {
		return ObjectPropertyExpressionImpl.owlTopObjectProperty;
	}

	@Override
	public ObjectPropertyExpression getBottomObjectProperty() {
		return ObjectPropertyExpressionImpl.owlBottomObjectProperty;
	}

	@Override
	public DataPropertyExpression getTopDataProperty() {
		return DataPropertyExpressionImpl.owlTopDataProperty;
	}

	@Override
	public DataPropertyExpression getBottomDataProperty() {
		return DataPropertyExpressionImpl.owlBottomDataProperty;
	}
	
}
