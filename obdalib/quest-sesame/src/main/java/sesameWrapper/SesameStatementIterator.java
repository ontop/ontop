package sesameWrapper;

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Semaphore;

import org.openrdf.model.Statement;
import org.openrdf.query.algebra.Create;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class SesameStatementIterator implements ListIterator<Assertion> {
	

	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	private String RDF_TYPE_PREDICATE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private String currTriple = null;
	private String currSubject = null;
	private String currPredicate = null;
	private String currObject = null;
	private Predicate currentPredicate = null;
	
	private final Map<Predicate, Description> equivalenceMap;
	
	private ListIterator<Statement> stmIterator = null;
	private Semaphore empty, full;
	private boolean hasNext = true, emptyb=true;
	
		
	public SesameStatementIterator(ListIterator<Statement> iterator, Semaphore empty, Semaphore full)
	{
		this.equivalenceMap =  new HashMap<Predicate, Description>();
		this.stmIterator = iterator;
		this.empty = empty;
		this.full = full;
	}
	
		
	public boolean hasNext() {
		
		if (!hasNext)
		{	//process what's left
			return stmIterator.hasNext();
		}
		else
		{
			//remain open for next
			//System.out.println("Wait while not empty...");
			try {
				
				empty.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//acquired semaphore, not empty anymore
			emptyb = false;
			return hasNext;
		}
	}
	
	protected void startRDF()
	{
		//keep stream of data open
		this.hasNext = true;
	}
	
	protected void endRDF()
	{
		//no more data will come
		//System.out.println("End rdf");
		this.hasNext = false;
		empty.release();
	}

	public Assertion next() {
		
		//if semaphore not acquired, do it
		if (emptyb)
			if(!hasNext()) {
			throw new NoSuchElementException();
		}		

		Statement statement = stmIterator.next();
		Assertion assertion = constructAssertion(statement);

		//Owl class assertion
		if (assertion == null)
		{
			remove();
			full.release();
			System.out.println("Remove "+statement.toString());
			//empty has to be acquired again
			emptyb = true;
			return next();
		}
		
		remove();
		full.release();
		System.out.println("Remove "+statement.toString());
		
	
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


	public void add(Assertion e) {
		// TODO Auto-generated method stub
		
	}


	public boolean hasPrevious() {
		// TODO Auto-generated method stub
		return false;
	}


	public int nextIndex() {
		// TODO Auto-generated method stub
		return 0;
	}


	public Assertion previous() {
		// TODO Auto-generated method stub
		return null;
	}


	public int previousIndex() {
		// TODO Auto-generated method stub
		return 0;
	}


	public void set(Assertion e) {
		// TODO Auto-generated method stub
		
	}


}
