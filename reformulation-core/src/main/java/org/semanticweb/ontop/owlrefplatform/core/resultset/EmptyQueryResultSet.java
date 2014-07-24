package org.semanticweb.ontop.owlrefplatform.core.resultset;

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

import java.net.URI;
import java.util.List;

import org.semanticweb.ontop.model.BNode;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAStatement;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.model.ValueConstant;

//import com.hp.hpl.jena.iri.IRI;

public class EmptyQueryResultSet implements TupleResultSet {

	List<String> head = null;
	private OBDAStatement st;

	public EmptyQueryResultSet(List<String> headvariables, OBDAStatement st) {
		this.head = headvariables;
		this.st = st;
	}

	@Override
	public void close() throws OBDAException {
	}

//	@Override
//	public double getDouble(int column) throws OBDAException {
//		return 0;
//	}
//
//	@Override
//	public int getInt(int column) throws OBDAException {
//		return 0;
//	}
//
//	@Override
//	public Object getObject(int column) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public String getString(int column) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public URI getURI(int column) throws OBDAException {
//		return null;
//	}
//	
//	@Override
//	public IRI getIRI(int column) throws OBDAException {
//		return null;
//	}

	@Override
	public int getColumnCount() throws OBDAException {
		return head.size();
	}

	@Override
	public int getFetchSize() throws OBDAException {
		return 0;
	}

	@Override
	public List<String> getSignature() throws OBDAException {
		return head;
	}

	@Override
	public boolean nextRow() throws OBDAException {
		return false;
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	@Override
	public Constant getConstant(int column) throws OBDAException {
		return null;
	}

//	@Override
//	public ValueConstant getLiteral(int column) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public BNode getBNode(int column) throws OBDAException {
//		return null;
//	}
//
	@Override
	public Constant getConstant(String name) throws OBDAException {
		return null;
	}
//
//	@Override
//	public URI getURI(String name) throws OBDAException {
//		return null;
//	}
//	
//	@Override
//	public IRI getIRI(String name) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public ValueConstant getLiteral(String name) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public BNode getBNode(String name) throws OBDAException {
//		return null;
//	}

	@Override
	public int getCountValue() throws OBDAException {
		return 0;
	}

}
