package sesameWrapper;

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.io.*;
import java.net.URI;
import java.util.*;

import org.openrdf.model.Statement;
import org.openrdf.query.algebra.Create;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class SesameStatementIterator implements Iterator<Assertion> {
	

	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	private String RDF_TYPE_PREDICATE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private String currTriple = null;
	private String currSubject = null;
	private String currPredicate = null;
	private String currObject = null;
	private Predicate currentPredicate = null;
	
	private final Map<Predicate, Description> equivalenceMap;
	
	private Iterator<Statement> stmIterator = null;
	
		
	public SesameStatementIterator(Iterator<Statement> iterator)
	{
		this.equivalenceMap =  new HashMap<Predicate, Description>();
		this.stmIterator = iterator;
	}
	
		
	public boolean hasNext() {
		return stmIterator.hasNext();
	}

	
	public Assertion next() {
		
		if (!hasNext()) {
			throw new NoSuchElementException();
		}		

		Assertion assertion = constructAssertion(stmIterator.next());
		if (assertion == null && hasNext())
		{
			return next();
		}
		return assertion;
	}

	
	public void remove() {
		//assume current assertion removal
		stmIterator.remove();
	}


	/***
	 * Constructs an ABox assertion with the data from the current result set.
	 * 
	 * @return
	 */
	private Assertion constructAssertion(Statement st) {
		Assertion assertion = null;
		
		//System.out.println("Construct "+st.toString());
		
		currSubject = st.getSubject().stringValue();
		currPredicate = st.getPredicate().stringValue();
		currObject = st.getObject().stringValue();
		currTriple = currSubject+" "+currPredicate+" "+currObject;
		
		
		
		if (currPredicate.equals(RDF_TYPE_PREDICATE))
		{
			//System.out.println("ClassPredicate");
			//OWLClassAssertionAxiom assertn = (OWLClassAssertionAxiom) st;
			//OWLClassExpression classExpression = assertn.getClassExpression();
			//if (!(classExpression instanceof OWLClass) || classExpression.isOWLThing() || classExpression.isOWLNothing())
			
			if (!(currObject.endsWith("/owl#Thing")) || currObject.endsWith("/owl#Nothing") || currObject.endsWith("/owl#Ontology"))
				currentPredicate = obdafac.getClassPredicate(currObject);
			else
				return null;
		}
		else
			{
			if (st.getObject().toString().contains("\""))
				currentPredicate = obdafac.getDataPropertyPredicate(currPredicate);
			else
				currentPredicate = obdafac.getObjectPropertyPredicate(currPredicate);
				
			}
		
		Description replacementDescription = equivalenceMap.get(currentPredicate);
		
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		if (currentPredicate.getArity() == 1) {
			URIConstant c = obdafac.getURIConstant(URI.create(currSubject));
			if (replacementDescription == null) {
				assertion = ofac.createClassAssertion(currentPredicate, c);
			} else {
				OClass replacementc = (OClass) replacementDescription;
				assertion = ofac.createClassAssertion(replacementc.getPredicate(), c);
			}
		//	System.out.println("class assertion");
			
		} else if (currentPredicate.getType(1) == Predicate.COL_TYPE.OBJECT) {
			URIConstant c1 = obdafac.getURIConstant(URI.create(currSubject));
			URIConstant c2 = obdafac.getURIConstant(URI.create(currObject));
			if (replacementDescription == null) {
				assertion = ofac.createObjectPropertyAssertion(currentPredicate, c1, c2);
			} else {
				Property replacementp = (Property) replacementDescription;
				if (!replacementp.isInverse()) {
					assertion = ofac.createObjectPropertyAssertion(replacementp.getPredicate(), c1, c2);
				} else {
					assertion = ofac.createObjectPropertyAssertion(replacementp.getPredicate(), c2, c1);
				}
			}
			//System.out.println("object assertion");
			
		} else if (currentPredicate.getType(1) == Predicate.COL_TYPE.LITERAL) {
			URIConstant c1 = obdafac.getURIConstant(URI.create(currSubject));
			ValueConstant c2 = obdafac.getValueConstant(currObject);
			if (replacementDescription == null) {
				assertion = ofac.createDataPropertyAssertion(currentPredicate, c1, c2);
			} else {
				Property replacementp = (Property) replacementDescription;
				assertion = ofac.createDataPropertyAssertion(replacementp.getPredicate(), c1, c2);

			}
			//System.out.println("data assertion");
		} 
		else {
			throw new RuntimeException("ERROR, Wrongly type predicate: " + currentPredicate.toString());
		}
		return assertion;
	}

}
