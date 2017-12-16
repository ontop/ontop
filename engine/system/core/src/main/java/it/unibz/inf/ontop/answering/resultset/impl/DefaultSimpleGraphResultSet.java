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

import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyFactoryImpl;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

public class DefaultSimpleGraphResultSet implements SimpleGraphResultSet {

	private List<Assertion> results = new ArrayList<>();

	private final TupleResultSet tupleResultSet;

	private final ConstructTemplate constructTemplate;

	private Map<String, ValueExpr> extMap = null;

	//store results in case of describe queries
	private final boolean storeResults;

	public DefaultSimpleGraphResultSet(TupleResultSet results, ConstructTemplate template,
                                       boolean storeResult) throws OntopResultConversionException, OntopConnectionException {
		this.tupleResultSet = results;
		this.constructTemplate = template;
		this.storeResults = storeResult;
		processResultSet(tupleResultSet, constructTemplate);
	}

    private void processResultSet(TupleResultSet resSet, ConstructTemplate template)
            throws OntopResultConversionException, OntopConnectionException {
		if (storeResults) {
			//process current result set into local buffer, 
			//since additional results will be collected
			while (resSet.hasNext()) {
				results.addAll(processResults(resSet, template));
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
    private List<Assertion> processResults(TupleResultSet result,
                                           ConstructTemplate template)
            throws OntopResultConversionException, OntopConnectionException {
        List<Assertion> tripleAssertions = new ArrayList<>();
        List<ProjectionElemList> peLists = template.getProjectionElemList();

        Extension ex = template.getExtension();
        if (ex != null) {
            List<ExtensionElem> extList = ex.getElements();
            Map<String, ValueExpr> newExtMap = new HashMap<>();
            for (ExtensionElem anExtList : extList) {
                newExtMap.put(anExtList.getName(), anExtList.getExpr());
            }
            extMap = newExtMap;
        }

        Ontology onto = OntologyFactoryImpl.getInstance().createOntology();

        for (ProjectionElemList peList : peLists) {
            int size = peList.getElements().size();

            for (int i = 0; i < size / 3; i++) {

                ObjectConstant subjectConstant = (ObjectConstant) getConstant(peList.getElements().get(i * 3), result);
                Constant predicateConstant = getConstant(peList.getElements().get(i * 3 + 1), result);
                Constant objectConstant = getConstant(peList.getElements().get(i * 3 + 2), result);

                // A triple can only be constructed when none of bindings is missing
                if (subjectConstant == null || predicateConstant == null || objectConstant==null){
                    continue;
                }

                // Determines the type of assertion
                String predicateName = predicateConstant.getValue();
                Assertion assertion;
                try {
                    if (predicateName.equals(IriConstants.RDF_TYPE)) {
                        OClass oc = onto.classes().create(objectConstant.getValue());
                        assertion = onto.createClassAssertion(oc, subjectConstant);
                    }
                    else {
                        if ((objectConstant instanceof URIConstant) || (objectConstant instanceof BNode)) {
                            ObjectPropertyExpression ope = onto.objectProperties().create(predicateName);
                            assertion = onto.createObjectPropertyAssertion(ope,
                                    subjectConstant, (ObjectConstant) objectConstant);
                        }
                        else {
                            DataPropertyExpression dpe = onto.dataProperties().create(predicateName);
                            assertion = onto.createDataPropertyAssertion(dpe,
                                    subjectConstant, (ValueConstant) objectConstant);
                        }
                    }
                    if (assertion != null)
                        tripleAssertions.add(assertion);
                }
                catch (InconsistentOntologyException e) {
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
            List<Assertion> newTriples = processResults(tupleResultSet, constructTemplate);
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

    private Constant getConstant(ProjectionElem node, TupleResultSet resSet)
            throws OntopResultConversionException, OntopConnectionException {
        Constant constant = null;
        String node_name = node.getSourceName();
        ValueExpr ve = null;

        if (extMap != null) {
            ve = extMap.get(node_name);
            if (ve != null && ve instanceof Var)
                throw new OntopResultConversionException("Invalid query. Found unbound variable: " + ve);
        }

//		if (node_name.charAt(0) == '-') {
        if (ve instanceof org.eclipse.rdf4j.query.algebra.ValueConstant) {
            org.eclipse.rdf4j.query.algebra.ValueConstant vc = (org.eclipse.rdf4j.query.algebra.ValueConstant) ve;
            if (vc.getValue() instanceof IRI) {
                constant = TERM_FACTORY.getConstantURI(vc.getValue().stringValue());
            } else if (vc.getValue() instanceof Literal) {
                constant = TERM_FACTORY.getConstantLiteral(vc.getValue().stringValue());
            } else {
                constant = TERM_FACTORY.getConstantBNode(vc.getValue().stringValue());
            }
        } else {
            // constant = resSet.getConstant(node_name);
            // TODO(xiao): this fix is suspecious
            constant = resSet.next().getConstant(node_name);
        }
        return constant;
    }

    @Override
	public void close() throws OntopConnectionException {
		tupleResultSet.close();
	}
}
