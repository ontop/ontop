package it.unibz.krdb.obda.owlapi3;

/*
 * #%L
 * ontop-obdalib-owlapi3
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
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class OBDAModelSynchronizer {

	/***
	 * This method will declare all classes and proeprties in the owl ontology
	 * into the OBDA model. This is required
	 * 
	 * @param ontology
	 * @param model
	 * 
	 * @return the total number of declared entities.
	 */
	public static int declarePredicates(OWLOntology ontology, OBDAModel model) {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		int declarations = 0;
		for (OWLClass c : ontology.getClassesInSignature()) {
			model.declareClass(ofac.createClass(c.getIRI().toString()));
			declarations += 1;
		}
		for (OWLDataProperty c : ontology.getDataPropertiesInSignature()) {
			model.declareDataProperty(ofac.createDataProperty(c.getIRI().toString()));
			declarations += 1;
		}
		for (OWLObjectProperty c : ontology.getObjectPropertiesInSignature()) {
			model.declareObjectProperty(ofac.createObjectProperty(c.getIRI().toString()));
			declarations += 1;
		}

		return declarations;
	}
}
