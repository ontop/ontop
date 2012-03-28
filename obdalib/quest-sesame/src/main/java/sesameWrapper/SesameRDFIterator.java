package sesameWrapper;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class SesameRDFIterator extends RDFHandlerBase implements Iterator<Assertion> {
	

	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	private String RDF_TYPE_PREDICATE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private String currTriple = null;
	private String currSubject = null;
	private String currPredicate = null;
	private String currObject = null;
	private Predicate currentPredicate = null;
	
	private final Map<Predicate, Description> equivalenceMap;

	private BlockingQueue<Statement> buffer;
	private  Iterator<Statement> iterator;
	private int size = 1;
	private boolean fromIterator = false, endRdf = false;
	private Semaphore empty;
	private ValueFactory fact = ValueFactoryImpl.getInstance();
	private Statement stm = fact.createStatement(null, null, null), stmt=null;
		

	public SesameRDFIterator() {
		
		this.equivalenceMap =  new HashMap<Predicate, Description>();
		buffer = new ArrayBlockingQueue<Statement>(size, true);
	}
	
	public SesameRDFIterator(Iterator<Statement> it) {
		
		this.equivalenceMap =  new HashMap<Predicate, Description>();
		this.iterator = it;
		this.fromIterator = true;
	}
	
	
	public void startRDF()
		throws RDFHandlerException
	{
		endRdf =false;
	}

	public void endRDF()
		throws RDFHandlerException
	{
		endRdf = true;
		try {
			buffer.put(stm);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("End rdf");
	}

	
	
	
	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{

		//add to buffer 
		System.out.print("Handle statement: "+st.getSubject().toString().split("#")[1] + " " 
				+st.getPredicate().getLocalName() + " ");
		if (st.getObject().toString().split("#").length > 1)
			System.out.println(st.getObject().toString().split("#")[1]);
		else
			System.out.println(st.getObject().toString().split("#")[0]);
			//add statement to buffer
			try {
				//add to the tail of the queue
				{
				buffer.put(st);
				
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


	}
	

	public boolean hasNext() {
		if (fromIterator)
			return iterator.hasNext();
		else {
		//check if head is null
		if (endRdf) //end has been signaled
		{
			//no more data to come, process what's left
			return (buffer.peek() != null);
		}
		else
		{
			try {
				if (((stmt = buffer.take()).equals(stm)))
				{
					return false;
				}
				else 
				{
					return true;
				}
					
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return buffer.peek()!=null;
		}
		}
	}

	public Assertion next() {
			
		
		if (stmt == null)
			if (!hasNext())
			throw new NoSuchElementException();
		
		Statement statement = null;
		if (fromIterator)
			 statement = iterator.next();
		else
		{
				statement = stmt;
		}
		Assertion assertion = constructAssertion(statement);

		//Owl class assertion
		if (assertion == null)
		{
			if (hasNext())
			return next();
		}
	
		return assertion;
	}

	
	

	/***
	 * Constructs an ABox assertion with the data from the current result set.
	 * 
	 * @return
	 */
	private Assertion constructAssertion(Statement st) {
		Assertion assertion = null;
		
		if (st==null) return null;
		
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
			URIConstant c = obdafac.getURIConstant(java.net.URI.create(currSubject));
			if (replacementDescription == null) {
				assertion = ofac.createClassAssertion(currentPredicate, c);
			} else {
				OClass replacementc = (OClass) replacementDescription;
				assertion = ofac.createClassAssertion(replacementc.getPredicate(), c);
			}
		//	System.out.println("class assertion");
			
		} else if (currentPredicate.getType(1) == Predicate.COL_TYPE.OBJECT) {
			URIConstant c1 = obdafac.getURIConstant(java.net.URI.create(currSubject));
			URIConstant c2 = obdafac.getURIConstant(java.net.URI.create(currObject));
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
			URIConstant c1 = obdafac.getURIConstant(java.net.URI.create(currSubject));
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


	public void remove() {
		//remove head
		buffer.poll();
	}

}
