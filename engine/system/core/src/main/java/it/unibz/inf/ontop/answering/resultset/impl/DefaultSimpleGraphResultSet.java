package it.unibz.inf.ontop.answering.resultset.impl;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
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

	private final List<RDFFact> results = new ArrayList<>();

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
                    .collect(ImmutableCollectors.toMap(ExtensionElem::getName, ExtensionElem::getExpr));
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
	public void addNewResult(RDFFact assertion)
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
    private List<RDFFact> processResults(OntopBindingSet bindingSet)
            throws OntopResultConversionException, OntopConnectionException {

        List<RDFFact> tripleAssertions = new ArrayList<>();


        for (ProjectionElemList peList : constructTemplate.getProjectionElemList()) {
            int size = peList.getElements().size();
            for (int i = 0; i < size / 3; i++) {

                ObjectConstant subjectConstant = (ObjectConstant) getConstant(peList.getElements().get(i * 3), bindingSet);
                IRIConstant propertyConstant = (IRIConstant) getConstant(peList.getElements().get(i * 3 + 1), bindingSet);
                RDFConstant objectConstant = (RDFConstant) getConstant(peList.getElements().get(i * 3 + 2), bindingSet);
                if (subjectConstant != null && propertyConstant != null && objectConstant != null)
                    tripleAssertions.add(RDFFact.createTripleFact(subjectConstant, propertyConstant, objectConstant));
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
            List<RDFFact> newTriples = processResults(tupleResultSet.next());
            if (!newTriples.isEmpty()) {
                results.addAll(newTriples);
                return true;
            }
        }
        return false;
    }

    @Override
    public RDFFact next() {
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
