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

public class SesameStatementIterator extends RDFHandlerBase implements Iterator<Assertion> {
	

	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	private Predicate RDF_TYPE_PREDICATE = obdafac.getObjectPropertyPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

	private String currTriple = null;
	private String currSubject = null;
	private String currPredicate = null;
	private String currObject = null;
	private Predicate currentPredicate = null;
	
	private final Map<Predicate, Description> equivalenceMap;
	private final BufferedReader input = null;
	
	private List<Statement> aList = null;
	private int index=-1;
	
	public  SesameStatementIterator()
	{
		this.equivalenceMap =  new HashMap<Predicate, Description>();
		aList = new LinkedList<Statement>();
	}
	
	public SesameStatementIterator(List<Statement> list)
	{
		this.equivalenceMap =  new HashMap<Predicate, Description>();
		this.aList = list;
	}
	
	public SesameStatementIterator(Statement st)
	{
		this();
		add(st);
	}
	
	  @Override
	  public void handleStatement(Statement st) {
		  add(st);
	  }
	  
	  public int size()
	  {
		  if (aList!=null)
		  return aList.size();
		  return -1;
	  }
	  
	  public List getAssertionList()
	  {
		  return this.aList;
	  }
	 
	public void add(Statement s)
	{
		if (aList!=null)
		{
			aList.add(s);
		}
	}
	
	
	public boolean hasNext() {
		if (aList!=null)
			return aList.size()-1 > index;
			
		return false;
	}

	
	public Assertion next() {
		
		if (!hasNext()) {
			throw new NoSuchElementException();
		}		

		index++;
		Assertion assertion = constructAssertion(aList.get(index));
		if (assertion == null)
		{
			remove(index);
			return next();
		}
		return assertion;
	}

	
	public void remove() {
		//assume current assertion removal
		Assertion a = constructAssertion(aList.get(index));
		aList.remove(a);
		 index--;
	}

	public void remove(int ind)
	{
		aList.remove(ind);
		index--;
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
		
		
		
		if (currPredicate.endsWith("rdf-syntax-ns#type"))
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
