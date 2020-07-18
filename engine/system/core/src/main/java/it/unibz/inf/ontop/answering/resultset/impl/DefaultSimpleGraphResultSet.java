package it.unibz.inf.ontop.answering.resultset.impl;

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

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.ontology.ABoxAssertionSupplier;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.InconsistentOntologyException;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class DefaultSimpleGraphResultSet implements SimpleGraphResultSet {

	private final List<Assertion> results = new ArrayList<>();

	private final TupleResultSet tupleResultSet;

	private final ConstructTemplate constructTemplate;

	private final ImmutableMap<String, ValueExpr> extMap;

	// store results in case of describe queries
	private final boolean storeResults;
    private final QueryLogger queryLogger;
    private final TermFactory termFactory;
    private final org.apache.commons.rdf.api.RDF rdfFactory;

    public DefaultSimpleGraphResultSet(TupleResultSet tupleResultSet, ConstructTemplate constructTemplate,
                                       boolean storeResults, QueryLogger queryLogger, TermFactory termFactory,
                                       org.apache.commons.rdf.api.RDF rdfFactory) throws OntopResultConversionException, OntopConnectionException {
		this.tupleResultSet = tupleResultSet;
		this.constructTemplate = constructTemplate;
        this.queryLogger = queryLogger;
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        Extension ex = constructTemplate.getExtension();
        if (ex != null) {
            extMap = ex.getElements().stream()
                    .collect(ImmutableCollectors.toMap(e -> e.getName(), e -> e.getExpr()));
        }
        else
            extMap = null;

        this.storeResults = storeResults;
        if (storeResults) {
            //process current result set into local buffer,
            //since additional results will be collected
            while (tupleResultSet.hasNext()) {
                results.addAll(processResults(tupleResultSet.next()));
            }
        }
	}


    @Override
    public int getFetchSize() throws OntopConnectionException {
        return tupleResultSet.getFetchSize();
    }

    @Override
	public void addNewResult(Assertion assertion)
	{
		results.add(assertion);
	}

	/**
	 * The method to actually process the current result set Row.
	 * Construct a list of assertions from the current result set row.
	 * In case of describe it is called to process and store all 
	 * the results from a resultset.
	 * In case of construct it is called upon next, to process
	 * the only current result set.
	 */
    private List<Assertion> processResults(OntopBindingSet bindingSet)
            throws OntopResultConversionException, OntopConnectionException {

        List<Assertion> tripleAssertions = new ArrayList<>();
        ABoxAssertionSupplier builder = OntologyBuilderImpl.assertionSupplier(rdfFactory);

        for (ProjectionElemList peList : constructTemplate.getProjectionElemList()) {
            int size = peList.getElements().size();

            for (int i = 0; i < size / 3; i++) {

                ObjectConstant subjectConstant = (ObjectConstant) getConstant(peList.getElements().get(i * 3), bindingSet);
                Constant predicateConstant = getConstant(peList.getElements().get(i * 3 + 1), bindingSet);
                Constant objectConstant = getConstant(peList.getElements().get(i * 3 + 2), bindingSet);

                // A triple can only be constructed when none of bindings is missing
                if (subjectConstant == null || predicateConstant == null || objectConstant==null) {
                    continue;
                }

                // Determines the type of assertion
                String predicateName = predicateConstant.getValue();
                try {
                    Assertion assertion;
                    if (predicateName.equals(RDF.TYPE.getIRIString())) {
                        assertion = builder.createClassAssertion(objectConstant.getValue(), subjectConstant);
                    }
                    else {
                        if ((objectConstant instanceof IRIConstant) || (objectConstant instanceof BNode)) {
                            assertion = builder.createObjectPropertyAssertion(predicateName,
                                    subjectConstant, (ObjectConstant) objectConstant);
                        }
                        else {
                            assertion = builder.createDataPropertyAssertion(predicateName,
                                    subjectConstant, (RDFLiteralConstant) objectConstant);
                        }
                    }
                    if (assertion != null)
                        tripleAssertions.add(assertion);
                }
                catch (InconsistentOntologyException e) {
                    queryLogger.declareConversionException(e);
                    throw new OntopResultConversionException("InconsistentOntologyException: " +
                            predicateName + " " + subjectConstant + " " + objectConstant);
                }
            }
        }
        return tripleAssertions;
    }

    @Override
    public boolean hasNext() throws OntopConnectionException, OntopResultConversionException {
        if (results.size() > 0)
            return true;
        //in case of describe, we return the collected results list information
        if (storeResults)
            return false;
        // construct
        while(tupleResultSet.hasNext()) {
            List<Assertion> newTriples = processResults(tupleResultSet.next());
            if (!newTriples.isEmpty()) {
                results.addAll(newTriples);
                return true;
            }
        }
        return false;
    }

    @Override
    public Assertion next() {
        if (results.size() > 0)
            return results.remove(0);
        else
            throw new NoSuchElementException("Please call hasNext() before calling next()");
    }

    private Constant getConstant(ProjectionElem node, OntopBindingSet bindingSet)
            throws OntopResultConversionException, OntopConnectionException {
        Constant constant = null;
        String node_name = node.getSourceName();
        ValueExpr ve = null;

        if (extMap != null) {
            ve = extMap.get(node_name);
        }

//		if (node_name.charAt(0) == '-') {
        if (ve instanceof ValueConstant) {
            ValueConstant vc = (ValueConstant) ve;
            if (vc.getValue() instanceof IRI) {
                constant = termFactory.getConstantIRI(rdfFactory.createIRI(vc.getValue().stringValue()));
            }
            else if (vc.getValue() instanceof Literal) {
                constant = termFactory.getRDFLiteralConstant(vc.getValue().stringValue(), XSD.STRING);
            }
            else {
                constant = termFactory.getConstantBNode(vc.getValue().stringValue());
            }
        }
        else if (ve instanceof BNodeGenerator) {
            // See https://www.w3.org/TR/sparql11-query/#tempatesWithBNodes
            String rowId = bindingSet.getRowUUIDStr();

            String label = Optional.ofNullable(((BNodeGenerator) ve).getNodeIdExpr())
                    // If defined, we expected the b-node label to be constant (as appearing in the CONSTRUCT block)
                    .filter(e -> e instanceof ValueConstant)
                    .map(v -> ((ValueConstant) v).getValue().stringValue())
                    .map(s -> s + rowId)
                    .orElseGet(() -> node_name + rowId);

            constant = termFactory.getConstantBNode(label);
        }
        else {
            constant = bindingSet.getConstant(node_name);
        }
        return constant;
    }

    @Override
	public void close() throws OntopConnectionException {
		tupleResultSet.close();
	}
}
