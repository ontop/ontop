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
import org.semanticweb.ontop.ontology.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AxiomToRuleTranslator {

    private static Logger log = LoggerFactory.getLogger(AxiomToRuleTranslator.class);

    private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		
	public static CQIE translate(Axiom axiom) throws UnsupportedOperationException {
		if (axiom instanceof SubDescriptionAxiom) {
			SubDescriptionAxiom subsumption = (SubDescriptionAxiom) axiom;
			Description descLeft = subsumption.getSub();
			Description descRight = subsumption.getSuper();

            /*
             * We don't need the translate the axioms of the range of the datatype property,
             * as they do not contribute to the reasoning.
             *
             * On the other hand, these axioms are used somewhere else for handing datatypes
             */
            if(isDataTypeRangeAxiom(subsumption)){
                // log.info(subsumption + " is a DataType range axiom");
                return null;
            }

            try {
                Function head = translate(descRight);
                Function body = translate(descLeft);
                return ofac.getCQIE(head, body);
            } catch (UnsupportedOperationException ex){
                throw new UnsupportedOperationException("Unsupported type of axiom: " + axiom.toString(),  ex);
            }
			

		} else {
			throw new UnsupportedOperationException("Unsupported type of axiom: " + axiom.toString());
		}
	}

    /**
     * Tests if the axiom is a datatype range axiom like range(age, xsd:int).
     * which in DL syntax is \exists age^- \subseteq xsd:int
     */
    private static boolean isDataTypeRangeAxiom(SubDescriptionAxiom axiom) {

        if (!(axiom.getSuper() instanceof DataType)) {
            return false;
        }

        Description sub = axiom.getSub();

        if (!(sub instanceof PropertySomeRestriction)) {
            return false;
        }

        PropertySomeRestriction some = (PropertySomeRestriction) sub;

        if (!some.isInverse()) {
            return false;
        }

        return true;

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
