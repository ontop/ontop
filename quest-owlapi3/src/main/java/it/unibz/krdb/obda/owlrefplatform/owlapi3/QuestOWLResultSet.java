package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.owlapi3.OWLAPI3IndividualTranslator;
import it.unibz.krdb.obda.owlapi3.OntopOWLException;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

/***
 * A wrapper for QuestResultSet that presents the results as OWLAPI objects.
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */
public class QuestOWLResultSet implements AutoCloseable {

	private final TupleResultSet res;

	private final QuestOWLStatement owlst;

	public QuestOWLResultSet(TupleResultSet res, QuestOWLStatement owlst) {
		if (res == null)
			throw new IllegalArgumentException("The result set must not be null");
		this.res = res;
		this.owlst = owlst;
	}

	public int getColumnCount() throws OWLException {
		try {
			return res.getColumnCount();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public List<String> getSignature() throws OWLException {
		try {
			return res.getSignature();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public int getFetchSize() throws OWLException {
		try {
			return res.getFetchSize();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public void close() throws OWLException {
		try {
			res.close();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}

	}

	public QuestOWLStatement getStatement() {
		return owlst;
	}

	public boolean nextRow() throws OWLException {
		try {
			return res.nextRow();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	private OWLAPI3IndividualTranslator translator = new OWLAPI3IndividualTranslator();
	
	private OWLPropertyAssertionObject translate(Constant c) {
		if (c instanceof ObjectConstant) 
			return translator.translate((ObjectConstant)c);
		else
			return translator.translate((ValueConstant)c);			
	}

	public OWLPropertyAssertionObject getOWLPropertyAssertionObject(int column) throws OWLException {
		try {
			return translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e + " Column: " + column);
		}
	}

	public OWLIndividual getOWLIndividual(int column) throws OWLException {
		try {
			return (OWLIndividual) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public OWLIndividual getOWLIndividual(String column) throws OWLException {
		try {
			return (OWLIndividual) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public OWLNamedIndividual getOWLNamedIndividual(int column) throws OWLException {
		try {
			return (OWLNamedIndividual) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public OWLAnonymousIndividual getOWLAnonymousIndividual(int column) throws OWLException {
		try {
			return (OWLAnonymousIndividual) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}


	public OWLLiteral getOWLLiteral(int column) throws OWLException {
		try {
			return (OWLLiteral) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public OWLLiteral getOWLLiteral(String column) throws OWLException {
		try {
			return (OWLLiteral) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public OWLObject getOWLObject(int column) throws OWLException {
		try {
			return (OWLObject) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public OWLObject getOWLObject(String column) throws OWLException {
		try {
			return (OWLObject) translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

}
