package org.semanticweb.ontop.ontology.impl;

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

import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.ObjectConstant;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.DataPropertyAssertion;

import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.Datatype;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.ObjectPropertyAssertion;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;



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
