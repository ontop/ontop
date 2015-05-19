package it.unibz.krdb.obda.model.impl;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

/**
 * Implementation for BNodes.
 */
public class BNodeConstantImpl implements BNode {

	private static final long serialVersionUID = 214867118996974157L;

	private final String name;

	private final int identifier;

	/**
	 * The default constructor.
	 * 
	 * @param uri
	 *            URI from a term.
	 */
	protected BNodeConstantImpl(String name) {
		this.name = name;
		this.identifier = name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof BNodeConstantImpl)) {
			return false;
		}
		BNodeConstantImpl uri2 = (BNodeConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getValue() {
		return name;
	}

	@Override
	public BNode clone() {
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public COL_TYPE getType() {
		return COL_TYPE.BNODE;
	}

}
