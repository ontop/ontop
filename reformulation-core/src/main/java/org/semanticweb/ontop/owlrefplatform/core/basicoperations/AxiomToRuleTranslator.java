package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

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

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Axiom;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.PropertySomeRestriction;
import org.semanticweb.ontop.ontology.SubDescriptionAxiom;

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
