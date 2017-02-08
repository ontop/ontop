package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-obdalib-owlapi
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

import it.unibz.inf.ontop.io.TargetQueryVocabularyValidator;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;

import java.util.List;

/***
 * Validates an OBDAModel (mappings) against the vocabulary of an ontology
 * and adds type information to the mapping predicates
 *
 * Used by the Protege plugin in
 * OBDAModelManager.OBDAPluginOWLModelManagerListener.handleChange
 * 
 * {@see TargetQueryValidator}
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */

//TODO: move to a more appropriate package

public class OBDAModelValidator {

	public static void validate(OBDAModel obdaModel, ImmutableOntologyVocabulary vocabulary) throws Exception {

		 TargetQueryVocabularyValidator validator = new TargetQueryValidator(vocabulary);

		 for (OBDAMappingAxiom mapping : obdaModel.getMappings()) {
			 List<Function> tq = mapping.getTargetQuery();
			 boolean bSuccess = validator.validate(tq);
			 if (!bSuccess) {
				 throw new Exception("Found an invalid target query: " + tq.toString());
			 }
		 }
		}
}
