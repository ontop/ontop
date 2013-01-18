package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.iri.IRIFactory;

public class NTripleAssertionIterator implements Iterator<Assertion> {

	private final Map<Predicate, Description> equivalenceMap;
	private final URI fileURI;

	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();

	private String RDF_TYPE_STRING = OBDAVocabulary.RDF_TYPE;

	private int rdftype_hash = RDF_TYPE_STRING.hashCode();

	private Predicate RDF_TYPE_PREDICATE = obdafac.getObjectPropertyPredicate(RDF_TYPE_STRING);

	private String currTriple = null;
	private String currSubject = null;
	private String currPredicate = null;
	private String currObject = null;
	private Predicate currentPredicate = null;

	private final BufferedReader input;

	public NTripleAssertionIterator(URI fileURI, Map<Predicate, Description> equivalenceMap) throws IOException {
		this.fileURI = fileURI;
		this.equivalenceMap = equivalenceMap;

		FileReader freader = new FileReader(new File(fileURI));
		input = new BufferedReader(freader);
	}

	/***
	 * Constructs an ABox assertion with the data from the current result set.
	 * 
	 * @return
	 */
	private Assertion constructAssertion() {
		Assertion assertion = null;

		Description replacementDescription = equivalenceMap.get(currentPredicate);

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		if (currentPredicate.getArity() == 1) {
			URIConstant c = obdafac.getURIConstant(OBDADataFactoryImpl.getIRI(currSubject));
			if (replacementDescription == null) {
				assertion = ofac.createClassAssertion(currentPredicate, c);
			} else {
				OClass replacementc = (OClass) replacementDescription;
				assertion = ofac.createClassAssertion(replacementc.getPredicate(), c);
			}
		} else if (currentPredicate.getType(1) == Predicate.COL_TYPE.OBJECT) {
			URIConstant c1 = obdafac.getURIConstant(OBDADataFactoryImpl.getIRI(currSubject));
			URIConstant c2 = obdafac.getURIConstant(OBDADataFactoryImpl.getIRI(currObject));
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
		} else if (currentPredicate.getType(1) == Predicate.COL_TYPE.LITERAL) {
			URIConstant c1 = obdafac.getURIConstant(OBDADataFactoryImpl.getIRI(currSubject));
			ValueConstant c2 = obdafac.getValueConstant(currObject);
			if (replacementDescription == null) {
				assertion = ofac.createDataPropertyAssertion(currentPredicate, c1, c2);
			} else {
				Property replacementp = (Property) replacementDescription;
				assertion = ofac.createDataPropertyAssertion(replacementp.getPredicate(), c1, c2);

			}
		} else {
			throw new RuntimeException("ERROR, Wrongly type predicate: " + currentPredicate.toString());
		}
		return assertion;
	}

	private boolean peeked = false;
	private boolean hasNext = false;

	@Override
	public boolean hasNext() {
		if (peeked)
			return hasNext;

		peeked = true;
		try {
			hasNext = readStatement();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return hasNext;

	}

	@Override
	public Assertion next() {

		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		peeked = false;

		Assertion assertion = constructAssertion();
		return assertion;
	}

	/***
	 * Reads one single statement
	 */

	private boolean readStatement() throws IOException {
		boolean staReady = false;

		/*
		 * flags indicating we are now processing a subject, pred or object, or
		 * none (white space)
		 */
		boolean inSub = false;
		boolean inPre = false;
		boolean inObj = false;

		/*
		 * flags indicating we are now processing a subject, pred or object, or
		 * none (white space)
		 */
		boolean subReady = false;
		boolean preReady = false;
		boolean objReady = false;

		boolean isObjectProperty = false;

		StringBuffer triBuf = new StringBuffer();
		StringBuffer subBuf = new StringBuffer();
		StringBuffer preBuf = new StringBuffer();
		StringBuffer objBuf = new StringBuffer();
		
		boolean terminating = false;

		while (!staReady) {

			int cint = input.read();

			if (cint == -1) {
				return false;
			}

			char c = (char) cint;
			triBuf.append(c);


			if (!inSub && !inPre && !inObj && isWS(c)) {
				continue;
			}

			if (!subReady && !inSub && c == '<') {
				inSub = true;
			} else if (!subReady && inSub && c == '>') {
				inSub = false;
				subReady = true;
			} else if (!subReady && inSub && c != '>') {
				subBuf.append(c);
			} else if (!preReady && !inPre && c == '<') {
				inPre = true;
			} else if (!preReady && inPre && c == '>') {
				inPre = false;
				preReady = true;
			} else if (!preReady && inPre && c != '>') {
				preBuf.append(c);
			} else if (!objReady && !inObj && (c == '<' || c == '\"')) {
				if (c == '<') {
					isObjectProperty = true;
				}
				inObj = true;
			} else if (!objReady && inObj && (c == '>' || c == '\"')) {
				inObj = false;
				objReady = true;
			} else if (!objReady && inObj && (c != '>' && c != '\"')) {
				objBuf.append(c);
			} else if (c == '.') {
				terminating = true;
			} else if (c == '\n' && terminating) {
				this.currSubject = subBuf.toString();
				this.currPredicate = preBuf.toString();
				this.currObject = objBuf.toString();
				this.currTriple = triBuf.toString();

				if (currPredicate.hashCode() == rdftype_hash) {
					currentPredicate = obdafac.getClassPredicate(currObject);
				} else {
					if (isObjectProperty) {
						currentPredicate = obdafac.getObjectPropertyPredicate(currPredicate);
					} else {
						currentPredicate = obdafac.getDataPropertyPredicate(currPredicate);
					}
				}
				staReady = true;
			} else {
				return false;
			}
		}
		return staReady;
	}

	private boolean isWS(char c) {
		if (c == '\t' || c == ' ')
			return true;
		return false;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}
}
