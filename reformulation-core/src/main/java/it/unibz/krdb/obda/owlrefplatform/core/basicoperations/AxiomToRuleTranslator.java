package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;

public class AxiomToRuleTranslator {
	
	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		
	public static CQIE translate(Axiom axiom) throws UnsupportedOperationException {
		if (axiom instanceof SubDescriptionAxiom) {
			SubDescriptionAxiom subsumption = (SubDescriptionAxiom) axiom;
			Description descLeft = subsumption.getSub();
			Description descRight = subsumption.getSuper();
			
			Function head = translate(descRight);
			Function body = translate(descLeft);
			
			return ofac.getCQIE(head, body);
		} else {
			throw new UnsupportedOperationException("Unsupported type of axiom: " + axiom.toString());
		}
	}
		
	public static Function translate(Description description) throws UnsupportedOperationException {
		final Variable varX = ofac.getVariable("x");
		final Variable varY = ofac.getVariable("y");
		if (description instanceof OClass) {
			OClass klass = (OClass) description;
			return ofac.getFunction(klass.getPredicate(), varX);
		} else if (description instanceof Property) {
			Property property = (Property) description;
			if (property.isInverse()) {
				return ofac.getFunction(property.getPredicate(), varY, varX);
			} else {
				return ofac.getFunction(property.getPredicate(), varX, varY);
			}
		} else if (description instanceof PropertySomeRestriction) {
			PropertySomeRestriction property = (PropertySomeRestriction) description;
			if (property.isInverse()) {
				return ofac.getFunction(property.getPredicate(), varY, varX);
			} else {
				return ofac.getFunction(property.getPredicate(), varX, varY);
			}
		} else {
			throw new UnsupportedOperationException("Unsupported type of description: " + description.toString());
		}
	}
}
