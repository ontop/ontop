package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ResultSet;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.BNodeConstantImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.Template;

public class QuestGraphResultSet implements GraphResultSet {

	private List<List<Assertion>> results = new ArrayList<List<Assertion>>();

	private TupleResultSet tupleResultSet;

	private Template template;

	//store results in case of describe queries
	private boolean storeResults = false;

	private OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public QuestGraphResultSet(TupleResultSet results, Template template,
			boolean storeResult) throws OBDAException {
		this.tupleResultSet = results;
		this.template = template;
		this.storeResults = storeResult;
		processResultSet(tupleResultSet, template);
	}

	@Override
	public TupleResultSet getTupleResultSet() {
		return tupleResultSet;
	}

	
	private void processResultSet(TupleResultSet resSet, Template template)
			throws OBDAException {
		if (storeResults) {
			//process current result set into local buffer, 
			//since additional results will be collected
			while (resSet.nextRow()) {
				this.results.add(processResults(resSet, template));
			}
		}
	}
	
	@Override
	public void addNewResultSet(List<Assertion> result)
	{
		results.add(result);
	}

	@Override
	public Template getTemplate() {
		return template;
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
			Template template) throws OBDAException {
		List<Assertion> tripleAssertions = new ArrayList<Assertion>();
		for (Triple triple : template.getTriples()) {

			Constant subjectConstant = getConstant(triple.getSubject(), result);
			Constant predicateConstant = getConstant(triple.getPredicate(),
					result);
			Constant objectConstant = getConstant(triple.getObject(), result);

			// Determines the type of assertion
			String predicateName = predicateConstant.getValue();
			if (predicateName.equals(OBDAVocabulary.RDF_TYPE)) {
				Predicate concept = dfac.getClassPredicate(objectConstant
						.getValue());
				ClassAssertion ca = ofac.createClassAssertion(concept,
						(ObjectConstant) subjectConstant);
				tripleAssertions.add(ca);
			} else {
				if (objectConstant instanceof URIConstant) {
					Predicate role = dfac
							.getObjectPropertyPredicate(predicateName);
					ObjectPropertyAssertion op = ofac
							.createObjectPropertyAssertion(role,
									(ObjectConstant) subjectConstant,
									(ObjectConstant) objectConstant);
					tripleAssertions.add(op);
				} else if (objectConstant instanceof BNodeConstantImpl) {
					Predicate role = dfac
							.getObjectPropertyPredicate(predicateName);
					ObjectPropertyAssertion op = ofac
							.createObjectPropertyAssertion(role,
									(ObjectConstant) subjectConstant,
									(ObjectConstant) objectConstant);
					tripleAssertions.add(op);
				} else {
					Predicate attribute = dfac
							.getDataPropertyPredicate(predicateName);
					DataPropertyAssertion dp = ofac
							.createDataPropertyAssertion(attribute,
									(ObjectConstant) subjectConstant,
									(ValueConstant) objectConstant);
					tripleAssertions.add(dp);
				}
			}
		}
		return (tripleAssertions);
	}

	@Override
	public boolean hasNext() throws OBDAException {
		//in case of describe, we return the collected results list information
		if (storeResults) {
			return results.size() != 0;
		} else {
			//in case of construct advance the result set cursor on hasNext
			return tupleResultSet.nextRow();
		}
	}

	@Override
	public List<Assertion> next() throws OBDAException {
		//if we collect results, then remove and return the next one in the list
		if (results.size() > 0) {
			return results.remove(0);
		} else {
			//otherwise we need to process the unstored result
			return processResults(tupleResultSet, template);
		}
	}

	private Constant getConstant(Node node, TupleResultSet resSet)
			throws OBDAException {
		Constant constant = null;
		if (node instanceof Node_Variable) {
			String columnName = ((Node_Variable) node).getName();
			constant = resSet.getConstant(columnName);
		} else if (node instanceof Node_URI) {
			String uriString = ((Node_URI) node).getURI();
			constant = dfac.getURIConstant(uriString);
		} else if (node instanceof Node_Literal) {
			String value = ((Node_Literal) node).getLiteralValue().toString();
			constant = dfac.getValueConstant(value);
		} else if (node instanceof Node_Blank) {
			String label = ((Node_Blank) node).getBlankNodeLabel();
			constant = dfac.getBNodeConstant(label);
		}
		return constant;
	}

	@Override
	public void close() throws OBDAException {
		tupleResultSet.close();
	}

}
