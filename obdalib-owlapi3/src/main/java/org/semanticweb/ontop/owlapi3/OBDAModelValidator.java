package org.semanticweb.ontop.owlapi3;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;

import org.semanticweb.ontop.io.TargetQueryVocabularyValidator;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.owlapi.model.OWLOntology;

/***
 * Validates an OBDAModel (mappings) against the vocabulary of an ontology. Used
 * by the Protege 4 plugin in
 * OBDAModelManager.OBDAPluginOWLModelManagerListener.handleChange
 * 
 * {@see TargetQueryValidator}
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */
public class OBDAModelValidator {

	private final OBDAModel obdaModel;
	private final TargetQueryVocabularyValidator validator;

	// TODO We should reduce the dependency to OWL-API to define the ontology.
	public OBDAModelValidator(OBDAModel obdaModel, OWLOntology ontology) {
		this.obdaModel = obdaModel;
		validator = new TargetQueryValidator(obdaModel);
	}

	public void run() throws Exception {
		Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappingTable = obdaModel.getMappings();
		for (URI datasourceUri : mappingTable.keySet()) {
			for (OBDAMappingAxiom mapping : mappingTable.get(datasourceUri)) {
				CQIE tq = (CQIE) mapping.getTargetQuery();
				boolean bSuccess = validator.validate(tq);
				if (!bSuccess) {
					throw new Exception("Found an invalid target query: " + tq.toString());
				}
			}
		}
	}
}
