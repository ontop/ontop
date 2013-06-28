package it.unibz.krdb.obda.sesame;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class SesameRDFIterator extends RDFHandlerBase implements
		Iterator<Assertion> {

	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	private String RDF_TYPE_PREDICATE = OBDAVocabulary.RDF_TYPE;

	private BlockingQueue<Statement> buffer;
	private Iterator<Statement> iterator;
	private int size = 1;
	private boolean fromIterator = false, endRdf = false;

	private Statement stmt = null;

	public SesameRDFIterator() {
		buffer = new ArrayBlockingQueue<Statement>(size, true);
	}

	public SesameRDFIterator(Iterator<Statement> it) {
		this.iterator = it;
		this.fromIterator = true;
	}

	public void startRDF() throws RDFHandlerException {
		endRdf = false;
	}

	public void endRDF() throws RDFHandlerException {
		endRdf = true;
//		try {
//			//buffer.put(stm);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		//System.out.println("End rdf");
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {

		// add to buffer
	//	System.out.println("Handle statement: "+st.toString());
//				+ st.getSubject().toString().split("#")[1] + " "
//				+ st.getPredicate().getLocalName() + " ");
//		if (st.getObject().toString().split("#").length > 1)
//			System.out.println(st.getObject().toString().split("#")[1]);
//		else
//			System.out.println(st.getObject().toString().split("#")[0]);
		// add statement to buffer
		try {
			// add to the tail of the queue
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
			// check if head is null

			
				//try {
					
					//if (((stmt = buffer.take()).equals(stm))) {
					if (endRdf && buffer.isEmpty()) {
						return false;
					} else {
						return true;
					}

//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				return buffer.peek() != null;
			
		}
	}

	public Assertion next() {

		if (stmt == null)
			if (!hasNext())
				throw new NoSuchElementException();

		Statement statement = null;
		if (fromIterator)
			statement = iterator.next();
		else {
			statement = stmt;
		}
		Assertion assertion = constructAssertion(statement);

		// Owl class assertion
		if (assertion == null) {
			if (hasNext())
				return next();
		}

		return assertion;
	}

	/***
	 * Constructs an ABox assertion with the data from the current result set.
	 * This can be a Class, Object or Data Property assertion. It is a class
	 * assertion if the predicate is rdf:type. Its an Object property if the
	 * predicate is not type and the object is URI or BNode. Its a data property
	 * if the predicate is not rdf:type and the object is a Literal.
	 * 
	 * @return
	 */
	private Assertion constructAssertion(Statement st) {
		Assertion assertion = null;

		if (st == null)
			return null;

		Predicate currentPredicate = null;

		Resource currSubject = st.getSubject();
		ObjectConstant c;
		if (currSubject instanceof URI)
			c = obdafac.getURIConstant(OBDADataFactoryImpl.getIRI(currSubject
					.stringValue()));
		else if (currSubject instanceof BNode) {
			c = obdafac.getBNodeConstant(currSubject.stringValue());
		} else {
			throw new RuntimeException(
					"Unsupported subject found in triple. Not URI or BNode. Statement: "
							+ st.toString());
		}

		URI currPredicate = st.getPredicate();
		Value currObject = st.getObject();

		if (currObject instanceof Literal) {
			currentPredicate = obdafac.getDataPropertyPredicate(currPredicate
					.stringValue());
		} else {
			String predStringValue = currPredicate.stringValue();
			if (predStringValue.equals(RDF_TYPE_PREDICATE)) {
				if (!(predStringValue.endsWith("/owl#Thing"))
						|| predStringValue.endsWith("/owl#Nothing")
						|| predStringValue.endsWith("/owl#Ontology"))
					currentPredicate = obdafac.getClassPredicate(currObject
							.stringValue());
				else
					return null;
			} else
				currentPredicate = obdafac
						.getObjectPropertyPredicate(currPredicate.stringValue());

		}

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		if (currentPredicate.getArity() == 1) {

			assertion = ofac.createClassAssertion(currentPredicate, c);

		} else {

			Constant c2;
			if (currObject instanceof URI)
				c2 = obdafac.getURIConstant(OBDADataFactoryImpl.getIRI(currObject
						.stringValue()));
			else if (currObject instanceof BNode) {
				c2 = obdafac.getBNodeConstant(currObject.stringValue());
			} else if (currObject instanceof Literal) {
				Literal l = (Literal) currObject;
				Predicate.COL_TYPE type = getColumnType(l.getDatatype());
				String lang = l.getLanguage();

				if (lang == null)
					c2 = obdafac.getValueConstant(l.getLabel(), type);
				else
					c2 = obdafac.getValueConstant(l.getLabel(), lang);
			} else {
				throw new RuntimeException(
						"Unsupported object found in triple. Not URI, BNode or Literal. Statement: "
								+ st.toString());
			}

			assertion = ofac.createPropertyAssertion(currentPredicate, c, c2);
		}

		return assertion;
	}

	private Predicate.COL_TYPE getColumnType(URI datatype) {
		if (datatype == null) {
			return Predicate.COL_TYPE.LITERAL;
		} else if (datatype.stringValue().equals(OBDAVocabulary.XSD_STRING_URI)) {
			return Predicate.COL_TYPE.STRING;
		} else if (datatype.stringValue().equals(
				OBDAVocabulary.RDFS_LITERAL_URI)) {
			return Predicate.COL_TYPE.LITERAL;
		} else if (datatype.stringValue()
				.equals(OBDAVocabulary.XSD_INTEGER_URI)) {
			return Predicate.COL_TYPE.INTEGER;
		} else if (datatype.stringValue()
				.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
			return Predicate.COL_TYPE.DECIMAL;
		} else if (datatype.stringValue().equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
			return Predicate.COL_TYPE.DOUBLE;
		} else if (datatype.stringValue().equals(
				OBDAVocabulary.XSD_DATETIME_URI)) {
			return Predicate.COL_TYPE.DATETIME;
		} else if (datatype.stringValue()
				.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
			return Predicate.COL_TYPE.BOOLEAN;
		}
		return Predicate.COL_TYPE.UNSUPPORTED;

	}

	public void remove() {
		// remove head
		buffer.poll();
	}

}
