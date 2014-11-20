package it.unibz.krdb.obda.owlrefplatform.core.abox;

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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class NTripleAssertionIterator implements Iterator<Assertion> {

	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	private final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private final int rdftype_hash = OBDAVocabulary.RDF_TYPE.hashCode();

	private String currSubject = null;
	private String currPredicate = null;
	private String currObject = null;
	private Predicate currentPredicate = null;

	private final BufferedReader input;

	public NTripleAssertionIterator(URI fileURI) throws IOException {
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

		if (currentPredicate.getArity() == 1) {
			URIConstant c = obdafac.getConstantURI(currSubject);
			OClass concept = ofac.createClass(currentPredicate.getName());
			assertion = ofac.createClassAssertion(concept, c);
		} 
		else if (currentPredicate.getType(1) == Predicate.COL_TYPE.OBJECT) {
			URIConstant c1 = obdafac.getConstantURI(currSubject);
			URIConstant c2 = obdafac.getConstantURI(currObject);
			ObjectPropertyExpression prop = ofac.createObjectProperty(currentPredicate.getName());
			assertion = ofac.createObjectPropertyAssertion(prop, c1, c2);
		} 
		else if (currentPredicate.getType(1) == Predicate.COL_TYPE.LITERAL) {
			URIConstant c1 = obdafac.getConstantURI(currSubject);
			ValueConstant c2 = obdafac.getConstantLiteral(currObject);
			DataPropertyExpression prop = ofac.createDataProperty(currentPredicate.getName());
			assertion = ofac.createDataPropertyAssertion(prop, c1, c2);
		} 
		else {
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

		StringBuilder triBuf = new StringBuilder();
		StringBuilder subBuf = new StringBuilder();
		StringBuilder preBuf = new StringBuilder();
		StringBuilder objBuf = new StringBuilder();
		
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
				//this.currTriple = triBuf.toString();

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

	private static boolean isWS(char c) {
		if (c == '\t' || c == ' ')
			return true;
		return false;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}
}
