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

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.ImmutableOntologyVocabulary;
import it.unibz.krdb.obda.ontology.InconsistentOntologyException;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;

/**
 * 
 * @author Roman Kontchakov
 *
 */


public class OntologyFactoryImpl implements OntologyFactory {

	private static final OntologyFactoryImpl instance = new OntologyFactoryImpl();
	
	private OntologyFactoryImpl() {
		// NO-OP to make the default constructor private
	}
	
	public static OntologyFactory getInstance() {
		return instance;
	}

	@Override
	public OntologyVocabulary createVocabulary() {
		return new OntologyVocabularyImpl();
	}
	
	@Override
	public Ontology createOntology(ImmutableOntologyVocabulary vb) {
		return new OntologyImpl((OntologyVocabularyImpl)vb);
	}
	
	
	
	@Override
	public ClassAssertion createClassAssertion(OClass ce, ObjectConstant object) throws InconsistentOntologyException {
		if (ce.isTop())
			return null;
		if (ce.isBottom())
			throw new InconsistentOntologyException();	
		
		return new ClassAssertionImpl(ce, object);
	}

	public ObjectPropertyAssertion createObjectPropertyAssertion(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException {
		if (ope.isTop())
			return null;
		if (ope.isBottom())
			throw new InconsistentOntologyException();
		
		if (ope.isInverse())
			return new ObjectPropertyAssertionImpl(ope.getInverse(), o2, o1);
		else
			return new ObjectPropertyAssertionImpl(ope, o1, o2);			
	}

	@Override
	public DataPropertyAssertion createDataPropertyAssertion(DataPropertyExpression dpe, ObjectConstant o1, ValueConstant o2) throws InconsistentOntologyException {
		if (dpe.isTop())
			return null;
		if (dpe.isBottom())
			throw new InconsistentOntologyException();
		
		return new DataPropertyAssertionImpl(dpe, o1, o2);
	}
}
