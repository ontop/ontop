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
import it.unibz.krdb.obda.ontology.AssertionFactory;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.InconsistentOntologyException;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;

/**
 * factory for ABox assertions 
 * 
 * IMPORTANT: this factory does NOT check whether the class / property has been declared
 * 
 * USES: OntologyFactoryImpl (and so, checks for top / bottom concept / property
* 			@see rules [C4], [O4], [D4]) 
  * 
 * @author Roman Kontchakov
 *
 */

public class AssertionFactoryImpl implements AssertionFactory {

	private static final AssertionFactoryImpl instance = new AssertionFactoryImpl();

	private final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
			
	private AssertionFactoryImpl() {
		// NO-OP to make the default constructor private
	}
	
	public static AssertionFactory getInstance() {
		return instance;
	}
	
	/**
	 * creates a class assertion (without checking any vocabulary)
	 * 
	 * @return null if it is top class ([C4], @see OntologyFactoryImpl)
	 * @throws InconsistentOntologyException if it is the bottom class ([C4], @see OntologyFactoryImpl)
	 */

	@Override
	public ClassAssertion createClassAssertion(String className, ObjectConstant o) throws InconsistentOntologyException {
		Predicate classp = fac.getClassPredicate(className);
		OClass oc = new ClassImpl(classp);
		return ofac.createClassAssertion(oc, o);
	}

	/**
	 * creates an object property assertion (without checking any vocabulary)
	 * 
	 * @return null if it is top object property ([O4], @see OntologyFactoryImpl)
	 * @throws InconsistentOntologyException if it is the bottom object property ([O4], @see OntologyFactoryImpl)
	 */

	@Override
	public ObjectPropertyAssertion createObjectPropertyAssertion(String propertyName, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException {
		Predicate prop = fac.getObjectPropertyPredicate(propertyName);
		ObjectPropertyExpression ope = new ObjectPropertyExpressionImpl(prop);
		return ofac.createObjectPropertyAssertion(ope, o1, o2);
	}

	/**
	 * creates a data property assertion (without checking any vocabulary)
	 * 
	 * @return null if it is top data property ([D4], @see OntologyFactoryImpl)
	 * @throws InconsistentOntologyException if it is the bottom data property ([D4], @see OntologyFactoryImpl)
	 */
	
	@Override
	public DataPropertyAssertion createDataPropertyAssertion(String propertyName, ObjectConstant o1, ValueConstant o2) throws InconsistentOntologyException {
		Predicate prop = fac.getDataPropertyPredicate(propertyName);
		DataPropertyExpression dpe = new DataPropertyExpressionImpl(prop);
		return ofac.createDataPropertyAssertion(dpe, o1, o2);
	}

}
