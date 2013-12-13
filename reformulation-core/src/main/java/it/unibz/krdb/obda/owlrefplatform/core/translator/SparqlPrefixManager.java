package it.unibz.krdb.obda.owlrefplatform.core.translator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.io.AbstractPrefixManager;

import java.util.List;
import java.util.Map;

//import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * A read-only prefix manager that wraps <code>PrefixMapping</code> from Jena-API
 * 
 * @see com.hp.hpl.jena.shared.PrefixMapping
 */
public class SparqlPrefixManager extends AbstractPrefixManager {


	public SparqlPrefixManager() {
		// NO-OP
		// TODO Implement using Sesame prefix manager
	}

	@Override
	public void addPrefix(String prefix, String uri) {
		throw new UnsupportedOperationException("This is a read-only prefix manager. Addition operation is not permitted");
	}

	@Override
	public String getURIDefinition(String prefix) {
		return ""; // TODO Implement using Sesame prefix manager
	}

	@Override
	public String getPrefix(String uri) {
		return ""; // TODO Implement using Sesame prefix manager
	}

	@Override
	public Map<String, String> getPrefixMap() {
		return null; // TODO Implement using Sesame prefix manager
	}

	@Override
	public boolean contains(String prefix) {
		return false; // TODO Implement using Sesame prefix manager
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("This is a read-only prefix manager. Clearing operation is not permitted");
	}

	@Override
	public List<String> getNamespaceList() {
		return null; // TODO Implement using Sesame prefix manager
	}
}
