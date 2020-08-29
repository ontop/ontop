package it.unibz.inf.ontop.answering.resultset;

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

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.ontology.ABoxAssertionSupplier;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.InconsistentOntologyException;

public interface SimpleGraphResultSet extends GraphResultSet<OntopResultConversionException> {

    static Assertion getAssertion(ABoxAssertionSupplier builder, ObjectConstant subjectConstant, Constant predicateConstant, Constant objectConstant) throws OntopResultConversionException {
        Assertion assertion = null;
        // A triple can only be constructed when none of bindings is missing
        if (subjectConstant != null && predicateConstant != null && objectConstant != null) {
            // Determines the type of assertion
            String predicateName = predicateConstant.getValue();
            try {
                if (predicateName.equals(RDF.TYPE.getIRIString())) {
                    assertion = builder.createClassAssertion(objectConstant.getValue(), subjectConstant);
                } else {
                    if ((objectConstant instanceof IRIConstant) || (objectConstant instanceof BNode)) {
                        assertion = builder.createObjectPropertyAssertion(predicateName,
                                subjectConstant, (ObjectConstant) objectConstant);
                    } else {
                        assertion = builder.createDataPropertyAssertion(predicateName,
                                subjectConstant, (RDFLiteralConstant) objectConstant);
                    }
                }
            } catch (InconsistentOntologyException e) {
                throw new OntopResultConversionException("InconsistentOntologyException: " +
                        predicateName + " " + subjectConstant + " " + objectConstant);
            }
        }
        return assertion;
    }

    int getFetchSize() throws OntopConnectionException;

    /**
     * TODO: remove this hack
     */
	@Deprecated
    void addNewResult(Assertion assertion);

}
