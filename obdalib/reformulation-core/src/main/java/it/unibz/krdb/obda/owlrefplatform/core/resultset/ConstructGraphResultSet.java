package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.Template;

public class ConstructGraphResultSet implements GraphResultSet {

	private OBDAResultSet tupleResultSet;
	
	private Template constructTemplate;

	private OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public ConstructGraphResultSet(OBDAResultSet results, Template template) throws OBDAException {
		tupleResultSet = results;
		constructTemplate = template;
	}

	@Override
	public boolean hasNext() throws OBDAException {
		return tupleResultSet.nextRow();
	}

	@Override
	public List<Assertion> next() throws OBDAException {
		List<Assertion> tripleAssertions = new ArrayList<Assertion>();
		for (Triple triple : constructTemplate.getTriples()) {
			Constant subjectConstant = getConstant(triple.getSubject());
			Constant predicateConstant = getConstant(triple.getPredicate());
			Constant objectConstant = getConstant(triple.getObject());
			
			// Determines the type of assertion
			String predicateName = predicateConstant.getValue();
			if (predicateName.equals(OBDAVocabulary.RDF_TYPE)) {
				Predicate concept = dfac.getClassPredicate(objectConstant.getValue());
				ClassAssertion ca = ofac.createClassAssertion(
						concept,
						(ObjectConstant) subjectConstant);
				tripleAssertions.add(ca);
			} else {
				if (objectConstant instanceof URIConstant) {
					Predicate role = dfac.getObjectPropertyPredicate(predicateName);
					ObjectPropertyAssertion op = ofac.createObjectPropertyAssertion(
							role, 
							(ObjectConstant) subjectConstant, 
							(ObjectConstant) objectConstant);
					tripleAssertions.add(op);
				} else if (objectConstant instanceof BNodeConstantImpl)
				{
					Predicate role = dfac.getObjectPropertyPredicate(predicateName);
					ObjectPropertyAssertion op = ofac.createObjectPropertyAssertion(
							role, 
							(ObjectConstant) subjectConstant, 
							(ObjectConstant) objectConstant);
					tripleAssertions.add(op);
				}else {
					Predicate attribute = dfac.getDataPropertyPredicate(predicateName);
					DataPropertyAssertion dp = ofac.createDataPropertyAssertion(
							attribute, 
							(ObjectConstant) subjectConstant, 
							(ValueConstant) objectConstant);
					tripleAssertions.add(dp);
				}
			}
		}
		return tripleAssertions;
	}
	
	private Constant getConstant(Node node) throws OBDAException {
		Constant constant = null;
		if (node instanceof Node_Variable) {
			String columnName = ((Node_Variable) node).getName();
			constant = tupleResultSet.getConstant(columnName);
		} else if (node instanceof Node_URI) {
			String uriString = ((Node_URI) node).getURI();
			constant = dfac.getURIConstant(URI.create(uriString));
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
